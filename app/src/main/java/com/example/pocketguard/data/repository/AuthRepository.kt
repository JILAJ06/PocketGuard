package com.example.pocketguard.data.repository

import com.example.pocketguard.data.api.AuthService
import com.example.pocketguard.data.api.GoogleTokenRequest
import com.example.pocketguard.data.api.RefreshTokenRequest
import com.example.pocketguard.data.models.*
import com.example.pocketguard.data.storage.TokenManager

class AuthRepository(
    private val authService: AuthService,
    private val tokenManager: TokenManager
) {

    suspend fun login(email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val loginRequest = LoginRequest(email, password)
            val response = authService.login(loginRequest)

            if (response.success && response.data != null) {
                tokenManager.saveAccessToken(response.data.accessToken)
                tokenManager.saveUserId(response.data.user.id)
                AuthResult.Success(response.data)
            } else {
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    suspend fun register(email: String, password: String, fullName: String): AuthResult<AuthResponse> {
        return try {
            val registerRequest = RegisterRequest(email, password, fullName)
            val response = authService.register(registerRequest)

            if (response.success && response.data != null) {
                tokenManager.saveAccessToken(response.data.accessToken)
                tokenManager.saveUserId(response.data.user.id)
                AuthResult.Success(response.data)
            } else {
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    suspend fun loginWithGoogle(idToken: String, accessToken: String? = null): AuthResult<AuthResponse> {
        return try {
            val googleTokenRequest = GoogleTokenRequest(idToken, accessToken)
            val response = authService.googleCallback(googleTokenRequest)

            if (response.success && response.data != null) {
                tokenManager.saveAccessToken(response.data.accessToken)
                tokenManager.saveUserId(response.data.user.id)
                AuthResult.Success(response.data)
            } else {
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    suspend fun getGoogleAuthUrl(): AuthResult<Map<String, String>> {
        return try {
            val response = authService.getGoogleAuthUrl()

            if (response.success && response.data != null) {
                AuthResult.Success(response.data)
            } else {
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    suspend fun refreshToken(): AuthResult<String> {
        return try {
            val currentRefreshToken = tokenManager.getRefreshToken()

            if (currentRefreshToken == null) {
                return AuthResult.Error("No hay refresh token disponible", null)
            }

            val refreshTokenRequest = RefreshTokenRequest(currentRefreshToken)
            val response = authService.refreshToken(refreshTokenRequest)

            if (response.success && response.data != null) {
                tokenManager.saveAccessToken(response.data.accessToken)
                AuthResult.Success(response.data.accessToken)
            } else {
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    suspend fun getProfile(): AuthResult<User> {
        return try {
            val token = tokenManager.getAccessToken()

            if (token == null) {
                return AuthResult.Error("No hay token disponible", null)
            }

            val response = authService.getProfile("Bearer $token")

            if (response.success && response.data != null) {
                AuthResult.Success(response.data.user)
            } else {
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    suspend fun logout(): AuthResult<Unit> {
        return try {
            val token = tokenManager.getAccessToken()

            if (token != null) {
                authService.logout("Bearer $token")
            }

            tokenManager.clearAuthData()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            tokenManager.clearAuthData()
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }

    fun clearAuthData() {
        tokenManager.clearAuthData()
    }

    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    fun getUserId(): String? {
        return tokenManager.getUserId()
    }
}

