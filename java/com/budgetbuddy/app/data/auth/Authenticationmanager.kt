package com.budgetbuddy.app.data.auth

import com.budgetbuddy.app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthenticationManager @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(AuthState.Authenticated(user))
            } else {
                trySend(AuthState.Unauthenticated)
            }
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String? = null
    ): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null && displayName != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            user?.let { AuthResult.Success(it) }
                ?: AuthResult.Error("Failed to create user")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign up failed")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            user?.let { AuthResult.Success(it) }
                ?: AuthResult.Error("Failed to sign in")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(currentUser!!)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send reset email")
        }
    }

    suspend fun updateProfile(displayName: String? = null): AuthResult {
        return try {
            val user = currentUser ?: return AuthResult.Error("No user signed in")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .apply { displayName?.let { setDisplayName(it) } }
                .build()

            user.updateProfile(profileUpdates).await()
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to update profile")
        }
    }

    suspend fun updatePassword(newPassword: String): AuthResult {
        return try {
            val user = currentUser ?: return AuthResult.Error("No user signed in")
            user.updatePassword(newPassword).await()
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to update password")
        }
    }

    suspend fun deleteAccount(): AuthResult {
        return try {
            val user = currentUser ?: return AuthResult.Error("No user signed in")
            user.delete().await()
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to delete account")
        }
    }

    fun isUserSignedIn(): Boolean = currentUser != null

    fun getUserEmail(): String? = currentUser?.email

    fun getUserDisplayName(): String? = currentUser?.displayName

    fun getUserId(): String? = currentUser?.uid

    fun toUser(firebaseUser: FirebaseUser): User {
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName,
            createdAt = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis(),
            lastLoginAt = firebaseUser.metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
        )
    }
}