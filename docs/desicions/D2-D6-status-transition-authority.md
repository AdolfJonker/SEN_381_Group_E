# D2 to D6: Status Transition Authority Rules

| Field | Value |
|---|---|
| Date agreed | 12/10/2026 |
| Last updated | 14/10/2026 |
| Status | D2 to D6 agreed. The clarifications in section 2 are proposed and awaiting team confirmation |
| Related requirements | FR008, FR009, FR011, NFR004 |
| Related ADRs | ADR-DESIGN-001 |
| Related items | RTM-008, RTM-009, RTM-011, RISK-011, issues #70 and #72 |
| Owner | Elcke (status lifecycle slice) |
| Agreed by | Elcke, Adolf, Donya |

## Purpose

These decisions define who may perform each legal status transition and what happens on an illegal attempt. They complete the permission layer that ADR-DESIGN-001 left open. The transition table in ADR-DESIGN-001 and in StatusTransitionGuard is unchanged.

## 1. Agreed decisions

### D2: Who may perform each status transition

| Transition | Allowed roles | Notes |
|---|---|---|
| Open to Assigned | Staff, Management | Assignment or accepting responsibility (FR008) |
| Assigned to InProgress | Assigned Staff member, Management | Only the person who owns the request, or Management |
| InProgress to Resolved | Assigned Staff member, Management | FR011, authorised staff |
| Resolved to Closed | Assigned Staff member, Management | FR011, separate sequential step |
| Any other transition | Nobody | Illegal |

### D3: Behaviour on an illegal transition

* Return HTTP 400 Bad Request with a clear message.
* Write zero StatusHistory rows.
* Do not change the request status or version.

### D4: Assignment behaviour (FR008)

* Setting assignedToId must go through the same transition guard (Open to Assigned).
* A request can only be assigned when its current status is Open.
* Once assigned, only the assigned staff member or Management may move it forward.

### D5: Null or missing values

* A null fromStatus or toStatus is always illegal.
* Attempting to set the status to null is illegal.

### D6: Management override

* Management can perform any legal forward transition.
* Management may reassign a request that is already Assigned or InProgress. This is the only exception to the rule that only the assigned staff member may act on a request.
* Reassignment still records a proper status history entry if the status actually changes.

## 2. Proposed clarifications (awaiting team confirmation in issue #70)

These points were found when D2 to D6 were checked against the requirements and the planned tests. They are not agreed decisions yet. Once confirmed, they move into section 1 and this heading is removed.

* D4 and D6 overlap. D4 is taken to apply to the first assignment of a request, and D6 to reassignment by Management.
* Reassignment by Management changes the assignee only. The status does not change, so no StatusHistory row is written and the request version increases by one. This is recorded in the Technical Debt Register, because reassignment is then not part of the audit trail.
* Staff may assign a request only to themselves. Management may assign a request to any Staff member. A request is never assigned to a Requester.
* A legal transition attempted by a user who is not permitted returns HTTP 403 Forbidden. A user who is not signed in receives HTTP 401 Unauthorized. Permission is checked before legality. In every case zero StatusHistory rows are written and the status and version are unchanged.
  * Implementation note (C4 / issue #70): staff visibility is enforced first via `requireVisible`, which returns HTTP 404 for requests another staff member already owns. Non-assigned staff therefore never reach the transition guard, so the staff-specific 403 ("Only the staff member assigned to this request…") is unreachable through the API for that case. The 403 still applies for roles that can see the request but are not allowed to change it (for example a Requester).

## 3. Consequences

* The permission check is added to StatusTransitionGuard or the service layer in a follow up PR (issue #72), after these decisions are recorded.
* All tests for FR009 and FR011 use these rules as the expected results.
* Any deviation from these rules requires a new controlled change and an ADR update.

## 4. Still open, tracked in issue #70

* Whether creating a request writes an initial Open history row. The V1 migration currently has from_status NOT NULL.
* The title length limit. The entity and the V1 migration both say 200 characters.
