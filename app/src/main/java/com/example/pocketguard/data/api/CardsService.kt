package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface CardsService {

    @GET("cards")
    suspend fun getAllCards(@Header("Authorization") token: String): ApiResponse<CardData>

    @GET("cards/{id}")
    suspend fun getCardById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<CardData>

    @POST("cards")
    suspend fun createCard(
        @Body request: CreateCardRequest,
        @Header("Authorization") token: String
    ): ApiResponse<CardData>

    @PATCH("cards/{id}")
    suspend fun updateCard(
        @Path("id") id: String,
        @Body request: UpdateCardRequest,
        @Header("Authorization") token: String
    ): ApiResponse<CardData>

    @DELETE("cards/{id}")
    suspend fun deleteCard(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, String>>

    @PATCH("cards/{id}/default")
    suspend fun setDefaultCard(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<CardData>
}

