package com.drivemate.sa.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivemate.sa.network.MaintenanceRecord
import com.drivemate.sa.repository.ApiMaintenanceRepository
import com.drivemate.sa.repository.MaintenanceRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class MaintenanceViewModel(
    private val repository:
    MaintenanceRepository =
        ApiMaintenanceRepository()
) : ViewModel() {

    val records: StateFlow<List<MaintenanceRecord>> =
        repository.observeRecords()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                repository.refresh()

                Log.i(
                    "DriveMateMaintenance",
                    "Maintenance records loaded successfully"
                )

            } catch (e: Exception) {
                Log.e(
                    "DriveMateMaintenance",
                    "Failed to load maintenance records",
                    e
                )
            }
        }
    }

    fun addRecord(
        record: MaintenanceRecord
    ) {
        viewModelScope.launch {
            try {
                repository.addRecord(record)

                Log.i(
                    "DriveMateMaintenance",
                    "Maintenance record saved successfully"
                )

            } catch (e: Exception) {
                Log.e(
                    "DriveMateMaintenance",
                    "Failed to save maintenance record",
                    e
                )
            }
        }
    }

    fun markCompleted(
        record: MaintenanceRecord
    ) {
        viewModelScope.launch {
            try {
                repository.updateRecord(
                    record.copy(
                        completed = true,
                        nextDueDate = null,
                        date = LocalDate.now()
                    )
                )

                Log.i(
                    "DriveMateMaintenance",
                    "Maintenance record marked completed"
                )

            } catch (e: Exception) {
                Log.e(
                    "DriveMateMaintenance",
                    "Failed to update maintenance record",
                    e
                )
            }
        }
    }

    fun deleteRecord(
        id: String
    ) {
        viewModelScope.launch {
            try {
                repository.deleteRecord(id)

                Log.i(
                    "DriveMateMaintenance",
                    "Maintenance record deleted"
                )

            } catch (e: Exception) {
                Log.e(
                    "DriveMateMaintenance",
                    "Failed to delete maintenance record",
                    e
                )
            }
        }
    }
}