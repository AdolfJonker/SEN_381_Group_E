# CivicConnect frontend

React 19 + Vite single-page app. It talks to the Spring Boot API in `backend/demo`.

## Run

```bash
npm install
npm run dev
```

The API address comes from `VITE_API_BASE` (default `http://localhost:8081/api`).
Sign in with a demo user's email and the `DEMO_PASSWORD` set on the backend (see `backend/demo/README.md`).

## Checks (also run in CI)

| Command | What it checks |
|---|---|
| `npm run lint` | ESLint rules (code quality) |
| `npm test` | Unit tests with Node's built-in test runner (`*.test.js`) |
| `npm run build` | The app compiles |

## Screens by role

| Role | Lands on | Requirements |
|---|---|---|
| Management | Summary dashboard: counts by status (Open, Overdue, Resolved, Closed, plus Assigned and In Progress) and the request list with category and status filters | FR012, FR013 |
| Staff | Request list with search, status and category filters, and date sort | FR005, FR006 |
| Requester | "My requests" and "New request" | FR001–FR003 |

The landing view is chosen in `src/auth/roleLanding.js`. The role always comes from the server (`GET /api/auth/me`); the UI never decides it.
Hiding a button is only for usability. The real access check is on the server (NFR003).

## Login and credentials

- Login uses HTTP Basic. The email and password are kept **in memory only** (not localStorage), so a page refresh signs the user out.
- Any 401 from the API sends the user back to the login screen.
- Outside local development the app must be served over HTTPS.

## Key files

| File | Purpose |
|---|---|
| `src/api/client.js` | API client; adds the `Authorization` header; maps 401/403 to clear messages |
| `src/auth/roleLanding.js` | Role → landing view, and which views each role may open |
| `src/components/LoginScreen.jsx` | Email + password login |
| `src/components/ManagementDashboard.jsx` | Management landing: summary + filtered list |
| `src/components/ManagementSummary.jsx` | FR012 summary cards (click a status card to filter) |
| `src/components/RequestList.jsx` | Staff / Requester list |
| `src/utils/requestFilters.js` | Shared category/status filter and date sort |
