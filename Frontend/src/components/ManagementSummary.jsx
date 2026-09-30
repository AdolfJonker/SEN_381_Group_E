import { STATUS_LABELS } from '../data/constants'

export default function ManagementSummary({ summary }) {
  const cards = [
    { key: 'Open', label: STATUS_LABELS.Open, value: summary?.open ?? 0 },
    { key: 'Overdue', label: 'Overdue', value: summary?.overdue ?? 0 },
    {
      key: 'Resolved',
      label: STATUS_LABELS.Resolved,
      value: summary?.resolved ?? 0,
    },
    { key: 'Closed', label: STATUS_LABELS.Closed, value: summary?.closed ?? 0 },
  ]

  return (
    <section className="panel">
      <header className="panel-header">
        <h2>Service activity summary</h2>
        <p>Aggregate counts across all campus service requests.</p>
      </header>
      <div className="summary-grid">
        {cards.map((card) => (
          <article key={card.key} className={`summary-card tone-${card.key}`}>
            <p className="summary-value">{card.value}</p>
            <p className="summary-label">{card.label}</p>
          </article>
        ))}
      </div>
    </section>
  )
}
