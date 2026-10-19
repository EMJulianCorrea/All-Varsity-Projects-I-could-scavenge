import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getCollection, removeFromCollection } from ".src/services/api.js";

function CollectionPage({ token }) {

    const [collection, setCollection] = useState([]);
    const [status, setStatus] = useState("loading");
    const [error, setError] = useState(null);

    const [removingId, setRemovingId] = useState(null);
    const [removeError, setRemoveError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        async function loadCollection() {
            setStatus("loading");
            setError(null);

            try {
                const result = await getCollection(token);
                if (isMounted) {
                    setCollection(result.data);
                    setStatus("loaded");
                }
            } catch (err) {
                if (isMounted) {
                    setError(err.message);
                    setStatus("error");
                }
            }
        }

        loadCollection();
        return () => { isMounted = false; };
    }, [token]);

    const handleRemove = async (gameId, gameTitle) => {
        const confirmed = window.confirm(
            `Remove "${gameTitle}" from your collection?`
        );
        if (!confirmed) return;

        setRemovingId(gameId);
        setRemoveError(null);

        try {
            await removeFromCollection(gameId, token);
            setCollection((currentCollection) =>
                currentCollection.filter((entry) => entry.game._id !== gameId)
            );
        } catch (err) {
            setRemoveError(err.message);
        } finally {
            setRemovingId(null);
        }
    };

    if (status === "loading") {
        return <p className="games-status">Loading your collection...</p>;
    }

    if (status === "error") {
        return <p className="form-error">{error}</p>;
    }

    return (
        <div className="collection-page">
            <h1>My Collection</h1>

            {removeError && <p className="form-error">{removeError}</p>}

            {collection.length === 0 ? (
                <p className="games-status">
                    You haven't added any games yet. <Link to="/games">Browse Games</Link> to get started.
                </p>
            ) : (
                <div className="games-grid">
                    {collection.map((entry) => (
                        <article key={entry.game._id} className="game-card">
                            <h2 className="game-card-title">{entry.game.title}</h2>

                            <dl className="game-card-meta">
                                <div><dt>Genre</dt><dd>{entry.game.genre}</dd></div>
                                <div><dt>Platform</dt><dd>{entry.game.platform}</dd></div>
                                <div><dt>Age rating</dt><dd>{entry.game.age_rating}</dd></div>
                                <div><dt>Release year</dt><dd>{entry.game.release_year}</dd></div>
                                <div><dt>Availability</dt><dd>{entry.game.availability ? "Available" : "Unavailable"}</dd></div>
                            </dl>

                            <div className="game-card-actions">
                                <Link to={`/games/${entry.game._id}`} className="game-card-button">
                                    View Details
                                </Link>
                                <button
                                    type="button"
                                    className="collection-remove-button"
                                    onClick={() => handleRemove(entry.game._id, entry.game.title)}
                                    disabled={removingId === entry.game._id}
                                >
                                    {removingId === entry.game._id ? "Removing..." : "Remove from Collection"}
                                </button>
                            </div>
                        </article>
                    ))}
                </div>
            )}
        </div>
    );
}

export default CollectionPage;