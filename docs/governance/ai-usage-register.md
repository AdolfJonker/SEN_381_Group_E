# AI Usage Register

Every AI contribution used in controlled artefacts is logged. A human reviews and verifies before merge.

| Date | Member | Tool | Artefact / task | What AI did | What was verified | Changed or rejected |
|---|---|---|---|---|---|---|
| 2026-09-09 | Adolf | Claude | M2 architecture notes | Draft C4 / stack wording | Checked against ADR-ARCH-001 / ADR-TECH-001 | Edited for team voice |
| 2026-09-30 | Adolf | Claude | M2 PED excerpts | Suggested structure for deployment section | Compared to Render/Railway constraints | Kept high-level only |
| 2026-10-14 | Adolf | Cursor Agent | M3 Adolf slice | Implemented FR006/FR013 filters, FR012 summary, staff detail wiring, API integration tests, Dockerfile/render.yaml, E2E scaffolds, PED drafts, RTM/risk updates | Ran `ServiceRequestApiIntegrationTest` (pass); reviewed write-order vs ADR-DATA-001; confirmed secrets stay in env | Renamed `status_history.timestamp` → `changed_at` for H2; adjusted Boot 4 test deps (`webmvc-test`, Jackson 3 packages); rejected unsafe duplicate Flyway V2 |
