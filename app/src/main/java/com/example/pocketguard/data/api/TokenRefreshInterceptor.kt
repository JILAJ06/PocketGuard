package com.example.pocketguard.data.api

import android.content.Context
import com.example.pocketguard.data.storage.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenRefreshInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        val token = tokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        val requestWithToken = addTokenToRequest(originalRequest, token)
        var response = chain.proceed(requestWithToken)

        if (response.code == 401 && !isPublicAuthEndpoint(path)) {
            synchronized(this) {
                val latestToken = tokenManager.getAccessToken()
                if (!latestToken.isNullOrEmpty() && latestToken != token) {
                    val newRequest = addTokenToRequest(originalRequest, latestToken)
                    response.close()
                    response = chain.proceed(newRequest)
                }
            }
        }

        return response
    }

    private fun isPublicAuthEndpoint(path: String): Boolean {
        return path.contains("/auth/login") ||
            path.contains("/auth/register") ||
            path.contains("/auth/google") ||
            path.contains("/auth/forgot-password") ||
            path.contains("/auth/reset-password") ||
            path.contains("/auth/refresh")
    }

    private fun addTokenToRequest(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}
