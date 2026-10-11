# Status Lifecycle and Submission Test Evidence

| Field | Value |
|---|---|
| Owner | Elcke Van Der Berg |
| Covers | FR001, FR009, FR011, NFR004 (RTM-001, RTM-009, RTM-011, RTM-017) |
| Document status | Test design complete. Tests not yet executed |
| Release candidate | To be confirmed at evidence freeze |
| Last updated | 11/10/2026 |

## 1. Purpose and scope

This record holds the test design and execution evidence for the status lifecycle and request submission behaviour. It supports PED v3.0 section 7 and the test references in the RTM. Every expected result was written before any test was executed, and each one comes from the requirement, acceptance criterion or design decision named in its Basis line. Actual result and Status stay as Not yet run until the test is executed against the release candidate.

## 2. Risk basis and technique choice

* The status model has five states and 25 ordered pairs of states. This is small enough to test exhaustively, so every pair is covered.
* State transition testing is used for FR009 because the outcome of a status change depends on the current state.
* The audit write is verified at service level with one positive and one negative case, because a missing or extra history row breaks NFR004.
* Equivalence partitioning is used for the FR001 category because the input has seven valid values and many invalid ones, and the database holds a constraint as a backstop.
* Boundary value analysis is used for title length only if the team agrees a limit. The technique is applied where a real limit exists.
* The decision table for who may set Resolved or Closed is designed jointly with Donya and will be recorded with the access control tests once the authorisation rule is agreed.

## 3. Summary

| ID | Title | Technique | Level | Basis | RTM | Status |
|---|---|---|---|---|---|---|
| TC-E-01 | Legal forward steps accepted | State transition | Unit | FR009 | RTM-009 | Not yet run |
| TC-E-02 | Skipped statuses rejected | State transition | Unit | FR009, RISK-011 | RTM-009 | Not yet run |
| TC-E-03 | Backwards, same state and out of Closed rejected | State transition | Unit | FR009 | RTM-009 | Not yet run |
| TC-E-04 | Legal change writes request and history together | Service level | Component | NFR004, ASR-02 | RTM-017 | Not yet run |
| TC-E-05 | Illegal change writes zero history rows | Negative, service level | Component | ASR-02, ASR-03, RISK-011 | RTM-009, RTM-017 | Not yet run |
| TC-E-06 | Notification failure does not undo a saved change (conditional) | Failure behaviour | Component | ADR-DATA-001, RISK-007 | RTM-004 | Not yet run |
| TC-E-07 | All seven valid categories accepted | Equivalence partitioning | Black box | FR001 | RTM-001 | Not yet run |
| TC-E-08 | Missing category rejected | Equivalence partitioning | Black box | FR001 | RTM-001 | Not yet run |
| TC-E-09 | Null, empty and blank category rejected | Equivalence partitioning | Black box | FR001 | RTM-001 | Not yet run |
| TC-E-10 | Category not on the list rejected | Equivalence partitioning | Black box | FR001 | RTM-001 | Not yet run |
| TC-E-11 | Title length limits (conditional) | Boundary value analysis | Black box | FR001, schema | RTM-001 | Not yet run |

## 4. Test records

### TC-E-01 Legal forward steps accepted
* Basis: FR009 state model, ADR-DESIGN-001.
* Why selected: if a legal step is refused, staff cannot complete the lifecycle.
* Technique and level: state transition, unit test of the guard.
* Input and precondition: the four pairs Open to Assigned, Assigned to InProgress, InProgress to Resolved, Resolved to Closed, each checked on its own.
* Expected result: all four are allowed.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-009.
* Interpretation: shows the lookup table allows the full legal path. It does not show who may make each move or that the service calls the guard.

### TC-E-02 Skipped statuses rejected
* Basis: FR009 acceptance criterion (skipping a state is prevented or flagged), RISK-011.
* Why selected: a skip such as Open to Closed breaks ownership and accountability.
* Technique and level: state transition, unit test of the guard.
* Input and precondition: Open to InProgress, Open to Resolved, Open to Closed, Assigned to Resolved, Assigned to Closed, InProgress to Closed.
* Expected result: all six are rejected with a reason.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-009, RISK-011.
* Interpretation: shows the table refuses every forward skip. It does not show that the service refuses the change before writing, which TC-E-05 checks.

### TC-E-03 Backwards, same state and out of Closed rejected
* Basis: FR009. The state model has no reopen transition.
* Why selected: a backwards move would rewrite the lifecycle and the history would stop describing what happened.
* Technique and level: state transition, unit test of the guard.
* Input and precondition: ten backwards pairs (Assigned to Open, InProgress to Open, InProgress to Assigned, Resolved to Open, Resolved to Assigned, Resolved to InProgress, Closed to Open, Closed to Assigned, Closed to InProgress, Closed to Resolved) and five same state pairs (each status to itself).
* Expected result: all fifteen are rejected.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-009.
* Interpretation: with TC-E-01 and TC-E-02 this covers all 25 pairs, so no transition rests on a sample. It does not cover permissions.

