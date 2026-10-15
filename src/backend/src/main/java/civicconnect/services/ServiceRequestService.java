package civicconnect.services;

import civicconnect.api.dto.CommentDto;
import civicconnect.api.dto.CreateCommentBody;
import civicconnect.api.dto.CreateRequestBody;
import civicconnect.api.dto.RequestResponse;
import civicconnect.api.dto.ServiceRequestDetailDto;
import civicconnect.api.dto.ServiceRequestResponse;
import civicconnect.api.dto.StatusHistoryDto;
import civicconnect.observers.AuditLogObserver;
import civicconnect.records.Comment;
import civicconnect.records.RequestCategory;
import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.CommentRepository;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.StatusHistoryRepository;
import civicconnect.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final CommentRepository commentRepository;
    private final RequestListService requestListService;
    private final StatusTransitionGuard statusTransitionGuard;
    private final AuditLogObserver auditLogObserver;

    public ServiceRequestService(
            ServiceRequestRepository requestRepository,
            UserRepository userRepository,
            StatusHistoryRepository statusHistoryRepository,
            CommentRepository commentRepository,
            RequestListService requestListService,
            StatusTransitionGuard statusTransitionGuard,
            AuditLogObserver auditLogObserver
    ) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.commentRepository = commentRepository;
        this.requestListService = requestListService;
        this.statusTransitionGuard = statusTransitionGuard;
        this.auditLogObserver = auditLogObserver;
    }

    @Transactional
    public RequestResponse create(String submitterEmail, CreateRequestBody body) {
        RequestCategory category;
        try {
            category = RequestCategory.fromLabel(body.category());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a category from the list.");
        }

        User submitter = requireUser(submitterEmail);

        ServiceRequest request = new ServiceRequest();
        request.setTitle(body.title());
        request.setDescription(body.description());
        request.setCategory(category.getLabel());
        request.setStatus(RequestStatus.Open);
        request.setSubmittedBy(submitter);

        return RequestResponse.from(requestRepository.save(request));
    }

    /**
     * Lists the requests submitted by the signed in user (FR003).
     * The user comes from the session, never from a value the browser sends (NFR003).
     */
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getRequestsForRequester(String requesterEmail) {
        User requester = requireUser(requesterEmail);

        return requestRepository.findBySubmittedById(requester.getId()).stream()
                .sorted(Comparator.comparing(ServiceRequest::getCreatedAt, Comparator.reverseOrder()))
                .map(ServiceRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceRequestDetailDto get(String email, Long requestId) {
        User user = requireUser(email);
        return toDetail(requireVisible(user, requestId));
    }

    @Transactional
    public ServiceRequestDetailDto accept(String email, Long requestId, Integer expectedVersion) {
        User user = requireUser(email);
        if (user.getRole() != Role.Staff) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only staff can accept a request.");
        }

        ServiceRequest request = requireVisible(user, requestId);
        checkVersion(request, expectedVersion);

        if (request.getStatus() != RequestStatus.Open) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A request can only be accepted while it is Open."
            );
        }

        if (!statusTransitionGuard.canPerformTransition(
                RequestStatus.Open, RequestStatus.Assigned, user.getRole().name(), false)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to accept this request.");
        }

        RequestStatus from = request.getStatus();
        request.setAssignedTo(user);
        request.setStatus(RequestStatus.Assigned);
        request.setUpdatedAt(Instant.now());
        requestRepository.saveAndFlush(request);
        auditLogObserver.record(request, from, RequestStatus.Assigned, user, null);
        return toDetail(request);
    }

    @Transactional
    public ServiceRequestDetailDto updateStatus(String email, Long requestId, RequestStatus toStatus, Integer expectedVersion) {
        User user = requireUser(email);
        if (user.getRole() != Role.Staff && user.getRole() != Role.Management) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to change this request.");
        }

        ServiceRequest request = requireVisible(user, requestId);
        checkVersion(request, expectedVersion);

        if (toStatus == RequestStatus.Assigned) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Accept the request to assign it to yourself."
            );
        }

        RequestStatus from = request.getStatus();
        if (!statusTransitionGuard.isLegalTransition(from, toStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, illegalTransitionMessage(from, toStatus));
        }

        boolean isAssignedToUser = request.getAssignedTo() != null
                && user.getId().equals(request.getAssignedTo().getId());
        if (!statusTransitionGuard.canPerformTransition(from, toStatus, user.getRole().name(), isAssignedToUser)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the staff member assigned to this request can update its status."
            );
        }

        request.setStatus(toStatus);
        request.setUpdatedAt(Instant.now());
        requestRepository.saveAndFlush(request);
        auditLogObserver.record(request, from, toStatus, user, null);
        return toDetail(request);
    }

    @Transactional
    public ServiceRequestDetailDto addComment(String email, Long requestId, CreateCommentBody body) {
        User user = requireUser(email);
        if (user.getRole() != Role.Staff && user.getRole() != Role.Management) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only staff can add a comment on a request.");
        }

        ServiceRequest request = requireVisible(user, requestId);

        Comment comment = new Comment();
        comment.setRequest(request);
        comment.setAuthor(user);
        comment.setContent(body.content());
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);
        return toDetail(request);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in"));
    }

    private ServiceRequest requireVisible(User user, Long requestId) {
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
        if (!requestListService.canView(user, request)) {
            // 403 matches RequestListService.getFor / NFR003 ASR-01 (not a silent 404).
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You may not view this request.");
        }
        return request;
    }

    private void checkVersion(ServiceRequest request, Integer expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(request.getVersion())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This request was updated by someone else. Reload and try again."
            );
        }
    }

    private String illegalTransitionMessage(RequestStatus from, RequestStatus to) {
        if (from == RequestStatus.Closed) {
            return "This request is already closed.";
        }
        RequestStatus next = statusTransitionGuard.nextStatus(from);
        String nextLabel = next == null ? "none" : displayStatus(next);
        return "Cannot change status from " + displayStatus(from) + " to " + displayStatus(to)
                + ". The next status is " + nextLabel + ".";
    }

    private static String displayStatus(RequestStatus status) {
        if (status == RequestStatus.InProgress) {
            return "In Progress";
        }
        return status.name();
    }

    private ServiceRequestDetailDto toDetail(ServiceRequest request) {
        List<StatusHistoryDto> history = statusHistoryRepository
                .findByRequestIdOrderByChangedAtAsc(request.getId())
                .stream()
                .map(StatusHistoryDto::from)
                .toList();
        List<CommentDto> comments = commentRepository
                .findByRequestIdOrderByCreatedAtAsc(request.getId())
                .stream()
                .map(CommentDto::from)
                .toList();
        return ServiceRequestDetailDto.from(request, history, comments);
    }
}
