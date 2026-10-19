package com.drivemate.sa.network

data class Vehicle(
    val id: Int,
    val firebaseUserId: String,
    val nickname: String,
    val make: String,
    val model: String,
    val year: Int,
    val registrationNumber: String,
    val fuelType: String,
    val odometerKm: Int,
    val licenceRenewalDate: String,
    val createdAt: String
)