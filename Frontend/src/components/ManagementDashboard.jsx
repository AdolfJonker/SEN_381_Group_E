import { useEffect, useMemo, useState } from 'react'
import { api } from '../api/client'
import { CATEGORIES, STATUSES, STATUS_LABELS } from '../data/constants'
import { applyFilters, sortByDate } from '../utils/requestFilters'
import ManagementSummary from './ManagementSummary'
import RequestTable from './RequestTable'

/**
 * Management landing view.
 *   FR012: summary counts (GET /api/summary, Management only).
 *   FR013: filter requests by category and status (GET /api/requests?status=&category=).
 */
export default function ManagementDashboard({ onSelect, onError }) {
  const [summary, setSummary] = useState(null)
  const [requests, setRequests] = useState([])
  const [status, setStatus] = useState('')
  const [category, setCategory] = useState('')
  const [sortDir, setSortDir] = useState('desc')
  const [loading, setLoading] = useState(true)
  const [reloadKey, setReloadKey] = useState(0)

  // FR012 counts. Reloaded when the user presses Refresh.
  useEffect(() => {
    let ignore = false
    api
      .getSummary()
      .then((data) => {
        if (!ignore) setSummary(data)
      })
      .catch((err) => {
        if (!ignore) onError(err.message)
      })
    return () => {
      ignore = true
    }
  }, [reloadKey, onError])

  // Reload the list whenever a filter changes. "ignore" stops an older,
  // slower response from overwriting a newer one.
  useEffect(() => {
    let ignore = false
    async function loadRequests() {
      setLoading(true)
      try {
        const data = await api.listRequests({ status, category })
        if (!ignore) setRequests(data)
      } catch (err) {
        if (!ignore) onError(err.message)
      } finally {
        if (!ignore) setLoading(false)
      }
    }
    loadRequests()
    return () => {
      ignore = true
    }
  }, [status, category, reloadKey, onError])

  const visible = useMemo(
    () => sortByDate(applyFilters(requests, { status, category }), sortDir),
    [requests, status, category, sortDir],
  )

  const total = summary
    ? summary.open +
      summary.assigned +
      summary.inProgress +
      summary.resolved +
      summary.closed
    : null
  const hasFilter = Boolean(status || category)

  function clearFilters() {
    setStatus('')
    setCategory('')
  }

  return (
    <>
      <ManagementSummary
        summary={summary}
        activeStatus={status}
        onPickStatus={setStatus}
      />

      <section className="panel" aria-labelledby="mgmt-list-heading">
        <header className="panel-header panel-header-row">
          <div>
            <h2 id="mgmt-list-heading">Requests by category and status</h2>
            <p aria-live="polite">
              {loading
                ? 'Loading requests…'
                : `Showing ${visible.length}${
                    total !== null ? ` of ${total}` : ''
                  } requests${hasFilter ? ' (filtered)' : ''}.`}
            </p>
          </div>
          <button
            type="button"
            className="btn ghost"
            onClick={() => setReloadKey((k) => k + 1)}
          >
            Refresh
          </button>
        </header>

        <div className="filters">
          <label>
            Category
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
            >
              <option value="">All categories</option>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </label>
          <label>
            Status
            <select value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="">All statuses</option>
              {STATUSES.map((s) => (
                <option key={s} value={s}>
                  {STATUS_LABELS[s]}
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
          <div className="filter-actions">
            <button
              type="button"
              className="btn ghost"
              onClick={clearFilters}
              disabled={!hasFilter}
            >
              Clear filters
            </button>
          </div>
        </div>

        {loading ? null : (
          <RequestTable
            requests={visible}
            onSelect={onSelect}
            emptyText={
              hasFilter
                ? 'No requests match these filters.'
                : 'There are no requests yet.'
            }
          />
        )}
      </section>
    </>
  )
}
