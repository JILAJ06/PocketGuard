package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface NotificationsService {

    @GET("notifications/settings")
    suspend fun getNotificationSettings(@Header("Authorization") token: String): ApiResponse<NotificationSettings>

    @PATCH("notifications/settings")
    suspend fun updateNotificationSettings(
        @Body request: UpdateNotificationSettingsRequest,
        @Header("Authorization") token: String
    ): ApiResponse<NotificationSettings>
}

