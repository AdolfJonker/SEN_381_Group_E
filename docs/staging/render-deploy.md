# Render staging deploy (Adolf M3)

Blueprint: repo-root [`render.yaml`](../../render.yaml) · Image: [`Dockerfile`](../../Dockerfile)

## One-time setup (Render dashboard)

1. Sign in at [https://dashboard.render.com](https://dashboard.render.com).
2. **New → Blueprint** → connect the `SEN_381_Group_E` GitHub repo → apply `render.yaml`.
3. Wait for `civicconnect-db` (Postgres free) and `civicconnect-staging` (web) to appear.
4. On the **web** service → **Environment**, set (from the DB **Connections** panel):
   - `DB_JDBC_URL` = `jdbc:postgresql://HOST:5432/civicconnect` (use the Render external/internal host)
   - `DB_USER` / `DB_PASSWORD` = DB credentials from Render
   - `CORS_ALLOWED_ORIGINS` = `https://YOUR-SERVICE.onrender.com` (same origin is fine; include localhost only if you still hit the API from Vite)
5. Confirm `SPRING_PROFILES_ACTIVE=postgres` and `SERVER_PORT=8080` (already in the blueprint).
6. Deploy / wait until health check `GET /api/v1/health` returns UP.

## After first green deploy

1. Copy the public URL (e.g. `https://civicconnect-staging.onrender.com`).
2. Smoke:
   - open UI → login `staff@civicconnect.local` / `Password123!`
   - `GET …/api/v1/health`
3. Update [`rollback.md`](./rollback.md) with **URL**, **SHA**, and date.
4. Tag: `git tag -a v3.0.0-rc1 <sha> -m "M3 release candidate"` then `git push origin v3.0.0-rc1`.
5. Run staging E2E + PERF (commands in [`docs/testing/m3-adolf-closeout.md`](../testing/m3-adolf-closeout.md)).

## Notes

- Free tier sleeps; first request after idle can be slow — warm with `/api/v1/health` before PERF/E2E.
- Demo users are created by `DemoUserLoader` when the profile allows seeding; if login fails on postgres, confirm the loader runs under your staging profile or seed once manually.
- Never commit DB passwords.
