# Staging rollback note (Adolf M3)

## Deployed revision (fill on first green Render deploy)

| Field | Value |
|---|---|
| Staging URL | _pending — see `docs/staging/render-deploy.md`_ |
| Git SHA | `a7f9913` (current `main` tip at close-out prep; replace with exact deploy SHA) |
| Tag | `v3.0.0-rc1` (create after first green staging smoke) |
| Health check | `GET /api/v1/health` |
| Smoke logins | `staff@civicconnect.local` / `requester@civicconnect.local` (password `Password123!`) |
| Recorded by / date | _pending_ |

## Restore target

- **Application:** redeploy the previous known-good Git tag or commit SHA that was recorded when staging last passed health + smoke checks.
  - Preferred: `git checkout v3.0.0-rc1` then trigger a Render deploy of that revision (or `docker build` / push the tagged image).
  - Record the deployed SHA in the release evidence summary (Donya) and in the table above when staging is first published.
- **Until the tag exists:** restore the last commit that passed CI on `main` (currently prepared as `a7f9913`).

## Data / configuration concerns

- Flyway migrations are forward-only. Rolling the app back to a version that expects an older schema is unsafe if a newer migration already ran.
- If a bad migration shipped, restore the database from the last Render Postgres snapshot (if available on the plan) **or** recreate the free database and re-run Flyway from empty — accepting loss of staging demo data.
- Secrets stay in the Render dashboard; do not copy production-like passwords into Git when rolling back.
- After rollback, verify `GET /api/v1/health` and one login (staff + requester) before calling staging usable again.
