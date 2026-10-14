// Run with: npm test   (Node's built-in test runner, no extra packages)
import assert from "node:assert/strict";
import { describe, it } from "node:test";
import { landingViewFor } from "./roleLanding.js";

// TC-UI-01 (NFR003, FR005, FR012): each role lands on its own view; unknown roles fail closed.
describe("landingViewFor", () => {
  it("sends Management to the management summary", () => {
    assert.equal(landingViewFor("Management"), "management-summary");
  });

  it("sends Staff to the staff list and Requester to my requests", () => {
    assert.equal(landingViewFor("Staff"), "staff-list");
    assert.equal(landingViewFor("Requester"), "my-requests");
  });

  it("gives no view to an unknown or missing role", () => {
    assert.equal(landingViewFor("Admin"), null);
    assert.equal(landingViewFor(undefined), null);
  });
});
