const { rateLimit } = require("express-rate-limit");

const apiLimiter = rateLimit({ 
    windowMs: 15 * 60 * 1000,
    limit: 100,
    standardHeaders: "draft-8",
    legacyHeaders: false,
    statusCode: 429,
    message: {
        success: false,
        error: "Too many requests. Please try again later."
    }
});

const authLimiter = rateLimit({ 
    windowMs: 15 * 60 * 1000,
    limit: 10,
    standardHeaders: "draft-8",
    legacyHeaders: false,
    statusCode: 429,
    message: {
        success: false,
        error: "Too many requests. Please try again later."
    },
    skipSuccessfulRequests: false
});

module.exports = { apiLimiter, authLimiter };