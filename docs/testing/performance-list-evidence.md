# Performance evidence — request list (Adolf M3)

| Field | Value |
|---|---|
| Basis | NFR-style list responsiveness for **FR006** staff list (hot path after role strategy; not FR005/RTM-005) |
| Why selected | List query is the hot path behind indexes on `submitted_by_id` / `assigned_to_id` |
| Technique | Timed HTTP GET as authenticated staff |
| Environment | **Simulated staging** — local demo/H2 (`http://127.0.0.1:8081`). Satisfies M3 “one small justified performance” exercise. Public Render not required (SEN381_M3.pdf §14). |
| Data size (rows returned, last call) | 9 |
| Workload | 10 sequential GETs as `staff@civicconnect.local` |
| Duration (wall, successful calls) | avg 5.9 ms; p95 8.3 ms |
| Response time target | None justified for M3 — recorded for comparison only |
| Error rate | 0.0% (0/10) |
| Status | Pass (simulated-staging characterisation; 9 rows) |
| Evidence | Output of `scripts/perf-list.mjs` on 2026-10-15T19:28:04.074Z |
| Interpretation | No contractual SLA. Warm-stack characterisation on the M3 simulated staging API. Cloud re-run optional only. |
| What this does **not** prove | Production-scale concurrent municipal users, cold-start after free-tier sleep, or multi-region latency. |
| Next run | Optional: `STAGING_BASE_URL=https://… node scripts/perf-list.mjs` if Render is deployed later |

## Raw successful durations (ms)

4.8, 4.8, 5.1, 5.2, 5.2, 5.3, 5.7, 6.6, 7.8, 8.3
