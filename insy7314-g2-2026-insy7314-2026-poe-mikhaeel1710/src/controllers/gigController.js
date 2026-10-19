const mongoose = require("mongoose");

const Gig = require("../models/Gig");
const AppError = require("../utils/AppError");
const catchAsync = require("../utils/catchAsync");

const ALLOWED_GIG_FIELDS = [
    "title",
    "description",
    "category",
    "price",
    "isActive",
];

function pickAllowedFields(source) {
    const result = {};

    for (const field of ALLOWED_GIG_FIELDS) {
        if (source[field] !== undefined) {
            result[field] = source[field];
        }
    }

    return result;
}

function isValidObjectId(id) {
    return mongoose.Types.ObjectId.isValid(id);
}

const getAllGigs = catchAsync(async (req, res, next) => {
    const filter = {};

    if (req.query.category) {
        filter.category = req.query.category;
    }

    if (req.query.freelancer) {
        if (!isValidObjectId(req.query.freelancer)) {
            return next(new AppError("freelancer must be a valid ID.", 400));
        }
        filter.freelancer = req.query.freelancer;
    }

    if (req.query.isActive !== undefined) {
        filter.isActive = req.query.isActive === "true";
    }

    const gigs = await Gig.find(filter)
        .populate("freelancer", "username")
        .sort({ createdAt: -1 });

    return res.status(200).json({
        success: true,
        count: gigs.length,
        gigs,
    });
});

const getGigById = catchAsync(async (req, res, next) => {
    const { id } = req.params;

    if (!isValidObjectId(id)) {
        return next(new AppError("Gig ID must be a valid ID.", 400));
    }

    const gig = await Gig.findById(id).populate("freelancer", "username");

    if (!gig) {
        return next(new AppError("Gig not found.", 404));
    }

    return res.status(200).json({
        success: true,
        gig,
    });
});

const createGig = catchAsync(async (req, res, next) => {
    const gigData = pickAllowedFields(req.body);

    const newGig = await Gig.create({
        ...gigData,
        freelancer: req.user.id,
    });

    return res.status(201).json({
        success: true,
        message: "Gig created successfully.",
        gig: newGig,
    });
});

const updateGig = catchAsync(async (req, res, next) => {
    const { id } = req.params;

    if (!isValidObjectId(id)) {
        return next(new AppError("Gig ID must be a valid ID.", 400));
    }

    const gig = await Gig.findById(id);

    if (!gig) {
        return next(new AppError("Gig not found.", 404));
    }

    const isOwner = gig.freelancer.toString() === req.user.id;
    const isAdmin = req.user.role === "admin";

    if (!isOwner && !isAdmin) {
        return next(
            new AppError(
                "You do not have permission to edit this gig.",
                403
            )
        );
    }

    const updates = pickAllowedFields(req.body);

    if (Object.keys(updates).length === 0) {
        return next(
            new AppError("At least one field must be supplied to update.", 400)
        );
    }

    Object.assign(gig, updates);
    await gig.save();

    return res.status(200).json({
        success: true,
        message: "Gig updated successfully.",
        gig,
    });
});

const deleteGig = catchAsync(async (req, res, next) => {
    const { id } = req.params;

    if (!isValidObjectId(id)) {
        return next(new AppError("Gig ID must be a valid ID.", 400));
    }

    const gig = await Gig.findById(id);

    if (!gig) {
        return next(new AppError("Gig not found.", 404));
    }

    const isOwner = gig.freelancer.toString() === req.user.id;
    const isAdmin = req.user.role === "admin";

    if (!isOwner && !isAdmin) {
        return next(
            new AppError(
                "You do not have permission to delete this gig.",
                403
            )
        );
    }

    await gig.deleteOne();

    return res.status(200).json({
        success: true,
        message: "Gig deleted successfully.",
    });
});


module.exports = {
    getAllGigs,
    getGigById,
    createGig,
    updateGig,
    deleteGig,
};