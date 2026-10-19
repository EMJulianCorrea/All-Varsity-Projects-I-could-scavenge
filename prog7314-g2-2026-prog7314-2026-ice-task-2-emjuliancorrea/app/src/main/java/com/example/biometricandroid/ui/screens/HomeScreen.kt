package com.example.biometricandroid.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.biometricandroid.auth.AuthUiState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: AuthUiState,
    onAuthenticateClick: () -> Unit,
    onLockClick: () -> Unit
) {
    if (uiState.isAuthenticated) {
        ProtectedScreen(
            modifier = modifier,
            onLockClick = onLockClick
        )
    } else {
        AuthenticationScreen(
            modifier = modifier,
            isBiometricAvailable = uiState.isBiometricAvailable,
            isAuthenticating = uiState.isAuthenticating,
            statusMessage = uiState.statusMessage,
            onAuthenticateClick = onAuthenticateClick
        )
    }
}