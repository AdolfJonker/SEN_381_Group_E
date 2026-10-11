import StatusBadge from './StatusBadge'

function formatDate(iso) {
  return new Date(iso).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

/** Shared request table used by the Staff, Requester and Management views. */
export default function RequestTable({ requests, onSelect, emptyText }) {
  if (requests.length === 0) {
    return (
      <p className="empty-state">{emptyText ?? 'No requests match this view.'}</p>
    )
  }

  return (
    <div className="table-wrap">
      <table className="request-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Category</th>
            <th>Submitted</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {requests.map((req) => (
            <tr key={req.id}>
              <td>
                <button
                  type="button"
                  className="linkish"
                  onClick={() => onSelect(req.id)}
                >
                  {req.id}
                </button>
              </td>
              <td>{req.title}</td>
              <td>{req.category}</td>
              <td>{formatDate(req.createdAt)}</td>
              <td>
                <StatusBadge status={req.status} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
