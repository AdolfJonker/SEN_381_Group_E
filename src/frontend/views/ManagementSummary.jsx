import { useEffect, useState } from "react";
import { fetchRequests, fetchSummary } from "../services/client.js";
import { CATEGORIES, STATUSES, STATUS_LABELS } from "../services/requestOptions.js";
import RequestTable from "./RequestTable.jsx";
import "../styles/views.css";

/** Management landing view (FR012, FR013). Summary and filtered list come from the server. */

export default function ManagementSummary({ onSelect }) {
  const [summary, setSummary] = useState(null);
  const [requests, setRequests] = useState([]);
  const [status, setStatus] = useState("");
  const [category, setCategory] = useState("");
  const [sortDir, setSortDir] = useState("desc");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  // Summary counts. Reloaded when the user presses Refresh.
  useEffect(() => {
    let ignore = false;
    fetchSummary()
      .then((data) => {
        if (!ignore) setSummary(data);
      })
      .catch((err) => {
        if (!ignore) setError(err.message);
      });
    return () => {
      ignore = true;
    };
  }, [reloadKey]);

  // Request list with server-side FR013 filters.
  useEffect(() => {
    let ignore = false;
    setLoading(true);
    fetchRequests({ status, category, sort: sortDir })
      .then((data) => {
        if (!ignore) setRequests(data);
      })
      .catch((err) => {
        if (!ignore) setError(err.message);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, [status, category, sortDir, reloadKey]);

  const total = summary
    ? summary.open + summary.assigned + summary.inProgress + summary.resolved + summary.closed
    : null;
  const hasFilter = Boolean(status || category);

  const mainCards = [
    { key: "Open", label: "Open", value: summary?.open, status: "Open" },
    { key: "Overdue", label: "Overdue", value: summary?.overdue, hint: "Not resolved after 7 days" },
    { key: "Resolved", label: "Resolved", value: summary?.resolved, status: "Resolved" },
    { key: "Closed", label: "Closed", value: summary?.closed, status: "Closed" },
  ];
  const workCards = [
    { key: "Assigned", label: "Assigned", value: summary?.assigned, status: "Assigned" },
    { key: "InProgress", label: "In Progress", value: summary?.inProgress, status: "InProgress" },
  ];

  function renderCard(card, small = false) {
    const active = card.status && card.status === status;
    const className = `summary-card tone-${card.key}${small ? " small" : ""}${active ? " active" : ""}`;
    const content = (
      <>
        <span className="summary-value">{card.value ?? "–"}</span>
        <span className="summary-label">{card.label}</span>
        {card.hint ? <span className="summary-hint">{card.hint}</span> : null}
      </>
    );
    if (!card.status) {
      return (
        <div key={card.key} className={className}>
          {content}
        </div>
      );
    }
    return (
      <button
        key={card.key}
        type="button"
        className={className}
        aria-pressed={active}
        title={active ? "Clear this filter" : `Show ${card.label} requests`}
        onClick={() => setStatus(active ? "" : card.status)}
      >
        {content}
      </button>
    );
  }

  return (
    <div className="view-stack">
      {error ? (
        <p className="error" role="alert">
          {error}
        </p>
      ) : null}

      <section className="card" aria-labelledby="summary-heading">
        <h2 id="summary-heading" className="view-title">Service activity summary</h2>
        <p className="muted view-sub">Counts across all requests. Click a status to filter the list.</p>
        <div className="summary-grid">{mainCards.map((c) => renderCard(c))}</div>
        <div className="summary-grid secondary">{workCards.map((c) => renderCard(c, true))}</div>
      </section>

      <section className="card" aria-labelledby="list-heading">
        <div className="view-header-row">
          <div>
            <h2 id="list-heading" className="view-title">Requests by category and status</h2>
            <p className="muted view-sub" aria-live="polite">
              {loading
                ? "Loading requests…"
                : `Showing ${requests.length}${total !== null ? ` of ${total}` : ""} requests${
                    hasFilter ? " (filtered)" : ""
                  }.`}
            </p>
          </div>
          <button type="button" className="btn-outline" onClick={() => setReloadKey((k) => k + 1)}>
            Refresh
          </button>
        </div>

        <div className="filters">
          <label>
            Category
            <select value={category} onChange={(e) => setCategory(e.target.value)}>
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
              className="btn-outline"
              disabled={!hasFilter}
              onClick={() => {
                setStatus("");
                setCategory("");
              }}
            >
              Clear filters
            </button>
          </div>
        </div>

        {loading ? null : (
          <RequestTable
            requests={requests}
            onSelect={onSelect}
            emptyText={hasFilter ? "No requests match these filters." : "There are no requests yet."}
          />
        )}
      </section>
    </div>
  );
}
