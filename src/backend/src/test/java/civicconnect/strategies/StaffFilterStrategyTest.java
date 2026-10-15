package civicconnect.strategies;

import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Staff visibility for accept path (FR008 / RTM-008 only): Open requests for all staff; assigned only for the owner.
 * TC-A-08/09 complement TC-UNIT-RBAC-02. Access-control owns FR005 / RTM-005.
 * Do not map these IDs to RTM-005.
 */
@ExtendWith(MockitoExtension.class)
class StaffFilterStrategyTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @InjectMocks
    private StaffFilterStrategy strategy;

    @DisplayName("TC-A-08 (FR008/RTM-008 only — not RTM-005) Any staff member can see an Open request")
    @Test
    void openRequestIsVisibleToAnyStaff() {
        ServiceRequest request = request(RequestStatus.Open, null);
        assertTrue(strategy.canView(user(1L), request));
        assertTrue(strategy.canView(user(99L), request));
    }

    @DisplayName("TC-A-09 (FR008/RTM-008 only — not RTM-005) Staff cannot see another staff member's assigned request")
    @Test
    void assignedRequestIsOnlyVisibleToAssignee() {
        User assignee = user(2L);
        ServiceRequest request = request(RequestStatus.Assigned, assignee);

        assertTrue(strategy.canView(assignee, request));
        assertFalse(strategy.canView(user(1L), request));
    }

    private static User user(long id) {
        User user = new User();
        user.setId(id);
        user.setRole(Role.Staff);
        return user;
    }

    private static ServiceRequest request(RequestStatus status, User assignedTo) {
        ServiceRequest request = new ServiceRequest();
        request.setStatus(status);
        request.setAssignedTo(assignedTo);
        return request;
    }
}
