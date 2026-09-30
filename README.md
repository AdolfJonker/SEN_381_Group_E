# CivicConnect — SEN381 Group E

CivicConnect is a campus service-request system. Requesters submit facility, equipment, security, IT and maintenance requests. Staff accept them and move them through a controlled lifecycle (Open → Assigned → In Progress → Resolved → Closed), with every status change recorded in an audit history. Management sees a summary of outstanding, overdue and resolved work.

This repository holds both the engineering documentation (Project Engineering Document, ADRs, RTM, risks) and the application code (Spring Boot backend + React frontend).

**Current status (Milestone 2):** the architecture, technology and initial design baseline is accepted (PED v2.0). A runnable demo backend and frontend exist. Several design decisions are still being translated into code; see [Known limitations](#known-limitations).

## Team

| Member | Lane | Milestone 2 responsibility |
|---|---|---|
| Adolf Jonker | Architecture & technology (Criteria B + D) | ASRs, architecture + C4 diagrams, ADR-ARCH-001, stack selection ADR-TECH-001, deployment compatibility, backend/frontend bootstrap |
| Elcke Van Der Berg | Requirements & traceability (Criterion A) | PED continuity and version control, RTM evolution, risk register and forward engineering considerations, baseline sign-off |
| Donya Pretorius | Data, design & integration (Criteria C + E) | Entity model, ADR-DATA-001, design-pattern ADRs (ADR-DESIGN-001/002), integration decision ADR-INT-001 |

All three share Criterion F (development, documentation and GitHub evidence).

## Repository structure

```text
.
├── backend/demo/                  Spring Boot API (Java 25, Maven wrapper)
│   ├── src/main/java/com/example/demo/
│   │   ├── config/                Security config, demo data loader
│   │   ├── domain/                Entities + enums (ServiceRequest, StatusHistory, Comment, UserAccount, RequestStatus, …)
│   │   ├── repo/                  Spring Data JPA repositories
│   │   ├── service/               ServiceRequestService (transactions, role checks, status transitions)
│   │   └── web/                   REST controllers, DTOs, error handler
│   ├── src/main/resources/
│   │   ├── application*.properties   demo (H2) and postgres profiles
│   │   └── db/migration/V1__init_schema.sql   Flyway schema
│   └── README.md                  Backend setup, demo users, endpoints
├── Frontend/                      React + Vite single-page app
│   └── src/                       api/client.js, components/, data/
├── docs/
│   ├── PED/                       Project Engineering Document (v1.0 and v2.0) + version history
│   ├── adr/                       Architecture Decision Records
│   ├── architecture/              C4 Context and Container diagrams
│   ├── requirements/              Functional / non-functional requirements, RTM
│   ├── risk/                      Risk register, engineering decision log, forward engineering considerations
│   ├── desicions/                 M1 baseline review (controlled change check)
│   └── governance/                AI usage register, baseline sign-off, team working agreement
└── .github/pull_request_template.md
```

## Technology stack

Versions are taken from `backend/demo/pom.xml`, `Frontend/package.json` / `package-lock.json` and the Maven wrapper. The decision and its rationale are in [ADR-TECH-001](docs/adr/ADR-TECH-001.md).

| Layer | Technology | Version in repo |
|---|---|---|
| Runtime | Java (OpenJDK) | 25 |
| Backend framework | Spring Boot (Web MVC, Data JPA, Security, Validation) | 4.1.1 |
| Build (backend) | Maven via wrapper (`mvnw`) | 3.9.16 |
| DB migrations | Flyway (managed by Spring Boot) | Boot-managed |
| Database (demo profile) | H2 in-memory, PostgreSQL mode | Boot-managed |
| Database (postgres profile) | PostgreSQL JDBC driver; ADR target PostgreSQL 17 | Boot-managed |
| Frontend | React / React DOM | 19.3 (range `^19.2.8`) |
| Bundler / dev server | Vite | 8.3 (range `^8.3.0`) |
| Node.js (for Vite) | Node | 20.19+ or 22.12+ |

## Quick start

1. **Backend**: see [`backend/demo/README.md`](backend/demo/README.md). In short: JDK 25, then `./mvnw spring-boot:run` from `backend/demo` (API on `http://localhost:8081/api`, in-memory H2 with demo data).
2. **Frontend**: from `Frontend/`, run `npm install` then `npm run dev`, and open the URL Vite prints. The UI calls the API at `http://localhost:8081/api` (override with the `VITE_API_BASE` environment variable).
3. Pick a demo role (Requester, Staff or Management) on the login screen.

Tests: `./mvnw test` in `backend/demo`.

## Engineering documentation

| Artefact | Path |
|---|---|
| PED v2.0 (M2, accepted baseline + sign-off) | [`docs/PED/SEN381_Project_M2_draft.docx`](docs/PED/SEN381_Project_M2_draft.docx) |
| PED v1.0 (M1 baseline) | [`docs/PED/PED_v1.0.docx`](docs/PED/PED_v1.0.docx) |
| Version history | [`docs/PED/version-history.md`](docs/PED/version-history.md) |
| ADR-ARCH-001 — Modular monolith | [`docs/adr/ADR-ARCH-001.md`](docs/adr/ADR-ARCH-001.md) |
| ADR-TECH-001 — Technology stack | [`docs/adr/ADR-TECH-001.md`](docs/adr/ADR-TECH-001.md) |
| ADR-DATA-001 — Atomic transaction + optimistic concurrency | [`docs/adr/ADR-DATA-001`](docs/adr/ADR-DATA-001) |
| ADR-DESIGN-001 — Status transition guard + Observer | [`docs/adr/ADR-DESIGN-001-status-transition-observer.md`](docs/adr/ADR-DESIGN-001-status-transition-observer.md) |
| ADR-DESIGN-002 — Strategy-per-role filtering | [`docs/adr/ADR-DESIGN-002-rbac-strategy.md`](docs/adr/ADR-DESIGN-002-rbac-strategy.md) |
| ADR-INT-001 — In-process observer seam | [`Initial Integration Decision`](Initial%20Integration%20Decision) (repo root) |
| C4 Context diagram | [`docs/architecture/civicconnect-c4-context.jpg`](docs/architecture/civicconnect-c4-context.jpg) |
| C4 Container diagram | [`docs/architecture/civicconnect-c4-container.jpg`](docs/architecture/civicconnect-c4-container.jpg) |
| Requirements Traceability Matrix | [`docs/requirements/rtm.md`](docs/requirements/rtm.md) |
| Functional / non-functional requirements | [`docs/requirements/functional-requirements.md`](docs/requirements/functional-requirements.md), [`docs/requirements/non-functional-requirements.md`](docs/requirements/non-functional-requirements.md) |
| Engineering decision log (DEC-001…) | [`docs/risk/Engineering-Decision-log`](docs/risk/Engineering-Decision-log) |
| Risk register | [`docs/risk/risk-register.md`](docs/risk/risk-register.md) |
| Forward engineering considerations | [`docs/risk/forward-engineering-considerations.md`](docs/risk/forward-engineering-considerations.md) |

## Contribution workflow

`main` is protected. Every change, including documentation, goes through a Pull Request:

1. Open (or pick) a GitHub issue for the work.
2. Create a branch from `main` (e.g. `feature/<short-name>`).
3. Commit with clear messages and open a PR using the PR template. Describe what changed and add `Closes #<issue>`.
4. **Two approvals from the other two team members** are required before merge. The author cannot approve their own PR.
5. Push all fixes **before** requesting review: a new push dismisses existing approvals.
6. Record material AI assistance in the [AI usage register](docs/governance/ai-usage-register.md).
7. Never commit secrets. Database credentials come from environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`).

## Known limitations

These are known and intentional at Milestone 2; they are tracked for later milestones.

- **Demo authentication only.** The API trusts an `X-User-Id` request header, and Spring Security is configured to `permitAll`. Any client can act as any user. Role checks happen in `ServiceRequestService`, but there is no real login, session or token yet.
- **Design patterns not yet in code.** The status-change audit row is written directly in `ServiceRequestService`; the Observer classes from ADR-DESIGN-001 / ADR-INT-001 (`AuditLogObserver`, `StatusVisibilityUpdater`) do not exist yet. Role-based list filtering is a `switch` on role; the `FilterStrategy` classes from ADR-DESIGN-002 are not implemented yet. The FR009 transition table lives in `RequestStatus`.
- **Staff see all requests.** Staff and Management both receive the full request list; FR005 staff scoping is not yet applied.
- **Concurrency.** `ServiceRequest` has an optimistic-lock `@Version`, but a conflict is not yet mapped to a "reload and try again" response, and the client does not send the version.
- **Schema backstops.** The Flyway schema has no `CHECK` constraint on status yet (ADR-DATA-001).
- **Personal data.** `GET /api/users` returns user email addresses.
- **Demo data.** Seed users have the placeholder password `{noop}demo`. The loader seeds any empty database, including under the postgres profile.
- **Tests.** Only a Spring context-load test exists (`DemoApplicationTests`).
- **Not deployed.** Deployment direction is documented in the PED (§5.8); there is no hosted environment or CI pipeline yet.
