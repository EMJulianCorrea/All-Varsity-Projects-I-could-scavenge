package com.drivemate.sa.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object VehicleRenewalUtils {

    fun daysUntilRenewal(
        licenceRenewalDate: String,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long? {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssX",
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }

        val renewalDate = formatter.parse(licenceRenewalDate)
            ?: return null

        val currentDay = startOfUtcDay(currentTimeMillis)
        val renewalDay = startOfUtcDay(renewalDate.time)

        return TimeUnit.MILLISECONDS.toDays(
            renewalDay - currentDay
        )
    }

    fun renewalStatus(daysRemaining: Long): RenewalStatus {
        return when {
            daysRemaining < 0 -> RenewalStatus.OVERDUE
            daysRemaining == 0L -> RenewalStatus.DUE_TODAY
            daysRemaining <= 30 -> RenewalStatus.DUE_SOON
            else -> RenewalStatus.OK
        }
    }

    private fun startOfUtcDay(timeMillis: Long): Long {
        return Calendar.getInstance(
            TimeZone.getTimeZone("UTC")
        ).apply {
            this.timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

enum class RenewalStatus {
    OVERDUE,
    DUE_TODAY,
    DUE_SOON,
    OK
}