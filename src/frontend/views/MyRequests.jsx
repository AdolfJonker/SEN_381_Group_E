
 // MyRequests view (FR003 + FR004 / NFR001): requester's own requests with live status refresh
import { useCallback, useEffect, useRef, useState } from "react";
import { fetchMyRequests } from "../services/client.js";
import { detectStatusChanges } from "../services/statusChanges.js";
import "../styles/requests.css";

const STATUS_LABELS = {
    Open: "Open",
    Assigned: "Assigned",
    InProgress: "In progress",
    Resolved: "Resolved",
    Closed: "Closed",
};

const POLL_INTERVAL_MS = 4000;

function formatDate(value) {
    if (!value) {
        return "";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "";
    }

    return date.toLocaleDateString("en-ZA", {
        year: "numeric",
        month: "short",
        day: "numeric",
    });
}

export default function MyRequests({ onSubmitClick, onSelectRequest }) {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [lastChange, setLastChange] = useState(null);
    const previousRef = useRef([]);

    const load = useCallback(async (isPoll = false) => {
        if (!isPoll) {
            setLoading(true);
            setError("");
        }

        try {
            const data = await fetchMyRequests();
            const list = Array.isArray(data) ? data : [];
            const changes = detectStatusChanges(previousRef.current, list);

            if (changes.length > 0) {
                setLastChange(changes[changes.length - 1]);
            }

            previousRef.current = list;
            setRequests(list);
        } catch (err) {
            if (!isPoll) {
                setError(err.message || "Could not load your requests.");
            }
        } finally {
            if (!isPoll) {
                setLoading(false);
            }
        }
    }, []);

    // Initial load
    useEffect(() => {
        load(false);
    }, [load]);

    // Poll every 4 seconds (NFR001: status visible within 5 seconds)
    useEffect(() => {
        const id = setInterval(() => {
            load(true);
        }, POLL_INTERVAL_MS);

        return () => clearInterval(id);
    }, [load]);

    return (
        <div className="card">
            <h2 className="page-title">My requests</h2>
            <p className="muted">
                The requests you have submitted, newest first. Status updates automatically.
            </p>

            {lastChange && (
                <div className="alert alert-info" role="status">
                    Request #{lastChange.id} moved from{" "}
                    {STATUS_LABELS[lastChange.from] ?? lastChange.from} to{" "}
                    {STATUS_LABELS[lastChange.to] ?? lastChange.to}.
                </div>
            )}

            {loading && (
                <p className="muted" role="status">
                    Loading your requests...
                </p>
            )}

            {!loading && error && (
                <>
                    <div className="alert alert-error" role="alert">
                        {error}
                    </div>
                    <div className="request-list-actions">
                        <button
                            type="button"
                            className="btn-outline"
                            onClick={() => load(false)}
                        >
                            Try again
                        </button>
                    </div>
                </>
            )}

            {!loading && !error && requests.length === 0 && (
                <>
                    <p>You have not submitted any requests yet.</p>
                    {onSubmitClick && (
                        <div className="request-list-actions">
                            <button type="button" onClick={onSubmitClick}>
                                Submit a request
                            </button>
                        </div>
                    )}
                </>
            )}

            {!loading && !error && requests.length > 0 && (
                <ul className="request-list">
                    {requests.map((request) => {
                        const statusKey = String(request.status ?? "");
                        const label = STATUS_LABELS[statusKey] ?? statusKey;

                        return (
                            <li key={request.id} className="request-item">
                                <button
                                    type="button"
                                    className="request-item-button"
                                    onClick={() => onSelectRequest?.(request.id)}
                                    aria-label={`View details for request ${request.id}: ${request.title}`}
                                >
                                    <div className="request-item-head">
                                        <span className="request-id">
                                            Request #{request.id}
                                        </span>
                                        <span
                                            className={`status-badge status-${statusKey.toLowerCase()}`}
                                        >
                                            {label}
                                        </span>
                                    </div>

                                    <p className="request-title">{request.title}</p>

                                    <div className="request-meta">
                                        <span>{request.category}</span>
                                        <span>
                                            Submitted {formatDate(request.createdAt)}
                                        </span>
                                    </div>

                                    <span className="request-open-hint">
                                        View request details →
                                    </span>
                                </button>
                            </li>
                        );
                    })}
                </ul>
            )}
        </div>
    );
}
