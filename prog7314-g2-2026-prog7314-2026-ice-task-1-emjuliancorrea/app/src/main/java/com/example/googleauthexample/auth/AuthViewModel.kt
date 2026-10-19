package com.example.googleauthexample.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleauthexample.R
import com.example.googleauthexample.data.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException

private const val TAG = "AuthViewModel"

class AuthViewModel : ViewModel() {

    private val firebaseAuth = Firebase.auth
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(user = firebaseAuth.currentUser, isCheckingAuth = false) }
    }

    fun signInWithGoogle(context: Context) {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val webClientId = context.getString(R.string.default_web_client_id)
                val credentialManager = CredentialManager.create(context)
                val googleIdTokenCredential = try {
                    requestGoogleCredential(
                        credentialManager = credentialManager,
                        context = context,
                        webClientId = webClientId,
                        filterByAuthorizedAccounts = true
                    )
                } catch (e: NoCredentialException) {
                    requestGoogleCredential(
                        credentialManager = credentialManager,
                        context = context,
                        webClientId = webClientId,
                        filterByAuthorizedAccounts = false
                    )
                }

                signInToFirebase(googleIdTokenCredential.idToken)
            } catch (e: GetCredentialCancellationException) {
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: NoCredentialException) {
                Log.w(TAG, "No Google credential available", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No Google account found. Add a Google account in Settings."
                    )
                }
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager request failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Google sign-in incomplete. Please try again."
                    )
                }
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Could not parse Google ID token", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Google credential reading error")
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, "Firebase rejected the Google credential", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Google sign-in not verified. Please try again.")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error ", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No internet connection. Please check your connection and try again.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in error", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Sign-in failed. Please try again.")
                }
            }
        }
    }

    private suspend fun requestGoogleCredential(
        credentialManager: CredentialManager,
        context: Context,
        webClientId: String,
        filterByAuthorizedAccounts: Boolean
    ): GoogleIdTokenCredential {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(filterByAuthorizedAccounts)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context = context, request = request)
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data)
        }

        throw IllegalStateException("Unexpected credential type")
    }

    private suspend fun signInToFirebase(googleIdToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
        val user = authResult.user

        if (user == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "FAILED: no user returned") }
            return
        }

        userRepository.saveUserProfile(user)

        _uiState.update { it.copy(user = user, isLoading = false, errorMessage = null) }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            firebaseAuth.signOut()

            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Could not clear credential state", e)
            }

            _uiState.update { AuthUiState(user = null, isCheckingAuth = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}