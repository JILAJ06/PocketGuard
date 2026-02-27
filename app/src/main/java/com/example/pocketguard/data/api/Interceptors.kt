package com.example.pocketguard.data.api

import android.content.Context
import com.example.pocketguard.constants.ApiConstants
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

        // Obtener el token de las preferencias compartidas
        val token = getAccessToken()

        // Si existe un token, agregarlo al header Authorization
        val requestWithToken = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(requestWithToken)
    }

    /**
     * Obtener el token de acceso guardado en SharedPreferences
     */
    private fun getAccessToken(): String {
        val sharedPreferences = context.getSharedPreferences(
            ApiConstants.SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getString(ApiConstants.ACCESS_TOKEN_KEY, "") ?: ""
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

