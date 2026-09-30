import { ROLES } from './constants'

export const USERS = [
  {
    id: 'u-req-1',
    name: 'Aisha Ndlovu',
    email: 'aisha@campus.edu',
    role: ROLES.REQUESTER,
  },
  {
    id: 'u-staff-1',
    name: 'Johan Botha',
    email: 'johan@campus.edu',
    role: ROLES.STAFF,
  },
  {
    id: 'u-mgmt-1',
    name: 'Thandi Mokoena',
    email: 'thandi@campus.edu',
    role: ROLES.MANAGEMENT,
  },
]

let nextRequestNum = 1004

export function createInitialRequests() {
  return [
    {
      id: 'SR-1001',
      title: 'Broken projector in Lecture Hall B',
      description:
        'The ceiling projector flickers and shuts off after a few minutes. Affects morning lectures.',
      category: 'Damaged Equipment',
      status: 'Assigned',
      submittedById: 'u-req-1',
      assignedToId: 'u-staff-1',
      createdAt: '2026-03-20T09:15:00Z',
      updatedAt: '2026-03-21T11:00:00Z',
      history: [
        {
          id: 'h1',
          fromStatus: null,
          toStatus: 'Open',
          changedById: 'u-req-1',
          timestamp: '2026-03-20T09:15:00Z',
          note: 'Request submitted',
        },
        {
          id: 'h2',
          fromStatus: 'Open',
          toStatus: 'Assigned',
          changedById: 'u-staff-1',
          timestamp: '2026-03-21T11:00:00Z',
          note: 'Accepted by facilities AV team',
        },
      ],
      comments: [
        {
          id: 'c1',
          authorId: 'u-staff-1',
          content: 'Spare bulb ordered; ETA Thursday.',
          createdAt: '2026-03-21T14:30:00Z',
        },
      ],
    },
    {
      id: 'SR-1002',
      title: 'Water leak near Library entrance',
      description:
        'Puddle forming by the automatic doors after rain. Possible roof or drainage issue.',
      category: 'Facility Fault',
      status: 'Open',
      submittedById: 'u-req-1',
      assignedToId: null,
      createdAt: '2026-03-22T07:40:00Z',
      updatedAt: '2026-03-22T07:40:00Z',
      history: [
        {
          id: 'h3',
          fromStatus: null,
          toStatus: 'Open',
          changedById: 'u-req-1',
          timestamp: '2026-03-22T07:40:00Z',
          note: 'Request submitted',
        },
      ],
      comments: [],
    },
    {
      id: 'SR-1003',
      title: 'VPN access for remote lab',
      description:
        'Cannot connect to the campus VPN from home; error 809 after credentials accepted.',
      category: 'IT Support',
      status: 'InProgress',
      submittedById: 'u-req-1',
      assignedToId: 'u-staff-1',
      createdAt: '2026-03-18T16:05:00Z',
      updatedAt: '2026-03-23T10:20:00Z',
      history: [
        {
          id: 'h4',
          fromStatus: null,
          toStatus: 'Open',
          changedById: 'u-req-1',
          timestamp: '2026-03-18T16:05:00Z',
          note: 'Request submitted',
        },
        {
          id: 'h5',
          fromStatus: 'Open',
          toStatus: 'Assigned',
          changedById: 'u-staff-1',
          timestamp: '2026-03-19T08:10:00Z',
          note: null,
        },
        {
          id: 'h6',
          fromStatus: 'Assigned',
          toStatus: 'InProgress',
          changedById: 'u-staff-1',
          timestamp: '2026-03-23T10:20:00Z',
          note: 'Checking RADIUS logs',
        },
      ],
      comments: [],
    },
  ]
}

export function nextRequestId() {
  nextRequestNum += 1
  return `SR-${nextRequestNum}`
}

export function userById(id) {
  return USERS.find((u) => u.id === id)
}
