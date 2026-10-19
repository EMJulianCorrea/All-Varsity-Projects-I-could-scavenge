package com.drivemate.sa.repository

import com.drivemate.sa.network.CreateExpenseRequest
import com.drivemate.sa.network.CreateFuelEntryRequest
import com.drivemate.sa.network.DriveMateApiService
import com.drivemate.sa.network.Expense
import com.drivemate.sa.network.FuelEntry
import com.drivemate.sa.network.RetrofitClient

interface RunningCostsRepository {

    suspend fun getFuelEntries(
        vehicleId: Int
    ): List<FuelEntry>

    suspend fun getExpenses(
        vehicleId: Int
    ): List<Expense>

    suspend fun addFuelEntry(
        request: CreateFuelEntryRequest
    ): FuelEntry

    suspend fun addExpense(
        request: CreateExpenseRequest
    ): Expense
}

class ApiRunningCostsRepository(
    private val api: DriveMateApiService = RetrofitClient.api
) : RunningCostsRepository {

    override suspend fun getFuelEntries(
        vehicleId: Int
    ): List<FuelEntry> =
        api.getFuelEntriesForVehicle(vehicleId)

    override suspend fun getExpenses(
        vehicleId: Int
    ): List<Expense> =
        api.getExpensesForVehicle(vehicleId)

    override suspend fun addFuelEntry(
        request: CreateFuelEntryRequest
    ): FuelEntry =
        api.createFuelEntry(request)

    override suspend fun addExpense(
        request: CreateExpenseRequest
    ): Expense =
        api.createExpense(request)
}