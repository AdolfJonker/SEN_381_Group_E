import { useCallback, useEffect, useState } from 'react'
import { api } from './api/client'
import LoginScreen from './components/LoginScreen'
import ManagementSummary from './components/ManagementSummary'
import RequestDetail from './components/RequestDetail'
import RequestForm from './components/RequestForm'
import RequestList from './components/RequestList'
import { ROLES } from './data/constants'
import './App.css'

function App() {
  const [users, setUsers] = useState([])
  const [usersLoading, setUsersLoading] = useState(true)
  const [usersError, setUsersError] = useState('')
  const [user, setUser] = useState(null)
  const [requests, setRequests] = useState([])
  const [summary, setSummary] = useState(null)
  const [selected, setSelected] = useState(null)
  const [view, setView] = useState('list')
  const [flash, setFlash] = useState('')
  const [busy, setBusy] = useState(false)

  const showFlash = useCallback((message) => {
    setFlash(message)
    window.setTimeout(() => setFlash(''), 4000)
  }, [])

  const loadUsers = useCallback(async () => {
    setUsersLoading(true)
    setUsersError('')
    try {
      const data = await api.listUsers()
      setUsers(data)
    } catch (err) {
      setUsersError(
        err.message ||
          'Cannot reach the backend. Start it with .\\mvnw.cmd spring-boot:run in backend/demo (port 8081).',
      )
    } finally {
      setUsersLoading(false)
    }
  }, [])

  useEffect(() => {
    loadUsers()
  }, [loadUsers])

  const refreshList = useCallback(
    async (activeUser = user) => {
      if (!activeUser) return
      const data = await api.listRequests(activeUser.id)
      setRequests(data)
      if (activeUser.role === ROLES.MANAGEMENT) {
        setSummary(await api.getSummary(activeUser.id))
      }
    },
    [user],
  )

  async function handleLogin(nextUser) {
    setUser(nextUser)
    setView(nextUser.role === ROLES.MANAGEMENT ? 'summary' : 'list')
    setSelected(null)
    setBusy(true)
    try {
      await refreshList(nextUser)
    } catch (err) {
      showFlash(err.message)
    } finally {
      setBusy(false)
    }
  }

  function handleLogout() {
    setUser(null)
    setRequests([])
    setSummary(null)
    setSelected(null)
    setView('list')
    setFlash('')
  }

  async function openDetail(id) {
    setBusy(true)
    try {
      const detail = await api.getRequest(user.id, id)
      setSelected(detail)
      setView('detail')
    } catch (err) {
      showFlash(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleSubmitRequest(form) {
    setBusy(true)
    try {
      const created = await api.createRequest(user.id, form)
      showFlash(`Request ${created.id} submitted successfully.`)
      setView('list')
      await refreshList()
    } catch (err) {
      showFlash(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleAssign(requestId) {
    setBusy(true)
    try {
      const updated = await api.assignRequest(user.id, requestId)
      setSelected(updated)
      showFlash('Request assigned to you.')
      await refreshList()
    } catch (err) {
      showFlash(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleTransition(requestId, toStatus, note) {
    setBusy(true)
    try {
      const updated = await api.updateStatus(user.id, requestId, {
        toStatus,
        note,
      })
      setSelected(updated)
      showFlash('Status updated.')
      await refreshList()
    } catch (err) {
      showFlash(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleAddComment(requestId, content) {
    setBusy(true)
    try {
      const updated = await api.addComment(user.id, requestId, content)
      setSelected(updated)
      await refreshList()
    } catch (err) {
      showFlash(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (!user) {
    return (
      <LoginScreen
        users={users}
        loading={usersLoading}
        error={usersError}
        onLogin={handleLogin}
        onRetry={loadUsers}
      />
    )
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-brand">
          <span className="brand-mark">CivicConnect</span>
          <span className="brand-sub">Service requests</span>
        </div>
        <nav className="topbar-nav" aria-label="Main">
          {user.role === ROLES.MANAGEMENT ? (
            <button
              type="button"
              className={view === 'summary' ? 'active' : ''}
              onClick={async () => {
                setView('summary')
                setSelected(null)
                try {
                  await refreshList()
                } catch (err) {
                  showFlash(err.message)
                }
              }}
            >
              Summary
            </button>
          ) : null}
          <button
            type="button"
            className={view === 'list' || view === 'detail' ? 'active' : ''}
            onClick={async () => {
              setView('list')
              setSelected(null)
              try {
                await refreshList()
              } catch (err) {
                showFlash(err.message)
              }
            }}
          >
            Requests
          </button>
          {user.role === ROLES.REQUESTER ? (
            <button
              type="button"
              className={view === 'submit' ? 'active' : ''}
              onClick={() => {
                setView('submit')
                setSelected(null)
              }}
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
            Switch role
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
          <>
            <ManagementSummary summary={summary} />
            <RequestList
              title="All requests"
              subtitle="Filter by category or status for oversight."
              requests={requests}
              showFilters
              onSelect={openDetail}
            />
          </>
        ) : null}

        {view === 'list' ? (
          <RequestList
            title={
              user.role === ROLES.REQUESTER
                ? 'My requests'
                : 'Service requests'
            }
            subtitle={
              user.role === ROLES.REQUESTER
                ? 'Track status for everything you have submitted.'
                : 'Search, filter, and open a request to take action.'
            }
            requests={requests}
            showFilters={user.role !== ROLES.REQUESTER}
            onSelect={openDetail}
          />
        ) : null}

        {view === 'submit' ? (
          <RequestForm
            onSubmit={handleSubmitRequest}
            onCancel={() => setView('list')}
          />
        ) : null}

        {view === 'detail' && selected ? (
          <RequestDetail
            request={selected}
            currentUser={user}
            onBack={() => {
              setSelected(null)
              setView(user.role === ROLES.MANAGEMENT ? 'summary' : 'list')
            }}
            onAssign={handleAssign}
            onTransition={handleTransition}
            onAddComment={handleAddComment}
          />
        ) : null}
      </main>
    </div>
  )
}

export default App
