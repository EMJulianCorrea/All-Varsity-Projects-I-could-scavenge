import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
    getGameById,
    addToCollection,
    getGameReviews,
    submitReview
} from ".src/services/api.js";

function GameDetailsPage({ token, user }) {

    const { id: gameId } = useParams();

    const [game, setGame] = useState(null);
    const [gameStatus, setGameStatus] = useState("loading");
    const [gameError, setGameError] = useState(null);

    const [reviews, setReviews] = useState([]);
    const [reviewsStatus, setReviewsStatus] = useState("loading");
    const [reviewsError, setReviewsError] = useState(null);

    const [collectionStatus, setCollectionStatus] = useState("idle");
    const [collectionError, setCollectionError] = useState(null);

    const [isReviewFormOpen, setIsReviewFormOpen] = useState(false);
    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState("");
    const [reviewSubmitStatus, setReviewSubmitStatus] = useState("idle");
    const [reviewSubmitError, setReviewSubmitError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        async function loadGame() {
            setGameStatus("loading");
            setGameError(null);

            try {
                const result = await getGameById(gameId);
                if (isMounted) {
                    setGame(result.data);
                    setGameStatus("loaded");
                }
            } catch (err) {
                if (isMounted) {
                    setGameError(err.message);
                    setGameStatus("error");
                }
            }
        }

        loadGame();
        return () => { isMounted = false; };
    }, [gameId]);

    useEffect(() => {
        let isMounted = true;

        async function loadReviews() {
            setReviewsStatus("loading");
            setReviewsError(null);

            try {
                const result = await getGameReviews(gameId);
                if (isMounted) {
                    setReviews(result.data);
                    setReviewsStatus("loaded");
                }
            } catch (err) {
                if (isMounted) {
                    setReviewsError(err.message);
                    setReviewsStatus("error");
                }
            }
        }

        loadReviews();
        return () => { isMounted = false; };
    }, [gameId]);

    const handleAddToCollection = async () => {
        setCollectionStatus("saving");
        setCollectionError(null);

        try {
            await addToCollection(gameId, token);
            setCollectionStatus("added");
        } catch (err) {
            setCollectionError(err.message);
            setCollectionStatus("error");
        }
    };

    const handleReviewSubmit = async (event) => {
        event.preventDefault();

        setReviewSubmitStatus("saving");
        setReviewSubmitError(null);

        try {
            const result = await submitReview(gameId, { rating, comment }, token);
            setReviews((currentReviews) => [result.data, ...currentReviews]);
            setComment("");
            setRating(5);
            setIsReviewFormOpen(false);
            setReviewSubmitStatus("idle");
        } catch (err) {
            setReviewSubmitError(err.message);
            setReviewSubmitStatus("error");
        }
    };

    if (gameStatus === "loading") {
        return <p className="games-status">Loading game...</p>;
    }

    if (gameStatus === "error") {
        return (
            <div className="game-details-page">
                <p className="form-error">{gameError}</p>
                <Link to="/games">&larr; Back to Browse Games</Link>
            </div>
        );
    }

    return (
        <div className="game-details-page">
            <Link to="/games" className="back-link">&larr; Back to Browse Games</Link>
            <h1>{game.title}</h1>
            <dl className="game-details-meta">
                <div><dt>Genre</dt><dd>{game.genre}</dd></div>
                <div><dt>Platform</dt><dd>{game.platform}</dd></div>
                <div><dt>Age rating</dt><dd>{game.age_rating}</dd></div>
                <div><dt>Release year</dt><dd>{game.release_year}</dd></div>
                <div><dt>Availability</dt><dd>{game.availability ? "Available" : "Unavailable"}</dd></div>
            </dl>

            <section className="game-details-section">
                <button
                    type="button"
                    className="collection-button"
                    onClick={handleAddToCollection}
                    disabled={collectionStatus === "saving" || collectionStatus === "added"}
                >
                    {collectionStatus === "added" ? "Added to Collection" : "Add to My Collection"}
                </button>

                {collectionStatus === "error" && (
                    <p className="form-error">{collectionError}</p>
                )}
            </section>

            <section className="game-details-section">
                <h2>Reviews</h2>

                {reviewsStatus === "loading" && (
                    <p className="games-status">Loading reviews...</p>
                )}

                {reviewsStatus === "error" && (
                    <p className="form-error">{reviewsError}</p>
                )}

                {reviewsStatus === "loaded" && reviews.length === 0 && (
                    <p className="games-status">No reviews yet. Be the first to write one.</p>
                )}

                {reviewsStatus === "loaded" && reviews.length > 0 && (
                    <ul className="reviews-list">
                        {reviews.map((review) => (
                            <li key={review._id} className="review-item">
                                <span className="review-rating">{review.rating} / 5</span>
                                <p className="review-comment">{review.comment}</p>
                                {review.authorName && (
                                    <span className="review-author">— {review.authorName}</span>
                                )}
                            </li>
                        ))}
                    </ul>
                )}
            </section>

            <section className="game-details-section">
                {!isReviewFormOpen ? (
                    <button
                        type="button"
                        className="review-toggle-button"
                        onClick={() => setIsReviewFormOpen(true)}
                    >
                        Write a Review
                    </button>
                ) : (
                    <form className="review-form" onSubmit={handleReviewSubmit}>
                        <h2>Write a Review</h2>

                        {reviewSubmitStatus === "error" && (
                            <p className="form-error">{reviewSubmitError}</p>
                        )}

                        <div className="form-field">
                            <label htmlFor="review-rating">Rating</label>
                            <select
                                id="review-rating"
                                value={rating}
                                onChange={(e) => setRating(Number(e.target.value))}
                            >
                                {[5, 4, 3, 2, 1].map((value) => (
                                    <option key={value} value={value}>{value} / 5</option>
                                ))}
                            </select>
                        </div>

                        <div className="form-field">
                            <label htmlFor="review-comment">Comment</label>
                            <textarea
                                id="review-comment"
                                value={comment}
                                onChange={(e) => setComment(e.target.value)}
                                rows={4}
                                required
                            />
                        </div>

                        <div className="review-form-actions">
                            <button
                                type="button"
                                className="review-cancel-button"
                                onClick={() => setIsReviewFormOpen(false)}
                            >
                                Cancel
                            </button>
                            <button type="submit" disabled={reviewSubmitStatus === "saving"}>
                                {reviewSubmitStatus === "saving" ? "Submitting..." : "Submit Review"}
                            </button>
                        </div>
                    </form>
                )}
            </section>
        </div>
    );
}

export default GameDetailsPage;