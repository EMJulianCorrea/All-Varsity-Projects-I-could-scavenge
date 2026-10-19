function RequireAdminRoute({ user, children }) {

    if (!user || user.role !== "admin") {
        return <Navigate to="/games" replace />;
    }

    return children;
}

export default RequireAdminRoute;