package com.drivemate.sa.network

//Category of a maintenance/service event
enum class MaintenanceType(val label: String) {
    SERVICE(label = "Service"),
    REPAIR(label = "Repair"),
    INSPECTION(label = "Inspection"),
    PICK_UP(label = "Pick Up"),
    OTHER(label = "Other")
}