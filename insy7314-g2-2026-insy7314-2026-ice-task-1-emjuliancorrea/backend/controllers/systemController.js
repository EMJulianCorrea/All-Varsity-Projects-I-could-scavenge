const getRoot = (req, res) => {

    const appName = process.env.APP_NAME || "GameVault API";

    return res.status(200).json({
        application: appName,
        message: "Welcome to the GameVault API"
    });

};

const getAbout = (req, res) => {

    const appName = process.env.APP_NAME || "GameVault API";

    return res.status(200).json({
        application: appName,
        description:
            "GameVault is a secure video game collection and review platform.",
        currentStage:
            "Learning Unit 2 - Refactoring the Backend"
    });

};

const getHealth = (req, res) => {

    const appName = process.env.APP_NAME || "GameVault API";

    return res.status(200).json({
        application: appName,
        status: "OK",
        protocol: "HTTPS",
        environment: process.env.NODE_ENV || "development",
        timestamp: new Date().toISOString()
    });

};

module.exports = {
    getRoot,
    getAbout,
    getHealth
};