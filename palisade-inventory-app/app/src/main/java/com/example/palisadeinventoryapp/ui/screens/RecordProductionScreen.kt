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
import com.example.palisadeinventoryapp.viewmodels.ProductionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordProductionScreen(
    inventoryViewModel: InventoryViewModel,
    productionViewModel: ProductionViewModel,
    onBack: () -> Unit
) {
    val products by inventoryViewModel.products.collectAsState()
    val message by productionViewModel.message.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var quantityText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            productionViewModel.clearMessage()
            selectedProduct = null
            quantityText = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Daily Production") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                            text = { Text(product.name) },
                            onClick = {
                                selectedProduct = product
                                expanded = false
                                validationError = ""
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it; validationError = "" },
                label = { Text("Quantity produced") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (validationError.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(validationError, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val product = selectedProduct
                    val quantity = quantityText.toIntOrNull()
                    when {
                        product == null -> validationError = "Please select a product"
                        quantity == null || quantity <= 0 -> validationError = "Enter a quantity greater than zero"
                        else -> productionViewModel.recordProduction(product.id, product.name, quantity)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Production Entry")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}