# ADR-ARCH-001: Modular Monolith

**Status:** Accepted  
**Closes:** DEC-004 (architecture deferred at M1)

## Context / Problem
CivicConnect needs a structure for components, communication and data flow before meaningful construction. M1 correctly deferred this (DEC-004). Architecturally significant requirements now exist for RBAC (ASR-01), audit integrity (ASR-02), controlled lifecycle (ASR-03) and status visibility (ASR-04). Choosing microservices because they were taught would add distribution cost without project evidence, under CON002/CON003.

## Alternatives considered
- **Option A — Modular monolith (layered web app)** — Packages/modules inside one deployable; one process can enforce FR009, write NFR004 audit, and publish FR004 visibility in one controlled flow. Fits free-tier PaaS (one backend + DB + static frontend).
- **Option B — Microservices (split API / notification / audit services)** — Network boundaries between status, audit and notification add partial-failure modes that threaten ASR-02 and ASR-04. Multiple services, contracts and ops surfaces are not justified by current evidence; free-tier credits and SPOFs multiply.

## Decision
Adopt **Option A — modular monolith with clear internal layers**. Keep status transition, audit write and in-process status-changed events inside one Spring Boot application. Expose a versioned HTTP JSON API to the SPA. Keep module boundaries as packages (`api`, `domain`/`service`, `persistence`, `security`) so FEC-007 maintainability is real without network hops.

Logical layers (architecture, not deployment tiers):

1. **Presentation** — React SPA (Requester / Staff / Management views).
2. **API / interface** — REST controllers; DTO validation; authentication entry.
3. **Application / domain services** — ServiceRequestService, StatusTransitionGuard, FilterStrategy resolution, Observer publish.
4. **Persistence** — Spring Data JPA repositories; PostgreSQL; Flyway migrations.
5. **Cross-cutting** — Spring Security (RBAC), transactional boundaries, structured logging.

Physical deployment tiers remain fewer: browser → HTTPS → application process → managed PostgreSQL.

## Benefit
Supports ASR-01–ASR-04 with one transaction and one security filter chain; lower ops burden under CON003; diagrams stay readable for defence. New Observer subscriber or FilterStrategy class stays local (ADR-DESIGN-001 / ADR-DESIGN-002).

## Trade-off / complexity introduced
The monolith can become a “ball of mud” if package rules are ignored — mitigated by package layout, ADRs, and two-reviewer PRs (DEC-002). Later escape hatch: if notification becomes an external service, introduce REST or outbox via a new ADR; do not invent that boundary now (aligns with A2 Task 3).

## Affected modules/classes
**Planned** package layout (not yet in repo):

- `api` — REST controllers and DTOs
- `domain` / `service` — ServiceRequestService, StatusTransitionGuard, Observer subject
- `persistence` — JPA repositories and entities
- `security` — Spring Security / RBAC filter chain

## RTM / implementation evidence
Links to NFR003, NFR004, FR004, FR008, FR009, FR005 (ASR-01–ASR-04).  
**Related ADRs:** ADR-DESIGN-001, ADR-DESIGN-002, ADR-TECH-001, ADR-DATA-001.  
**Tracking / application artefact:** Planned — no runnable modular-monolith bootstrap in the repository yet; this ADR records the architecture baseline for M2.
