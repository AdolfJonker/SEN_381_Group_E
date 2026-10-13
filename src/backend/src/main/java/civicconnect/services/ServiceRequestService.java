package civicconnect.services;

import civicconnect.api.dto.CreateRequestBody;
import civicconnect.api.dto.RequestResponse;
import civicconnect.api.dto.ServiceRequestResponse;
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

import java.util.Comparator;
import java.util.List;

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

    /**
     * Lists the requests submitted by the signed in user (FR003).
     * The user comes from the session, never from a value the browser sends (NFR003).
     */
    @Transactional(readOnly = true)
    public List<ServiceRequestResponse> getRequestsForRequester(String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in"));

        return requestRepository.findBySubmittedById(requester.getId()).stream()
                .sorted(Comparator.comparing(ServiceRequest::getCreatedAt, Comparator.reverseOrder()))
                .map(ServiceRequestResponse::from)
                .toList();
    }
}
