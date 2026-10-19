package com.drivemate.sa.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivemate.sa.network.CreateExpenseRequest
import com.drivemate.sa.network.CreateFuelEntryRequest
import com.drivemate.sa.network.Expense
import com.drivemate.sa.network.FuelEntry
import com.drivemate.sa.repository.ApiRunningCostsRepository
import com.drivemate.sa.repository.RunningCostsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RunningCostsViewModel(
    private val repository: RunningCostsRepository =
        ApiRunningCostsRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "DriveMateRunningCosts"
    }

    private val _selectedVehicleId =
        MutableStateFlow<Int?>(null)

    val selectedVehicleId: StateFlow<Int?> =
        _selectedVehicleId.asStateFlow()

    private val _fuelEntries =
        MutableStateFlow<List<FuelEntry>>(emptyList())

    val fuelEntries: StateFlow<List<FuelEntry>> =
        _fuelEntries.asStateFlow()

    private val _expenses =
        MutableStateFlow<List<Expense>>(emptyList())

    val expenses: StateFlow<List<Expense>> =
        _expenses.asStateFlow()

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()

    fun selectVehicle(vehicleId: Int) {
        if (_selectedVehicleId.value == vehicleId) {
            return
        }

        _selectedVehicleId.value = vehicleId

        Log.d(
            TAG,
            "Selected vehicle $vehicleId"
        )

        refresh()
    }

    fun refresh() {
        val vehicleId =
            _selectedVehicleId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                _fuelEntries.value =
                    repository.getFuelEntries(vehicleId)

                _expenses.value =
                    repository.getExpenses(vehicleId)

                Log.i(
                    TAG,
                    "Running costs loaded for vehicle $vehicleId"
                )
            } catch (e: Exception) {
                _errorMessage.value =
                    "Unable to load running costs."

                Log.e(
                    TAG,
                    "Failed to load running costs for vehicle $vehicleId",
                    e
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFuelEntry(
        request: CreateFuelEntryRequest
    ) {
        viewModelScope.launch {
            _errorMessage.value = null

            try {
                val saved =
                    repository.addFuelEntry(request)

                _fuelEntries.value =
                    (_fuelEntries.value + saved)
                        .sortedByDescending { it.date }

                Log.i(
                    TAG,
                    "Fuel entry saved successfully"
                )
            } catch (e: Exception) {
                _errorMessage.value =
                    "Unable to save fuel entry."

                Log.e(
                    TAG,
                    "Failed to save fuel entry",
                    e
                )
            }
        }
    }

    fun addExpense(
        request: CreateExpenseRequest
    ) {
        viewModelScope.launch {
            _errorMessage.value = null

            try {
                val saved =
                    repository.addExpense(request)

                _expenses.value =
                    (_expenses.value + saved)
                        .sortedByDescending { it.date }

                Log.i(
                    TAG,
                    "Expense saved successfully"
                )
            } catch (e: Exception) {
                _errorMessage.value =
                    "Unable to save expense."

                Log.e(
                    TAG,
                    "Failed to save expense",
                    e
                )
            }
        }
    }
}