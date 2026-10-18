# Defects & Quality Interpretation – Status Lifecycle Slice

**PED Section 9 contribution**  
**Date**: 14 October 2026

## Defects found in this slice

| ID     | Description                                      | Severity | Status     | Resolution / Notes                              | Regression test |
|--------|--------------------------------------------------|----------|------------|-------------------------------------------------|-----------------|
| DEF-E01 | Horizontal scroll / overflow at 375 px on login and submission form (NFR005) | Low     | Fixed      | Layout uses some fixed widths. To be fixed after form is stable. Fixed: 375 px recheck passed on 18/10/2026 (submission form and My requests; login, role landing and Management screens). | Manual 375 px check |
| DEF-E02 | Potential missing initial “Open” StatusHistory row on create | Medium  | Fixed      | Depends on team decision in #70. V1 schema has from_status NOT NULL. Fixed in #133: creating a request writes one initial Open history row (from_status allowed to be null, migration V3). | TC-E-07; TC-A-INT-01 |
| DEF-E03 | Reassignment by Management does not write a StatusHistory row | Low     | Accepted as Tech Debt | Recorded in Technical Debt Register. Status does not change. | — |

## Quality interpretation

The automated tests now protect the critical transition rules and the audit-observer behaviour against regression. Illegal transitions are rejected before any database write, and a successful transition always produces exactly one StatusHistory row inside the same transaction (ADR-DATA-001).

The most important remaining quality gaps in this slice are:

1. Responsive layout still shows horizontal scroll at 375 px on some screens (NFR005).
2. StatusVisibilityUpdater (NFR001/FR004) is not yet implemented – waiting on final scope decision.
3. The question of whether request creation should write an initial Open history row is still open in #70.

None of these are release-blocking for the core status-change capability, but they are recorded as residual limitations / technical debt.

**Final status (release v4.0.0, 18/10/2026):** all three gaps above are now closed. 1: the 375 px recheck passed (DEF-E01 fixed). 2: FR004/NFR001 were delivered by polling My Requests every 4 seconds (PR #126, DEC-006) instead of StatusVisibilityUpdater. 3: request creation now writes an initial Open history row (#133, DEF-E02 fixed). DEF-E03 stays accepted as technical debt.

## Link to full evidence

Test records: [status-lifecycle-test-evidence.md](./status-lifecycle-test-evidence.md)
