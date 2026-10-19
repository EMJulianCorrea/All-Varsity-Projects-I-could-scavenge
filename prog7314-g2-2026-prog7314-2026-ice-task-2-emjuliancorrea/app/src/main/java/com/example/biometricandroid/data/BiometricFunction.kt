package com.example.biometricandroid.data

import androidx.biometric.BiometricPrompt
import com.example.biometricandroid.MainActivity

class BiometricFunction(activity: MainActivity) {

    fun createBiometricPrompt(
        onSuccess: () -> Unit,
        onFailed: () -> Unit,
        onError: Any
    ): BiometricPrompt? {
        return null;
    }

    fun checkAvailability() {
        TODO("Not yet implemented")
    }

    fun buildPromptInfo(): BiometricPrompt.PromptInfo {}
}