# CivicConnect documentation index

All engineering documentation for SEN381 Group E lives under `docs/`, grouped by topic. The Project Engineering Document (PED) is compiled in Word from these sources.

Module briefs and Word working copies of the PED are kept in the repository root (`SEN381_M3.pdf`, `Risk register m1.docx`).

## PED — Project Engineering Document

| Document | Path |
|---|---|
| Version history | [`PED/version-history.md`](PED/version-history.md) |
| PED v1.0 (M1 baseline) | [`PED/PED-v1.0.md`](PED/PED-v1.0.md), [`PED/PED_v1.0.docx`](PED/PED_v1.0.docx) |
| PED v2.0 (M2) | [`PED/SEN381_PED v2.0.docx`](PED/SEN381_PED%20v2.0.docx), draft: [`PED/SEN381_Project_M2_draft.docx`](PED/SEN381_Project_M2_draft.docx) |
| M3 PED drafts (Adolf: §2–4, §8) | [`PED/contributions/m3-adolf-sections.md`](PED/contributions/m3-adolf-sections.md) |
| M3 PED paste pack (Adolf) | [`PED/contributions/m3-adolf-paste.md`](PED/contributions/m3-adolf-paste.md) |

## Requirements and traceability

| Document | Path |
|---|---|
| Functional requirements (FR001–FR013) | [`requirements/functional-requirements.md`](requirements/functional-requirements.md) |
| Non-functional requirements (NFR001–NFR005) | [`requirements/non-functional-requirements.md`](requirements/non-functional-requirements.md) |
| Requirements Traceability Matrix | [`requirements/rtm.md`](requirements/rtm.md) |

## Architecture and design

| Document | Path |
|---|---|
| C4 Context diagram | [`architecture/civicconnect-c4-context.jpg`](architecture/civicconnect-c4-context.jpg) |
| C4 Container diagram | [`architecture/civicconnect-c4-container.jpg`](architecture/civicconnect-c4-container.jpg) |
| Design problem 1 — status transition guard + Observer | [`design/design-problem-1-status-transition.md`](design/design-problem-1-status-transition.md) |
| Design problem 2 — role-based request filtering | [`design/design-problem-2-rbac-filtering.md`](design/design-problem-2-rbac-filtering.md) |
| Staff view UI design | [`design/staff-view-design.pdf`](design/staff-view-design.pdf) |

## Architecture Decision Records

| ADR | Path |
|---|---|
| ADR-ARCH-001 — Modular monolith | [`adr/ADR-ARCH-001.md`](adr/ADR-ARCH-001.md) |
| ADR-TECH-001 — Technology stack | [`adr/ADR-TECH-001.md`](adr/ADR-TECH-001.md) |
| ADR-DATA-001 — Atomic transaction + optimistic concurrency | [`adr/ADR-DATA-001.md`](adr/ADR-DATA-001.md) |
| ADR-DESIGN-001 — Status transition guard + Observer | [`adr/ADR-DESIGN-001-status-transition-observer.md`](adr/ADR-DESIGN-001-status-transition-observer.md) |
| ADR-DESIGN-002 — Strategy-per-role filtering | [`adr/ADR-DESIGN-002-rbac-strategy.md`](adr/ADR-DESIGN-002-rbac-strategy.md) |
| ADR-INT-001 — In-process observer seam | [`adr/ADR-INT-001-initial-integration.md`](adr/ADR-INT-001-initial-integration.md) |
| ADR-SEC-001 — Session login | [`adr/ADR-SEC-001-session-login.md`](adr/ADR-SEC-001-session-login.md) |

## Data and persistence

| Document | Path |
|---|---|
| Entity model | [`data/entity-model.md`](data/entity-model.md) |
| Access patterns | [`data/access-patterns.md`](data/access-patterns.md) |
| Transaction and concurrency decision | [`data/transaction-and-concurrency-decision.md`](data/transaction-and-concurrency-decision.md) |
| Scalability and SPOF notes | [`data/scalability-and-spof-notes.md`](data/scalability-and-spof-notes.md) |

## Decisions

| Document | Path |
|---|---|
| Engineering decision log (DEC-001…, D2–D6) | [`decisions/engineering-decision-log.md`](decisions/engineering-decision-log.md) |
| D2–D6 status transition authority | [`decisions/D2-D6-status-transition-authority.md`](decisions/D2-D6-status-transition-authority.md) |
| M1 baseline review (controlled change check) | [`decisions/decision-log.md`](decisions/decision-log.md) |

## Risk and technical debt

| Document | Path |
|---|---|
| Risk register (with v4.0.0 final review) | [`risk/risk-register.md`](risk/risk-register.md) |
| RISK-003 / RISK-004 evidence | [`risk/risk-003-risk-004-evidence.md`](risk/risk-003-risk-004-evidence.md) |
| Technical debt register (TD-…) | [`risk/technical-debt.md`](risk/technical-debt.md) |
| Forward engineering considerations (FEC-…) | [`risk/forward-engineering-considerations.md`](risk/forward-engineering-considerations.md) |

## Testing and quality

| Document | Owner | Path |
|---|---|---|
| Quality strategy (PED §6) | Elcke | [`testing/quality-strategy.md`](testing/quality-strategy.md) |
| Defects and quality interpretation (PED §9) | Elcke | [`testing/defects-and-interpretation.md`](testing/defects-and-interpretation.md) |
| Status lifecycle test evidence (TC-E-…) | Elcke | [`testing/status-lifecycle-test-evidence.md`](testing/status-lifecycle-test-evidence.md) |
| Access control test evidence (TC-D-…) | Donya | [`testing/access-control-test-evidence.md`](testing/access-control-test-evidence.md) |
| Persistence, E2E and manual UI evidence (TC-A-…) | Adolf | [`testing/adolf-test-evidence.md`](testing/adolf-test-evidence.md) |
| List performance (PERF-LIST-01) | Adolf | [`testing/performance-list-evidence.md`](testing/performance-list-evidence.md) |
| Live API check, 14/10/2026 | Elcke | [`testing/evidence/live-check-2026-10-14.txt`](testing/evidence/live-check-2026-10-14.txt) |
| UI screenshots (staff status, FR010, 375 px) | Adolf | [`testing/evidence/adolf/`](testing/evidence/adolf/) |

## Staging and release

| Document | Path |
|---|---|
| Staging vs development | [`staging/staging-vs-dev.md`](staging/staging-vs-dev.md) |
| Simulated staging demo walkthrough | [`staging/demo-prep.md`](staging/demo-prep.md) |
| Rollback and release-candidate tags | [`staging/rollback.md`](staging/rollback.md) |
| Optional Render cloud deploy | [`staging/render-deploy.md`](staging/render-deploy.md) |
| Release evidence summary (team) | [`release/release-evidence-summary.md`](release/release-evidence-summary.md) |
| M3 release evidence summary (Adolf) | [`release/m3-release-evidence-summary.md`](release/m3-release-evidence-summary.md) |
| M3 close-out checklist (Adolf) | [`release/m3-adolf-closeout.md`](release/m3-adolf-closeout.md) |

## Governance

| Document | Path |
|---|---|
| Team working agreement | [`governance/team-working-agreement.md`](governance/team-working-agreement.md) |
| Baseline sign-off | [`governance/baseline-signoff.md`](governance/baseline-signoff.md) |
| AI usage register | [`governance/ai-usage-register.md`](governance/ai-usage-register.md) |
