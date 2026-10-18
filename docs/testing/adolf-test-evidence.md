# Adolf M3 test evidence record

| Field | Value |
|---|---|
| Owner | Adolf |
| Covers | FR001, FR006, FR007 (detail deny), FR008, FR009 (status unit on write path), FR012, FR013, NFR003, NFR004, ADR-DATA-001 |
| Does **not** claim | **FR005 / RTM-005** (access-control owns staff list scope: `TC-UNIT-RBAC-*`, `TC-API-SEC-04`) |
| Document status | M3 evidence complete against **simulated staging** (2026-10-15): Surefire green; E2E 3/3; PERF 9-row run. Public Render E2E/PERF **optional** (SEN381_M3.pdf §14). |
| Last updated | 15/10/2026 |

## 1. Purpose and scope

This record maps **each** Adolf-owned test to one primary requirement (no FR005/RTM-005 for Adolf IDs). A case is marked **Pass** only when a recorded run passed after any listed defects were closed.

Related teammate evidence (not counted as Adolf-owned):

* Access control (owns RTM-005): [`access-control-test-evidence.md`](./access-control-test-evidence.md)
* Status lifecycle: [`status-lifecycle-test-evidence.md`](./status-lifecycle-test-evidence.md)
* List performance detail: [`performance-list-evidence.md`](./performance-list-evidence.md)

### Mapping rule (Adolf)

| Adolf IDs | Primary basis | Not allowed |
|---|---|---|
| TC-A-01, A-04, A-05, A-07, A-08, A-09 | FR008 / RTM-008 (+ ADR-DATA-001 where version/history) | FR005 / RTM-005 |
| TC-A-02, A-03 | FR009 / RTM-009 (write-path unit; Elcke owns main guard suite) | FR005 / RTM-005 |
| TC-A-06 | FR007 / RTM-007 + NFR003 (detail `get` deny via `canView`) | FR005 / RTM-005 |
| TC-A-INT-01 | FR001 / RTM-001 | — |
| TC-A-INT-02 | NFR004 / RTM-017 | — |
| TC-A-INT-03 | FR008 / RTM-008 + ADR-DATA-001 | — |
| TC-A-INT-04 | FR006 / RTM-006, FR012 / RTM-012, FR013 / RTM-013 | FR005 / RTM-005 |
| TC-A-E2E-01 | FR001–FR011 path (E2E) | — |
| TC-A-E2E-02 / 02b | NFR003 / RTM-016 | — |
| PERF-LIST-01 | FR006 list hot path (simulated staging; 9 rows) | Claiming cloud staging / SLA |

## 2. How to read Result

| Result | Meaning |
|---|---|
| Pass (automated) | Surefire / Playwright / script run recorded as green |
| Pass (local) | Passed against simulated staging (Vite + demo/H2); public cloud re-run not required for M3 |
| Fail → Pass | Failed in an earlier recorded run; later fixed and re-passed |
| Error → Pass | Failed to load / compile (not an assertion fail); later fixed |

## 3. Inventory (current)

| Layer | Executable cases | Evidence IDs | Current result |
|---|---|---|---|
| Unit — `ServiceRequestLifecycleTest` | 7 | TC-A-01…A-07 | Pass (automated) |
| Unit — `StaffFilterStrategyTest` | 2 | TC-A-08, TC-A-09 | Pass (automated) |
| API — `ServiceRequestApiIntegrationTest` | 4 | TC-A-INT-01…04 | Pass (automated) |
| E2E — Playwright | 3 | TC-A-E2E-01, TC-A-E2E-02, TC-A-E2E-02b | Pass (local 2026-10-15, 3/3) |
| Performance — `scripts/perf-list.mjs` | 1 | PERF-LIST-01 | Pass (local; **9 rows**; avg ~5.9 ms) |
| **Total** | **17** | **17 IDs** (each mapped once; none to RTM-005) | **17 pass / 0 fail (current local)** |

Backend Adolf classes alone: **13** Surefire tests. Full project Surefire after merge isolation fix: **116** tests, **0** failures (local 2026-10-15).

## 4. Test records (current expected / actual)

