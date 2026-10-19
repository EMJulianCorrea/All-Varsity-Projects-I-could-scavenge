package com.example.googleauthexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleauthexample.auth.AuthViewModel
import com.example.googleauthexample.ui.screens.DashboardScreen
import com.example.googleauthexample.ui.screens.SignedOutScreen
import com.example.googleauthexample.ui.theme.GoogleAuthExampleTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoogleAuthExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

                    when {
                        // Brief check on launch for an existing Firebase session,
                        // so a returning signed-in user never flashes the sign-in screen.
                        uiState.isCheckingAuth -> {
                            LoadingScreen(modifier = Modifier.padding(innerPadding))
                        }

                        uiState.user != null -> {
                            DashboardScreen(
                                modifier = Modifier.padding(innerPadding),
                                user = uiState.user!!,
                                onSignOutClick = { authViewModel.signOut(this@MainActivity) }
                            )
                        }

                        else -> {
                            SignedOutScreen(
                                modifier = Modifier.padding(innerPadding),
                                isLoading = uiState.isLoading,
                                errorMessage = uiState.errorMessage,
                                onSignInClick = { authViewModel.signInWithGoogle(this@MainActivity) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}