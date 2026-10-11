// Run with: npm test   (Node's built-in test runner, no extra packages)
import assert from 'node:assert/strict'
import { describe, it } from 'node:test'
import { applyFilters, sortByDate } from './requestFilters.js'

// Small fixed data set: one row per combination the filters must separate.
const rows = [
  { id: 'SR-1', category: 'IT Support', status: 'Open', createdAt: '2026-10-01T08:00:00Z' },
  { id: 'SR-2', category: 'IT Support', status: 'Closed', createdAt: '2026-10-03T08:00:00Z' },
  { id: 'SR-3', category: 'Maintenance', status: 'Open', createdAt: '2026-10-02T08:00:00Z' },
  { id: 'SR-4', category: 'Maintenance', status: 'Resolved', createdAt: '2026-10-04T08:00:00Z' },
]
const ids = (list) => list.map((r) => r.id)

// TC-UI-02 (FR013): equivalence partitions for the two filters
// (no filter / category only / status only / both / no match).
describe('applyFilters', () => {
  it('returns everything when no filter is set', () => {
    assert.deepEqual(ids(applyFilters(rows)), ['SR-1', 'SR-2', 'SR-3', 'SR-4'])
  })

  it('filters by category only', () => {
    assert.deepEqual(ids(applyFilters(rows, { category: 'Maintenance' })), ['SR-3', 'SR-4'])
  })

  it('filters by status only', () => {
    assert.deepEqual(ids(applyFilters(rows, { status: 'Open' })), ['SR-1', 'SR-3'])
  })

  it('combines category and status (AND, not OR)', () => {
    assert.deepEqual(
      ids(applyFilters(rows, { category: 'IT Support', status: 'Open' })),
      ['SR-1'],
    )
  })

  it('returns an empty list when nothing matches', () => {
    assert.deepEqual(applyFilters(rows, { category: 'Lost Property' }), [])
  })
})

describe('sortByDate', () => {
  it('sorts newest first by default and oldest first on request', () => {
    assert.deepEqual(ids(sortByDate(rows)), ['SR-4', 'SR-2', 'SR-3', 'SR-1'])
    assert.deepEqual(ids(sortByDate(rows, 'asc')), ['SR-1', 'SR-3', 'SR-2', 'SR-4'])
  })

  it('does not change the original array', () => {
    sortByDate(rows, 'asc')
    assert.deepEqual(ids(rows), ['SR-1', 'SR-2', 'SR-3', 'SR-4'])
  })
})
