const jwt = require("jsonwebtoken");

const authenticateToken = (req, res, next) => {

    const authHeader = req.headers["authorisation"];

    if (!authHeader) {
        return res.status(401).json({
            success: false,
            error: "Authentication token is required."
        });
    }

    const headerParts = authHeader.split(" ");

    if (
        headerParts.length !== 2 ||
        headerParts[0] !== "Bearer" ||
        !headerParts[1]
    ) {
        return res.status(401).json({
            success: false,
            error: "Authorisation header must use the Bearer token format."
        });
    }

    const token = headerParts[1];

    const jwtSecret = process.env.JWT_SECRET;
    const jwtIssuer = process.env.JWT_ISSUER;
    const jwtAudience = process.env.JWT_AUDIENCE;

    if (!jwtSecret || !jwtIssuer || !jwtAudience) {
        console.error(
            "JWT configuration is missing. Check JWT_SECRET, JWT_ISSUER, and JWT_AUDIENCE."
        );
        return res.status(500).json({
            success: false,
            error: "Internal Server Error"
        });
    }

    try {

        const decoded = jwt.verify(token, jwtSecret, {
            algorithms: ["HS256"],
            issuer: jwtIssuer,
            audience: jwtAudience
        });

        req.user = decoded;

        return next();

    } catch (err) {

        if (err.name === "TokenExpiredError") {
            return res.status(401).json({
                success: false,
                error: "Authentication token has expired."
            });
        }

        return res.status(401).json({
            success: false,
            error: "Authentication token is invalid."
        });
    }
};

module.exports = authenticateToken;