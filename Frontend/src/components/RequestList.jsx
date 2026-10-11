import { useMemo, useState } from 'react'
import { CATEGORIES, STATUSES, STATUS_LABELS } from '../data/constants'
import { applyFilters, sortByDate } from '../utils/requestFilters'
import RequestTable from './RequestTable'

/**
 * Staff work list (FR005, FR006) and Requester "My requests" (FR003).
 * Staff get search, status/category filters and date sort.
 */
export default function RequestList({
  title,
  subtitle,
  requests,
  onSelect,
  showFilters = false,
}) {
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [sortDir, setSortDir] = useState('desc')

  const visible = useMemo(() => {
    const term = search.trim().toLowerCase()
    const searched = term
      ? requests.filter(
          (r) =>
            r.id.toLowerCase().includes(term) ||
            r.title.toLowerCase().includes(term),
        )
      : requests
    const filtered = applyFilters(searched, {
      status: statusFilter,
      category: categoryFilter,
    })
    return sortByDate(filtered, sortDir)
  }, [requests, search, statusFilter, categoryFilter, sortDir])

  return (
    <section className="panel">
      <header className="panel-header">
        <h2>{title}</h2>
        {subtitle ? <p>{subtitle}</p> : null}
      </header>

      {showFilters ? (
        <div className="filters">
          <label>
            Search
            <input
              type="search"
              placeholder="ID or title"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </label>
          <label>
            Status
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All</option>
              {STATUSES.map((s) => (
                <option key={s} value={s}>
                  {STATUS_LABELS[s]}
                </option>
              ))}
            </select>
          </label>
          <label>
            Category
            <select
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
            >
              <option value="">All</option>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
          <label>
            Sort by date
            <select value={sortDir} onChange={(e) => setSortDir(e.target.value)}>
              <option value="desc">Newest first</option>
              <option value="asc">Oldest first</option>
            </select>
          </label>
        </div>
      ) : null}

      <RequestTable requests={visible} onSelect={onSelect} />
    </section>
  )
}
