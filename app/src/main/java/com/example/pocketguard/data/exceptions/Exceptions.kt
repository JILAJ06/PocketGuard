package com.example.pocketguard.data.exceptions

class ApiException(
    val statusCode: Int?,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class ValidationException(
    val errors: List<String>,
    message: String = "Errores de validación"
) : Exception(message)

class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class AuthenticationException(
    message: String = "No autenticado",
    cause: Throwable? = null
) : Exception(message, cause)

