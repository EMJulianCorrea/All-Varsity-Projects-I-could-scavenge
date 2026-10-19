const jwt = require("jsonwebtoken");

function generateToken(user) {
    const jwtSecret = process.env.JWT_SECRET;

    if (!jwtSecret) {
        throw new Error("JWT secret not configured.");
    }

    const payload = {
        id: user.id,
        email: user.email,
        role: user.role,
    };

    return jwt.sign(payload, jwtSecret, {
        expiresIn: process.env.JWT_EXPIRES_IN || "1h",
    });
}

function authenticateToken(req, res, next) {
    const authHeader = req.headers.authorization;

    if (!authHeader) {
        return res.status(401).json({
            success: false,
            message: "Access token is required.",
        });
    }

    const [scheme, token] = authHeader.split(" ");

    if (scheme !== "Bearer" || !token) {
        return res.status(401).json({
            success: false,
            message: "Authorization header must use the Bearer scheme.",
        });
    }

    const jwtSecret = process.env.JWT_SECRET;

    if (!jwtSecret) {
        return next(new Error("JWT secret not configured."));
    }

    jwt.verify(token, jwtSecret, (error, decoded) => {
        if (error) {
            return res.status(401).json({
                success: false,
                message: "Invalid or expired token.",
            });
        }

        req.user = decoded;
        next();
    });
}

module.exports = {
    generateToken,
    authenticateToken,
};