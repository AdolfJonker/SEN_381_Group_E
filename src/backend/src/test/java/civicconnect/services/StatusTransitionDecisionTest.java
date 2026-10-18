package civicconnect.services;

import civicconnect.records.RequestStatus;
import civicconnect.records.Role;
import civicconnect.services.StatusTransitionGuard.Decision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Decision table for who may make each status change (FR011, D2 to D6, C4).
 * Expected results come from docs/decisions/D2-D6-status-transition-authority.md.
 * No Spring context and no database.
 */
class StatusTransitionDecisionTest {

    private final StatusTransitionGuard guard = new StatusTransitionGuard();

    @DisplayName("Requester is always forbidden, whether the move is legal or not (permission before legality)")
    @ParameterizedTest(name = "Requester {0} to {1}")
    @CsvSource({
            "Open,Assigned",
            "Open,Closed",
            "Closed,Open",
            "Assigned,InProgress"
    })
    void requesterIsAlwaysForbidden(RequestStatus from, RequestStatus to) {
        assertEquals(Decision.FORBIDDEN, guard.evaluate(from, to, Role.Requester, true));
    }

    @DisplayName("Illegal move by Staff or Management is ILLEGAL, even for the owner")
    @ParameterizedTest(name = "{0}: {1} to {2}")
    @CsvSource({
            "Staff,Open,InProgress",
            "Staff,Assigned,Open",
            "Staff,Closed,Closed",
            "Management,Open,Closed",
            "Management,Resolved,Assigned",
            "Management,InProgress,InProgress"
    })
    void illegalMovesAreIllegal(Role role, RequestStatus from, RequestStatus to) {
        assertEquals(Decision.ILLEGAL, guard.evaluate(from, to, role, true));
    }

    @DisplayName("Legal move: who may do it (D2, D6)")
    @ParameterizedTest(name = "{0}: {1} to {2}, owner={3} -> {4}")
    @CsvSource({
            "Staff,Open,Assigned,false,ALLOWED",
            "Staff,Assigned,InProgress,true,ALLOWED",
            "Staff,Assigned,InProgress,false,FORBIDDEN",
            "Staff,InProgress,Resolved,true,ALLOWED",
            "Staff,InProgress,Resolved,false,FORBIDDEN",
            "Staff,Resolved,Closed,true,ALLOWED",
            "Staff,Resolved,Closed,false,FORBIDDEN",
            "Management,Open,Assigned,false,ALLOWED",
            "Management,Assigned,InProgress,false,ALLOWED",
            "Management,InProgress,Resolved,false,ALLOWED",
            "Management,Resolved,Closed,false,ALLOWED"
    })
    void legalMovesFollowTheAuthorityTable(Role role, RequestStatus from, RequestStatus to,
                                           boolean owner, Decision expected) {
        assertEquals(expected, guard.evaluate(from, to, role, owner));
    }

    @DisplayName("Missing role is forbidden")
    @Test
    void nullRoleIsForbidden() {
        assertEquals(Decision.FORBIDDEN,
                guard.evaluate(RequestStatus.Open, RequestStatus.Assigned, null, false));
    }

    @DisplayName("Missing status is illegal for Staff (D5)")
    @Test
    void nullStatusIsIllegalForStaff() {
        assertEquals(Decision.ILLEGAL, guard.evaluate(null, RequestStatus.Assigned, Role.Staff, true));
        assertEquals(Decision.ILLEGAL, guard.evaluate(RequestStatus.Open, null, Role.Staff, true));
    }
}