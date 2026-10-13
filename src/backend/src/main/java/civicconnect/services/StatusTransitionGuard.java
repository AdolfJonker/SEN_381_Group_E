package civicconnect.services;

import civicconnect.records.RequestStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static civicconnect.records.RequestStatus.*;

/**
 * Single source of truth for the request lifecycle (FR009 + FR011, ADR-DESIGN-001 + D2–D6).
 * <p>
 * 1. Checks whether the transition is structurally legal.
 * 2. Checks whether the current user is allowed to perform it.
 * Nothing is written here — callers remain responsible for the actual update and history.
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

    /**
     * Structural check only (no roles). Kept for unit tests and simple callers.
     */
    public boolean isLegalTransition(RequestStatus from, RequestStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED_NEXT.getOrDefault(from, Set.of()).contains(to);
    }

    /**
     * Full check used by the service layer (D2–D6).
     *
     * @param from              current status
     * @param to                desired new status
     * @param userRole          role of the person attempting the change ("Staff" or "Management")
     * @param isAssignedToUser  true if the request is currently assigned to this user
     */
    public boolean canPerformTransition(RequestStatus from,
                                        RequestStatus to,
                                        String userRole,
                                        boolean isAssignedToUser) {

        // 1. Structural legality
        if (!isLegalTransition(from, to)) {
            return false;
        }

        // 2. Role + ownership rules (D2 + D6)
        boolean isManagement = "Management".equalsIgnoreCase(userRole);
        boolean isStaff = "Staff".equalsIgnoreCase(userRole);

        if (!isStaff && !isManagement) {
            return false; // Requesters may never change status
        }

        // Management may do any legal forward transition (D6)
        if (isManagement) {
            return true;
        }

        // Staff rules
        return switch (from) {
            case Open -> to == Assigned;                         // any Staff may assign
            case Assigned, InProgress, Resolved -> isAssignedToUser; // only the owner
            default -> false;
        };
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