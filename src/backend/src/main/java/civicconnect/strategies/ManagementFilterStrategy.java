package civicconnect.strategies;

import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Management scope: all requests, for oversight (FR012, FR013).
 */
@Component
public class ManagementFilterStrategy implements FilterStrategy {

    private final ServiceRequestRepository serviceRequestRepository;

    public ManagementFilterStrategy(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public Role supports() {
        return Role.Management;
    }

    @Override
    public List<ServiceRequest> getFilteredRequests(Long userId) {
        return serviceRequestRepository.findAll();
    }

    @Override
    public boolean canView(User user, ServiceRequest request) {
        return true;
    }
}
