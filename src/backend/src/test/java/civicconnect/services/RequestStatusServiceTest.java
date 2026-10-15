package civicconnect.services;

import civicconnect.api.dto.StatusChangeResponse;
import civicconnect.observers.AuditLogObserver;
import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Service level tests for changing a status (FR009, FR011, NFR004, ADR-DATA-001).
 * No Spring context and no database: they show the order of steps and that a rejected
 * change writes nothing. Database behaviour is covered by the integration tests.
 */
@ExtendWith(MockitoExtension.class)
class RequestStatusServiceTest {

    @Mock
    private ServiceRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogObserver auditLogObserver;

    private RequestStatusService service;

    @BeforeEach
    void setUp() {
        service = new RequestStatusService(
                requestRepository, userRepository, new StatusTransitionGuard(), auditLogObserver);
    }

    private static User user(long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private static ServiceRequest request(long id, RequestStatus status, User assignee, int version) {
        ServiceRequest request = new ServiceRequest();
        request.setId(id);
        request.setStatus(status);
        request.setAssignedTo(assignee);
        request.setVersion(version);
        return request;
    }

    private void signedInAs(User actor) {
        when(userRepository.findByEmail(actor.getEmail())).thenReturn(Optional.of(actor));
    }

    private void requestExists(ServiceRequest request) {
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
    }

    private void assertNothingWritten() {
        verify(requestRepository, never()).saveAndFlush(any(ServiceRequest.class));
        verifyNoInteractions(auditLogObserver);
    }

    @DisplayName("TC-E-15 a legal change updates the request, then records one history entry (NFR004, ADR-DATA-001 order)")
    @Test
    void legalChangeUpdatesThenRecordsHistory() {
        User staff = user(5L, "staff@civicconnect.local", Role.Staff);
        ServiceRequest request = request(41L, RequestStatus.Assigned, staff, 3);
        signedInAs(staff);
        requestExists(request);
        when(requestRepository.saveAndFlush(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        StatusChangeResponse result =
                service.changeStatus(staff.getEmail(), 41L, RequestStatus.InProgress, 3, "Started work");

        assertEquals("InProgress", result.status());
        assertEquals(RequestStatus.InProgress, request.getStatus());
        InOrder order = inOrder(requestRepository, auditLogObserver);
        order.verify(requestRepository).saveAndFlush(request);
        order.verify(auditLogObserver)
                .record(request, RequestStatus.Assigned, RequestStatus.InProgress, staff, "Started work");
    }

    @DisplayName("TC-E-16 an illegal change is rejected with 400 and writes nothing (RISK-011)")
    @Test
    void illegalChangeWritesNothing() {
        User staff = user(5L, "staff@civicconnect.local", Role.Staff);
        ServiceRequest request = request(41L, RequestStatus.Open, null, 0);
        signedInAs(staff);
        requestExists(request);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(staff.getEmail(), 41L, RequestStatus.Closed, null, null));

        assertEquals(400, error.getStatusCode().value());
        assertEquals(RequestStatus.Open, request.getStatus());
        assertEquals(Integer.valueOf(0), request.getVersion());
        assertNothingWritten();
    }

    @DisplayName("TC-E-17 a requester may not change status: 403 and nothing is written (FR011, NFR003)")
    @Test
    void requesterIsForbiddenAndNothingIsWritten() {
        User staff = user(5L, "staff@civicconnect.local", Role.Staff);
        User requester = user(7L, "requester@civicconnect.local", Role.Requester);
        ServiceRequest request = request(41L, RequestStatus.Assigned, staff, 3);
        signedInAs(requester);
        requestExists(request);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(requester.getEmail(), 41L, RequestStatus.InProgress, 3, null));

        assertEquals(403, error.getStatusCode().value());
        assertEquals(RequestStatus.Assigned, request.getStatus());
        assertNothingWritten();
    }

    @DisplayName("TC-E-18 staff who do not own the request are forbidden: 403 and nothing is written (D2)")
    @Test
    void staffWhoDoNotOwnTheRequestAreForbidden() {
        User owner = user(5L, "staff@civicconnect.local", Role.Staff);
        User other = user(6L, "other.staff@civicconnect.local", Role.Staff);
        ServiceRequest request = request(41L, RequestStatus.Assigned, owner, 3);
        signedInAs(other);
        requestExists(request);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(other.getEmail(), 41L, RequestStatus.InProgress, 3, null));

        assertEquals(403, error.getStatusCode().value());
        assertNothingWritten();
    }

    @DisplayName("TC-E-19 a stale version is rejected with 409 and nothing is written (ADR-DATA-001)")
    @Test
    void staleVersionIsAConflict() {
        User staff = user(5L, "staff@civicconnect.local", Role.Staff);
        ServiceRequest request = request(41L, RequestStatus.Assigned, staff, 3);
        signedInAs(staff);
        requestExists(request);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(staff.getEmail(), 41L, RequestStatus.InProgress, 2, null));

        assertEquals(409, error.getStatusCode().value());
        assertEquals(RequestStatus.Assigned, request.getStatus());
        assertNothingWritten();
    }

    @DisplayName("TC-E-20 if the update fails at flush, no history entry is written (ADR-DATA-001)")
    @Test
    void failedUpdateWritesNoHistory() {
        User staff = user(5L, "staff@civicconnect.local", Role.Staff);
        ServiceRequest request = request(41L, RequestStatus.Assigned, staff, 3);
        signedInAs(staff);
        requestExists(request);
        doThrow(new ObjectOptimisticLockingFailureException(ServiceRequest.class, 41L))
                .when(requestRepository).saveAndFlush(any(ServiceRequest.class));

        assertThrows(OptimisticLockingFailureException.class,
                () -> service.changeStatus(staff.getEmail(), 41L, RequestStatus.InProgress, 3, null));

        verifyNoInteractions(auditLogObserver);
    }

    @DisplayName("TC-E-21 an unknown request is 404 and nothing is written")
    @Test
    void unknownRequestIsNotFound() {
        User staff = user(5L, "staff@civicconnect.local", Role.Staff);
        signedInAs(staff);
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(staff.getEmail(), 99L, RequestStatus.InProgress, null, null));

        assertEquals(404, error.getStatusCode().value());
        assertNothingWritten();
    }

    @DisplayName("TC-E-22 assigning is not done here: 400 points to the assign action (FR008 is separate)")
    @Test
    void assigningIsHandledElsewhere() {
        User staff = user(5L, "staff@civicconnect.local", Role.Staff);
        ServiceRequest request = request(41L, RequestStatus.Open, null, 0);
        signedInAs(staff);
        requestExists(request);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.changeStatus(staff.getEmail(), 41L, RequestStatus.Assigned, null, null));

        assertEquals(400, error.getStatusCode().value());
        assertTrue(error.getReason().toLowerCase().contains("assign"));
        assertNothingWritten();
    }

    @DisplayName("TC-E-23 management may move any request forward, and the history shows who did it (D6)")
    @Test
    void managementMayMoveAnyRequestForward() {
        User staff = user(5L, "staff@civicconnect.local", Role.Staff);
        User manager = user(9L, "management@civicconnect.local", Role.Management);
        ServiceRequest request = request(41L, RequestStatus.Assigned, staff, 3);
        signedInAs(manager);
        requestExists(request);
        when(requestRepository.saveAndFlush(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        StatusChangeResponse result =
                service.changeStatus(manager.getEmail(), 41L, RequestStatus.InProgress, null, null);

        assertEquals("InProgress", result.status());
        verify(auditLogObserver)
                .record(request, RequestStatus.Assigned, RequestStatus.InProgress, manager, null);
    }
}
