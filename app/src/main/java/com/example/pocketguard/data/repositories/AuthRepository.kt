package com.example.pocketguard.data.repositories

import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.models.*

/**
 * Repositorio de autenticación
 * Maneja todas las operaciones de auth con la API
 */
class AuthRepository {

    private val authService = RetrofitClient.authService

    /**
     * Registra un nuevo usuario
     * @return Result con AuthResponse o Exception
     */
    suspend fun register(
        fullName: String,
        email: String,
        password: String
    ): Result<AuthResponse> = try {
        val request = RegisterRequest(fullName, email, password)
        val response = authService.register(request)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.errorBody()?.string() ?: "Error desconocido"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Inicia sesión
     * @return Result con AuthResponse o Exception
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponse> = try {
        val request = LoginRequest(email, password)
        val response = authService.login(request)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.errorBody()?.string() ?: "Error desconocido"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Refresca el token JWT
     */
    suspend fun refresh(token: String): Result<RefreshResponse> = try {
        val response = authService.refresh("Bearer $token")

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.errorBody()?.string() ?: "Error desconocido"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Cierra la sesión
     */
    suspend fun logout(token: String): Result<LogoutResponse> = try {
        val response = authService.logout("Bearer $token")

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.errorBody()?.string() ?: "Error desconocido"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Obtiene los datos del usuario autenticado
     */
    suspend fun getMe(token: String): Result<MeResponse> = try {
        val response = authService.getMe("Bearer $token")

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.errorBody()?.string() ?: "Error desconocido"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

