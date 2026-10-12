package civicconnect.services;

import civicconnect.records.RequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the FR009 lifecycle rules. Test IDs refer to
 * docs/testing/status-lifecycle-test-evidence.md. No Spring context and no database.
 */
class StatusTransitionGuardTest {

    private final StatusTransitionGuard guard = new StatusTransitionGuard();

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
}
