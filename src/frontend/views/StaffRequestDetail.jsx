import { useEffect, useState } from "react";
import {
    acceptRequest,
    addRequestComment,
    fetchRequest,
    updateRequestStatus,
} from "../services/client.js";
import { formatStatus, formatWhen, nextStatus, statusActionLabel } from "../services/format.js";
import { COMMENT_MAX, validateComment } from "../services/validation.js";
import { CategoryIcon, Icon, StatusSteps } from "./Visuals.jsx";

export default function StaffRequestDetail({ requestId, currentUserId, onBack }) {
    const [request, setRequest] = useState(null);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");
    const [actionError, setActionError] = useState("");
    const [actionMessage, setActionMessage] = useState("");
    const [working, setWorking] = useState("");
    const [comment, setComment] = useState("");
    const [commentError, setCommentError] = useState("");

    useEffect(() => {
        let active = true;

        async function load() {
            setLoading(true);
            setLoadError("");
            try {
                const data = await fetchRequest(requestId);
                if (active) {
                    setRequest(data);
                }
            } catch (error) {
                if (active) {
                    setLoadError(error.message || "Could not load this request.");
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        }

        if (requestId) {
            load();
        }
        return () => {
            active = false;
        };
    }, [requestId]);

    async function runAction(action, successMessage) {
        setActionError("");
        setActionMessage("");
        setWorking("status");
        try {
            const updated = await action();
            setRequest(updated);
            setActionMessage(successMessage);
        } catch (error) {
            const message = error.message || "Could not update this request.";
            setActionError(message);
            if (message.includes("Reload")) {
                try {
                    setRequest(await fetchRequest(requestId));
                } catch {
                    // Keep the error already shown.
                }
            }
        } finally {
            setWorking("");
        }
    }

    async function handleStatusAction() {
        if (!request) {
            return;
        }
        if (request.status === "Open") {
            await runAction(
                () => acceptRequest(request.id, request.version),
                "You accepted this request. It is now assigned to you."
            );
            return;
        }
        const status = nextStatus(request.status);
        if (!status) {
            return;
        }
        await runAction(
            async () => {
                await updateRequestStatus(request.id, status, request.version);
                return fetchRequest(requestId);
            },
            `Status updated to ${formatStatus(status)}.`
        );
    }

    async function handleComment(event) {
        event.preventDefault();
        setActionError("");
        setActionMessage("");
        const result = validateComment(comment);
        setCommentError(result.errors.content || "");
        if (!result.isValid || !request) {
            return;
        }

        setWorking("comment");
        try {
            const updated = await addRequestComment(request.id, result.values.content);
            setRequest(updated);
            setComment("");
            setActionMessage("Comment saved.");
        } catch (error) {
            setActionError(error.message || "Could not save the comment.");
        } finally {
            setWorking("");
        }
    }

    const upcoming = request ? nextStatus(request.status) : null;
    const isAssignee = request?.assignedToId != null && request.assignedToId === currentUserId;
    const canMoveStatus = request && upcoming && (request.status === "Open" || isAssignee);

    return (
        <div className="card detail-card">
            <div className="detail-top">
                <button type="button" className="btn-ghost btn-icon" onClick={onBack}>
                    <Icon name="back" />
                    <span>Back to requests</span>
                </button>
            </div>

            {loading && <p className="muted">Loading request…</p>}
            {loadError && (
                <div className="alert alert-error" role="alert">
                    {loadError}
                </div>
            )}

            {request && (
                <>
                    <div className="detail-hero">
                        <CategoryIcon category={request.category} size="lg" />
                        <div className="detail-heading">
                            <span className="request-id">Request #{request.id}</span>
                            <h2 className="page-title">{request.title}</h2>
                        </div>
                        <span className={`status-badge status-${String(request.status || "").toLowerCase()}`}>
                            {formatStatus(request.status)}
                        </span>
                    </div>
                    <StatusSteps status={request.status} showLabels />

                    {actionMessage && (
                        <div className="alert alert-success" role="status">
                            {actionMessage}
                        </div>
                    )}
                    {actionError && (
                        <div className="alert alert-error" role="alert">
                            {actionError}
                        </div>
                    )}


                    <div className="detail-grid">
                    <div className="detail-main">
                    <h3 className="section-title">Description</h3>
                    <p className="description-block">{request.description}</p>

                    <div className="next-step">
                    <h3 className="section-title">Next step</h3>
                    {request.status === "Closed" ? (
                        <p className="muted">This request is closed. No further status change is available.</p>
                    ) : canMoveStatus ? (
                        <>
                            <p className="muted">
                                {request.status === "Open"
                                    ? "Accepting assigns this request to you and moves it to Assigned. Later steps stay with you."
                                    : `The only allowed next status is ${formatStatus(upcoming)}.`}
                            </p>
                            <div className="form-actions">
                                <button type="button" onClick={handleStatusAction} disabled={Boolean(working)}>
                                    {working === "status" ? "Saving…" : statusActionLabel(request.status)}
                                </button>
                            </div>
                        </>
                    ) : (
                        <p className="muted">
                            Only the staff member assigned to this request can move it forward.
                        </p>
                    )}
                    </div>

                    <h3 className="section-title">Comments</h3>
                    {request.comments?.length ? (
                        <ul className="comment-list">
                            {request.comments.map((entry) => (
                                <li key={entry.id}>
                                    <div className="muted small">
                                        {entry.authorName}
                                        {" · "}
                                        {formatWhen(entry.createdAt)}
                                    </div>
                                    <p>{entry.content}</p>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="muted">No comments yet.</p>
                    )}

                    <form onSubmit={handleComment} noValidate>
                        <div className="field">
                            <label htmlFor="comment">Add a comment</label>
                            <textarea
                                id="comment"
                                name="comment"
                                rows={4}
                                value={comment}
                                maxLength={COMMENT_MAX}
                                onChange={(event) => setComment(event.target.value)}
                                disabled={Boolean(working)}
                                aria-invalid={Boolean(commentError)}
                            />
                            <div className="field-hint">
                                <span>Visible in this request’s history</span>
                                <span>
                                    {comment.trim().length}/{COMMENT_MAX}
                                </span>
                            </div>
                            {commentError && <p className="error">{commentError}</p>}
                        </div>
                        <div className="form-actions">
                            <button type="submit" className="btn-outline" disabled={Boolean(working)}>
                                {working === "comment" ? "Saving…" : "Save comment"}
                            </button>
                        </div>
                    </form>
                    </div>
                    <aside className="detail-side">
                    <dl className="meta-grid">
                        <div>
                            <dt>Category</dt>
                            <dd>{request.category}</dd>
                        </div>
                        <div>
                            <dt>Submitted</dt>
                            <dd>{formatWhen(request.createdAt)}</dd>
                        </div>
                        <div>
                            <dt>Last updated</dt>
                            <dd>{formatWhen(request.updatedAt)}</dd>
                        </div>
                        <div>
                            <dt>Submitted by</dt>
                            <dd>{request.submittedByName}</dd>
                        </div>
                        <div>
                            <dt>Assigned to</dt>
                            <dd>{request.assignedToName || "Unassigned"}</dd>
                        </div>
                    </dl>
                    <h3 className="section-title">Status history</h3>
                    {request.history?.length ? (
                        <ol className="timeline">
                            {request.history.map((entry) => (
                                <li key={entry.id}>
                                    <strong>
                                        {entry.fromStatus
                                            ? `${formatStatus(entry.fromStatus)} → ${formatStatus(entry.toStatus)}`
                                            : "Submitted"}
                                    </strong>
                                    <div className="muted small">
                                        {entry.changedByName}
                                        {" · "}
                                        {formatWhen(entry.changedAt)}
                                    </div>
                                    {entry.note && <p>{entry.note}</p>}
                                </li>
                            ))}
                        </ol>
                    ) : (
                        <p className="muted">No status changes yet.</p>
                    )}

                    </aside>
                    </div>
                </>
            )}
        </div>
    );
}
