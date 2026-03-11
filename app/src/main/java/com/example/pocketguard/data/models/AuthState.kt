package com.example.pocketguard.data.models

// Estados posibles de autenticación
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User, val accessToken: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object Unauthenticated : AuthState()
}

// Resultado de operaciones de autenticación
sealed class AuthResult<T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error<T>(val message: String, val statusCode: Int? = null) : AuthResult<T>()
    class Loading<T> : AuthResult<T>()
}

