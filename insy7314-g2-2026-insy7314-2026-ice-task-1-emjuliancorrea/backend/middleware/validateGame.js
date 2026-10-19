const validateGame = (req, res, next) => {

    const {
        title,
        genre,
        platform,
        releaseYear,
        ageRating
    } = req.body;

    if (
        !title ||
        !genre ||
        !platform ||
        releaseYear === undefined ||
        !ageRating
    ) {
        return res.status(400).json({
            error:
                "Title, genre, platform, release year and age rating are required."
        });
    }

    if (
        typeof title !== "string" ||
        typeof genre !== "string" ||
        typeof platform !== "string" ||
        typeof ageRating !== "string"
    ) {
        return res.status(400).json({
            error:
                "Title, genre, platform and age rating must be text."
        });
    }

    const cleanedTitle = title.trim();
    const cleanedGenre = genre.trim();
    const cleanedPlatform = platform.trim();
    const cleanedAgeRating = ageRating.trim().toUpperCase();

    if (
        cleanedTitle.length < 2 ||
        cleanedTitle.length > 100
    ) {
        return res.status(400).json({
            error:
                "Title must contain between 2 and 100 characters."
        });
    }
    if (
        cleanedGenre.length < 2 ||
        cleanedGenre.length > 50
    ) {
        return res.status(400).json({
            error:
                "Genre must contain between 2 and 50 characters."
        });
    }
    if (
        cleanedPlatform.length < 2 ||
        cleanedPlatform.length > 50
    ) {
        return res.status(400).json({
            error:
                "Platform must contain between 2 and 50 characters."
        });
    }

    const currentYear = new Date().getFullYear();

    if (
        typeof releaseYear !== "number" ||
        !Number.isInteger(releaseYear) ||
        releaseYear < 1950 ||
        releaseYear > currentYear + 2
    ) {
        return res.status(400).json({
            error:
                `Release year must be a whole number between 1950 and ${currentYear + 2}.`
        });
    }

    const allowedAgeRatings = [
        "E",
        "E10+",
        "T",
        "M",
        "18"
    ];
    if (!allowedAgeRatings.includes(cleanedAgeRating)) {
        return res.status(400).json({
            error:
                `Age rating must be one of: ${allowedAgeRatings.join(", ")}.`
        });
    }

    req.validateGame = {
        title: cleanedTitle,
        genre: cleanedGenre,
        platform: cleanedPlatform,
        releaseYear,
        ageRating: cleanedAgeRating
    };

    next();

};

module.exports = validateGame;