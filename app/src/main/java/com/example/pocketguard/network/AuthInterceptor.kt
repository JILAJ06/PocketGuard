package com.example.pocketguard.network

import android.content.Context
import com.example.pocketguard.data.store.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Si es un endpoint que no requiere autenticación, pasar sin token
        if (path.contains("auth/register") ||
            path.contains("auth/login") ||
            path.contains("auth/google") ||
            path.contains("health")) {
            return chain.proceed(originalRequest)
        }

        // Obtener token guardado
        val tokenStore = TokenStore(context)
        val token = runBlocking { tokenStore.getAccessTokenSync() }

        // Si no hay token, dejar pasar
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Agregar token al header
        val requestWithToken = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(requestWithToken)

        // Si recibimos 401, intentar renovar el token
        if (response.code == 401 && !path.contains("auth/refresh")) {
            response.close()

            // Intentar renovar token
            val refreshRequest = originalRequest.newBuilder()
                .url(originalRequest.url.toString().replace(path, "/api/v1/auth/refresh"))
                .post("".toRequestBody(null))
                .build()

            val refreshResponse = chain.proceed(refreshRequest)

            if (refreshResponse.code == 200) {
                try {
                    // Parsear nuevo token de la respuesta
                    val responseBody = refreshResponse.body?.string()
                    if (responseBody != null && responseBody.contains("accessToken")) {
                        // Extraer token
                        val newToken = responseBody
                            .substringAfter("\"accessToken\":\"")
                            .substringBefore("\"")

                        // Guardar nuevo token
                        runBlocking {
                            tokenStore.saveAccessToken(newToken)
                        }

                        // Reintentar request original con nuevo token
                        val retryRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()

                        return chain.proceed(retryRequest)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            refreshResponse.close()
        }

        return response
    }
}
