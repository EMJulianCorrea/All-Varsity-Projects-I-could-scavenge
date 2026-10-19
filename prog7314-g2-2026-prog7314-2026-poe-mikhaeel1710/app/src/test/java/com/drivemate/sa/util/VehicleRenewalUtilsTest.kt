package com.drivemate.sa.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class VehicleRenewalUtilsTest {

    @Test
    fun renewalTenDaysAway_returnsTenDays() {
        val currentDate =
            Instant.parse("2026-09-15T00:00:00Z").toEpochMilli()

        val result = VehicleRenewalUtils.daysUntilRenewal(
            licenceRenewalDate = "2026-09-25T00:00:00Z",
            currentTimeMillis = currentDate
        )

        assertEquals(10L, result)
    }

    @Test
    fun renewalToday_returnsZeroDays() {
        val currentDate =
            Instant.parse("2026-09-15T00:00:00Z").toEpochMilli()

        val result = VehicleRenewalUtils.daysUntilRenewal(
            licenceRenewalDate = "2026-09-15T00:00:00Z",
            currentTimeMillis = currentDate
        )

        assertEquals(0L, result)
    }

    @Test
    fun expiredLicence_returnsNegativeDays() {
        val currentDate =
            Instant.parse("2026-09-15T00:00:00Z").toEpochMilli()

        val result = VehicleRenewalUtils.daysUntilRenewal(
            licenceRenewalDate = "2026-09-14T00:00:00Z",
            currentTimeMillis = currentDate
        )

        assertEquals(-1L, result)
    }

    @Test
    fun tenDaysRemaining_isDueSoon() {
        assertEquals(
            RenewalStatus.DUE_SOON,
            VehicleRenewalUtils.renewalStatus(10)
        )
    }

    @Test
    fun zeroDaysRemaining_isDueToday() {
        assertEquals(
            RenewalStatus.DUE_TODAY,
            VehicleRenewalUtils.renewalStatus(0)
        )
    }

    @Test
    fun negativeDays_isOverdue() {
        assertEquals(
            RenewalStatus.OVERDUE,
            VehicleRenewalUtils.renewalStatus(-1)
        )
    }

    @Test
    fun moreThanThirtyDays_isOkay() {
        assertEquals(
            RenewalStatus.OK,
            VehicleRenewalUtils.renewalStatus(31)
        )
    }
}