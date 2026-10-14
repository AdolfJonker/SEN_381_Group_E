/**
 * Role-based landing view (NFR003, FR005, FR012).
 * After login, each role opens on the screen for its job:
 *   Management -> management summary
 *   Staff      -> staff request list
 *   Requester  -> my requests
 * An unknown role gets no view, so the app shows "no access" instead of guessing.
 */
const LANDING_BY_ROLE = {
  Management: "management-summary",
  Staff: "staff-list",
  Requester: "my-requests",
};

export function landingViewFor(role) {
  return LANDING_BY_ROLE[role] ?? null;
}
