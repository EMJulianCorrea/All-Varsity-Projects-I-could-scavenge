package com.example.biometricandroid.auth

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val isAuthenticating: Boolean = false,
    val statusMessage: String? = null
)
