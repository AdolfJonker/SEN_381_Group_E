package civicconnect.strategies;

import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Staff scope (FR005): requests assigned to this staff member, plus Open requests
 * that nobody has accepted yet. Staff need to see Open requests to accept them (FR008).
 */
@Component
public class StaffFilterStrategy implements FilterStrategy {

    private final ServiceRequestRepository serviceRequestRepository;

    public StaffFilterStrategy(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public Role supports() {
        return Role.Staff;
    }

    @Override
    public List<ServiceRequest> getFilteredRequests(Long userId) {
        return serviceRequestRepository.findByAssignedToIdOrStatus(userId, RequestStatus.Open);
    }

    @Override
    public boolean canView(User user, ServiceRequest request) {
        boolean assignedToMe = request.getAssignedTo() != null
                && Objects.equals(request.getAssignedTo().getId(), user.getId());
        return assignedToMe || request.getStatus() == RequestStatus.Open;
    }
}
