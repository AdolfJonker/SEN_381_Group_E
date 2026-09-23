# ADR-DESIGN-002: Strategy-per-Role RBAC Filtering

**Status:** Proposed

## Context / Problem
NFR003 requires that a requester cannot retrieve another requester's data, and FR005 requires staff to see only requests relevant to their authorised role. `User.role` has three values (Requester/Staff/Management), each needing genuinely different filtering behaviour (own requests only; authorised-scope requests; full aggregate view for FR012/FR013). Hardcoded conditional branches work today but risk an accidental cross-role access leak as rules change.

## Alternatives considered (A2 Task 1)
- **Simple controlled-list validation** — appropriate for input validation (e.g. FR001 category), but does not fit access-control filtering, which must vary query/output behaviour per role rather than validate a fixed value.

## Decision
Adopt the Strategy pattern: `RequesterFilterStrategy`, `StaffFilterStrategy` and `ManagementFilterStrategy` each implement a shared `FilterStrategy` interface. The service layer resolves the correct strategy from `User.role` and delegates filtering to it, rather than branching on role inline.

## Benefit
Each role's access rule is isolated, testable and independently reviewable, supporting NFR003's defensibility and mitigating RISK-003 (RBAC implemented incorrectly). Adding a future role means adding a class, not editing existing role logic.

## Trade-off / complexity introduced
Three additional classes plus an interface for a fixed, small set of roles. Accepted because RBAC is explicitly security-critical (CON005). The service layer's role-to-strategy resolution step needs its own test, independent of each strategy's own filtering test.

## Affected modules/classes
- `FilterStrategy` (new interface)
- `RequesterFilterStrategy`, `StaffFilterStrategy`, `ManagementFilterStrategy` (new)
- `ServiceRequestService` (resolves and delegates)
- `User` entity (role field drives resolution)

## RTM / implementation evidence
Links to NFR003, FR005, FR012, FR013 (RTM-016, RTM-005, RTM-012, RTM-013). Implementation evidence: GitHub board #22 Role-Based Request Filtering.
