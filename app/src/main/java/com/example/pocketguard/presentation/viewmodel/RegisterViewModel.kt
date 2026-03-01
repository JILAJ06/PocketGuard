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
            emailError = error,
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
            confirmPasswordError = confirmError,
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
            confirmPasswordError = error,
            isValid = validateForm()
        )
    }

    fun onFullNameChanged(fullName: String) {
        val error = ValidationManager.validateFullName(fullName)
        _formState.value = _formState.value.copy(
            fullName = fullName,
            fullNameError = error,
            isValid = validateForm()
        )
    }

    fun onAcceptedTermsChanged(accepted: Boolean) {
        Log.d("RegisterViewModel", "onAcceptedTermsChanged() - accepted=$accepted")
        _formState.value = _formState.value.copy(acceptedTerms = accepted)
        val isFormValid = validateForm()
        Log.d("RegisterViewModel", "onAcceptedTermsChanged() - isFormValid=$isFormValid, currentState=${_formState.value}")
        _formState.value = _formState.value.copy(isValid = isFormValid)
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

