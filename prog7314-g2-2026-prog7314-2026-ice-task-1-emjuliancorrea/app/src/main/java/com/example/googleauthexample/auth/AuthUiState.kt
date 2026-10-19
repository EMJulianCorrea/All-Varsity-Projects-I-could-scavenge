package com.example.googleauthexample.auth

import com.google.firebase.auth.FirebaseUser

data class AuthUiState(
    val user: FirebaseUser? = null,
    val isCheckingAuth: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)