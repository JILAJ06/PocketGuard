package com.example.pocketguard.data.repository

import android.content.Context
import com.example.pocketguard.data.api.ExpensesService
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.*
import com.example.pocketguard.data.storage.TokenManager
import retrofit2.HttpException

class ExpensesRepository(private val context: Context) {

    private val expensesService: ExpensesService = RetrofitClient.getExpensesService(context)
    private val tokenManager = TokenManager(context)

    private fun getAuthHeader(): String {
        val token = tokenManager.getAccessToken() ?: ""
        return "Bearer $token"
    }

    suspend fun getAllExpenses(): Result<List<Expense>> = try {
        val response = expensesService.getAllExpenses(getAuthHeader())
        if (response.success && response.data?.expenses != null) {
            Result.success(response.data!!.expenses!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getExpenseById(id: String): Result<Expense> = try {
        val response = expensesService.getExpenseById(id, getAuthHeader())
        if (response.success && response.data?.expense != null) {
            Result.success(response.data!!.expense!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun createExpense(request: CreateExpenseRequest): Result<Expense> = try {
        val response = expensesService.createExpense(request, getAuthHeader())
        if (response.success && response.data?.expense != null) {
            Result.success(response.data!!.expense!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateExpense(id: String, request: UpdateExpenseRequest): Result<Expense> = try {
        val response = expensesService.updateExpense(id, request, getAuthHeader())
        if (response.success && response.data?.expense != null) {
            Result.success(response.data!!.expense!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun deleteExpense(id: String): Result<Unit> = try {
        val response = expensesService.deleteExpense(id, getAuthHeader())
        if (response.success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getExpensesSummary(month: Int? = null, year: Int? = null): Result<ExpenseSummary> = try {
        val response = expensesService.getExpensesSummary(month, year, getAuthHeader())
        if (response.success) {
            Result.success(response.data!!.summary)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }
}
