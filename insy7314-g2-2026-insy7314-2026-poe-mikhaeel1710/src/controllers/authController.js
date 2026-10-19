// Handles registration, login and profile logic using MongoDB.

const User = require("../models/User");

const {
    hashPassword,
    comparePassword,
} = require("../utils/passwordUtils");

const {
    generateToken,
} = require("../utils/tokenFactory");


function toSafeUser(user) {
    return {
        id: user.id,
        username: user.username,
        email: user.email,
        role: user.role,
        createdAt: user.createdAt,
        updatedAt: user.updatedAt,
    };
}


async function registerUser(req, res, next) {
    try {
        const {
            username,
            email,
            password,
            role,
        } = req.body;

        const existingEmail = await User.findOne({
            email: email.toLowerCase(),
        });

        if (existingEmail) {
            return res.status(409).json({
                success: false,
                errors: [
                    {
                        field: "email",
                        message: "Email is already registered.",
                    },
                ],
            });
        }

        const existingUsername = await User.findOne({
            username,
        });

        if (existingUsername) {
            return res.status(409).json({
                success: false,
                errors: [
                    {
                        field: "username",
                        message: "Username is already taken.",
                    },
                ],
            });
        }

        const passwordHash = await hashPassword(password);

        const newUser = await User.create({
            username,
            email,
            passwordHash,
            role,
        });

        return res.status(201).json({
            success: true,
            message: "User registered successfully.",
            user: toSafeUser(newUser),
        });
    } catch (error) {
        if (error && error.code === 11000) {
            const duplicateField =
                Object.keys(error.keyPattern || {})[0] || "account";

            return res.status(409).json({
                success: false,
                errors: [
                    {
                        field: duplicateField,
                        message:
                            duplicateField === "email"
                                ? "Email is already registered."
                                : duplicateField === "username"
                                    ? "Username is already taken."
                                    : "Account already exists.",
                    },
                ],
            });
        }

        next(error);
    }
}


async function loginUser(req, res, next) {
    try {
        const {
            email,
            password,
        } = req.body;

        const user = await User.findOne({
            email: email.toLowerCase(),
        }).select("+passwordHash");

        if (!user) {
            return res.status(401).json({
                success: false,
                message: "Invalid email or password.",
            });
        }

        const passwordMatches = await comparePassword(
            password,
            user.passwordHash
        );

        if (!passwordMatches) {
            return res.status(401).json({
                success: false,
                message: "Invalid email or password.",
            });
        }

        const token = generateToken(user);

        return res.status(200).json({
            success: true,
            message: "Login successful.",
            token,
            user: toSafeUser(user),
        });
    } catch (error) {
        next(error);
    }
}


async function getProfile(req, res, next) {
    try {
        const user = await User.findById(req.user.id);

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User no longer exists.",
            });
        }

        return res.status(200).json({
            success: true,
            user: toSafeUser(user),
        });
    } catch (error) {
        next(error);
    }
}


module.exports = {
    registerUser,
    loginUser,
    getProfile,
};