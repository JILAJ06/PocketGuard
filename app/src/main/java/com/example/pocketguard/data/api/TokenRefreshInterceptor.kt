package com.example.pocketguard.data.api

import android.content.Context
import android.util.Log
import com.example.pocketguard.data.storage.TokenManager
import com.example.pocketguard.constants.ApiConstants
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class TokenRefreshInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        try {

            val token = tokenManager.getAccessToken()
        // If this is a public auth endpoint (login/refresh/etc), do not try to add/refresh here
        if (isPublicAuthEndpoint(path)) {
            return chain.proceed(originalRequest)
        }

        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        var requestWithToken = addTokenToRequest(originalRequest, token)
        var response = chain.proceed(requestWithToken)

        // If unauthorized and not an auth public endpoint, try refresh flow
            if (response.code == 401 && !isPublicAuthEndpoint(path)) {
                response.close()

                synchronized(this) {
                // Check if another thread already refreshed
                val latestToken = tokenManager.getAccessToken()
                if (!latestToken.isNullOrEmpty() && latestToken != token) {
                    // Retry with the latest token
                    val newRequest = addTokenToRequest(originalRequest, latestToken)
                    return chain.proceed(newRequest)
                }

                // Attempt refresh using stored refresh token
                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken.isNullOrEmpty()) {
                    // No refresh token available, proceed with original unauthorized response
                    return chain.proceed(originalRequest)
                }

                    try {
                    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                    val json = JSONObject().put("refreshToken", refreshToken).toString()
                    val body = json.toRequestBody(mediaType)

                    // Build the refresh URL using the configured BASE_URL to avoid duplicating or
                    // dropping the /api/v1 segment when manipulating the original request URL.
                    val refreshUrl = ApiConstants.BASE_URL + "auth/refresh"
                    val refreshRequest = originalRequest.newBuilder()
                        .url(refreshUrl)
                        .post(body)
                        .header("Content-Type", "application/json")
                        .build()

                    val refreshResponse = chain.proceed(refreshRequest)

                    if (refreshResponse.isSuccessful) {
                        val respBody = refreshResponse.body?.string()
                        refreshResponse.close()
                        if (respBody != null) {
                            val respJson = JSONObject(respBody)
                            val data = respJson.optJSONObject("data")
                            val newAccess = data?.optString("accessToken")
                            val newRefresh = data?.optString("refreshToken")
                            if (!newAccess.isNullOrEmpty()) {
                                tokenManager.saveAccessToken(newAccess)
                                if (!newRefresh.isNullOrEmpty()) tokenManager.saveRefreshToken(newRefresh)
                                // Retry original request with new access token
                                val retried = addTokenToRequest(originalRequest, newAccess)
                                return chain.proceed(retried)
                            }
                        }
                    } else {
                        refreshResponse.close()
                    }
                    } catch (e: IOException) {
                        // Network error during refresh — fall through
                        Log.w("TokenRefreshInterceptor", "IOException during token refresh: ${e.message}")
                    }
            }
        }
            return response
        } catch (e: Exception) {
            // Catch any unexpected exception to avoid crashing the app from the interceptor
            Log.e("TokenRefreshInterceptor", "Unexpected error in interceptor: ${e.message}", e)
            return chain.proceed(originalRequest)
        }
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
