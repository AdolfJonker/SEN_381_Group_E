# ADR-DESIGN-001: Status Transition Guard + Observer for Audit Logging

**Status:** Proposed

## Context / Problem
FR009 requires that ServiceRequest.status only moves through a fixed path (Open to Assigned to InProgress to Resolved to Closed) and that no step is skipped. Invalid transitions must be blocked before they reach the database. When a status changes, two other parts of the system must react. The audit log must record the change (NFR004) and the requester must see the update without manually refreshing the page (NFR001, FR004), and staff should not need a separate manual step to trigger that visibility. If all this logic lives inside one function, it becomes hard to test each part in isolation and hard to add new reactions later, for example sending an email when a request is resolved.

## Alternatives considered (A2 Task 1)
- **Inline validation with inline side effects (switch case or if else chain)** is simple to write but becomes hard to change. Every new transition or new side effect requires editing the same block of code, which increases the chance of breaking existing behaviour.
- **State or full State machine pattern per status** is more formally correct but adds a class per state for a fixed, small, linear lifecycle. This is not justified by CivicConnect's scope of five states with no branching.

## Decision
Adopt a lookup table transition guard plus the Observer pattern for side effects, rather than a full State pattern, since the transition set is small and unlikely to grow. The lookup table is a map from each status to its list of valid next statuses, and it defines the rules in one place.

`StatusTransitionGuard` checks the requested transition against this map before anything is written. If the transition is not in the map, the change is rejected and no state is written.

`ServiceRequestService` plays the Subject role from the Observer pattern. Once the guard confirms a transition is legal, `ServiceRequestService` notifies two registered Observers in turn. `AuditLogObserver` writes the StatusHistory row in the same transaction as the status change (ADR-DATA-001). `StatusVisibilityUpdater` signals the UI layer to refresh the requester's status view (NFR001, FR004). Each Observer handles one concern only, so each can be tested on its own.

Additional Observers, such as an email notification observer for RISK-007, can be registered later without changing the guard, the Subject, or existing Observers.

## Benefit
Decoupling transition legality from transition consequences means new consequences, such as an email notification observer for RISK-007, can be added without touching the guard or existing observers. The lookup table is a single, testable source of truth for FR009, and each Observer can be tested in isolation.

## Trade-off and complexity introduced
This adds an interface and an observer registration step that a single function would not need. The overhead is small and accepted. The guard check and each Observer's write must happen inside the same transaction as the optimistic concurrency check (ADR-DATA-001), so an audit record is never written for a transition that does not actually commit.

## Constraints
CON003 (no project budget, three person team with limited combined capability) means the guard and Observers must be simple enough for all three team members to build, test and defend independently, not just the person who designed it. The lookup table guard and two Observer setup were chosen partly because they are small enough to meet this constraint. A full State machine pattern would have added more classes than a three person team can realistically maintain and explain at defence.

## Risks
A2 research flagged that event driven or Observer mechanisms are harder to trace through code than direct method calls, since the trigger and the handler are decoupled. This creates a risk, linked to RISK-002 (team member unavailable), that if only one team member fully understands the publish and subscribe flow, the others cannot maintain or defend it. Mitigation: the guard then notify sequence is documented explicitly in this ADR under Decision, so any team member can trace a status change from request to audit record without needing to read the Observer implementations first.

## Later Consequence
To be updated in M3. Once implemented, M3 must verify the transition guard and each Observer independently. This includes a unit test of the guard's allowed transition table, a separate check that AuditLogObserver writes StatusHistory only after a successful transaction commit per the sequencing note in ADR-DATA-001, and a check that StatusVisibilityUpdater actually triggers a UI refresh for NFR001.

## Affected modules and classes
- `StatusTransitionGuard` (new). Holds the transition lookup table and validates each requested transition before anything is written.
- `ServiceRequestService`. Plays the Subject role in the Observer pattern. Calls the guard, performs the status update, and notifies registered Observers on success.
- `AuditLogObserver` and the `StatusHistory` entity (per Entity Model). Writes the audit row inside the active transaction.
- `StatusVisibilityUpdater` (new). Signals the UI layer to refresh the requester's status view for NFR001.

## SPOF and architecture note
Keeping the Observers in process, rather than using an external message broker, avoids introducing a second single point of failure (see Section 5.3, Scalability and SPOF Notes). The design allows notifications to move out of process later without changing the core status logic.

## RTM and implementation evidence
Links to FR009, NFR004, FR004 and NFR001 (RTM-004, RTM-009, RTM-014, RTM-017). Implementation evidence: GitHub board #21 Status Transition Management and #23 In-Process Observer Seam.
