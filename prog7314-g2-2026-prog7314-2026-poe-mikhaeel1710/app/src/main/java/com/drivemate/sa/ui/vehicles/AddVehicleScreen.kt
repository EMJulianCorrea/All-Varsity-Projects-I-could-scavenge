package com.drivemate.sa.ui.vehicles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.drivemate.sa.network.CreateVehicleRequest

@Composable
fun AddVehicleScreen(
    firebaseUserId: String,
    onSaveVehicle: (CreateVehicleRequest) -> Unit,
    onBack: () -> Unit,
    saveStatus: String? = null,
    isSaving: Boolean = false
) {
    var nickname by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var registrationNumber by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var odometerKm by remember { mutableStateOf("") }
    var licenceRenewalDate by remember { mutableStateOf("") }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add Vehicle",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "South African vehicle profile",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Vehicle nickname") },
            placeholder = { Text("e.g. Daily Car") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = make,
            onValueChange = { make = it },
            label = { Text("Make") },
            placeholder = { Text("e.g. Toyota") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Model") },
            placeholder = { Text("e.g. Corolla") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = year,
            onValueChange = {
                year = it.filter { character ->
                    character.isDigit()
                }
            },
            label = { Text("Year") },
            placeholder = { Text("e.g. 2022") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = registrationNumber,
            onValueChange = {
                registrationNumber = it.uppercase()
            },
            label = { Text("Registration number") },
            placeholder = { Text("e.g. ND 123-456") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = fuelType,
            onValueChange = { fuelType = it },
            label = { Text("Fuel type") },
            placeholder = {
                Text("Petrol / Diesel / Hybrid / Electric")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = odometerKm,
            onValueChange = {
                odometerKm = it.filter { character ->
                    character.isDigit()
                }
            },
            label = { Text("Current odometer (km)") },
            placeholder = { Text("e.g. 45000") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = licenceRenewalDate,
            onValueChange = { licenceRenewalDate = it },
            label = { Text("Licence renewal date") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        saveStatus?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving,
            onClick = {
                val yearValue = year.toIntOrNull()
                val odometerValue = odometerKm.toIntOrNull()

                when {
                    make.isBlank() -> {
                        errorMessage =
                            "Please enter the vehicle make."
                    }

                    model.isBlank() -> {
                        errorMessage =
                            "Please enter the vehicle model."
                    }

                    yearValue == null -> {
                        errorMessage =
                            "Please enter a valid vehicle year."
                    }

                    registrationNumber.isBlank() -> {
                        errorMessage =
                            "Please enter the registration number."
                    }

                    fuelType.isBlank() -> {
                        errorMessage =
                            "Please enter the fuel type."
                    }

                    odometerValue == null -> {
                        errorMessage =
                            "Please enter a valid odometer reading."
                    }

                    licenceRenewalDate.isBlank() -> {
                        errorMessage =
                            "Please enter the licence renewal date."
                    }

                    !licenceRenewalDate.matches(
                        Regex("""\d{4}-\d{2}-\d{2}""")
                    ) -> {
                        errorMessage =
                            "Use YYYY-MM-DD for the licence renewal date."
                    }

                    else -> {
                        errorMessage = null

                        onSaveVehicle(
                            CreateVehicleRequest(
                                firebaseUserId = firebaseUserId,
                                nickname = nickname.trim(),
                                make = make.trim(),
                                model = model.trim(),
                                year = yearValue,
                                registrationNumber =
                                    registrationNumber.trim(),
                                fuelType = fuelType.trim(),
                                odometerKm = odometerValue,
                                licenceRenewalDate =
                                    "${licenceRenewalDate}T00:00:00Z"
                            )
                        )
                    }
                }
            }
        ) {
            Text(
                if (isSaving) {
                    "Saving Vehicle..."
                } else {
                    "Save Vehicle"
                }
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving,
            onClick = onBack
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}