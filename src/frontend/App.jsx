import { useEffect, useState } from "react";
import LoginScreen from "./views/LoginScreen.jsx";
import { fetchCurrentUser, logout } from "./services/client.js";

export default function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCurrentUser()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <main className="page"><p>Loading…</p></main>;
  }

  if (!user) {
    return <LoginScreen onLogin={setUser} />;
  }

  return (
    <main className="page">
      <h1>CivicConnect</h1>
      <p>
        Signed in as <strong>{user.name}</strong> ({user.role})
      </p>
      <p>{user.email}</p>
      <button
        type="button"
        onClick={async () => {
          await logout();
          setUser(null);
        }}
      >
        Sign out
      </button>
    </main>
  );
}