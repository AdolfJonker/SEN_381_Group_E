package civicconnect.services;

import civicconnect.api.dto.StatusChangeResponse;
import civicconnect.observers.AuditLogObserver;
import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Objects;

/**
 * Changes a request's status (FR009, FR011, NFR004).
 * Order follows ADR-DATA-001: guard, conditional update (flush), audit row, commit.
 * Any failure rolls everything back, so a rejected change writes nothing.
 */
@Service
public class RequestStatusService {

    static final String CONFLICT_MESSAGE = "This request was changed by someone else. Reload and try again.";

    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final StatusTransitionGuard guard;
    private final AuditLogObserver auditLogObserver;

    public RequestStatusService(
            ServiceRequestRepository requestRepository,
            UserRepository userRepository,
            StatusTransitionGuard guard,
            AuditLogObserver auditLogObserver
    ) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.guard = guard;
        this.auditLogObserver = auditLogObserver;
    }

    @Transactional
    public StatusChangeResponse changeStatus(String actorEmail,
                                             Long requestId,
                                             RequestStatus to,
                                             Integer expectedVersion,
                                             String note) {
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in"));
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        RequestStatus from = request.getStatus();
        boolean assignedToActor = request.getAssignedTo() != null
                && Objects.equals(request.getAssignedTo().getId(), actor.getId());

        StatusTransitionGuard.Decision decision = guard.evaluate(from, to, actor.getRole(), assignedToActor);
        if (decision == StatusTransitionGuard.Decision.FORBIDDEN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to make this change.");
        }
        if (decision == StatusTransitionGuard.Decision.ILLEGAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A request cannot move from " + from + " to " + to + ".");
        }

        // Assigning needs an assignee, so it is handled by the assign action (FR008), not here.
        if (to == RequestStatus.Assigned) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Use the assign action to take or assign a request.");
        }

        if (expectedVersion != null && !expectedVersion.equals(request.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, CONFLICT_MESSAGE);
        }

        request.setStatus(to);
        request.setUpdatedAt(Instant.now());
        // Flush now so the version check runs before the audit row is written.
        ServiceRequest saved = requestRepository.saveAndFlush(request);
        auditLogObserver.record(saved, from, to, actor, note);
        // comment out the auditLogObserver line above tp prove a test can fail
        return new StatusChangeResponse(saved.getId(), saved.getStatus().name(), saved.getVersion());
    }
}
