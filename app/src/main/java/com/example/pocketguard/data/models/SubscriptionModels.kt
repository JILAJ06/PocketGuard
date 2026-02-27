package com.example.pocketguard.data.models

data class Subscription(
    val subscription_id: String,
    val user_id: String,
    val service_name: String,
    val amount: Double,
    val next_payment_date: String,
    val category_name: String,
    val category_icon: String?,
    val category_color: String,
    val billing_cycle: String,
    val card_alias: String?,
    val card_last_digits: String?,
    val days_until_payment: Int
)

data class CreateSubscriptionRequest(
    val service_name: String,
    val amount: Double,
    val next_payment_date: String,
    val billing_cycle_id: Int,
    val category_id: String,
    val used_card_id: String? = null
)

data class UpdateSubscriptionRequest(
    val service_name: String? = null,
    val amount: Double? = null,
    val next_payment_date: String? = null,
    val billing_cycle_id: Int? = null,
    val category_id: String? = null,
    val used_card_id: String? = null
)

data class SubscriptionResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: SubscriptionData
)

data class SubscriptionData(
    val subscriptions: List<Subscription>? = null,
    val subscription: Subscription? = null
)

data class BillingCycle(
    val id: Int,
    val name: String
)

data class BillingCycleResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: BillingCycleData
)

data class BillingCycleData(
    val billing_cycles: List<BillingCycle>
)

data class SubscriptionMetric(
    val user_id: String,
    val service_name: String,
    val amount: Double,
    val billing_cycle: String,
    val annualized_cost: Double
)

data class SubscriptionMetricsResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: SubscriptionMetricsData
)

data class SubscriptionMetricsData(
    val metrics: List<SubscriptionMetric>
)