| Test ID | Basis (primary) | Why selected | Technique | Input | Expected (before run) | Actual | Status | Evidence | Interpretation |
|---|---|---|---|---|---|---|---|---|---|
| TC-A-01 | FR008 / RTM-008, ADR-DATA-001 | Accept returns flushed version | Unit | Open + expectedVersion 0 | Assigned; version matches entity | Pass | Pass (automated) | `ServiceRequestLifecycleTest` | Not DB-level race |
| TC-A-02 | FR009 / RTM-009, ADR-DATA-001 | Legal status bump returns flushed version | Unit | Assigned → InProgress | Version + status updated | Pass | Pass (automated) | same | Complements Elcke guard suite |
| TC-A-03 | FR009 / RTM-009 | Illegal skip writes nothing | Unit | Open → Closed | 400; zero history | Pass | Pass (automated) | same | D3 |
| TC-A-04 | FR008 / RTM-008, ADR-DATA-001 | Stale accept rejected | Unit | expectedVersion 0 vs stored 1 | 409; nothing written | Pass | Pass (automated) | same | Optimistic lock |
| TC-A-05 | FR008 / RTM-008, NFR003 | Requester cannot accept | Unit | Requester email | 403; no write | Pass | Pass (automated) | same | Role gate on accept |
| TC-A-06 | FR007 / RTM-007, NFR003 | Detail deny when `canView` false | Unit | Other staff’s Assigned | 403 on `get` | Pass | Pass (automated) | same | **Not** RTM-005 (detail, not list strategy) |
| TC-A-07 | FR008 / RTM-008, NFR004 | Accept writes one history row | Unit | Open accept | Exactly one Open→Assigned history | Pass | Pass (automated) | same | Audit on assign |
| TC-A-08 | **FR008 / RTM-008 only** | Open visible for accept readiness | Unit | Open request | `canView` true for any staff | Pass | Pass (automated) | `StaffFilterStrategyTest` | **Not** FR005 / RTM-005 |
| TC-A-09 | **FR008 / RTM-008 only** | Other staff’s Assigned hidden | Unit | Assigned to other | `canView` false | Pass | Pass (automated) | same | Complements `TC-UNIT-RBAC-02`; RTM-005 stays access-control |
| TC-A-INT-01 | FR001 / RTM-001 | Persist unique ID | API (H2) | Two creates | Distinct IDs | Pass | Pass (automated) | `ServiceRequestApiIntegrationTest` | Demo DB only |
| TC-A-INT-02 | NFR004 / RTM-017 | Illegal write leaves no orphan history | API (H2) | Assigned → Closed skip | 400; history unchanged | Pass | Pass (automated) | same | TX rollback |
| TC-A-INT-03 | FR008 / RTM-008, ADR-DATA-001 | Parallel conflict | Parallel MockMvc | Two same-version updates | One 200, one 409; +1 history | Pass | Pass (automated) | same | H2 single JVM |
| TC-A-INT-04 | FR006 / RTM-006, FR012 / RTM-012, FR013 / RTM-013 | Filter + summary | API (H2) | status/category/search; summary | Filtered list; counts; staff 403 on summary | Pass | Pass (automated) | same | Not FR005 list-strategy ownership |
| TC-A-E2E-01 | FR001–FR011 path | Full lifecycle | Playwright | Demo users | Closed visible to requester | Pass 2026-10-15 (Vite `127.0.0.1:5173`) | Pass (local) | `e2e/tests/lifecycle.spec.js` | Simulated staging; cloud re-run optional |
| TC-A-E2E-02 | NFR003 / RTM-016 | Unauth list blocked | Playwright API | No cookie | 401/403 | Pass 2026-10-15 | Pass (local) | `e2e/tests/unauthorised.spec.js` | Complements Donya ASR-01 |
| TC-A-E2E-02b | NFR003 / RTM-016, FR012 | Staff denied summary | Playwright API | Staff session | 403 | Pass 2026-10-15 | Pass (local) | same | — |
| PERF-LIST-01 | FR006 (characterisation) | List hot path | Timed HTTP | Staff list 10× | Metrics only; no SLA | avg 5.9 ms; p95 8.3 ms; **9 rows**; demo `8081` | Pass (simulated staging) | `performance-list-evidence.md` | M3 one-scenario evidence; Render optional |

## 5. Failure history (recorded)

These are tests that **did fail (or error) at some point** during Adolf M3 / merge work, then were fixed. Include these in PED §9 style interpretation.

### Brief: why each failed and how it was corrected

