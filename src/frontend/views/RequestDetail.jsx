// RequestDetail view


import { useEffect, useState } from "react";
import { fetchRequest } from "../services/client.js";
import "../styles/requests.css";

const STATUS_LABELS = {
    Open: "Open",
    Assigned: "Assigned",
    InProgress: "In progress",
    Resolved: "Resolved",
    Closed: "Closed",
};

function formatDate(value) {
    if (!value) return "Not available";

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "Not available";

    return date.toLocaleString("en-ZA", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}

function displayStatus(status) {
    return STATUS_LABELS[status] ?? status ?? "Unknown";
}

export default function RequestDetail({ requestId, onBack }) {
    const [request, setRequest] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let cancelled = false;

        async function loadRequest() {
            setLoading(true);
            setError("");

            try {
                const data = await fetchRequest(requestId);
                if (!cancelled) setRequest(data);
            } catch (err) {
                if (!cancelled) {
                    setError(
                        err.message || "Could not load this request. Please try again."
                    );
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        loadRequest();

        return () => {
            cancelled = true;
        };
    }, [requestId]);

    return (
        <section className="card" aria-labelledby="request-detail-heading">
            <div className="request-list-actions">
                <button type="button" className="btn-outline" onClick={onBack}>
                    Back to My Requests
                </button>
            </div>

            {loading && (
                <p className="muted" role="status">
                    Loading request details...
                </p>
            )}

            {!loading && error && (
                <div className="alert alert-error" role="alert">
                    {error}
                </div>
            )}

            {!loading && !error && request && (
                <>
                    <div className="request-detail-heading">
                        <div>
                            <h2 id="request-detail-heading" className="page-title">
                                Request #{request.id}
                            </h2>
                            <p className="muted">
                                Submitted {formatDate(request.createdAt)}
                            </p>
                        </div>

                        <span
                            className={`status-badge status-${String(
                                request.status ?? ""
                            ).toLowerCase()}`}
                        >
                            {displayStatus(request.status)}
                        </span>
                    </div>

                    <dl className="request-detail-fields">
                        <div>
                            <dt>Title</dt>
                            <dd>{request.title || "Not provided"}</dd>
                        </div>
                        <div>
                            <dt>Category</dt>
                            <dd>{request.category || "Not provided"}</dd>
                        </div>
                        <div>
                            <dt>Description</dt>
                            <dd className="request-description">
                                {request.description || "No description provided."}
                            </dd>
                        </div>
                        <div>
                            <dt>Last updated</dt>
                            <dd>{formatDate(request.updatedAt)}</dd>
                        </div>
                    </dl>

                    <section aria-labelledby="request-history-heading">
                        <h3 id="request-history-heading">Status history</h3>

                        {request.history?.length ? (
                            <ol className="request-history">
                                {request.history.map((entry) => (
                                    <li key={entry.id}>
                                        <p className="request-history-status">
                                            {entry.fromStatus
                                                ? `${displayStatus(entry.fromStatus)} → `
                                                : ""}
                                            {displayStatus(entry.toStatus)}
                                        </p>
                                        <p className="muted">
                                            {formatDate(entry.changedAt)}
                                        </p>
                                        {entry.note && <p>{entry.note}</p>}
                                    </li>
                                ))}
                            </ol>
                        ) : (
                            <p className="muted">
                                No status history is available yet.
                            </p>
                        )}
                    </section>
                </>
            )}
        </section>
    );
}
