require("dotenv").config();

const fs = require("fs");
const https = require("https");
const path = require("path");

const app = require("./src/app");
const connectDatabase = require("./src/config/database");

const PORT = process.env.PORT || 3000;

const httpsOptions = {
    pfx: fs.readFileSync(
        path.join(__dirname, "certs", "localhost.pfx")
    ),
    passphrase: process.env.SSL_PASSPHRASE
};

async function startServer() {
    try {
        await connectDatabase();

        https.createServer(httpsOptions, app).listen(PORT, () => {
            console.log(
                `HustleHub+ API running securely at https://localhost:${PORT}`
            );
        });
    } catch (error) {
        console.error("Failed to start HustleHub+:", error.message);
        process.exit(1);
    }
}

startServer();