package civicconnect.strategies;

import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequesterFilterStrategy implements FilterStrategy {

    private final ServiceRequestRepository serviceRequestRepository;

    public RequesterFilterStrategy(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Override
    public List<ServiceRequest> getFilteredRequests(Long userId) {
        return serviceRequestRepository.findBySubmittedById(userId);
    }

    @Override
    public boolean canView(ServiceRequest request, Long userId) {
        User submitter = request.getSubmittedBy();
        return submitter != null && userId.equals(submitter.getId());
    }
}
