// RequestDetail view: a requester's own request with its status history (FR002, NFR004).
import { useEffect, useState } from "react";
import { fetchRequest } from "../services/client.js";
import "../styles/requests.css";
import { CategoryIcon, Icon, StatusSteps } from "./Visuals.jsx";

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
        <section className="card detail-card" aria-labelledby="request-detail-heading">
            <div className="detail-top">
                <button type="button" className="btn-ghost btn-icon" onClick={onBack}>
                    <Icon name="back" />
                    <span>Back to My Requests</span>
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
                    <div className="detail-hero">
                        <CategoryIcon category={request.category} size="lg" />
                        <div className="detail-heading">
                            <span className="request-id">
                                Request #{request.id} · Submitted {formatDate(request.createdAt)}
                            </span>
                            <h2 id="request-detail-heading" className="page-title">
                                {request.title || "Not provided"}
                            </h2>
                        </div>
                        <span
                            className={`status-badge status-${String(
                                request.status ?? ""
                            ).toLowerCase()}`}
                        >
                            {displayStatus(request.status)}
                        </span>
                    </div>
                    <StatusSteps status={request.status} showLabels />

                    <div className="detail-grid">
                        <div className="detail-main">
                            <h3 className="section-title">Description</h3>
                            <p className="description-block">
                                {request.description || "No description provided."}
                            </p>

                            <section aria-labelledby="request-history-heading">
                                <h3 id="request-history-heading" className="section-title">
                                    Status history
                                </h3>

                                {request.history?.length ? (
                                    <ol className="timeline">
                                        {request.history.map((entry) => (
                                            <li key={entry.id}>
                                                <strong>
                                                    {entry.fromStatus
                                                        ? `${displayStatus(entry.fromStatus)} → `
                                                        : ""}
                                                    {displayStatus(entry.toStatus)}
                                                </strong>
                                                <div className="muted small">
                                                    {formatDate(entry.changedAt)}
                                                </div>
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
                        </div>

                        <aside className="detail-side">
                            <dl className="meta-grid">
                                <div>
                                    <dt>Category</dt>
                                    <dd>{request.category || "Not provided"}</dd>
                                </div>
                                <div>
                                    <dt>Status</dt>
                                    <dd>{displayStatus(request.status)}</dd>
                                </div>
                                <div>
                                    <dt>Submitted</dt>
                                    <dd>{formatDate(request.createdAt)}</dd>
                                </div>
                                <div>
                                    <dt>Last updated</dt>
                                    <dd>{formatDate(request.updatedAt)}</dd>
                                </div>
                            </dl>
                            <p className="muted small side-note">
                                Updates appear here as staff work on your request.
                            </p>
                        </aside>
                    </div>
                </>
            )}
        </section>
    );
}
