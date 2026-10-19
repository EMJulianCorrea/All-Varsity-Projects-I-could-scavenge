const mongoose = require("mongoose");

const RATINGS = ["E", "E10+", "T", "M", "18"];

const currentYear = new Date().getFullYear();

const gameSchema = new mongoose.Schema(
    {

        title: {
            type: String,
            required: [true, "Title is required."],
            trim: true,
            minlength: [2, "Title must be at least 2 characters long."],
            maxlength: [100, "Title cannot exceed 100 characters."]
        },

        genre: {
            type: String,
            required: [true, "Genre is required."],
            trim: true,
            minlength: [2, "Genre must be at least 2 characters long."],
            maxlength: [50, "Genre cannot exceed 50 characters."]
        },

        platform: {
            type: String,
            required: [true, "Platform is required."],
            trim: true,
            minlength: [2, "Platform must be at least 2 characters long."],
            maxlength: [50, "Platform cannot exceed 50 characters."]
        },

        release_year: {
            type: Number,
            required: [true, "Release year is required."],
            min: [1950, "Release year cannot be before 1950."],
            max: [currentYear + 2, `Release year cannot be later than ${currentYear + 2}.`]
        },
        age_rating: {
            type: String,
            required: [true, "Age rating is required."],
            trim: true,
            uppercase: true,
            enum: {
                values: RATINGS,
                message: "{VALUE} is not a recognised rating."
            },
        },
        availability: {
            type: Boolean,
            default: true
        },
    },
    {
        timestamps: true
    }
);

const Game = mongoose.model("Game", gameSchema);

module.exports = Game;