package com.example.pocketguard.data.api

import android.content.Context
import android.util.Log
import com.example.pocketguard.constants.ApiConstants
import com.example.pocketguard.data.storage.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Interceptor que agrega automáticamente el token de autenticación a todas las solicitudes
 * También maneja la inyección del token en el header Authorization
 */
class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.encodedPath

        Log.d("AuthInterceptor", "Request URL: $url")

        // No agregar token a endpoints públicos de autenticación
        if (isPublicAuthEndpoint(url) || url.contains("/health")) {
            Log.d("AuthInterceptor", "Endpoint público, no se agrega token")
            return chain.proceed(originalRequest)
        }

        val token = getAccessToken()
        Log.d("AuthInterceptor", "Token: ${if (token.isEmpty()) "vacío" else "presente (${token.length} chars)"}")

        val requestWithToken = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return try {
            val response = chain.proceed(requestWithToken)
            Log.d("AuthInterceptor", "Response code: ${response.code}")
            response
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error en request: ${e.message}", e)
            throw e
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

    /**
     * Obtener el token de acceso guardado en SharedPreferences
     */
    private fun getAccessToken(): String {
        val tokenManager = TokenManager(context)
        return tokenManager.getAccessToken() ?: ""
    }
}

/**
 * Interceptor para logging de solicitudes y respuestas HTTP
 * Útil para debugging
 */
class HttpLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val startTime = System.nanoTime()
        println("Iniciando solicitud: ${request.url}")
        println("Headers: ${request.headers}")

        val response = chain.proceed(request)
        val duration = (System.nanoTime() - startTime) / 1_000_000

        println("Respuesta recibida en: ${duration}ms")
        println("Código: ${response.code}")

        return response
    }
}