| ID | Why it failed | How it was corrected |
|---|---|---|
| DEF-A01 | Production `ServiceRequestService` constructor changed (extra deps after merge); the unit test still used the old signature, so CI failed at `testCompile` before any assertions ran. | Updated the test to construct the service with the current constructor args, then re-ran that class green. |
| DEF-A02 | After Option A merge, two controllers both mapped `POST /api/v1/requests/{id}/status`, so Spring refused to start. The four INT tests errored on context load (not assertion fails). | Kept only `RequestStatusController`, removed the duplicate from `ServiceRequestController`, aligned FE/tests to `ChangeStatusBody` (`toStatus` / `expectedVersion`). |
| DEF-A03 | The Playwright lifecycle test never reached the login form (or failed on brittle “My requests” selectors), so it hit the 60s timeout. | Hardened locators / flow so login and the lifecycle path work; simulated-staging suite then passed 3/3. |
| DEF-A04 | Adolf INT tests commit rows into shared H2; AccessControl expected a clean DB (`open:2`, all staff rows `Open`) and saw leftover Assigned/Open data. *(Teammate assertions; Adolf side-effect.)* | AccessControl `@BeforeEach` now deletes comments, status history, and requests before seeding. |

Detail tables for each defect follow.

### 5.1 DEF-A01 — `ServiceRequestLifecycleTest` would not compile (CI)

| Field | Detail |
|---|---|
| When | 2026-10-14 |
| Symptom | `testCompile` failure: constructor args for `ServiceRequestService` did not match production constructor |
| Cases affected | TC-A-01…A-07 (class could not run) |
| Severity | Medium (blocked CI) |
| Root cause | Service constructor changed (merge / observer wiring); test still used old signature |
| Fix | Align test construction with current `ServiceRequestService` |
| Re-test | `-Dtest=ServiceRequestLifecycleTest` green; status **Error → Pass** |

### 5.2 DEF-A02 — Ambiguous `POST …/requests/{id}/status` (CI)

| Field | Detail |
|---|---|
| When | 2026-10-15 |
| Symptom | Spring `IllegalStateException: Ambiguous mapping`; ApplicationContext failed to start |
| Cases affected | All 4 methods in `ServiceRequestApiIntegrationTest` (**Errors**, not assertion Failures); also teammate `AccessControlApiTest` (8 Errors) — **12 Errors / 116 tests** |
| Severity | High (full verify red) |
| Root cause | Duplicate status handlers after Option A merge: Adolf `ServiceRequestController#updateStatus` vs main `RequestStatusController` |
| Fix | Keep `RequestStatusController` only; align client + integration body to `ChangeStatusBody` (`toStatus` / `expectedVersion`); remove `UpdateStatusBody` |
| Re-test | Local `./mvnw -B verify` → **116 tests, 0 failures, 0 errors** (2026-10-15 ~10:08) |
| Note | Adolf INT cases did **not** fail assertions here; they **errored** because the context never started |

### 5.3 DEF-A03 — Playwright `TC-A-E2E-01` timeout / login

| Field | Detail |
|---|---|
| When | 2026-10-14 (local) |
| Symptom | 60s timeout on `getByLabel(/email/i)` (login UI not reached); earlier variant failed asserting **My requests** visibility |
| Cases affected | TC-A-E2E-01 (`lifecycle.spec.js`); suite reported **1 failed** |
| Severity | Medium (release E2E evidence blocked) |
| Root cause | App / selectors not ready (backend or UI wiring); brittle locators |
| Fix | Selector / flow fixes in E2E; later local suite **3/3 pass** |
| Re-test | Playwright pass recorded as **Pass (local)** against simulated staging; public cloud re-run optional per SEN381_M3.pdf §14 |

### 5.4 Related (not Adolf-owned assertions) — DEF-A04 side-effect

| Field | Detail |
|---|---|
| When | 2026-10-15 CI (after DEF-A02 fixed) |
| Symptom | `AccessControlApiTest.staffListIsScoped` expected all `Open`, got `Assigned`; `managementReadsSummary` expected `open:2`, was `5` |
| Owner of failing assertions | Access-control suite (teammate) |
| Link to Adolf code | `ServiceRequestApiIntegrationTest` commits data (no class-level `@Transactional`); shared H2 polluted absolute counts when Surefire ran INT before AccessControl |
| Fix applied | `AccessControlApiTest` `@BeforeEach` clears comments, status history, and requests before seed |
| Re-test | Local `./mvnw -B verify` → **116 / 0** (2026-10-15) |
| Count for Adolf inventory | **Do not count as Adolf test failures** — recorded so the side-effect is documented |

## 6. Runs recorded

