/**
 * Performance scenario: timed GET /api/v1/requests (staff list).
 * Usage:
 *   STAGING_BASE_URL=https://… node scripts/perf-list.mjs
 *   (optional) PERF_ITERATIONS=20 PERF_TARGET_MS=2000
 *
 * Writes docs/testing/performance-list-evidence.md
 */

import { writeFileSync, mkdirSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const base = (process.env.STAGING_BASE_URL || process.env.BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const iterations = Number(process.env.PERF_ITERATIONS || 10);
const targetMs = process.env.PERF_TARGET_MS ? Number(process.env.PERF_TARGET_MS) : null;
const email = process.env.PERF_EMAIL || "staff@civicconnect.local";
const password = process.env.PERF_PASSWORD || "Password123!";

async function main() {
  const loginRes = await fetch(`${base}/api/v1/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  if (!loginRes.ok) {
    throw new Error(`Login failed: ${loginRes.status} ${await loginRes.text()}`);
  }
  const cookie = loginRes.headers.getSetCookie?.()?.join("; ")
    || loginRes.headers.get("set-cookie")
    || "";

  const durations = [];
  let errors = 0;
  let lastCount = 0;

  for (let i = 0; i < iterations; i++) {
    const started = performance.now();
    try {
      const res = await fetch(`${base}/api/v1/requests`, {
        headers: cookie ? { Cookie: cookie } : {},
      });
      const elapsed = performance.now() - started;
      if (!res.ok) {
        errors += 1;
      } else {
        const body = await res.json();
        lastCount = Array.isArray(body) ? body.length : 0;
        durations.push(elapsed);
      }
    } catch {
      errors += 1;
    }
  }

  durations.sort((a, b) => a - b);
  const avg = durations.length ? durations.reduce((a, b) => a + b, 0) / durations.length : null;
  const p95 = durations.length ? durations[Math.min(durations.length - 1, Math.floor(durations.length * 0.95))] : null;
  const errorRate = errors / iterations;

  const outDir = join(dirname(fileURLToPath(import.meta.url)), "..", "docs", "testing");
  mkdirSync(outDir, { recursive: true });
  const outPath = join(outDir, "performance-list-evidence.md");

  const md = `# Performance evidence — request list (Adolf M3)

| Field | Value |
|---|---|
| Basis | NFR-style list responsiveness for FR005/FR006 staff list |
| Why selected | List query is the hot path behind indexes on \`submitted_by_id\` / \`assigned_to_id\` |
| Technique | Timed HTTP GET against staging/dev API |
| Environment | \`${base}\` |
| Data size (rows returned, last call) | ${lastCount} |
| Workload | ${iterations} sequential GETs as \`${email}\` |
| Duration (wall, successful calls) | avg ${avg?.toFixed(1) ?? "n/a"} ms; p95 ${p95?.toFixed(1) ?? "n/a"} ms |
| Response time target | ${targetMs == null ? "None justified for M3 free-tier staging — recorded for comparison only" : `${targetMs} ms`} |
| Error rate | ${(errorRate * 100).toFixed(1)}% (${errors}/${iterations}) |
| Status | ${errors === 0 ? "Pass (completed without HTTP errors)" : "Completed with errors — investigate before release"} |
| Evidence | Output of \`scripts/perf-list.mjs\` on ${new Date().toISOString()} |
| Interpretation | ${targetMs == null
    ? "No contractual SLA exists; this run only characterises free-tier staging under a light sequential load."
    : avg != null && avg <= targetMs
      ? `Average under the ${targetMs} ms comparison target.`
      : `Average above the ${targetMs} ms comparison target — do not claim production readiness.`} |
| What this does **not** prove | Production-scale concurrent municipal users, cold-start after free-tier sleep, or multi-region latency. |

## Raw successful durations (ms)

${durations.map((d) => d.toFixed(1)).join(", ") || "(none)"}
`;

  writeFileSync(outPath, md, "utf8");
  console.log(md);
  console.log(`Wrote ${outPath}`);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
