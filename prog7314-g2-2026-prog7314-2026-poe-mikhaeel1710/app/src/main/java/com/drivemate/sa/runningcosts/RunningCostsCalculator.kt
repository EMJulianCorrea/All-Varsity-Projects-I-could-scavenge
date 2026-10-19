package com.drivemate.sa.runningcosts

import com.drivemate.sa.network.Expense
import com.drivemate.sa.network.FuelEntry



// A fuel efficiency reading between two consecutive full-tank fill-ups
data class ConsumptionReading(
    val date: String,
    val distanceKm: Int,
    val litresUsed: Double,
    val consumptionLPer100Km: Double
)


// Combined totals and stats for the Running Costs overview
data class RunningCostsOverview(
    val totalFuelExpenditure: Double,
    val totalLitresPurchased: Double,
    val averagePricePerLitre: Double?,
    val estimatedConsumptionLPer100Km: Double?,
    val consumptionHistory: List<ConsumptionReading>,
    val totalExpenseAmount: Double,
    val expenseTotalsByCategory: Map<String, Double>,
    val totalRunningCost: Double
)
// Calculates fuel and expense totals, no Android dependencies so it's easy to unit test
object RunningCostsCalculator {

    fun totalFuelExpenditure(fuelEntries: List<FuelEntry>): Double =
        fuelEntries.sumOf { it.totalCost }

    fun totalLitresPurchased(fuelEntries: List<FuelEntry>): Double =
        fuelEntries.sumOf { it.litres }

    // Ignores entries that didn't record a price
    fun averagePricePerLitre(fuelEntries: List<FuelEntry>): Double? {
        val pricesRecorded = fuelEntries.mapNotNull { it.pricePerLitre }
        if (pricesRecorded.isEmpty()) return null
        return pricesRecorded.average()
    }

    // Only full tanks are used, since a partial fill-up understates litres used
    fun consumptionHistory(fuelEntries: List<FuelEntry>): List<ConsumptionReading> {
        val fullTankEntriesSorted = fuelEntries
            .filter { it.isFullTank }
            .sortedBy { it.odometerKm }

        if (fullTankEntriesSorted.size < 2) return emptyList()

        val readings = mutableListOf<ConsumptionReading>()
        for (i in 1 until fullTankEntriesSorted.size) {
            val previous = fullTankEntriesSorted[i - 1]
            val current = fullTankEntriesSorted[i]

            val distanceKm = current.odometerKm - previous.odometerKm
            if (distanceKm <= 0) continue

            val consumption = (current.litres / distanceKm) * 100.0

            readings.add(
                ConsumptionReading(
                    date = current.date,
                    distanceKm = distanceKm,
                    litresUsed = current.litres,
                    consumptionLPer100Km = consumption
                ))
        }
        return readings
    }


    fun estimatedConsumptionLPer100Km(fuelEntries: List<FuelEntry>): Double? {
        val readings = consumptionHistory(fuelEntries)
        if (readings.isEmpty()) return null
        return readings.map { it.consumptionLPer100Km }.average()
    }

    fun totalExpenseAmount(expenses: List<Expense>): Double =  expenses.sumOf { it.amount }

    // Groups and sums expenses per category
    fun expenseTotalsByCategory(expenses: List<Expense>): Map<String, Double> =
        expenses.groupingBy { it.category }.fold(0.0) { total, expense -> total + expense.amount }

    fun totalRunningCost(fuelEntries: List<FuelEntry>, expenses: List<Expense>): Double = totalFuelExpenditure(fuelEntries) + totalExpenseAmount(expenses)


    // Builds everything an overview screen needs in 1 call
    fun buildOverview(fuelEntries: List<FuelEntry>, expenses: List<Expense>): RunningCostsOverview {
        val consumption = consumptionHistory(fuelEntries)
        return RunningCostsOverview(
            totalFuelExpenditure = totalFuelExpenditure(fuelEntries),
            totalLitresPurchased = totalLitresPurchased(fuelEntries),
            averagePricePerLitre = averagePricePerLitre(fuelEntries),
            estimatedConsumptionLPer100Km = if (consumption.isEmpty()) null else consumption.map { it.consumptionLPer100Km }.average(),
            consumptionHistory = consumption,
            totalExpenseAmount = totalExpenseAmount(expenses),
            expenseTotalsByCategory = expenseTotalsByCategory(expenses),
            totalRunningCost = totalRunningCost(fuelEntries, expenses)
        )

    }

    private fun normalizedDate(rawDate: String): String = rawDate.take(10)

    fun fuelEntriesInPeriod(
        fuelEntries: List<FuelEntry>,
        fromInclusive: String,
        toInclusive: String
    ): List<FuelEntry> = fuelEntries.filter { entry ->
        val d = normalizedDate(entry.date)
        d >= fromInclusive && d <= toInclusive
    }


    fun expensesInPeriod(
        expenses: List<Expense>,
        fromInclusive: String,
        toInclusive: String
    ): List<Expense> = expenses.filter { expense ->
        val d = normalizedDate(expense.date)
        d >= fromInclusive && d <= toInclusive
    }
}