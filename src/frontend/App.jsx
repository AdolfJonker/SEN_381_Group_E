import { useEffect, useState } from "react";
import LoginScreen from "./views/LoginScreen.jsx";
import SubmitRequest from "./views/SubmitRequest.jsx";
import MyRequests from "./views/MyRequests.jsx";
import RequestDetail from "./views/RequestDetail.jsx";
import ManagementSummary from "./views/ManagementSummary.jsx";
import StaffRequestList from "./views/StaffRequestList.jsx";
import StaffRequestDetail from "./views/StaffRequestDetail.jsx";
import { fetchCurrentUser, fetchMyRequests, logout } from "./services/client.js";
import { landingViewFor } from "./services/roleLanding.js";
import { detectStatusChanges } from "./services/statusChanges.js";

const UPDATE_CHECK_MS = 4000;

// Last-seen statuses are kept per user in localStorage so changes made while
// the requester was signed out still show on their next sign-in.
function seenStorageKey(userId) {
    return `civicconnect.seenStatuses.${userId}`;
}

function readSeenStatuses(userId) {
    try {
        const raw = window.localStorage.getItem(seenStorageKey(userId));
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

function writeSeenStatuses(userId, list) {
    try {
        const slim = list.map((r) => ({ id: r.id, status: r.status }));
        window.localStorage.setItem(seenStorageKey(userId), JSON.stringify(slim));
    } catch {
        // Storage unavailable: the badge falls back to this session only.
    }
}

export default function App() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [screen, setScreen] = useState("home");
    const [selectedRequestId, setSelectedRequestId] = useState(null);
    const [unseenUpdates, setUnseenUpdates] = useState(0);

    const requesterId =
        user?.role?.toLowerCase() === "requester" ? user.id ?? user.email : null;

    // Badge on "My requests" when a status changed since the requester last viewed the list.
    useEffect(() => {
        if (requesterId == null) {
            setUnseenUpdates(0);
            return undefined;
        }

        let cancelled = false;

        async function checkForUpdates() {
            try {
                const data = await fetchMyRequests();
                if (cancelled) {
                    return;
                }
                const list = Array.isArray(data) ? data : [];
                const seen = readSeenStatuses(requesterId);

                if (screen === "mine" || seen === null) {
                    writeSeenStatuses(requesterId, list);
                    setUnseenUpdates(0);
                    return;
                }

                setUnseenUpdates(detectStatusChanges(seen, list).length);
            } catch {
                // A missed check is retried on the next interval.
            }
        }

        checkForUpdates();
        const id = setInterval(checkForUpdates, UPDATE_CHECK_MS);

        return () => {
            cancelled = true;
            clearInterval(id);
        };
    }, [requesterId, screen]);

    useEffect(() => {
        let cancelled = false;

        fetchCurrentUser()
            .then((currentUser) => {
                if (!cancelled) {
                    setUser(currentUser);
                }
            })
            .catch(() => {
                if (!cancelled) {
                    setUser(null);
                }
            })
            .finally(() => {
                if (!cancelled) {
                    setLoading(false);
                }
            });

        return () => {
            cancelled = true;
        };
    }, []);

    if (loading) {
        return (
            <main className="page login-page">
                <p className="muted" role="status">
                    Loading...
                </p>
            </main>
        );
    }

    if (!user) {
        return <LoginScreen onLogin={setUser} />;
    }

    const isRequester = user.role?.toLowerCase() === "requester";

    const screens = [
        { id: "home", label: "Home" },
        ...(isRequester
            ? [
                { id: "submit", label: "Submit a request" },
                { id: "mine", label: "My requests" },
            ]
            : []),
    ];

    function navigateTo(screenId) {
        setScreen(screenId);
        setSelectedRequestId(null);
        if (screenId === "mine") {
            setUnseenUpdates(0);
        }
    }

    async function handleLogout() {
        try {
            await logout();
        } catch {
            // The session may already have ended.
        }

        setUser(null);
        setScreen("home");
        setSelectedRequestId(null);
    }

    return (
        <div className="app-shell">
            <header className="app-header">
                <h1>CivicConnect</h1>

                <div className="user-info">
                    {user.name} ({user.role})
                    <button
                        type="button"
                        className="btn-outline"
                        style={{
                            marginLeft: "1rem",
                            padding: "0.4rem 0.8rem",
                            fontSize: "0.85rem",
                        }}
                        onClick={handleLogout}
                    >
                        Sign out
                    </button>
                </div>
            </header>

            <nav className="app-nav" aria-label="Main">
                {screens.map((item) => {
                    const badgeCount = item.id === "mine" ? unseenUpdates : 0;

                    return (
                        <button
                            key={item.id}
                            type="button"
                            className="nav-link"
                            aria-current={screen === item.id ? "page" : undefined}
                            onClick={() => navigateTo(item.id)}
                        >
                            {item.label}
                            {badgeCount > 0 && (
                                <span className="nav-badge" role="status">
                                    {badgeCount}
                                    <span className="visually-hidden">
                                        {badgeCount === 1 ? " status update" : " status updates"}
                                    </span>
                                </span>
                            )}
                        </button>
                    );
                })}
            </nav>

            <main className="app-main">
                {screen === "submit" && isRequester ? (
                    <SubmitRequest />
                ) : screen === "mine" && isRequester ? (
                    selectedRequestId != null ? (
                        <RequestDetail
                            requestId={selectedRequestId}
                            onBack={() => setSelectedRequestId(null)}
                        />
                    ) : (
                        <MyRequests
                            onSubmitClick={() => navigateTo("submit")}
                            onSelectRequest={setSelectedRequestId}
                        />
                    )
                ) : (
                    <RoleLanding
                        user={user}
                        selectedRequestId={selectedRequestId}
                        onSelectRequest={setSelectedRequestId}
                        onClearSelection={() => setSelectedRequestId(null)}
                    />
                )}
            </main>
        </div>
    );
}

// Role-based landing view (NFR003).
// The role comes from the server (/auth/me).
// Hiding screens is for usability; the server must enforce access control.
function RoleLanding({
                         user,
                         selectedRequestId,
                         onSelectRequest,
                         onClearSelection,
                     }) {
    const landing = landingViewFor(user.role);

    if (
        (landing === "staff-list" || landing === "management-summary") &&
        selectedRequestId != null
    ) {
        return (
            <StaffRequestDetail
                requestId={selectedRequestId}
                currentUserId={user.id}
                onBack={onClearSelection}
            />
        );
    }

    switch (landing) {
        case "management-summary":
            return <ManagementSummary onSelect={onSelectRequest} />;

        case "staff-list":
            return <StaffRequestList onSelect={onSelectRequest} />;

        case "my-requests":
            return (
                <div className="card">
                    <h2 style={{ marginTop: 0, color: "var(--navy)" }}>
                        Welcome, {user.name}
                    </h2>
                    <p className="muted">
                        You are signed in as <strong>{user.role}</strong>.
                    </p>
                </div>
            );

        default:
            return (
                <div className="card" role="alert">
                    <h2 style={{ marginTop: 0, color: "var(--navy)" }}>
                        No access
                    </h2>
                    <p className="muted">
                        Your account has no CivicConnect role. Contact an
                        administrator.
                    </p>
                </div>
            );
    }
}