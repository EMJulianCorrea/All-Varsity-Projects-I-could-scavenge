const mongoose = require("mongoose");

const gigSchema = new mongoose.Schema(
    {
        title: {
            type: String,
            required: [true, "Title is required."],
            trim: true,
            minlength: [3, "Title must be at least 3 characters."],
            maxlength: [100, "Title cannot exceed 100 characters."],
        },

        description: {
            type: String,
            required: [true, "Description is required."],
            trim: true,
            minlength: [10, "Description must be at least 10 characters."],
            maxlength: [2000, "Description cannot exceed 2000 characters."],
        },

        category: {
            type: String,
            required: [true, "Category is required."],
            trim: true,
            minlength: [2, "Category must be at least 2 characters long."],
            maxlength: [50, "Category cannot exceed 50 characters."],
        },

        price: {
            type: Number,
            required: [true, "Price is required."],
            min: [0, "Price cannot be negative."],
        },

        freelancer: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "User",
            required: true,
            index: true,
        },

        isActive: {
            type: Boolean,
            default: true,
        },
    },
    {
        timestamps: true,
    }
);

const Gig = mongoose.model("Gig", gigSchema);

module.exports = Gig;