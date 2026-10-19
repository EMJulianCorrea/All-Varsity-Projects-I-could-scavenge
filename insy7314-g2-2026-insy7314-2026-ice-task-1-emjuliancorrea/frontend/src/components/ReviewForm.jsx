import { useState } from "react";
import { submitReview, updateReview } from ".src/services/api.js";

function ReviewForm({ gameId, token, existingReview, onSubmitted, onCancel }) {

    const isEditing = Boolean(existingReview);

    const [rating, setRating] = useState(existingReview?.rating ?? 5);
    const [comment, setComment] = useState(existingReview?.comment ?? "");
    const [validationError, setValidationError] = useState(null);
    const [submitStatus, setSubmitStatus] = useState("idle"); 
    const [submitError, setSubmitError] = useState(null);

    const handleSubmit = async (event) => {
        event.preventDefault();

        setValidationError(null);

        const trimmedComment = comment.trim();

        if (!trimmedComment) {
            setValidationError("Please write a review before submitting.");
            return;
        }

        if (!Number.isInteger(rating) || rating < 1 || rating > 5) {
            setValidationError("Please choose a rating between 1 and 5.");
            return;
        }

        setSubmitStatus("saving");
        setSubmitError(null);

        try {
            const result = isEditing
                ? await updateReview(gameId, existingReview._id, { rating, comment: trimmedComment }, token)
                : await submitReview(gameId, { rating, comment: trimmedComment }, token);

            if (!isEditing) {
                setRating(5);
                setComment("");
            }

            setSubmitStatus("idle");
            onSubmitted(result.data);

        } catch (err) {
            setSubmitError(err.message);
            setSubmitStatus("error");
        }
    };

    return (
        <form className="review-form" onSubmit={handleSubmit}>
            <h2>{isEditing ? "Edit Your Review" : "Write a Review"}</h2>

            {validationError && <p className="form-error">{validationError}</p>}
            {submitStatus === "error" && <p className="form-error">{submitError}</p>}

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
                <label htmlFor="review-comment">Review</label>
                <textarea
                    id="review-comment"
                    value={comment}
                    onChange={(e) => setComment(e.target.value)}
                    rows={4}
                    placeholder="What did you think of this game?"
                    required
                />
            </div>

            <div className="review-form-actions">
                {onCancel && (
                    <button type="button" className="review-cancel-button" onClick={onCancel}>
                        Cancel
                    </button>
                )}
                <button type="submit" disabled={submitStatus === "saving"}>
                    {submitStatus === "saving"
                        ? "Submitting..."
                        : isEditing ? "Save Changes" : "Submit Review"}
                </button>
            </div>
        </form>
    );
}

export default ReviewForm;