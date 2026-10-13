import { useEffect, useState } from "react";
import LoginScreen from "./views/LoginScreen.jsx";
import SubmitRequest from "./views/SubmitRequest.jsx";
import StaffRequestList from "./views/StaffRequestList.jsx";
import StaffRequestDetail from "./views/StaffRequestDetail.jsx";
import { fetchCurrentUser, logout } from "./services/client.js";

export default function App() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [screen, setScreen] = useState("home");
    const [selectedRequestId, setSelectedRequestId] = useState(null);

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

    const role = user.role?.toLowerCase();
    const isRequester = role === "requester";
    const isStaff = role === "staff";
    const screens = [
        ...(isStaff ? [{ id: "requests", label: "Requests" }] : [{ id: "home", label: "Home" }]),
        ...(isRequester ? [{ id: "submit", label: "Submit a request" }] : []),
    ];

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
                            try {
                                await logout();
                            } catch {
                                // The session may already have ended.
                            }
                            setUser(null);
                            setSelectedRequestId(null);
                            setScreen("home");
                        }}
                    >
                        Sign out
                    </button>
                </div>
            </header>

            <nav className="app-nav" aria-label="Main">
                {screens.map((s) => (
                    <button
                        key={s.id}
                        type="button"
                        className="nav-link"
                        aria-current={
                            (isStaff ? s.id === "requests" : screen === s.id) ? "page" : undefined
                        }
                        onClick={() => {
                            setSelectedRequestId(null);
                            setScreen(s.id);
                        }}
                    >
                        {s.label}
                    </button>
                ))}
            </nav>

            <main className="app-main">
                {isStaff && screen === "request" && (
                    <StaffRequestDetail
                        requestId={selectedRequestId}
                        currentUserId={user.id}
                        onBack={() => {
                            setSelectedRequestId(null);
                            setScreen("requests");
                        }}
                    />
                )}
                {isStaff && screen !== "request" && (
                    <StaffRequestList
                        onOpen={(id) => {
                            setSelectedRequestId(id);
                            setScreen("request");
                        }}
                    />
                )}
                {!isStaff && screen === "submit" && isRequester ? (
                    <SubmitRequest />
                ) : null}
                {!isStaff && screen !== "submit" && (
                    <div className="card">
                        <h2 className="page-title">Welcome, {user.name}</h2>
                        <p className="muted">
                            You are signed in as <strong>{user.role}</strong>.
                        </p>
                        <p>
                            This is the temporary home screen.
                            Next we will add the real screens for your role.
                        </p>
                    </div>
                )}
            </main>
        </div>
    );
}