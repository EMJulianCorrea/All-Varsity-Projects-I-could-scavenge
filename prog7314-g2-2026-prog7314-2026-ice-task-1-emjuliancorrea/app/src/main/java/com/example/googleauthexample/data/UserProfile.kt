package com.example.googleauthexample.data

data class UserProfile(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val provider: String = "google.com",
    val emailVerified: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "userName" to userName,
        "email" to email,
        "photoUrl" to photoUrl,
        "provider" to provider,
        "emailVerified" to emailVerified,
        "updatedAt" to updatedAt
    )
}