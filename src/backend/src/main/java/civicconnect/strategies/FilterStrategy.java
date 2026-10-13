package civicconnect.strategies;

import civicconnect.records.ServiceRequest;

import java.util.List;

public interface FilterStrategy {
    List<ServiceRequest> getFilteredRequests(Long userId);

    boolean canView(ServiceRequest request, Long userId);
}
