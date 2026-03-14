package com.example.pocketguard.data.models

// Respuesta genérica de la API
data class ApiResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T? = null,
    val errors: List<ApiError>? = null
)

// Respuesta específica para Login/Register
data class AuthResponse(
    val accessToken: String,
    val user: User,
    // El backend ahora también devuelve refreshToken en las respuestas de autenticación
    val refreshToken: String? = null
)

// Respuesta para Refresh Token
data class RefreshTokenResponse(
    val accessToken: String,
    // El endpoint de refresh puede rotar el refreshToken y devolver uno nuevo
    val refreshToken: String? = null
)

// Respuesta para obtener perfil del usuario
data class UserProfileResponse(
    val user: User
)

// Modelo de error de validación
data class ApiError(
    val field: String,
    val message: String
)

