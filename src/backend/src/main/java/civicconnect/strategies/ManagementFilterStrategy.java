package civicconnect.strategies;

import civicconnect.records.ServiceRequest;
import civicconnect.repositories.ServiceRequestRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ManagementFilterStrategy implements FilterStrategy {

    private final ServiceRequestRepository serviceRequestRepository;

    public ManagementFilterStrategy(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public List<ServiceRequest> getFilteredRequests(Long userId) {
        return serviceRequestRepository.findAll();
    }

    @Override
    public boolean canView(ServiceRequest request, Long userId) {
        return true;
    }
}
