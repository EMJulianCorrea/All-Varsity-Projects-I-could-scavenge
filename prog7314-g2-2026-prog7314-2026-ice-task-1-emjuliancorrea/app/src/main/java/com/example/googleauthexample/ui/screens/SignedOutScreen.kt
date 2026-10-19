package com.example.googleauthexample.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.googleauthexample.ui.theme.GoogleAuthExampleTheme

@Composable
fun SignedOutScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    errorMessage: String?,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(height = 16.dp)

        Text(
            text = "GoogleAuthExample",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(height = 8.dp)

        Text(
            text = "Welcome",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(height = 8.dp)

        Text(
            text = "Sign in with your Google account to continue"
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(height = 32.dp)

        if (isLoading) {
            CircularProgressIndicator()
            Spacer(height = 12.dp)
            Text(
                text = "Signing in…",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Button(
                onClick = onSignInClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F1F1F)
                )
            ) {
                Text(
                    text = "Continue with Google",
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (errorMessage != null) {
            Spacer(height = 24.dp)
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Spacer(height: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(height))
}

@Preview(showBackground = true)
@Composable
private fun SignedOutScreenPreview() {
    GoogleAuthExampleTheme {
        SignedOutScreen(
            isLoading = false,
            errorMessage = null,
            onSignInClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignedOutScreenErrorPreview() {
    GoogleAuthExampleTheme {
        SignedOutScreen(
            isLoading = false,
            errorMessage = "No internet connection. Please check your connection and try again.",
            onSignInClick = {}
        )
    }
}