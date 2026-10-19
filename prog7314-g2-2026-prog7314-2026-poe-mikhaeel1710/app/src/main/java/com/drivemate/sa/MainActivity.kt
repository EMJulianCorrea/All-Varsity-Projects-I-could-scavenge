package com.drivemate.sa

import com.drivemate.sa.ui.screens.RunningCostsScreen
import com.drivemate.sa.viewmodels.RunningCostsViewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drivemate.sa.ui.screens.MaintenanceScreen
import com.drivemate.sa.viewmodels.MaintenanceViewModel
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.drivemate.sa.network.CreateCloudTestRequest
import com.drivemate.sa.network.RetrofitClient
import com.drivemate.sa.network.Vehicle
import com.drivemate.sa.ui.theme.DriveMateTheme
import com.drivemate.sa.ui.vehicles.AddVehicleScreen
import com.drivemate.sa.ui.vehicles.VehicleProfileScreen
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG_APP = "DriveMateApp"
        private const val TAG_AUTH = "DriveMateAuth"
        private const val TAG_API = "DriveMateApi"
        private const val TAG_CLOUD = "DriveMateCloud"
        private const val TAG_VEHICLE = "DriveMateVehicle"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)
    private var refreshUi by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(
            TAG_APP,
            "DriveMate application started"
        )

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        if (auth.currentUser != null) {
            Log.i(
                TAG_AUTH,
                "Existing Firebase session detected"
            )
        } else {
            Log.d(
                TAG_AUTH,
                "No existing Firebase session"
            )
        }

        setContent {
            DriveMateTheme {

                val ignored = refreshUi
                val currentUser = auth.currentUser

                var apiStatus by remember {
                    mutableStateOf("Checking API...")
                }

                var cloudStatus by remember {
                    mutableStateOf(
                        "Cloud database not tested yet"
                    )
                }

                var cloudLoading by remember {
                    mutableStateOf(false)
                }

                var showAddVehicle by remember {
                    mutableStateOf(false)
                }
                var showMaintenance by remember {
                    mutableStateOf(false)
                }
                var showRunningCosts by remember {
                    mutableStateOf(false)
                }
                var vehicleSaveStatus by remember {
                    mutableStateOf<String?>(null)
                }

                var vehicleSaving by remember {
                    mutableStateOf(false)
                }

                var vehicles by remember {
                    mutableStateOf<List<Vehicle>>(
                        emptyList()
                    )
                }

                var vehiclesLoading by remember {
                    mutableStateOf(false)
                }

                var vehiclesError by remember {
                    mutableStateOf<String?>(null)
                }

                var vehicleRefreshKey by remember {
                    mutableStateOf(0)
                }

                LaunchedEffect(Unit) {

                    Log.d(
                        TAG_API,
                        "Checking DriveMate API health"
                    )

                    apiStatus = try {
                        val response =
                            RetrofitClient.api.getHealth()

                        Log.i(
                            TAG_API,
                            "API health check successful: " +
                                    "${response.service} ${response.status}"
                        )

                        "${response.service}: ${response.status}"

                    } catch (e: Exception) {

                        Log.e(
                            TAG_API,
                            "API health check failed",
                            e
                        )

                        "API connection failed"
                    }
                }

                LaunchedEffect(
                    currentUser?.uid,
                    vehicleRefreshKey
                ) {
                    if (currentUser == null) {

                        Log.d(
                            TAG_VEHICLE,
                            "No signed-in user. Clearing vehicle list"
                        )

                        vehicles = emptyList()
                        vehiclesLoading = false
                        vehiclesError = null

                    } else {

                        Log.d(
                            TAG_VEHICLE,
                            "Loading vehicles for signed-in user"
                        )

                        vehiclesLoading = true
                        vehiclesError = null

                        try {
                            vehicles =
                                RetrofitClient.api
                                    .getVehiclesForUser(
                                        currentUser.uid
                                    )

                            Log.i(
                                TAG_VEHICLE,
                                "Vehicles loaded successfully. Count: " +
                                        vehicles.size
                            )

                        } catch (e: Exception) {

                            Log.e(
                                TAG_VEHICLE,
                                "Failed to load vehicles",
                                e
                            )

                            vehiclesError =
                                "Failed to load vehicles."

                        } finally {
                            vehiclesLoading = false

                            Log.d(
                                TAG_VEHICLE,
                                "Vehicle loading operation finished"
                            )
                        }
                    }
                }

                when {
                    currentUser != null &&
                            showRunningCosts -> {

                        val runningCostsViewModel:
                                RunningCostsViewModel = viewModel()

                        val selectedVehicleId by
                        runningCostsViewModel.selectedVehicleId.collectAsState()

                        val fuelEntries by
                        runningCostsViewModel.fuelEntries.collectAsState()

                        val expenses by
                        runningCostsViewModel.expenses.collectAsState()

                        LaunchedEffect(
                            vehicles,
                            selectedVehicleId
                        ) {
                            if (
                                selectedVehicleId == null &&
                                vehicles.isNotEmpty()
                            ) {
                                runningCostsViewModel
                                    .selectVehicle(
                                        vehicles.first().id
                                    )
                            }
                        }

                        RunningCostsScreen(
                            vehicles = vehicles,

                            selectedVehicleId =
                                selectedVehicleId,

                            fuelEntries =
                                fuelEntries,

                            expenses =
                                expenses,

                            onSelectVehicle = {
                                runningCostsViewModel
                                    .selectVehicle(it)
                            },

                            onAddFuelEntry = {
                                runningCostsViewModel
                                    .addFuelEntry(it)
                            },

                            onAddExpense = {
                                runningCostsViewModel
                                    .addExpense(it)
                            },

                            onBack = {
                                showRunningCosts = false
                            }
                        )
                    }
                    currentUser != null &&
                            showMaintenance -> {

                        val maintenanceViewModel:
                                MaintenanceViewModel = viewModel()

                        val maintenanceRecords by
                        maintenanceViewModel.records.collectAsState()

                        MaintenanceScreen(
                            records = maintenanceRecords,

                            onAddRecord = {
                                maintenanceViewModel.addRecord(it)
                            },

                            onMarkCompleted = {
                                maintenanceViewModel.markCompleted(it)
                            },

                            onDeleteRecord = {
                                maintenanceViewModel.deleteRecord(it)
                            },

                            onBack = {
                                showMaintenance = false
                            }
                        )
                    }
                    currentUser != null &&
                            showAddVehicle -> {

                        AddVehicleScreen(
                            firebaseUserId =
                                currentUser.uid,

                            onSaveVehicle = { request ->

                                lifecycleScope.launch {

                                    Log.i(
                                        TAG_VEHICLE,
                                        "Vehicle save requested"
                                    )

                                    vehicleSaving = true
                                    vehicleSaveStatus = null

                                    try {
                                        val vehicle =
                                            RetrofitClient.api
                                                .createVehicle(
                                                    request
                                                )

                                        Log.i(
                                            TAG_VEHICLE,
                                            "Vehicle saved successfully: " +
                                                    "${vehicle.make} " +
                                                    vehicle.model
                                        )

                                        vehicleSaveStatus =
                                            "Vehicle saved: " +
                                                    "${vehicle.make} " +
                                                    vehicle.model

                                        showAddVehicle = false
                                        vehicleRefreshKey++

                                    } catch (e: Exception) {

                                        Log.e(
                                            TAG_VEHICLE,
                                            "Failed to save vehicle",
                                            e
                                        )

                                        vehicleSaveStatus =
                                            "Failed to save vehicle."

                                    } finally {
                                        vehicleSaving = false

                                        Log.d(
                                            TAG_VEHICLE,
                                            "Vehicle save operation finished"
                                        )
                                    }
                                }
                            },

                            onBack = {
                                Log.d(
                                    TAG_VEHICLE,
                                    "User exited Add Vehicle screen"
                                )

                                vehicleSaveStatus = null
                                showAddVehicle = false
                            },

                            saveStatus =
                                vehicleSaveStatus,

                            isSaving =
                                vehicleSaving
                        )
                    }

                    currentUser != null -> {

                        VehicleProfileScreen(

                            vehicles = vehicles,

                            isLoading =
                                vehiclesLoading,

                            errorMessage =
                                vehiclesError,

                            statusMessage =
                                vehicleSaveStatus,

                            onOpenMaintenance = {

                                Log.d(
                                    TAG_VEHICLE,
                                    "Opening Vehicle Health screen"
                                )

                                showAddVehicle = false
                                showRunningCosts = false
                                showMaintenance = true
                            },
                            onOpenRunningCosts = {

                                Log.d(
                                    TAG_VEHICLE,
                                    "Opening Running Costs screen"
                                )

                                showAddVehicle = false
                                showMaintenance = false
                                showRunningCosts = true
                            },
                            onAddVehicle = {

                                Log.d(
                                    TAG_VEHICLE,
                                    "Opening Add Vehicle screen"
                                )

                                vehicleSaveStatus = null
                                showMaintenance = false
                                showAddVehicle = true
                                showRunningCosts = false
                            },


                            onRefresh = {

                                Log.i(
                                    TAG_VEHICLE,
                                    "Manual vehicle refresh requested"
                                )

                                vehicleRefreshKey++
                            },
                            onSignOut = {

                                Log.i(
                                    TAG_AUTH,
                                    "User signing out"
                                )

                                auth.signOut()

                                errorMessage = null
                                vehicleSaveStatus = null
                                vehicles = emptyList()
                                showAddVehicle = false
                                showMaintenance = false
                                showRunningCosts = false

                                refreshUi++

                                Log.i(
                                    TAG_AUTH,
                                    "User signed out successfully"
                                )
                            }
                        )
                    }

                    else -> {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),

                            verticalArrangement =
                                Arrangement.Center,

                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "DriveMate",

                                style =
                                    MaterialTheme.typography
                                        .headlineLarge,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    "Your vehicle companion",

                                style =
                                    MaterialTheme.typography
                                        .bodyLarge
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Text(
                                text = apiStatus,

                                style =
                                    MaterialTheme.typography
                                        .bodyMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(16.dp)
                            )

                            Button(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                enabled =
                                    !cloudLoading,

                                onClick = {
                                    lifecycleScope.launch {

                                        Log.i(
                                            TAG_CLOUD,
                                            "Cloud database test started"
                                        )

                                        cloudLoading = true

                                        cloudStatus = try {

                                            RetrofitClient.api
                                                .createCloudTestRecord(
                                                    CreateCloudTestRequest(
                                                        message =
                                                            "Hello from DriveMate Android"
                                                    )
                                                )

                                            Log.d(
                                                TAG_CLOUD,
                                                "Cloud test record created"
                                            )

                                            val records =
                                                RetrofitClient.api
                                                    .getCloudTestRecords()

                                            Log.d(
                                                TAG_CLOUD,
                                                "Cloud test records retrieved. Count: " +
                                                        records.size
                                            )

                                            val latestRecord =
                                                records.firstOrNull()

                                            if (
                                                latestRecord != null
                                            ) {

                                                Log.i(
                                                    TAG_CLOUD,
                                                    "Cloud database test successful"
                                                )

                                                "Cloud DB connected: " +
                                                        latestRecord.message

                                            } else {

                                                Log.w(
                                                    TAG_CLOUD,
                                                    "Cloud record created but no records returned"
                                                )

                                                "Record created, but no records returned."
                                            }

                                        } catch (
                                            e: Exception
                                        ) {

                                            Log.e(
                                                TAG_CLOUD,
                                                "Cloud database test failed",
                                                e
                                            )

                                            "Cloud database test failed"

                                        } finally {
                                            cloudLoading = false

                                            Log.d(
                                                TAG_CLOUD,
                                                "Cloud database test finished"
                                            )
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    if (cloudLoading) {
                                        "Testing cloud database..."
                                    } else {
                                        "Test Cloud Database"
                                    }
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Text(
                                text = cloudStatus,

                                style =
                                    MaterialTheme.typography
                                        .bodyMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(40.dp)
                            )

                            Text(
                                text =
                                    "Sign in to continue",

                                style =
                                    MaterialTheme.typography
                                        .titleMedium
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(20.dp)
                            )

                            Button(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                enabled =
                                    !isLoading,

                                onClick = {

                                    Log.d(
                                        TAG_AUTH,
                                        "Google sign-in button selected"
                                    )

                                    signInWithGoogle()
                                }
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator()
                                } else {
                                    Text(
                                        "Sign in with Google"
                                    )
                                }
                            }

                            errorMessage?.let {
                                    message ->

                                Spacer(
                                    modifier =
                                        Modifier.height(16.dp)
                                )

                                Text(
                                    text = message,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun signInWithGoogle() {

        Log.i(
            TAG_AUTH,
            "Starting Google credential request"
        )

        isLoading = true
        errorMessage = null

        val signInWithGoogleOption =
            GetSignInWithGoogleOption.Builder(
                serverClientId =
                    getString(
                        R.string.default_web_client_id
                    )
            ).build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(
                    signInWithGoogleOption
                )
                .build()

        lifecycleScope.launch {

            try {
                val result =
                    credentialManager.getCredential(
                        context =
                            this@MainActivity,

                        request = request
                    )

                Log.d(
                    TAG_AUTH,
                    "Credential Manager returned a credential"
                )

                val credential =
                    result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    Log.d(
                        TAG_AUTH,
                        "Google ID token credential received"
                    )

                    val googleCredential =
                        GoogleIdTokenCredential
                            .createFrom(
                                credential.data
                            )

                    firebaseAuthWithGoogle(
                        googleCredential.idToken
                    )

                } else {

                    Log.w(
                        TAG_AUTH,
                        "Unsupported credential type returned"
                    )

                    isLoading = false

                    errorMessage =
                        "Google sign-in returned an unsupported credential."
                }

            } catch (e: Exception) {

                Log.e(
                    TAG_AUTH,
                    "Google sign-in failed",
                    e
                )

                isLoading = false

                errorMessage =
                    "Google sign-in failed. Please try again."
            }
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String
    ) {

        Log.d(
            TAG_AUTH,
            "Authenticating Google credential with Firebase"
        )

        val firebaseCredential =
            GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(
            firebaseCredential
        ).addOnCompleteListener(this) {
                task ->

            isLoading = false

            if (task.isSuccessful) {

                Log.i(
                    TAG_AUTH,
                    "Firebase Google authentication successful"
                )

                errorMessage = null
                refreshUi++

            } else {

                Log.e(
                    TAG_AUTH,
                    "Firebase authentication failed",
                    task.exception
                )

                errorMessage =
                    task.exception
                        ?.localizedMessage
                        ?: "Firebase authentication failed."
            }
        }
    }
}