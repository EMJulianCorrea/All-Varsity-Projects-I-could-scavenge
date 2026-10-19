const authoriseRoles = (...allowedRoles) => {

    return (req, res, next) => {

        if (!req.user) {
            return res.status(401).json({
                success: false,
                error: "Authentication is required."
            });
        }

        const userRole = req.user.role;

        const isAllowed = allowedRoles.includes(userRole);

        if (!isAllowed) {
            return res.status(403).json({
                success: false,
                error: "You do not have permission to perform this action."
            });
        }

        return next();

    };

};

module.exports = authoriseRoles;