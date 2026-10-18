import { useState } from "react";
import { login } from "../services/client.js";
import { validateLoginForm } from "../services/validation.js";
import { BrandMark, CityScene } from "./Visuals.jsx";

export default function LoginScreen({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setFormError("");

    const result = validateLoginForm({ email, password });
    setFieldErrors(result.errors);

    if (!result.isValid) {
      return;
    }

    setSubmitting(true);
    try {
      const user = await login(result.values.email, result.values.password);
      onLogin?.(user);
    } catch (error) {
      setFormError(error.message || "Could not sign in.");
      setPassword("");
    } finally {
      setSubmitting(false);
    }
  }

  return (
      <main className="page login-page">
        <div className="login-shell">
          <div className="login-hero">
            <div className="login-brand">
              <BrandMark />
              <span>CivicConnect</span>
            </div>
            <CityScene />
          </div>

          <div className="login-card">
            <h1 className="login-title">Report it. Track it. Sorted.</h1>
            <p className="login-sub">
              Sign in to report problems around you and follow them until they are fixed.
            </p>

            <form onSubmit={handleSubmit} noValidate>
              <div className="field">
                <label htmlFor="email">Email</label>
                <input
                    id="email"
                    name="email"
                    type="email"
                    autoComplete="username"
                    inputMode="email"
                    maxLength={255}
                    placeholder="you@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    disabled={submitting}
                    required
                />
                {fieldErrors.email && <p className="error">{fieldErrors.email}</p>}
              </div>

              <div className="field">
                <label htmlFor="password">Password</label>
                <input
                    id="password"
                    name="password"
                    type="password"
                    autoComplete="current-password"
                    maxLength={128}
                    placeholder="Your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    disabled={submitting}
                    required
                />
                {fieldErrors.password && <p className="error">{fieldErrors.password}</p>}
              </div>

              {formError && (
                  <div className="alert alert-error" role="alert">
                    {formError}
                  </div>
              )}

              <button type="submit" className="btn-block" disabled={submitting}>
                {submitting ? "Signing in…" : "Sign in"}
              </button>
            </form>

            {/* RISK-004: demo hint only on the local dev server, never in a production build. No password shown. */}
            {import.meta.env.DEV && (
            <div className="demo-hint">
              <p className="demo-hint-title">Demo accounts (local demo only)</p>
              <ul>
                <li>requester@civicconnect.local</li>
                <li>staff@civicconnect.local</li>
                <li>management@civicconnect.local</li>
              </ul>
            </div>
            )}
          </div>
        </div>
      </main>
  );
}
