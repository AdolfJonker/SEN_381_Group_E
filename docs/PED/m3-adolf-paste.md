# PED v3.0 — Adolf paste pack (sections 2, 3, 4, 8 + submission block)

Copy each block into the matching Word PED section, then compile PDF.  
Authority: [SEN381_M3.pdf](../../SEN381_M3.pdf) — real cloud staging optional; simulated staging acceptable.

Source drafts also live in [`docs/m3-adolf-sections.md`](../m3-adolf-sections.md). Evidence: [`docs/testing/adolf-test-evidence.md`](../testing/adolf-test-evidence.md).

---

## Submission information block (PED beginning or end)

| Field | Value |
|---|---|
| Team | SEN381 Group E — Adolf Jonker, Elcke Van Der Berg, Donya Pretorius |
| GitHub repository URL | _(team controlled repo URL)_ |
| M3 release candidate | Tag **`v3.0.0-rc1`** (or exact commit SHA recorded in `docs/staging/rollback.md`; code baseline at close-out prep: `1dde26bee1b9c8d443438bb53629ea68d21c314f`) |
| PED version | v3.0 |
| RTM location | `docs/requirements/rtm.md` |
| CI / test evidence | `docs/testing/adolf-test-evidence.md`; CI on GitHub Actions |
| Staging / test environment | **Credible simulated staging** (assessor demo): Vite `http://127.0.0.1:5173` + Spring Boot `demo`/H2 on port `8081`. Start commands and walkthrough: `docs/staging/demo-prep.md`. Real cloud staging not used for freeze (optional per brief §14). |
| Release Evidence Summary | `docs/release/m3-release-evidence-summary.md` |
| Date | 15/10/2026 |

---

## 2. M2 conformance

M3 construction follows the M2 ADRs:

- **ADR-ARCH-001** — modular monolith; single deployable Spring Boot service + React UI.
- **ADR-TECH-001** — Java/Spring Boot, PostgreSQL, React/Vite.
- **ADR-DATA-001** — one transaction for status/assignment + history; optimistic `@Version`; HTTP 409 with “Reload and try again.”
- **ADR-DESIGN-001 / 002** — status guard/observer and RBAC strategies remain owned with Elcke/Donya; Adolf’s write path calls the guard before flush and the audit observer after a successful conditional update.

Where implementation uses JPA `@Version` instead of a hand-written `UPDATE … WHERE version = ?`, behaviour is equivalent (Hibernate emits the version predicate). No separate deviation ADR was required beyond this note.

---

## 3. Controlled changes

Release-candidate scope for Adolf’s slice includes FR006, FR008 (accept path), FR012 summary query check, FR013, RTM-008 concurrency, a credible staging/test environment, E2E, and the list performance scenario. None of these were cut.

**Staging decision (aligned to SEN381_M3.pdf §14):** M3 freeze uses a **credible simulated staging** environment (Vite + Spring `demo`/H2 on fixed ports). Real cloud staging (Render) artefacts remain in the repo (`Dockerfile`, `render.yaml`, `docs/staging/render-deploy.md`) but were **not** required for freeze and earn no automatic bonus marks. RISK-006 residual: free-tier cloud limits avoided for the submission baseline by using simulated staging.

**Verification status (15/10/2026):**

- Automated unit/integration for the slice: pass (see Adolf test evidence).
- Critical E2E (Playwright) against simulated staging: 3/3 pass (`BASE_URL=http://127.0.0.1:5173`).
- PERF-LIST-01 against simulated staging API: pass with 9 staff-visible rows (avg ~5.9 ms); no SLA claimed.
- Public Render E2E/PERF: not required for M3; optional if a public URL is desired later.

Release-candidate Git tag name: **`v3.0.0-rc1`**.

---

## 4. Construction and configuration

- **Backend:** Flyway `V1__init_schema.sql` (`chk_request_status`, indexes on `submitted_by_id` / `assigned_to_id`, `version`); postgres-only V2 append-only triggers (TD-006).
- **API:** List/detail/summary GETs owned by access-control `RequestViewController` (Option A). Adolf adds FR006/FR013 query params on that list via `RequestListService`, plus accept/status/comments write paths with version conflict → 409.
- **Frontend ownership:** API client (`VITE_API_BASE_URL`), staff detail (accept, comments, 409 reload message).
- **Simulated staging (M3 primary):** Spring profile `demo` (H2), `SERVER_PORT=8081`, Vite on `5173`; secrets via environment variables / defaults only (`.env` gitignored). Demo procedure: `docs/staging/demo-prep.md`. Staging-vs-dev: `docs/staging/staging-vs-dev.md`. Rollback/RC: `docs/staging/rollback.md`.
- **Optional cloud:** Docker multi-stage image (`Dockerfile`) + Render blueprint (`render.yaml`) documented for later public URL if needed.
- **Config:** `SPRING_PROFILES_ACTIVE`, `DB_JDBC_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `SERVER_PORT`.
- **Security note (TD-007):** API routes are authenticated/role-gated; `.anyRequest().permitAll()` remains only so packaged SPA client routes can load.

---

## 8. Performance and reliability

- **Scenario:** authenticated staff `GET /api/v1/requests` timed via `scripts/perf-list.mjs`; evidence in `docs/testing/performance-list-evidence.md`.
- **Recorded simulated-staging run (15/10/2026):** 10 sequential GETs; **9 rows**; avg **5.9 ms**; p95 **8.3 ms**; **0%** errors; environment `http://127.0.0.1:8081` (demo/H2). Not production load.
- **Target:** no contractual SLA; results are characterisation only (one small justified M3 performance exercise).
- **Reliability controls:** optimistic concurrency (RISK-010); transactional audit write; rollback note for bad RC checkout.
- **Limits:** H2 single-JVM; postgres trigger path deferred (TD-006). A green performance run does **not** prove municipal production load. Cloud free-tier sleep/memory (RISK-006) applies only if Render is used later.
