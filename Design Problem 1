

## Problem Statement

FR009 requires that status changes follow a fixed path: Open → Assigned → In Progress → Resolved → Closed. No step may be skipped. Invalid transitions must be blocked before they reach the database. When a status changes, two other parts of the system must react: the audit log must record the change (NFR004) and the requester must see the update without manually refreshing the page (NFR001). If all this logic lives inside one function, it becomes hard to test each part in isolation and hard to add new reactions later (for example, sending an email when a request is resolved).

## A2 Research Evidence

Assignment 2, Task 1 researched how to manage status transitions. Two main options were found.

**Option 1: switch-case / if-else chain.** This is simple to write but becomes hard to change. Every new transition or new side effect requires editing the same block of code. This increases the chance of breaking existing behaviour.

**Option 2: Observer pattern combined with a transition lookup table.** The lookup table is a map from each status to the list of valid next statuses. It defines the rules in one place. The Observer pattern separates the status change from its side effects. When a status changes, the Subject (the transition service) notifies each registered Observer in turn. Each Observer handles one concern: audit logging, UI update, and so on. This makes each concern independently testable (Gamma et al., 1995; Chikkanayakanahalli Ramachandrappa, 2024).

## Final Judgement

Our team chose Option 2: Observer pattern with a transition lookup table. The A2 recommendation matched the project need; it was adopted without modification. The transition lookup table enforces FR009 directly. Before any observer is notified, the service checks whether the requested transition is in the map. If it is not, the change is rejected. No state is written.

The `StatusTransitionService` acts as the Subject. Two observers are registered at M2. The `AuditLogObserver` writes the `StatusHistory` row inside the same transaction (ADR-DATA-001). The `StatusVisibilityUpdater` signals the UI layer to refresh the requester view (NFR001).

**Trade-off:** This adds an interface and an observer registration step that a simple function would not need. This overhead is small and accepted. The benefit is that each observer can be tested on its own. New observers, such as an email notification observer for RISK-007, can be added later without touching the transition logic or the transaction.

---

## ADR-DES-001 — Observer + Lookup Table for Status Transitions

| Field | Content |
| --- | --- |
| **ID** | ADR-DES-001 |
| **Status** | Decided — M2 |
| **Context** | FR009 requires controlled status transitions. NFR004 requires an immutable audit log. NFR001 requires that the requester sees status changes without a manual refresh. Multiple concerns react to a single status-change event. |
| **Alternatives considered** | 1) Single function with if-else checks and direct audit-log call. Simple, but becomes hard to change. 2) Observer and lookup table: separates concerns, enforces transition rules in one place, independently testable. |
| **Decision** | Observer pattern with a transition lookup table. `StatusTransitionService` is the Subject. Transition rules are stored in a `transitionRules` map. `AuditLogObserver` and `StatusVisibilityUpdater` are registered as Observers. |
| **Evidence** | A2 Task 1: Gamma et al. (1995) Observer pattern; Chikkanayakanahalli Ramachandrappa (2024) state-machine management. Project context: FR009, NFR001, NFR004, RISK-007. |
| **Trade-offs** | More classes than a single-function approach. Accepted because each observer is independently testable and the Subject does not need to change when a new observer is added. |
| **RTM links** | RTM-009 (FR009); RTM-014 (NFR001); RTM-017 (NFR004) |

---

## Application Evidence

The following classes implement this decision.

- **`StatusTransitionService`:** Subject; holds the `transitionRules` map and the observer list; validates transitions before notifying observers.
- **`AuditLogObserver`:** writes a `StatusHistory` row inside the active transaction when notified.
- **`StatusVisibilityUpdater`:** signals the UI layer to refresh the requester's status view.
- **`transitionRules` config:** defines the map: `Open → [Assigned]`, `Assigned → [InProgress]`, `InProgress → [Resolved]`, `Resolved → [Closed]`.
