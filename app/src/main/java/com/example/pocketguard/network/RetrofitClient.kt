package com.example.pocketguard.network

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Para emulador Android: http://10.0.2.2:3001
    // Para dispositivo físico: http://192.168.x.x:3001
    private const val BASE_URL = "http://10.0.2.2:3001/api/v1/"

    private var context: Context? = null

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(ErrorInterceptor())
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)

            // Agregar AuthInterceptor solo si tenemos contexto
            if (context != null) {
                builder.addInterceptor(AuthInterceptor(context!!))
            }

            return builder.build()
        }

    val api: PocketGuardApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(PocketGuardApi::class.java)

    fun setContext(ctx: Context) {
        context = ctx
    }
}
