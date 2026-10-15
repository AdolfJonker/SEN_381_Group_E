package civicconnect.services;

import civicconnect.api.dto.ServiceRequestDetailDto;
import civicconnect.observers.AuditLogObserver;
import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.StatusHistory;
import civicconnect.records.User;
import civicconnect.repositories.CommentRepository;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.StatusHistoryRepository;
import civicconnect.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for accept / status updates (optimistic locking, history, detail deny).
 * IDs use TC-A-* (Adolf). Elcke's evidence cases remain TC-E-*.
 * Mapping: A-01/04/05/07 → FR008/RTM-008; A-02/03 → FR009/RTM-009; A-06 → FR007/RTM-007.
 * None of these IDs claim FR005/RTM-005 (access-control owns list scope).
 * No Spring context and no database.
 */
@ExtendWith(MockitoExtension.class)
class ServiceRequestLifecycleTest {

    private static final String STAFF_EMAIL = "staff@civicconnect.local";
    private static final String OTHER_STAFF_EMAIL = "other.staff@civicconnect.local";
    private static final String REQUESTER_EMAIL = "requester@civicconnect.local";

    @Mock
    private ServiceRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RequestListService requestListService;

    private ServiceRequestService service;

    @BeforeEach
    void setUp() {
        // Real observer so history assertions still go through StatusHistoryRepository.
        AuditLogObserver auditLogObserver = new AuditLogObserver(statusHistoryRepository);
        service = new ServiceRequestService(
                requestRepository,
                userRepository,
                statusHistoryRepository,
                commentRepository,
                requestListService,
                new StatusTransitionGuard(),
                auditLogObserver
        );
    }

    @DisplayName("TC-A-01 (FR008/RTM-008) Accept returns the flushed version that matches the stored entity")
    @Test
    void acceptReturnsFlushedVersion() {
        User staff = user(1L, Role.Staff, STAFF_EMAIL, "Staff One");
        ServiceRequest request = openRequest(5L, staff);
        request.setVersion(0);
        stubVisibleStaff(staff, request);
        stubFlushIncrementsVersion();
        stubEmptyDetailLists(5L);

        ServiceRequestDetailDto detail = service.accept(STAFF_EMAIL, 5L, 0);

        assertEquals(1, request.getVersion());
        assertEquals(request.getVersion(), detail.version());
        assertEquals(RequestStatus.Assigned, detail.status());
        verify(requestRepository).saveAndFlush(request);
    }

    @DisplayName("TC-A-02 (FR009/RTM-009) updateStatus returns the flushed version that matches the stored entity")
    @Test
    void updateStatusReturnsFlushedVersion() {
        User staff = user(1L, Role.Staff, STAFF_EMAIL, "Staff One");
        ServiceRequest request = assignedRequest(5L, staff);
        request.setVersion(1);
        stubVisibleStaff(staff, request);
        stubFlushIncrementsVersion();
        stubEmptyDetailLists(5L);

        ServiceRequestDetailDto detail = service.updateStatus(
                STAFF_EMAIL, 5L, RequestStatus.InProgress, 1);

        assertEquals(2, request.getVersion());
        assertEquals(request.getVersion(), detail.version());
        assertEquals(RequestStatus.InProgress, detail.status());
        verify(requestRepository).saveAndFlush(request);
    }

    @DisplayName("TC-A-03 (FR009/RTM-009) Illegal status move returns 400 and writes zero history rows (D3)")
    @Test
    void illegalMoveWritesZeroHistoryRows() {
        User staff = user(1L, Role.Staff, STAFF_EMAIL, "Staff One");
        ServiceRequest request = openRequest(5L, staff);
        request.setVersion(0);
        stubVisibleStaff(staff, request);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(STAFF_EMAIL, 5L, RequestStatus.Closed, 0)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(statusHistoryRepository, never()).save(any(StatusHistory.class));
        verify(requestRepository, never()).saveAndFlush(any());
        assertEquals(RequestStatus.Open, request.getStatus());
        assertEquals(0, request.getVersion());
    }

