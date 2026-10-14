# Access Control, Role Views and Summary Test Evidence

| Field | Value |
|---|---|
| Owner | Donya Pretorius |
| Covers | NFR003, FR005, FR006, FR011, FR012, FR013 (RTM-016, RTM-005, RTM-006, RTM-011, RTM-012, RTM-013) |
| Document status | Test design complete. Frontend unit tests run. Backend tests to be run in CI |
| Release candidate | To be confirmed at evidence freeze |
| Last updated | 13/10/2026 |

## 1. Purpose and scope

This record holds the test design and results for role-based access, the role views and the Management summary. It supports PED v3.0 sections 5 and 7 and the test references in the RTM. Every expected result was written before the test was run.

## 2. Risk basis and technique choice

* RBAC done wrong (RISK-003) and data exposure (RISK-004) are High risks. Each role is therefore tested on its own, including negative cases (401 and 403).
* Each FilterStrategy is unit tested alone, and the role-to-strategy choice is tested separately, as ADR-DESIGN-002 asks.
* FR011 has three conditions (role, assigned or not, target status). A decision table lists all 12 combinations.
* The filters (FR013) have input groups that behave the same way, so equivalence partitioning is used.

## 3. Summary

| ID | Title | Technique | Level | Basis | RTM | Status |
|---|---|---|---|---|---|---|
| TC-UNIT-RBAC-01 | Requester strategy returns only own requests | Unit | Unit | FR005, NFR003 | RTM-005, RTM-016 | To run in CI |
| TC-UNIT-RBAC-02 | Staff strategy returns own work plus Open requests | Unit | Unit | FR005, FR008 | RTM-005 | To run in CI |
| TC-UNIT-RBAC-03 | Management strategy returns all requests | Unit | Unit | FR012, FR013 | RTM-005 | To run in CI |
| TC-UNIT-RBAC-04 | Service picks the right strategy per role; missing role refused | Unit | Unit | ADR-DESIGN-002 | RTM-005 | To run in CI |
| TC-API-SEC-01 | Not signed in or wrong password returns 401 | Negative | API | NFR003, RISK-004 | RTM-016 | To run in CI |
| TC-API-SEC-02 | Wrong role returns 403; Management gets summary counts | Negative | API | NFR003, FR012 | RTM-016, RTM-012 | To run in CI |
| TC-API-SEC-03 | Requester cannot get another requester's request | Negative | API | NFR003 AC, ASR-01 | RTM-016 | To run in CI |
| TC-API-SEC-04 | Staff list holds only the staff scope | API | API | FR005 | RTM-005 | To run in CI |
| TC-BB-DT-01 | Who may set Resolved or Closed | Decision table | Unit (black box on the guard) | FR011, D2–D6 | RTM-011 | To run in CI |
| TC-UI-01 | Each role lands on its own view | Unit | Frontend unit | NFR003, FR012 | RTM-016 | Pass (13/10/2026) |
| TC-UI-02 | Category, status and search filters | Equivalence partitioning | Frontend unit | FR013, FR006 | RTM-013, RTM-006 | Pass (13/10/2026) |

## 4. Test records

### TC-UNIT-RBAC-01 to 03 Each role strategy on its own
* Basis: FR005, NFR003, ADR-DESIGN-002.
* Why selected: if a strategy asks the database for the wrong rows, a role sees data it should not.
* Input: a mocked repository and users of each role.
* Expected result: Requester strategy calls only `findBySubmittedById`; Staff calls only `findByAssignedToIdOrStatus(id, Open)`; Management calls `findAll`. `canView` is true for own rows and false for others.
* Evidence: `src/backend/src/test/java/civicconnect/strategies/FilterStrategyTest.java`; CI run [link].
* Interpretation: shows each rule in isolation. It does not show the controller uses the service; TC-API-SEC-03 and 04 cover that.

### TC-UNIT-RBAC-04 Strategy chosen per role
* Basis: ADR-DESIGN-002 ("the role-to-strategy resolution step needs its own test").
* Input: the three strategies; then only one strategy; then two strategies for one role.
* Expected result: each role gets its own strategy class; a role with no strategy gets 403; two strategies for one role stop start-up.
* Evidence: `RequestListServiceTest.java`; CI run [link].
* Interpretation: shows the service fails closed when a role has no rule.

### TC-API-SEC-01 Not signed in is rejected
* Basis: NFR003, RISK-004.
* Input: GET /api/v1/requests and /api/v1/requests/summary with no session; POST /api/v1/auth/login with a wrong password.
* Expected result: 401, 401; login 401 with "Invalid email or password".
* Evidence: `AccessControlApiTest.java`; CI run [link].
* Interpretation: anonymous calls cannot read data. Lockout after 5 failures is not covered here.

### TC-API-SEC-02 Wrong role returns 403
* Basis: NFR003, FR012, FR001.
* Input: summary as Requester and Staff; summary as Management (seed: 2 Open, 1 Closed, one Open request 10 days old); POST /requests as Staff.
* Expected result: 403, 403; 200 with open = 2, closed = 1, resolved = 0, overdue = 1; 403.
* Evidence: `AccessControlApiTest.java`; CI run [link].
* Interpretation: route rules work and the FR012 counts are correct for known data.

### TC-API-SEC-03 Requester cannot get another requester's data (ASR-01)
* Basis: NFR003 acceptance criterion.
* Input: Requester A opens Requester B's request by id; A opens own request; A lists requests.
* Expected result: 403; 200; every row has A's id and B's request is not in the list.
* Evidence: `AccessControlApiTest.java`; CI run [link].
* Interpretation: shows the "direct request" part of the NFR003 criterion for read and list.

### TC-API-SEC-04 Staff scope through the API
* Basis: FR005.
* Input: Staff with no assigned work lists requests (seed: 2 Open, 1 Closed).
* Expected result: 2 rows, all Open.
* Evidence: `AccessControlApiTest.java`; CI run [link].

### TC-BB-DT-01 Decision table for Resolved and Closed (FR011)
* Basis: FR011, D2–D6 rules in `StatusTransitionGuard` (Elcke).
* Conditions: role (Requester, Staff, Management) × assigned to this user (Y/N) × target (Resolved, Closed) = 12 rules.
* Expected result: Requester never; Staff only when assigned; Management always (for legal steps).
* Evidence: `StatusAuthorityDecisionTableTest.java`; CI run [link].
* Interpretation: covers every combination of the three conditions. It tests the guard, not the endpoint that will call it.

### TC-UI-01 and TC-UI-02 Frontend unit tests
* Evidence: `src/frontend/services/roleLanding.test.js`, `requestFilters.test.js`. `npm test`: 11 pass, 0 fail (13/10/2026).
* Interpretation: UI routing and filtering only. They are not the security control.

## 5. Manual UI check (supporting, not counted)

Run against a local stub of the API on 13/10/2026: Management lands on the summary; IT Support + Open filter shows 1 of 7; Staff lands on the request list; search works; no sideways scroll at 375 px. Re-run against staging.
