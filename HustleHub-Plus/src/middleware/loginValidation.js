const { body, validationResult } = require("express-validator");

const loginValidationRules = [
    body("email")
        .trim()
        .notEmpty()
        .withMessage("Email is required.")
        .isEmail()
        .withMessage("Please provide a valid email address.")
        .normalizeEmail(),

    body("password")
        .notEmpty()
        .withMessage("Password is required.")
        .isString()
        .withMessage("Password must be text.")
];

function handleLoginValidationErrors(req, res, next) {
    const errors = validationResult(req);

    if (!errors.isEmpty()) {
        return res.status(400).json({
            success: false,
            errors: errors.array().map((err) => ({
                field: err.path,
                message: err.msg
            }))
        });
    }

    next();
}

module.exports = {
    loginValidationRules,
    handleLoginValidationErrors
};