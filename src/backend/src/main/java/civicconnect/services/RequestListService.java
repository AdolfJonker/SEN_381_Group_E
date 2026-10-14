package civicconnect.services;

import civicconnect.api.dto.RequestResponse;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.repositories.ServiceRequestRepository;
import civicconnect.repositories.UserRepository;
import civicconnect.strategies.FilterStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the request list for the logged-in role (FR005, NFR003, ADR-DESIGN-002).
 * The role is resolved once, here, and the matching FilterStrategy does the filtering.
 * There is no if/else on role in this class. A role with no strategy is refused (fail closed).
 */
@Service
public class RequestListService {

    private final Map<Role, FilterStrategy> strategies = new EnumMap<>(Role.class);
    private final UserRepository userRepository;
    private final ServiceRequestRepository requestRepository;

    public RequestListService(
            List<FilterStrategy> strategyBeans,
            UserRepository userRepository,
            ServiceRequestRepository requestRepository
    ) {
        for (FilterStrategy strategy : strategyBeans) {
            FilterStrategy previous = strategies.put(strategy.supports(), strategy);
            if (previous != null) {
                throw new IllegalStateException("Two filter strategies for role " + strategy.supports());
            }
        }
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    /** Picks the strategy for a role. */
    FilterStrategy strategyFor(Role role) {
        FilterStrategy strategy = role == null ? null : strategies.get(role);
        if (strategy == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your role cannot view requests.");
        }
        return strategy;
    }

    @Transactional(readOnly = true)
    public List<RequestResponse> listFor(String email) {
        User user = requireUser(email);
        return strategyFor(user.getRole()).getFilteredRequests(user.getId()).stream()
                .sorted(Comparator.comparing(ServiceRequest::getCreatedAt).reversed())
                .map(RequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequestResponse getFor(String email, Long requestId) {
        User user = requireUser(email);
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
        if (!strategyFor(user.getRole()).canView(user, request)) {
            // 403: the caller is told they are not allowed to view request (NFR003, ASR-01).
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You may not view this request.");
        }
        return RequestResponse.from(request);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in"));
    }
}
