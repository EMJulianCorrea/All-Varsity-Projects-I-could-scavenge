const handleJSONParseError = () => {    
    const err = new Error ("Malformed JSON in request body.");
    err.statusCode = 400;
    err.isOperational = true;
    return err;
    
};

const handleJWTError = () => {    
    const err = new Error ("Invalid authentication token. Please log in again.");
    err.statusCode = 401;
    err.isOperational = true;
    return err;
};

const handleJWTExpiredError = () => {    
    const err = new Error ("Your session has expired. Please log in again.");
    err.statusCode = 401;
    err.isOperational = true;
    return err;
};

const sendErrorDev = (err, res) => {    
    res.status(err.statusCode).json({
        success: false,
        message: err.message,
        stack: err.stack

    });
};

const sendErrorProd = (err, res) => {   
    if (err.isOperational) {    
        res.status(err.statusCode).json({
            success: false,
            message: err.message
        });
        return;
    }
console.error("Unexpected Error:", err);
res.status(500).json ({
    success: false,
    message: "Something went wrong, try again."
});
};

const errorHandler=(err, req, res, next) => {   
    err.statusCode = err.statusCode || 500;

    const isDev = process.env.NODE_ENV == "development";

    if(isDev){  
        sendErrorDev(err, res);
        return;
    }
        let error = err;
    if (err.type === "entity.parse.failed") error = handleJSONParseError();
    if (err.name === "JsonWebTokenError") error = handleJWTError();
    if (err.name === "TokenExpiredError") error = handleJWTExpiredError();

    sendErrorProd(error, res);
};

module.exports = errorHandler;
