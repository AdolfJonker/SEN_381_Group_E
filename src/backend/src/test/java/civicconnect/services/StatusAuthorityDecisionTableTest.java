package civicconnect.services;

import civicconnect.records.RequestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**TC-BB-DT-01: decision table for FR011 ("only authorised staff can set Resolved or Closed").*/
class StatusAuthorityDecisionTableTest {

    private final StatusTransitionGuard guard = new StatusTransitionGuard();

    @DisplayName("TC-BB-DT-01 FR011 decision table")
    @ParameterizedTest(name = "R{0}: {1}, assigned={2}, {3} -> {4} => allowed={5}")
    @CsvSource({
            // rule, role,       assigned, from,       to,       allowed
            "1,  Requester,  false, InProgress, Resolved, false",
            "2,  Requester,  true,  InProgress, Resolved, false",
            "3,  Requester,  false, Resolved,   Closed,   false",
            "4,  Requester,  true,  Resolved,   Closed,   false",
            "5,  Staff,      false, InProgress, Resolved, false",
            "6,  Staff,      true,  InProgress, Resolved, true",
            "7,  Staff,      false, Resolved,   Closed,   false",
            "8,  Staff,      true,  Resolved,   Closed,   true",
            "9,  Management, false, InProgress, Resolved, true",
            "10, Management, true,  InProgress, Resolved, true",
            "11, Management, false, Resolved,   Closed,   true",
            "12, Management, true,  Resolved,   Closed,   true"
    })
    void decisionTable(int rule, String role, boolean assigned,
                       RequestStatus from, RequestStatus to, boolean allowed) {
        assertEquals(allowed, guard.canPerformTransition(from, to, role, assigned),
                "Decision table rule " + rule);
    }
}
