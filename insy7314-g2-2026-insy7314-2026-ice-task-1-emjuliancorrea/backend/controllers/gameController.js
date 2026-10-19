const games = require("../models/Game")
const mongoose = require("mongoose");

function isValidObjectId(id) {
    return mongoose.Types.ObjectId.isValid(id);
}

const getAllGames = async (req, res, next) => {
    try {
        const games = await Game.find();

        return res.status(200).json({
            count: games.length,
            data: games
        });

    } catch (err) {
        return next(err);
    }
};

const getGameById = async (req, res, next) => {
    try {
        const gameId = req.params.id;

        if (!isValidObjectId(gameId)) {
            return res.status(400).json({
                error: "Game ID must be a valid ID."
            });
        }

        const game = await Game.findById(gameId);

        if (!game) {
            return res.status(404).json({
                error: "Game not found."
            });
        }

        return res.status(200).json({
            data: game
        });

    } catch (err) {
        return next(err);
    }
};

const createGame = async (req, res, next) => {
    try {
        const {
            title,
            genre,
            platform,
            releaseYear,
            ageRating
        } = req.validateGame;

        const newGame = new Game({
            title,
            genre,
            platform,
            release_year: releaseYear,
            age_rating: ageRating
        });

        await newGame.save();

        return res.status(201).json({
            message: "Game created successfully.",
            data: newGame
        });

    } catch (err) {
        return next(err);
    }
};

const replaceGame = async (req, res, next) => {
    try {
        const gameId = req.params.id;

        if (!isValidObjectId(gameId)) {
            return res.status(400).json({
                error: "Game ID must be a valid ID."
            });
        }

        const {
            title,
            genre,
            platform,
            releaseYear,
            ageRating,
            available
        } = req.validateGame;

        const updatedGame = await Game.findByIdAndUpdate(
            gameId,
            {
                title,
                genre,
                platform,
                release_year: releaseYear,
                age_rating: ageRating,
                availability: available
            },
            {
                new: true,
                runValidators: true,
                overwrite: true
            }
        );

        if (!updatedGame) {
            return res.status(404).json({
                error: "Game not found."
            });
        }

        return res.status(200).json({
            message: "Game replaced successfully.",
            data: updatedGame
        });

    } catch (err) {
        return next(err);
    }
};

const updateGame = async (req, res, next) => {
    try {
        const gameId = req.params.id;

        if (!isValidObjectId(gameId)) {
            return res.status(400).json({
                error: "Game ID must be a valid ID."
            });
        }

        const {
            title,
            genre,
            platform,
            releaseYear,
            ageRating,
            available
        } = req.validateGame;

        const updates = {};
        if (title !== undefined) updates.title = title;
        if (genre !== undefined) updates.genre = genre;
        if (platform !== undefined) updates.platform = platform;
        if (releaseYear !== undefined) updates.release_year = releaseYear;
        if (ageRating !== undefined) updates.age_rating = ageRating;
        if (available !== undefined) updates.availability = available;

        const updatedGame = await Game.findByIdAndUpdate(
            gameId,
            updates,
            {
                new: true,
                runValidators: true
            }
        );

        if (!updatedGame) {
            return res.status(404).json({
                error: "Game not found."
            });
        }

        return res.status(200).json({
            message: "Game updated successfully.",
            data: updatedGame
        });

    } catch (err) {
        return next(err);
    }
};

const deleteGame = async (req, res, next) => {
    try {
        const gameId = req.params.id;

        if (!isValidObjectId(gameId)) {
            return res.status(400).json({
                error: "Game ID must be a valid ID."
            });
        }

        const deletedGame = await Game.findByIdAndDelete(gameId);

        if (!deletedGame) {
            return res.status(404).json({
                error: "Game not found."
            });
        }

        return res.status(200).json({
            message: "Game deleted successfully.",
            data: deletedGame
        });

    } catch (err) {
        return next(err);
    }
};

module.exports = {
    getAllGames,
    getGameById,
    createGame,
    replaceGame,
    updateGame,
    deleteGame
};