package com.drivemate.sa.network

data class CreateVehicleRequest(
    val firebaseUserId: String,
    val nickname: String,
    val make: String,
    val model: String,
    val year: Int,
    val registrationNumber: String,
    val fuelType: String,
    val odometerKm: Int,
    val licenceRenewalDate: String
)