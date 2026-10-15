import { test, expect } from "@playwright/test";

/**
 * TC-A-E2E-02: unauthenticated API access is blocked (401).
 * Complements Donya's role tests; proves the gate on staging.
 */
test("TC-A-E2E-02 unauthenticated request list is blocked", async ({ request }) => {
  const response = await request.get("/api/v1/requests");
  expect([401, 403]).toContain(response.status());
});

test("TC-A-E2E-02b staff cannot open management summary", async ({ request }) => {
  const login = await request.post("/api/v1/auth/login", {
    data: {
      email: "staff@civicconnect.local",
      password: "Password123!",
    },
  });
  expect(login.ok()).toBeTruthy();

  const summary = await request.get("/api/v1/requests/summary");
  expect(summary.status()).toBe(403);
});
