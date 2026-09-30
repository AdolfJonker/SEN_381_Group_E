# ADR-DESIGN-001: Status Transition Guard + Observer for Audit Logging

**Status:** Proposed

## Context / Problem
ServiceRequest.status must only move through the defined lifecycle (Open → Assigned → InProgress → Resolved → Closed per FR009), and every transition must produce an immutable audit record (NFR004) plus visible feedback to the requester (FR004) without staff needing a separate manual step. Validating that a transition is legal and reacting to a valid transition (writing history, future notifications) are distinct concerns that risk becoming entangled in a single service method.

## Alternatives considered (A2 Task 1)
- **Inline validation + inline side effects** — simplest, but couples transition logic to every consumer of a status change.
- **State/full State-machine pattern per status** — more formally correct but adds a class per state for a fixed, small, linear lifecycle; not justified by CivicConnect's scope (5 states, no branching).

## Decision
Adopt a lookup-table transition guard plus the Observer pattern for side effects, in preference to a full State pattern, since the transition set is small and unlikely to grow. The guard validates legality first; on success it notifies two registered Observers: AuditLogObserver, which writes the StatusHistory row in the same transaction (ADR-DATA-001), and StatusVisibilityUpdater, which signals the UI layer to refresh the requester's status view without a manual page reload (NFR001, FR004). Additional Observers (e.g. an email notification observer for RISK-007) can be registered later without changing the guard or existing Observers.

## Benefit
Decouples transition legality from transition consequences; new consequences (e.g. an email notification observer for RISK-007) can be added without touching the guard or existing observers. The lookup table is a single, testable source of truth for FR009.

## Trade-off / complexity introduced
An extra layer of indirection for what is currently two observers (audit logging and UI visibility). Requires that the guard check and the Observers' writes happen inside the same transaction as the optimistic-concurrency check (ADR-DATA-001), so an audit record is never written for a transition that does not actually commit.

## Constraints
CON003 (no project budget, three-person team with limited combined capability) means the guard and 
Observers must be simple enough for all three team members to build, test and defend independently — not 
just the person who designed it. The lookup-table guard and two-Observer setup were chosen partly because 
they are small enough to meet this constraint; a full State-machine pattern would have added more classes 
than a three-person team can realistically maintain and explain at defence.

## Risks
A2 research flagged that event-driven/Observer mechanisms are harder to trace through code than direct 
method calls, since the trigger and the handler are decoupled. This creates a risk (linked to RISK-002: 
team member unavailable) that if only one team member fully understands the publish/subscribe flow, the 
others cannot maintain or defend it. Mitigation: the guard-then-notify sequence is documented explicitly 
in this ADR (see Decision, above) so any team member can trace a status change from request to audit 
record without needing to read the Observer implementations first.

## Later Consequence
To be updated in M3. Once implemented, M3 must verify the transition guard and each Observer independently 
(unit test the guard's allowed-transition table; separately verify AuditLogObserver writes StatusHistory 
only after a successful transaction commit, per the sequencing note in ADR-DATA-001; verify 
StatusVisibilityUpdater actually triggers a UI refresh for NFR001).

## Affected modules/classes
StatusTransitionGuard (new); ServiceRequestService (status-update method); AuditLogObserver and StatusHistory entity (per Entity Model); StatusVisibilityUpdater (new, signals UI refresh for NFR001).

## SPOF / architecture note
Keeping the Observers in-process (rather than an external message broker) avoids introducing a second single point of failure (see Section 5.3, Scalability and SPOF Notes); the design allows moving notifications out-of-process later without changing the core status logic.

## RTM / implementation evidence
Links to FR009, NFR004, FR004, NFR001 (RTM-004, RTM-009, RTM-014, RTM-017). Implementation evidence: GitHub board #21 Status Transition Management and #23 In-Process Observer Seam.
