package com.drivemate.sa.runningcosts

import com.drivemate.sa.network.Expense
import com.drivemate.sa.network.FuelEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RunningCostsCalculatorTest {

    private fun fuelEntry(
        id: Int = 1,
        vehicleId: Int = 1,
        date: String = "2026-08-01",
        odometerKm: Int = 0,
        litres: Double = 0.0,
        pricePerLitre: Double? = null,
        totalCost: Double = 0.0,
        isFullTank: Boolean = true
    ) = FuelEntry(
        id = id,
        vehicleId = vehicleId,
        date = date,
        odometerKm = odometerKm,
        litres = litres,
        pricePerLitre = pricePerLitre,
        totalCost = totalCost,
        isFullTank = isFullTank,
        createdAt = "2026-08-01T00:00:00Z"
    )

    private fun expense(
        id: Int = 1,
        vehicleId: Int = 1,
        category: String = "OTHER",
        amount: Double = 0.0,
        date: String = "2026-08-01",
        description: String? = null
    ) = Expense(
        id = id,
        vehicleId = vehicleId,
        category = category,
        amount = amount,
        date = date,
        description = description,
        createdAt = "2026-08-01T00:00:00Z"
    )

    @Test
    fun `totalFuelExpenditure sums totalCost across all entries`() {
        val entries = listOf(
            fuelEntry(totalCost = 643.29),
            fuelEntry(totalCost = 810.00)
        )
        assertEquals(1453.29, RunningCostsCalculator.totalFuelExpenditure(entries), 0.001)
    }

    @Test
    fun `totalFuelExpenditure returns zero for an empty list`() {
        assertEquals(0.0, RunningCostsCalculator.totalFuelExpenditure(emptyList()), 0.001)
    }

    @Test
    fun `averagePricePerLitre ignores entries with a null price`() {
        val entries = listOf(
            fuelEntry(pricePerLitre = 22.00),
            fuelEntry(pricePerLitre = null),
            fuelEntry(pricePerLitre = 24.00)
        )
        assertEquals(23.00, RunningCostsCalculator.averagePricePerLitre(entries)!!, 0.001)
    }

    @Test
    fun `averagePricePerLitre returns null when no entry has a price recorded`() {
        val entries = listOf(fuelEntry(pricePerLitre = null))
        assertNull(RunningCostsCalculator.averagePricePerLitre(entries))
    }

    @Test
    fun `consumptionHistory calculates correct L per 100km between two full tanks`() {
        val entries = listOf(
            fuelEntry(odometerKm = 10000, litres = 35.0, isFullTank = true, date = "2026-08-01"),
            fuelEntry(odometerKm = 10500, litres = 40.0, isFullTank = true, date = "2026-08-15")
        )
        val history = RunningCostsCalculator.consumptionHistory(entries)

        assertEquals(1, history.size)
        assertEquals(500, history[0].distanceKm)
        assertEquals(40.0, history[0].litresUsed, 0.001)
        assertEquals(8.0, history[0].consumptionLPer100Km, 0.001)
    }

    @Test
    fun `consumptionHistory excludes partial fill-ups entirely`() {
        val entries = listOf(
            fuelEntry(odometerKm = 10000, litres = 35.0, isFullTank = true),
            fuelEntry(odometerKm = 10200, litres = 15.0, isFullTank = false),
            fuelEntry(odometerKm = 10500, litres = 40.0, isFullTank = true)
        )
        val history = RunningCostsCalculator.consumptionHistory(entries)

        assertEquals(1, history.size)
        assertEquals(500, history[0].distanceKm)
    }

    @Test
    fun `consumptionHistory returns empty list with fewer than two full tank entries`() {
        val entries = listOf(fuelEntry(isFullTank = true))
        assertEquals(0, RunningCostsCalculator.consumptionHistory(entries).size)
    }

    @Test
    fun `consumptionHistory skips a pair with identical odometer readings`() {
        val duplicateOdometerEntries = listOf(
            fuelEntry(odometerKm = 10000, litres = 35.0, isFullTank = true),
            fuelEntry(odometerKm = 10000, litres = 40.0, isFullTank = true)
        )
        assertEquals(0, RunningCostsCalculator.consumptionHistory(duplicateOdometerEntries).size)
    }

    @Test
    fun `estimatedConsumptionLPer100Km averages across multiple readings`() {
        val entries = listOf(
            fuelEntry(odometerKm = 10000, litres = 35.0, isFullTank = true),
            fuelEntry(odometerKm = 10500, litres = 40.0, isFullTank = true),
            fuelEntry(odometerKm = 11000, litres = 30.0, isFullTank = true)
        )
        assertEquals(7.0, RunningCostsCalculator.estimatedConsumptionLPer100Km(entries)!!, 0.001)
    }

    @Test
    fun `estimatedConsumptionLPer100Km returns null when it cannot be calculated`() {
        assertNull(RunningCostsCalculator.estimatedConsumptionLPer100Km(emptyList()))
    }

    @Test
    fun `totalExpenseAmount sums amount across all expenses`() {
        val expenses = listOf(expense(amount = 3200.00), expense(amount = 950.00))
        assertEquals(4150.00, RunningCostsCalculator.totalExpenseAmount(expenses), 0.001)
    }

    @Test
    fun `expenseTotalsByCategory groups and sums correctly`() {
        val expenses = listOf(
            expense(category = "TYRES", amount = 3200.00),
            expense(category = "INSURANCE", amount = 950.00),
            expense(category = "TYRES", amount = 500.00)
        )
        val totals = RunningCostsCalculator.expenseTotalsByCategory(expenses)

        assertEquals(3700.00, totals["TYRES"]!!, 0.001)
        assertEquals(950.00, totals["INSURANCE"]!!, 0.001)
    }

    @Test
    fun `totalRunningCost adds fuel and expense totals together`() {
        val fuelEntries = listOf(fuelEntry(totalCost = 643.29))
        val expenses = listOf(expense(amount = 3200.00))

        assertEquals(3843.29, RunningCostsCalculator.totalRunningCost(fuelEntries, expenses), 0.001)
    }

    @Test
    fun `fuelEntriesInPeriod includes entries on the boundary dates`() {
        val entries = listOf(
            fuelEntry(date = "2026-01-01"),
            fuelEntry(date = "2026-06-15"),
            fuelEntry(date = "2026-08-20"),
            fuelEntry(date = "2026-09-01")
        )
        val filtered = RunningCostsCalculator.fuelEntriesInPeriod(
            entries,
            "2026-01-01",
            "2026-08-20"
        )
        assertEquals(3, filtered.size)
    }

    @Test
    fun `fuelEntriesInPeriod handles a full ISO datetime string, not just a plain date`() {
        val entries = listOf(fuelEntry(date = "2026-08-01T00:00:00Z"))
        val filtered = RunningCostsCalculator.fuelEntriesInPeriod(
            entries,
            "2026-08-01",
            "2026-08-31"
        )
        assertEquals(1, filtered.size)
    }

    @Test
    fun `buildOverview combines every calculation into a single result`() {
        val fuelEntries = listOf(
            fuelEntry(odometerKm = 10000, litres = 35.0, totalCost = 800.00, pricePerLitre = 22.86, isFullTank = true),
            fuelEntry(odometerKm = 10500, litres = 40.0, totalCost = 900.00, pricePerLitre = 22.50, isFullTank = true)
        )
        val expenses = listOf(expense(category = "TYRES", amount = 3200.00))

        val overview = RunningCostsCalculator.buildOverview(fuelEntries, expenses)

        assertEquals(1700.00, overview.totalFuelExpenditure, 0.001)
        assertEquals(75.0, overview.totalLitresPurchased, 0.001)
        assertEquals(8.0, overview.estimatedConsumptionLPer100Km!!, 0.001)
        assertEquals(3200.00, overview.totalExpenseAmount, 0.001)
        assertEquals(4900.00, overview.totalRunningCost, 0.001)
        assertEquals(1, overview.consumptionHistory.size)
    }
}