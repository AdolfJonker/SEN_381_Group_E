# CivicConnect — SEN381 Group E

CivicConnect is a campus service-request system. Requesters submit facility, equipment, security, IT and maintenance requests. Staff accept them and move them through a controlled lifecycle (Open → Assigned → In Progress → Resolved → Closed), with every status change recorded in an audit history. Management sees a summary of outstanding, overdue and resolved work.

This repository holds both the engineering documentation (Project Engineering Document, ADRs, RTM, risks) and the application code (Spring Boot backend + React frontend).

**Current status (Milestone 4):** M3 release candidate tagged `v3.0.0-rc1`; M4 release freeze (`v4.0.0`) in progress. See [Known limitations](#known-limitations).

All engineering documentation lives under [`docs/`](docs/README.md); start at the [documentation index](docs/README.md).

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
├── src/
│   ├── backend/                   Spring Boot API (Java 25, Maven wrapper); layout in src/README.md
│   └── frontend/                  React + Vite single-page app
├── e2e/                           Playwright end-to-end tests
├── scripts/                       perf-list.mjs (list performance check)
├── docs/                          All engineering documentation — see docs/README.md
│   ├── PED/                       PED versions, version history, member contributions
│   ├── requirements/              Functional / non-functional requirements, RTM
│   ├── architecture/              C4 Context and Container diagrams
│   ├── adr/                       Architecture Decision Records
│   ├── design/                    Design problems (PED §6) and UI design
│   ├── data/                      Entity model, access patterns, transactions, SPOF notes
│   ├── decisions/                 Engineering decision log, D2–D6, M1 baseline review
│   ├── risk/                      Risk register, technical debt, forward engineering
│   ├── testing/                   Quality strategy, test evidence, defects, screenshots
│   ├── staging/                   Simulated staging, demo prep, rollback, optional Render
│   ├── release/                   Release evidence summaries, M3 close-out
│   └── governance/                AI usage register, baseline sign-off, team agreement
├── Dockerfile, render.yaml        Optional cloud staging
└── .github/                       CI workflow, PR template
```

## Technology stack

Versions are taken from `src/backend/pom.xml`, `src/frontend/package.json` / `package-lock.json` and the Maven wrapper. The decision and its rationale are in [ADR-TECH-001](docs/adr/ADR-TECH-001.md).

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

1. **Backend** (JDK 25): from `src/backend`, set `SPRING_PROFILES_ACTIVE=demo` and `SERVER_PORT=8081`, then run `./mvnw spring-boot:run` (in-memory H2 with demo data). PostgreSQL setup: [`src/README.md`](src/README.md).
2. **Frontend**: from `src/frontend`, run `npm install` then `npm run dev`, and open `http://127.0.0.1:5173`. Vite proxies `/api/v1` to `http://localhost:8081`.
3. Sign in as a demo user (Requester, Staff or Management). Full walkthrough: [`docs/staging/demo-prep.md`](docs/staging/demo-prep.md).

Tests: `./mvnw verify` in `src/backend`.

## Engineering documentation

The full list, grouped by topic, is in the [documentation index](docs/README.md). The most-used artefacts:

| Artefact | Path |
|---|---|
| Documentation index | [`docs/README.md`](docs/README.md) |
| PED version history | [`docs/PED/version-history.md`](docs/PED/version-history.md) |
| Requirements Traceability Matrix | [`docs/requirements/rtm.md`](docs/requirements/rtm.md) |
| Architecture Decision Records | [`docs/adr/`](docs/adr/) |
| Risk register | [`docs/risk/risk-register.md`](docs/risk/risk-register.md) |
| Technical debt register | [`docs/risk/technical-debt.md`](docs/risk/technical-debt.md) |
| Release evidence summary | [`docs/release/release-evidence-summary.md`](docs/release/release-evidence-summary.md) |

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

Tracked technical debt is in [`docs/risk/technical-debt.md`](docs/risk/technical-debt.md). Highlights:

- **SPA fallback auth (TD-007).** `/api/v1/**` routes are authenticated / role-gated; `.anyRequest().permitAll()` remains only so packaged React Router paths can load.
- **H2 append-only (TD-006).** NFR004 DB triggers apply on the postgres profile; demo/H2 is app-layer only.
- **PERF-LIST-01.** Recorded on simulated staging (demo/H2, 9 rows), not public cloud — see [`docs/testing/performance-list-evidence.md`](docs/testing/performance-list-evidence.md).
- **StatusVisibilityUpdater (NFR001 / FR004).** Still pending team scope decision.
- **Release-candidate tag.** Use `v3.0.0-rc1` (see `docs/staging/rollback.md` and PED version history).
