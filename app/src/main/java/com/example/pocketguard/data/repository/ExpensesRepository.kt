package com.example.pocketguard.data.repository

import android.content.Context
import android.util.Log
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
        Log.d("ExpensesRepository", "getAllExpenses() - Obteniendo gastos...")
        val response = expensesService.getAllExpenses(getAuthHeader())
        Log.d("ExpensesRepository", "getAllExpenses() - Response: success=${response.success}")
        if (response.success && response.data?.expenses != null) {
            Log.d("ExpensesRepository", "getAllExpenses() - ${response.data.expenses.size} gastos")
            Result.success(response.data.expenses)
        } else {
            Log.e("ExpensesRepository", "getAllExpenses() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("ExpensesRepository", "getAllExpenses() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getExpenseById(id: String): Result<Expense> = try {
        Log.d("ExpensesRepository", "getExpenseById() - ID: $id")
        val response = expensesService.getExpenseById(id, getAuthHeader())
        if (response.success && response.data?.expense != null) {
            Result.success(response.data.expense)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("ExpensesRepository", "getExpenseById() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun createExpense(request: CreateExpenseRequest): Result<Expense> = try {
        Log.d("ExpensesRepository", "createExpense() - Nombre: ${request.name}")
        val response = expensesService.createExpense(request, getAuthHeader())
        if (response.success && response.data?.expense != null) {
            Log.d("ExpensesRepository", "createExpense() - Gasto creado")
            Result.success(response.data.expense)
        } else {
            Log.e("ExpensesRepository", "createExpense() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("ExpensesRepository", "createExpense() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateExpense(id: String, request: UpdateExpenseRequest): Result<Expense> = try {
        Log.d("ExpensesRepository", "updateExpense() - ID: $id")
        val response = expensesService.updateExpense(id, request, getAuthHeader())
        if (response.success && response.data?.expense != null) {
            Log.d("ExpensesRepository", "updateExpense() - Gasto actualizado")
            Result.success(response.data.expense)
        } else {
            Log.e("ExpensesRepository", "updateExpense() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("ExpensesRepository", "updateExpense() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun deleteExpense(id: String): Result<Unit> = try {
        Log.d("ExpensesRepository", "deleteExpense() - ID: $id")
        val response = expensesService.deleteExpense(id, getAuthHeader())
        if (response.success) {
            Log.d("ExpensesRepository", "deleteExpense() - Gasto eliminado")
            Result.success(Unit)
        } else {
            Log.e("ExpensesRepository", "deleteExpense() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("ExpensesRepository", "deleteExpense() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getExpensesSummary(month: Int? = null, year: Int? = null): Result<ExpenseSummary> = try {
        Log.d("ExpensesRepository", "getExpensesSummary() - Month: $month, Year: $year")
        val response = expensesService.getExpensesSummary(month, year, getAuthHeader())
        if (response.success) {
            Log.d("ExpensesRepository", "getExpensesSummary() - Summary obtenido")
            Result.success(response.data!!.summary)
        } else {
            Log.e("ExpensesRepository", "getExpensesSummary() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("ExpensesRepository", "getExpensesSummary() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }
}
