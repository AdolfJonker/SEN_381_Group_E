# PostgreSQL smoke test evidence (Adolf)

**Date:** 2026-10-18  
**Environment:** Local simulated production-like run  
**Profile:** `postgres` (PostgreSQL 17.4)  
**Backend:** `java -jar target/civicconnect-0.0.1-SNAPSHOT.jar` on `http://127.0.0.1:8081`  
**Frontend build:** `npm run build` + `vite preview` (built assets)  
**Secrets:** `DB_*`, `DEMO_PASSWORD`, `CORS_ALLOWED_ORIGINS` supplied only via process environment (not committed)  
**Demo users:** `DEMO_USERS_ENABLED=true` for assessable logins (known limitation vs production)  
**Branch/commit at run:** `feature/m4-adolf-freeze` (fill final `v4.0.0` hash after freeze tag)

## Startup

- Active Spring profile: `postgres`
- Flyway applied **3 migrations** on empty schema:
  - V1 `init schema`
  - V2 `status history append only`
  - V3 `allow initial status history`
- Health: `GET /api/v1/health` → `{"status":"UP","database":"UP"}`

## Checklist results

| Step | Result | Notes |
|------|--------|-------|
| Health (API + DB) | Pass | HTTP 200 |
| Wrong password rejected | Pass | HTTP 400 (rejected; not authenticated) |
| Requester / staff / management login | Pass | Session cookies issued |
| Requester creates request | Pass | id=1, status=Open, version=0 |
| Staff accept | Pass | status=Assigned, version=1 |
| Status InProgress → Resolved → Closed | Pass | versions 2 → 3 → 4 |
| Staff comment visible on detail | Pass | commentCount=1 |
| Management summary | Pass | `assigned=1`, `closed=1` (after lifecycle + second Assigned request) |
| Flyway V1–V3 present | Pass | all `success=t` |
| `status_history` rows written | Pass | count=7 |
| Append-only **UPDATE** blocked | Pass | `ERROR: status_history is append-only (NFR004)` |
| Append-only **DELETE** blocked | Pass | `ERROR: status_history is append-only (NFR004)` |

## Strongest integrity evidence

Direct SQL against PostgreSQL:

```text
UPDATE status_history SET note = 'tamper' WHERE id = ...;
-- ERROR: status_history is append-only (NFR004)

DELETE FROM status_history WHERE id = ...;
-- ERROR: status_history is append-only (NFR004)
```

This is the NFR004 / RTM-013 database-level protection from `migration-postgres/V2` that does not exist on H2.

## Limitations (honest)

- Demo accounts were enabled for the smoke run (`DEMO_USERS_ENABLED=true`).
- Single local JVM; not a hosted multi-instance deploy.
- Automated suite was not re-run as part of this smoke (optional follow-up: `mvnw verify` + Playwright E2E against this profile).
- Final release tag/hash should be filled after `v4.0.0` is cut.
