package com.drivemate.sa.network

data class HealthResponse(
    val status: String,
    val service: String,
    val timestamp: String
)