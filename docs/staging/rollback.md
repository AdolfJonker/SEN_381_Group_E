# Staging rollback note (Adolf M3)

## Restore target

- **Application:** redeploy the previous known-good Git tag or commit SHA that was recorded when staging last passed health + smoke checks.
  - Preferred: `git checkout <tag>` then trigger a Render deploy of that revision (or `docker build` / push the tagged image).
  - Record the deployed SHA in the release evidence summary (Donya) and here when staging is first published.
- **Placeholder until first deploy:** restore `main` at the release-candidate tag once cut (`v3.0.0-rc1`, matching PED). Until that tag exists, restore the last commit that passed CI on the staging branch.

## Data / configuration concerns

- Flyway migrations are forward-only. Rolling the app back to a version that expects an older schema is unsafe if a newer migration already ran.
- If a bad migration shipped, restore the database from the last Render Postgres snapshot (if available on the plan) **or** recreate the free database and re-run Flyway from empty — accepting loss of staging demo data.
- Secrets stay in the Render dashboard; do not copy production-like passwords into Git when rolling back.
- After rollback, verify `GET /api/v1/health` and one login (staff + requester) before calling staging usable again.
