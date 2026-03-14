package com.example.pocketguard.data.repository

import android.content.Context
import android.util.Log
import com.example.pocketguard.data.api.BanksService
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.*
import com.example.pocketguard.data.storage.TokenManager
import retrofit2.HttpException

class BanksRepository(private val context: Context) {

    private val banksService: BanksService = RetrofitClient.getBanksService(context)
    private val tokenManager = TokenManager(context)

    private fun getAuthHeader(): String {
        val token = tokenManager.getAccessToken() ?: ""
        return "Bearer $token"
    }

    suspend fun getAllBanks(): Result<List<Bank>> = try {
        Log.d("BanksRepository", "getAllBanks() - Obteniendo bancos...")
        val response = banksService.getAllBanks(getAuthHeader())
        Log.d("BanksRepository", "getAllBanks() - Response: success=${response.success}")
        if (response.success && response.data?.banks != null) {
            Log.d("BanksRepository", "getAllBanks() - ${response.data.banks.size} bancos")
            Result.success(response.data.banks)
        } else {
            Log.e("BanksRepository", "getAllBanks() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("BanksRepository", "getAllBanks() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getGlobalBanks(): Result<List<Bank>> = try {
        val response = banksService.getGlobalBanks(getAuthHeader())
        if (response.success && response.data?.banks != null) {
            Result.success(response.data!!.banks!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getBankById(id: String): Result<Bank> = try {
        val response = banksService.getBankById(id, getAuthHeader())
        if (response.success && response.data?.bank != null) {
            Result.success(response.data!!.bank!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun createBank(request: CreateBankRequest): Result<Bank> = try {
        Log.d("BanksRepository", "createBank() - Nombre: ${request.name}")
        val response = banksService.createBank(request, getAuthHeader())
        if (response.success && response.data?.bank != null) {
            Log.d("BanksRepository", "createBank() - Banco creado")
            Result.success(response.data.bank)
        } else {
            Log.e("BanksRepository", "createBank() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("BanksRepository", "createBank() - Exception: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateBank(id: String, request: UpdateBankRequest): Result<Bank> = try {
        val response = banksService.updateBank(id, request, getAuthHeader())
        if (response.success && response.data?.bank != null) {
            Result.success(response.data!!.bank!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun deleteBank(id: String): Result<Unit> = try {
        val response = banksService.deleteBank(id, getAuthHeader())
        if (response.success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }
}
