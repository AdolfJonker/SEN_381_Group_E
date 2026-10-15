package civicconnect.services;

import civicconnect.api.dto.RequestListQuery;
import civicconnect.api.dto.RequestResponse;
import civicconnect.records.RequestStatus;
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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Builds the request list for the logged-in role (FR005, NFR003, ADR-DESIGN-002).
 * The role is resolved once, here, and the matching FilterStrategy does the filtering.
 * Optional search / status / category / sort (FR006, FR013) narrow that scoped result set.
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
        return listFor(email, RequestListQuery.empty());
    }

    /**
     * Role-scoped list with optional search / status / category / sort (FR006, FR013).
     * Role strategy runs first; query params narrow that result set.
     */
    @Transactional(readOnly = true)
    public List<RequestResponse> listFor(String email, RequestListQuery query) {
        User user = requireUser(email);
        RequestListQuery q = query == null ? RequestListQuery.empty() : query;
        RequestStatus statusFilter = parseStatus(q.status());
        String categoryFilter = blankToNull(q.category());
        String search = blankToNull(q.search());
        boolean ascending = "asc".equalsIgnoreCase(blankToNull(q.sort()));

        Stream<ServiceRequest> stream = strategyFor(user.getRole())
                .getFilteredRequests(user.getId())
                .stream();

        if (statusFilter != null) {
            stream = stream.filter(r -> r.getStatus() == statusFilter);
        }
        if (categoryFilter != null) {
            stream = stream.filter(r -> categoryFilter.equalsIgnoreCase(r.getCategory()));
        }
        if (search != null) {
            String term = search.toLowerCase(Locale.ROOT);
            stream = stream.filter(r -> {
                String idText = r.getId() == null ? "" : String.valueOf(r.getId());
                String title = r.getTitle() == null ? "" : r.getTitle().toLowerCase(Locale.ROOT);
                return idText.contains(term) || title.contains(term);
            });
        }

        Comparator<ServiceRequest> byDate = Comparator.comparing(ServiceRequest::getCreatedAt);
        if (!ascending) {
            byDate = byDate.reversed();
        }

        return stream
                .sorted(byDate)
                .map(RequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RequestResponse getFor(String email, Long requestId) {
        User user = requireUser(email);
        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
        if (!canView(user, request)) {
            // 403: the caller is told they are not allowed to view request (NFR003, ASR-01).
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You may not view this request.");
        }
        return RequestResponse.from(request);
    }

    public boolean canView(User user, ServiceRequest request) {
        return strategyFor(user.getRole()).canView(user, request);
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not signed in"));
    }

    private static RequestStatus parseStatus(String raw) {
        String value = blankToNull(raw);
        if (value == null) {
            return null;
        }
        try {
            return RequestStatus.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown status filter: " + value);
        }
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
