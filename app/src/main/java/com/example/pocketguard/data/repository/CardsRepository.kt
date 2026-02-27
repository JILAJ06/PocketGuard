package com.example.pocketguard.data.repository

import android.content.Context
import com.example.pocketguard.data.api.CardsService
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.*
import com.example.pocketguard.data.storage.TokenManager
import retrofit2.HttpException

class CardsRepository(private val context: Context) {

    private val cardsService: CardsService = RetrofitClient.getCardsService(context)
    private val tokenManager = TokenManager(context)

    private fun getAuthHeader(): String {
        val token = tokenManager.getAccessToken() ?: ""
        return "Bearer $token"
    }

    suspend fun getAllCards(): Result<List<Card>> = try {
        val response = cardsService.getAllCards(getAuthHeader())
        if (response.success && response.data?.cards != null) {
            Result.success(response.data!!.cards!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getCardById(id: String): Result<Card> = try {
        val response = cardsService.getCardById(id, getAuthHeader())
        if (response.success && response.data?.card != null) {
            Result.success(response.data!!.card!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun createCard(request: CreateCardRequest): Result<Card> = try {
        val response = cardsService.createCard(request, getAuthHeader())
        if (response.success && response.data?.card != null) {
            Result.success(response.data!!.card!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateCard(id: String, request: UpdateCardRequest): Result<Card> = try {
        val response = cardsService.updateCard(id, request, getAuthHeader())
        if (response.success && response.data?.card != null) {
            Result.success(response.data!!.card!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun deleteCard(id: String): Result<Unit> = try {
        val response = cardsService.deleteCard(id, getAuthHeader())
        if (response.success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun setDefaultCard(id: String): Result<Card> = try {
        val response = cardsService.setDefaultCard(id, getAuthHeader())
        if (response.success && response.data?.card != null) {
            Result.success(response.data!!.card!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }
}

