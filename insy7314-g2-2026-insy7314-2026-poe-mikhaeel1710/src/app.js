const express = require("express");
const helmet = require("helmet");
const cors = require("cors");

const notFound = require("./middleware/notFound");
const errorHandler = require("./middleware/errorHandler");

const {
    apiLimiter,
} = require("./middleware/security");

const healthRoutes = require("./routes/healthRoutes");
const authRoutes = require("./routes/authRoutes");
const gigRoutes = require("./routes/gigRoutes");

const app = express();

app.use(helmet());

app.use(
    cors({
        origin: process.env.FRONTEND_ORIGIN || true,
        methods: [
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
        ],
        allowedHeaders: [
            "Content-Type",
            "Authorization",
        ],
    })
);

app.use(
    express.json({
        limit: "10kb",
    })
);

app.use(apiLimiter);

app.use("/", healthRoutes);
app.use("/api/auth", authRoutes);
app.use("/api/gigs", gigRoutes);

app.use(notFound);
app.use(errorHandler);

module.exports = app;