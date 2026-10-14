# Quality Strategy – Status Lifecycle Slice

**PED Section 6 contribution**  
**Date**: 14 October 2026

## Risk-based focus

The strongest verification effort was placed on the status lifecycle and audit trail for these reasons:

- FR009, FR011 and NFR004 are High-priority requirements and sit on the critical path of every request.
- RISK-011 (illegal status skip or missing audit row) would destroy accountability and make the system non-auditable.
- The transition rules and Observer pattern are the main design decisions recorded in ADR-DESIGN-001. If they are wrong, the rest of the design is undermined.

## What received stronger verification

| Area                        | Why stronger verification                          | Techniques used                          |
|----------------------------|----------------------------------------------------|------------------------------------------|
| Transition legality (FR009)| Core state machine; easy to get wrong              | Unit + State Transition + Negative      |
| Audit row integrity (NFR004)| Append-only requirement; RISK-011                  | Unit (observer) + Negative (zero rows)  |
| Authorisation on Resolve/Close (FR011) | Separate sequential steps + role rules     | Unit + Decision Table (with Donya)      |
| Category validation (FR001)| First data quality gate                            | Equivalence Partitioning + BVA          |

## What received lighter verification

- Pure UI layout polish (except the 375 px NFR005 check).
- StatusVisibilityUpdater (NFR001) – still pending team decision on scope.
- Full multi-user concurrency load – belongs to Adolf’s performance/E2E slice.

## Link to evidence

Full test records: [status-lifecycle-test-evidence.md](./status-lifecycle-test-evidence.md)  
PR containing the implementation and tests: #115
