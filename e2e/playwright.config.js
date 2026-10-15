import { defineConfig } from "@playwright/test";

const baseURL = process.env.STAGING_BASE_URL || process.env.BASE_URL || "http://localhost:8080";

export default defineConfig({
  testDir: "./tests",
  timeout: 60_000,
  expect: { timeout: 15_000 },
  fullyParallel: false,
  retries: process.env.CI ? 1 : 0,
  reporter: [["list"], ["html", { open: "never", outputFolder: "playwright-report" }]],
  use: {
    baseURL,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
});
