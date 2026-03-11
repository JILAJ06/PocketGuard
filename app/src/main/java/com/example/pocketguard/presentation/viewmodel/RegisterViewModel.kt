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

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(RegisterFormState())
    val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    fun onEmailChanged(email: String) {
        val error = ValidationManager.validateEmail(email)
        _formState.value = _formState.value.copy(
            email = email,
            emailError = error
        )
        _formState.value = _formState.value.copy(
            isValid = validateForm()
        )
    }

    fun onPasswordChanged(password: String) {
        val error = ValidationManager.validatePassword(password)
        val confirmError = if (_formState.value.confirmPassword.isNotEmpty()) {
            ValidationManager.validatePasswordMatch(password, _formState.value.confirmPassword)
        } else {
            ValidationError.NONE
        }

        _formState.value = _formState.value.copy(
            password = password,
            passwordError = error,
            confirmPasswordError = confirmError
        )
        _formState.value = _formState.value.copy(
            isValid = validateForm()
        )
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        val error = ValidationManager.validatePasswordMatch(
            _formState.value.password,
            confirmPassword
        )

        _formState.value = _formState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = error
        )
        _formState.value = _formState.value.copy(
            isValid = validateForm()
        )
    }

    fun onFullNameChanged(fullName: String) {
        val error = ValidationManager.validateFullName(fullName)
        _formState.value = _formState.value.copy(
            fullName = fullName,
            fullNameError = error
        )
        _formState.value = _formState.value.copy(
            isValid = validateForm()
        )
    }

    fun onAcceptedTermsChanged(accepted: Boolean) {
        Log.d("RegisterViewModel", "onAcceptedTermsChanged() - accepted=$accepted")
        _formState.value = _formState.value.copy(
            acceptedTerms = accepted
        )
        _formState.value = _formState.value.copy(
            isValid = validateForm()
        )
        Log.d("RegisterViewModel", "onAcceptedTermsChanged() - finalState=${_formState.value}")
    }

    fun register() {
        val currentState = _formState.value

        val emailError = ValidationManager.validateEmail(currentState.email)
        val passwordError = ValidationManager.validatePassword(currentState.password)
        val confirmPasswordError = ValidationManager.validatePasswordMatch(
            currentState.password,
            currentState.confirmPassword
        )
        val fullNameError = ValidationManager.validateFullName(currentState.fullName)

        _formState.value = _formState.value.copy(
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            fullNameError = fullNameError
        )

        if (emailError != ValidationError.NONE ||
            passwordError != ValidationError.NONE ||
            confirmPasswordError != ValidationError.NONE ||
            fullNameError != ValidationError.NONE
        ) {
            Log.w("RegisterViewModel", "register() - Validación de campos fallida")
            return
        }

        if (!currentState.acceptedTerms) {
            Log.w("RegisterViewModel", "register() - Términos no aceptados")
            _errorMessage.value = "Debes aceptar los términos y condiciones"
            return
        }

        Log.d("RegisterViewModel", "register() - Iniciando proceso de registro...")
        _formState.value = _formState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.register(
                currentState.email,
                currentState.password,
                currentState.fullName
            )

            result.let { authResult ->
                when {
                    authResult is com.example.pocketguard.data.models.AuthResult.Success -> {
                        Log.d("RegisterViewModel", "register() - Registro exitoso!")
                        _isSuccess.value = true
                        _errorMessage.value = ""
                    }
                    authResult is com.example.pocketguard.data.models.AuthResult.Error -> {
                        Log.e("RegisterViewModel", "register() - Error: ${authResult.message}")
                        _errorMessage.value = authResult.message
                        _isSuccess.value = false
                    }
                    else -> {
                        Log.e("RegisterViewModel", "register() - Estado desconocido")
                        _isSuccess.value = false
                    }
                }
            }

            _formState.value = _formState.value.copy(isLoading = false)
        }
    }

    private fun validateForm(): Boolean {
        val currentState = _formState.value
        val emailError = ValidationManager.validateEmail(currentState.email)
        val passwordError = ValidationManager.validatePassword(currentState.password)
        val confirmPasswordError = ValidationManager.validatePasswordMatch(
            currentState.password,
            currentState.confirmPassword
        )
        val fullNameError = ValidationManager.validateFullName(currentState.fullName)

        return emailError == ValidationError.NONE &&
                passwordError == ValidationError.NONE &&
                confirmPasswordError == ValidationError.NONE &&
                fullNameError == ValidationError.NONE &&
                currentState.acceptedTerms
    }

    fun getEmailErrorMessage(): String {
        return ValidationManager.getErrorMessage(_formState.value.emailError)
    }

    fun getPasswordErrorMessage(): String {
        return ValidationManager.getErrorMessage(_formState.value.passwordError)
    }

    fun getConfirmPasswordErrorMessage(): String {
        return ValidationManager.getErrorMessage(_formState.value.confirmPasswordError)
    }

    fun getFullNameErrorMessage(): String {
        return ValidationManager.getErrorMessage(_formState.value.fullNameError)
    }

    fun resetForm() {
        _formState.value = RegisterFormState()
        _isSuccess.value = false
        _errorMessage.value = ""
    }

    /**
     * Registro/Login con Google usando el ID Token
     * (El backend maneja automáticamente si es nuevo usuario o ya existe)
     */
    fun loginWithGoogle(idToken: String) {
        Log.d("RegisterViewModel", "loginWithGoogle() - Iniciando registro/login con Google")
        Log.d("RegisterViewModel", "loginWithGoogle() - idToken length: ${idToken.length}")

        // Validar que el idToken no esté vacío
        if (idToken.isBlank()) {
            Log.e("RegisterViewModel", "loginWithGoogle() - idToken está vacío")
            _errorMessage.value = "Error: Token de Google inválido"
            return
        }

        _formState.value = _formState.value.copy(isLoading = true)
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                Log.d("RegisterViewModel", "loginWithGoogle() - Enviando request al backend...")
                val result = authRepository.googleMobileAuth(idToken)
                result.let { authResult ->
                    when {
                        authResult is com.example.pocketguard.data.models.AuthResult.Success -> {
                            Log.d("RegisterViewModel", "loginWithGoogle() - Registro/Login con Google exitoso!")
                            _isSuccess.value = true
                            _errorMessage.value = ""
                        }
                        authResult is com.example.pocketguard.data.models.AuthResult.Error -> {
                            Log.e("RegisterViewModel", "loginWithGoogle() - Error: ${authResult.message}")
                            Log.e("RegisterViewModel", "loginWithGoogle() - Status Code: ${authResult.statusCode}")

                            val errorMsg = when {
                                authResult.statusCode == 500 -> {
                                    "Error del servidor (500). Verifica:\n" +
                                    "1. Que el backend esté configurado con Google OAuth\n" +
                                    "2. Que el GOOGLE_CLIENT_ID del backend coincida con el del frontend\n" +
                                    "3. Los logs del backend para más detalles"
                                }
                                authResult.statusCode == 401 -> "Token de Google inválido o expirado"
                                authResult.message.contains("Unable to resolve host") ||
                                authResult.message.contains("Failed to connect") ->
                                    "No se pudo conectar al servidor. Verifica que tu backend esté corriendo"
                                authResult.message.contains("timeout") ->
                                    "Tiempo de espera agotado. Verifica tu conexión"
                                else -> authResult.message
                            }
                            _errorMessage.value = errorMsg
                            _isSuccess.value = false
                        }
                        else -> {
                            Log.e("RegisterViewModel", "loginWithGoogle() - Estado desconocido")
                            _errorMessage.value = "Error desconocido al registrarse con Google"
                            _isSuccess.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "loginWithGoogle() - Exception: ${e.message}", e)
                _errorMessage.value = "Error de conexión: ${e.message}"
                _isSuccess.value = false
            } finally {
                _formState.value = _formState.value.copy(isLoading = false)
            }
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
        _formState.value = _formState.value.copy(isLoading = false)
        _isSuccess.value = false
    }
}

class RegisterViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

