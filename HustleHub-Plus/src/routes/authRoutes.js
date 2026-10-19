// https://dev.to/aneeqakhan/create-user-model-3cfb
const express = require("express");

const {
    registerUser,
    loginUser,
    getProfile,
} = require("../controllers/authController");

const {
    registrationValidationRules,
    handleValidationErrors,
} = require("../middleware/registrationValidation");

const {
    loginValidationRules,
    handleLoginValidationErrors,
} = require("../middleware/loginValidation");

const {
    authenticateToken,
} = require("../utils/tokenFactory");

const router = express.Router();

// POST /api/auth/register
router.post(
    "/register",
    registrationValidationRules,
    handleValidationErrors,
    registerUser
);

// POST /api/auth/login
router.post(
    "/login",
    loginValidationRules,
    handleLoginValidationErrors,
    loginUser
);

// GET /api/auth/profile
router.get(
    "/profile",
    authenticateToken,
    getProfile
);

module.exports = router;