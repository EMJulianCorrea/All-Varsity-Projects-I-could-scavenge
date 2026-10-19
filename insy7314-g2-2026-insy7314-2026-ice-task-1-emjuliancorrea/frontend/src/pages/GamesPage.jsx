import { useEffect, useState } from "react";
import GameCard from "../components/GameCard";
import { getAllGames } from ".src/services/api.js";

function GamesPage() {

    const [games, setGames] = useState([]);
    const [status, setStatus] = useState("loading");
    const [error, setError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        async function loadGames() {
            setStatus("loading");
            setError(null);

            try {
                const result = await getAllGames();

                if (isMounted) {
                    setGames(result.data);
                    setStatus("loaded");
                }
            } catch (err) {
                if (isMounted) {
                    setError(err.message);
                    setStatus("error");
                }
            }
        }

        loadGames();

        return () => {
            isMounted = false;
        };
    }, []);

    return (
        <div className="games-page">
            <h1>Browse Games</h1>

            {status === "loading" && (
                <p className="games-status">Loading games...</p>
            )}

            {status === "error" && (
                <p className="form-error">{error}</p>
            )}

            {status === "loaded" && games.length === 0 && (
                <p className="games-status">
                    No games are available right now. Check back later.
                </p>
            )}

            {status === "loaded" && games.length > 0 && (
                <div className="games-grid">
                    {games.map((game) => (
                        <GameCard key={game._id} game={game} />
                    ))}
                </div>
            )}
        </div>
    );
}

export default GamesPage;