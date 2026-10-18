# Status Lifecycle and Submission Test Evidence

| Field             | Value                                                                                                                                                                                         |
| ----------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Owner             | Elcke Van Der Berg                                                                                                                                                                            |
| Covers            | FR001, FR003, FR004, FR008, FR009, FR011, NFR001, NFR003, NFR004, NFR005 (RTM-001, RTM-003, RTM-004, RTM-008, RTM-009, RTM-011, RTM-016, RTM-017)                                              |
| Issue             | #82                                                                                                                                                                                           |
| Code baselines    | A: main at 2f970a1. B: feature/audit-log-observer at fa8598e (#115). E: feature/fr001-automated-tests (#119). F: feature/requester-live-status at 19ceeec (FR004/NFR001)                       |
| Document status   | Results recorded for A, B, E and F. Database level tests not run. FR004/NFR001 demo and 375 px recheck recorded 18/10/2026                                                                     |
| Release candidate | To be confirmed at evidence freeze                                                                                                                                                            |
| Last updated      | 18/10/2026                                                                                                                                                                                    |

## 1. Purpose and scope

This record maps each test to its requirement, states the expected result, and records what actually happened. A test is marked Pass only where a recorded run shows it passing. Expected results come from the requirements, the decisions in docs/desicions/D2-D6-status-transition-authority.md and ADR-DATA-001. This file supports PED v3.0 sections 6, 7 and 9 and the test references in the RTM.

## 2. How to read Level and Result

* Unit: pure logic. No Spring context and no database.
* Service (mocks): the real service class with mocked repositories and a mocked audit observer. Shows the order of steps and that a rejected change never reaches the write path. Does not show database behaviour.
* Live API: a manual call sequence against the running application and a real PostgreSQL database. Real behaviour, but not automated and run once.
* Database: an automated test against real PostgreSQL. None exist yet.
* Result: Pass (automated), Pass (manual), Not run, or Not written.

## 3. Runs recorded

| Run | Code                                                    | Result                               | Date       |
| --- | ------------------------------------------------------- | ------------------------------------ | ---------- |
| A   | main at 2f970a1                                         | 66 tests, 0 failures, 0 errors       | 15/10/2026 |
| B   | feature/audit-log-observer at fa8598e                   | 76 tests, 0 failures, 0 errors       | 14/10/2026 |
| C   | Manual API check on the code of run B, local PostgreSQL | All calls as expected, see section 7 | 14/10/2026 |
| D   | Deliberate fault checks on the code of run B            | Each fault detected, see section 6   | 14/10/2026 |
| E   | feature/fr001-automated-tests (#119)                    | 15 new tests, 0 failures             | 15/10/2026 |
| F   | feature/requester-live-status at 19ceeec                | Demo + 6 unit tests pass             | 18/10/2026 |
| CI  | Workflow added in PR #112                               | Run reference to add                 |            |

## 4. Summary

| ID       | Title                                                                                       | Technique                | Level           | Basis                  | RTM              | Result                                      | Run  |
| -------- | ------------------------------------------------------------------------------------------- | ------------------------ | --------------- | ---------------------- | ---------------- | ------------------------------------------- | ---- |
| TC-E-01  | Legal forward steps accepted (4 cases)                                                      | State transition         | Unit            | FR009                  | RTM-009          | Pass (automated)                            | A, B |
| TC-E-02  | Skipped statuses rejected (6 cases)                                                         | State transition         | Unit            | FR009, RISK-011        | RTM-009          | Pass (automated)                            | A, B |
| TC-E-03  | Backwards and same status moves rejected (15 cases)                                         | State transition         | Unit            | FR009                  | RTM-009          | Pass (automated)                            | A, B |
| TC-E-03b | Exactly 4 of the 25 pairs are legal                                                         | State transition         | Unit            | FR009                  | RTM-009          | Pass (automated)                            | A, B |
| TC-E-03c | Missing status is never legal                                                               | Negative                 | Unit            | FR009, D5              | RTM-009          | Pass (automated)                            | A, B |
| TC-E-04  | Staff may assign an Open request                                                            | Decision rules           | Unit            | FR008, D2              | RTM-008, RTM-009 | Pass (automated)                            | A, B |
| TC-E-05  | Staff who do not own the request may not advance it (3 cases)                               | Decision rules           | Unit            | FR011, D2              | RTM-011          | Pass (automated)                            | A, B |
| TC-E-06  | Assigned Staff may advance their own request (3 cases)                                      | Decision rules           | Unit            | FR011, D2              | RTM-011          | Pass (automated)                            | A, B |
| TC-E-07  | Management may make any legal move (4 cases)                                                | Decision rules           | Unit            | FR011, D6              | RTM-011          | Pass (automated)                            | A, B |
| TC-E-08  | Requester may never change status                                                           | Negative                 | Unit            | FR011, NFR003          | RTM-011, RTM-016 | Pass (automated)                            | A, B |
| TC-E-09  | Illegal move rejected even for Management                                                   | Negative                 | Unit            | FR009, D6              | RTM-009          | Pass (automated)                            | A, B |
| none yet | StatusTransitionDecisionTest, 23 cases: ALLOWED, FORBIDDEN or ILLEGAL by role and ownership | Decision rules           | Unit            | FR009, FR011           | RTM-009, RTM-011 | Pass (automated)                            | A, B |
| TC-E-12  | List shows request ID, category, date and status                                            | Output check             | Service (mocks) | FR003                  | RTM-003          | Pass (automated)                            | A, B |
| TC-E-13  | A requester only receives their own requests                                                | Isolation                | Service (mocks) | FR003, NFR003          | RTM-003, RTM-016 | Pass (automated)                            | A, B |
| TC-E-14  | Unknown user rejected with 401 and nothing is read                                          | Negative                 | Service (mocks) | NFR003                 | RTM-016          | Pass (automated)                            | A, B |
| TC-E-15  | Legal change updates the request, then records one history entry                            | Order of steps           | Service (mocks) | NFR004, ADR-DATA-001   | RTM-009, RTM-017 | Pass (automated)                            | B    |
| TC-E-16  | Illegal change gives 400 and writes nothing                                                 | Negative                 | Service (mocks) | FR009, RISK-011        | RTM-009          | Pass (automated)                            | B    |
| TC-E-17  | Requester gets 403 and nothing is written                                                   | Negative                 | Service (mocks) | FR011, NFR003          | RTM-011, RTM-016 | Pass (automated)                            | B    |
| TC-E-18  | Staff who do not own the request get 403, nothing written                                   | Negative                 | Service (mocks) | FR011, D2              | RTM-011          | Pass (automated)                            | B    |
| TC-E-19  | Stale version gives 409, nothing written                                                    | Negative                 | Service (mocks) | ADR-DATA-001           | RTM-009, RTM-017 | Pass (automated)                            | B    |
| TC-E-20  | If the update fails at flush, no history entry is written                                   | Failure                  | Service (mocks) | NFR004                 | RTM-017          | Pass (automated)                            | B    |
| TC-E-21  | Unknown request gives 404, nothing written                                                  | Negative                 | Service (mocks) | FR009                  | RTM-009          | Pass (automated)                            | B    |
| TC-E-22  | Assigning through the status endpoint gives 400                                             | Negative                 | Service (mocks) | FR008                  | RTM-008          | Pass (automated)                            | B    |
| TC-E-23  | Management may move a request forward and history shows who did it                          | Decision rules           | Service (mocks) | FR011, D6              | RTM-011          | Pass (automated)                            | B    |
| TC-E-24  | One history row holds from, to, who, when and the note                                      | Output check             | Service (mocks) | NFR004                 | RTM-017          | Pass (automated)                            | B    |
| TC-E-25  | All seven valid categories accepted                                                         | Equivalence partitioning | Unit + Service  | FR001                  | RTM-001          | Pass (automated)                            | E    |
| TC-E-26  | Missing category rejected                                                                   | Equivalence partitioning | Unit            | FR001                  | RTM-001          | Pass (automated)                            | E    |
| TC-E-27  | Null, empty and blank category rejected                                                     | Equivalence partitioning | Unit            | FR001                  | RTM-001          | Pass (automated)                            | E    |
| TC-E-28  | Categories not on the list rejected                                                         | Equivalence partitioning | Service         | FR001, D4              | RTM-001          | Pass (automated)                            | E    |
| TC-E-29  | Title length 0, 1, 200 and 201 + trim behaviour                                             | Boundary value analysis  | Unit            | FR001                  | RTM-001          | Pass (automated)                            | E    |
| TC-E-30  | Legal change persists request and history together in PostgreSQL                            | Persistence              | Database        | NFR004, ASR-02         | RTM-017          | Not run                                     |      |
| TC-E-31  | Rejected change writes zero history rows in PostgreSQL                                      | Negative                 | Database        | NFR004, ASR-03         | RTM-009, RTM-017 | Not run                                     |      |
| TC-E-32  | Status visible on My Requests within 5 seconds after a staff change (poll every 4s) | Observation / timed demo | Frontend | FR004, NFR001 | RTM-004 | Pass (manual demo) | F |

Automated tests for TC-E-25 to TC-E-29 were added in PR #119 (15 tests, all passing).

## 5. Records

| ID       | Expected result                                                        | Actual result                                                                                | Evidence                                                                 | Interpretation                                                                |
| -------- | ---------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------ | ----------------------------------------------------------------------------- |
| TC-E-01  | The 4 legal moves are allowed                                          | 4 of 4 pass                                                                                  | StatusTransitionGuardTest, legalForwardStepsAreAllowed                   | The table allows the full path. It says nothing about who may move            |
| TC-E-02  | The 6 skipped moves are rejected                                       | 6 of 6 pass. Failed as intended under fault A                                                | skippedStatusesAreRejected                                               | Every forward skip is refused                                                 |
| TC-E-03  | The 15 backwards and same status moves are rejected                    | 15 of 15 pass                                                                                | backwardsAndSameStatusMovesAreRejected                                   | With TC-E-01 and TC-E-02 this covers all 25 pairs                             |
| TC-E-03b | Exactly 4 pairs are legal                                              | Pass. Failed under fault A (5 legal)                                                         | exactlyFourOfTwentyFivePairsAreLegal                                     | Catches a legal move added by mistake                                         |
| TC-E-03c | A null status is never legal                                           | Pass                                                                                         | nullStatusIsRejected                                                     | The guard fails closed                                                        |
| TC-E-04  | Staff may take an Open request                                         | Pass                                                                                         | staffMayAssignOpenRequest                                                | Rule check only. The assign action itself is separate                         |
| TC-E-05  | Non-owner Staff may not move Assigned, InProgress or Resolved requests | 3 of 3 pass                                                                                  | nonOwnerStaffMayNotAdvance                                               | Ownership rule of D2                                                          |
| TC-E-06  | The assigned Staff member may advance                                  | 3 of 3 pass                                                                                  | ownerStaffMayAdvance                                                     | Positive side of D2                                                           |
| TC-E-07  | Management may make any legal move                                     | 4 of 4 pass                                                                                  | managementMayDoAnyLegalTransition                                        | D6                                                                            |
| TC-E-08  | A Requester is always refused                                          | Pass                                                                                         | requesterMayNeverChangeStatus                                            | Role rule only. HTTP behaviour is in section 7                                |
| TC-E-09  | An illegal move is rejected even for Management                        | Pass. Failed under fault A                                                                   | illegalTransitionRejectedEvenForManagement                               | Management cannot bypass the table                                            |
| none yet | Decision for every role and ownership case                             | 23 of 23 pass. One failed under fault A                                                      | StatusTransitionDecisionTest                                             | Needs TC IDs if it is to be cited in the RTM                                  |
| TC-E-12  | Required fields are present                                            | Pass. Errored under fault C                                                                  | RequesterListTest, listShowsRequiredFields                               | Shows the response mapping, not the HTTP call                                 |
| TC-E-13  | Only the signed in user's requests are returned                        | Pass. Failed under fault C (another user's request returned)                                 | requesterOnlyReceivesOwnRequests                                         | Shows the query uses the signed in user's id                                  |
| TC-E-14  | An unknown user gets 401 and nothing is read                           | Pass                                                                                         | unknownUserIsRejected                                                    | Service level only. A signed out HTTP call returned 403 (observation 1)       |
| TC-E-15  | Update first, then one history entry                                   | Pass. Failed under fault B. Live call: 200, version 1                                        | RequestStatusServiceTest, legalChangeUpdatesThenRecordsHistory           | Order of calls is correct. Database effect is shown only by section 7         |
| TC-E-16  | 400 and no write                                                       | Pass. Failed under fault A through a NullPointerException, not its assertion. Live call: 400 | illegalChangeWritesNothing                                               | Detects a wrong legality rule, but only by accident (observation 2)           |
| TC-E-17  | 403 and no write                                                       | Pass. Live call: 403                                                                         | requesterIsForbiddenAndNothingIsWritten                                  | Permission is checked before the write                                        |
| TC-E-18  | 403 and no write                                                       | Pass. Live call: 403                                                                         | staffWhoDoNotOwnTheRequestAreForbidden                                   | Ownership is enforced                                                         |
| TC-E-19  | 409 and no write                                                       | Pass. Live call: 409. Did not fail under fault B                                             | staleVersionIsAConflict                                                  | Shows the stale version check. Hibernate's own version check is not exercised |
| TC-E-20  | No history entry when the update fails                                 | Pass. Also passes if the audit call is removed                                               | failedUpdateWritesNoHistory                                              | Protects the failure path only                                                |
| TC-E-21  | 404 and no write                                                       | Pass. Live call: 404                                                                         | unknownRequestIsNotFound                                                 |                                                                               |
| TC-E-22  | 400 pointing to the assign action                                      | Pass. Live call: 400                                                                         | assigningIsHandledElsewhere                                              | Assignment is not handled by this endpoint                                    |
| TC-E-23  | Management may move forward, actor recorded                            | Pass. Failed under fault B. Live call: 200, version 2, history row by Management             | managementMayMoveAnyRequestForward                                       | D6                                                                            |
| TC-E-24  | One row with from, to, actor, time and note                            | Pass                                                                                         | AuditLogObserverTest, recordsOneRowWithAllFields                         | Observer output with a mocked repository                                      |
| TC-E-25  | All seven valid categories accepted                                    | 11 validation tests + service create path pass                                               | CreateRequestBodyValidationTest + ServiceRequestServiceCreateTest (#119) | FR001 valid class now automated                                               |
| TC-E-26  | Missing category rejected                                              | Pass (null / empty / blank)                                                                  | CreateRequestBodyValidationTest (#119)                                   | FR001 main acceptance criterion now automated                                 |
| TC-E-27  | Null, empty and blank category rejected                                | Pass                                                                                         | CreateRequestBodyValidationTest (#119)                                   |                                                                               |
| TC-E-28  | Category not on the list rejected                                      | Pass (400, no save)                                                                          | ServiceRequestServiceCreateTest (#119)                                   | Exact labels only (D4)                                                        |
| TC-E-29  | Title length 0, 1, 200, 201 + trim behaviour                           | Pass                                                                                         | CreateRequestBodyValidationTest (#119)                                   | Limit is 200 characters; trim happens before the check                        |
| TC-E-30  | Both rows saved in one transaction                                     | Not run                                                                                      |                                                                          | Needs a PostgreSQL test. Supporting evidence in section 7                     |
| TC-E-31  | No history row after a rejection                                       | Not run                                                                                      |                                                                          | Supporting evidence in section 7                                              |
| TC-E-32  | Status badge/banner updates on My Requests within ~5s after staff change | Pass (manual demo ~4s). Unit: 6/6 pass | statusChanges.test.js + two-window demo (section 11) | FR004/NFR001 in scope; poll every 4s |

## 6. Deliberate fault checks (run D)

Each fault was put into the code on purpose, the suite was run, and the file was restored with git restore. After each restore the suite passed again (76 tests).

| Fault                                                              | Result                                                                                                                                                                                                                                                                 |
| ------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A: the guard allows Open to Closed                                 | 5 failures: skippedStatusesAreRejected (TC-E-02), exactlyFourOfTwentyFivePairsAreLegal (TC-E-03b), illegalTransitionRejectedEvenForManagement (TC-E-09), illegalMovesAreIllegal (decision tests), illegalChangeWritesNothing (TC-E-16, through a NullPointerException) |
| B: the audit call is removed from the status change service        | 2 failures: legalChangeUpdatesThenRecordsHistory (TC-E-15), managementMayMoveAnyRequestForward (TC-E-23)                                                                                                                                                               |
| C: the list reads another user's id                                | 1 failure, 1 error: requesterOnlyReceivesOwnRequests (TC-E-13) failed. listShowsRequiredFields (TC-E-12) errored on a Mockito strict stubbing check                                                                                                                    |
| E: the newest first sort is removed (run on a main based checkout) | Suite still passed (66 tests, RequesterListTest ran 3). The behaviour had no test. A newest first test, planned as TC-E-14b, is not yet merged                                                                                                                         |

## 7. Live API check and database evidence (run C)

The calls were made on 14/10/2026 against the running application and the local PostgreSQL database. The cleaned record is in docs/testing/live-check-2026-10-14.txt. The status calls were made against POST /requests/{id}/status from PR #115, with the body fields toStatus, expectedVersion and note.

| Call                                                                                | Expected                                     | Actual                                                                                        |
| ----------------------------------------------------------------------------------- | -------------------------------------------- | --------------------------------------------------------------------------------------------- |
| GET /requests/mine, signed out (TC-E-14)                                            | 401 or 403                                   | 403                                                                                           |
| POST /requests, seven valid labels (TC-E-25)                                        | 201 each                                     | 201 each                                                                                      |
| POST /requests, no category (TC-E-26)                                               | 400                                          | 400                                                                                           |
| POST /requests, null, empty, blank category (TC-E-27)                               | 400 each                                     | 400 each                                                                                      |
| POST /requests, Plumbing, Facility Faults, facility fault, FACILITY_FAULT (TC-E-28) | 400 each                                     | 400 each                                                                                      |
| POST /requests, title length 0, 1, 200, 201 (TC-E-29)                               | 400, 201, 201, 400                           | 400, 201, 201, 400                                                                            |
| GET /requests/mine as requester and as staff (TC-E-12, TC-E-13)                     | Requester sees own requests, staff sees none | Requester: 200 with own requests, newest created request first. Staff: 200 with an empty list |
| Status change Assigned to Closed by the owner (TC-E-16)                             | 400                                          | 400                                                                                           |
| Status change by a requester (TC-E-17)                                              | 403                                          | 403                                                                                           |
| Status change by Staff who does not own the request (TC-E-18)                       | 403                                          | 403                                                                                           |
| Status change on an unknown id (TC-E-21)                                            | 404                                          | 404                                                                                           |
| Open to Assigned through the status endpoint (TC-E-22)                              | 400                                          | 400                                                                                           |
| Owner moves to InProgress, expected version 0 (TC-E-15)                             | 200, version 1                               | 200, InProgress, version 1                                                                    |
| Same call again with version 0 (TC-E-19)                                            | 409                                          | 409                                                                                           |
| Management moves to Resolved (TC-E-23)                                              | 200                                          | 200, Resolved, version 2                                                                      |

Database results from pgAdmin:

* service_requests: request 1 is Resolved at version 2. Requests 2 and 3 remained unchanged after rejected attempts.
* status_history holds exactly two rows for request 1. No rows were written for the 400, 403, 404 or 409 attempts.
* UPDATE on status_history was refused by the append-only trigger.

Limits: the check was manual, ran once on local demo data, and cannot be repeated by CI. The append-only trigger exists in the hand-built local database. The V1 migration does not create it (observation 3).

## 8. Decisions

Open:
* Whether creating a request writes an initial Open history row.
* The database test approach (Testcontainers or a PostgreSQL service in CI). TC-E-30 and TC-E-31 depend on it.

Closed (18/10/2026):
* NFR001 / FR004 stay in scope. Implemented as 4-second polling on My Requests (feature/requester-live-status).

## 9. Observations and defect candidates

1. A signed out request to GET /requests/mine returned 403, not 401. Severity low. Owner: access control (Donya).
2. TC-E-16 detects a wrong legality rule only through a NullPointerException. It should be made less brittle.
3. The V1 migration has from_status NOT NULL and no append-only triggers, so the database-level protection of NFR004 exists only in the hand-built local database.
4. The newest-first behaviour had no test until fault E. TC-E-14b is written but not merged.

## 10. Limits of this evidence

* Tests marked Service (mocks) show the order of calls and that rejected changes do not reach the write path. They do not show what the database does.
* The manual API check is one run on local data and is not automated.
* Run B covers PR #115. These rows should be re-run on main after it merges.
* Runs were made on a developer machine. The CI run reference is still to be added.
* Mockito prints a self-attaching warning under Java 25/26. It is a warning and does not affect results.

## 11. NFR005 and FR004 / NFR001 checks (run F — 18/10/2026)

### 375 px (NFR005 / DEF-E01)
* Date: 18/10/2026
* Tool: Chrome DevTools, 375 px width
* Screens: Submission form, My requests (with live status refresh)
* Result: No horizontal scroll on either screen
* Status: Pass — DEF-E01 remains Fixed

### Live status refresh (FR004 / NFR001)
* Branch: feature/requester-live-status (commit 19ceeec)
* Implementation: poll GET /requests/mine every 4 seconds; pure helper `detectStatusChanges`; info banner when status changes
* Unit tests: `statusChanges.test.js` — 6 cases, all pass (npm test: 17 pass, 0 fail)
* Demo: two windows (requester on My requests + staff status change) against local PostgreSQL
* Result: status badge and banner updated within about 4 seconds
* Status: Implemented; PR open for review
