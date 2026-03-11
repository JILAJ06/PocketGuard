package com.example.pocketguard.data.repository

import android.content.Context
import android.util.Log
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
        Log.d("SubscriptionsRepository", "getAllSubscriptions() - Obteniendo suscripciones...")
        val response = subscriptionsService.getAllSubscriptions(getAuthHeader())
        Log.d("SubscriptionsRepository", "getAllSubscriptions() - Response: success=${response.success}")
        if (response.success && response.data?.subscriptions != null) {
            Log.d("SubscriptionsRepository", "getAllSubscriptions() - ${response.data.subscriptions.size} suscripciones")
            Result.success(response.data.subscriptions)
        } else {
            Log.e("SubscriptionsRepository", "getAllSubscriptions() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("SubscriptionsRepository", "getAllSubscriptions() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getSubscriptionById(id: String): Result<Subscription> = try {
        Log.d("SubscriptionsRepository", "getSubscriptionById() - ID: $id")
        val response = subscriptionsService.getSubscriptionById(id, getAuthHeader())
        if (response.success && response.data?.subscription != null) {
            Result.success(response.data.subscription)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("SubscriptionsRepository", "getSubscriptionById() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun createSubscription(request: CreateSubscriptionRequest): Result<Subscription> = try {
        Log.d("SubscriptionsRepository", "createSubscription() - Servicio: ${request.service_name}")
        val response = subscriptionsService.createSubscription(request, getAuthHeader())
        if (response.success && response.data?.subscription != null) {
            Log.d("SubscriptionsRepository", "createSubscription() - Suscripción creada")
            Result.success(response.data.subscription)
        } else {
            Log.e("SubscriptionsRepository", "createSubscription() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("SubscriptionsRepository", "createSubscription() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateSubscription(id: String, request: UpdateSubscriptionRequest): Result<Subscription> = try {
        Log.d("SubscriptionsRepository", "updateSubscription() - ID: $id")
        val response = subscriptionsService.updateSubscription(id, request, getAuthHeader())
        if (response.success && response.data?.subscription != null) {
            Log.d("SubscriptionsRepository", "updateSubscription() - Suscripción actualizada")
            Result.success(response.data.subscription)
        } else {
            Log.e("SubscriptionsRepository", "updateSubscription() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("SubscriptionsRepository", "updateSubscription() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun deleteSubscription(id: String): Result<Unit> = try {
        Log.d("SubscriptionsRepository", "deleteSubscription() - ID: $id")
        val response = subscriptionsService.deleteSubscription(id, getAuthHeader())
        if (response.success) {
            Log.d("SubscriptionsRepository", "deleteSubscription() - Suscripción eliminada")
            Result.success(Unit)
        } else {
            Log.e("SubscriptionsRepository", "deleteSubscription() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("SubscriptionsRepository", "deleteSubscription() - Exception: ${e.message}", e)
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
