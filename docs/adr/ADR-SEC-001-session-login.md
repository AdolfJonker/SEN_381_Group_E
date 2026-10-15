# ADR-SEC-001: Authentication Method (Session Login)

**Status:** Accepted (M3)

## Context / Problem
ADR-TECH-001 chose Spring Security, and FEC-001 left session vs token open. The M2 demo code used `permitAll` on every route and trusted a user id sent by the browser. Any client could act as any user, so NFR003 and RISK-004 were not met.

## Alternatives considered
- **HTTP Basic** — stateless and simple, but the password is sent on every call and there is no logout or lockout.
- **JWT token** — stateless, but needs a signing key, expiry and refresh handling.
- **Server session with a cookie** — one login call, server-side logout, works with the React app on the same site.

## Decision
Session login (`POST /api/v1/auth/login`) with BCrypt password hashes and a lockout after 5 failed attempts for 15 minutes. `SecurityConfig` returns 401 when not signed in and checks the role per route (403 for the wrong role). A `FilterStrategy` per role limits the rows each role can see (ADR-DESIGN-002).

## Consequences
- No token or signing key to manage.
- CSRF is off for the JSON API, so the session cookie is HttpOnly and SameSite=Strict (TD-004).
- The lockout is kept in memory (TD-001).
- Routes outside `/api/v1` are open so the React app can be served from the jar (TD-007).

## RTM / evidence
RTM-016 (NFR003), RTM-005 (FR005), RTM-012 (FR012). RISK-003, RISK-004, FEC-001.
Tests: TC-D-INT-01…04, TC-D-01…05 (`docs/testing/access-control-test-evidence.md`).
Implementation: `SecurityConfig.java`, `AuthService.java`, `LoginAttemptService.java`, `RequestListService.java`.
