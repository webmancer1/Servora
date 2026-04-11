package com.example.servora.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class User(
    val name: String,
    val email: String
)

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    val currentUser: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(User(
                    name = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: ""
                ))
            } else {
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    val isLoggedIn: Flow<Boolean> = currentUser.map { it != null }

    suspend fun signUp(name: String, email: String, password: String): Result<Unit> {
        if (name.isBlank()) return Result.failure(Exception("Name cannot be empty"))
        if (email.isBlank()) return Result.failure(Exception("Email cannot be empty"))
        if (password.length < 6) return Result.failure(Exception("Password must be at least 6 characters"))

        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
            
            if (user != null && name.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
                
                // Force an auth state change to emit the updated user profile
                firebaseAuth.updateCurrentUser(user).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "An error occurred during sign up"))
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Email cannot be empty"))
        if (password.isBlank()) return Result.failure(Exception("Password cannot be empty"))

        return try {
            firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Invalid login credentials"))
        }
    }

    suspend fun logout() {
        firebaseAuth.signOut()
    }
}
