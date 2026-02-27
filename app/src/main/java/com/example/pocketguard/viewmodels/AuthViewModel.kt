package com.example.pocketguard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.models.User
import com.example.pocketguard.data.repository.AuthRepository
import com.example.pocketguard.data.store.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    init {
        // Verificar si ya hay sesión activa
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            if (isLoggedIn) {
                _authUiState.value = _authUiState.value.copy(isLoggedIn = true)
                // Obtener datos del usuario
                val result = authRepository.getCurrentUser()
                result.onSuccess { user ->
                    _authUiState.value = _authUiState.value.copy(user = user)
                }.onFailure { error ->
                    _authUiState.value = _authUiState.value.copy(errorMessage = error.message)
                }
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.register(email, password, fullName)
            result.onSuccess { authResponse ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    user = authResponse.user
                )
            }.onFailure { error ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al registrar"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.login(email, password)
            result.onSuccess { authResponse ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    user = authResponse.user
                )
            }.onFailure { error ->
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun loginWithGoogle(token: String, user: User) {
        viewModelScope.launch {
            authRepository.saveGoogleToken(token, user)
            _authUiState.value = _authUiState.value.copy(
                isLoggedIn = true,
                user = user
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            val result = authRepository.logout()
            result.onSuccess {
                _authUiState.value = AuthUiState(isLoggedIn = false)
            }.onFailure { error ->
                _authUiState.value = _authUiState.value.copy(
                    errorMessage = error.message ?: "Error al cerrar sesión"
                )
            }
        }
    }

    fun clearError() {
        _authUiState.value = _authUiState.value.copy(errorMessage = null)
    }
}

