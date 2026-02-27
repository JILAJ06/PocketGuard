package com.example.pocketguard.data.validators

import com.example.pocketguard.data.models.ValidationError
import java.util.regex.Pattern

object ValidationManager {

    fun validateEmail(email: String): ValidationError {
        return when {
            email.isEmpty() -> ValidationError.EMPTY_EMAIL
            !isValidEmail(email) -> ValidationError.INVALID_EMAIL
            else -> ValidationError.NONE
        }
    }

    fun validatePassword(password: String): ValidationError {
        return when {
            password.isEmpty() -> ValidationError.EMPTY_PASSWORD
            password.length < 8 -> ValidationError.PASSWORD_TOO_SHORT
            !password.any { it.isUpperCase() } -> ValidationError.PASSWORD_NO_UPPERCASE
            !password.any { it.isDigit() } -> ValidationError.PASSWORD_NO_NUMBER
            else -> ValidationError.NONE
        }
    }

    fun validateFullName(fullName: String): ValidationError {
        return when {
            fullName.isEmpty() -> ValidationError.EMPTY_FULL_NAME
            fullName.length < 3 -> ValidationError.FULL_NAME_TOO_SHORT
            fullName.length > 100 -> ValidationError.FULL_NAME_TOO_LONG
            else -> ValidationError.NONE
        }
    }

    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationError {
        return if (password != confirmPassword) {
            ValidationError.PASSWORDS_NOT_MATCH
        } else {
            ValidationError.NONE
        }
    }

    fun validateLoginForm(email: String, password: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        val emailError = validateEmail(email)
        if (emailError != ValidationError.NONE) {
            errors.add(emailError)
        }

        val passwordError = validatePassword(password)
        if (passwordError != ValidationError.NONE) {
            errors.add(passwordError)
        }

        return errors
    }

    fun validateRegisterForm(
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        val emailError = validateEmail(email)
        if (emailError != ValidationError.NONE) {
            errors.add(emailError)
        }

        val passwordError = validatePassword(password)
        if (passwordError != ValidationError.NONE) {
            errors.add(passwordError)
        }

        val matchError = validatePasswordMatch(password, confirmPassword)
        if (matchError != ValidationError.NONE) {
            errors.add(matchError)
        }

        val fullNameError = validateFullName(fullName)
        if (fullNameError != ValidationError.NONE) {
            errors.add(fullNameError)
        }

        return errors
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        )
        return emailPattern.matcher(email).matches()
    }

    fun getErrorMessage(error: ValidationError): String {
        return when (error) {
            ValidationError.EMPTY_EMAIL -> "El email es requerido"
            ValidationError.INVALID_EMAIL -> "El email no es válido"
            ValidationError.EMPTY_PASSWORD -> "La contraseña es requerida"
            ValidationError.PASSWORD_TOO_SHORT -> "La contraseña debe tener al menos 8 caracteres"
            ValidationError.PASSWORD_NO_UPPERCASE -> "La contraseña debe contener al menos una mayúscula"
            ValidationError.PASSWORD_NO_NUMBER -> "La contraseña debe contener al menos un número"
            ValidationError.EMPTY_FULL_NAME -> "El nombre es requerido"
            ValidationError.FULL_NAME_TOO_SHORT -> "El nombre debe tener al menos 3 caracteres"
            ValidationError.FULL_NAME_TOO_LONG -> "El nombre no puede exceder 100 caracteres"
            ValidationError.PASSWORDS_NOT_MATCH -> "Las contraseñas no coinciden"
            ValidationError.NONE -> ""
        }
    }
}

