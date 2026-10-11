import { STATUS_LABELS } from '../data/constants'

/**
 * FR012: aggregate service activity summary.
 * The acceptance criterion asks for Open, Overdue, Resolved and Closed,
 * so these four are the main cards. Assigned and In Progress are shown
 * smaller so the counts add up to the total.
 * Clicking a status card applies that status filter (FR013).
 */
export default function ManagementSummary({ summary, activeStatus, onPickStatus }) {
  const mainCards = [
    { key: 'Open', label: STATUS_LABELS.Open, value: summary?.open, status: 'Open' },
    {
      key: 'Overdue',
      label: 'Overdue',
      value: summary?.overdue,
      hint: 'Not resolved after 7 days',
    },
    {
      key: 'Resolved',
      label: STATUS_LABELS.Resolved,
      value: summary?.resolved,
      status: 'Resolved',
    },
    { key: 'Closed', label: STATUS_LABELS.Closed, value: summary?.closed, status: 'Closed' },
  ]
  const workCards = [
    {
      key: 'Assigned',
      label: STATUS_LABELS.Assigned,
      value: summary?.assigned,
      status: 'Assigned',
    },
    {
      key: 'InProgress',
      label: STATUS_LABELS.InProgress,
      value: summary?.inProgress,
      status: 'InProgress',
    },
  ]

  function renderCard(card, small = false) {
    const value = card.value ?? '–'
    const className = `summary-card tone-${card.key}${small ? ' small' : ''}${
      card.status && card.status === activeStatus ? ' active' : ''
    }`
    const content = (
      <>
        <p className="summary-value">{value}</p>
        <p className="summary-label">{card.label}</p>
        {card.hint ? <p className="summary-hint">{card.hint}</p> : null}
      </>
    )
    if (!card.status) {
      return (
        <article key={card.key} className={className}>
          {content}
        </article>
      )
    }
    const isActive = card.status === activeStatus
    return (
      <button
        key={card.key}
        type="button"
        className={className}
        aria-pressed={isActive}
        title={isActive ? 'Clear this filter' : `Show ${card.label} requests`}
        onClick={() => onPickStatus(isActive ? '' : card.status)}
      >
        {content}
      </button>
    )
  }

  return (
    <section className="panel" aria-labelledby="summary-heading">
      <header className="panel-header">
        <h2 id="summary-heading">Service activity summary</h2>
        <p>Counts across all service requests. Click a status to filter the list.</p>
      </header>
      <div className="summary-grid">{mainCards.map((c) => renderCard(c))}</div>
      <div className="summary-grid secondary">
        {workCards.map((c) => renderCard(c, true))}
      </div>
    </section>
  )
}
