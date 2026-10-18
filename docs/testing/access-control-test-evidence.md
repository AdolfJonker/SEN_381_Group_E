# Access Control, Role Views and Summary Test Evidence

| Field | Value |
|---|---|
| Owner | Donya Pretorius |
| Covers | NFR003, FR005, FR006, FR011, FR012, FR013 (RTM-016, RTM-005, RTM-006, RTM-011, RTM-012, RTM-013) |
| Document status | Backend tests passed locally (`mvnw verify`, 66 tests, 0 failures, 14/10/2026). TC-D-INT-01c passed locally 15/10/2026 (AccessControlApiTest: 9 tests, 0 failures). CI run on main: link to add. |
| Release candidate | To be confirmed at evidence freeze |
| Code baseline | main at cf5a4f2 (after #120) |
| Last updated | 15/10/2026 |

## Test ID change (15/10/2026)

The IDs were renamed to the team format used by Adolf: TC-<initial>-NN for unit tests and TC-<initial>-INT-NN for API/integration tests. Older PRs and commits use the old IDs.

| Old ID | New ID |
|---|---|
| TC-UNIT-RBAC-01 to 04 | TC-D-01 to 04 |
| TC-BB-DT-01 | TC-D-05 |
| TC-UI-01 | TC-D-06 |
| TC-UI-02 | TC-D-07 |
| TC-API-SEC-01 to 04 | TC-D-INT-01 to 04 |

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
| TC-D-01 | Requester strategy returns only own requests | Unit | Unit | FR005, NFR003 | RTM-005, RTM-016 | Pass (local, 14/10/2026) |
| TC-D-02 | Staff strategy returns own work plus Open requests | Unit | Unit | FR005, FR008 | RTM-005 | Pass (local, 14/10/2026) |
| TC-D-03 | Management strategy returns all requests | Unit | Unit | FR012, FR013 | RTM-005 | Pass (local, 14/10/2026) |
| TC-D-04 | Service picks the right strategy per role; missing role refused | Unit | Unit | ADR-DESIGN-002 | RTM-005 | Pass (local, 14/10/2026) |
| TC-D-INT-01a/b | Not signed in or wrong password returns 401 | Negative | API | NFR003, RISK-004 | RTM-016 | Pass (local, 14/10/2026) |
| TC-D-INT-01c | Not signed in on `/requests/mine`, `/requests/{id}` and `/requests/{id}/status` returns 401 | Negative | API | NFR003 | RTM-016 | Pass (local, 15/10/2026) |
| TC-D-INT-02 | Wrong role returns 403; Management gets summary counts | Negative | API | NFR003, FR012 | RTM-016, RTM-012 | Pass (local, 14/10/2026) |
| TC-D-INT-03 | Requester cannot get another requester's request | Negative | API | NFR003 AC, ASR-01 | RTM-016 | Pass (local, 14/10/2026) |
| TC-D-INT-04 | Staff list holds only the staff scope | API | API | FR005 | RTM-005 | Pass (local, 14/10/2026) |
| TC-D-05 | Who may set Resolved or Closed | Decision table | Unit (black box on the guard) | FR011, D2–D6 | RTM-011 | Pass (local, 14/10/2026) |
| TC-D-06 | Each role lands on its own view | Unit | Frontend unit | NFR003, FR012 | RTM-016 | Pass (13/10/2026) |
| TC-D-07 | Category, status and search filters | Equivalence partitioning | Frontend unit | FR013, FR006 | RTM-013, RTM-006 | Pass (13/10/2026) |
| TC-D-08 | Demo users switched off (postgres/staging default): no accounts created | Negative | Unit | RISK-004, NFR003 | RTM-016 | [TO COMPLETE: result on v4.0.0] |
| TC-D-09 | Demo users switched on without a password: start-up stops, nothing saved | Negative | Unit | RISK-004 | RTM-016 | [TO COMPLETE: result on v4.0.0] |
| TC-D-10 | Demo users switched on: three accounts saved with the configured password | Unit | Unit | RISK-004 | RTM-016 | [TO COMPLETE: result on v4.0.0] |

## 4. Test records

### TC-D-INT-01c Signed-out calls on the requester and status routes
* Basis: NFR003; observation 1 in `status-lifecycle-test-evidence.md` (a signed-out `GET /requests/mine` returned 403 on 14/10).
* Why selected: the 401 rule was merged after that live check. This test shows which code answer is correct now.
* Input: no session. `GET /api/v1/requests/mine`, `GET /api/v1/requests/{id}`, `POST /api/v1/requests/{id}/status`.
* Expected result: 401 for all three.
* Evidence: `AccessControlApiTest.java`; local `mvnw verify` 15/10/2026: AccessControlApiTest 9 tests, 0 failures.
* Actual result: 401 for all three. Pass.
* Interpretation: observation 1 is closed on main. The 14/10 live check stays a record of the older code.

### TC-D-01 to 03 Each role strategy on its own
* Basis: FR005, NFR003, ADR-DESIGN-002.
* Why selected: if a strategy asks the database for the wrong rows, a role sees data it should not.
* Input: a mocked repository and users of each role.
* Expected result: Requester strategy calls only `findBySubmittedById`; Staff calls only `findByAssignedToIdOrStatus(id, Open)`; Management calls `findAll`. `canView` is true for own rows and false for others.
* Evidence: `src/backend/src/test/java/civicconnect/strategies/FilterStrategyTest.java`; CI run [link].
* Interpretation: shows each rule in isolation. It does not show the controller uses the service; TC-D-INT-03 and 04 cover that.

### TC-D-04 Strategy chosen per role
* Basis: ADR-DESIGN-002 ("the role-to-strategy resolution step needs its own test").
* Input: the three strategies; then only one strategy; then two strategies for one role.
* Expected result: each role gets its own strategy class; a role with no strategy gets 403; two strategies for one role stop start-up.
* Evidence: `RequestListServiceTest.java`; CI run [link].
* Interpretation: shows the service fails closed when a role has no rule.

### TC-D-INT-01 Not signed in is rejected
* Basis: NFR003, RISK-004.
* Input: GET /api/v1/requests and /api/v1/requests/summary with no session; POST /api/v1/auth/login with a wrong password.
* Expected result: 401, 401; login 401 with "Invalid email or password".
* Evidence: `AccessControlApiTest.java`; CI run [link].
* Interpretation: anonymous calls cannot read data. Lockout after 5 failures is not covered here.

### TC-D-INT-02 Wrong role returns 403
* Basis: NFR003, FR012, FR001.
* Input: summary as Requester and Staff; summary as Management (seed: 2 Open, 1 Closed, one Open request 10 days old); POST /requests as Staff.
* Expected result: 403, 403; 200 with open = 2, closed = 1, resolved = 0, overdue = 1; 403.
* Evidence: `AccessControlApiTest.java`; CI run [link].
* Interpretation: route rules work and the FR012 counts are correct for known data.

### TC-D-INT-03 Requester cannot get another requester's data (ASR-01)
* Basis: NFR003 acceptance criterion.
* Input: Requester A opens Requester B's request by id; A opens own request; A lists requests.
* Expected result: 403; 200; every row has A's id and B's request is not in the list.
* Evidence: `AccessControlApiTest.java`; CI run [link].
* Interpretation: shows the "direct request" part of the NFR003 criterion for read and list.

### TC-D-INT-04 Staff scope through the API
* Basis: FR005.
* Input: Staff with no assigned work lists requests (seed: 2 Open, 1 Closed).
* Expected result: 2 rows, all Open.
* Evidence: `AccessControlApiTest.java`; CI run [link].

### TC-D-05 Decision table for Resolved and Closed (FR011)
* Basis: FR011, D2–D6 rules in `StatusTransitionGuard` (Elcke).
* Conditions: role (Requester, Staff, Management) × assigned to this user (Y/N) × target (Resolved, Closed) = 12 rules.
* Expected result: Requester never; Staff only when assigned; Management always (for legal steps).
* Evidence: `StatusAuthorityDecisionTableTest.java`; CI run [link].
* Interpretation: covers every combination of the three conditions. It tests the guard, not the endpoint that will call it.

### How TC-D-05 fits with Elcke's tests
* TC-D-05 checks only Resolved and Closed (12 rules: 3 roles × assigned Y/N × 2 targets).
* Elcke's `StatusTransitionDecisionTest` (23 cases, no TC ID yet) checks every role and ownership case for all moves. TC-E-05 to 08 check the same rules one at a time.
* Both tests call the same `StatusTransitionGuard`, so they agree by design. TC-D-05 is the black-box view for FR011. Elcke's test is the wider white-box check.

### TC-D-08 to TC-D-10 Demo accounts (RISK-004)
* Basis: RISK-004 release blocker. Demo accounts with one known password were created on every profile and the login screen showed the password.
* Input: DemoUserLoader with demo users off; on with a blank or missing password; on with a configured password. Mocked repository and password encoder.
* Expected result: off creates nothing; on without a password throws and saves nothing; on with a password saves three accounts hashed from that password.
* Evidence: `src/backend/src/test/java/civicconnect/config/DemoUserLoaderTest.java`. Production frontend build checked 18/10/2026: the built JavaScript contains neither the demo hint nor the password.
* Interpretation: proves the rule in the loader. It does not prove a host's environment settings; staging must not set DEMO_USERS_ENABLED.

### TC-D-06 and TC-D-07 Frontend unit tests
* Evidence: `src/frontend/services/roleLanding.test.js`, `requestFilters.test.js`. `npm test`: 11 pass, 0 fail (13/10/2026).
* Interpretation: UI routing and filtering only. They are not the security control.

## 5. Related tests by other members (not counted here)

| ID | Owner | What it adds |
|---|---|---|
| TC-A-08, TC-A-09 | Adolf | Staff `canView` for Open and other staff's assigned requests (FR008). Same rule as TC-D-02 |
| TC-A-06 | Adolf | Staff cannot open another staff member's assigned request (403) |
| TC-A-INT-04 | Adolf | FR006/FR013 query filters and FR012 counts through the API; Staff gets 403 on summary |
| TC-A-E2E-02 | Adolf | 401 on the list and 403 for Staff on the summary, in Playwright (local run) |
| TC-E-08, TC-E-13, TC-E-14, TC-E-17 | Elcke | Requester cannot change status; requester list holds own rows only; unknown user 401 at service level; requester 403 on status change |

## 6. Manual UI check (supporting, not counted)

Run against a local stub of the API on 13/10/2026: Management lands on the summary; IT Support + Open filter shows 1 of 7; Staff lands on the request list; search works; no sideways scroll at 375 px. Re-run against staging.

### 375 px recheck (NFR005), 18/10/2026
* Screens: login (with the dev-only demo hint showing, the widest case), Management landing (summary cards, filters, request table), Staff landing (search, filters, request table), Requester landing.
* Method: headless Chromium at 375 x 812 against the frontend from the RISK-004 branch, with the API answered by fixed test data (5 requests with long titles). Measured the page width against the screen width and listed any element wider than the screen.
* Result: page width 375 = screen width on all four screens; no element wider than the screen; no sideways scroll. Screenshots kept with the PED evidence.
* Status: Pass. DEF-D01 stays fixed. Together with Elcke's recheck of the submit form and My requests (18/10/2026), DEF-E01 is closed.
* Limit: stubbed API, not the live backend; layout only.

## 7. Defects found in this slice

| ID | Description | Severity | Status | Resolution | Regression test |
|---|---|---|---|---|---|
| DEF-D01 | Management page scrolled sideways at 375 px (NFR005) | Low | Fixed | Layout changed; re-checked at 375 px | Manual 375 px check |
| DEF-D02 | A signed-out call returned 403 instead of 401 (same as OBS-01 in the status-lifecycle evidence) | Low | Fixed | 401 entry point in SecurityConfig | TC-D-INT-01c |
