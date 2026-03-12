package com.example.pocketguard.data.models
import kotlinx.serialization.Serializable
/**
 * NOTA: LoginRequest y RegisterRequest están en AuthRequest.kt
 * AuthResponse está en AuthResponse.kt  
 * Este archivo solo contiene modelos auxiliares deprecated
 */
@Serializable
data class AuthData(
    val accessToken: String,
    val user: UserData
)
/**
 * Datos del usuario - deprecated, usar User.kt
 */
@Serializable
data class UserData(
    val id: String,
    val email: String,
    val fullName: String,
    val role: String? = null,
    val theme: String? = null,
    val language: String? = null
)
/**
 * Response de refresh token - deprecated
 */
@Serializable
data class RefreshResponse(
    val token: String
)
/**
 * Response de logout - deprecated
 */
@Serializable
data class LogoutResponse(
    val message: String
)
/**
 * Response de me (usuario actual) - deprecated
 */
@Serializable
data class MeResponse(
    val user: UserData
)
