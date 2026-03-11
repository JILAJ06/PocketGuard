package com.example.pocketguard.constants

object ApiConstants {
    // ⚠️ IMPORTANTE: Cambia esta IP a la IP de tu computadora donde corre el backend
    // Para obtener tu IP:
    // 1. Abre terminal/cmd en tu PC
    // 2. Ejecuta: ipconfig (Windows) o ifconfig (Mac/Linux)
    // 3. Busca la IPv4 de tu WiFi (ejemplo: 192.168.x.x o 10.x.x.x)
    // 4. Asegúrate que tu teléfono esté en la misma red WiFi
    const val BASE_URL = "http://10.27.49.163:3001/api/v1/"

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

