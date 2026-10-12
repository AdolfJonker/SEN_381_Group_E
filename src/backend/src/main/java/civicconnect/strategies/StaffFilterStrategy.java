package civicconnect.strategies;

import civicconnect.records.ServiceRequest;
import civicconnect.repositories.ServiceRequestRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaffFilterStrategy implements FilterStrategy {

    private final ServiceRequestRepository serviceRequestRepository;

    public StaffFilterStrategy(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public List<ServiceRequest> getFilteredRequests(Long userId) {
        return serviceRequestRepository.findByAssignedToId(userId);
    }
}