### TC-E-04 Legal change writes request and history together
* Basis: NFR004, ASR-02, ADR-DATA-001.
* Why selected: the status update and its audit row must succeed or fail together.
* Technique and level: component test of the service against the real database.
* Input and precondition: a request in Open and a legal move to Assigned by an authorised staff member. Record the history row count before the call.
* Expected result: status is Assigned, version has increased by 1, and exactly one new history row exists with from Open, to Assigned, the acting user and a timestamp.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-017, RTM-009.
* Interpretation: shows the happy path writes both records. It does not show behaviour under concurrent updates, which is covered in the integration tests.

### TC-E-05 Illegal change writes zero history rows
* Basis: ASR-02, ASR-03, RISK-011, ADR-DATA-001 (guard runs before any write).
* Why selected: an audit row for a change that never happened is as wrong as a change with no row.
* Technique and level: negative test, component test of the service against the real database.
* Input and precondition: a request in Open and an attempted move to Closed by an authorised staff member. Record the history row count and version before the call.
* Expected result: the request is rejected with a clear error, status is still Open, version is unchanged and the history row count is unchanged.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-009, RTM-017, RISK-011.
* Interpretation: shows a rejected change leaves no trace in the data. It does not show every other code path is blocked, so any new path that writes status needs its own test.

### TC-E-06 Notification failure does not undo a saved change (conditional)
* Basis: ADR-DATA-001 (notification runs after commit), ADR-DESIGN-001, RISK-007.
* Why selected: a failed notification must never lose a saved status change.
* Technique and level: failure behaviour, component test. Counted only if the StatusVisibilityUpdater is built.
* Input and precondition: a legal change where the notification step is made to fail.
* Expected result: the status change and history row remain saved, the failure is logged and the caller receives a success response.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-004, RTM-014, RISK-007.
* Interpretation: shows notification is isolated from the transaction. It does not show requesters see the new status in time, which is an NFR001 check.

### TC-E-07 All seven valid categories accepted
* Basis: FR001 acceptance criterion (successful submission returns a unique request ID and confirmation), schema chk_request_category.
* Why selected: proves every valid category is accepted and stored in a form the database constraint accepts.
* Technique and level: equivalence partitioning (valid class), black box through the API.
* Input and precondition: one submission for each of the seven labels exactly as written: Facility Fault, Damaged Equipment, Security Concern, IT Support, Maintenance, Lost Property, Other, with a valid title and description.
* Expected result: each is accepted, a unique request ID and a confirmation are returned, status is Open and the stored category equals the label.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-001.
* Interpretation: shows valid input is handled end to end. It does not show invalid input is blocked, which TC-E-08 to TC-E-10 check.

### TC-E-08 Missing category rejected
* Basis: FR001 acceptance criterion (submission is rejected if category is missing).
* Why selected: this is the main acceptance criterion of FR001.
* Technique and level: equivalence partitioning (invalid class, absent), black box through the API.
* Input and precondition: a submission with a valid title and description and no category field.
* Expected result: rejected with a client error and no request is saved.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-001.
* Interpretation: shows the API enforces the criterion. It does not show the database rejects bad data if the API check is bypassed.

### TC-E-09 Null, empty and blank category rejected
* Basis: FR001.
* Why selected: a field that is present but empty is a different failure from an absent field.
* Technique and level: equivalence partitioning (invalid class, empty), black box through the API.
* Input and precondition: category set to null, to an empty string and to spaces only.
* Expected result: all three are rejected and no request is saved.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-001.
* Interpretation: shows empty values are treated as missing.

### TC-E-10 Category not on the list rejected
* Basis: FR001, schema chk_request_category.
* Why selected: only the controlled list may be accepted.
* Technique and level: equivalence partitioning (invalid class, not listed), black box through the API.
* Input and precondition: Plumbing and Facility Faults. If decision D4 is exact labels only, also facility fault in lower case and FACILITY_FAULT.
* Expected result: all are rejected and no request is saved.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-001.
* Interpretation: shows values outside the list are blocked. The lower case and constant name inputs are provisional until D4 is decided.

### TC-E-11 Title length limits (conditional)
* Basis: FR001 and the schema (title is required and holds up to 255 characters). Applies only if decision D5 confirms the limit.
* Why selected: limits are where length checks usually fail.
* Technique and level: boundary value analysis, black box through the API.
* Input and precondition: titles of length 0, 1, 255 and 256 with a valid description and category.
* Expected result: lengths 0 and 256 are rejected. Lengths 1 and 255 are accepted.
* Actual result: Not yet run.
* Status: Not yet run.
* Evidence: test class and CI run to be added.
* Traceability: RTM-001.
* Interpretation: shows both edges of the title limit behave correctly. It does not cover other fields.

## 5. Open decisions these expected results depend on

* D2 (who may make each status change) affects TC-E-04 and TC-E-05.
* D3 (whether creating a request logs an initial Open history row) affects the row counts in TC-E-04 and TC-E-05.
* D4 (strict or lenient category matching) affects TC-E-10.
* D5 (title length limit) decides whether TC-E-11 exists.

## 6. Limits of this evidence

* Guard tests show the rules are correct. They do not show the service always calls the guard, so the service level tests carry that claim.
* Tests run on test data in a single database. Concurrent updates are covered by the integration tests.
* This record does not cover screen layout or usability at 375px width.
