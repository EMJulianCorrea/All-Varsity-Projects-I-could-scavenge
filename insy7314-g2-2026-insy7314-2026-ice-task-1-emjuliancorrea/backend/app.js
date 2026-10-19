const express = require("express");
const cors = require("cors");
const helmet = require("helmet");

const notFound = require("./middleware/notFound");
const errorHandler = require("./middleware/errorHandler");
const { apiLimiter } = require("./middleware/rateLimiters");

const systemRoutes = require("./routes/systemRoutes");
const gameRoutes = require("./routes/gameRoutes");
const authRoutes = require("./routes/authRoutes")

const app = express();

app.use(
    helmet({
        contentSecurityPolicy: {
            directives: {
                defaultSrc: ["'self'"],
                scriptSrc: ["'self'"],
                styleSrc: ["'self'"],
                fontSrc: ["'self'"],
                imgSrc: ["'self'", "data:"],
                connectSrc: ["'self'"],
                objectSrc: ["'none'"],
                frameAncestors: ["'none'"],
                baseUri: ["'self'"],
                formAction: ["'self'"]
            }
        }
    })
);

const clientOrigin = process.env.CLIENT_ORIGIN;

if (!clientOrigin) {
    throw new Error("CLIENT_ORIGIN is not defined. Check your .env file.");
}

app.use(
    cors({
        origin: clientOrigin,
        credentials: true
    })
);

app.use(express.json());

app.use(apiLimiter);

app.use("/", systemRoutes);

app.use("/games", gameRoutes);
app.use("/auth", authRoutes);

app.use(notFound);

app.use(errorHandler);

module.exports = app;