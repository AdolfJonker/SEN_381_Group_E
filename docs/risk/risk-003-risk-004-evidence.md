RISK-003 RBAC implemented incorrectly

Evidence (M3): the M2 demo code allowed every route (permitAll) and trusted a user id sent by the browser.
Mitigation now in place: login is session based (AuthService). SecurityConfig returns 401 when not signed in and checks the role per route (summary = Management, submit = Requester). Each role has its own FilterStrategy, and RequestListService picks it from the role, so the rows each role sees are limited in one place (FR005). Each role is tested on its own: TC-D-01 to 04, TC-D-INT-02 to 04, and the FR011 decision table TC-D-05.
Residual: a new route could be added without a role rule. Unknown `/api/v1` routes default to "signed in". Routes outside `/api/v1` are open so the React app can be served from the jar (TD-007).
Check 15/10/2026: signed-out calls to `/requests/mine`, `/requests/{id}` and `/requests/{id}/status` return 401 on main (TC-D-INT-01c). The 403 in Elcke's live check (14/10) was recorded on code from before the 401 rule was merged.
Prob: Low. Impact: High. Priority: Medium. Status: Mitigated.

RISK-004 Sensitive data exposed

Evidence (M3): the M2 demo code had a public /api/users list with every email.
Mitigation now in place: every /api/v1 route except health and login needs a signed-in user. Passwords are BCrypt hashes. Five failed logins lock the email for 15 minutes. The session cookie is HttpOnly and SameSite=Strict. A requester gets 403 for another requester's request (TC-D-INT-03). A secrets scan (gitleaks) runs in CI.
Fix (18/10/2026): DemoUserLoader only creates the demo accounts when civicconnect.demo-users.enabled is true. That is the default only on the demo profile (local, in-memory H2). On postgres and staging it is off unless DEMO_USERS_ENABLED is set, and then DEMO_PASSWORD must be set too (there is no default password, start-up stops without one). The password is no longer written to the log, and the login screen no longer shows it; the demo hint only appears on the local dev server. Tests: TC-D-08 to TC-D-10 (DemoUserLoaderTest).
Residual: TC-D-08 to TC-D-10 test the loader only. They do not show that a hosted environment has the right profile and environment variables. A hosted deploy must run the postgres profile (set in the Dockerfile and render.yaml) and must not set DEMO_USERS_ENABLED or the demo profile; no test checks this. The known default password (Password123!) stays only in the demo profile, which runs on an in-memory H2 database and is for local demonstrations only.
Prob: Low. Impact: High. Priority: Medium. Status: Mitigated.
