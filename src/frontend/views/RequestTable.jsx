import { STATUS_LABELS } from "../services/requestOptions.js";

function formatDate(iso) {
  return new Date(iso).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

/** Shared request table for the Staff and Management views. */
export default function RequestTable({ requests, emptyText, onSelect }) {
  if (requests.length === 0) {
    return <p className="muted empty-state">{emptyText ?? "No requests match this view."}</p>;
  }

  return (
    <div className="table-wrap">
      <table className="request-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th className="col-category">Category</th>
            <th>Submitted</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {requests.map((r) => (
            <tr key={r.id}>
              <td>
                {onSelect ? (
                  <button type="button" className="link-button" onClick={() => onSelect(r.id)}>
                    #{r.id}
                  </button>
                ) : (
                  `#${r.id}`
                )}
              </td>
              <td>{r.title}</td>
              <td className="col-category">{r.category}</td>
              <td>{formatDate(r.createdAt)}</td>
              <td>
                <span className={`status-badge status-${r.status}`}>
                  {STATUS_LABELS[r.status] ?? r.status}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
