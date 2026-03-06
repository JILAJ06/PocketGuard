package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface AuthService {

    /**
     * Login con email y contraseña
     * POST /auth/login
     */
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): ApiResponse<AuthResponse>

    /**
     * Registro de nuevo usuario
     * POST /auth/register
     */
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): ApiResponse<AuthResponse>

    /**
     * Iniciar sesión con Google - obtiene URL de autenticación
     * GET /auth/google
     */
    @GET("auth/google")
    suspend fun getGoogleAuthUrl(): ApiResponse<Map<String, String>>

    /**
     * Callback de Google después de autenticación
     * POST /auth/google/callback
     */
    @POST("auth/google/callback")
    suspend fun googleCallback(@Body googleTokenRequest: GoogleTokenRequest): ApiResponse<AuthResponse>

    /**
     * Login con Google desde móvil (Android/iOS)
     * POST /auth/google/mobile
     */
    @POST("auth/google/mobile")
    suspend fun googleMobileAuth(@Body request: GoogleMobileAuthRequest): ApiResponse<AuthResponse>

    /**
     * Refrescar el access token usando el refresh token
     * POST /auth/refresh
     */
    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): ApiResponse<RefreshTokenResponse>

    /**
     * Obtener perfil del usuario autenticado
     * GET /auth/me
     */
    @GET("auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): ApiResponse<UserProfileResponse>

    /**
     * Cerrar sesión
     * POST /auth/logout
     */
    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): ApiResponse<Map<String, String>>

    /**
     * Eliminar cuenta de usuario
     * DELETE /users/me
     */
    @DELETE("users/me")
    suspend fun deleteAccount(@Header("Authorization") token: String): ApiResponse<Map<String, String>>
}

// Data classes para requests adicionales
data class GoogleTokenRequest(
    val idToken: String,
    val accessToken: String? = null
)

data class GoogleMobileAuthRequest(
    val idToken: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)
