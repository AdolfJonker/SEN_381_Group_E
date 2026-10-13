package civicconnect.strategies;

import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Staff scope from FR005, FR008, D2 and D4: every Open request, so a staff
 * member can accept it, plus the requests already assigned to that staff member.
 */
@Component
public class StaffFilterStrategy implements FilterStrategy {

    private final ServiceRequestRepository serviceRequestRepository;

    public StaffFilterStrategy(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public List<ServiceRequest> getFilteredRequests(Long userId) {
        return serviceRequestRepository.findVisibleToStaff(RequestStatus.Open, userId);
    }

    @Override
    public boolean canView(ServiceRequest request, Long userId) {
        if (request.getStatus() == RequestStatus.Open) {
            return true;
        }
        User assignedTo = request.getAssignedTo();
        return assignedTo != null && userId.equals(assignedTo.getId());
    }
}
