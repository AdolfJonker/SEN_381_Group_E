import { useMemo, useState } from 'react'
import { CATEGORIES, STATUSES, STATUS_LABELS } from '../data/constants'
import StatusBadge from './StatusBadge'

function formatDate(iso) {
  return new Date(iso).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

export default function RequestList({
  title,
  subtitle,
  requests,
  onSelect,
  showFilters = false,
}) {
  const [statusFilter, setStatusFilter] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [sortDir, setSortDir] = useState('desc')

  const visible = useMemo(() => {
    let list = [...requests]
    if (statusFilter) list = list.filter((r) => r.status === statusFilter)
    if (categoryFilter) list = list.filter((r) => r.category === categoryFilter)
    list.sort((a, b) => {
      const diff = new Date(a.createdAt) - new Date(b.createdAt)
      return sortDir === 'asc' ? diff : -diff
    })
    return list
  }, [requests, statusFilter, categoryFilter, sortDir])

  return (
    <section className="panel">
      <header className="panel-header">
        <h2>{title}</h2>
        {subtitle ? <p>{subtitle}</p> : null}
      </header>

      {showFilters ? (
        <div className="filters">
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
            <select
              value={sortDir}
              onChange={(e) => setSortDir(e.target.value)}
            >
              <option value="desc">Newest first</option>
              <option value="asc">Oldest first</option>
            </select>
          </label>
        </div>
      ) : null}

      {visible.length === 0 ? (
        <p className="empty-state">No requests match this view.</p>
      ) : (
        <div className="table-wrap">
          <table className="request-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Category</th>
                <th>Submitted</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((req) => (
                <tr key={req.id}>
                  <td>
                    <button
                      type="button"
                      className="linkish"
                      onClick={() => onSelect(req.id)}
                    >
                      {req.id}
                    </button>
                  </td>
                  <td>{req.title}</td>
                  <td>{req.category}</td>
                  <td>{formatDate(req.createdAt)}</td>
                  <td>
                    <StatusBadge status={req.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
