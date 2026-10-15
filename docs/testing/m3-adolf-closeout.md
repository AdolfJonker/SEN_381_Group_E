# Adolf M3 close-out checklist

Owner: Adolf · Updated: 15/10/2026 · Code baseline on `main`: `a7f9913`

## Done (local / repo)

| Item | Evidence |
|---|---|
| FR006 / FR013 list filters + FR012 summary check | `TC-A-INT-04`; RTM-006/012/013 |
| FR008 accept + concurrency | `TC-A-01…09`, `TC-A-INT-03`; RTM-008 |
| Staging artefacts in repo | `Dockerfile`, `render.yaml`, `docs/staging/*` |
| PED section drafts | `docs/m3-adolf-sections.md` (+ paste pack below) |
| Local E2E | Playwright **3/3** on 2026-10-15 (`BASE_URL=http://127.0.0.1:5173`) |
| Local PERF-LIST-01 | **9 rows**, avg **5.9 ms**, 0% errors (`docs/testing/performance-list-evidence.md`) |
| Traceability / doc hygiene | Per-test FR/RTM in `adolf-test-evidence.md`; Adolf stubs → `docs/` |
| Tech debt recorded | TD-006 (H2 triggers), TD-007 (SPA `permitAll`) |

## Still required for release freeze (Adolf)

These need your Render account (cannot be finished from the laptop alone):

1. **Deploy staging** — follow [`docs/staging/render-deploy.md`](../staging/render-deploy.md).
2. **Record URL + SHA** — paste into [`docs/staging/rollback.md`](../staging/rollback.md) (template ready).
3. **Tag `v3.0.0-rc1`** on the deployed commit (matches PED).
4. **Staging E2E**
   ```powershell
   cd e2e
   $env:BASE_URL = "https://YOUR-RENDER-URL"
   npx playwright test --reporter=list
   ```
5. **Staging PERF**
   ```powershell
   $env:STAGING_BASE_URL = "https://YOUR-RENDER-URL"
   node scripts/perf-list.mjs
   ```
6. **Paste PED** — copy [`docs/PED/m3-adolf-paste.md`](../PED/m3-adolf-paste.md) into the Word PED sections 2–4 and 8, then PDF.

## Hand-offs (not Adolf blockers)

| Item | Owner |
|---|---|
| Requester detail UI (TD-003) | Elcke |
| Postgres CI for NFR004 triggers (TD-006) | Adolf + Donya (M4 ok if recorded) |
| Release evidence summary sheet | Donya |
