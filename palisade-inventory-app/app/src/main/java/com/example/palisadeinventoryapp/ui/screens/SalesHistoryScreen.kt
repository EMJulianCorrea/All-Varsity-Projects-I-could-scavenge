package com.example.palisadeinventoryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palisadeinventoryapp.data.entity.Sale
import com.example.palisadeinventoryapp.viewmodels.InventoryViewModel
import com.example.palisadeinventoryapp.viewmodels.SalesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    salesViewModel: SalesViewModel,
    inventoryViewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val sales by salesViewModel.salesHistory.collectAsState()
    val products by inventoryViewModel.products.collectAsState()
    val totalRevenue = sales.sumOf { it.salePrice * it.quantitySold }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Total Revenue: R${"%.2f".format(totalRevenue)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(16.dp))

            if (sales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No sales recorded yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(sales, key = { it.id }) { sale ->
                        val productName = products.find { it.id == sale.productId }?.name ?: "Deleted product"
                        SaleCard(sale, productName)
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleCard(sale: Sale, productName: String) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val total = sale.salePrice * sale.quantitySold

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(productName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("R${"%.2f".format(total)}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Qty: ${sale.quantitySold}  •  R${"%.2f".format(sale.salePrice)} each",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                dateFormat.format(Date(sale.date)),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}