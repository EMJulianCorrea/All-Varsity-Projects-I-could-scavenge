// Handles registration, login and profile logic.
// https://dev.to/aneeqakhan/create-user-model-3cfb

const usersData = require("../data/users");
const {
    createUserRecord,
    toSafeUser,
    findByEmail,
    findById,
} = usersData;

const {
    hashPassword,
    comparePassword,
} = require("../utils/passwordUtils");

const {
    generateToken,
} = require("../utils/tokenFactory");


async function registerUser(req, res, next) {
    try {
        const { username, email, password, role } = req.body;

        if (usersData.findByEmail(email)) {
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

        if (usersData.findByUsername(username)) {
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

        const hashedPassword = await hashPassword(password);

        const newUser = createUserRecord({
            username,
            email,
            hashedPassword,
            role,
        });

        usersData.createUser(newUser);

        return res.status(201).json({
            success: true,
            message: "User registered successfully.",
            user: toSafeUser(newUser),
        });
    } catch (error) {
        next(error);
    }
}


async function loginUser(req, res, next) {
    try {
        const { email, password } = req.body;

        const user = findByEmail(email);

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


function getProfile(req, res, next) {
    try {
        const user = findById(req.user.id);

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