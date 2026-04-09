package com.example.servora.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

data class User(
    val name: String,
    val email: String
)

@Singleton
class AuthRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_PASSWORD_HASH = stringPreferencesKey("password_hash")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val currentUser: Flow<User?> = dataStore.data.map { prefs ->
        val name = prefs[KEY_USER_NAME]
        val email = prefs[KEY_USER_EMAIL]
        if (name != null && email != null) User(name, email) else null
    }

    suspend fun signUp(name: String, email: String, password: String): Result<Unit> {
        if (name.isBlank()) return Result.failure(Exception("Name cannot be empty"))
        if (email.isBlank()) return Result.failure(Exception("Email cannot be empty"))
        if (!email.contains("@") || !email.contains(".")) {
            return Result.failure(Exception("Invalid email format"))
        }
        if (password.length < 6) return Result.failure(Exception("Password must be at least 6 characters"))

        val existingEmail = dataStore.data.first()[KEY_USER_EMAIL]
        if (existingEmail != null && existingEmail == email) {
            return Result.failure(Exception("An account with this email already exists"))
        }

        dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_PASSWORD_HASH] = hashPassword(password)
            prefs[KEY_IS_LOGGED_IN] = true
        }
        return Result.success(Unit)
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(Exception("Email cannot be empty"))
        if (password.isBlank()) return Result.failure(Exception("Password cannot be empty"))

        val prefs = dataStore.data.first()
        val storedEmail = prefs[KEY_USER_EMAIL]
        val storedHash = prefs[KEY_PASSWORD_HASH]

        if (storedEmail == null || storedHash == null) {
            return Result.failure(Exception("No account found. Please sign up first"))
        }

        if (email != storedEmail) {
            return Result.failure(Exception("No account found with this email"))
        }

        if (hashPassword(password) != storedHash) {
            return Result.failure(Exception("Incorrect password"))
        }

        dataStore.edit { it[KEY_IS_LOGGED_IN] = true }
        return Result.success(Unit)
    }

    suspend fun logout() {
        dataStore.edit { it[KEY_IS_LOGGED_IN] = false }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
