package com.example.pocketguard.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.models.AuthResponse
import com.example.pocketguard.data.repositories.AuthRepository
import com.example.pocketguard.data.repositories.TokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la autenticación (login/registro)
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val tokenRepository = TokenRepository(application.applicationContext)

    // Estado de loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Mensaje de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Respuesta exitosa
    private val _authSuccess = MutableStateFlow<AuthResponse?>(null)
    val authSuccess: StateFlow<AuthResponse?> = _authSuccess

    /**
     * Intenta hacer login
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.login(email, password)

            result.onSuccess { authResponse ->
                // Guardar token (extraer de data.accessToken)
                tokenRepository.saveToken(authResponse.data.accessToken)
                tokenRepository.saveUserId(authResponse.data.user.id)

                _authSuccess.value = authResponse
                _errorMessage.value = null
            }

            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error desconocido"
                _authSuccess.value = null
            }

            _isLoading.value = false
        }
    }

    /**
     * Intenta hacer registro
     */
    fun register(fullName: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.register(fullName, email, password)

            result.onSuccess { authResponse ->
                // Guardar token (extraer de data.accessToken)
                tokenRepository.saveToken(authResponse.data.accessToken)
                tokenRepository.saveUserId(authResponse.data.user.id)

                _authSuccess.value = authResponse
                _errorMessage.value = null
            }

            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error desconocido"
                _authSuccess.value = null
            }

            _isLoading.value = false
        }
    }

    /**
     * Cierra la sesión (logout)
     */
    fun logout() {
        viewModelScope.launch {
            tokenRepository.clearAll()
            _authSuccess.value = null
            _errorMessage.value = null
        }
    }

    /**
     * Limpia los mensajes y estado
     */
    fun clearState() {
        _errorMessage.value = null
        _authSuccess.value = null
    }
}


