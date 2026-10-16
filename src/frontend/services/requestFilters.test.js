// Run with: npm test   (Node's built-in test runner, no extra packages)
import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { applyFilters, sortByDate } from "./requestFilters.js";

const rows = [
  { id: 1, title: "Wi-Fi down", category: "IT Support", status: "Open", createdAt: "2026-10-01T08:00:00Z" },
  { id: 2, title: "VPN access", category: "IT Support", status: "Closed", createdAt: "2026-10-03T08:00:00Z" },
  { id: 3, title: "Leaking tap", category: "Maintenance", status: "Open", createdAt: "2026-10-02T08:00:00Z" },
  { id: 4, title: "Door hinge", category: "Maintenance", status: "Resolved", createdAt: "2026-10-04T08:00:00Z" },
];
const ids = (list) => list.map((r) => r.id);

// TC-D-07 (FR013, FR006): equivalence partitions for the filters.
describe("applyFilters", () => {
  it("returns everything when no filter is set", () => {
    assert.deepEqual(ids(applyFilters(rows)), [1, 2, 3, 4]);
  });

  it("filters by category only", () => {
    assert.deepEqual(ids(applyFilters(rows, { category: "Maintenance" })), [3, 4]);
  });

  it("filters by status only", () => {
    assert.deepEqual(ids(applyFilters(rows, { status: "Open" })), [1, 3]);
  });

  it("combines category and status with AND", () => {
    assert.deepEqual(ids(applyFilters(rows, { category: "IT Support", status: "Open" })), [1]);
  });

  it("returns an empty list when nothing matches", () => {
    assert.deepEqual(applyFilters(rows, { category: "Lost Property" }), []);
  });

  it("searches by id or title, ignoring case", () => {
    assert.deepEqual(ids(applyFilters(rows, { search: "vpn" })), [2]);
    assert.deepEqual(ids(applyFilters(rows, { search: "4" })), [4]);
  });
});

describe("sortByDate", () => {
  it("sorts newest first by default and oldest first on request", () => {
    assert.deepEqual(ids(sortByDate(rows)), [4, 2, 3, 1]);
    assert.deepEqual(ids(sortByDate(rows, "asc")), [1, 3, 2, 4]);
  });

  it("does not change the original array", () => {
    sortByDate(rows, "asc");
    assert.deepEqual(ids(rows), [1, 2, 3, 4]);
  });
});
