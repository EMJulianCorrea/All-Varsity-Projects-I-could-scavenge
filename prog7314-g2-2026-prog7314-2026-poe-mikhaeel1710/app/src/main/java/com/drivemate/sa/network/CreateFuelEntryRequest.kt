package com.drivemate.sa.network

data class CreateFuelEntryRequest(
    val vehicleId: Int,
    val date: String,
    val odometerKm: Int,
    val litres: Double,
    val pricePerLitre: Double?,
    val totalCost: Double,
    val isFullTank: Boolean
)