package com.drivemate.sa.ui.vehicles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drivemate.sa.network.Vehicle
import com.drivemate.sa.util.RenewalStatus
import com.drivemate.sa.util.VehicleRenewalUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun VehicleProfileScreen(
    vehicles: List<Vehicle>,
    isLoading: Boolean,
    errorMessage: String?,
    statusMessage: String?,
    onAddVehicle: () -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    onOpenMaintenance: () -> Unit,
    onOpenRunningCosts: () -> Unit,
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "My Vehicles",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "South African vehicle profiles",
            style = MaterialTheme.typography.bodyMedium
        )

        statusMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        when {
            isLoading -> {
                Text("Loading vehicles...")
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRefresh
                ) {
                    Text("Try Again")
                }
            }

            vehicles.isEmpty() -> {
                Text(
                    text = "You have not added any vehicles yet."
                )
            }

            else -> {
                vehicles.forEach { vehicle ->
                    VehicleCard(vehicle)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onAddVehicle
        ) {
            Text("Add Vehicle")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenMaintenance
        ) {
            Text("Vehicle Health")
        }
        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = onOpenRunningCosts,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Running Costs")
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSignOut
        ) {
            Text("Sign out")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle
) {
    val numberFormatter =
        NumberFormat.getIntegerInstance(
            Locale("en", "ZA")
        )

    val daysRemaining =
        VehicleRenewalUtils.daysUntilRenewal(
            vehicle.licenceRenewalDate
        )

    val formattedRenewalDate =
        formatRenewalDate(
            vehicle.licenceRenewalDate
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = vehicle.nickname.ifBlank {
                    "${vehicle.make} ${vehicle.model}"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "${vehicle.make} ${vehicle.model} • ${vehicle.year}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = vehicle.registrationNumber,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text =
                    "${numberFormatter.format(vehicle.odometerKm)} km"
            )

            Text(
                text = "Fuel: ${vehicle.fuelType}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Licence renewal",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formattedRenewalDate
            )

            if (daysRemaining == null) {
                Text(
                    text = "Unable to calculate renewal status.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                when (
                    VehicleRenewalUtils.renewalStatus(
                        daysRemaining
                    )
                ) {
                    RenewalStatus.OVERDUE -> {
                        Text(
                            text =
                                "Licence renewal overdue by " +
                                        "${-daysRemaining} days",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    RenewalStatus.DUE_TODAY -> {
                        Text(
                            text = "Licence renewal is due today",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    RenewalStatus.DUE_SOON -> {
                        Text(
                            text =
                                "Licence renewal due soon: " +
                                        "$daysRemaining days remaining",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    RenewalStatus.OK -> {
                        Text(
                            text =
                                "$daysRemaining days until renewal"
                        )
                    }
                }
            }
        }
    }
}

private fun formatRenewalDate(
    dateValue: String
): String {
    return try {
        val inputFormatter =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )

        val outputFormatter =
            SimpleDateFormat(
                "d MMM yyyy",
                Locale("en", "ZA")
            )

        val date =
            inputFormatter.parse(
                dateValue.take(10)
            )

        if (date != null) {
            outputFormatter.format(date)
        } else {
            dateValue.take(10)
        }

    } catch (e: Exception) {
        dateValue.take(10)
    }
}