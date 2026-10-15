# PED M3 drafts — Adolf sections (paste into PED v3.0)

These are working drafts for the Word PED. Prefer the paste pack [`docs/PED/m3-adolf-paste.md`](PED/m3-adolf-paste.md) (includes submission info block). Authority: SEN381_M3.pdf — simulated staging acceptable; real cloud optional.

## 2. M2 conformance

M3 construction follows the M2 ADRs:

- **ADR-ARCH-001** — modular monolith; single deployable Spring Boot service + React UI.
- **ADR-TECH-001** — Java/Spring Boot, PostgreSQL, React/Vite.
- **ADR-DATA-001** — one transaction for status/assignment + history; optimistic `@Version`; HTTP 409 with “Reload and try again.”
- **ADR-DESIGN-001 / 002** — status guard/observer and RBAC strategies remain owned with Elcke/Donya; Adolf’s write path calls the guard before flush and the audit observer after a successful conditional update.

Where implementation uses JPA `@Version` instead of a hand-written `UPDATE … WHERE version = ?`, behaviour is equivalent (Hibernate emits the version predicate). No separate deviation ADR was required beyond this note.

## 3. Controlled changes

Release-candidate scope for Adolf’s slice includes FR006, FR008 (accept path), FR012 summary query check, FR013, RTM-008 concurrency, a credible staging/test environment, E2E, and the list performance scenario. None of these were cut.

M3 freeze uses **credible simulated staging** (Vite + Spring `demo`/H2). Real cloud staging is optional per SEN381_M3.pdf §14 and was not required for freeze; RISK-006 residual is accepted by avoiding free-tier cloud for the submission baseline.

## 4. Construction and configuration

- **Backend:** Flyway `V1__init_schema.sql` (`chk_request_status`, indexes on `submitted_by_id` / `assigned_to_id`, `version`); postgres-only V2 append-only triggers (TD-006).
- **API:** List/detail/summary GETs owned by access-control `RequestViewController` (Option A). Adolf adds FR006/FR013 query params on that list via `RequestListService`, plus accept/status/comments write paths with version conflict → 409.
- **Frontend ownership:** API client (`VITE_API_BASE_URL`), staff detail (accept, comments, 409 reload message).
- **Simulated staging:** demo/H2 on `8081` + Vite `5173`; secrets via env only. See `docs/staging/demo-prep.md`, `docs/staging/staging-vs-dev.md`, `docs/staging/rollback.md`. Optional Docker/Render artefacts remain for a later public URL.
- **Config:** `SPRING_PROFILES_ACTIVE`, `DB_JDBC_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `SERVER_PORT`.

## 8. Performance and reliability

- **Scenario:** authenticated staff `GET /api/v1/requests` timed via `scripts/perf-list.mjs`; evidence in `docs/testing/performance-list-evidence.md`.
- **Recorded simulated-staging run (15/10/2026):** **9 rows**, avg **5.9 ms**, 0% errors on demo/H2 (`8081`). Satisfies the M3 single small performance exercise. Cloud re-run optional only.
- **Target:** no contractual SLA; results are characterisation only.
- **Reliability controls:** optimistic concurrency (RISK-010); transactional audit write; RC rollback note.
- **Limits:** H2 single-JVM; free-tier cloud limits (RISK-006) only if Render is used later. A green performance run does **not** prove municipal production load.
- **Close-out tracker:** `docs/testing/m3-adolf-closeout.md`. PED paste pack: `docs/PED/m3-adolf-paste.md`. Release summary: `docs/release/m3-release-evidence-summary.md`.
