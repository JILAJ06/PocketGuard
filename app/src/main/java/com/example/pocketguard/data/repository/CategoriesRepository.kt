package com.example.pocketguard.data.repository

import android.content.Context
import com.example.pocketguard.data.api.CategoriesService
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.*
import com.example.pocketguard.data.storage.TokenManager
import retrofit2.HttpException

class CategoriesRepository(private val context: Context) {

    private val categoriesService: CategoriesService = RetrofitClient.getCategoriesService(context)
    private val tokenManager = TokenManager(context)

    private fun getAuthHeader(): String {
        val token = tokenManager.getAccessToken() ?: ""
        return "Bearer $token"
    }

    suspend fun getAllCategories(): Result<List<Category>> = try {
        val response = categoriesService.getAllCategories(getAuthHeader())
        if (response.success && response.data?.categories != null) {
            Result.success(response.data!!.categories!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getGlobalCategories(): Result<List<Category>> = try {
        val response = categoriesService.getGlobalCategories(getAuthHeader())
        if (response.success && response.data?.categories != null) {
            Result.success(response.data!!.categories!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun getCategoryById(id: String): Result<Category> = try {
        val response = categoriesService.getCategoryById(id, getAuthHeader())
        if (response.success && response.data?.category != null) {
            Result.success(response.data!!.category!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun createCategory(request: CreateCategoryRequest): Result<Category> = try {
        val response = categoriesService.createCategory(request, getAuthHeader())
        if (response.success && response.data?.category != null) {
            Result.success(response.data!!.category!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updateCategory(id: String, request: UpdateCategoryRequest): Result<Category> = try {
        val response = categoriesService.updateCategory(id, request, getAuthHeader())
        if (response.success && response.data?.category != null) {
            Result.success(response.data!!.category!!)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun deleteCategory(id: String): Result<Unit> = try {
        val response = categoriesService.deleteCategory(id, getAuthHeader())
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

