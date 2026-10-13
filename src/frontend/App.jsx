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
        return (
            <main className="page login-page">
                <p className="muted">Loading…</p>
            </main>
        );
    }

    if (!user) {
        return <LoginScreen onLogin={setUser} />;
    }

    // ----- Logged-in App Shell -----
    return (
        <div className="app-shell">
            <header className="app-header">
                <h1>CivicConnect</h1>
                <div className="user-info">
                    {user.name} ({user.role})
                    <button
                        type="button"
                        className="btn-outline"
                        style={{ marginLeft: "1rem", padding: "0.4rem 0.8rem", fontSize: "0.85rem" }}
                        onClick={async () => {
                            await logout();
                            setUser(null);
                        }}
                    >
                        Sign out
                    </button>
                </div>
            </header>

            <main className="app-main">
                <div className="card">
                    <h2 style={{ marginTop: 0, color: "var(--navy)" }}>
                        Welcome, {user.name}
                    </h2>
                    <p className="muted">
                        You are signed in as <strong>{user.role}</strong>.
                    </p>
                    <p>
                        This is the temporary home screen.
                        Next we will add the real screens for your role.
                    </p>
                </div>
            </main>
        </div>
    );
}