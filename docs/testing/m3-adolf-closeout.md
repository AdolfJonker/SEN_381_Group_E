# Adolf M3 close-out checklist

Owner: Adolf · Updated: 15/10/2026  
Authority: [SEN381_M3.pdf](../../SEN381_M3.pdf) — real cloud staging is **optional**; a credible simulated staging environment is fully acceptable.

## Done (local / simulated staging)

| Item | Evidence |
|---|---|
| FR006 / FR013 list filters + FR012 summary check | `TC-A-INT-04`; RTM-006/012/013 |
| FR008 accept + concurrency | `TC-A-01…09`, `TC-A-INT-03`; RTM-008 |
| Simulated staging artefacts | `docs/staging/staging-vs-dev.md`, `docs/staging/demo-prep.md`, `docs/staging/rollback.md` |
| Optional cloud artefacts (unused for freeze) | `Dockerfile`, `render.yaml`, `docs/staging/render-deploy.md` |
| PED section drafts + paste pack | `docs/m3-adolf-sections.md`, `docs/PED/m3-adolf-paste.md` |
| Critical E2E against simulated staging | Playwright **3/3** on 2026-10-15 (`BASE_URL=http://127.0.0.1:5173`, Vite + demo API) |
| PERF-LIST-01 (one small scenario) | **9 rows**, avg **5.9 ms**, 0% errors (`docs/testing/performance-list-evidence.md`) |
| Traceability / doc hygiene | Per-test FR/RTM in `adolf-test-evidence.md`; Adolf stubs → `docs/` |
| Tech debt recorded | TD-006 (H2 triggers), TD-007 (SPA `permitAll`) |
| Release Evidence Summary | `docs/release/m3-release-evidence-summary.md` |

## Remaining for Adolf (brief-minimum — no Render required)

1. **Identify RC** — tag `v3.0.0-rc1` on the merge/commit that includes this close-out, or state that exact SHA in the PED submission block (see [`docs/staging/rollback.md`](../staging/rollback.md)).
2. **Paste PED** — copy [`docs/PED/m3-adolf-paste.md`](../PED/m3-adolf-paste.md) into Word PED sections 2–4 and 8 (plus submission info block), then PDF as PED v3.0.
3. **Presentation demo** — follow [`docs/staging/demo-prep.md`](../staging/demo-prep.md) (simulated staging + critical journey).

## Optional only (public URL — not required by SEN381_M3.pdf §14)

If the team later wants a public staging URL:

1. Deploy via [`docs/staging/render-deploy.md`](../staging/render-deploy.md).
2. Record URL + SHA in [`docs/staging/rollback.md`](../staging/rollback.md).
3. Re-run E2E/PERF with `BASE_URL` / `STAGING_BASE_URL` pointing at Render.

Cloud staging earns no automatic bonus marks per the brief.

## Hand-offs (not Adolf blockers)

| Item | Owner |
|---|---|
| Requester detail UI (TD-003) | Elcke |
| Postgres CI for NFR004 triggers (TD-006) | Adolf + Donya (M4 ok if recorded) |
| Team PED PDF compile + Moodle pack | Team (Adolf paste pack ready) |
