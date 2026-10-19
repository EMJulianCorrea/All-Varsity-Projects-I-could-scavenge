package com.drivemate.sa.network

import java.time.LocalDate
import java.util.UUID

/**
 * UI-facing shape for a single maintenance/service item shown on the
 * Vehicle Health screen. This is intentionally minimal — just what the
 * cards and form need to render. Whoever wires up the API/DB can map their
 * real model onto this (or extend it) when that work happens.
 */
data class MaintenanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val type: MaintenanceType,
    val title: String,
    val date: LocalDate,
    val mileageKm: Int? = null,
    val cost: Double? = null,
    val notes: String = "",
    val nextDueDate: LocalDate? = null,
    val completed: Boolean = true
) {
    /** True when this is an upcoming/overdue item rather than a completed past one. */
    fun isUpcoming(today: LocalDate = LocalDate.now()): Boolean =
        !completed || (nextDueDate != null && !nextDueDate.isBefore(today))

    fun isOverdue(today: LocalDate = LocalDate.now()): Boolean =
        !completed && nextDueDate != null && nextDueDate.isBefore(today)
}