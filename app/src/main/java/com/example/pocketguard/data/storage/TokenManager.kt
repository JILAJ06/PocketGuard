package com.example.pocketguard.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.pocketguard.constants.ApiConstants

/**
 * Gestor centralizado para almacenar y recuperar datos de autenticación
 */
class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        ApiConstants.SHARED_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Guardar el token de acceso
     */
    fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString(ApiConstants.ACCESS_TOKEN_KEY, token).apply()
    }

    /**
     * Obtener el token de acceso
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(ApiConstants.ACCESS_TOKEN_KEY, null)
    }

    /**
     * Guardar el refresh token
     */
    fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString(ApiConstants.REFRESH_TOKEN_KEY, token).apply()
    }

    /**
     * Obtener el refresh token
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(ApiConstants.REFRESH_TOKEN_KEY, null)
    }

    /**
     * Guardar el ID del usuario
     */
    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(ApiConstants.USER_ID_KEY, userId).apply()
    }

    /**
     * Obtener el ID del usuario
     */
    fun getUserId(): String? {
        return sharedPreferences.getString(ApiConstants.USER_ID_KEY, null)
    }

    /**
     * Verificar si el usuario está autenticado (tiene token)
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null && getAccessToken()!!.isNotEmpty()
    }

    /**
     * Limpiar todos los datos de autenticación (logout)
     */
    fun clearAuthData() {
        sharedPreferences.edit().apply {
            remove(ApiConstants.ACCESS_TOKEN_KEY)
            remove(ApiConstants.REFRESH_TOKEN_KEY)
            remove(ApiConstants.USER_ID_KEY)
            apply()
        }
    }

    /**
     * Guardar todos los datos de autenticación en una sola operación
     */
    fun saveAuthData(accessToken: String, refreshToken: String? = null, userId: String? = null) {
        sharedPreferences.edit().apply {
            putString(ApiConstants.ACCESS_TOKEN_KEY, accessToken)
            if (refreshToken != null) {
                putString(ApiConstants.REFRESH_TOKEN_KEY, refreshToken)
            }
            if (userId != null) {
                putString(ApiConstants.USER_ID_KEY, userId)
            }
            apply()
        }
    }
}

