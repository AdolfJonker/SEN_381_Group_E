import { useEffect, useState } from "react";
import LoginScreen from "./views/LoginScreen.jsx";
import SubmitRequest from "./views/SubmitRequest.jsx";
import MyRequests from "./views/MyRequests.jsx";
import RequestDetail from "./views/RequestDetail.jsx";
import ManagementSummary from "./views/ManagementSummary.jsx";
import StaffRequestList from "./views/StaffRequestList.jsx";
import StaffRequestDetail from "./views/StaffRequestDetail.jsx";
import { BrandMark, CategoryIcon, Icon, PinTile } from "./views/Visuals.jsx";
import { fetchCurrentUser, fetchMyRequests, logout } from "./services/client.js";
import { landingViewFor } from "./services/roleLanding.js";
import { CATEGORIES } from "./services/requestOptions.js";
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
    const [startCategory, setStartCategory] = useState("");
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
        { id: "home", label: "Home", icon: "home" },
        ...(isRequester
            ? [
                  { id: "submit", label: "Submit a request", icon: "plus" },
                  { id: "mine", label: "My requests", icon: "list" },
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

    function openSubmit(category = "") {
        setStartCategory(category);
        navigateTo("submit");
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

    // ----- Logged-in App Shell -----
    return (
        <div className={`app-shell${screens.length > 1 ? " has-tabs" : ""}`}>
            <header className="app-header">
                <div className="brand">
                    <BrandMark />
                    <h1>CivicConnect</h1>
                </div>
                <div className="user-info">
                    <span className="avatar" aria-hidden="true">
                        {(user.name || "?").trim().charAt(0).toUpperCase()}
                    </span>
                    <span className="user-text">
                        <span className="user-name">{user.name}</span>
                        <span className="role-chip">{user.role}</span>
                    </span>
                    <button
                        type="button"
                        className="btn-ghost signout"
                        onClick={handleLogout}
                    >
                        <Icon name="logout" />
                        <span>Sign out</span>
                    </button>
                </div>
            </header>

            {screens.length > 1 && (
                <nav className="app-nav" aria-label="Main">
                    {screens.map((s) => {
                        const badgeCount = s.id === "mine" ? unseenUpdates : 0;

                        return (
                            <button
                                key={s.id}
                                type="button"
                                className="nav-link"
                                aria-current={screen === s.id ? "page" : undefined}
                                onClick={() => (s.id === "submit" ? openSubmit("") : navigateTo(s.id))}
                            >
                                <Icon name={s.icon} />
                                {s.label}
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
            )}

            <main className="app-main">
                {screen === "submit" && isRequester ? (
                    <SubmitRequest key={startCategory || "blank"} initialCategory={startCategory} />
                ) : screen === "mine" && isRequester ? (
                    selectedRequestId != null ? (
                        <RequestDetail
                            requestId={selectedRequestId}
                            onBack={() => setSelectedRequestId(null)}
                        />
                    ) : (
                        <MyRequests
                            onSubmitClick={() => openSubmit("")}
                            onSelectRequest={setSelectedRequestId}
                        />
                    )
                ) : (
                    <RoleLanding
                        user={user}
                        selectedRequestId={selectedRequestId}
                        onSelectRequest={setSelectedRequestId}
                        onClearSelection={() => setSelectedRequestId(null)}
                        onReport={openSubmit}
                        onTrack={() => navigateTo("mine")}
                    />
                )}
            </main>
        </div>
    );
}

/**
 * Role-based landing view (NFR003). The role comes from the server (/auth/me).
 * Hiding screens here is for usability only; the server checks every request.
 */
function RoleLanding({ user, selectedRequestId, onSelectRequest, onClearSelection, onReport, onTrack }) {
    const landing = landingViewFor(user.role);

    if ((landing === "staff-list" || landing === "management-summary") && selectedRequestId != null) {
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
            return <ManagementSummary user={user} onSelect={onSelectRequest} />;
        case "staff-list":
            return <StaffRequestList user={user} onSelect={onSelectRequest} />;
        case "my-requests":
            return <RequesterHome user={user} onReport={onReport} onTrack={onTrack} />;
        default:
            return (
                <div className="card" role="alert">
                    <h2 className="page-title">No access</h2>
                    <p className="muted">Your account has no CivicConnect role. Contact an administrator.</p>
                </div>
            );
    }
}

/** Requester home: a short welcome, one big call to action and quick category picks. */
function RequesterHome({ user, onReport, onTrack }) {
    const firstName = (user.name || "").trim().split(/\s+/)[0] || "there";
    return (
        <div className="view-stack">
            <section className="greeting">
                <p className="eyebrow">Welcome back</p>
                <h2 className="hello">Hi {firstName}, what needs fixing today?</h2>
            </section>

            <section className="promo-card" aria-labelledby="promo-heading">
                <div className="promo-text">
                    <h3 id="promo-heading">Spotted a problem?</h3>
                    <p>Report it in under a minute. We keep you posted as it gets sorted.</p>
                    <button type="button" className="btn-light" onClick={() => onReport("")}>
                        Report a problem
                        <Icon name="arrow" />
                    </button>
                </div>
                <div className="promo-art" aria-hidden="true">
                    <span className="bubble b1" />
                    <span className="bubble b2" />
                    <PinTile />
                </div>
            </section>

            <section className="card" aria-labelledby="cat-heading">
                <div className="section-head">
                    <h3 id="cat-heading" className="section-title">Pick a category</h3>
                </div>
                <div className="cat-grid">
                    {CATEGORIES.map((c) => (
                        <button key={c} type="button" className="cat-tile" onClick={() => onReport(c)}>
                            <CategoryIcon category={c} />
                            <span>{c}</span>
                        </button>
                    ))}
                </div>
            </section>

            <button type="button" className="track-card" onClick={onTrack}>
                <span className="track-icon">
                    <Icon name="clock" />
                </span>
                <span className="track-text">
                    <strong>Track progress</strong>
                    <span className="muted">See the status of everything you reported</span>
                </span>
                <Icon name="arrow" />
            </button>
        </div>
    );
}
