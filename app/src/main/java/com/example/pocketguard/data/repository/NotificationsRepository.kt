package com.example.pocketguard.data.repository

import android.content.Context
import com.example.pocketguard.data.api.NotificationsService
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.exceptions.AuthenticationException
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

    suspend fun getNotificationSettings(): Result<NotificationSettings> = try {
        val response = notificationsService.getNotificationSettings(getAuthHeader())
        if (response.success && response.data != null) {
            Result.success(response.data!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateNotificationSettings(request: UpdateNotificationSettingsRequest): Result<NotificationSettings> = try {
        val response = notificationsService.updateNotificationSettings(request, getAuthHeader())
        if (response.success && response.data != null) {
            Result.success(response.data!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }
}

