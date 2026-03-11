package com.example.pocketguard.presentation.session

import android.content.Context
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.models.User
import com.example.pocketguard.data.storage.TokenManager

class SessionManager(private val context: Context, private val tokenManager: TokenManager) {

    fun isSessionActive(): Boolean {
        return tokenManager.isAuthenticated()
    }

    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    fun saveSession(accessToken: String, refreshToken: String? = null, user: User? = null) {
        val userId = user?.id ?: tokenManager.getUserId()
        tokenManager.saveAuthData(accessToken, refreshToken, userId)
    }

    fun clearSession() {
        tokenManager.clearAuthData()
        RetrofitClient.resetInstances()
    }

    fun isTokenExpired(): Boolean {
        val token = getAccessToken() ?: return true
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val payload = parts[1]
            val decoded = String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE))
            val exp = decoded.split("\"exp\":")[1].split(",")[0].toLong()
            val currentTime = System.currentTimeMillis() / 1000

            currentTime > exp
        } catch (e: Exception) {
            true
        }
    }

    fun refreshSessionIfNeeded(): Boolean {
        return !isTokenExpired()
    }
}

