package com.example.pocketguard.data.api

import android.content.Context
import com.example.pocketguard.constants.ApiConstants
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

        val token = tokenManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        val requestWithToken = addTokenToRequest(originalRequest, token)
        var response = chain.proceed(requestWithToken)

        if (response.code == 401) {
            synchronized(this) {
                val newToken = tokenManager.getAccessToken()
                if (newToken != null && newToken != token) {
                    val newRequest = addTokenToRequest(originalRequest, newToken)
                    response.close()
                    return chain.proceed(newRequest)
                }

                tokenManager.clearAuthData()
                response.close()
            }
        }

        return response
    }

    private fun addTokenToRequest(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}

