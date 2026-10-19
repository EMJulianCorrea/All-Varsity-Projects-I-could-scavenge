package com.drivemate.sa.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drivemate.sa.network.CreateExpenseRequest
import com.drivemate.sa.network.CreateFuelEntryRequest
import com.drivemate.sa.network.Expense
import com.drivemate.sa.network.FuelEntry
import com.drivemate.sa.network.Vehicle
import com.drivemate.sa.runningcosts.RunningCostsCalculator
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun RunningCostsScreen(
    vehicles: List<Vehicle>,
    selectedVehicleId: Int?,
    fuelEntries: List<FuelEntry>,
    expenses: List<Expense>,
    onSelectVehicle: (Int) -> Unit,
    onAddFuelEntry: (CreateFuelEntryRequest) -> Unit,
    onAddExpense: (CreateExpenseRequest) -> Unit,
    onBack: () -> Unit
) {
    var showFuelDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var vehicleMenuExpanded by remember { mutableStateOf(false) }

    val selectedVehicle =
        vehicles.firstOrNull { it.id == selectedVehicleId }
            ?: vehicles.firstOrNull()

    val selectedFuelEntries = selectedVehicle?.let { vehicle ->
        fuelEntries.filter { it.vehicleId == vehicle.id }
    } ?: emptyList()

    val selectedExpenses = selectedVehicle?.let { vehicle ->
        expenses.filter { it.vehicleId == vehicle.id }
    } ?: emptyList()

    val overview = RunningCostsCalculator.buildOverview(
        fuelEntries = selectedFuelEntries,
        expenses = selectedExpenses
    )

    val currencyFormatter =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Running Costs",
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(onClick = onBack) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (vehicles.isEmpty()) {
            Text(
                text = "Add a vehicle before recording running costs.",
                style = MaterialTheme.typography.bodyLarge
            )
            return@Column
        }

        Text(
            text = "Vehicle",
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box {
            OutlinedButton(
                onClick = { vehicleMenuExpanded = true }
            ) {
                Text(
                    selectedVehicle?.let {
                        "${it.nickname} - ${it.registrationNumber}"
                    } ?: "Select vehicle"
                )
            }

            DropdownMenu(
                expanded = vehicleMenuExpanded,
                onDismissRequest = {
                    vehicleMenuExpanded = false
                }
            ) {
                vehicles.forEach { vehicle ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${vehicle.nickname} - ${vehicle.registrationNumber}"
                            )
                        },
                        onClick = {
                            vehicleMenuExpanded = false
                            onSelectVehicle(vehicle.id)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OverviewCard(
            title = "Total Running Cost",
            value = currencyFormatter.format(overview.totalRunningCost)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OverviewCard(
            title = "Fuel Spend",
            value = currencyFormatter.format(overview.totalFuelExpenditure)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OverviewCard(
            title = "Other Expenses",
            value = currencyFormatter.format(overview.totalExpenseAmount)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OverviewCard(
            title = "Fuel Purchased",
            value = String.format(
                Locale.US,
                "%.2f L",
                overview.totalLitresPurchased
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OverviewCard(
            title = "Average Fuel Price",
            value = overview.averagePricePerLitre?.let {
                "${currencyFormatter.format(it)} / L"
            } ?: "Not enough data"
        )

        Spacer(modifier = Modifier.height(10.dp))

        OverviewCard(
            title = "Estimated Consumption",
            value = overview.estimatedConsumptionLPer100Km?.let {
                String.format(
                    Locale.US,
                    "%.2f L/100 km",
                    it
                )
            } ?: "Not enough data"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showFuelDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Fuel")
            }

            Button(
                onClick = { showExpenseDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Expense")
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Fuel History",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedFuelEntries.isEmpty()) {
            Text("No fuel entries recorded yet.")
        } else {
            selectedFuelEntries
                .sortedByDescending { it.date }
                .forEach { entry ->
                    FuelEntryCard(
                        entry = entry,
                        currencyFormatter = currencyFormatter
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Expense History",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedExpenses.isEmpty()) {
            Text("No expenses recorded yet.")
        } else {
            selectedExpenses
                .sortedByDescending { it.date }
                .forEach { expense ->
                    ExpenseCard(
                        expense = expense,
                        currencyFormatter = currencyFormatter
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
        }
    }

    if (showFuelDialog && selectedVehicle != null) {
        AddFuelDialog(
            vehicleId = selectedVehicle.id,
            onDismiss = {
                showFuelDialog = false
            },
            onSave = { request ->
                onAddFuelEntry(request)
                showFuelDialog = false
            }
        )
    }

    if (showExpenseDialog && selectedVehicle != null) {
        AddExpenseDialog(
            vehicleId = selectedVehicle.id,
            onDismiss = {
                showExpenseDialog = false
            },
            onSave = { request ->
                onAddExpense(request)
                showExpenseDialog = false
            }
        )
    }
}

@Composable
private fun OverviewCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun FuelEntryCard(
    entry: FuelEntry,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = entry.date.take(10),
                style = MaterialTheme.typography.titleMedium
            )

            Text("${entry.litres} L")

            Text(
                "${entry.odometerKm} km"
            )

            Text(
                currencyFormatter.format(entry.totalCost)
            )

            entry.pricePerLitre?.let {
                Text(
                    "${currencyFormatter.format(it)} / L"
                )
            }

            Text(
                if (entry.isFullTank) {
                    "Full tank"
                } else {
                    "Partial fill"
                }
            )
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = expense.category,
                style = MaterialTheme.typography.titleMedium
            )

            Text(expense.date.take(10))

            Text(
                currencyFormatter.format(expense.amount)
            )

            if (!expense.description.isNullOrBlank()) {
                Text(expense.description)
            }
        }
    }
}

@Composable
private fun AddFuelDialog(
    vehicleId: Int,
    onDismiss: () -> Unit,
    onSave: (CreateFuelEntryRequest) -> Unit
) {
    var date by remember {
        mutableStateOf(LocalDate.now().toString())
    }

    var odometer by remember {
        mutableStateOf("")
    }

    var litres by remember {
        mutableStateOf("")
    }

    var pricePerLitre by remember {
        mutableStateOf("")
    }

    var isFullTank by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Fuel")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it },
                    label = { Text("Odometer (km)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = litres,
                    onValueChange = { litres = it },
                    label = { Text("Litres") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pricePerLitre,
                    onValueChange = { pricePerLitre = it },
                    label = { Text("Price per litre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFullTank,
                        onCheckedChange = {
                            isFullTank = it
                        }
                    )

                    Text("Full tank")
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val odometerValue = odometer.toIntOrNull()
                    val litresValue = litres.toDoubleOrNull()
                    val priceValue = pricePerLitre.toDoubleOrNull()

                    if (
                        odometerValue == null ||
                        odometerValue < 0 ||
                        litresValue == null ||
                        litresValue <= 0 ||
                        priceValue == null ||
                        priceValue < 0
                    ) {
                        errorMessage =
                            "Enter valid odometer, litres and fuel price."
                    } else {
                        onSave(
                            CreateFuelEntryRequest(
                                vehicleId = vehicleId,
                                date = date,
                                odometerKm = odometerValue,
                                litres = litresValue,
                                pricePerLitre = priceValue,
                                totalCost = litresValue * priceValue,
                                isFullTank = isFullTank
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddExpenseDialog(
    vehicleId: Int,
    onDismiss: () -> Unit,
    onSave: (CreateExpenseRequest) -> Unit
) {
    var category by remember {
        mutableStateOf("")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var date by remember {
        mutableStateOf(LocalDate.now().toString())
    }

    var description by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Expense")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = {
                        Text("Category e.g. Insurance, Tyres")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (R)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()

                    if (
                        category.isBlank() ||
                        amountValue == null ||
                        amountValue < 0
                    ) {
                        errorMessage =
                            "Enter a category and valid amount."
                    } else {
                        onSave(
                            CreateExpenseRequest(
                                vehicleId = vehicleId,
                                category = category.trim().uppercase(),
                                amount = amountValue,
                                date = date,
                                description =
                                    description.trim()
                                        .ifBlank { null }
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}