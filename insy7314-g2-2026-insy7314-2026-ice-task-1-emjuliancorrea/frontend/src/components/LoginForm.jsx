import { useState } from "react";
import { loginUser } from ".src/services/api.js";

function LoginForm({ onLoggedIn }) {

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError(null);
        setIsSubmitting(true);

        try {
            const result = await loginUser({ email, password });
            onLoggedIn(result.user, result.token);
        } catch (err) {
            setError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <h2>Log In</h2>

            {error && <p role="alert">{error}</p>}

            <label htmlFor="login-email">Email</label>
            <input
                id="login-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
            />

            <label htmlFor="login-password">Password</label>
            <input
                id="login-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
            />

            <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Logging in..." : "Log In"}
            </button>
        </form>
    );
}

export default LoginForm;