export default function LoginScreen({ users, loading, error, onLogin, onRetry }) {
  return (
    <div className="login-screen">
      <div className="login-panel">
        <p className="brand-mark">CivicConnect</p>
        <h1>Campus service requests, clearly tracked</h1>
        <p className="login-lead">
          Choose a demo role. The UI talks to the Spring Boot API on
          localhost:8081.
        </p>

        {loading ? <p className="empty-state">Loading demo users…</p> : null}
        {error ? (
          <div className="form-error" role="alert">
            <p>{error}</p>
            <button type="button" className="btn primary" onClick={onRetry}>
              Retry connection
            </button>
          </div>
        ) : null}

        {!loading && !error ? (
          <ul className="role-list">
            {users.map((user) => (
              <li key={user.id}>
                <button type="button" onClick={() => onLogin(user)}>
                  <span className="role-name">{user.role}</span>
                  <span className="role-meta">
                    {user.name} · {user.email}
                  </span>
                </button>
              </li>
            ))}
          </ul>
        ) : null}
      </div>
      <div className="login-visual" aria-hidden="true">
        <div className="login-visual-grid" />
        <p>Belgium Campus · SEN381</p>
      </div>
    </div>
  )
}
