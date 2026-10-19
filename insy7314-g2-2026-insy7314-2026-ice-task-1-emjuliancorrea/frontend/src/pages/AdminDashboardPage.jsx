import { useEffect, useState } from "react";
import { getAllGames, deleteGame } from "../services/api";
import GameForm from "../components/admin/GameForm";

function AdminDashboardPage({ token }) {

    const [games, setGames] = useState([]);
    const [status, setStatus] = useState("loading");
    const [error, setError] = useState(null);

    const [formMode, setFormMode] = useState(null);
    const [deletingId, setDeletingId] = useState(null);
    const [deleteError, setDeleteError] = useState(null);

    const loadGames = async () => {
        setStatus("loading");
        setError(null);

        try {
            const result = await getAllGames();
            setGames(result.data);
            setStatus("loaded");
        } catch (err) {
            setError(err.message);
            setStatus("error");
        }
    };

    useEffect(() => {
        loadGames();
    }, []);

    const handleGameSaved = () => {
        setFormMode(null);
        loadGames();
    };

    const handleDelete = async (game) => {
        const confirmed = window.confirm(`Delete "${game.title}"? This cannot be undone.`);
        if (!confirmed) return;

        setDeletingId(game._id);
        setDeleteError(null);

        try {
            await deleteGame(game._id, token);
            setGames((current) => current.filter((g) => g._id !== game._id));
        } catch (err) {
            setDeleteError(err.message);
        } finally {
            setDeletingId(null);
        }
    };

    return (
        <div className="admin-dashboard-page">
            <div className="admin-dashboard-header">
                <h1>Admin Dashboard</h1>
                <button
                    type="button"
                    className="add-game-button"
                    onClick={() => setFormMode("add")}
                >
                    Add New Game
                </button>
            </div>

            {formMode && (
                <GameForm
                    existingGame={formMode === "add" ? null : formMode}
                    token={token}
                    onSaved={handleGameSaved}
                    onCancel={() => setFormMode(null)}
                />
            )}

            {deleteError && <p className="form-error">{deleteError}</p>}

            {status === "loading" && (
                <p className="games-status">Loading games...</p>
            )}

            {status === "error" && (
                <p className="form-error">{error}</p>
            )}

            {status === "loaded" && games.length === 0 && (
                <p className="games-status">No games in the library yet.</p>
            )}

            {status === "loaded" && games.length > 0 && (
                <table className="admin-games-table">
                    <thead>
                        <tr>
                            <th>Title</th>
                            <th>Genre</th>
                            <th>Platform</th>
                            <th>Release Year</th>
                            <th>Age Rating</th>
                            <th>Available</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {games.map((game) => (
                            <tr key={game._id}>
                                <td>{game.title}</td>
                                <td>{game.genre}</td>
                                <td>{game.platform}</td>
                                <td>{game.release_year}</td>
                                <td>{game.age_rating}</td>
                                <td>{game.availability ? "Yes" : "No"}</td>
                                <td className="admin-games-actions">
                                    <button
                                        type="button"
                                        className="admin-edit-button"
                                        onClick={() => setFormMode(game)}
                                    >
                                        Edit
                                    </button>
                                    <button
                                        type="button"
                                        className="admin-delete-button"
                                        onClick={() => handleDelete(game)}
                                        disabled={deletingId === game._id}
                                    >
                                        {deletingId === game._id ? "Deleting..." : "Delete"}
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

export default AdminDashboardPage;