
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
 *
 * Permission is checked before the version, and the version is checked
 * before transition legality. This ensures forbidden users receive 403
 * and permitted users with stale data receive 409.
 *
 * The update is flushed before the audit row is written, following
 * ADR-DATA-001. A failed update must not produce an audit entry.
 */
@Service
public class RequestStatusService {

    static final String CONFLICT_MESSAGE =
            "This request was changed by someone else. Reload and try again.";

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
    public StatusChangeResponse changeStatus(
            String actorEmail,
            Long requestId,
            RequestStatus to,
            Integer expectedVersion,
            String note
    ) {
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Not signed in"));

        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Request not found"));

        RequestStatus from = request.getStatus();

        boolean assignedToActor = request.getAssignedTo() != null
                && Objects.equals(
                request.getAssignedTo().getId(),
                actor.getId());

        StatusTransitionGuard.Decision decision =
                guard.evaluate(from, to, actor.getRole(), assignedToActor);

        // 1. Permission takes precedence over version and transition checks.
        if (decision == StatusTransitionGuard.Decision.FORBIDDEN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to make this change.");
        }

        // 2. Reject stale data before checking whether the transition is legal.
        // A permitted user with an outdated version must receive HTTP 409,
        // even when the request's current status makes their transition illegal.
        if (expectedVersion != null
                && !expectedVersion.equals(request.getVersion())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    CONFLICT_MESSAGE);
        }

        // 3. The version is current, so transition legality can be checked.
        if (decision == StatusTransitionGuard.Decision.ILLEGAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A request cannot move from " + from + " to " + to + ".");
        }

        // Assignment requires an assignee and is handled by the assign action (FR008).
        if (to == RequestStatus.Assigned) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use the assign action to take or assign a request.");
        }

        request.setStatus(to);
        request.setUpdatedAt(Instant.now());

        // Flush first so persistence/version failures occur before audit recording.
        ServiceRequest saved = requestRepository.saveAndFlush(request);

        // Record the audit entry only after the update has successfully flushed.
        auditLogObserver.record(saved, from, to, actor, note);

        return new StatusChangeResponse(
                saved.getId(),
                saved.getStatus().name(),
                saved.getVersion());
    }
}
