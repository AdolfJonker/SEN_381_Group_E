import { useState } from "react";
import { login } from "../services/client.js";
import { validateLoginForm } from "../services/validation.js";

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
        <div className="card" style={{ width: "100%", maxWidth: "400px" }}>
          <h1 style={{
            margin: "0 0 0.25rem 0",
            color: "#000080",
            fontSize: "1.75rem",
            textAlign: "center"
          }}>
            CivicConnect
          </h1>
          <p style={{
            margin: "0 0 1.75rem 0",
            color: "var(--muted)",
            textAlign: "center"
          }}>
            Sign in to continue
          </p>

          <form onSubmit={handleSubmit} noValidate>
            <div style={{ marginBottom: "1rem" }}>
              <label htmlFor="email">Email</label>
              <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="username"
                  inputMode="email"
                  maxLength={255}
                  placeholder="Enter your email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={submitting}
                  required
              />
              {fieldErrors.email && <p className="error">{fieldErrors.email}</p>}
            </div>

            <div style={{ marginBottom: "1.25rem" }}>
              <label htmlFor="password">Password</label>
              <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="current-password"
                  maxLength={128}
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={submitting}
                  required
              />
              {fieldErrors.password && <p className="error">{fieldErrors.password}</p>}
            </div>

            {formError && (
                <p className="error" role="alert" style={{ marginBottom: "1rem" }}>
                  {formError}
                </p>
            )}

            <button
                type="submit"
                disabled={submitting}
                style={{ width: "100%" }}
            >
              {submitting ? "Signing in…" : "Sign in"}
            </button>
          </form>

          {/* RISK-004: demo hint only on the local dev server, never in a production build. No password shown. */}
          {import.meta.env.DEV && (
          <div style={{
            marginTop: "1.75rem",
            padding: "1rem",
            background: "var(--subtle)",
            borderRadius: "10px",
            fontSize: "0.875rem",
            color: "var(--muted)"
          }}>
            <p style={{ margin: "0 0 0.5rem 0", fontWeight: 500 }}>
              Demo accounts (local demo only; ask the team for the password)
            </p>
            <ul style={{ margin: 0, paddingLeft: "1.2rem" }}>
              <li>requester@civicconnect.local</li>
              <li>staff@civicconnect.local</li>
              <li>management@civicconnect.local</li>
            </ul>
          </div>
          )}
        </div>
      </main>
  );
}