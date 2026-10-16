# Staging vs development (Adolf M3)

Authority: [SEN381_M3.pdf](../../SEN381_M3.pdf) — M3 accepts **one real OR credible simulated** staging/test environment. Real cloud is optional.

| Concern | Day-to-day development | Simulated staging (M3 primary) | Optional cloud (Render) |
|---|---|---|---|
| Purpose | Feature work, hot reload | Assessor-accessible integrated RC demo + critical E2E/PERF | Public HTTPS URL if desired |
| Database | Local PostgreSQL or H2 (`demo`) | Fixed **demo/H2** profile (`SPRING_PROFILES_ACTIVE=demo`) | Managed Postgres free tier; may sleep / expire |
| App process | `mvnw spring-boot:run` + Vite | Same stack, fixed ports: API `8081`, UI `5173` | Single Docker image; Spring serves Vite build from `/static` |
| JVM memory | Host defaults | Host defaults | Constrained free-tier RAM (`-XX:MaxRAMPercentage=75.0`) |
| Networking | localhost; Vite proxies `/api/v1` | localhost; same as assessor demo | Public HTTPS; API and UI same origin |
| Secrets | Root `.env` (gitignored) | Env vars / profile defaults only — no committed passwords | Render environment variables only |
| Profile | `demo` or `postgres` | **`demo`** | `postgres` with Flyway V1 + V2 triggers |
| Evidence runs | Ad-hoc | Playwright E2E 3/3 + PERF-LIST-01 (2026-10-15) | Optional re-run after deploy |

## How simulated staging differs from casual local work

- Uses an explicit **release-candidate** Git tag/commit (`v3.0.0-rc1` / recorded SHA).
- Backend started with `SPRING_PROFILES_ACTIVE=demo` and `SERVER_PORT=8081` (see [`demo-prep.md`](./demo-prep.md)).
- Critical E2E and the list performance scenario were executed against this environment.
- Demo logins are documented; secrets are not committed.

## What this does not prove

- Production-scale concurrent municipal load
- Multi-instance session affinity / sticky sessions
- Long-term retention or backup restore drills beyond free-tier cloud (if used)
- PostgreSQL append-only triggers on H2 (TD-006 — postgres profile only)
