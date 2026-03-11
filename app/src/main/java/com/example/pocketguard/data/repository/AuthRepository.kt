package com.example.pocketguard.data.repository

import android.util.Log
import com.example.pocketguard.data.api.AuthService
import com.example.pocketguard.data.api.GoogleMobileAuthRequest
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
            Log.d("AuthRepository", "login() - Iniciando login para: $email")
            val loginRequest = LoginRequest(email, password)
            val response = authService.login(loginRequest)

            Log.d("AuthRepository", "login() - Response success=${response.success}, statusCode=${response.statusCode}")

            if (response.success && response.data != null) {
                Log.d("AuthRepository", "login() - Token recibido, guardando...")
                tokenManager.saveAccessToken(response.data.accessToken)
                tokenManager.saveUserId(response.data.user.id)
                tokenManager.saveIsGoogleAuth(false) // Marcar como login normal
                Log.d("AuthRepository", "login() - Usuario guardado: ${response.data.user.id}")
                AuthResult.Success(response.data)
            } else {
                Log.e("AuthRepository", "login() - Error: ${response.message}")
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "login() - Exception: ${e.message}", e)
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    suspend fun register(email: String, password: String, fullName: String): AuthResult<AuthResponse> {
        return try {
            Log.d("AuthRepository", "register() - Iniciando registro para: $email")
            val registerRequest = RegisterRequest(email, password, fullName)
            val response = authService.register(registerRequest)

            Log.d("AuthRepository", "register() - Response success=${response.success}, statusCode=${response.statusCode}")

            if (response.success && response.data != null) {
                Log.d("AuthRepository", "register() - Token recibido, guardando...")
                tokenManager.saveAccessToken(response.data.accessToken)
                tokenManager.saveUserId(response.data.user.id)
                tokenManager.saveIsGoogleAuth(false) // Marcar como registro normal
                Log.d("AuthRepository", "register() - Usuario registrado: ${response.data.user.id}")
                AuthResult.Success(response.data)
            } else {
                Log.e("AuthRepository", "register() - Error: ${response.message}")
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "register() - Exception: ${e.message}", e)
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

    /**
     * Login con Google desde móvil (Android/iOS)
     * Usa el endpoint /auth/google/mobile
     */
    suspend fun googleMobileAuth(idToken: String): AuthResult<AuthResponse> {
        return try {
            Log.d("AuthRepository", "googleMobileAuth() - Iniciando login con Google")
            Log.d("AuthRepository", "googleMobileAuth() - idToken length: ${idToken.length}")
            Log.d("AuthRepository", "googleMobileAuth() - idToken preview: ${idToken.take(50)}...")

            val request = GoogleMobileAuthRequest(idToken)
            Log.d("AuthRepository", "googleMobileAuth() - Enviando request a /auth/google/mobile")

            val response = authService.googleMobileAuth(request)

            Log.d("AuthRepository", "googleMobileAuth() - Response success=${response.success}, statusCode=${response.statusCode}")

            if (response.success && response.data != null) {
                Log.d("AuthRepository", "googleMobileAuth() - Token recibido, guardando...")
                tokenManager.saveAccessToken(response.data.accessToken)
                tokenManager.saveUserId(response.data.user.id)
                tokenManager.saveIsGoogleAuth(true) // Marcar como usuario de Google
                Log.d("AuthRepository", "googleMobileAuth() - Usuario logueado: ${response.data.user.email}")
                AuthResult.Success(response.data)
            } else {
                Log.e("AuthRepository", "googleMobileAuth() - Error: ${response.message}")
                Log.e("AuthRepository", "googleMobileAuth() - Status Code: ${response.statusCode}")
                AuthResult.Error(response.message ?: "Error en autenticación con Google", response.statusCode)
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("AuthRepository", "googleMobileAuth() - HttpException: ${e.code()} - ${e.message()}")
            try {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AuthRepository", "googleMobileAuth() - Error body: $errorBody")
            } catch (ex: Exception) {
                Log.e("AuthRepository", "googleMobileAuth() - No se pudo leer error body")
            }
            AuthResult.Error("Error del servidor (${e.code()}): ${e.message()}", e.code())
        } catch (e: Exception) {
            Log.e("AuthRepository", "googleMobileAuth() - Exception: ${e.javaClass.simpleName} - ${e.message}", e)
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

    suspend fun deleteAccount(password: String): AuthResult<Unit> {
        return try {
            Log.d("AuthRepository", "deleteAccount() - Iniciando eliminación de cuenta...")
            val token = tokenManager.getAccessToken()

            if (token == null) {
                return AuthResult.Error("No hay token disponible", null)
            }

            val request = com.example.pocketguard.data.api.DeleteAccountRequest(password)
            val response = authService.deleteAccount("Bearer $token", request)
            Log.d("AuthRepository", "deleteAccount() - Response: success=${response.success}")

            if (response.success) {
                Log.d("AuthRepository", "deleteAccount() - Cuenta eliminada, limpiando datos...")
                tokenManager.clearAuthData()
                AuthResult.Success(Unit)
            } else {
                Log.e("AuthRepository", "deleteAccount() - Error: ${response.message}")
                AuthResult.Error(response.message, response.statusCode)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "deleteAccount() - Exception: ${e.message}", e)
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    /**
     * Solicitar enlace de recuperación de contraseña
     */
    suspend fun forgotPassword(email: String): AuthResult<Unit> {
        return try {
            Log.d("AuthRepository", "forgotPassword() - Solicitando recuperación para: $email")
            val request = com.example.pocketguard.data.api.ForgotPasswordRequest(email)
            val response = authService.forgotPassword(request)

            Log.d("AuthRepository", "forgotPassword() - Response success=${response.success}")

            if (response.success) {
                Log.d("AuthRepository", "forgotPassword() - Solicitud exitosa")
                AuthResult.Success(Unit)
            } else {
                Log.e("AuthRepository", "forgotPassword() - Error: ${response.message}")
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "forgotPassword() - Exception: ${e.message}", e)
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    /**
     * Restablecer contraseña con token de recuperación
     */
    suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit> {
        return try {
            Log.d("AuthRepository", "resetPassword() - Restableciendo contraseña con token")
            val request = com.example.pocketguard.data.api.ResetPasswordRequest(token, newPassword)
            val response = authService.resetPassword(request)

            Log.d("AuthRepository", "resetPassword() - Response success=${response.success}")

            if (response.success) {
                Log.d("AuthRepository", "resetPassword() - Contraseña actualizada exitosamente")
                AuthResult.Success(Unit)
            } else {
                Log.e("AuthRepository", "resetPassword() - Error: ${response.message}")
                AuthResult.Error(response.message ?: "Error desconocido", response.statusCode)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "resetPassword() - Exception: ${e.message}", e)
            AuthResult.Error(e.message ?: "Error en la conexión", null)
        }
    }

    fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }

    fun isGoogleUser(): Boolean {
        return tokenManager.isGoogleAuth()
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
