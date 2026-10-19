import { useEffect, useState } from "react";
import { fetchProfile, updateProfile } from ".src/services/api.js";

function ProfilePage({ token }) {

    const [profile, setProfile] = useState(null);
    const [status, setStatus] = useState("loading");
    const [error, setError] = useState(null);

    const [isEditing, setIsEditing] = useState(false);
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [validationError, setValidationError] = useState(null);
    const [saveStatus, setSaveStatus] = useState("idle");
    const [saveError, setSaveError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        async function loadProfile() {
            setStatus("loading");
            setError(null);

            try {
                const result = await fetchProfile(token);
                if (isMounted) {
                    setProfile(result.user);
                    setStatus("loaded");
                }
            } catch (err) {
                if (isMounted) {
                    setError(err.message);
                    setStatus("error");
                }
            }
        }

        loadProfile();
        return () => { isMounted = false; };
    }, [token]);

    const startEditing = () => {
        setName(profile.name);
        setEmail(profile.email);
        setValidationError(null);
        setSaveError(null);
        setIsEditing(true);
    };

    const cancelEditing = () => {
        setIsEditing(false);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        setValidationError(null);

        const trimmedName = name.trim();
        const trimmedEmail = email.trim();

        if (!trimmedName || !trimmedEmail) {
            setValidationError("Name and email are both required.");
            return;
        }

        setSaveStatus("saving");
        setSaveError(null);

        try {
            const result = await updateProfile(
                { name: trimmedName, email: trimmedEmail },
                token
            );
            setProfile(result.user);
            setIsEditing(false);
            setSaveStatus("idle");
        } catch (err) {
            setSaveError(err.message);
            setSaveStatus("error");
        }
    };

    if (status === "loading") {
        return <p className="games-status">Loading your profile...</p>;
    }

    if (status === "error") {
        return <p className="form-error">{error}</p>;
    }

    return (
        <div className="profile-page">
            <h1>My Profile</h1>

            {!isEditing ? (
                <div className="profile-card">
                    <dl className="profile-details">
                        <div>
                            <dt>Name</dt>
                            <dd>{profile.name}</dd>
                        </div>
                        <div>
                            <dt>Email</dt>
                            <dd>{profile.email}</dd>
                        </div>
                        <div>
                            <dt>Role</dt>
                            <dd>{profile.role}</dd>
                        </div>
                    </dl>

                    <button type="button" onClick={startEditing}>
                        Edit Profile
                    </button>
                </div>
            ) : (
                <form className="auth-form profile-form" onSubmit={handleSubmit}>
                    {validationError && <p className="form-error">{validationError}</p>}
                    {saveStatus === "error" && <p className="form-error">{saveError}</p>}

                    <div className="form-field">
                        <label htmlFor="profile-name">Name</label>
                        <input
                            id="profile-name"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-field">
                        <label htmlFor="profile-email">Email</label>
                        <input
                            id="profile-email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                    <p className="profile-role-note">
                        Role: {profile.role} (cannot be changed here)
                    </p>

                    <div className="review-form-actions">
                        <button type="button" className="review-cancel-button" onClick={cancelEditing}>
                            Cancel
                        </button>
                        <button type="submit" disabled={saveStatus === "saving"}>
                            {saveStatus === "saving" ? "Saving..." : "Save Changes"}
                        </button>
                    </div>
                </form>
            )}
        </div>
    );
}

export default ProfilePage;