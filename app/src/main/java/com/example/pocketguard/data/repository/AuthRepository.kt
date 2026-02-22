package com.example.pocketguard.data.repository

import com.example.pocketguard.data.local.TokenManager
import com.example.pocketguard.data.model.*
import com.example.pocketguard.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val errors: List<String>? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class AuthRepository(private val tokenManager: TokenManager) {

    private val apiService = RetrofitClient.authApiService

    suspend fun register(name: String, email: String, password: String): Result<AuthData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(
                    RegisterRequest(name = name, email = email, password = password)
                )

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true && authResponse.data != null) {
                        // Guardar tokens y user info
                        tokenManager.saveTokens(
                            authResponse.data.tokens.accessToken,
                            authResponse.data.tokens.refreshToken
                        )
                        tokenManager.saveUserInfo(
                            authResponse.data.user.id,
                            authResponse.data.user.name,
                            authResponse.data.user.email
                        )
                        Result.Success(authResponse.data)
                    } else {
                        Result.Error(authResponse?.message ?: "Error en el registro")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    Result.Error(
                        errorResponse?.message ?: "Error en el registro",
                        errorResponse?.errors
                    )
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    suspend fun login(email: String, password: String): Result<AuthData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(
                    LoginRequest(email = email, password = password)
                )

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true && authResponse.data != null) {
                        // Guardar tokens y user info
                        tokenManager.saveTokens(
                            authResponse.data.tokens.accessToken,
                            authResponse.data.tokens.refreshToken
                        )
                        tokenManager.saveUserInfo(
                            authResponse.data.user.id,
                            authResponse.data.user.name,
                            authResponse.data.user.email
                        )
                        Result.Success(authResponse.data)
                    } else {
                        Result.Error(authResponse?.message ?: "Error al iniciar sesión")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java)
                    } catch (e: Exception) {
                        null
                    }
                    Result.Error(
                        errorResponse?.message ?: "Credenciales inválidas",
                        errorResponse?.errors
                    )
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.logout()

                // Limpiar tokens localmente sin importar la respuesta del servidor
                tokenManager.clearTokens()

                if (response.isSuccessful) {
                    Result.Success(true)
                } else {
                    // Aunque falle en el servidor, ya limpiamos tokens locales
                    Result.Success(true)
                }
            } catch (e: Exception) {
                // Aunque falle la conexión, limpiamos tokens locales
                tokenManager.clearTokens()
                Result.Success(true)
            }
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentUser()

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse?.success == true && userResponse.data != null) {
                        Result.Success(userResponse.data)
                    } else {
                        Result.Error(userResponse?.message ?: "Error al obtener usuario")
                    }
                } else {
                    Result.Error("Error al obtener información del usuario")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    suspend fun refreshToken(): Result<Tokens> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken.isNullOrEmpty()) {
                    return@withContext Result.Error("No hay refresh token disponible")
                }

                val response = apiService.refreshToken(
                    RefreshTokenRequest(refreshToken)
                )

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse?.success == true && authResponse.data != null) {
                        tokenManager.saveTokens(
                            authResponse.data.tokens.accessToken,
                            authResponse.data.tokens.refreshToken
                        )
                        Result.Success(authResponse.data.tokens)
                    } else {
                        Result.Error("Error al refrescar token")
                    }
                } else {
                    Result.Error("Token expirado, inicia sesión nuevamente")
                }
            } catch (e: Exception) {
                Result.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }
}

