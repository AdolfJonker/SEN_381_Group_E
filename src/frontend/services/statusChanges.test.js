import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { detectStatusChanges } from "./statusChanges.js";

describe("detectStatusChanges", () => {
    it("returns empty when both lists are empty", () => {
        assert.deepEqual(detectStatusChanges([], []), []);
    });

    it("returns empty when statuses are unchanged", () => {
        const list = [
            { id: 1, status: "Open", title: "A" },
            { id: 2, status: "Assigned", title: "B" },
        ];
        assert.deepEqual(detectStatusChanges(list, list), []);
    });

    it("detects a single status change", () => {
        const previous = [{ id: 1, status: "Open", title: "Broken tap" }];
        const current = [{ id: 1, status: "InProgress", title: "Broken tap" }];
        const changes = detectStatusChanges(previous, current);
        assert.equal(changes.length, 1);
        assert.equal(changes[0].id, 1);
        assert.equal(changes[0].from, "Open");
        assert.equal(changes[0].to, "InProgress");
    });

    it("detects multiple status changes", () => {
        const previous = [
            { id: 1, status: "Open", title: "A" },
            { id: 2, status: "Assigned", title: "B" },
        ];
        const current = [
            { id: 1, status: "Assigned", title: "A" },
            { id: 2, status: "Resolved", title: "B" },
        ];
        const changes = detectStatusChanges(previous, current);
        assert.equal(changes.length, 2);
    });

    it("ignores new requests that have no previous status", () => {
        const previous = [{ id: 1, status: "Open", title: "A" }];
        const current = [
            { id: 1, status: "Open", title: "A" },
            { id: 2, status: "Open", title: "B" },
        ];
        assert.deepEqual(detectStatusChanges(previous, current), []);
    });

    it("ignores removed requests", () => {
        const previous = [
            { id: 1, status: "Open", title: "A" },
            { id: 2, status: "Closed", title: "B" },
        ];
        const current = [{ id: 1, status: "Open", title: "A" }];
        assert.deepEqual(detectStatusChanges(previous, current), []);
    });
});