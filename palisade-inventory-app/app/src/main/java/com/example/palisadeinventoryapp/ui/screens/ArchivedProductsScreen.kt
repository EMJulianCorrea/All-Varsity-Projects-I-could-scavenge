package com.example.palisadeinventoryapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palisadeinventoryapp.data.entity.Product
import com.example.palisadeinventoryapp.viewmodels.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedProductsScreen(
    viewModel: InventoryViewModel,
    onBack: () -> Unit
) {
    val archivedProducts by viewModel.archivedProducts.collectAsState()
    val message by viewModel.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var productPendingRestore by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived Products") },
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            if (archivedProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No archived products. Discontinued products will show up here.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(archivedProducts, key = { it.id }) { product ->
                        ArchivedProductCard(
                            product = product,
                            categoryName = viewModel.categoryName(product.categoryId),
                            onRestoreClick = { productPendingRestore = product }
                        )
                    }
                }
            }
        }
    }

    productPendingRestore?.let { product ->
        AlertDialog(
            onDismissRequest = { productPendingRestore = null },
            title = { Text("Restore product?") },
            text = {
                Text("\"${product.name}\" will show up in Inventory again, with its original price and category.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restoreProduct(product)
                    productPendingRestore = null
                }) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { productPendingRestore = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ArchivedProductCard(
    product: Product,
    categoryName: String,
    onRestoreClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    categoryName,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text("R${"%.2f".format(product.price)}", fontSize = 14.sp)
            }
            Button(onClick = onRestoreClick) {
                Text("Restore")
            }
        }
    }
}