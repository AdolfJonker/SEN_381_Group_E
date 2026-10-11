/**
 * FR013: filter requests by category and status.
 * The server filters first (query parameters). The UI applies the same rule
 * to what comes back, so the table always matches the chosen filters even
 * if the server ignores a parameter. An empty filter value means "All".
 */
export function applyFilters(requests, { status = '', category = '' } = {}) {
  return requests.filter(
    (r) =>
      (!status || r.status === status) &&
      (!category || r.category === category),
  )
}

/** Newest first by default; 'asc' gives oldest first. */
export function sortByDate(requests, direction = 'desc') {
  return [...requests].sort((a, b) => {
    const diff = new Date(a.createdAt) - new Date(b.createdAt)
    return direction === 'asc' ? diff : -diff
  })
}
