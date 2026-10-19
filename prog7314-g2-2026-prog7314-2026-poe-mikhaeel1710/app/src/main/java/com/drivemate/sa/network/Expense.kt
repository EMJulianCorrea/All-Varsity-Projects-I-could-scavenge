package com.drivemate.sa.network

data class Expense(

    val id: Int,
    val vehicleId: Int,
    val category: String,
    val amount: Double,
    val date: String,

    val description: String?,
    val createdAt: String

)