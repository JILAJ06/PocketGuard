package com.example.pocketguard.data.models

data class Notification(
    val id: String,
    val userId: String,
    val type: String,
    val priority: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val subscriptionId: String?,
    val metadata: Map<String, Any>?,
    val createdAt: String
)

data class NotificationData(
    val notifications: List<Notification>
)

data class NotificationSettings(
    val email_enabled: Boolean,
    val push_enabled: Boolean,
    val subscription_reminders: Boolean,
    val days_before_notice: Int
)

data class NotificationSettingsResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: NotificationSettings? = null
)

data class UpdateNotificationSettingsRequest(
    val email_enabled: Boolean? = null,
    val push_enabled: Boolean? = null,
    val subscription_reminders: Boolean? = null,
    val days_before_notice: Int? = null
)

