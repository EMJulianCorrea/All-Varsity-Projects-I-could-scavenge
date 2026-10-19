const {
    body,
    validationResult,
} = require("express-validator");

const PUBLIC_REGISTRATION_ROLES = [
    "freelancer",
    "client",
];

const registrationValidationRules = [
    body("username")
        .trim()
        .notEmpty()
        .withMessage("Username is required.")
        .isLength({
            min: 3,
            max: 20,
        })
        .withMessage(
            "Username must be between 3 and 20 characters."
        )
        .matches(/^[A-Za-z0-9_]+$/)
        .withMessage(
            "Username can only contain letters, numbers and underscores."
        ),

    body("email")
        .trim()
        .notEmpty()
        .withMessage("Email is required.")
        .isEmail()
        .withMessage(
            "Please provide a valid email address."
        )
        .normalizeEmail(),

    body("password")
        .notEmpty()
        .withMessage("Password is required.")
        .isLength({
            min: 8,
        })
        .withMessage(
            "Password must be at least 8 characters long."
        )
        .matches(/[A-Z]/)
        .withMessage(
            "Password must contain at least one uppercase letter."
        )
        .matches(/[a-z]/)
        .withMessage(
            "Password must contain at least one lowercase letter."
        )
        .matches(/[0-9]/)
        .withMessage(
            "Password must contain at least one number."
        )
        .matches(/[^A-Za-z0-9]/)
        .withMessage(
            "Password must contain at least one special character."
        ),

    body("confirmPassword")
        .notEmpty()
        .withMessage(
            "Please confirm your password."
        )
        .custom(
            (value, { req }) =>
                value === req.body.password
        )
        .withMessage(
            "Passwords do not match."
        ),

    body("role")
        .trim()
        .notEmpty()
        .withMessage("Role is required.")
        .isIn(PUBLIC_REGISTRATION_ROLES)
        .withMessage(
            `Role must be one of: ${PUBLIC_REGISTRATION_ROLES.join(", ")}.`
        ),
];


function handleValidationErrors(req, res, next) {
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
    registrationValidationRules,
    handleValidationErrors,
};