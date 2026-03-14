package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface BanksService {

    @GET("banks")
    suspend fun getAllBanks(@Header("Authorization") token: String): ApiResponse<BankData>

    @GET("banks/global")
    suspend fun getGlobalBanks(@Header("Authorization") token: String): ApiResponse<BankData>

    @GET("banks/{id}")
    suspend fun getBankById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<BankData>

    @POST("banks")
    suspend fun createBank(
        @Body request: CreateBankRequest,
        @Header("Authorization") token: String
    ): ApiResponse<BankData>

    @PATCH("banks/{id}")
    suspend fun updateBank(
        @Path("id") id: String,
        @Body request: UpdateBankRequest,
        @Header("Authorization") token: String
    ): ApiResponse<BankData>

    @DELETE("banks/{id}")
    suspend fun deleteBank(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, String>>
}

