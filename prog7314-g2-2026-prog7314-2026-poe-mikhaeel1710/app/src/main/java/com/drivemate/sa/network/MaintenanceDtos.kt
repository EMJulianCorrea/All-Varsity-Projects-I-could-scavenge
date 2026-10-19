package com.drivemate.sa.network

data class MaintenanceRecordResponse(
    val id: String,
    val firebaseUserId: String,
    val type: String,
    val title: String,
    val date: String,
    val mileageKm: Int?,
    val cost: Double?,
    val notes: String,
    val nextDueDate: String?,
    val completed: Boolean,
    val createdAt: String
)

data class MaintenanceRecordRequest(
    val firebaseUserId: String,
    val type: String,
    val title: String,
    val date: String,
    val mileageKm: Int?,
    val cost: Double?,
    val notes: String,
    val nextDueDate: String?,
    val completed: Boolean
)