| Run | What | Result | Date |
|---|---|---|---|
| A | Local Vite + demo; Playwright + perf script | E2E initially failed (§5.3); later 3/3 pass; PERF-LIST-01 pass | 14/10/2026 |
| B | CI `./mvnw -B verify` after service ctor drift | `ServiceRequestLifecycleTest` compile fail (§5.1) → fixed | 14/10/2026 |
| C | CI after Option A merge | Ambiguous status mapping; 12 Errors (§5.2) → fixed | 15/10/2026 |
| D | CI after status ownership fix | AccessControl 2 Failures (pollution, §5.4) → isolation fix | 15/10/2026 |
| E | Local `./mvnw -B verify` after isolation fix | **116 tests, 0 failures, 0 errors** | 15/10/2026 |
| F | Local Vite+demo: Playwright + PERF (seeded 9 Open rows) | E2E **3/3 pass**; PERF avg 5.9 ms / 0% errors / 9 rows | 15/10/2026 |
| G | Render cloud E2E + PERF | **Skipped for M3 freeze** — simulated staging accepted (SEN381_M3.pdf §14); optional later | — |

## 7. Quality interpretation (Adolf slice)

* **Current:** all Adolf-owned executable cases pass; no open assertion fails in Adolf Surefire classes.
* **Historical:** three Adolf-touching incidents (compile, ambiguous mapping Errors, E2E timeout) were found and closed; one teammate suite failure was caused by Adolf INT data leakage and was mitigated by AccessControl cleanup.
* **Gaps:** Public cloud E2E/PERF not run (optional only); H2 concurrency is not multi-node; AccessControl absolute counts still depend on cleanup discipline.

## 8. File map

| Evidence | Code / artefact |
|---|---|
| TC-A-01…A-07 | `src/backend/src/test/java/civicconnect/services/ServiceRequestLifecycleTest.java` |
| TC-A-08 / A-09 | `src/backend/src/test/java/civicconnect/strategies/StaffFilterStrategyTest.java` |
| TC-A-INT-01…04 | `src/backend/src/test/java/civicconnect/api/ServiceRequestApiIntegrationTest.java` |
| TC-A-E2E-01 | `e2e/tests/lifecycle.spec.js` |
| TC-A-E2E-02 / 02b | `e2e/tests/unauthorised.spec.js` |
| PERF-LIST-01 | `docs/testing/performance-list-evidence.md`, `scripts/perf-list.mjs` |
| UI-A-STATUS / UI-A-375 | `docs/testing/evidence/adolf/` (manual UI §9) |

## 9. Manual UI checks (Adolf M4 — supporting)

| Field | Value |
|---|---|
| Date | 2026-10-18 |
| Environment | Simulated staging — demo/H2 API `127.0.0.1:8081` + Vite `127.0.0.1:5173` |
| Branch / commit | `feature/m4-adolf-freeze` / `313d4bd` |
| Login | `staff@civicconnect.local` / `Password123!` |
| How started | Backend: `SPRING_PROFILES_ACTIVE=demo`, `SERVER_PORT=8081`, `mvnw -DskipTests spring-boot:run`. Frontend: `npm run dev`. |

| Check ID | Basis | Steps / what was shown | Expected | Actual | Status | Evidence |
|---|---|---|---|---|---|---|
| UI-A-STATUS | FR008 / FR009 staff status path | Staff detail `#1`: Open → Accept → Assigned → In Progress → Resolved → Closed | Full lifecycle on detail; history complete | History: Open→Assigned→In Progress→Resolved→Closed; badge Closed; “No further status change” | Pass | `ui-status-open-accept.png`, `ui-status-assigned-start.png`, `ui-status-closed-history.png` |
| UI-A-FR010 | FR010 comments | Staff → add comments on detail → confirm listed | Comments visible with author/time | Two comments on detail (“I am starting work…”, “I have fixed…”) by Demo Staff | Pass | `ui-status-closed-history.png`, `ui-fr010-comments-375.png` |
| UI-A-375 | NFR005 | DevTools **375×812** on staff list + staff detail | No sideways scroll | List and detail fit at 375×812; no horizontal scroll visible | Pass | `ui-375-staff-list.png`, `ui-375-staff-detail.png`, `ui-fr010-comments-375.png` |

### Interpretation

* Manual UI support for M4; not counted in the 17 automated Adolf IDs.
* Staff status, FR010 comments, and 375 px staff list/detail all Pass on simulated staging (2026-10-18).
* PED: one line each pointing here + `docs/testing/evidence/adolf/`.
