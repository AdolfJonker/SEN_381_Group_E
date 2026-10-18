# Release Evidence Summary – M3 release candidate

| Field | Value |
|---|---|
| Owner | Donya Pretorius |
| Candidate | `v3.0.0-rc1` (tag to cut on main after the blockers below are closed) |
| Code baseline checked | main at cf5a4f2 (after #120), 15/10/2026 |
| Deployed SHA on staging | Not deployed yet. Add when Render staging is live (see `docs/staging/rollback.md`) |

## 1. Quality gate (CI)

| Check | Job | Blocks merge? | Result on main |
|---|---|---|---|
| Backend build + unit/API tests | Backend build + tests (`mvnw verify`) | Yes, once "Require status checks" is ticked in branch protection | Run link to add |
| Checkstyle | same job | No, warn only (TD-005) | 2 warnings (IllegalCatch in HealthController, AvoidStarImport in StatusTransitionGuard) |
| Frontend tests + build | Frontend tests + build | Yes | Run link to add |
| npm audit (high) | same job | No, report only | 0 vulnerabilities (13/10/2026) |
| Secrets scan | gitleaks | Yes | Run link to add |
| Dependency alerts | Dependabot (repo setting) | No | Screenshot to add |

Blocked-PR evidence: screenshot of a PR with a failing required check and the disabled merge button (to add).

## 2. Test evidence by member

| Owner | Evidence file | Tests | Result recorded |
|---|---|---|---|
| Elcke | `docs/testing/status-lifecycle-test-evidence.md` | TC-E-01…29 (+ 23-case decision test); TC-E-30…32 not run | Pass (runs A, B, E); manual live check 14/10 |
| Adolf | `docs/testing/adolf-test-evidence.md` | TC-A-01…09, TC-A-INT-01…04, TC-A-E2E-01/02, PERF-LIST-01 | Unit/integration pass; E2E and perf pass on local only |
| Donya | `docs/testing/access-control-test-evidence.md` | TC-D-01…04, TC-D-INT-01…04, TC-D-05, TC-D-06/07 | Pass (local, 14/10); TC-D-INT-01c to run |

Total backend test count on main: take it from the CI run (Surefire summary) and add it here. Do not add the counts from the separate runs; some runs overlap.

## 3. Release blockers and open items

| Item | Owner | Blocks release? |
|---|---|---|
| Demo accounts with a known password are created on every profile (`DemoUserLoader`), RISK-004 | Team (bootstrap code) | Yes, if the staging URL is public |
| Staging not deployed; E2E and performance only run locally | Adolf | Yes for staging evidence |
| CI run links and blocked-PR screenshot | Donya | Yes for the PED |
| TC-E-30/31 (PostgreSQL tests) not run; TD-006 | Elcke / Adolf | No, recorded as a limit |
| DEF-E01: horizontal scroll at 375 px on login and submit form (NFR005) | Elcke | No, recorded as a defect |
| FR004/NFR001 StatusVisibilityUpdater is an empty class | Team | No, scope decision open |

## 4. What this release does not prove

* Behaviour under real municipal load, or after a free-tier cold start.
* Database-level append-only protection on H2 (only the postgres V2 migration has it).
* Any result on staging, until staging is deployed and the E2E tests are re-run there.
