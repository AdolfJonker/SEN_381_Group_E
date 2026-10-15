# Performance evidence — request list (Adolf M3)

| Field | Value |
|---|---|
| Basis | NFR-style list responsiveness for FR006 staff list (hot path after role strategy) |
| Why selected | List query is the hot path behind indexes on `submitted_by_id` / `assigned_to_id` |
| Technique | Timed HTTP GET via Vite proxy to local demo API |
| Environment | **Local only:** `http://127.0.0.1:5173` (Vite + Spring Boot demo/H2). **Not** Render staging. |
| Data size (rows returned, last call) | **0** (empty list — no seeded staff-visible requests in that run) |
| Workload | 10 sequential GETs as `staff@civicconnect.local` |
| Duration (wall, successful calls) | avg 9.2 ms; p95 26.4 ms |
| Response time target | None justified for M3 — recorded for comparison only |
| Error rate | 0.0% (0/10) |
| Status | Pass (completed without HTTP errors) — **empty-list characterisation only** |
| Evidence | Output of `scripts/perf-list.mjs` on 2026-10-14T21:04:30.735Z |
| Interpretation | Times reflect an empty JSON array over a warm local stack. They do **not** represent staging, populated municipal data, or a contractual SLA. |
| What this does **not** prove | Staging/Render latency, non-empty list cost, production-scale concurrent users, cold-start after free-tier sleep, or multi-region latency. |
| Next run required | Re-run on Render with a populated staff list (`STAGING_BASE_URL` / staging login) before release freeze. |

## Raw successful durations (ms)

6.6, 6.7, 6.8, 7.1, 7.1, 7.5, 7.6, 8.2, 8.5, 26.4
