# Performance evidence — request list (Adolf M3)

| Field | Value |
|---|---|
| Basis | NFR-style list responsiveness for FR005/FR006 staff list |
| Why selected | List query is the hot path behind indexes on `submitted_by_id` / `assigned_to_id` |
| Technique | Timed HTTP GET against staging/dev API |
| Environment | `http://127.0.0.1:5173` |
| Data size (rows returned, last call) | 0 |
| Workload | 10 sequential GETs as `staff@civicconnect.local` |
| Duration (wall, successful calls) | avg 9.2 ms; p95 26.4 ms |
| Response time target | None justified for M3 free-tier staging — recorded for comparison only |
| Error rate | 0.0% (0/10) |
| Status | Pass (completed without HTTP errors) |
| Evidence | Output of `scripts/perf-list.mjs` on 2026-10-14T21:04:30.735Z |
| Interpretation | No contractual SLA exists; this run only characterises free-tier staging under a light sequential load. |
| What this does **not** prove | Production-scale concurrent municipal users, cold-start after free-tier sleep, or multi-region latency. |

## Raw successful durations (ms)

6.6, 6.7, 6.8, 7.1, 7.1, 7.5, 7.6, 8.2, 8.5, 26.4
