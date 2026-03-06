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

    @POST("device-tokens")
    suspend fun registerFCMToken(
        @Body request: FCMTokenRequest,
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, String>>

    @HTTP(method = "DELETE", path = "device-tokens", hasBody = true)
    suspend fun removeFCMToken(
        @Body request: FCMTokenRequest,
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, String>>

    @POST("notifications/test-push")
    suspend fun sendTestNotification(
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, String>>
}

data class FCMTokenRequest(
    val fcmToken: String,
    val platform: String = "android"
)

