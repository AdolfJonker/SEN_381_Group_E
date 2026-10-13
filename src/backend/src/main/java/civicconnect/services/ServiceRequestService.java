package civicconnect.services;

import civicconnect.api.dto.CreateRequestBody;
import civicconnect.api.dto.RequestResponse;
import civicconnect.records.RequestCategory;
import civicconnect.records.RequestStatus;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;

    public ServiceRequestService(
            ServiceRequestRepository requestRepository,
            UserRepository userRepository
    ) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RequestResponse create(String submitterEmail, CreateRequestBody body) {
        RequestCategory category;
        try {
            category = RequestCategory.fromLabel(body.category());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a category from the list.");
        }

        User submitter = userRepository.findByEmail(submitterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in"));

        ServiceRequest request = new ServiceRequest();
        request.setTitle(body.title());
        request.setDescription(body.description());
        request.setCategory(category.getLabel());
        request.setStatus(RequestStatus.Open);
        request.setSubmittedBy(submitter);

        return RequestResponse.from(requestRepository.save(request));
    }
}