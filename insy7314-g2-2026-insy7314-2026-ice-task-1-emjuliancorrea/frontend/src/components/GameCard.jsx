import { useNavigate } from "react-router-dom";

function GameCard({ game }) {

    const navigate = useNavigate();

    return (
        <article className="game-card">
            <h2 className="game-card-title">{game.title}</h2>

            <dl className="game-card-meta">
                <div>
                    <dt>Genre</dt>
                    <dd>{game.genre}</dd>
                </div>
                <div>
                    <dt>Platform</dt>
                    <dd>{game.platform}</dd>
                </div>
                <div>
                    <dt>Age rating</dt>
                    <dd>{game.age_rating}</dd>
                </div>
                <div>
                    <dt>Release year</dt>
                    <dd>{game.release_year}</dd>
                </div>
                <div>
                    <dt>Availability</dt>
                    <dd>{game.availability ? "Available" : "Unavailable"}</dd>
                </div>
            </dl>

            <button
                type="button"
                className="game-card-button"
                onClick={() => navigate(`/games/${game._id}`)}
            >
                View Details
            </button>
        </article>
    );
}

export default GameCard;