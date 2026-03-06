package com.example.pocketguard.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.pocketguard.data.models.ValidationError
import com.example.pocketguard.data.repository.AuthRepository
import com.example.pocketguard.data.validators.ValidationManager

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    fun onEmailChanged(email: String) {
        val error = ValidationManager.validateEmail(email)
        _formState.value = _formState.value.copy(
            email = email,
            emailError = error,
            isValid = validateForm(email, _formState.value.password)
        )
    }

    fun onPasswordChanged(password: String) {
        val error = ValidationManager.validatePassword(password)
        _formState.value = _formState.value.copy(
            password = password,
            passwordError = error,
            isValid = validateForm(_formState.value.email, password)
        )
    }

    fun login() {
        val currentState = _formState.value
        val emailError = ValidationManager.validateEmail(currentState.email)
        val passwordError = ValidationManager.validatePassword(currentState.password)

        _formState.value = _formState.value.copy(
            emailError = emailError,
            passwordError = passwordError
        )

        if (emailError != ValidationError.NONE || passwordError != ValidationError.NONE) {
            Log.w("LoginViewModel", "login() - Validación fallida")
            return
        }

        Log.d("LoginViewModel", "login() - Iniciando proceso de login...")
        _formState.value = _formState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.login(currentState.email, currentState.password)
            result.let { authResult ->
                when {
                    authResult is com.example.pocketguard.data.models.AuthResult.Success -> {
                        Log.d("LoginViewModel", "login() - Login exitoso!")
                        _isSuccess.value = true
                        _errorMessage.value = ""
                    }
                    authResult is com.example.pocketguard.data.models.AuthResult.Error -> {
                        Log.e("LoginViewModel", "login() - Error: ${authResult.message}")
                        _errorMessage.value = authResult.message
                        _isSuccess.value = false
                    }
                    else -> {
                        Log.e("LoginViewModel", "login() - Estado desconocido")
                        _isSuccess.value = false
                    }
                }
            }
            _formState.value = _formState.value.copy(isLoading = false)
        }
    }

    /**
     * Login con Google usando el ID Token
     */
    fun loginWithGoogle(idToken: String) {
        Log.d("LoginViewModel", "loginWithGoogle() - Iniciando login con Google")
        _formState.value = _formState.value.copy(isLoading = true)
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val result = authRepository.googleMobileAuth(idToken)
                result.let { authResult ->
                    when {
                        authResult is com.example.pocketguard.data.models.AuthResult.Success -> {
                            Log.d("LoginViewModel", "loginWithGoogle() - Login con Google exitoso!")
                            _isSuccess.value = true
                            _errorMessage.value = ""
                        }
                        authResult is com.example.pocketguard.data.models.AuthResult.Error -> {
                            Log.e("LoginViewModel", "loginWithGoogle() - Error: ${authResult.message}")
                            val errorMsg = when {
                                authResult.message.contains("Unable to resolve host") ||
                                authResult.message.contains("Failed to connect") ->
                                    "No se pudo conectar al servidor. Verifica que tu backend esté corriendo en 192.168.110.230:3001"
                                authResult.message.contains("timeout") ->
                                    "Tiempo de espera agotado. Verifica tu conexión"
                                else -> authResult.message
                            }
                            _errorMessage.value = errorMsg
                            _isSuccess.value = false
                        }
                        else -> {
                            Log.e("LoginViewModel", "loginWithGoogle() - Estado desconocido")
                            _errorMessage.value = "Error desconocido al iniciar sesión con Google"
                            _isSuccess.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "loginWithGoogle() - Exception: ${e.message}", e)
                _errorMessage.value = "Error de conexión: ${e.message}"
                _isSuccess.value = false
            } finally {
                _formState.value = _formState.value.copy(isLoading = false)
            }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        val emailError = ValidationManager.validateEmail(email)
        val passwordError = ValidationManager.validatePassword(password)
        return emailError == ValidationError.NONE && passwordError == ValidationError.NONE
    }

    fun getEmailErrorMessage(): String {
        return ValidationManager.getErrorMessage(_formState.value.emailError)
    }

    fun getPasswordErrorMessage(): String {
        return ValidationManager.getErrorMessage(_formState.value.passwordError)
    }

    fun setError(message: String) {
        _errorMessage.value = message
        _formState.value = _formState.value.copy(isLoading = false)
        _isSuccess.value = false
    }

    fun resetForm() {
        _formState.value = LoginFormState()
        _isSuccess.value = false
        _errorMessage.value = ""
    }
}

class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

