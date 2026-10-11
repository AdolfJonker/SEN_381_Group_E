import { useState } from 'react'

/**
 * Login screen (NFR003). Email + password are checked by the backend
 * (GET /api/auth/me with HTTP Basic). The screen never decides the role
 * itself; it only shows what the server returns.
 */
export default function LoginScreen({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    if (!email.trim() || !password) {
      setError('Enter your email and password.')
      return
    }
    setSubmitting(true)
    try {
      await onLogin(email.trim(), password)
    } catch (err) {
      setError(
        err.status === 401
          ? 'Email or password is incorrect.'
          : err.message || 'Sign in failed. Try again.',
      )
      setPassword('')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="login-screen">
      <div className="login-panel">
        <p className="brand-mark">CivicConnect</p>
        <h1>Campus service requests, clearly tracked</h1>
        <p className="login-lead">
          Sign in with your campus email. You will land on the view for your
          role.
        </p>

        <form className="login-form" onSubmit={handleSubmit} noValidate>
          <label>
            Email
            <input
              type="email"
              name="email"
              autoComplete="username"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={submitting}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              name="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={submitting}
              required
            />
          </label>

          {error ? (
            <p className="form-error" role="alert">
              {error}
            </p>
          ) : null}

          <button type="submit" className="btn primary" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
      </div>
      <div className="login-visual" aria-hidden="true">
        <div className="login-visual-grid" />
        <p>Belgium Campus · SEN381</p>
      </div>
    </div>
  )
}
