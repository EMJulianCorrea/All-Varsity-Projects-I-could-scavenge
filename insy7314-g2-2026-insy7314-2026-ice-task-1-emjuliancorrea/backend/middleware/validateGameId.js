const mongoose = require("mongoose");

const validateGameId = (req, res, next) => {

    const gameId = req.params.id;

    if (!mongoose.Types.ObjectId.isValid(gameId)) {
        return res.status(400).json({
            error: "Game ID must be a valid ID."
        });
    }

    return next();

};

module.exports = validateGameId;