const jwt = require("jsonwebtoken");

const generateToken = (user) => {

    const jwtSecret = process.env.JWT_SECRET;

    if (!jwtSecret) {
        throw new Error("JWT secret not configured.");
    }

    const payload = {
        userId: user._id.toString(),
        email: user.email,
        role: user.role
    };

    const expiresIn = process.env.JWT_EXPIRES_IN || "1h";

    const token = jwt.sign(payload, jwtSecret, { expiresIn });

    return token;
};

module.exports = generateToken;