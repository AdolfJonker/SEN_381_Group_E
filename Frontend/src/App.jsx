import { useCallback, useEffect, useState } from 'react'
import {
  ApiError,
  api,
  clearCredentials,
  setCredentials,
  setUnauthorizedHandler,
} from './api/client'
import { canOpenView, landingViewFor } from './auth/roleLanding'
import LoginScreen from './components/LoginScreen'
import ManagementDashboard from './components/ManagementDashboard'
import RequestDetail from './components/RequestDetail'
import RequestForm from './components/RequestForm'
import RequestList from './components/RequestList'
import { ROLES } from './data/constants'
import './App.css'

function App() {
  const [user, setUser] = useState(null)
  const [requests, setRequests] = useState([])
  const [selected, setSelected] = useState(null)
  const [view, setView] = useState(null)
  const [flash, setFlash] = useState('')
  const [busy, setBusy] = useState(false)

  const showFlash = useCallback((message) => {
    setFlash(message)
    window.setTimeout(() => setFlash(''), 4000)
  }, [])

  const handleLogout = useCallback(() => {
    clearCredentials()
    setUser(null)
    setRequests([])
    setSelected(null)
    setView(null)
    setFlash('')
  }, [])

  // Any 401 from the API (e.g. password changed) sends the user back to login.
  useEffect(() => {
    setUnauthorizedHandler(() => handleLogout())
    return () => setUnauthorizedHandler(null)
  }, [handleLogout])

  const refreshList = useCallback(async () => {
    setRequests(await api.listRequests())
  }, [])

  /** Login: the server checks the password and tells us the role. */
  async function handleLogin(email, password) {
    setCredentials(email, password)
    let me
    try {
      me = await api.me()
    } catch (err) {
      clearCredentials()
      throw err
    }
    const landing = landingViewFor(me.role)
    if (!landing) {
      clearCredentials()
      throw new ApiError('Your account has no role in CivicConnect.', 403)
    }
    setUser(me)
    setSelected(null)
    setView(landing)
    if (landing === 'list') {
      setBusy(true)
      try {
        setRequests(await api.listRequests())
      } catch (err) {
        showFlash(err.message)
      } finally {
        setBusy(false)
      }
    }
  }

  function goTo(nextView) {
    if (!canOpenView(user.role, nextView)) return
    setSelected(null)
    setView(nextView)
    if (nextView === 'list') refreshList().catch((err) => showFlash(err.message))
  }

  async function openDetail(id) {
    setBusy(true)
    try {
      setSelected(await api.getRequest(id))
      setView('detail')
    } catch (err) {
      showFlash(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function runAction(action, successMessage) {
    setBusy(true)
    try {
      const updated = await action()
      if (updated) setSelected(updated)
      if (successMessage) showFlash(successMessage)
      await refreshList()
    } catch (err) {
      showFlash(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleSubmitRequest(form) {
    setBusy(true)
    try {
      const created = await api.createRequest(form)
      showFlash(`Request ${created.id} submitted successfully.`)
      setView('list')
      await refreshList()
    } catch (err) {
      showFlash(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (!user) {
    return <LoginScreen onLogin={handleLogin} />
  }

  const landing = landingViewFor(user.role)

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-brand">
          <span className="brand-mark">CivicConnect</span>
          <span className="brand-sub">Service requests</span>
        </div>
        <nav className="topbar-nav" aria-label="Main">
          {canOpenView(user.role, 'summary') ? (
            <button
              type="button"
              className={view === 'summary' || view === 'detail' ? 'active' : ''}
              onClick={() => goTo('summary')}
            >
              Summary
            </button>
          ) : null}
          {canOpenView(user.role, 'list') ? (
            <button
              type="button"
              className={view === 'list' || view === 'detail' ? 'active' : ''}
              onClick={() => goTo('list')}
            >
              {user.role === ROLES.REQUESTER ? 'My requests' : 'Requests'}
            </button>
          ) : null}
          {canOpenView(user.role, 'submit') ? (
            <button
              type="button"
              className={view === 'submit' ? 'active' : ''}
              onClick={() => goTo('submit')}
            >
              New request
            </button>
          ) : null}
        </nav>
        <div className="topbar-user">
          <div>
            <strong>{user.name}</strong>
            <span>{user.role}</span>
          </div>
          <button type="button" className="btn ghost" onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </header>

      <main className="app-main">
        {flash ? (
          <div className="flash" role="status">
            {flash}
          </div>
        ) : null}
        {busy ? <p className="empty-state">Working…</p> : null}

        {view === 'summary' ? (
          <ManagementDashboard onSelect={openDetail} onError={showFlash} />
        ) : null}

        {view === 'list' ? (
          <RequestList
            title={
              user.role === ROLES.REQUESTER ? 'My requests' : 'Service requests'
            }
            subtitle={
              user.role === ROLES.REQUESTER
                ? 'Track status for everything you have submitted.'
                : 'Search, filter, and open a request to take action.'
            }
            requests={requests}
            showFilters={user.role === ROLES.STAFF}
            onSelect={openDetail}
          />
        ) : null}

        {view === 'submit' ? (
          <RequestForm
            onSubmit={handleSubmitRequest}
            onCancel={() => goTo('list')}
          />
        ) : null}

        {view === 'detail' && selected ? (
          <RequestDetail
            request={selected}
            currentUser={user}
            onBack={() => {
              setSelected(null)
              setView(landing)
            }}
            onAssign={(id) =>
              runAction(() => api.assignRequest(id), 'Request assigned to you.')
            }
            onTransition={(id, toStatus, note) =>
              runAction(
                () => api.updateStatus(id, { toStatus, note }),
                'Status updated.',
              )
            }
            onAddComment={(id, content) =>
              runAction(() => api.addComment(id, content))
            }
          />
        ) : null}
      </main>
    </div>
  )
}

export default App
