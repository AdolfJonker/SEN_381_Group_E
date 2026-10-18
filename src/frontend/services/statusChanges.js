/**
 * Compare two request lists and return the ones whose status changed.
 * Pure function so it can be unit-tested without a browser.
 */
export function detectStatusChanges(previous = [], current = []) {
    const prevById = new Map(previous.map((r) => [String(r.id), r]));
    const changes = [];

    for (const req of current) {
        const id = String(req.id);
        const old = prevById.get(id);
        if (old && String(old.status) !== String(req.status)) {
            changes.push({
                id: req.id,
                from: old.status,
                to: req.status,
                title: req.title,
            });
        }
    }

    return changes;
}