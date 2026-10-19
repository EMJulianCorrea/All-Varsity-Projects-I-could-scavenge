package com.drivemate.sa.ui.theme

import androidx.compose.ui.graphics.Color
import com.drivemate.sa.network.MaintenanceType

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Badge colours for maintenance record types — same flat-val style as Color.kt
val ServiceYellow = Color(0xFFF4C430)
val RepairOrange = Color(0xFFE07A1F)
val InspectionBlue = Color(0xFF4C6EF5)
val PickUpGreen = Color(0xFF2FB380)
val OtherGrey = Color(0xFF9E9E9E)

fun MaintenanceType.badgeColor(): Color = when (this) {
    MaintenanceType.SERVICE -> ServiceYellow
    MaintenanceType.REPAIR -> RepairOrange
    MaintenanceType.INSPECTION -> InspectionBlue
    MaintenanceType.PICK_UP -> PickUpGreen
    MaintenanceType.OTHER -> OtherGrey
}