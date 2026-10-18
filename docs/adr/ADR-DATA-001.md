# ADR-DATA-001: Atomic Transaction and Optimistic Concurrency

**Status:** Decided — M2

## Context / Problem
A status change touches two records: it updates `ServiceRequest.status` and adds a new `StatusHistory` row. If only one write succeeds, the audit trail (NFR004) is broken — a history row for a change that never happened is as wrong as a change with no history row. Concurrent staff updates (assign FR008 or status FR009) can silently overwrite each other without a safeguard, leaving an incomplete audit log.

**Constraints:** CON003 (small team, free tools — design must stay simple); CON005 (audit trail must be designed in from the start).

## Alternatives considered
1. **Pessimistic row locking** — rejected; blocks other staff while a request is being edited.
2. **Last write wins** — rejected; silently loses updates.
3. **Commit status first and write the audit later via an outbox (A2 Approach B)** — rejected for M2; the audit could lag and it needs extra infrastructure.
4. **Single local transaction + optimistic `version` (A2 Approach A — chosen)** — related writes are all-or-nothing; conflicts are assumed rare.

## Decision
Wrap every status change and assignment in **one database transaction**. Add a `version` integer to `ServiceRequest` that covers every write to it. Update with `WHERE id = ? AND version = ?`. Order:

1. **Guard** — `StatusTransitionGuard` checks that the transition is legal (FR009) and that the user is allowed to make it (FR011). If not, reject before any write.
2. **Conditional update** — update `ServiceRequest` and increment `version`.
3. **Row check** — if zero rows were updated, raise a conflict error and roll back; the audit observer never runs.
4. **Audit** — only after exactly one row is updated does `AuditLogObserver` write the `StatusHistory` row inside the same transaction.
5. **Commit** — status, version and history are saved together.
6. **Notification** — requester visibility (FR004) runs only after commit; if it fails, the status change stays saved and the failure is logged.

Database constraints act as a backstop (CHECK on status values; FK from `StatusHistory` to `ServiceRequest`). `StatusHistory` remains append-only under normal actions.

This follows A2 Task 2 research: a transaction makes related writes all-or-nothing; optimistic concurrency assumes conflicts are rare. Pessimistic locking was rejected because it holds the row until the first user finishes.

## Benefit
NFR004 correctness is a property of the write path: legal transitions write request + history together; illegal or conflicted transitions write zero `StatusHistory` rows. Aligns with A2 Task 2 Approach A and ADR-DESIGN-001.

## Trade-off / complexity introduced
Each update carries a small version-check overhead. Conflict errors must be handled in the UI (“reload and try again”). Accepted because updates are infrequent and NFR004 correctness matters more than write speed.

**Risks:** Staff may see conflict errors on busy requests — mitigation: clear “reload and try again” messaging. A new code path could write to `ServiceRequest` without the version check — mitigation: all writes go through `ServiceRequestService`, and PR review checks for it.

## Affected modules/classes
**Implemented (M3):**

- `ServiceRequest` entity — JPA `@Version` optimistic-concurrency counter
- `StatusHistory` entity — append-only app path; postgres V2 triggers for DB-level append-only
- `ServiceRequestService` — transactional accept/status path (guard → flush → audit)
- `AuditLogObserver` — `MANDATORY` propagation; history only inside the status TX
- Flyway `V1__init_schema.sql` — status CHECK, indexes, `version` column

## RTM / implementation evidence
| Field | Content |
|---|---|
| RTM links | RTM-008 (FR008); RTM-009 (FR009); RTM-011 (FR011); RTM-017 (NFR004) |
| Related ADR | ADR-DESIGN-001 (Status Transition Guard + Observer) |
| Evidence | A2 Task 2 (5.2–5.3): transactions; optimistic concurrency; outbox rejected for M2; OWASP server-side validation. NFR004: history not editable or deletable by normal users. |
| Later consequence | **Done (M3):** `TC-A-INT-03` — two parallel status updates; one HTTP 409; exactly one extra `StatusHistory` row. |
| Application artefact | Implemented — JPA `@Version` (equivalent to `WHERE id = ? AND version = ?`) |
