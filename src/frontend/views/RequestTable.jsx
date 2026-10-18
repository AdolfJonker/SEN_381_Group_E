import { STATUS_LABELS } from "../services/requestOptions.js";
import { CategoryIcon } from "./Visuals.jsx";

function formatDate(iso) {
  return new Date(iso).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

/** Shared request table for the Staff and Management views. Rows become cards on small screens. */
export default function RequestTable({ requests, emptyText, onSelect }) {
  if (requests.length === 0) {
    return <p className="muted empty-state">{emptyText ?? "No requests match this view."}</p>;
  }

  return (
    <div className="table-wrap">
      <table className="request-table">
        <thead>
          <tr>
            <th>Request</th>
            <th className="col-category">Category</th>
            <th>Submitted</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {requests.map((r) => (
            <tr key={r.id}>
              <td className="col-main">
                <div className="cell-main">
                <CategoryIcon category={r.category} size="sm" />
                <span className="cell-stack">
                  {onSelect ? (
                    <button type="button" className="link-button" onClick={() => onSelect(r.id)}>
                      {r.title}
                    </button>
                  ) : (
                    <span className="cell-title">{r.title}</span>
                  )}
                  <span className="cell-id">#{r.id}</span>
                </span>
                </div>
              </td>
              <td className="col-category" data-label="Category">{r.category}</td>
              <td className="col-date" data-label="Submitted">{formatDate(r.createdAt)}</td>
              <td className="col-status">
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
