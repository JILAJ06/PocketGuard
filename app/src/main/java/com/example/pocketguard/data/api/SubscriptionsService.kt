package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface SubscriptionsService {

    @GET("subscriptions")
    suspend fun getAllSubscriptions(@Header("Authorization") token: String): ApiResponse<SubscriptionData>

    @GET("subscriptions/{id}")
    suspend fun getSubscriptionById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<SubscriptionData>

    @POST("subscriptions")
    suspend fun createSubscription(
        @Body request: CreateSubscriptionRequest,
        @Header("Authorization") token: String
    ): ApiResponse<SubscriptionData>

    @PATCH("subscriptions/{id}")
    suspend fun updateSubscription(
        @Path("id") id: String,
        @Body request: UpdateSubscriptionRequest,
        @Header("Authorization") token: String
    ): ApiResponse<SubscriptionData>

    @DELETE("subscriptions/{id}")
    suspend fun deleteSubscription(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, String>>

    @GET("subscriptions/billing-cycles")
    suspend fun getBillingCycles(@Header("Authorization") token: String): ApiResponse<BillingCycleData>

    @GET("subscriptions/metrics")
    suspend fun getMetrics(@Header("Authorization") token: String): ApiResponse<SubscriptionMetricsData>
}

