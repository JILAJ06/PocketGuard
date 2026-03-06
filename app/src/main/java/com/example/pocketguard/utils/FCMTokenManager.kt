package com.example.pocketguard.utils

import android.content.Context
import android.util.Log
import com.example.pocketguard.data.repository.NotificationsRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Manager para manejar el token FCM (Firebase Cloud Messaging)
 * Se encarga de obtener, guardar y enviar el token al backend
 */
class FCMTokenManager(private val context: Context) {

    companion object {
        private const val TAG = "FCMTokenManager"
        private const val PREFS_NAME = "fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_TOKEN_SENT = "fcm_token_sent_to_server"
    }

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val notificationsRepository = NotificationsRepository(context)

    /**
     * Obtiene el token FCM actual guardado localmente
     */
    fun getSavedToken(): String? {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null)
    }

    /**
     * Guarda el token FCM localmente
     */
    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
        Log.d(TAG, "Token FCM guardado localmente: ${token.take(20)}...")
    }

    /**
     * Marca si el token fue enviado al servidor
     */
    private fun setTokenSentToServer(sent: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_TOKEN_SENT, sent)
            .apply()
    }

    /**
     * Verifica si el token fue enviado al servidor
     */
    private fun isTokenSentToServer(): Boolean {
        return sharedPreferences.getBoolean(KEY_TOKEN_SENT, false)
    }

    /**
     * Limpia el token guardado (llamar al hacer logout)
     */
    fun clearToken() {
        sharedPreferences.edit()
            .remove(KEY_FCM_TOKEN)
            .remove(KEY_TOKEN_SENT)
            .apply()
        Log.d(TAG, "Token FCM eliminado localmente")
    }

    /**
     * Obtiene el token FCM de Firebase y lo guarda
     */
    suspend fun fetchAndSaveToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Token FCM obtenido de Firebase: ${token.take(20)}...")
            saveToken(token)
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener token FCM: ${e.message}", e)
            null
        }
    }

    /**
     * Registra el token FCM en el backend
     * Se debe llamar después de login exitoso
     */
    fun registerTokenWithBackend() {
        val token = getSavedToken()
        if (token == null) {
            Log.w(TAG, "No hay token FCM para registrar")
            return
        }

        if (isTokenSentToServer()) {
            Log.d(TAG, "Token FCM ya fue enviado al servidor")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = notificationsRepository.registerFCMToken(token)
                if (result.isSuccess) {
                    setTokenSentToServer(true)
                    Log.d(TAG, "Token FCM registrado en el backend correctamente")
                } else {
                    Log.e(TAG, "Error al registrar token en backend: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al registrar token: ${e.message}", e)
            }
        }
    }

    /**
     * Elimina el token FCM del backend
     * Se debe llamar al hacer logout
     */
    fun removeTokenFromBackend() {
        val token = getSavedToken()
        if (token == null) {
            Log.w(TAG, "No hay token FCM para eliminar")
            clearToken()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = notificationsRepository.removeFCMToken(token)
                if (result.isSuccess) {
                    clearToken()
                    Log.d(TAG, "Token FCM eliminado del backend correctamente")
                } else {
                    Log.e(TAG, "Error al eliminar token del backend: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción al eliminar token: ${e.message}", e)
            }
        }
    }

    /**
     * Inicializa el FCM: obtiene el token y lo registra si es necesario
     * Llamar en MainActivity después de verificar que el usuario está logueado
     */
    suspend fun initializeFCM() {
        Log.d(TAG, "Inicializando FCM...")

        // Obtener el token de Firebase
        val token = fetchAndSaveToken()

        if (token != null) {
            // Registrar el token en el backend
            registerTokenWithBackend()
        } else {
            Log.e(TAG, "No se pudo obtener el token FCM")
        }
    }

    /**
     * Actualiza el token cuando Firebase genera uno nuevo
     * Llamar desde MyFirebaseMessagingService.onNewToken()
     */
    fun onTokenRefreshed(newToken: String) {
        Log.d(TAG, "Token FCM actualizado")
        saveToken(newToken)
        // Marcar como no enviado para que se envíe al backend
        setTokenSentToServer(false)
        registerTokenWithBackend()
    }
}

