package com.drivemate.sa.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DriveMateApiService {

    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @POST("api/cloud-test")
    suspend fun createCloudTestRecord(
        @Body request: CreateCloudTestRequest
    ): CloudTestRecord

    @GET("api/cloud-test")
    suspend fun getCloudTestRecords(): List<CloudTestRecord>

    // Vehicles

    @POST("api/vehicles")
    suspend fun createVehicle(
        @Body request: CreateVehicleRequest
    ): Vehicle

    @GET("api/vehicles/user/{firebaseUserId}")
    suspend fun getVehiclesForUser(
        @Path("firebaseUserId") firebaseUserId: String
    ): List<Vehicle>

    // Maintenance

    @GET("api/maintenance/user/{firebaseUserId}")
    suspend fun getMaintenanceRecordsForUser(
        @Path("firebaseUserId") firebaseUserId: String
    ): List<MaintenanceRecordResponse>

    @POST("api/maintenance")
    suspend fun createMaintenanceRecord(
        @Body request: MaintenanceRecordRequest
    ): MaintenanceRecordResponse

    @PUT("api/maintenance/{id}")
    suspend fun updateMaintenanceRecord(
        @Path("id") id: String,
        @Body request: MaintenanceRecordRequest
    ): MaintenanceRecordResponse

    @DELETE("api/maintenance/{id}")
    suspend fun deleteMaintenanceRecord(
        @Path("id") id: String
    ): Response<Unit>

    // Running Costs: fuel entries

    @POST("api/fuel-entries")
    suspend fun createFuelEntry(
        @Body request: CreateFuelEntryRequest
    ): FuelEntry

    @GET("api/fuel-entries/vehicle/{vehicleId}")
    suspend fun getFuelEntriesForVehicle(
        @Path("vehicleId") vehicleId: Int
    ): List<FuelEntry>

    @PUT("api/fuel-entries/{id}")
    suspend fun updateFuelEntry(
        @Path("id") id: Int,
        @Body request: CreateFuelEntryRequest
    ): FuelEntry

    @DELETE("api/fuel-entries/{id}")
    suspend fun deleteFuelEntry(
        @Path("id") id: Int
    )

    // Running Costs: expenses

    @POST("api/expenses")
    suspend fun createExpense(
        @Body request: CreateExpenseRequest
    ): Expense

    @GET("api/expenses/vehicle/{vehicleId}")
    suspend fun getExpensesForVehicle(
        @Path("vehicleId") vehicleId: Int
    ): List<Expense>

    @PUT("api/expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: Int,
        @Body request: CreateExpenseRequest
    ): Expense

    @DELETE("api/expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") id: Int
    )
}