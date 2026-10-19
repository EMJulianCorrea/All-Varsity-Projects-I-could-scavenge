package com.example.biometricandroid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.biometricandroid.auth.AuthViewModel
import com.example.biometricandroid.data.BiometricFunction
import com.example.biometricandroid.ui.screens.HomeScreen
import com.example.biometricandroid.ui.theme.BiometricAndroidTheme

class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var biometricFunction: BiometricFunction
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBiometricAuthentication()

        enableEdgeToEdge()
        setContent {
            BiometricAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

                    HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        uiState = uiState,
                        onAuthenticateClick = { showBiometricPrompt() },
                        onLockClick = { lockApplication() }
                    )
                }
            }
        }
    }

    private fun setupBiometricAuthentication() {
        biometricFunction = BiometricFunction(this)

        val availability = biometricFunction.checkAvailability()
        authViewModel.setBiometricAvailability(availability.isAvailable, availability.message)

        biometricPrompt = biometricFunction.createBiometricPrompt(
            onSuccess = { authViewModel.onAuthenticationSucceeded() },
            onFailed = { authViewModel.onAuthenticationFailed() },
            onError = { message -> authViewModel.onAuthenticationError(message) }
        )!!

        promptInfo = biometricFunction.buildPromptInfo()
    }

    private fun showBiometricPrompt() {
        val uiState = authViewModel.uiState.value

        if (!uiState.isBiometricAvailable) return
        if (!::biometricPrompt.isInitialized) return
        if (uiState.isAuthenticating) return

        authViewModel.onAuthenticationStarted()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun lockApplication() {
        authViewModel.lockApplication()
    }
}