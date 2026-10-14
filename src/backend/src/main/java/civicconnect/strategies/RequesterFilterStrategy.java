package civicconnect.strategies;

import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Requester scope: only the requests they submitted (NFR003 acceptance criterion, ASR-01).
 */
@Component
public class RequesterFilterStrategy implements FilterStrategy {

    private final ServiceRequestRepository serviceRequestRepository;

    public RequesterFilterStrategy(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public Role supports() {
        return Role.Requester;
    }

    @Override
    public List<ServiceRequest> getFilteredRequests(Long userId) {
        return serviceRequestRepository.findBySubmittedById(userId);
    }

    @Override
    public boolean canView(User user, ServiceRequest request) {
        return request.getSubmittedBy() != null
                && Objects.equals(request.getSubmittedBy().getId(), user.getId());
    }
}
