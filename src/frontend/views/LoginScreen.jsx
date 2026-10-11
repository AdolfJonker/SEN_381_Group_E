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
      <h1>CivicConnect</h1>
      <p>Sign in to continue</p>

      <form className="login-form" onSubmit={handleSubmit} noValidate>
        <label htmlFor="email">Email</label>
        <input
          id="email"
          name="email"
          type="email"
          autoComplete="username"
          inputMode="email"
          maxLength={255}
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          disabled={submitting}
          required
        />
        {fieldErrors.email ? <p className="error">{fieldErrors.email}</p> : null}

        <label htmlFor="password">Password</label>
        <input
          id="password"
          name="password"
          type="password"
          autoComplete="current-password"
          maxLength={128}
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          disabled={submitting}
          required
        />
        {fieldErrors.password ? <p className="error">{fieldErrors.password}</p> : null}

        {formError ? <p className="error" role="alert">{formError}</p> : null}

        <button type="submit" disabled={submitting}>
          {submitting ? "Signing in…" : "Sign in"}
        </button>
      </form>

      <section className="demo-hint">
        <p>Demo accounts (password for all: Password123!)</p>
        <ul>
          <li>requester@civicconnect.local</li>
          <li>staff@civicconnect.local</li>
          <li>management@civicconnect.local</li>
        </ul>
      </section>
    </main>
  );
}