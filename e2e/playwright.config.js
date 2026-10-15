import { defineConfig } from "@playwright/test";

// Prefer BASE_URL for local Vite (5173). Use STAGING_BASE_URL only for Render/public runs.
const baseURL = process.env.BASE_URL || process.env.STAGING_BASE_URL || "http://127.0.0.1:5173";

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
