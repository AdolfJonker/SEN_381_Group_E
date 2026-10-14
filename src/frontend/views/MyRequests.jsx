// MyRequests view (FR003): the signed in requester's own requests

import { useCallback, useEffect, useState } from "react";
import { fetchMyRequests } from "../services/client.js";
import "../styles/requests.css";

const STATUS_LABELS = {
    Open: "Open",
    Assigned: "Assigned",
    InProgress: "In progress",
    Resolved: "Resolved",
    Closed: "Closed",
};

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

export default function MyRequests({ onSubmitClick }) {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const load = useCallback(async () => {
        setLoading(true);
        setError("");
        try {
            const data = await fetchMyRequests();
            setRequests(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message || "Could not load your requests.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        load();
    }, [load]);

    return (
        <div className="card">
            <h2 className="page-title">My requests</h2>
            <p className="muted">The requests you have submitted, newest first.</p>

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
                        <button type="button" className="btn-outline" onClick={load}>
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
                                <div className="request-item-head">
                                    <span className="request-id">Request #{request.id}</span>
                                    <span className={`status-badge status-${statusKey.toLowerCase()}`}>
                                        {label}
                                    </span>
                                </div>
                                <p className="request-title">{request.title}</p>
                                <div className="request-meta">
                                    <span>{request.category}</span>
                                    <span>Submitted {formatDate(request.createdAt)}</span>
                                </div>
                            </li>
                        );
                    })}
                </ul>
            )}
        </div>
    );
}
