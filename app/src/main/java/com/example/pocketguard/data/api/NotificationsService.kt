package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface NotificationsService {

    @GET("notifications")
    suspend fun getNotifications(@Header("Authorization") token: String): ApiResponse<NotificationData>

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, String>>

    @GET("notifications/settings")
    suspend fun getNotificationSettings(@Header("Authorization") token: String): ApiResponse<NotificationSettings>

    @PATCH("notifications/settings")
    suspend fun updateNotificationSettings(
        @Body request: UpdateNotificationSettingsRequest,
        @Header("Authorization") token: String
    ): ApiResponse<NotificationSettings>
}

