const express = require("express");
const authenticateToken = require("../middleware/authenticateToken");
const { authLimiter } = require("../middleware/rateLimiters")

const { register, login, getProfile } = require("../controllers/authController");
const {validateRegistration, validateLogin} = require("../middleware/validateAuth");



const router = express.Router();

router.post("/register", authLimiter,validateRegistration, register);

router.post("/login", authLimiter, validateLogin, login);

router.get("/profile", authenticateToken, getProfile);

module.exports = router;