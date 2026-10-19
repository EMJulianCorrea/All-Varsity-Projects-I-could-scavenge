const bcrypt = require("bcrypt");
const User = require("../models/User");
const generateToken = require("../utils/generateToken");

const register = async (req, res, next) => {
    try {
        const {
            name, email, password
        } = req.validateRegistration;

        const existingUser = await User.findOne({ email });

        if (existingUser) {
            return res.status(409).json({
                success: false,
                error: "An account with this email already exists"
            });
        }

        const saltRounds = Number(process.env.BCRYPT_ROUNDS) || 10;
        const passwordHash = await bcrypt.hash(password, saltRounds);

        const newUser = new User({
            name,
            email,
            passwordHash
        });

        await newUser.save();

        const token = generateToken(newUser);

        return res.status(201).json({
            success: true,
            token,
            user: {
                id: newUser._id,
                name: newUser.name,
                email: newUser.email,
                role: newUser.role,
                createdAt: newUser.createdAt,
                updatedAt: newUser.updatedAt
            }
        });

    } catch (err) {
        return next(err);
    }
};
const login = async (req, res, next) => {
    try {
        const {
            email, password
        } = req.validateLogin;

        const existingUser = await User.findOne({ email }).select("+passwordHash");

        if (!existingUser) {
            return res.status(401).json({
                success: false,
                error: "Invalid email or password"
            });
        }

        const passwordMatches = await bcrypt.compare(
            password,
            existingUser.passwordHash
        );

        if (!passwordMatches) {
            return res.status(401).json({
                success: false,
                error: "Invalid email or password"
            });
        }

        const token = generateToken(existingUser);

        return res.status(200).json({
            success: true,
            token,
            user: {
                id: existingUser._id,
                name: existingUser.name,
                email: existingUser.email,
                role: existingUser.role,
                createdAt: existingUser.createdAt
            }
        });

    } catch (err) {
        return next(err);
    }
};

const getProfile = async (req, res, next) => {
    try {
        const { userId } = req.user;

        const existingUser = await User.findById(userId);

        if (!existingUser) {
            return res.status(404).json({
                success: false,
                error: "User not found"
            });
        }
        return res.status(200).json({
            success: true,
            user: {
                id: existingUser._id,
                name: existingUser.name,
                email: existingUser.email,
                role: existingUser.role,
                createdAt: existingUser.createdAt,
                updatedAt: existingUser.updatedAt
            }
        });

    } catch (err) {
        return next(err);
    }
};

module.exports = { register, login, getProfile };