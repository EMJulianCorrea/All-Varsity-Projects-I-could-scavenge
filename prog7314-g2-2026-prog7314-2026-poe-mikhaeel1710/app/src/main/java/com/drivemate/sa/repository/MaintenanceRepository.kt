package com.drivemate.sa.repository

import com.drivemate.sa.network.DriveMateApiService
import com.drivemate.sa.network.MaintenanceRecord
import com.drivemate.sa.network.MaintenanceRecordRequest
import com.drivemate.sa.network.MaintenanceRecordResponse
import com.drivemate.sa.network.MaintenanceType
import com.drivemate.sa.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

interface MaintenanceRepository {
    fun observeRecords(): StateFlow<List<MaintenanceRecord>>

    suspend fun refresh()

    suspend fun addRecord(
        record: MaintenanceRecord
    )

    suspend fun updateRecord(
        record: MaintenanceRecord
    )

    suspend fun deleteRecord(
        id: String
    )
}

class ApiMaintenanceRepository(
    private val api: DriveMateApiService =
        RetrofitClient.api,

    private val auth: FirebaseAuth =
        FirebaseAuth.getInstance()
) : MaintenanceRepository {

    private val records =
        MutableStateFlow<List<MaintenanceRecord>>(
            emptyList()
        )

    override fun observeRecords():
            StateFlow<List<MaintenanceRecord>> =
        records.asStateFlow()

    override suspend fun refresh() {
        val userId = requireUserId()

        records.value =
            api.getMaintenanceRecordsForUser(
                userId
            ).map {
                it.toUiModel()
            }
    }

    override suspend fun addRecord(
        record: MaintenanceRecord
    ) {
        val userId = requireUserId()

        val saved =
            api.createMaintenanceRecord(
                record.toApiRequest(userId)
            )

        records.value =
            records.value + saved.toUiModel()
    }

    override suspend fun updateRecord(
        record: MaintenanceRecord
    ) {
        val userId = requireUserId()

        val updated =
            api.updateMaintenanceRecord(
                id = record.id,
                request =
                    record.toApiRequest(userId)
            ).toUiModel()

        records.value =
            records.value.map {
                if (it.id == updated.id) {
                    updated
                } else {
                    it
                }
            }
    }

    override suspend fun deleteRecord(
        id: String
    ) {
        val response =
            api.deleteMaintenanceRecord(id)

        if (!response.isSuccessful) {
            throw IllegalStateException(
                "Failed to delete maintenance record."
            )
        }

        records.value =
            records.value.filterNot {
                it.id == id
            }
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException(
                "No signed-in Firebase user."
            )
    }
}

private fun MaintenanceRecord.toApiRequest(
    firebaseUserId: String
): MaintenanceRecordRequest {
    return MaintenanceRecordRequest(
        firebaseUserId = firebaseUserId,
        type = type.name,
        title = title,
        date = date.toString(),
        mileageKm = mileageKm,
        cost = cost,
        notes = notes,
        nextDueDate =
            nextDueDate?.toString(),
        completed = completed
    )
}

private fun MaintenanceRecordResponse.toUiModel():
        MaintenanceRecord {

    val maintenanceType =
        runCatching {
            MaintenanceType.valueOf(
                type.uppercase()
            )
        }.getOrDefault(
            MaintenanceType.OTHER
        )

    return MaintenanceRecord(
        id = id,
        type = maintenanceType,
        title = title,
        date = LocalDate.parse(date),
        mileageKm = mileageKm,
        cost = cost,
        notes = notes,
        nextDueDate =
            nextDueDate?.let {
                LocalDate.parse(it)
            },
        completed = completed
    )
}