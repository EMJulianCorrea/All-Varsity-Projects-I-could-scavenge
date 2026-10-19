package com.example.palisadeinventoryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.palisadeinventoryapp.di.DatabaseProvider
import com.example.palisadeinventoryapp.navigation.AppNavigation
import com.example.palisadeinventoryapp.ui.theme.PalisadeInventoryAppTheme
import com.example.palisadeinventoryapp.viewmodels.AuthViewModel
import com.example.palisadeinventoryapp.viewmodels.InventoryViewModel
import com.example.palisadeinventoryapp.viewmodels.ProductionViewModel
import com.example.palisadeinventoryapp.viewmodels.SalesViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise the database and repositories once here.
        DatabaseProvider.init(applicationContext)

        enableEdgeToEdge()

        setContent {
            PalisadeInventoryAppTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(
                        DatabaseProvider.userRepository
                    )
                )

                val inventoryViewModel: InventoryViewModel = viewModel(
                    factory = InventoryViewModel.Factory(
                        DatabaseProvider.productRepository,
                        DatabaseProvider.categoryRepository
                    )
                )

                val salesViewModel: SalesViewModel = viewModel(
                    factory = SalesViewModel.Factory(
                        DatabaseProvider.saleRepository
                    )
                )

                val productionViewModel: ProductionViewModel = viewModel(
                    factory = ProductionViewModel.Factory(
                        DatabaseProvider.productionRepository
                    )
                )

                AppNavigation(
                    authViewModel = authViewModel,
                    inventoryViewModel = inventoryViewModel,
                    salesViewModel = salesViewModel,
                    productionViewModel = productionViewModel
                )
            }
        }
    }
}