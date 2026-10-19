package com.drivemate.sa.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drivemate.sa.ui.screens.HomeScreen
import com.drivemate.sa.ui.screens.MaintenanceScreen
import com.drivemate.sa.ui.screens.SettingScreen
import com.drivemate.sa.viewmodels.MaintenanceViewModel
import com.drivemate.sa.viewmodels.SettingsViewModel
import com.google.firebase.auth.FirebaseUser

@Composable
fun AppNavigation(
    currentUser: FirebaseUser?,
    isLoading: Boolean,
    errorMessage: String?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    it: Any
) {
    val backStack = remember { mutableStateListOf(Screen.HOME) }
    val currentScreen = backStack.last()

    fun navigateTo(screen: Screen) {
        backStack.add(screen)
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    when (currentScreen) {
        Screen.HOME -> HomeScreen(
            currentUser = currentUser,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onSignIn = onSignIn,
            onSignOut = onSignOut,
            onOpenMaintenance = { navigateTo(Screen.MAINTENANCE) }
        )

        Screen.MAINTENANCE -> {
            val viewModel: MaintenanceViewModel = viewModel()
            val records by viewModel.records.collectAsState()

            MaintenanceScreen(
                records = records,
                onAddRecord = { viewModel.addRecord(it) },
                onMarkCompleted = { viewModel.markCompleted(it) },
                onDeleteRecord = { viewModel.deleteRecord(it) }
            )
        }
        Screen.SETTINGS -> {
            val viewModel: SettingsViewModel = viewModel()
            SettingScreen(
                currentUser = currentUser,
                onBackClick = { navigateBack() },
                onViewFullProfile = { viewModel.viewProfile(it) },
                onMyBookingsClick = { viewModel.viewBookings(it) },
                onMyMessagesClick = { viewModel.viewMessages(it) },
                onHelpCenterClick = { viewModel.helpCenter(it) },
                onAppFeedbackClick = { viewModel.feedbackForm(it) },
                onLanguageClick = { viewModel.languageChanger(it) },
                onNotificationClick = { viewModel.viewNotifications(it) },
            )
        }
    }
}