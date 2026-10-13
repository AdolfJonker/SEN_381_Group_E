package civicconnect.services;

import civicconnect.records.RequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static civicconnect.records.RequestStatus.*;   // ← this line is missing or incomplete
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FR009 lifecycle rules + D2–D6 permission rules.
 * Test IDs refer to docs/testing/status-lifecycle-test-evidence.md.
 * No Spring context, no database.
 */
class StatusTransitionGuardTest {

    private final StatusTransitionGuard guard = new StatusTransitionGuard();

    // -------------------------------------------------------------------------
    // Original structural tests (unchanged)
    // -------------------------------------------------------------------------

    @DisplayName("TC-E-01 legal forward step is allowed")
    @ParameterizedTest(name = "{0} to {1}")
    @CsvSource({
            "Open,Assigned",
            "Assigned,InProgress",
            "InProgress,Resolved",
            "Resolved,Closed"
    })
    void legalForwardStepsAreAllowed(RequestStatus from, RequestStatus to) {
        assertTrue(guard.isLegalTransition(from, to));
    }

    @DisplayName("TC-E-02 skipped status is rejected")
    @ParameterizedTest(name = "{0} to {1}")
    @CsvSource({
            "Open,InProgress",
            "Open,Resolved",
            "Open,Closed",
            "Assigned,Resolved",
            "Assigned,Closed",
            "InProgress,Closed"
    })
    void skippedStatusesAreRejected(RequestStatus from, RequestStatus to) {
        assertFalse(guard.isLegalTransition(from, to));
    }

    @DisplayName("TC-E-03 backwards or same status move is rejected")
    @ParameterizedTest(name = "{0} to {1}")
    @CsvSource({
            "Assigned,Open",
            "InProgress,Open",
            "InProgress,Assigned",
            "Resolved,Open",
            "Resolved,Assigned",
            "Resolved,InProgress",
            "Closed,Open",
            "Closed,Assigned",
            "Closed,InProgress",
            "Closed,Resolved",
            "Open,Open",
            "Assigned,Assigned",
            "InProgress,InProgress",
            "Resolved,Resolved",
            "Closed,Closed"
    })
    void backwardsAndSameStatusMovesAreRejected(RequestStatus from, RequestStatus to) {
        assertFalse(guard.isLegalTransition(from, to));
    }

    @DisplayName("Partition check: exactly 4 of the 25 pairs are legal")
    @Test
    void exactlyFourOfTwentyFivePairsAreLegal() {
        int legal = 0;
        for (RequestStatus from : RequestStatus.values()) {
            for (RequestStatus to : RequestStatus.values()) {
                if (guard.isLegalTransition(from, to)) {
                    legal++;
                }
            }
        }
        assertEquals(4, legal);
    }

    @DisplayName("Missing status is never a legal transition")
    @Test
    void nullStatusIsRejected() {
        assertFalse(guard.isLegalTransition(null, RequestStatus.Assigned));
        assertFalse(guard.isLegalTransition(RequestStatus.Open, null));
    }

    // -------------------------------------------------------------------------
    // New permission tests (D2–D6)
    // -------------------------------------------------------------------------

    @DisplayName("TC-E-04 Staff may assign an Open request")
    @Test
    void staffMayAssignOpenRequest() {
        assertTrue(guard.canPerformTransition(Open, Assigned, "Staff", false));
    }

    @DisplayName("TC-E-05 Staff who is not the assignee may not move Assigned/InProgress/Resolved")
    @ParameterizedTest(name = "{0} to {1}")
    @CsvSource({
            "Assigned,InProgress",
            "InProgress,Resolved",
            "Resolved,Closed"
    })
    void nonOwnerStaffMayNotAdvance(RequestStatus from, RequestStatus to) {
        assertFalse(guard.canPerformTransition(from, to, "Staff", false));
    }

    @DisplayName("TC-E-06 Assigned Staff may advance their own request")
    @ParameterizedTest(name = "{0} to {1}")
    @CsvSource({
            "Assigned,InProgress",
            "InProgress,Resolved",
            "Resolved,Closed"
    })
    void ownerStaffMayAdvance(RequestStatus from, RequestStatus to) {
        assertTrue(guard.canPerformTransition(from, to, "Staff", true));
    }

    @DisplayName("TC-E-07 Management may perform any legal transition")
    @ParameterizedTest(name = "{0} to {1}")
    @CsvSource({
            "Open,Assigned",
            "Assigned,InProgress",
            "InProgress,Resolved",
            "Resolved,Closed"
    })
    void managementMayDoAnyLegalTransition(RequestStatus from, RequestStatus to) {
        assertTrue(guard.canPerformTransition(from, to, "Management", false));
        assertTrue(guard.canPerformTransition(from, to, "Management", true));
    }

    @DisplayName("TC-E-08 Requester may never change status")
    @Test
    void requesterMayNeverChangeStatus() {
        assertFalse(guard.canPerformTransition(Open, Assigned, "Requester", false));
        assertFalse(guard.canPerformTransition(Assigned, InProgress, "Requester", true));
    }

    @DisplayName("TC-E-09 Illegal transition is rejected even for Management")
    @Test
    void illegalTransitionRejectedEvenForManagement() {
        assertFalse(guard.canPerformTransition(Open, Closed, "Management", false));
        assertFalse(guard.canPerformTransition(Assigned, Resolved, "Management", true));
    }
}