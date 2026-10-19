package com.example.palisadeinventoryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palisadeinventoryapp.viewmodels.AuthViewModel
import com.example.palisadeinventoryapp.viewmodels.InventoryViewModel
import com.example.palisadeinventoryapp.viewmodels.ProductionViewModel
import com.example.palisadeinventoryapp.viewmodels.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashBoardScreen(
    authViewModel: AuthViewModel,
    inventoryViewModel: InventoryViewModel,
    salesViewModel: SalesViewModel,
    productionViewModel: ProductionViewModel,
    onOpenInventory: () -> Unit,
    onOpenSales: () -> Unit,
    onOpenSalesHistory: () -> Unit,
    onOpenRecordProduction: () -> Unit,
    onOpenProductionHistory: () -> Unit,
    onOpenArchivedProducts: () -> Unit,
    onLogout: () -> Unit
) {
    val products by inventoryViewModel.products.collectAsState()
    val sales by salesViewModel.salesHistory.collectAsState()
    // Recompose when history changes so the "today" totals stay current.
    val todayProduction by productionViewModel.todayProduction.collectAsState()

    var showLogoutConfirm by remember { mutableStateOf(false) }

    val lowStockCount = products.count { it.quantity <= InventoryViewModel.LOW_STOCK_THRESHOLD }
    val totalRevenue = sales.sumOf { it.salePrice * it.quantitySold }
    val unitsProducedToday = todayProduction.sumOf { it.quantityProduced }
    val productionEntriesToday = todayProduction.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    TextButton(onClick = { showLogoutConfirm = true }) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Products",
                    value = products.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Low Stock",
                    value = lowStockCount.toString(),
                    isWarning = lowStockCount > 0,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Revenue",
                    value = "R${"%.2f".format(totalRevenue)}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            TodaysProductionCard(
                unitsProducedToday = unitsProducedToday,
                entriesToday = productionEntriesToday,
                onRecordProduction = onOpenRecordProduction,
                onViewHistory = onOpenProductionHistory
            )

            Spacer(Modifier.height(28.dp))

            Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))

            DashboardActionButton("View Inventory", onOpenInventory)
            Spacer(Modifier.height(10.dp))
            DashboardActionButton("Record a Sale", onOpenSales)
            Spacer(Modifier.height(10.dp))
            DashboardActionButton("Sales History", onOpenSalesHistory)
            Spacer(Modifier.height(10.dp))
            DashboardActionButton("Record Production", onOpenRecordProduction)
            Spacer(Modifier.height(10.dp))
            DashboardActionButton("Production History", onOpenProductionHistory)
            Spacer(Modifier.height(10.dp))
            DashboardActionButton("Archived Products", onOpenArchivedProducts)
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to log back in to access the dashboard.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    authViewModel.logout()
                    onLogout()
                }) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TodaysProductionCard(
    unitsProducedToday: Int,
    entriesToday: Int,
    onRecordProduction: () -> Unit,
    onViewHistory: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Today's Production", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "$unitsProducedToday",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "units produced",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        "$entriesToday",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "entries today",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRecordProduction, modifier = Modifier.weight(1f)) {
                    Text("Record Production")
                }
                OutlinedButton(onClick = onViewHistory, modifier = Modifier.weight(1f)) {
                    Text("View History")
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DashboardActionButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(label)
    }
}
