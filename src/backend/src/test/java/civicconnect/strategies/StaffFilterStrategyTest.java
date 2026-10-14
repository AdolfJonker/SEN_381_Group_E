package civicconnect.strategies;

import civicconnect.records.RequestStatus;
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
 * Staff visibility (FR005 / FR008): Open requests for all staff; assigned only for the owner.
 */
@ExtendWith(MockitoExtension.class)
class StaffFilterStrategyTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @InjectMocks
    private StaffFilterStrategy strategy;

    @DisplayName("Any staff member can see an Open request")
    @Test
    void openRequestIsVisibleToAnyStaff() {
        ServiceRequest request = request(RequestStatus.Open, null);
        assertTrue(strategy.canView(request, 1L));
        assertTrue(strategy.canView(request, 99L));
    }

    @DisplayName("Staff cannot see another staff member's assigned request")
    @Test
    void assignedRequestIsOnlyVisibleToAssignee() {
        User assignee = new User();
        assignee.setId(2L);
        ServiceRequest request = request(RequestStatus.Assigned, assignee);

        assertTrue(strategy.canView(request, 2L));
        assertFalse(strategy.canView(request, 1L));
    }

    private static ServiceRequest request(RequestStatus status, User assignedTo) {
        ServiceRequest request = new ServiceRequest();
        request.setStatus(status);
        request.setAssignedTo(assignedTo);
        return request;
    }
}
