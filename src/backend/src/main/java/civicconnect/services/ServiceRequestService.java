
package civicconnect.services;

import civicconnect.api.dto.ServiceRequestResponse;
import civicconnect.records.ServiceRequest;
import civicconnect.repositories.ServiceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;

    public ServiceRequestService(
            ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getRequestsForRequester(Long requesterId) {
        List<ServiceRequest> requests =
                serviceRequestRepository.findBySubmittedById(requesterId);

        return requests.stream()
                .map(ServiceRequestResponse::from)
                .toList();
    }
}
