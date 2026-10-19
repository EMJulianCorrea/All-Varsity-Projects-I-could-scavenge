import { useState } from "react";
import { registerUser } from ".src/services/api.js";

function RegisterForm({ onRegistered }) {

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();

        setError(null);
        setIsSubmitting(true);

        try {
            const result = await registerUser({ name, email, password });
            onRegistered(result.user, result.token);
        } catch (err) {
            setError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <h2>Register</h2>

            {error && <p role="alert">{error}</p>}

            <label htmlFor="register-name">Name</label>
            <input
                id="register-name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
            />

            <label htmlFor="register-email">Email</label>
            <input
                id="register-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
            />

            <label htmlFor="register-password">Password</label>
            <input
                id="register-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
            />

            <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Registering..." : "Register"}
            </button>
        </form>
    );
}

export default RegisterForm;