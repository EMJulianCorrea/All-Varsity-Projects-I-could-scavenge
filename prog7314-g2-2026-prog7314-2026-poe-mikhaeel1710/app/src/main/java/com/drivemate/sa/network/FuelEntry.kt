package com.drivemate.sa.network

data class FuelEntry(
    val id: Int,
    val vehicleId: Int,
    val date: String,
    val odometerKm: Int,
    val litres: Double,
    val pricePerLitre: Double?,
    val totalCost: Double,
    val isFullTank: Boolean,
    val createdAt: String
)