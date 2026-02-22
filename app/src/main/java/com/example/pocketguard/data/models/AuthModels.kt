package com.example.pocketguard.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request para registro de usuario
 * El backend espera: fullName, email, password
 */
@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

/**
 * Request para login
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Response de autenticación (register/login)
 * El backend devuelve: {success, message, data: {accessToken, user}}
 */
@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData
)

@Serializable
data class AuthData(
    val accessToken: String,
    val user: UserData
)

/**
 * Datos del usuario
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
 * Response de refresh token
 */
@Serializable
data class RefreshResponse(
    val token: String
)

/**
 * Response de logout
 */
@Serializable
data class LogoutResponse(
    val message: String
)

/**
 * Response de me (usuario actual)
 */
@Serializable
data class MeResponse(
    val user: UserData
)

