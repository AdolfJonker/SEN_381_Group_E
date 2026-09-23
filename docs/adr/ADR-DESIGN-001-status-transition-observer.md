# ADR-DESIGN-001: Status Transition Guard + Observer for Audit Logging

**Status:** Proposed

## Context / Problem
ServiceRequest.status must only move through the defined lifecycle (Open → Assigned → InProgress → Resolved → Closed per FR009), and every transition must produce an immutable audit record (NFR004) plus visible feedback to the requester (FR004) without staff needing a separate manual step. Validating that a transition is legal and reacting to a valid transition (writing history, future notifications) are distinct concerns that risk becoming entangled in a single service method.

## Alternatives considered (A2 Task 1)
- **Inline validation + inline side effects** — simplest, but couples transition logic to every consumer of a status change.
- **State/full State-machine pattern per status** — more formally correct but adds a class per state for a fixed, small, linear lifecycle; not justified by CivicConnect's scope (5 states, no branching).

## Decision
Adopt a lookup-table transition guard plus an Observer for side effects, in preference to a full State pattern, since the transition set is small and unlikely to grow. The guard validates legality first; on success it publishes to `AuditLogObserver`, which writes the `StatusHistory` row in the same transaction (ADR-DATA-001) and can later fan out to a notification observer for FR004 without changing the guard.

## Benefit
Decouples transition legality from transition consequences; new consequences (e.g. a future notification observer) can be added without touching the guard or existing observers. The lookup table is a single, testable source of truth for FR009.

## Trade-off / complexity introduced
An extra layer of indirection for what is currently a single observer (audit logging). Requires that the guard check and the Observer's write happen inside the same transaction as the optimistic-concurrency check (ADR-DATA-001), so an audit record is never written for a transition that does not actually commit.

## Affected modules/classes
- `StatusTransitionGuard` (new)
- `ServiceRequestService` (status-update method)
- `AuditLogObserver` and `StatusHistory` entity (per Entity Model)

## SPOF / architecture note
Keeping the Observer in-process (rather than an external message broker) avoids introducing a second single point of failure (see Section 5.3, Scalability and SPOF Notes); the design allows moving notifications out-of-process later without changing the core status logic.

## RTM / implementation evidence
Links to FR009, NFR004, FR004 (RTM-004, RTM-009, RTM-017). Implementation evidence: GitHub board #21 Status Transition Management and #23 In-Process Observer Seam.
