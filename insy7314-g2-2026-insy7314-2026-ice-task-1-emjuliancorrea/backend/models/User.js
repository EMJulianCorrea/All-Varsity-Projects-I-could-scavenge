const mongoose = require("mongoose");

const ROLES = ["user", "admin"];

const userSchema = new mongoose.Schema(
    {

        name: {
            type: String,
            required: [true, "Name is required."],
            trim: true,
            minlength: [2, "Name must be at least 2 characters long."],
            maxlength: [50, "Name cannot exceed 50 characters."]
        },

        email: {
            type: String,
            required: [true, "Email is required."],
            trim: true,
            lowercase: true,
            unique: true
        },

        passwordHash: {
            type: String,
            required: [true, "Password hash is required."],
            select: false
        },

        role: {
            type: String,
            enum: {
                values: ROLES,
                message: "{VALUE} is not a recognised role."
            },
            default: "user"
        }
    },
    {
        timestamps: true
    }
);

const User = mongoose.model("User", userSchema);

module.exports = User;