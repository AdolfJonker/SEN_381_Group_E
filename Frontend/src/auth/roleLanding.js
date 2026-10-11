import { ROLES } from '../data/constants.js'

/**
 * Role-based landing view (NFR003, FR005, FR012).
 * After login, each role opens on the screen that matches its job:
 *   Management -> summary dashboard (counts + filters)
 *   Staff      -> request work list
 *   Requester  -> "My requests"
 * An unknown role gets no landing view, so the UI refuses to continue
 * instead of guessing (fail closed).
 */
const LANDING_BY_ROLE = {
  [ROLES.MANAGEMENT]: 'summary',
  [ROLES.STAFF]: 'list',
  [ROLES.REQUESTER]: 'list',
}

export function landingViewFor(role) {
  return LANDING_BY_ROLE[role] ?? null
}

/** Views each role may open. Used to hide navigation the role cannot use. */
const VIEWS_BY_ROLE = {
  [ROLES.MANAGEMENT]: ['summary', 'detail'],
  [ROLES.STAFF]: ['list', 'detail'],
  [ROLES.REQUESTER]: ['list', 'submit', 'detail'],
}

export function canOpenView(role, view) {
  return (VIEWS_BY_ROLE[role] ?? []).includes(view)
}
