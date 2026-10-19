package com.example.biometricandroid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.biometricandroid.ui.theme.BiometricAndroidTheme

@Composable
fun AuthenticationScreen(
    modifier: Modifier = Modifier,
    isBiometricAvailable: Boolean,
    isAuthenticating: Boolean,
    statusMessage: String?,
    onAuthenticateClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Biometric Authentication",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(16.dp)

        Text(
            text = "Verify your identity to access the protected content.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(32.dp)

        // Status / error message area. Text-based rather than colour-only,
        // and phrased in plain language rather than a raw error code.
        if (statusMessage != null) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (isBiometricAvailable) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Spacer(24.dp)
        }

        if (isAuthenticating) {
            CircularProgressIndicator()
            Spacer(12.dp)
            Text(
                text = "Authenticating…",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Button(
                onClick = onAuthenticateClick,
                enabled = isBiometricAvailable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Authenticate")
            }

            if (!isBiometricAvailable) {
                Spacer(12.dp)
                Text(
                    text = "Biometric authentication isn't available right now, so sign-in is disabled.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Spacer(height: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(height))
}

@Preview(showBackground = true)
@Composable
private fun AuthenticationScreenAvailablePreview() {
    BiometricAndroidTheme {
        AuthenticationScreen(
            isBiometricAvailable = true,
            isAuthenticating = false,
            statusMessage = "Biometric authentication is available.",
            onAuthenticateClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthenticationScreenUnavailablePreview() {
    BiometricAndroidTheme {
        AuthenticationScreen(
            isBiometricAvailable = false,
            isAuthenticating = false,
            statusMessage = "No biometric credentials are enrolled on this device.",
            onAuthenticateClick = {}
        )
    }
}