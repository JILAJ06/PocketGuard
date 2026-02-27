package com.example.pocketguard.presentation.viewmodel

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
            return
        }

        _formState.value = _formState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = authRepository.login(currentState.email, currentState.password)
            result.let { authResult ->
                when {
                    authResult is com.example.pocketguard.data.models.AuthResult.Success -> {
                        _isSuccess.value = true
                        _errorMessage.value = ""
                    }
                    authResult is com.example.pocketguard.data.models.AuthResult.Error -> {
                        _errorMessage.value = authResult.message
                        _isSuccess.value = false
                    }
                    else -> {
                        _isSuccess.value = false
                    }
                }
            }
            _formState.value = _formState.value.copy(isLoading = false)
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

