# Staging rollback note (Adolf M3)

## M3 release candidate (simulated staging — primary)

| Field | Value |
|---|---|
| Environment | Credible **simulated staging**: Vite UI + Spring Boot `demo`/H2 (see [`demo-prep.md`](./demo-prep.md)) |
| Staging URL | Local assessor demo — `http://127.0.0.1:5173` (API `http://127.0.0.1:8081`) |
| Code baseline (close-out branch tip before brief-alignment docs) | `1dde26bee1b9c8d443438bb53629ea68d21c314f` |
| Intended RC tag | `v3.0.0-rc1` — create on the commit that merges this M3 close-out to the submission baseline |
| Health check | `GET http://127.0.0.1:8081/api/v1/health` |
| Smoke logins | `staff@civicconnect.local` / `requester@civicconnect.local` / `management@civicconnect.local` (password `Password123!`) |
| Critical E2E | Pass (local simulated staging, 2026-10-15) — `e2e/` Playwright 3/3 |
| PERF | Pass (local simulated staging, 2026-10-15) — PERF-LIST-01, 9 rows |
| Cloud staging | **Not used for M3 freeze** — optional per SEN381_M3.pdf §14; see [`render-deploy.md`](./render-deploy.md) |
| Recorded by / date | Adolf Jonker / 15/10/2026 |

## Restore target (simulated staging)

- Check out the RC tag or recorded SHA, then restart backend + frontend per [`demo-prep.md`](./demo-prep.md).
- Preferred: `git checkout v3.0.0-rc1` (once tagged), then restart the demo stack.
- Until the tag exists: restore `1dde26b` (or the merge commit that supersedes it on `main`) and re-run health + one staff/requester login.

## Optional cloud staging (fill only if Render is deployed later)

| Field | Value |
|---|---|
| Staging URL | _not deployed for M3_ |
| Git SHA | _n/a_ |
| Tag | `v3.0.0-rc1` (same RC identifier) |
| Health check | `GET /api/v1/health` |
| Notes | Secrets stay in the Render dashboard. Flyway is forward-only — do not roll back across incompatible migrations without DB restore. |

## Data / configuration concerns

- Simulated staging uses in-memory H2 (`demo`) — restart clears demo data; re-seed via `DemoUserLoader` / manual create as needed for PERF.
- Postgres-only V2 append-only triggers do not run on H2 (TD-006).
- Secrets must not be committed; use environment variables only.
