require("dotenv").config();

const https = require("https");

const app = require("./app");

const notFound = require("./middleware/notFound");

const systemRoutes = require("./routes/systemRoutes");

const httpsOptions = require("../config/httpsConfig");

const connectDatabase = require("../config/database");

const HTTPS_PORT = process.env.HTTPS_PORT || 4000;
const APP_NAME =
    process.env.APP_NAME || "GameVault API";

function handleServerError(error) {

    console.error("The GameVault server could not start.");
    console.error(error.message);

}

async function startServer() {

    try {
        await connectDatabase();
    } catch (err) {
        console.error("Failed to connect to database:", err.message);
        process.exit(1);
    }

    const server = https.createServer(httpsOptions, app);

    server.listen(HTTPS_PORT, () => {

        console.log(
            `${APP_NAME} is running securely on https://localhost:${HTTPS_PORT}`
        );

    });

    server.on("error", handleServerError);

}

startServer();