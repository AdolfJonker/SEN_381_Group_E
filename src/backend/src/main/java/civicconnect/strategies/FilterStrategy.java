package civicconnect.strategies;

import civicconnect.records.Role;
import civicconnect.records.ServiceRequest;
import civicconnect.records.User;

import java.util.List;

/**
 * Strategy per role for request visibility (FR005, NFR003, ADR-DESIGN-002).
 * Each role has one strategy. RequestListService picks the strategy from the user's role.
 */
public interface FilterStrategy {

    /** The role this strategy serves. Used by RequestListService to pick the strategy. */
    Role supports();

    /** All requests this user may see in their list. */
    List<ServiceRequest> getFilteredRequests(Long userId);

    /** True if this user may open this one request */
    boolean canView(User user, ServiceRequest request);
}
