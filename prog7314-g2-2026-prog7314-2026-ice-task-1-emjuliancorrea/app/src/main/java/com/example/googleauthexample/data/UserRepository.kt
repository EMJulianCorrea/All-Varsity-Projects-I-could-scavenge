package com.example.googleauthexample.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

private const val TAG = "UserRepository"
private const val COLLECTION_USERS = "users"

class UserRepository {

    private val firestore by lazy { Firebase.firestore }
    private val realtimeDb by lazy { Firebase.database }

    suspend fun saveUserProfile(user: FirebaseUser) {
        val profile = UserProfile(
            uid = user.uid,
            userName = user.userName,
            email = user.email,
            photoUrl = user.photoUrl?.toString(),
            provider = user.providerData
                .firstOrNull { it.providerId == "google.com" }
                ?.providerId ?: "google.com",
            emailVerified = user.isEmailVerified
        )

        try {
            firestore.collection(COLLECTION_USERS)
                .document(profile.uid)
                .set(profile.toMap())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Could not write user profile to Firestore", e)
        }

        try {
            realtimeDb.getReference(COLLECTION_USERS)
                .child(profile.uid)
                .setValue(profile.toMap())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Could not write user profile to database", e)
        }
    }
}