package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface ExpensesService {

    @GET("expenses")
    suspend fun getAllExpenses(@Header("Authorization") token: String): ApiResponse<ExpenseData>

    @GET("expenses/{id}")
    suspend fun getExpenseById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<ExpenseData>

    @POST("expenses")
    suspend fun createExpense(
        @Body request: CreateExpenseRequest,
        @Header("Authorization") token: String
    ): ApiResponse<ExpenseData>

    @PATCH("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Body request: UpdateExpenseRequest,
        @Header("Authorization") token: String
    ): ApiResponse<ExpenseData>

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, String>>

    @GET("expenses/summary")
    suspend fun getExpensesSummary(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null,
        @Header("Authorization") token: String
    ): ApiResponse<ExpenseSummaryData>
}

