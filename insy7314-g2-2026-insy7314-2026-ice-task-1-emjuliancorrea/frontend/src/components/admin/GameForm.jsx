import { useState } from "react";
import { createGame, updateGame } from ".src/services/api.js";

function GameForm({ existingGame, token, onSaved, onCancel }) {

    const isEditing = Boolean(existingGame);

    const [title, setTitle] = useState(existingGame?.title ?? "");
    const [genre, setGenre] = useState(existingGame?.genre ?? "");
    const [platform, setPlatform] = useState(existingGame?.platform ?? "");
    const [releaseYear, setReleaseYear] = useState(existingGame?.release_year ?? "");
    const [ageRating, setAgeRating] = useState(existingGame?.age_rating ?? "E");
    const [available, setAvailable] = useState(existingGame?.availability ?? true);

    const [validationError, setValidationError] = useState(null);
    const [saveStatus, setSaveStatus] = useState("idle");
    const [saveError, setSaveError] = useState(null);

    const RATINGS = ["E", "E10+", "T", "M", "18"];

    const handleSubmit = async (event) => {
        event.preventDefault();

        setValidationError(null);

        const trimmedTitle = title.trim();
        const trimmedGenre = genre.trim();
        const trimmedPlatform = platform.trim();
        const parsedYear = Number(releaseYear);

        if (!trimmedTitle || !trimmedGenre || !trimmedPlatform) {
            setValidationError("Title, genre, and platform are all required.");
            return;
        }

        if (!Number.isInteger(parsedYear)) {
            setValidationError("Release year must be a whole number.");
            return;
        }

        const gamePayload = {
            title: trimmedTitle,
            genre: trimmedGenre,
            platform: trimmedPlatform,
            releaseYear: parsedYear,
            ageRating,
            available
        };

        setSaveStatus("saving");
        setSaveError(null);

        try {
            const result = isEditing
                ? await updateGame(existingGame._id, gamePayload, token)
                : await createGame(gamePayload, token);

            setSaveStatus("idle");
            onSaved(result.data);

        } catch (err) {
            setSaveError(err.message);
            setSaveStatus("error");
        }
    };

    return (
        <form className="auth-form game-form" onSubmit={handleSubmit}>
            <h2>{isEditing ? "Edit Game" : "Add New Game"}</h2>

            {validationError && <p className="form-error">{validationError}</p>}
            {saveStatus === "error" && <p className="form-error">{saveError}</p>}

            <div className="form-field">
                <label htmlFor="game-title">Title</label>
                <input
                    id="game-title"
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                />
            </div>

            <div className="form-field">
                <label htmlFor="game-genre">Genre</label>
                <input
                    id="game-genre"
                    type="text"
                    value={genre}
                    onChange={(e) => setGenre(e.target.value)}
                    required
                />
            </div>

            <div className="form-field">
                <label htmlFor="game-platform">Platform</label>
                <input
                    id="game-platform"
                    type="text"
                    value={platform}
                    onChange={(e) => setPlatform(e.target.value)}
                    required
                />
            </div>

            <div className="form-field">
                <label htmlFor="game-release-year">Release Year</label>
                <input
                    id="game-release-year"
                    type="number"
                    value={releaseYear}
                    onChange={(e) => setReleaseYear(e.target.value)}
                    required
                />
            </div>

            <div className="form-field">
                <label htmlFor="game-age-rating">Age Rating</label>
                <select
                    id="game-age-rating"
                    value={ageRating}
                    onChange={(e) => setAgeRating(e.target.value)}
                >
                    {RATINGS.map((value) => (
                        <option key={value} value={value}>{value}</option>
                    ))}
                </select>
            </div>

            <div className="form-field form-field-checkbox">
                <label htmlFor="game-available">
                    <input
                        id="game-available"
                        type="checkbox"
                        checked={available}
                        onChange={(e) => setAvailable(e.target.checked)}
                    />
                    Available
                </label>
            </div>

            <div className="review-form-actions">
                <button type="button" className="review-cancel-button" onClick={onCancel}>
                    Cancel
                </button>
                <button type="submit" disabled={saveStatus === "saving"}>
                    {saveStatus === "saving" ? "Saving..." : isEditing ? "Save Changes" : "Add Game"}
                </button>
            </div>
        </form>
    );
}

export default GameForm;