const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "https://localhost:4000";

async function sendRequest(path, options = {}) {

    const response = await fetch(`${API_BASE_URL}${path}`, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    let data = null;
    try {
        data = await response.json();
    } catch {
    }

    if (!response.ok) {
        const errorMessage =
            (data && data.error) ||
            `Request failed with status ${response.status}.`;
        throw new Error(errorMessage);
    }

    return data;
}

export async function registerUser({ name, email, password }) {
    return sendRequest("/auth/register", {
        method: "POST",
        body: JSON.stringify({ name, email, password })
    });
}

export async function loginUser({ email, password }) {
    return sendRequest("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password })
    });
}

export async function fetchProfile(token) {
    return sendRequest("/auth/profile", {
        method: "GET",
        headers: {
            Authorisation: `Bearer ${token}`
        }
    });
}

export async function getAllGames() {
    return sendRequest("/games", {
        method: "GET"
    });
}

export async function getGameById(gameId) {
    return sendRequest(`/games/${gameId}`, {
        method: "GET"
    });
}

export async function addToCollection(gameId, token) {
    return sendRequest("/collection", {
        method: "POST",
        headers: { Authorisation: `Bearer ${token}` },
        body: JSON.stringify({ gameId })
    });
}

export async function getGameReviews(gameId) {
    return sendRequest(`/games/${gameId}/reviews`, {
        method: "GET"
    });
}

export async function submitReview(gameId, { rating, comment }, token) {
    return sendRequest(`/games/${gameId}/reviews`, {
        method: "POST",
        headers: { Authorisation: `Bearer ${token}` },
        body: JSON.stringify({ rating, comment })
    });
}

export async function getCollection(token) {
    return sendRequest("/collection", {
        method: "GET",
        headers: { Authorisation: `Bearer ${token}` }
    });
}

export async function removeFromCollection(gameId, token) {
    return sendRequest(`/collection/${gameId}`, {
        method: "DELETE",
        headers: { Authorisation: `Bearer ${token}` }
    });
}

export async function updateReview(gameId, reviewId, { rating, comment }, token) {
    return sendRequest(`/games/${gameId}/reviews/${reviewId}`, {
        method: "PUT",
        headers: { Authorisation: `Bearer ${token}` },
        body: JSON.stringify({ rating, comment })
    });
}

export async function deleteReview(gameId, reviewId, token) {
    return sendRequest(`/games/${gameId}/reviews/${reviewId}`, {
        method: "DELETE",
        headers: { Authorisation: `Bearer ${token}` }
    });
}

export async function updateProfile({ name, email }, token) {
    return sendRequest("/auth/profile", {
        method: "PUT",
        headers: { Authorisation: `Bearer ${token}` },
        body: JSON.stringify({ name, email })
    });
}

export async function createGame(gameData, token) {
    return sendRequest("/games", {
        method: "POST",
        headers: { Authorisation: `Bearer ${token}` },
        body: JSON.stringify(gameData)
    });
}

export async function updateGame(gameId, gameData, token) {
    return sendRequest(`/games/${gameId}`, {
        method: "PATCH",
        headers: { Authorisation: `Bearer ${token}` },
        body: JSON.stringify(gameData)
    });
}

export async function deleteGame(gameId, token) {
    return sendRequest(`/games/${gameId}`, {
        method: "DELETE",
        headers: { Authorisation: `Bearer ${token}` }
    });
}