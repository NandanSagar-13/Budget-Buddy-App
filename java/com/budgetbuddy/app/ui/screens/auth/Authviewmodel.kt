package com.budgetbuddy.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetbuddy.app.data.auth.AuthResult
import com.budgetbuddy.app.data.auth.AuthenticationManager
import com.budgetbuddy.app.data.model.User
import com.budgetbuddy.app.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthenticationManager,
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            authManager.authState.collect { state ->
                when (state) {
                    is com.budgetbuddy.app.data.auth.AuthState.Authenticated -> {
                        _uiState.update { it.copy(isAuthenticated = true, isLoading = false) }
                        // Initialize default categories for new users
                        repository.initializeDefaultCategories()
                    }
                    is com.budgetbuddy.app.data.auth.AuthState.Unauthenticated -> {
                        _uiState.update { it.copy(isAuthenticated = false, isLoading = false) }
                    }
                    is com.budgetbuddy.app.data.auth.AuthState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is com.budgetbuddy.app.data.auth.AuthState.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = state.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authManager.signInWithEmail(email, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            error = null
                        )
                    }

                    // Save/update user profile
                    val user = authManager.toUser(result.user)
                    repository.saveUserProfile(user.copy(lastLoginAt = System.currentTimeMillis()))
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authManager.signUpWithEmail(email, password, displayName)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            error = null
                        )
                    }

                    // Create user profile
                    val user = authManager.toUser(result.user)
                    repository.saveUserProfile(user)

                    // Initialize default categories
                    repository.initializeDefaultCategories()
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authManager.resetPassword(email)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Password reset email sent. Please check your inbox.",
                            error = null
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
            _uiState.update {
                AuthUiState(isAuthenticated = false)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}