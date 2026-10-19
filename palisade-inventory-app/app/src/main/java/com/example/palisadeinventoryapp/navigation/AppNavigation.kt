package com.example.palisadeinventoryapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.palisadeinventoryapp.data.entity.Product
import com.example.palisadeinventoryapp.ui.screens.AddEditProductScreen
import com.example.palisadeinventoryapp.ui.screens.ArchivedProductsScreen
import com.example.palisadeinventoryapp.ui.screens.DashBoardScreen
import com.example.palisadeinventoryapp.ui.screens.InventoryScreen
import com.example.palisadeinventoryapp.ui.screens.LoginScreen
import com.example.palisadeinventoryapp.ui.screens.ProductionHistoryScreen
import com.example.palisadeinventoryapp.ui.screens.RecordProductionScreen
import com.example.palisadeinventoryapp.ui.screens.RegisterScreen
import com.example.palisadeinventoryapp.ui.screens.SalesHistoryScreen
import com.example.palisadeinventoryapp.ui.screens.SalesScreen
import com.example.palisadeinventoryapp.viewmodels.AuthViewModel
import com.example.palisadeinventoryapp.viewmodels.InventoryViewModel
import com.example.palisadeinventoryapp.viewmodels.ProductionViewModel
import com.example.palisadeinventoryapp.viewmodels.SalesViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    inventoryViewModel: InventoryViewModel,
    salesViewModel: SalesViewModel,
    productionViewModel: ProductionViewModel
) {
    val backStack = remember { mutableStateListOf(Screen.LOGIN) }
    val currentScreen = backStack.last()

    var productBeingEdited by remember {
        mutableStateOf<Product?>(null)
    }

    fun navigateTo(screen: Screen) {
        backStack.add(screen)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    when (currentScreen) {
        Screen.LOGIN -> LoginScreen(
            viewModel = authViewModel,
            onRegisterClick = {
                navigateTo(Screen.REGISTER)
            },
            onLoginSuccess = {
                backStack.clear()
                backStack.add(Screen.DASHBOARD)
            }
        )

        Screen.REGISTER -> RegisterScreen(
            viewModel = authViewModel,
            onLoginClick = {
                navigateBack()
            }
        )

        Screen.DASHBOARD -> DashBoardScreen(
            authViewModel = authViewModel,
            inventoryViewModel = inventoryViewModel,
            salesViewModel = salesViewModel,
            productionViewModel = productionViewModel,
            onOpenInventory = {
                navigateTo(Screen.INVENTORY)
            },
            onOpenSales = {
                navigateTo(Screen.SALES)
            },
            onOpenSalesHistory = {
                navigateTo(Screen.SALES_HISTORY)
            },
            // already implemented
            onOpenRecordProduction = {
                navigateTo(Screen.RECORD_PRODUCTION)
            },
            // already implemented
            onOpenProductionHistory = {
                navigateTo(Screen.PRODUCTION_HISTORY)
            },
            onOpenArchivedProducts = {
                navigateTo(Screen.ARCHIVED_PRODUCTS)
            },
            onLogout = {
                backStack.clear()
                backStack.add(Screen.LOGIN)
            }
        )

        Screen.INVENTORY -> InventoryScreen(
            viewModel = inventoryViewModel,
            onBack = {
                navigateBack()
            },
            onAddProduct = {
                productBeingEdited = null
                navigateTo(Screen.ADD_EDIT_PRODUCT)
            },
            onEditProduct = { product ->
                productBeingEdited = product
                navigateTo(Screen.ADD_EDIT_PRODUCT)
            },
            // already implemented
            onOpenArchived = {
                navigateTo(Screen.ARCHIVED_PRODUCTS)
            }
        )

        Screen.ADD_EDIT_PRODUCT -> AddEditProductScreen(
            viewModel = inventoryViewModel,
            product = productBeingEdited,
            onDone = {
                navigateBack()
            }
        )

        // already implemented
        Screen.ARCHIVED_PRODUCTS -> ArchivedProductsScreen(
            viewModel = inventoryViewModel,
            onBack = {
                //function already present
                navigateBack()
            }
        )

        Screen.SALES -> SalesScreen(
            inventoryViewModel = inventoryViewModel,
            salesViewModel = salesViewModel,
            onBack = {
                navigateBack()
            }
        )

        Screen.SALES_HISTORY -> SalesHistoryScreen(
            salesViewModel = salesViewModel,
            inventoryViewModel = inventoryViewModel,
            onBack = {
                navigateBack()
            }
        )

        // already implemented
        Screen.RECORD_PRODUCTION -> RecordProductionScreen(
            inventoryViewModel = inventoryViewModel,
            productionViewModel = productionViewModel,
            onBack = {
                navigateBack()
            }
        )

        // already implemented
        Screen.PRODUCTION_HISTORY -> ProductionHistoryScreen(
            productionViewModel = productionViewModel,
            inventoryViewModel = inventoryViewModel,
            onBack = {
                navigateBack()
            }
        )
    }
}
