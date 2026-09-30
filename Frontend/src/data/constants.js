export const ROLES = {
  REQUESTER: 'Requester',
  STAFF: 'Staff',
  MANAGEMENT: 'Management',
}

export const CATEGORIES = [
  'Facility Fault',
  'Damaged Equipment',
  'Security Concern',
  'IT Support',
  'Maintenance',
  'Lost Property',
  'Other',
]

export const STATUSES = ['Open', 'Assigned', 'InProgress', 'Resolved', 'Closed']

export const STATUS_LABELS = {
  Open: 'Open',
  Assigned: 'Assigned',
  InProgress: 'In Progress',
  Resolved: 'Resolved',
  Closed: 'Closed',
}

/** Allowed next statuses from the FR009 state model */
export const TRANSITIONS = {
  Open: ['Assigned'],
  Assigned: ['InProgress'],
  InProgress: ['Resolved'],
  Resolved: ['Closed'],
  Closed: [],
}
