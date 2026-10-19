package com.example.biometricandroid.auth

import androidx.lifecycle.ViewModel
import androidx.room.util.copy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setBiometricAvailability(isAvailable: Boolean, message: String?) {
        _uiState.update {
            it.copy(isBiometricAvailable = isAvailable, statusMessage = message)
        }
    }

    fun onAuthenticationStarted() {
        _uiState.update {
            it.copy(isAuthenticating = true, statusMessage = null)
        }
    }

    fun onAuthenticationSucceeded() {
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                isAuthenticating = false,
                statusMessage = null
            )
        }
    }

    fun onAuthenticationFailed() {
        _uiState.update {
            it.copy(
                isAuthenticating = false,
                statusMessage = "Biometric not recognised. Please try again."
            )
        }
    }

    fun onAuthenticationError(message: String) {
        _uiState.update {
            it.copy(isAuthenticating = false, statusMessage = message)
        }
    }

    fun lockApplication() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                isAuthenticating = false,
                statusMessage = null
            )
        }
    }
}