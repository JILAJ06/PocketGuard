package com.example.pocketguard.data.models

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

data class Subscription(
    val id: String,
    val name: String,
    val category: String,
    val monthlyPrice: Double,
    val currentMonthPrice: Double,
    val nextPaymentDate: LocalDate,
    val isActive: Boolean,
    val color: Color,
    val icon: String
)

data class SubscriptionStats(
    val totalMonthly: Double,
    val totalCurrent: Double,
    val activeCount: Int,
    val totalCount: Int
)

