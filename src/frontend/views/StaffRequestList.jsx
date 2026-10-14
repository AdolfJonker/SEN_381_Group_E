import { useEffect, useMemo, useState } from "react";
import { fetchRequests } from "../services/client.js";
import { CATEGORIES, STATUSES, STATUS_LABELS } from "../services/requestOptions.js";
import { applyFilters, sortByDate } from "../services/requestFilters.js";
import RequestTable from "./RequestTable.jsx";
import "../styles/views.css";

/**
 * Staff landing view (FR005, FR006).
 * The server returns only the staff member's scope: their assigned work plus Open requests.
 * Staff can search by ID or title, filter by status and category, and sort by date.
 */
export default function StaffRequestList({ onSelect }) {
  const [requests, setRequests] = useState([]);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");
  const [category, setCategory] = useState("");
  const [sortDir, setSortDir] = useState("desc");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let ignore = false;
    fetchRequests({ status, category })
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
  }, [status, category, reloadKey]);

  const visible = useMemo(
    () => sortByDate(applyFilters(requests, { status, category, search }), sortDir),
    [requests, status, category, search, sortDir]
  );

  return (
    <section className="card" aria-labelledby="staff-heading">
      <div className="view-header-row">
        <div>
          <h2 id="staff-heading" className="view-title">Service requests</h2>
          <p className="muted view-sub">Your assigned work and Open requests waiting to be accepted.</p>
        </div>
        <button type="button" className="btn-outline" onClick={() => setReloadKey((k) => k + 1)}>
          Refresh
        </button>
      </div>

      {error ? (
        <p className="error" role="alert">
          {error}
        </p>
      ) : null}

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
          Sort by date
          <select value={sortDir} onChange={(e) => setSortDir(e.target.value)}>
            <option value="desc">Newest first</option>
            <option value="asc">Oldest first</option>
          </select>
        </label>
      </div>

      {loading ? (
        <p className="muted">Loading requests…</p>
      ) : (
        <RequestTable requests={visible} onSelect={onSelect} />
      )}
    </section>
  );
}
