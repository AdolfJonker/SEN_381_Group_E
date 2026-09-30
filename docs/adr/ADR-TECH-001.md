# ADR-TECH-001: Technology Stack Selection

**Status:** Proposed  
**Closes:** DEC-003 (stack deferred at M1)

## Context / Problem
M1 deferred stack selection until requirements and constraints were stable. RISK-005 warned against choosing tools for interest. CON003 limits the team to free/OSS. ASRs now demand server-side RBAC, transactional audit writes, and a responsive web UI. Versions below are the **team-confirmed intended baseline**; they are **not yet evidenced** in `pom.xml` / `package.json` (no application bootstrap in the repository as of this ADR).

## Alternatives considered
| Criterion | Candidate 1 — Java 25 / Spring Boot 4.1.x + React 19 (Vite) + PostgreSQL 17 (chosen) | Candidate 2 — Node.js / Express + React 19 (Vite) + PostgreSQL 17 |
|---|---|---|
| ASR-01 RBAC | Spring Security is a mature, documented authz stack for role checks on every route (app-layer, per Cost-vs-Security) | Passport/middleware possible, but team has clearer Java/Spring coursework path for server-side enforcement |
| ASR-02 / ASR-03 transactions | Spring `@Transactional` + JPA matches A2 Task 2 atomic assign/audit recommendation | Possible with an ORM, but weaker fit to the team’s existing Java persistence teaching |
| Maintainability (FEC-007) | Clear layered packages; strong typing on the server | Flexible but easier to grow unstructured without discipline |
| Cost / licensing (CON003) | All core components OSS | Also OSS — not a differentiator |
| Deployment | Executable JAR + Vite static SPA; widely documented on free PaaS | Also deployable; similar free-tier constraints |
| Team capability (RISK-005) | Preferred: Java backend familiarity; React + Vite frontend shared by all | Would force full-stack JS on the server without a clear capability gain |

Other stacks (e.g. .NET, Python/Django) were not shortlisted because they add a new language learning load for no ASR gain.

## Decision
Select **Candidate 1**:

| Layer | Technology | Intended version baseline | Licence |
|---|---|---|---|
| Runtime | Java (Temurin/OpenJDK) | **25 LTS** | GPL Classpath Exception (OpenJDK) |
| Backend framework | Spring Boot | **4.1.x** (latest 4.1 patch at bootstrap) | Apache License 2.0 |
| Security | Spring Security (via Boot starter) | Managed by Boot 4.1 BOM | Apache License 2.0 |
| Persistence API | Spring Data JPA / Hibernate | Managed by Boot BOM | Apache License 2.0 |
| DB migrations | Flyway | Managed compatible release | Apache License 2.0 |
| Database | PostgreSQL | **17** (managed instance) | PostgreSQL License |
| Local/test DB | H2 (test profile only) | Managed by Boot | MPL 2.0 / EPL |
| Frontend | React | **19.x** | MIT |
| Bundler / dev server | Vite | **8.x** (or current stable pin) | MIT |
| Build (backend) | Maven | 3.9+ | Apache License 2.0 |
| Build (frontend) | Node.js + npm | Node **22 LTS** (or team-confirmed LTS) | Node licence / various |
| API style | REST/JSON over HTTPS | `/api/v1/...` | — |

Pin exact patches in `pom.xml` / `package.json` and the application README **at bootstrap**. Do not drift majors mid-project without a new ADR.

## Benefit
Direct fit to ASR-01–ASR-05; licences comply with CON003; matches A2 persistence/API recommendations (local transactions; in-process events; HTTP only at the UI boundary); app-layer RBAC/audit matches the Cost-vs-Security trade-off.

## Trade-off / complexity introduced
JVM memory on free tiers is tight (RISK-006) — must set heap flags and accept cold starts. Spring Boot 4.x is a newer major line — pin 4.1.x patches and avoid mid-project major jumps without an ADR. Frontend and backend are two build toolchains (npm/Vite + Maven) — to be documented in README at bootstrap.

**Deferred inside this decision:** exact PaaS vendor account, production backup vendor, and email/SMS provider (still FEC / future ADR).

## Affected modules/classes
**Planned** at bootstrap (not yet in repository):

- Backend: Spring Boot project with Maven wrapper; packages aligned to ADR-ARCH-001
- Frontend: React 19 + Vite SPA talking to `/api` routes
- Config: `application.yml` + env-supplied secrets (never committed)

## RTM / implementation evidence
Evidence links: Spring Boot 4.1 system requirements / support policy; OpenJDK 25 LTS; PostgreSQL 17 licence; React and Vite MIT licensing; A2 Tasks 2–3 recommendations; CON003; RISK-005; RISK-006.  
**Tracking / application artefact:** Versions **baselined in this ADR**; **will be pinned** in `pom.xml` / `package.json` and application README at bootstrap. **Planned / Not Yet Implemented.**
