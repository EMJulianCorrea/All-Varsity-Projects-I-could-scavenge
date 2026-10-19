const express = require("express");

const authenticateToken = require("../middleware/authenticateToken")
const authoriseRoles = require("../middleware/authoriseRoles");

const {
    getAllGames,
    getGameById,
    createGame,
    replaceGame,
    updateGame,
    deleteGame
} = require("../controllers/gameController");

const validateGame = require("../middleware/validateGame");
const validateGameId = require("../middleware/validateGameId");

const router = express.Router();

router.get("/", getAllGames);

router.get("/:id", validateGameId, getGameById);

router.post("/", authenticateToken, authoriseRoles("admin"), validateGame, createGame);

router.put("/:id", authenticateToken, authoriseRoles("admin"), validateGameId, replaceGame);

router.patch("/:id", authenticateToken, authoriseRoles("admin"), validateGameId, updateGame);

router.delete("/:id", authenticateToken, authoriseRoles("admin"), validateGameId, deleteGame);

module.exports = router;