package com.example.pocketguard.data.repository

import com.example.pocketguard.data.models.*
import com.example.pocketguard.data.store.TokenStore
import com.example.pocketguard.network.RetrofitClient

class AuthRepository(private val tokenStore: TokenStore) {

    private val api = RetrofitClient.api

    suspend fun register(email: String, password: String, fullName: String): Result<AuthResponse> {
        return try {
            val request = RegisterRequest(email, password, fullName)
            val response = api.register(request)

            if (response.success && response.data != null) {
                // Guardar token y usuario
                tokenStore.saveAccessToken(response.data.accessToken)
                tokenStore.saveUser(
                    response.data.user.id,
                    response.data.user.email,
                    response.data.user.fullName,
                    response.data.user.authProvider
                )
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = api.login(request)

            if (response.success && response.data != null) {
                // Guardar token y usuario
                tokenStore.saveAccessToken(response.data.accessToken)
                tokenStore.saveUser(
                    response.data.user.id,
                    response.data.user.email,
                    response.data.user.fullName,
                    response.data.user.authProvider
                )
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = api.getCurrentUser()

            if (response.success && response.data != null) {
                Result.success(response.data.user)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(): Result<String> {
        return try {
            val response = api.refreshToken()

            if (response.success && response.data != null) {
                // Guardar nuevo token
                tokenStore.saveAccessToken(response.data.accessToken)
                Result.success(response.data.accessToken)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val response = api.logout()

            if (response.success) {
                // Limpiar token y datos
                tokenStore.clearAll()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            // Aunque falle, limpiar token localmente
            tokenStore.clearAll()
            Result.failure(e)
        }
    }

    suspend fun saveGoogleToken(token: String, user: User) {
        tokenStore.saveAccessToken(token)
        tokenStore.saveUser(user.id, user.email, user.fullName, user.authProvider)
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenStore.getAccessTokenSync() != null
    }
}

