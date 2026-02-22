package com.example.pocketguard.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Configuración centralizada de Retrofit y OkHttp
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3001/" // 10.0.2.2 es localhost para emulador
    // Para dispositivo físico o testing, cambiar a: "http://localhost:3001/"

    // JSON serializer con configuración flexible
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // OkHttp client con logging
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // API Service singleton
    val authService: AuthApiService = retrofit.create(AuthApiService::class.java)
}

