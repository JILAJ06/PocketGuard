package com.example.pocketguard.utils

object ErrorMessageMapper {

    fun getDisplayMessage(errorMessage: String): String {
        return when {
            errorMessage.contains("401", ignoreCase = true) -> "Credenciales inválidas"
            errorMessage.contains("409", ignoreCase = true) -> "El email ya está registrado"
            errorMessage.contains("400", ignoreCase = true) -> "Datos inválidos"
            errorMessage.contains("500", ignoreCase = true) -> "Error del servidor"
            errorMessage.contains("Connection", ignoreCase = true) -> "Error de conexión"
            errorMessage.contains("timeout", ignoreCase = true) -> "Conexión agotada"
            errorMessage.contains("Network", ignoreCase = true) -> "Problema de red"
            else -> errorMessage
        }
    }

    fun isNetworkError(errorMessage: String): Boolean {
        return errorMessage.contains("Connection") ||
                errorMessage.contains("Network") ||
                errorMessage.contains("timeout")
    }

    fun isAuthenticationError(errorMessage: String): Boolean {
        return errorMessage.contains("401") ||
                errorMessage.contains("Unauthorized")
    }

    fun isValidationError(errorMessage: String): Boolean {
        return errorMessage.contains("400") ||
                errorMessage.contains("validation") ||
                errorMessage.contains("invalid")
    }
}

