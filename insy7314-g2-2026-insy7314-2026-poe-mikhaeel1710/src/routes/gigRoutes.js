const express = require("express");

const {
    getAllGigs,
    getGigById,
    createGig,
    updateGig,
    deleteGig,
} = require("../controllers/gigController");

const {
    createGigValidationRules,
    updateGigValidationRules,
    handleGigValidationErrors,
} = require("../middleware/gigValidation");

const { authenticateToken } = require("../utils/tokenFactory");
const authoriseRoles = require("../middleware/authoriseRoles");

const router = express.Router();

router.get("/", getAllGigs);

router.get("/:id", getGigById);

router.post(
    "/",
    authenticateToken,
    authoriseRoles("freelancer"),
    createGigValidationRules,
    handleGigValidationErrors,
    createGig
);

router.patch(
    "/:id",
    authenticateToken,
    updateGigValidationRules,
    handleGigValidationErrors,
    updateGig
);

router.delete(
    "/:id",
    authenticateToken,
    deleteGig
);


module.exports = router;