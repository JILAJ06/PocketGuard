package com.example.pocketguard.data.repository

import android.content.Context
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.api.SubscriptionsService
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.*
import com.example.pocketguard.data.storage.TokenManager
import retrofit2.HttpException

class SubscriptionsRepository(private val context: Context) {

    private val subscriptionsService: SubscriptionsService = RetrofitClient.getSubscriptionsService(context)
    private val tokenManager = TokenManager(context)

    private fun getAuthHeader(): String {
        val token = tokenManager.getAccessToken() ?: ""
        return "Bearer $token"
    }

    suspend fun getAllSubscriptions(): Result<List<Subscription>> = try {
        val response = subscriptionsService.getAllSubscriptions(getAuthHeader())
        if (response.success && response.data?.subscriptions != null) {
            Result.success(response.data!!.subscriptions!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getSubscriptionById(id: String): Result<Subscription> = try {
        val response = subscriptionsService.getSubscriptionById(id, getAuthHeader())
        if (response.success && response.data?.subscription != null) {
            Result.success(response.data!!.subscription!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun createSubscription(request: CreateSubscriptionRequest): Result<Subscription> = try {
        val response = subscriptionsService.createSubscription(request, getAuthHeader())
        if (response.success && response.data?.subscription != null) {
            Result.success(response.data!!.subscription!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateSubscription(id: String, request: UpdateSubscriptionRequest): Result<Subscription> = try {
        val response = subscriptionsService.updateSubscription(id, request, getAuthHeader())
        if (response.success && response.data?.subscription != null) {
            Result.success(response.data!!.subscription!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun deleteSubscription(id: String): Result<Unit> = try {
        val response = subscriptionsService.deleteSubscription(id, getAuthHeader())
        if (response.success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getBillingCycles(): Result<List<BillingCycle>> = try {
        val response = subscriptionsService.getBillingCycles(getAuthHeader())
        if (response.success) {
            Result.success(response.data!!.billing_cycles)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getMetrics(): Result<List<SubscriptionMetric>> = try {
        val response = subscriptionsService.getMetrics(getAuthHeader())
        if (response.success) {
            Result.success(response.data!!.metrics)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }
}
