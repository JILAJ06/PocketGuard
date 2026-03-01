package com.example.pocketguard.presentation.viewmodel

import com.example.pocketguard.data.models.ValidationError

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: ValidationError = ValidationError.NONE,
    val passwordError: ValidationError = ValidationError.NONE,
    val isLoading: Boolean = false,
    val isValid: Boolean = false
)

data class RegisterFormState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullName: String = "",
    val acceptedTerms: Boolean = false,
    val emailError: ValidationError = ValidationError.NONE,
    val passwordError: ValidationError = ValidationError.NONE,
    val confirmPasswordError: ValidationError = ValidationError.NONE,
    val fullNameError: ValidationError = ValidationError.NONE,
    val isLoading: Boolean = false,
    val isValid: Boolean = false
)

data class GoogleAuthState(
    val isLoading: Boolean = false,
    val authUrl: String? = null,
    val error: String? = null
)

