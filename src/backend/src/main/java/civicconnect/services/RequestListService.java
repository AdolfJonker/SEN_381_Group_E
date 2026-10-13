package civicconnect.services;

import civicconnect.api.dto.ServiceRequestSummaryDto;
import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;
import civicconnect.strategies.FilterStrategy;
import civicconnect.strategies.ManagementFilterStrategy;
import civicconnect.strategies.RequesterFilterStrategy;
import civicconnect.strategies.StaffFilterStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class RequestListService {

    private final Map<Role, FilterStrategy> strategies;

    public RequestListService(
            RequesterFilterStrategy requesterFilterStrategy,
            StaffFilterStrategy staffFilterStrategy,
            ManagementFilterStrategy managementFilterStrategy
    ) {
        this.strategies = Map.of(
                Role.Requester, requesterFilterStrategy,
                Role.Staff, staffFilterStrategy,
                Role.Management, managementFilterStrategy
        );
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestSummaryDto> listFor(User user) {
        return strategyFor(user.getRole()).getFilteredRequests(user.getId()).stream()
                .sorted(Comparator.comparing(ServiceRequest::getCreatedAt).reversed())
                .map(ServiceRequestSummaryDto::from)
                .toList();
    }

    public boolean canView(User user, ServiceRequest request) {
        return strategyFor(user.getRole()).canView(request, user.getId());
    }

    private FilterStrategy strategyFor(Role role) {
        FilterStrategy strategy = strategies.get(role);
        if (strategy == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view requests.");
        }
        return strategy;
    }
}
