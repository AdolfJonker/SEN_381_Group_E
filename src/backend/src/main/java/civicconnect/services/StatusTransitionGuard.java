package civicconnect.services;

import civicconnect.records.RequestStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static civicconnect.records.RequestStatus.Assigned;
import static civicconnect.records.RequestStatus.Closed;
import static civicconnect.records.RequestStatus.InProgress;
import static civicconnect.records.RequestStatus.Open;
import static civicconnect.records.RequestStatus.Resolved;

/**
 * Single source of truth for the request lifecycle (FR009, ADR-DESIGN-001).
 * Each status maps to the statuses it may move to. Nothing is written here.
 * Permission rules (FR011) are added in a separate change once decision D2 is agreed.
 */
@Component
public class StatusTransitionGuard {

    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED_NEXT = Map.of(
            Open, Set.of(Assigned),
            Assigned, Set.of(InProgress),
            InProgress, Set.of(Resolved),
            Resolved, Set.of(Closed),
            Closed, Set.of()
    );

    public boolean isLegalTransition(RequestStatus from, RequestStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED_NEXT.getOrDefault(from, Set.of()).contains(to);
    }
}
