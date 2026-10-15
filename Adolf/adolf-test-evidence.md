# Adolf M3 test evidence record

For every counted test: basis, why selected, technique, input, expected (before run), actual, status, evidence, interpretation.

| Test ID | Basis | Why selected | Technique | Input | Expected (before run) | Actual | Status | Evidence | Interpretation |
|---|---|---|---|---|---|---|---|---|---|
| TC-A-01…A-07 | FR008, ADR-DATA-001 | Unit path for accept/status/version | Unit (Mockito) | Stale/legal versions | 409 or success; history rules | As coded in `ServiceRequestLifecycleTest` | Pass in CI | Surefire | Does not prove DB-level races |
| TC-A-INT-01 | FR001 | Persist unique ID | API/integration (H2) | Two creates | Distinct IDs stored | Distinct IDs | Pass | `ServiceRequestApiIntegrationTest` | Demo DB only |
| TC-A-INT-02 | NFR004 / ASR rollback | Illegal write must not leave history | API/integration | Assigned → Closed skip | HTTP 400; history unchanged | 400; history unchanged | Pass | same | Confirms TX rollback on guard failure |
| TC-A-INT-03 | ADR-DATA-001 later consequence | True concurrency | Parallel MockMvc | Two InProgress with same version | One 200, one 409; +1 history | One 200, one 409; +1 history | Pass | same | H2 single JVM — not multi-node |
| TC-A-INT-04 | FR006, FR012, FR013 | Filter + summary | API/integration | status/category/search; summary | Filtered list; counts; staff 403 on summary | Matched | Pass | same | Complements UI filters |
| TC-A-E2E-01 | FR001–FR011 path | Full lifecycle | Playwright E2E | Demo users | Closed visible to requester | Pass against local Vite+demo (2026-10-14); re-run with `STAGING_BASE_URL` after Render deploy | Pass (local) | `e2e/tests/lifecycle.spec.js` | Re-run on public staging URL for release evidence |
| TC-A-E2E-02 | NFR003 | Unauth blocked | Playwright API | No cookie / staff summary | 401/403 | 401 on list; 403 staff summary | Pass (local) | `e2e/tests/unauthorised.spec.js` | Complements Donya ASR-01 |
| PERF-LIST-01 | List hot path | Indexes + FR006 | Timed HTTP | Staff list 10× | Record metrics; no SLA claimed | avg ~9 ms, 0% errors (local via Vite proxy) | Pass (local characterisation) | `docs/testing/performance-list-evidence.md` | Does not prove municipality load; re-run on Render |
