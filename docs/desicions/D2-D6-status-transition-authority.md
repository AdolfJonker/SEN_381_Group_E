# D2–D6: Status Transition Authority Rules

**Date:** 12 October 2026  
**Status:** Agreed  
**Related requirements:** FR008, FR009, FR011, NFR004  
**Related ADRs:** ADR-DESIGN-001  
**Owner:** Elcke (Status Lifecycle slice)  
**Agreed by:** Team (Elcke, Adolf, Donya)

## Purpose
These decisions define **who** may perform each legal status transition and what happens on an illegal attempt.  
They complete the permission rules that were left open in the original `StatusTransitionGuard` implementation.

## D2 – Who may perform each status transition

| From → To              | Allowed roles                                      | Notes |
|------------------------|----------------------------------------------------|-------|
| Open → Assigned        | Staff, Management                                  | Assignment / accepting responsibility (FR008) |
| Assigned → InProgress  | Assigned Staff member, Management                 | Only the person who owns the request or Management |
| InProgress → Resolved  | Assigned Staff member, Management                 | FR011 – authorised staff |
| Resolved → Closed      | Assigned Staff member, Management                 | FR011 – separate sequential step |
| Any other transition   | Nobody                                             | Illegal |

## D3 – Behaviour on illegal transition
- Return **HTTP 400 Bad Request** with a clear message.
- Write **zero** `StatusHistory` rows.
- Do **not** change the request status or version.

## D4 – Assignment behaviour (FR008)
- Setting `assignedToId` **must** go through the same transition guard (Open → Assigned).
- A request can only be assigned when its current status is `Open`.
- Once assigned, only the assigned staff member (or Management) may move it forward.

## D5 – Null / missing values
- `fromStatus` or `toStatus` = null → always illegal.
- Attempting to set status to null → illegal.

## D6 – Management override
- Management can perform **any** legal forward transition.
- Management may re-assign a request that is already in `Assigned` or `InProgress` (this is the only exception to the “only assigned staff” rule).
- Re-assignment still records a proper status-history entry if the status actually changes.

## Consequences
- The permission check will be added to `StatusTransitionGuard` (or the service layer) in a follow-up PR after this decision is recorded.
- All future tests for FR009 / FR011 must use these rules as the expected results.
- Any deviation from these rules requires a new controlled change + ADR update.
