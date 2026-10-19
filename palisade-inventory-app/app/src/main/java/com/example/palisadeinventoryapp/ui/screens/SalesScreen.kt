package com.example.palisadeinventoryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palisadeinventoryapp.data.entity.Product
import com.example.palisadeinventoryapp.viewmodels.InventoryViewModel
import com.example.palisadeinventoryapp.viewmodels.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    inventoryViewModel: InventoryViewModel,
    salesViewModel: SalesViewModel,
    onBack: () -> Unit
) {
    val products by inventoryViewModel.products.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Sale") },
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select product") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (products.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No products available") },
                            onClick = {}
                        )
                    }
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = { Text("${product.name} (${product.quantity} in stock)") },
                            onClick = {
                                selectedProduct = product
                                expanded = false
                                message = ""
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it; message = "" },
                label = { Text("Quantity sold") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            selectedProduct?.let { product ->
                val quantity = quantityText.toIntOrNull() ?: 0
                if (quantity > 0) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Total: R${"%.2f".format(product.price * quantity)}",
                        fontSize = 15.sp
                    )
                }
            }

            if (message.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    message,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val product = selectedProduct
                    val quantity = quantityText.toIntOrNull()
                    when {
                        product == null -> {
                            message = "Please select a product"
                            isError = true
                        }
                        quantity == null || quantity <= 0 -> {
                            message = "Enter a valid quantity"
                            isError = true
                        }
                        quantity > product.quantity -> {
                            message = "Only ${product.quantity} left in stock"
                            isError = true
                        }
                        else -> {
                            // Valid so far — confirm before it actually touches stock.
                            showConfirmDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Record Sale")
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showConfirmDialog) {
        val product = selectedProduct
        val quantity = quantityText.toIntOrNull()
        if (product != null && quantity != null) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Confirm sale") },
                text = {
                    Text(
                        "Sell $quantity x \"${product.name}\" for a total of R${"%.2f".format(product.price * quantity)}? This will reduce stock by $quantity."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        salesViewModel.recordSale(product.id, quantity, product.price) { success ->
                            if (success) {
                                message = "Sale recorded"
                                isError = false
                                selectedProduct = null
                                quantityText = ""
                            } else {
                                message = "Not enough stock for that sale"
                                isError = true
                            }
                        }
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}