# PED M3 drafts — Adolf sections (paste into PED v3.0)

These are working drafts for the Word PED. Copy into the matching sections before PDF compile.

## 2. M2 conformance

M3 construction follows the M2 ADRs:

- **ADR-ARCH-001** — modular monolith; single deployable Spring Boot service + React UI.
- **ADR-TECH-001** — Java/Spring Boot, PostgreSQL, React/Vite.
- **ADR-DATA-001** — one transaction for status/assignment + history; optimistic `@Version`; HTTP 409 with “Reload and try again.”
- **ADR-DESIGN-001 / 002** — status guard/observer and RBAC strategies remain owned with Elcke/Donya; Adolf’s write path calls the guard before flush and the audit observer after a successful conditional update.

Where implementation uses JPA `@Version` instead of a hand-written `UPDATE … WHERE version = ?`, behaviour is equivalent (Hibernate emits the version predicate). No separate deviation ADR was required beyond this note.

## 3. Controlled changes

Release-candidate scope for Adolf’s slice includes FR006, FR008 (accept path), FR012 summary query check, FR013, RTM-008 concurrency, staging deploy, E2E, and the list performance scenario. None of these were cut. If staging platform limits force a temporary skip, record it here with date, owner, and residual risk (RISK-006).

## 4. Construction and configuration

- **Backend:** Flyway `V1__init_schema.sql` (`chk_request_status`, indexes on `submitted_by_id` / `assigned_to_id`, `version`); postgres-only V2 append-only triggers (TD-006).
- **API:** List/detail/summary GETs owned by access-control `RequestViewController` (Option A). Adolf adds FR006/FR013 query params on that list via `RequestListService`, plus accept/status/comments write paths with version conflict → 409.
- **Frontend ownership:** API client (`VITE_API_BASE_URL`), staff detail (accept, comments, 409 reload message).
- **Staging:** Docker multi-stage image (`Dockerfile`) + Render blueprint (`render.yaml`). Secrets via environment variables only (`.env` gitignored). Rollback and staging-vs-dev notes under `docs/staging/`.
- **Config:** `SPRING_PROFILES_ACTIVE`, `DB_JDBC_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `SERVER_PORT`.

## 8. Performance and reliability

- **Scenario:** authenticated staff `GET /api/v1/requests` timed via `scripts/perf-list.mjs`; evidence in `docs/testing/performance-list-evidence.md`.
- **Recorded run:** local Vite + demo/H2, **0 rows** returned — not staging. Re-run on Render with data before freeze.
- **Target:** no contractual SLA; results are characterisation only.
- **Reliability controls:** optimistic concurrency (RISK-010); transactional audit write; staging rollback note for bad deploys.
- **Limits:** free-tier DB sleep/expiry and JVM memory (RISK-006). A green performance run does **not** prove municipal production load.
