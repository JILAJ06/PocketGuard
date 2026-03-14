package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service para autenticación
 * Base URL: http://localhost:3001
 */
interface AuthApiService {

    /**
     * POST /api/v1/auth/register
     * Registra un nuevo usuario
     */
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    /**
     * POST /api/v1/auth/login
     * Inicia sesión con email y contraseña
     */
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    /**
     * POST /api/v1/auth/refresh
     * Refresca el token JWT
     */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Header("Authorization") token: String): Response<RefreshResponse>

    /**
     * POST /api/v1/auth/logout
     * Cierra la sesión del usuario
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<LogoutResponse>

    /**
     * GET /api/v1/auth/me
     * Obtiene los datos del usuario autenticado
     */
    @GET("api/v1/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<MeResponse>
}