    @DisplayName("TC-A-04 (FR008/RTM-008, ADR-DATA-001) Stale version returns 409 and writes nothing")
    @Test
    void staleVersionReturnsConflict() {
        User staff = user(1L, Role.Staff, STAFF_EMAIL, "Staff One");
        ServiceRequest request = openRequest(5L, staff);
        request.setVersion(1);
        stubVisibleStaff(staff, request);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.accept(STAFF_EMAIL, 5L, 0)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(statusHistoryRepository, never()).save(any(StatusHistory.class));
        verify(requestRepository, never()).saveAndFlush(any());
    }

    @DisplayName("TC-A-05 (FR008/RTM-008, NFR003) Requester cannot accept a request")
    @Test
    void requesterCannotAccept() {
        User requester = user(7L, Role.Requester, REQUESTER_EMAIL, "Requester");
        when(userRepository.findByEmail(REQUESTER_EMAIL)).thenReturn(Optional.of(requester));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.accept(REQUESTER_EMAIL, 5L, 0)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(requestRepository, never()).findById(any());
        verify(statusHistoryRepository, never()).save(any(StatusHistory.class));
    }

    @DisplayName("TC-A-06 (FR007/RTM-007, NFR003 — not RTM-005) Staff cannot get another staff member's assigned request (403)")
    @Test
    void staffCannotSeeAnotherStaffMembersAssignedRequest() {
        User staff = user(1L, Role.Staff, STAFF_EMAIL, "Staff One");
        User otherStaff = user(2L, Role.Staff, OTHER_STAFF_EMAIL, "Staff Two");
        ServiceRequest request = assignedRequest(5L, otherStaff);
        when(userRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(requestListService.canView(staff, request)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.get(STAFF_EMAIL, 5L)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(statusHistoryRepository, never()).findByRequestIdOrderByChangedAtAsc(any());
    }

    @DisplayName("TC-A-07 (FR008/RTM-008, NFR004) Accept writes exactly one history row")
    @Test
    void acceptWritesOneHistoryRow() {
        User staff = user(1L, Role.Staff, STAFF_EMAIL, "Staff One");
        ServiceRequest request = openRequest(5L, staff);
        request.setVersion(0);
        stubVisibleStaff(staff, request);
        stubFlushIncrementsVersion();
        stubEmptyDetailLists(5L);

        service.accept(STAFF_EMAIL, 5L, 0);

        ArgumentCaptor<StatusHistory> historyCaptor = ArgumentCaptor.forClass(StatusHistory.class);
        verify(statusHistoryRepository).save(historyCaptor.capture());
        StatusHistory history = historyCaptor.getValue();
        assertEquals(RequestStatus.Open, history.getFromStatus());
        assertEquals(RequestStatus.Assigned, history.getToStatus());
        assertEquals(staff, history.getChangedBy());
    }

    private void stubVisibleStaff(User staff, ServiceRequest request) {
        when(userRepository.findByEmail(staff.getEmail())).thenReturn(Optional.of(staff));
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(requestListService.canView(staff, request)).thenReturn(true);
    }

    private void stubFlushIncrementsVersion() {
        when(requestRepository.saveAndFlush(any(ServiceRequest.class))).thenAnswer(invocation -> {
            ServiceRequest saved = invocation.getArgument(0);
            Integer current = saved.getVersion();
            saved.setVersion(current == null ? 1 : current + 1);
            return saved;
        });
    }

    private void stubEmptyDetailLists(long requestId) {
        when(statusHistoryRepository.findByRequestIdOrderByChangedAtAsc(requestId)).thenReturn(List.of());
        when(commentRepository.findByRequestIdOrderByCreatedAtAsc(eq(requestId))).thenReturn(List.of());
    }

    private static User user(long id, Role role, String email, String name) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash("hash");
        return user;
    }

    private static ServiceRequest openRequest(long id, User submitter) {
        ServiceRequest request = new ServiceRequest();
        request.setId(id);
        request.setTitle("Broken light");
        request.setDescription("Hallway light out");
        request.setCategory("Facility Fault");
        request.setStatus(RequestStatus.Open);
        request.setSubmittedBy(submitter);
        request.setCreatedAt(Instant.parse("2026-10-13T10:00:00Z"));
        request.setUpdatedAt(Instant.parse("2026-10-13T10:00:00Z"));
        request.setVersion(0);
        return request;
    }

    private static ServiceRequest assignedRequest(long id, User assignee) {
        ServiceRequest request = openRequest(id, assignee);
        request.setStatus(RequestStatus.Assigned);
        request.setAssignedTo(assignee);
        return request;
    }
}
