// Run with: npm test   (Node's built-in test runner, no extra packages)
import assert from 'node:assert/strict'
import { describe, it } from 'node:test'
import { canOpenView, landingViewFor } from './roleLanding.js'

// TC-UI-01 (NFR003, FR012): each role lands on its own view; unknown roles fail closed.
describe('landingViewFor', () => {
  it('sends Management to the summary dashboard', () => {
    assert.equal(landingViewFor('Management'), 'summary')
  })

  it('sends Staff and Requester to their request list', () => {
    assert.equal(landingViewFor('Staff'), 'list')
    assert.equal(landingViewFor('Requester'), 'list')
  })

  it('gives no landing view to an unknown or missing role', () => {
    assert.equal(landingViewFor('Admin'), null)
    assert.equal(landingViewFor(undefined), null)
  })
})

describe('canOpenView', () => {
  it('lets only Management open the summary', () => {
    assert.equal(canOpenView('Management', 'summary'), true)
    assert.equal(canOpenView('Staff', 'summary'), false)
    assert.equal(canOpenView('Requester', 'summary'), false)
  })

  it('lets only Requesters open the submit form', () => {
    assert.equal(canOpenView('Requester', 'submit'), true)
    assert.equal(canOpenView('Management', 'submit'), false)
  })
})
