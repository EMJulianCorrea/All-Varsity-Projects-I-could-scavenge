const fs = require("fs");
const path = require("path");

const backendDirectory = path.resolve(__dirname, "..");

const sslKeyPath =
    process.env.SSL_KEY_PATH ||
    "certificates/privatekey.pem";

const sslCertPath =
    process.env.SSL_CERT_PATH ||
    "certificates/certificate.pem";

const resolvedKeyPath = path.resolve(
    backendDirectory,
    sslKeyPath
);

const resolvedCertPath = path.resolve(
    backendDirectory,
    sslCertPath
);

const httpsOptions = {
    key: fs.readFileSync(resolvedKeyPath),
    cert: fs.readFileSync(resolvedCertPath)
};

module.exports = httpsOptions;