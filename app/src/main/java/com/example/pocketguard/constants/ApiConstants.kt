package com.example.pocketguard.constants

object ApiConstants {
    // API en producción (Railway)
    const val BASE_URL = "https://web-production-43b24.up.railway.app/api/v1/"

    // Para desarrollo local, usa esto (descomenta y comenta la línea de arriba):
    // const val BASE_URL = "http://192.168.110.230:3001/api/v1/"

    const val GOOGLE_CLIENT_ID = "948325541313-gr9dhia5r6gidgejehr6si9s89r0tdv2.apps.googleusercontent.com"
    const val AUTH_ENDPOINT = "auth"

    // Endpoints de autenticación
    const val LOGIN_ENDPOINT = "$AUTH_ENDPOINT/login"
    const val REGISTER_ENDPOINT = "$AUTH_ENDPOINT/register"
    const val GOOGLE_LOGIN_ENDPOINT = "$AUTH_ENDPOINT/google"
    const val GOOGLE_MOBILE_LOGIN_ENDPOINT = "$AUTH_ENDPOINT/google/mobile"
    const val GOOGLE_CALLBACK_ENDPOINT = "$AUTH_ENDPOINT/google/callback"
    const val REFRESH_TOKEN_ENDPOINT = "$AUTH_ENDPOINT/refresh"
    const val LOGOUT_ENDPOINT = "$AUTH_ENDPOINT/logout"
    const val GET_PROFILE_ENDPOINT = "$AUTH_ENDPOINT/me"

    // Nombres de preferencias compartidas
    const val SHARED_PREFERENCES_NAME = "PocketGuard_Preferences"
    const val ACCESS_TOKEN_KEY = "access_token"
    const val REFRESH_TOKEN_KEY = "refresh_token"
    const val USER_ID_KEY = "user_id"

    // Tiempos de expiración
    const val ACCESS_TOKEN_EXPIRATION_MINUTES = 15
    const val REFRESH_TOKEN_EXPIRATION_DAYS = 7
}

