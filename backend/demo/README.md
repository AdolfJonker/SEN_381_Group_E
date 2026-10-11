# CivicConnect backend (demo)

Spring Boot REST API for the CivicConnect UI. It implements request submission, staff assignment, controlled status transitions with an audit history, comments, and a management summary. The architecture is a modular monolith ([ADR-ARCH-001](../../docs/adr/ADR-ARCH-001.md)); the stack is recorded in [ADR-TECH-001](../../docs/adr/ADR-TECH-001.md).

## Versions

| Component | Version | Source |
|---|---|---|
| Java | 25 | `pom.xml` (`java.version`) |
| Spring Boot | 4.1.1 | `pom.xml` (parent) |
| Maven | 3.9.16 | `.mvn/wrapper/maven-wrapper.properties` |
| Spring Web MVC, Data JPA, Security, Validation, Flyway | Managed by Spring Boot 4.1.1 | `pom.xml` |
| H2 (demo profile) / PostgreSQL driver (postgres profile) | Managed by Spring Boot 4.1.1 | `pom.xml` |

## Prerequisites

- **JDK 25** installed. Check with `java -version`, which should report 25.
- If `java -version` shows an older JDK, point `JAVA_HOME` at your JDK 25 install and put its `bin` folder first on your `PATH`, then open a new terminal.
- No local Maven install is needed; use the wrapper (`mvnw` / `mvnw.cmd`).

## Run the demo

From `backend/demo`:

```bash
# macOS / Linux
./mvnw spring-boot:run
```

```powershell
# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

The default profile is `demo`: an in-memory H2 database (PostgreSQL mode). Flyway creates the schema from `src/main/resources/db/migration/V1__init_schema.sql`, and `DemoDataLoader` seeds three users and sample requests. Data is lost when the app stops.

API base URL: `http://localhost:8081/api` (port set in `application.properties`).

Then start the frontend in another terminal:

```bash
cd ../../Frontend
npm install
npm run dev
```

Open the URL Vite prints and sign in with a demo user's email and the demo password (see below).

## Run the tests

```bash
./mvnw test
```

This runs the context-load test (`DemoApplicationTests`) and the access-control API tests (`AccessControlApiTest`: 401 for missing/wrong credentials, 403 for the wrong role, and a requester cannot open another requester's request).

## Demo users

| Role | Name | Email (login) |
|---|---|---|
| Requester | Aisha Ndlovu | `aisha@campus.edu` |
| Staff | Johan Botha | `johan@campus.edu` |
| Management | Thandi Mokoena | `thandi@campus.edu` |

All three share one demo password. It is **not** in the repository. Set it before starting the backend:

```bash
export DEMO_PASSWORD="<choose-a-password>"
```

```powershell
$env:DEMO_PASSWORD="<choose-a-password>"
```

If `DEMO_PASSWORD` is not set, the backend generates a random password and prints it once in the startup log.

## Authentication and roles (NFR003)

- Every `/api/**` route needs a logged-in user. Login uses HTTP Basic (email + password); the API is stateless, so every call carries the `Authorization` header.
- Passwords are stored as BCrypt hashes (`{bcrypt}…`).
- A missing or wrong password returns **401**. A logged-in user with the wrong role returns **403**.
- `GET /api/auth/me` checks the login and returns the user's id, name, email and role. The frontend uses the role to pick the landing view.
- Role rules are checked twice: per route in `SecurityConfig`, and per record in `ServiceRequestService` (for example, a requester can only open their own request).

## Endpoints

| Method | Path | Who | Purpose |
|---|---|---|---|
| GET | `/api/auth/me` | any logged-in user | Check login; returns id, name, email, role |
| GET | `/api/requests` | any user | Requester: own requests. Staff/Management: all requests |
| GET | `/api/requests/{id}` | any user | Request detail with history and comments (a requester can only open their own) |
| POST | `/api/requests` | Requester | Submit a request (`title`, `description`, `category`) |
| POST | `/api/requests/{id}/assign` | Staff | Accept an Open request (Open → Assigned) |
| POST | `/api/requests/{id}/status` | Staff | Move to the next status (`toStatus`, optional `note`); illegal transitions return 400 |
| POST | `/api/requests/{id}/comments` | Staff | Add a comment (`content`) |
| GET | `/api/summary` | Management | Counts by status, plus overdue (open more than 7 days) |

Errors are returned as JSON `ProblemDetail` with a `message` field.

## Code layout

| Package | Contents |
|---|---|
| `config` | `SecurityConfig` (HTTP Basic, role rules per route, CORS), `AppUserDetailsService`, `DemoDataLoader` |
| `domain` | `ServiceRequest` (with `@Version`), `StatusHistory`, `Comment`, `UserAccount`, `Role`, `RequestCategory`, `RequestStatus` (FR009 transition table) |
| `repo` | Spring Data JPA repositories |
| `service` | `ServiceRequestService`: transactional create / assign / transition / comment / summary, and role checks |
| `web` | `ServiceRequestController`, `AuthController`, `CurrentUserResolver`, `ApiExceptionHandler`, DTOs |

Related decisions: [ADR-DATA-001](../../docs/adr/ADR-DATA-001) (transaction + optimistic concurrency), [ADR-DESIGN-001](../../docs/adr/ADR-DESIGN-001-status-transition-observer.md) (transition guard + Observer), [ADR-DESIGN-002](../../docs/adr/ADR-DESIGN-002-rbac-strategy.md) (Strategy per role). Full reasoning is in the PED v2.0 (`docs/PED/SEN381_Project_M2_draft.docx`).

## Optional PostgreSQL profile

Create a database, set the credentials as environment variables, then run with the `postgres` profile:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/civicconnect"
export DB_USER="postgres"
export DB_PASSWORD="<your-password>"
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/civicconnect"
$env:DB_USER="postgres"
$env:DB_PASSWORD="<your-password>"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Never commit real credentials.

## Known limitations

- **HTTP Basic.** The browser keeps the email and password in memory and sends them on every call. This is only safe over HTTPS (staging must use HTTPS). There is no logout on the server, no token expiry and no lockout after failed attempts (see Technical Debt Register).
- **Observer not implemented yet.** The `StatusHistory` row is written directly in `ServiceRequestService`. The observer classes in ADR-DESIGN-001 / ADR-INT-001 (`AuditLogObserver`, `StatusVisibilityUpdater`) do not exist yet.
- **Strategy not implemented yet.** Role-based list filtering is a `switch` in `ServiceRequestService.listFor`. The `FilterStrategy` classes in ADR-DESIGN-002 do not exist yet. Staff currently see all requests (FR005 scoping pending).
- **No separate `StatusTransitionGuard`.** The legal-transition table is `RequestStatus.canTransitionTo`.
- **Concurrency.** `@Version` is present, but an optimistic-lock conflict is not yet mapped to a 409 "reload and try again" response.
- **Schema.** No `CHECK` constraint on `status` yet, and history rows are mapped with `orphanRemoval` (append-only is not enforced at the database level).
- **Request IDs** (`SR-1001`, …) come from an in-memory counter; this is not safe for multiple instances.
- **Seed data** runs on any empty database, including under the postgres profile. Its password comes from `DEMO_PASSWORD`.
- **Tests.** Context-load and access-control API tests only so far.
