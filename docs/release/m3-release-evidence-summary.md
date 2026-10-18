# M3 Release Evidence Summary (Adolf slice + team pointers)

| Field | Value |
|---|---|
| Product | CivicConnect |
| Milestone | M3 — Controlled Construction, Integration, Quality & Release Readiness |
| Authority | [SEN381_M3.pdf](../../SEN381_M3.pdf) |
| Owner (this summary) | Adolf Jonker (Adolf-owned evidence); team PED/Moodle pack shared |
| Date | 15/10/2026 |
| Gate recommendation | **CONDITIONALLY READY** for M3 submission |

## Release candidate

| Item | Value |
|---|---|
| Intended tag | `v3.0.0-rc1` |
| Code baseline recorded | `1dde26bee1b9c8d443438bb53629ea68d21c314f` (`feature/m3-adolf-closeout` tip at close-out; tag the merge/commit that lands brief-alignment docs on the submission baseline) |
| Staging/test environment | **Credible simulated staging** — Vite `127.0.0.1:5173` + Spring `demo`/H2 on `8081` ([`docs/staging/demo-prep.md`](../staging/demo-prep.md)) |
| Public cloud staging | Not used for freeze (optional per brief §14; no automatic bonus) |

## Verification snapshot (Adolf-owned)

| Gate | Result | Evidence |
|---|---|---|
| Unit / component (Adolf) | Pass | TC-A-01…09 — Surefire |
| API / integration (Adolf) | Pass | TC-A-INT-01…04 |
| E2E / system (≥2 required) | Pass (3/3) | Playwright against simulated staging, 2026-10-15 |
| Performance (1 scenario) | Pass | PERF-LIST-01 — 9 rows, avg 5.9 ms, 0% errors |
| Full backend suite (local) | 116 tests, 0 failures | 2026-10-15 after DEF-A01…A04 closure |
| CI quality gate | Build + tests block on failure | `.github/workflows` (team CI) |

Detail: [`docs/testing/adolf-test-evidence.md`](../testing/adolf-test-evidence.md).

## Blockers

None open for Adolf-owned M3 minimum evidence.

## Accepted limitations

| ID | Limitation | Residual risk |
|---|---|---|
| TD-006 | H2 demo does not run postgres append-only triggers | RISK-related NFR004 DB path proven on postgres profile / later CI |
| TD-007 | SPA client routes use `permitAll`; API remains authenticated | Misconfiguration of static vs API paths — monitored |
| PERF | Characterisation only; no contractual SLA | Does not prove municipal production load |
| Cloud | Public Render URL not published | RISK-006 mitigated by simulated staging per brief |

## Residual risks (release readiness)

- RISK-006 — free-tier cloud limits: **accepted / mitigated** by using simulated staging for M3 (cloud optional).
- RISK-010 — concurrency: mitigated by ADR-DATA-001; H2 single-JVM concurrency only.
- RISK-003 / NFR003 — RBAC: covered by Adolf E2E deny cases + teammate access-control suite.

## Verdict

**CONDITIONALLY READY** — M3 minimum evidence is in place against a credible simulated staging environment and an identified RC identifier. Condition: create/tag `v3.0.0-rc1` (or state the exact submission SHA) and paste PED v3.0 from [`docs/PED/contributions/m3-adolf-paste.md`](../PED/contributions/m3-adolf-paste.md) before Moodle submission.
