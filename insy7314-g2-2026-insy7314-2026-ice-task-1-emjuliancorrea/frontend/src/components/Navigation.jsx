import { NavLink } from "react-router-dom";

function Navigation({ user, onLogout }) {

    const navLinkClassName = ({ isActive }) =>
        isActive ? "nav-link active" : "nav-link";

    return (
        <nav className="main-nav">
            <NavLink to="/games" className={navLinkClassName}>
                Browse Games
            </NavLink>

            <NavLink to="/collection" className={navLinkClassName}>
                My Collection
            </NavLink>

            <NavLink to="/profile" className={navLinkClassName}>
                My Profile
            </NavLink>

            {user.role === "admin" && (
                <NavLink to="/admin" className={navLinkClassName}>
                    Admin Dashboard
                </NavLink>
            )}

            <button type="button" className="nav-link nav-logout" onClick={onLogout}>
                Logout
            </button>
        </nav>
    );
}

export default Navigation;