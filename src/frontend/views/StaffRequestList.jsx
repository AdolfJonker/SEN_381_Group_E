import { useEffect, useMemo, useState } from "react";
import { fetchMyRequests } from "../services/client.js";
import { formatStatus, formatWhen, STATUS_OPTIONS } from "../services/format.js";
import { REQUEST_CATEGORIES } from "../services/validation.js";

export default function StaffRequestList({ onOpen }) {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("");
    const [category, setCategory] = useState("");
    const [sort, setSort] = useState("newest");

    useEffect(() => {
        let active = true;
        setLoading(true);
        setLoadError("");
        fetchMyRequests()
            .then((data) => {
                if (active) {
                    setRequests(Array.isArray(data) ? data : []);
                }
            })
            .catch((error) => {
                if (active) {
                    setLoadError(error.message || "Could not load requests.");
                }
            })
            .finally(() => {
                if (active) {
                    setLoading(false);
                }
            });
        return () => {
            active = false;
        };
    }, []);

    const visible = useMemo(() => {
        const query = search.trim().toLowerCase();
        const filtered = requests.filter((request) => {
            if (status && request.status !== status) {
                return false;
            }
            if (category && request.category !== category) {
                return false;
            }
            if (!query) {
                return true;
            }
            const haystack = [
                request.id,
                request.title,
                request.category,
                request.assignedToName,
                formatStatus(request.status),
            ]
                .filter(Boolean)
                .join(" ")
                .toLowerCase();
            return haystack.includes(query);
        });

        filtered.sort((left, right) => {
            const leftTime = new Date(left.createdAt).getTime();
            const rightTime = new Date(right.createdAt).getTime();
            return sort === "oldest" ? leftTime - rightTime : rightTime - leftTime;
        });
        return filtered;
    }, [requests, search, status, category, sort]);

    return (
        <div className="card">
            <h2 className="page-title">Service requests</h2>
            <p className="muted">
                Open requests you can accept, and requests already assigned to you.
            </p>

            {loadError && (
                <div className="alert alert-error" role="alert">
                    {loadError}
                </div>
            )}

            <div className="filter-grid">
                <div className="field">
                    <label htmlFor="request-search">Search</label>
                    <input
                        id="request-search"
                        type="search"
                        value={search}
                        placeholder="Title, category or request number"
                        onChange={(event) => setSearch(event.target.value)}
                    />
                </div>
                <div className="field">
                    <label htmlFor="status-filter">Status</label>
                    <select
                        id="status-filter"
                        value={status}
                        onChange={(event) => setStatus(event.target.value)}
                    >
                        <option value="">All statuses</option>
                        {STATUS_OPTIONS.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="field">
                    <label htmlFor="category-filter">Category</label>
                    <select
                        id="category-filter"
                        value={category}
                        onChange={(event) => setCategory(event.target.value)}
                    >
                        <option value="">All categories</option>
                        {REQUEST_CATEGORIES.map((item) => (
                            <option key={item} value={item}>
                                {item}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="field">
                    <label htmlFor="date-sort">Sort by date</label>
                    <select
                        id="date-sort"
                        value={sort}
                        onChange={(event) => setSort(event.target.value)}
                    >
                        <option value="newest">Newest first</option>
                        <option value="oldest">Oldest first</option>
                    </select>
                </div>
            </div>

            {loading ? (
                <p className="muted">Loading requests…</p>
            ) : (
                <>
                    <p className="muted result-count">
                        {visible.length} {visible.length === 1 ? "request" : "requests"}
                    </p>
                    {visible.length === 0 ? (
                        <p>
                            {requests.length === 0
                                ? "No requests are waiting. New requests appear here when a requester submits one."
                                : "No requests match these filters."}
                        </p>
                    ) : (
                        <div className="request-list">
                            {visible.map((request) => (
                                <button
                                    key={request.id}
                                    type="button"
                                    className="request-row"
                                    onClick={() => onOpen?.(request.id)}
                                >
                                    <span className="request-row-top">
                                        <span className="request-id">#{request.id}</span>
                                        <span className={`status-pill status-${String(request.status || "").toLowerCase()}`}>
                                            {formatStatus(request.status)}
                                        </span>
                                    </span>
                                    <span className="request-title">{request.title}</span>
                                    <span className="muted">
                                        {request.category}
                                        {" · "}
                                        {formatWhen(request.createdAt)}
                                        {" · "}
                                        {request.assignedToName || "Unassigned"}
                                    </span>
                                </button>
                            ))}
                        </div>
                    )}
                </>
            )}
        </div>
    );
}
