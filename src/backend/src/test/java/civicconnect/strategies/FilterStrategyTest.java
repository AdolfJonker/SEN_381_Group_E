package civicconnect.strategies;

import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**Unit tests for each role strategy on its own (FR005, NFR003, ADR-DESIGN-002, RISK-003)*/
class FilterStrategyTest {

    private final ServiceRequestRepository repository = mock(ServiceRequestRepository.class);

    private static User user(long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    private static ServiceRequest request(User submitter, User assignee, RequestStatus status) {
        ServiceRequest request = new ServiceRequest();
        request.setSubmittedBy(submitter);
        request.setAssignedTo(assignee);
        request.setStatus(status);
        return request;
    }

    @Test
    @DisplayName("TC-UNIT-RBAC-01 Requester strategy asks only for the requester's own requests")
    void requesterSeesOnlyOwnRequests() {
        User alice = user(1, Role.Requester);
        User bob = user(2, Role.Requester);
        List<ServiceRequest> own = List.of(request(alice, null, RequestStatus.Open));
        when(repository.findBySubmittedById(1L)).thenReturn(own);

        RequesterFilterStrategy strategy = new RequesterFilterStrategy(repository);

        assertSame(own, strategy.getFilteredRequests(1L));
        verify(repository).findBySubmittedById(1L);
        verifyNoMoreInteractions(repository);
        assertTrue(strategy.canView(alice, request(alice, null, RequestStatus.Open)));
        assertFalse(strategy.canView(alice, request(bob, null, RequestStatus.Open)));
    }

    @Test
    @DisplayName("TC-UNIT-RBAC-02 Staff strategy returns own assigned work plus unaccepted Open requests")
    void staffSeesAssignedAndOpen() {
        User requester = user(1, Role.Requester);
        User staff = user(5, Role.Staff);
        User otherStaff = user(6, Role.Staff);
        List<ServiceRequest> scope = List.of(request(requester, staff, RequestStatus.InProgress));
        when(repository.findByAssignedToIdOrStatus(5L, RequestStatus.Open)).thenReturn(scope);

        StaffFilterStrategy strategy = new StaffFilterStrategy(repository);

        assertSame(scope, strategy.getFilteredRequests(5L));
        verify(repository).findByAssignedToIdOrStatus(5L, RequestStatus.Open);
        verifyNoMoreInteractions(repository);
        assertTrue(strategy.canView(staff, request(requester, staff, RequestStatus.InProgress)));
        assertTrue(strategy.canView(staff, request(requester, null, RequestStatus.Open)));
        assertFalse(strategy.canView(staff, request(requester, otherStaff, RequestStatus.InProgress)));
    }

    @Test
    @DisplayName("TC-UNIT-RBAC-03 Management strategy returns all requests")
    void managementSeesAll() {
        User requester = user(1, Role.Requester);
        User manager = user(9, Role.Management);
        List<ServiceRequest> all = List.of(
                request(requester, null, RequestStatus.Open),
                request(requester, null, RequestStatus.Closed)
        );
        when(repository.findAll()).thenReturn(all);

        ManagementFilterStrategy strategy = new ManagementFilterStrategy(repository);

        assertEquals(2, strategy.getFilteredRequests(9L).size());
        assertTrue(strategy.canView(manager, request(requester, null, RequestStatus.Closed)));
    }
}
