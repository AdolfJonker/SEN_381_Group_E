# PED v3.0 — Adolf paste pack (sections 2, 3, 4, 8)

Copy each block into the matching Word PED section, then compile PDF.

Source drafts also live in [`docs/m3-adolf-sections.md`](../m3-adolf-sections.md). Evidence: [`docs/testing/adolf-test-evidence.md`](../testing/adolf-test-evidence.md).

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

Release-candidate scope for Adolf’s slice includes FR006, FR008 (accept path), FR012 summary query check, FR013, RTM-008 concurrency, staging deploy, E2E, and the list performance scenario. None of these were cut.

**Verification status (15/10/2026):**

- Automated unit/integration for the slice: pass (see Adolf test evidence).
- Local E2E (Playwright): 3/3 pass against Vite + demo API.
- Local PERF-LIST-01: pass with 9 staff-visible rows (avg ~5.9 ms); no SLA claimed.
- Public Render staging E2E/PERF: complete when `STAGING_BASE_URL` is live (procedure in `docs/staging/render-deploy.md`). If free-tier limits delay staging, record date, owner (Adolf), and residual RISK-006 here.

Release-candidate Git tag name: **`v3.0.0-rc1`**.

---

## 4. Construction and configuration

- **Backend:** Flyway `V1__init_schema.sql` (`chk_request_status`, indexes on `submitted_by_id` / `assigned_to_id`, `version`); postgres-only V2 append-only triggers (TD-006).
- **API:** List/detail/summary GETs owned by access-control `RequestViewController` (Option A). Adolf adds FR006/FR013 query params on that list via `RequestListService`, plus accept/status/comments write paths with version conflict → 409.
- **Frontend ownership:** API client (`VITE_API_BASE_URL`), staff detail (accept, comments, 409 reload message).
- **Staging:** Docker multi-stage image (`Dockerfile`) + Render blueprint (`render.yaml`). Secrets via environment variables only (`.env` gitignored). Rollback and staging-vs-dev notes under `docs/staging/`.
- **Config:** `SPRING_PROFILES_ACTIVE`, `DB_JDBC_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `SERVER_PORT`.
- **Security note (TD-007):** API routes are authenticated/role-gated; `.anyRequest().permitAll()` remains only so packaged SPA client routes can load.

---

## 8. Performance and reliability

- **Scenario:** authenticated staff `GET /api/v1/requests` timed via `scripts/perf-list.mjs`; evidence in `docs/testing/performance-list-evidence.md`.
- **Recorded local run (15/10/2026):** 10 sequential GETs; **9 rows**; avg **5.9 ms**; p95 **8.3 ms**; **0%** errors; environment `http://127.0.0.1:8081` (demo/H2). Not production load.
- **Target:** no contractual SLA on free-tier staging; results are characterisation only.
- **Reliability controls:** optimistic concurrency (RISK-010); transactional audit write; staging rollback note for bad deploys.
- **Limits:** free-tier DB sleep/expiry and JVM memory (RISK-006). A green performance run does **not** prove municipal production load. Re-run on Render after deploy for freeze evidence.
