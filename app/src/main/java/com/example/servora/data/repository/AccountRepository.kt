package com.example.servora.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class UserSettings(
    val role: String = "Administrator",
    val plan: String = "Free",
    val twoFactorAuth: Boolean = false,
    val dataRegion: String = "US-East",
    val notificationChannels: String = "Email, Push",
    val apiKeysCount: Int = 0
)

@Singleton
class AccountRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    fun getUserSettings(): Flow<UserSettings> = callbackFlow {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            trySend(UserSettings())
            close()
            return@callbackFlow
        }

        val docRef = firestore.collection("users").document(userId).collection("settings").document("preferences")
        
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(UserSettings())
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val settings = snapshot.toObject(UserSettings::class.java) ?: UserSettings()
                trySend(settings)
            } else {
                val defaultSettings = UserSettings()
                trySend(defaultSettings)
                // Initialize default document
                docRef.set(defaultSettings)
            }
        }

        awaitClose { listener.remove() }
    }

    suspend fun updateSetting(field: String, value: Any): Result<Unit> {
        val userId = firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        return try {
            firestore.collection("users").document(userId).collection("settings").document("preferences")
                .update(field, value).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to update setting"))
        }
    }

    suspend fun updateDisplayName(newName: String): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("User not logged in"))
        if (newName.isBlank()) return Result.failure(Exception("Name cannot be blank"))
        
        return try {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName.trim())
                .build()
            user.updateProfile(profileUpdates).await()
            
            // Notify Auth system
            firebaseAuth.updateCurrentUser(user).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to update profile"))
        }
    }
}
