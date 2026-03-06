package com.example.pocketguard.data.repository

import android.content.Context
import android.util.Log
import com.example.pocketguard.data.api.FCMTokenRequest
import com.example.pocketguard.data.api.NotificationsService
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.Notification
import com.example.pocketguard.data.models.NotificationSettings
import com.example.pocketguard.data.models.UpdateNotificationSettingsRequest
import com.example.pocketguard.data.storage.TokenManager
import retrofit2.HttpException

class NotificationsRepository(private val context: Context) {

    private val notificationsService: NotificationsService = RetrofitClient.getNotificationsService(context)
    private val tokenManager = TokenManager(context)

    private fun getAuthHeader(): String {
        val token = tokenManager.getAccessToken() ?: ""
        return "Bearer $token"
    }

    suspend fun getNotifications(): Result<List<Notification>> = try {
        Log.d("NotificationsRepository", "getNotifications() - Obteniendo notificaciones...")
        val response = notificationsService.getNotifications(getAuthHeader())
        Log.d("NotificationsRepository", "getNotifications() - Response: success=${response.success}")
        if (response.success && response.data != null) {
            Log.d("NotificationsRepository", "getNotifications() - ${response.data.notifications.size} notificaciones obtenidas")
            Result.success(response.data.notifications)
        } else {
            Log.e("NotificationsRepository", "getNotifications() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("NotificationsRepository", "getNotifications() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun markAsRead(notificationId: String): Result<Boolean> = try {
        Log.d("NotificationsRepository", "markAsRead() - Marcando notificación $notificationId...")
        val response = notificationsService.markAsRead(notificationId, getAuthHeader())
        if (response.success) {
            Log.d("NotificationsRepository", "markAsRead() - Notificación marcada como leída")
            Result.success(true)
        } else {
            Log.e("NotificationsRepository", "markAsRead() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("NotificationsRepository", "markAsRead() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getNotificationSettings(): Result<NotificationSettings> = try {
        Log.d("NotificationsRepository", "getNotificationSettings() - Obteniendo configuración...")
        val response = notificationsService.getNotificationSettings(getAuthHeader())
        Log.d("NotificationsRepository", "getNotificationSettings() - Response: success=${response.success}")
        if (response.success && response.data != null) {
            Log.d("NotificationsRepository", "getNotificationSettings() - Configuración obtenida")
            Result.success(response.data)
        } else {
            Log.e("NotificationsRepository", "getNotificationSettings() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("NotificationsRepository", "getNotificationSettings() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateNotificationSettings(request: UpdateNotificationSettingsRequest): Result<NotificationSettings> = try {
        Log.d("NotificationsRepository", "updateNotificationSettings() - Actualizando...")
        val response = notificationsService.updateNotificationSettings(request, getAuthHeader())
        if (response.success && response.data != null) {
            Log.d("NotificationsRepository", "updateNotificationSettings() - Configuración actualizada")
            Result.success(response.data)
        } else {
            Log.e("NotificationsRepository", "updateNotificationSettings() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("NotificationsRepository", "updateNotificationSettings() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun registerFCMToken(fcmToken: String): Result<Boolean> = try {
        Log.d("NotificationsRepository", "registerFCMToken() - Registrando token FCM...")
        val request = FCMTokenRequest(fcmToken = fcmToken, platform = "android")
        val response = notificationsService.registerFCMToken(request, getAuthHeader())
        if (response.success) {
            Log.d("NotificationsRepository", "registerFCMToken() - Token registrado correctamente")
            Result.success(true)
        } else {
            Log.e("NotificationsRepository", "registerFCMToken() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("NotificationsRepository", "registerFCMToken() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun removeFCMToken(fcmToken: String): Result<Boolean> = try {
        Log.d("NotificationsRepository", "removeFCMToken() - Eliminando token FCM...")
        val request = FCMTokenRequest(fcmToken = fcmToken, platform = "android")
        val response = notificationsService.removeFCMToken(request, getAuthHeader())
        if (response.success) {
            Log.d("NotificationsRepository", "removeFCMToken() - Token eliminado correctamente")
            Result.success(true)
        } else {
            Log.e("NotificationsRepository", "removeFCMToken() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("NotificationsRepository", "removeFCMToken() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun sendTestNotification(): Result<Boolean> = try {
        Log.d("NotificationsRepository", "sendTestNotification() - Enviando notificación de prueba...")
        val response = notificationsService.sendTestNotification(getAuthHeader())
        if (response.success) {
            Log.d("NotificationsRepository", "sendTestNotification() - Notificación enviada correctamente")
            Result.success(true)
        } else {
            Log.e("NotificationsRepository", "sendTestNotification() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("NotificationsRepository", "sendTestNotification() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }
}
