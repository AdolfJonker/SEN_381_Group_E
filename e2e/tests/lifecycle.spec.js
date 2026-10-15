import { test, expect } from "@playwright/test";

const PASSWORD = "Password123!";

async function login(page, email) {
  await page.goto("/");
  // Already signed in from a shared cookie? Sign out first.
  const signOut = page.getByRole("button", { name: /sign out/i });
  if (await signOut.count()) {
    await signOut.click();
    await expect(page.getByLabel(/^email$/i)).toBeVisible({ timeout: 10_000 });
  }
  await page.getByLabel(/^email$/i).fill(email);
  await page.getByLabel(/^password$/i).fill(PASSWORD);
  await page.getByRole("button", { name: /sign in/i }).click();
  await expect(page.getByRole("button", { name: /sign out/i })).toBeVisible({ timeout: 20_000 });
}

/**
 * TC-A-E2E-01: submit → accept → In Progress → Resolved → Closed; requester sees status.
 * Covers FR001–FR011 path (E2E). Does not claim FR005/RTM-005.
 * Run against staging: STAGING_BASE_URL=https://… npm test
 */
test("TC-A-E2E-01 full lifecycle visible to requester", async ({ browser }) => {
  const title = `E2E lifecycle ${Date.now()}`;

  const requesterContext = await browser.newContext();
  const requesterPage = await requesterContext.newPage();
  await login(requesterPage, "requester@civicconnect.local");
  await requesterPage.getByRole("button", { name: "Submit a request" }).click();
  await requesterPage.getByLabel(/^category$/i).selectOption({ label: "IT Support" });
  await requesterPage.getByLabel(/^title$/i).fill(title);
  await requesterPage.getByLabel(/^description$/i).fill("Playwright staging lifecycle request.");
  await requesterPage.getByRole("button", { name: "Submit request" }).click();
  await expect(requesterPage.getByText(/Request #\d+ submitted/i)).toBeVisible({ timeout: 20_000 });

  const staffContext = await browser.newContext();
  const staffPage = await staffContext.newPage();
  await login(staffPage, "staff@civicconnect.local");
  await expect(staffPage.getByText(title)).toBeVisible({ timeout: 20_000 });
  await staffPage.locator("tr", { hasText: title }).getByRole("button").click();

  await staffPage.getByRole("button", { name: /accept request/i }).click();
  await expect(staffPage.getByText(/assigned/i).first()).toBeVisible();

  await staffPage.getByRole("button", { name: /start work/i }).click();
  await expect(staffPage.getByText(/in progress/i).first()).toBeVisible({ timeout: 10_000 });
  await staffPage.getByRole("button", { name: /mark resolved/i }).click();
  await expect(staffPage.getByText(/resolved/i).first()).toBeVisible({ timeout: 10_000 });
  await staffPage.getByRole("button", { name: /close request/i }).click();
  await expect(staffPage.getByText(/closed/i).first()).toBeVisible({ timeout: 20_000 });

  await requesterPage.getByRole("button", { name: /my requests/i }).click();
  const row = requesterPage.locator(".request-item", { hasText: title });
  await expect(row).toBeVisible();
  await expect(row.getByText(/^closed$/i)).toBeVisible();

  await staffContext.close();
  await requesterContext.close();
});
