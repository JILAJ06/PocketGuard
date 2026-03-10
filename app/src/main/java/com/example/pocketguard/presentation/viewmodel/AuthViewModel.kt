package com.example.pocketguard.presentation.viewmodel

// AuthViewModel - Gestiona la autenticación y recuperación de contraseña
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.pocketguard.data.models.AuthState
import com.example.pocketguard.data.models.AuthResult
import com.example.pocketguard.data.models.User
import com.example.pocketguard.data.models.ValidationError
import com.example.pocketguard.data.repository.AuthRepository
import com.example.pocketguard.data.validators.ValidationManager

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _validationErrors = MutableStateFlow<List<ValidationError>>(emptyList())
    val validationErrors: StateFlow<List<ValidationError>> = _validationErrors.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        if (authRepository.isAuthenticated()) {
            _authState.value = AuthState.Idle
            _accessToken.value = authRepository.getAccessToken()
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        val validationErrors = ValidationManager.validateLoginForm(email, password)

        if (validationErrors.isNotEmpty()) {
            _validationErrors.value = validationErrors
            _authState.value = AuthState.Error("Errores de validación")
            return
        }

        _validationErrors.value = emptyList()
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.data.user
                    _accessToken.value = result.data.accessToken
                    _authState.value = AuthState.Success(result.data.user, result.data.accessToken)
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String, fullName: String) {
        val validationErrors = ValidationManager.validateRegisterForm(
            email, password, confirmPassword, fullName
        )

        if (validationErrors.isNotEmpty()) {
            _validationErrors.value = validationErrors
            _authState.value = AuthState.Error("Errores de validación")
            return
        }

        _validationErrors.value = emptyList()
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            when (val result = authRepository.register(email, password, fullName)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.data.user
                    _accessToken.value = result.data.accessToken
                    _authState.value = AuthState.Success(result.data.user, result.data.accessToken)
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun loginWithGoogle(idToken: String, accessToken: String? = null) {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            when (val result = authRepository.loginWithGoogle(idToken, accessToken)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.data.user
                    _accessToken.value = result.data.accessToken
                    _authState.value = AuthState.Success(result.data.user, result.data.accessToken)
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun getProfile() {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            when (val result = authRepository.getProfile()) {
                is AuthResult.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Success(result.data, _accessToken.value ?: "")
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun logout() {
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            when (val result = authRepository.logout()) {
                is AuthResult.Success -> {
                    _currentUser.value = null
                    _accessToken.value = null
                    _authState.value = AuthState.Unauthenticated
                    _validationErrors.value = emptyList()
                }
                is AuthResult.Error -> {
                    _currentUser.value = null
                    _accessToken.value = null
                    _authState.value = AuthState.Unauthenticated
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun refreshAccessToken() {
        viewModelScope.launch {
            when (val result = authRepository.refreshToken()) {
                is AuthResult.Success -> {
                    _accessToken.value = result.data
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error("No se pudo refrescar el token")
                    logout()
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun validateEmail(email: String): ValidationError {
        return ValidationManager.validateEmail(email)
    }

    fun validatePassword(password: String): ValidationError {
        return ValidationManager.validatePassword(password)
    }

    fun validateFullName(fullName: String): ValidationError {
        return ValidationManager.validateFullName(fullName)
    }

    fun getErrorMessage(error: ValidationError): String {
        return ValidationManager.getErrorMessage(error)
    }

    fun clearErrors() {
        _validationErrors.value = emptyList()
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun isAuthenticated(): Boolean {
        return authRepository.isAuthenticated()
    }

    /**
     * Solicitar enlace de recuperación de contraseña
     */
    fun forgotPassword(email: String) {
        val emailError = ValidationManager.validateEmail(email)

        if (emailError != ValidationError.NONE) {
            _validationErrors.value = listOf(emailError)
            _authState.value = AuthState.Error("Email inválido")
            return
        }

        _validationErrors.value = emptyList()
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            when (val result = authRepository.forgotPassword(email)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Idle
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    /**
     * Restablecer contraseña con token de recuperación
     */
    fun resetPassword(token: String, newPassword: String, confirmPassword: String) {
        val passwordError = ValidationManager.validatePassword(newPassword)
        val errors = mutableListOf<ValidationError>()

        if (passwordError != ValidationError.NONE) {
            errors.add(passwordError)
        }

        if (newPassword != confirmPassword) {
            errors.add(ValidationError.PASSWORDS_NOT_MATCH)
        }

        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            _authState.value = AuthState.Error("Errores de validación")
            return
        }

        _validationErrors.value = emptyList()
        _authState.value = AuthState.Loading

        viewModelScope.launch {
            when (val result = authRepository.resetPassword(token, newPassword)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Idle
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

