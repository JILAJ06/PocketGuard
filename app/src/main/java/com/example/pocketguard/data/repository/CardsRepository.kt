package com.example.pocketguard.data.repository

import android.content.Context
import android.util.Log
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
        Log.d("CardsRepository", "getAllCards() - Obteniendo tarjetas...")
        val response = cardsService.getAllCards(getAuthHeader())
        Log.d("CardsRepository", "getAllCards() - Response: success=${response.success}")
        if (response.success && response.data?.cards != null) {
            Log.d("CardsRepository", "getAllCards() - ${response.data!!.cards!!.size} tarjetas cargadas")
            Result.success(response.data!!.cards!!)
        } else {
            Log.e("CardsRepository", "getAllCards() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("CardsRepository", "getAllCards() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getCardById(id: String): Result<Card> = try {
        Log.d("CardsRepository", "getCardById() - ID: $id")
        val response = cardsService.getCardById(id, getAuthHeader())
        if (response.success && response.data?.card != null) {
            Log.d("CardsRepository", "getCardById() - Tarjeta encontrada")
            Result.success(response.data!!.card!!)
        } else {
            Log.e("CardsRepository", "getCardById() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("CardsRepository", "getCardById() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun createCard(request: CreateCardRequest): Result<Card> = try {
        Log.d("CardsRepository", "createCard() - Alias: ${request.alias}")
        val response = cardsService.createCard(request, getAuthHeader())
        if (response.success && response.data?.card != null) {
            Log.d("CardsRepository", "createCard() - Tarjeta creada exitosamente")
            Result.success(response.data!!.card!!)
        } else {
            Log.e("CardsRepository", "createCard() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("CardsRepository", "createCard() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateCard(id: String, request: UpdateCardRequest): Result<Card> = try {
        Log.d("CardsRepository", "updateCard() - ID: $id")
        val response = cardsService.updateCard(id, request, getAuthHeader())
        if (response.success && response.data?.card != null) {
            Log.d("CardsRepository", "updateCard() - Tarjeta actualizada")
            Result.success(response.data!!.card!!)
        } else {
            Log.e("CardsRepository", "updateCard() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("CardsRepository", "updateCard() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun deleteCard(id: String): Result<Unit> = try {
        Log.d("CardsRepository", "deleteCard() - ID: $id")
        val response = cardsService.deleteCard(id, getAuthHeader())
        if (response.success) {
            Log.d("CardsRepository", "deleteCard() - Tarjeta eliminada")
            Result.success(Unit)
        } else {
            Log.e("CardsRepository", "deleteCard() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("CardsRepository", "deleteCard() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun setDefaultCard(id: String): Result<Card> = try {
        Log.d("CardsRepository", "setDefaultCard() - ID: $id")
        val response = cardsService.setDefaultCard(id, getAuthHeader())
        if (response.success && response.data?.card != null) {
            Log.d("CardsRepository", "setDefaultCard() - Tarjeta marcada como predeterminada")
            Result.success(response.data!!.card!!)
        } else {
            Log.e("CardsRepository", "setDefaultCard() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("CardsRepository", "setDefaultCard() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }
}

