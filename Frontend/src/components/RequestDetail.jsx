import { useState } from 'react'
import { ROLES, TRANSITIONS, STATUS_LABELS } from '../data/constants'
import { userById } from '../data/mockData'
import StatusBadge from './StatusBadge'

function formatDateTime(iso) {
  return new Date(iso).toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export default function RequestDetail({
  request,
  currentUser,
  onBack,
  onAssign,
  onTransition,
  onAddComment,
}) {
  const [comment, setComment] = useState('')
  const [note, setNote] = useState('')
  const submitter = userById(request.submittedById)
  const assignee = request.assignedToId
    ? userById(request.assignedToId)
    : null
  const nextStatuses = TRANSITIONS[request.status] ?? []
  const isStaff = currentUser.role === ROLES.STAFF
  const canAssign =
    isStaff && request.status === 'Open' && !request.assignedToId
  const canTransition = isStaff && nextStatuses.length > 0

  function handleComment(e) {
    e.preventDefault()
    if (!comment.trim()) return
    onAddComment(request.id, comment.trim())
    setComment('')
  }

  return (
    <section className="panel detail-panel">
      <button type="button" className="btn ghost back-btn" onClick={onBack}>
        ← Back to list
      </button>

      <header className="panel-header detail-header">
        <div>
          <p className="eyebrow">{request.id}</p>
          <h2>{request.title}</h2>
          <p>{request.description}</p>
        </div>
        <StatusBadge status={request.status} />
      </header>

      <dl className="meta-grid">
        <div>
          <dt>Category</dt>
          <dd>{request.category}</dd>
        </div>
        <div>
          <dt>Submitted</dt>
          <dd>{formatDateTime(request.createdAt)}</dd>
        </div>
        <div>
          <dt>Requester</dt>
          <dd>{submitter?.name ?? '—'}</dd>
        </div>
        <div>
          <dt>Assigned to</dt>
          <dd>{assignee?.name ?? 'Unassigned'}</dd>
        </div>
      </dl>

      {isStaff ? (
        <div className="staff-actions">
          {canAssign ? (
            <button
              type="button"
              className="btn primary"
              onClick={() => onAssign(request.id)}
            >
              Accept / assign to me
            </button>
          ) : null}

          {canTransition ? (
            <div className="transition-box">
              <label>
                Status note (optional)
                <input
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  placeholder="Reason for the update"
                />
              </label>
              <div className="form-actions">
                {nextStatuses.map((status) => (
                  <button
                    key={status}
                    type="button"
                    className="btn primary"
                    onClick={() => {
                      onTransition(request.id, status, note.trim() || null)
                      setNote('')
                    }}
                  >
                    Move to {STATUS_LABELS[status]}
                  </button>
                ))}
              </div>
            </div>
          ) : null}
        </div>
      ) : null}

      <div className="split-columns">
        <div>
          <h3>Status history</h3>
          <ol className="timeline">
            {[...request.history]
              .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
              .map((entry) => {
                const actor = userById(entry.changedById)
                return (
                  <li key={entry.id}>
                    <strong>
                      {entry.fromStatus
                        ? `${STATUS_LABELS[entry.fromStatus]} → ${STATUS_LABELS[entry.toStatus]}`
                        : STATUS_LABELS[entry.toStatus]}
                    </strong>
                    <span>
                      {actor?.name ?? 'System'} · {formatDateTime(entry.timestamp)}
                    </span>
                    {entry.note ? <em>{entry.note}</em> : null}
                  </li>
                )
              })}
          </ol>
        </div>

        <div>
          <h3>Comments</h3>
          {request.comments.length === 0 ? (
            <p className="empty-state">No comments yet.</p>
          ) : (
            <ul className="comment-list">
              {[...request.comments]
                .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
                .map((c) => {
                  const author = userById(c.authorId)
                  return (
                    <li key={c.id}>
                      <strong>{author?.name ?? 'Unknown'}</strong>
                      <span>{formatDateTime(c.createdAt)}</span>
                      <p>{c.content}</p>
                    </li>
                  )
                })}
            </ul>
          )}

          {isStaff ? (
            <form className="comment-form" onSubmit={handleComment}>
              <label>
                Add a comment
                <textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  rows={3}
                  placeholder="Resolution notes or updates for the requester"
                />
              </label>
              <button type="submit" className="btn primary">
                Post comment
              </button>
            </form>
          ) : null}
        </div>
      </div>
    </section>
  )
}
