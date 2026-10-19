function hasNumberAndSpecialCharacter(str) {
    const hasDigit = /[0-9]/.test(str);
    const hasSpecialChar = /[^a-zA-Z0-9]/.test(str);
    const hasLetter = /[a-zA-Z]/.test(str);

    return hasDigit && hasSpecialChar && hasLetter;
}

function validateEmail(str) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(str);
}

const validateRegistration = (req, res, next) => {

    if (!req.body || Object.keys(req.body).length === 0) {
        return res.status(400).json({
            error: "Request body is required."
        });
    }

    const {
        name,
        email,
        password
    } = req.body;

    if (
        !name ||
        !email ||
        !password
    ) {
        return res.status(400).json({
            error:
                "All user details, excluding role, are required."
        });
    }

    if (
        typeof name !== "string" ||
        typeof email !== "string" ||
        typeof password !== "string"
    ) {
        return res.status(400).json({
            error:
                "User details must be text."
        });
    }

    const cleanedName = name.trim();
    let cleanedEmail = email.trim();
    const cleanedPassword = password.trim();

    cleanedEmail = cleanedEmail.toLowerCase();

    if (
        cleanedName.length < 2 ||
        cleanedName.length > 100
    ) {
        return res.status(400).json({
            error:
                "Name must contain between 2 and 100 characters."
        });
    }

    if (
        !validateEmail(cleanedEmail)
    ) {
        return res.status(400).json({
            error:
                "Email is not in the correct format"
        });
    }

    if (
        cleanedPassword.length < 8 ||
        cleanedPassword.length > 128 ||
        cleanedPassword == cleanedPassword.toLowerCase() ||
        cleanedPassword == cleanedPassword.toUpperCase() ||
        !hasNumberAndSpecialCharacter(cleanedPassword)
    ) {
        return res.status(400).json({
            error:
                "Password is invalid. Please follow the password policy provided"
        });
    }

    const newRole = "user";

    req.validateRegistration = {
        name: cleanedName,
        email: cleanedEmail,
        password: cleanedPassword,
        role: newRole
    };

    next();

};

const validateLogin = (req, res, next) => {

    if (!req.body || Object.keys(req.body).length === 0) {
        return res.status(400).json({
            error: "Request body is required."
        });
    }

    const { email, password } = req.body;

    if (
        !email ||
        !password
    ) {
        return res.status(400).json({
            error:
                "Email and password are required."
        });
    }

    if (
        typeof email !== "string" ||
        typeof password !== "string"
    ) {
        return res.status(400).json({
            error:
                "Email and password must be text."
        });
    }

    let cleanedEmail = email.trim();
    cleanedEmail = cleanedEmail.toLowerCase();

    if (
        !validateEmail(cleanedEmail)
    ) {
        return res.status(400).json({
            error:
                "Email is not in the correct format"
        });
    }

    req.validateLogin = {
        email: cleanedEmail,
        password
    };

    next();

};

module.exports = { validateRegistration, validateLogin };