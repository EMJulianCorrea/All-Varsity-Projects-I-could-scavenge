import { useState, useEffect } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Navigation from "../components/Navigation";
import RequireAdminRoute from "../components/admin/GameManagement";
import RegisterForm from "../components/RegisterForm";
import LoginForm from "../components/LoginForm";
import GamesPage from "../pages/GamesPage";
import GameDetailsPage from "../pages/GameDetailsPage";
import CollectionPage from "../pages/CollectionPage";
import ProfilePage from "../pages/ProfilePage";
import AdminDashboardPage from "../pages/AdminDashboardPage";
import { fetchProfile } from ".src/services/api.js";

function App() {

    const [token, setToken] = useState(() => localStorage.getItem("token"));
    const [user, setUser] = useState(null);
    const [isCheckingSession, setIsCheckingSession] = useState(true);
    const [authView, setAuthView] = useState("login");

    useEffect(() => {
        async function restoreSession() {
            if (!token) {
                setIsCheckingSession(false);
                return;
            }

            try {
                const result = await fetchProfile(token);
                setUser(result.user);
            } catch {
                handleLogout();
            } finally {
                setIsCheckingSession(false);
            }
        }

        restoreSession();
    }, []);

    const handleAuthSuccess = (authenticatedUser, authToken) => {
        setUser(authenticatedUser);
        setToken(authToken);
        localStorage.setItem("token", authToken);
    };

    const handleLogout = () => {
        setUser(null);
        setToken(null);
        localStorage.removeItem("token");
        setAuthView("login");
    };

    if (isCheckingSession) {
        return <p>Loading...</p>;
    }

    if (!user) {
        return (
            <div className="auth-card">
                <h1>GameVault</h1>
                <p className="tagline">Track, rate, and revisit your game library.</p>

                <div className="auth-nav">
                    <button
                        type="button"
                        className={authView === "login" ? "active" : ""}
                        onClick={() => setAuthView("login")}
                    >
                        Log In
                    </button>
                    <button
                        type="button"
                        className={authView === "register" ? "active" : ""}
                        onClick={() => setAuthView("register")}
                    >
                        Register
                    </button>
                </div>

                {authView === "login" ? (
                    <LoginForm onLoggedIn={handleAuthSuccess} />
                ) : (
                    <RegisterForm onRegistered={handleAuthSuccess} />
                )}
            </div>
        );
    }

    return (
        <BrowserRouter>
            <Navigation user={user} onLogout={handleLogout} />

            <main>
                <Routes>
                    <Route path="/games" element={<GamesPage />} />
                    <Route path="/games/:id" element={<GameDetailsPage token={token} user={user} />} />
                    <Route path="/collection" element={<CollectionPage token={token} />} />
                    <Route path="/profile" element={<ProfilePage token={token} />} />

                    <Route
                        path="/admin"
                        element={
                            <RequireAdminRoute user={user}>
                                <AdminDashboardPage token={token} />
                            </RequireAdminRoute>
                        }
                    />

                    <Route path="/" element={<Navigate to="/games" replace />} />
                    <Route path="*" element={<Navigate to="/games" replace />} />
                </Routes>
            </main>
        </BrowserRouter>
    );
}

export default App;