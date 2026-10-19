package com.drivemate.sa.network

data class CreateExpenseRequest(

    val vehicleId: Int,
    val category: String,
    val amount: Double,
    val date: String,
    val description: String?
)