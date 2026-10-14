/**
 * FR006 / FR013: search, filter and sort a request list.
 * An empty value means "All". Category and status combine with AND.
 */
export function applyFilters(requests, { status = "", category = "", search = "" } = {}) {
  const term = search.trim().toLowerCase();
  return requests.filter((r) => {
    if (status && r.status !== status) return false;
    if (category && r.category !== category) return false;
    if (term) {
      const text = `${r.id} ${r.title ?? ""}`.toLowerCase();
      if (!text.includes(term)) return false;
    }
    return true;
  });
}


export function sortByDate(requests, direction = "desc") {
  return [...requests].sort((a, b) => {
    const diff = new Date(a.createdAt) - new Date(b.createdAt);
    return direction === "asc" ? diff : -diff;
  });
}
