const { body, validationResult } = require("express-validator");

const createGigValidationRules = [
    body("title")
        .trim()
        .notEmpty()
        .withMessage("Title is required.")
        .isLength({ min: 3, max: 100 })
        .withMessage("Title must be between 3 and 100 characters."),

    body("description")
        .trim()
        .notEmpty()
        .withMessage("Description is required.")
        .isLength({ min: 10, max: 2000 })
        .withMessage("Description must be between 10 and 2000 characters."),

    body("category")
        .trim()
        .notEmpty()
        .withMessage("Category is required.")
        .isLength({ min: 2, max: 50 })
        .withMessage("Category must be between 2 and 50 characters."),

    body("price")
        .notEmpty()
        .withMessage("Price is required.")
        .isFloat({ min: 0 })
        .withMessage("Price must be a number of 0 or greater."),

    body("isActive")
        .optional()
        .isBoolean()
        .withMessage("isActive must be true or false."),
];

const updateGigValidationRules = [
    body("title")
        .optional()
        .trim()
        .notEmpty()
        .withMessage("Title cannot be empty.")
        .isLength({ min: 3, max: 100 })
        .withMessage("Title must be between 3 and 100 characters."),

    body("description")
        .optional()
        .trim()
        .notEmpty()
        .withMessage("Description cannot be empty.")
        .isLength({ min: 10, max: 2000 })
        .withMessage("Description must be between 10 and 2000 characters."),

    body("category")
        .optional()
        .trim()
        .notEmpty()
        .withMessage("Category cannot be empty.")
        .isLength({ min: 2, max: 50 })
        .withMessage("Category must be between 2 and 50 characters."),

    body("price")
        .optional()
        .isFloat({ min: 0 })
        .withMessage("Price must be a number of 0 or greater."),

    body("isActive")
        .optional()
        .isBoolean()
        .withMessage("isActive must be true or false."),
];

function handleGigValidationErrors(req, res, next) {
    const errors = validationResult(req);

    if (!errors.isEmpty()) {
        return res.status(400).json({
            success: false,
            errors: errors.array().map((error) => ({
                field: error.path,
                message: error.msg,
            })),
        });
    }

    next();
}

module.exports = {
    createGigValidationRules,
    updateGigValidationRules,
    handleGigValidationErrors,
};