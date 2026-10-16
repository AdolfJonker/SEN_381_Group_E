# M3 presentation demo prep (simulated staging)

Owner: Adolf · ~4 minutes for the critical journey  
Environment: credible simulated staging (Vite + Spring `demo`/H2) per [SEN381_M3.pdf](../../SEN381_M3.pdf) §14.

## Prerequisites

- JDK 25, Node 20.19+ or 22.12+
- Repo at RC tag `v3.0.0-rc1` or the recorded submission SHA ([`rollback.md`](./rollback.md))

## Start simulated staging

**Terminal 1 — backend (API on 8081):**

```powershell
cd "src\backend"
$env:SPRING_PROFILES_ACTIVE = "demo"
$env:SERVER_PORT = "8081"
.\mvnw.cmd spring-boot:run
```

**Terminal 2 — frontend (UI on 5173):**

```powershell
cd "src\frontend"
npm install
npm run dev
```

Open `http://127.0.0.1:5173`. Confirm `GET http://127.0.0.1:8081/api/v1/health` returns OK.

## Demo logins (password for all: `Password123!`)

| Role | Email |
|---|---|
| Staff | `staff@civicconnect.local` |
| Requester | `requester@civicconnect.local` |
| Management | `management@civicconnect.local` |

## Critical journey (live walkthrough)

1. **Requester** — log in → create a service request → note the ID.
2. **Staff** — log in → open list (FR006 filters if useful) → accept the Open request (FR008) → advance status through the legal path toward Closed.
3. **Conflict cue (optional, 30 s)** — if two tabs accept/update with a stale version, show the “Reload and try again” 409 message (ADR-DATA-001).
4. **Management** — log in → open summary (FR012); staff must not reach summary (NFR003).
5. **State RC** — announce tag/commit `v3.0.0-rc1` / SHA from [`rollback.md`](./rollback.md).

## Re-run automated evidence against this environment (optional before presentation)

```powershell
cd e2e
$env:BASE_URL = "http://127.0.0.1:5173"
npx playwright test --reporter=list
```

```powershell
# From repo root; API must be on 8081 with seeded Open rows for a non-empty list
node scripts/perf-list.mjs
```

## Assessor note (PED / Moodle)

No public cloud URL is required for M3. Demonstration uses the identified RC on this simulated staging stack. Optional Render deploy remains documented in [`render-deploy.md`](./render-deploy.md) but was not used for freeze.
