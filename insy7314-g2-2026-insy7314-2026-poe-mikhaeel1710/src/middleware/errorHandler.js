function createOperationalError(
    message,
    statusCode
) {
    const error = new Error(message);

    error.statusCode = statusCode;
    error.isOperational = true;

    return error;
}


function normaliseError(error) {
    if (
        error.type === "entity.parse.failed"
    ) {
        return createOperationalError(
            "Malformed JSON in request body.",
            400
        );
    }

    if (
        error.name === "JsonWebTokenError"
    ) {
        return createOperationalError(
            "Invalid authentication token. Please log in again.",
            401
        );
    }

    if (
        error.name === "TokenExpiredError"
    ) {
        return createOperationalError(
            "Your session has expired. Please log in again.",
            401
        );
    }

    if (
        error.name === "ValidationError"
    ) {
        const messages = Object.values(
            error.errors || {}
        )
            .map((item) => item.message)
            .filter(Boolean);

        return createOperationalError(
            messages.length > 0
                ? messages.join(" ")
                : "The supplied data is invalid.",
            400
        );
    }

    if (
        error.name === "CastError"
    ) {
        return createOperationalError(
            `Invalid value supplied for ${error.path || "resource identifier"}.`,
            400
        );
    }

    if (
        error.code === 11000
    ) {
        const duplicateField =
            Object.keys(
                error.keyPattern ||
                error.keyValue ||
                {}
            )[0];

        const fieldName =
            duplicateField || "value";

        return createOperationalError(
            `${fieldName} already exists.`,
            409
        );
    }

    return error;
}


function sendDevelopmentError(error, res) {
    return res
        .status(error.statusCode || 500)
        .json({
            success: false,
            message:
                error.message ||
                "Something went wrong.",
            stack: error.stack,
        });
}


function sendProductionError(error, res) {
    if (error.isOperational) {
        return res
            .status(error.statusCode)
            .json({
                success: false,
                message: error.message,
            });
    }

    console.error(
        "Unexpected Error:",
        error
    );

    return res.status(500).json({
        success: false,
        message:
            "Something went wrong. Please try again.",
    });
}


function errorHandler(
    err,
    req,
    res,
    next
) {
    const error = normaliseError(err);

    error.statusCode =
        error.statusCode || 500;

    const isDevelopment =
        process.env.NODE_ENV ===
        "development";

    if (isDevelopment) {
        return sendDevelopmentError(
            error,
            res
        );
    }

    return sendProductionError(
        error,
        res
    );
}


module.exports = errorHandler;