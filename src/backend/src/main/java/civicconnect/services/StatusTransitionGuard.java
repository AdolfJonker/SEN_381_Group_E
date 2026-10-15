package civicconnect.services;

import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static civicconnect.records.RequestStatus.*;

/**
 * Single source of truth for the request lifecycle (FR009, FR011, ADR-DESIGN-001, D2 to D6).
 * <p>
 * evaluate() gives the caller three answers so it can choose the right HTTP status:
 * ALLOWED (go ahead), FORBIDDEN (403, this user may not do it) or ILLEGAL (400, not a valid move).
 * Permission is checked before legality (clarification C4).
 * Nothing is written here - callers remain responsible for the update and the history row.
 */
@Component
public class StatusTransitionGuard {

    public enum Decision { ALLOWED, FORBIDDEN, ILLEGAL }

    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED_NEXT = Map.of(
            Open, Set.of(Assigned),
            Assigned, Set.of(InProgress),
            InProgress, Set.of(Resolved),
            Resolved, Set.of(Closed),
            Closed, Set.of()
    );

    /**
     * Structural check only (no roles).
     */
    public boolean isLegalTransition(RequestStatus from, RequestStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED_NEXT.getOrDefault(from, Set.of()).contains(to);
    }

    /**
     * Full decision (D2 to D6, C4).
     *
     * @param isAssignedToUser true if the request is currently assigned to this user
     */
    public Decision evaluate(RequestStatus from, RequestStatus to, Role role, boolean isAssignedToUser) {
        // 1. Permission first: only Staff and Management may change status at all
        if (role != Role.Staff && role != Role.Management) {
            return Decision.FORBIDDEN;
        }
        // 2. Legality of the move
        if (!isLegalTransition(from, to)) {
            return Decision.ILLEGAL;
        }
        // 3. Management may make any legal forward step (D6)
        if (role == Role.Management) {
            return Decision.ALLOWED;
        }
        // 4. Staff: any Staff member may take an Open request, later steps only the assigned owner (D2)
        if (from == Open) {
            return Decision.ALLOWED;
        }
        return isAssignedToUser ? Decision.ALLOWED : Decision.FORBIDDEN;
    }

    /**
     * Boolean form kept for existing callers and tests.
     */
    public boolean canPerformTransition(RequestStatus from,
                                        RequestStatus to,
                                        String userRole,
                                        boolean isAssignedToUser) {
        return evaluate(from, to, parseRole(userRole), isAssignedToUser) == Decision.ALLOWED;
    }

    private static Role parseRole(String value) {
        if (value == null) {
            return null;
        }
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }

    /**
     * The single legal next status, or null when the request is Closed.
     */
    public RequestStatus nextStatus(RequestStatus from) {
        if (from == null) {
            return null;
        }
        return ALLOWED_NEXT.getOrDefault(from, Set.of()).stream().findFirst().orElse(null);
    }
}