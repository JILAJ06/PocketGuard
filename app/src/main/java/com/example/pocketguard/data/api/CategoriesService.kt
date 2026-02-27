package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface CategoriesService {

    @GET("categories")
    suspend fun getAllCategories(@Header("Authorization") token: String): ApiResponse<CategoryData>

    @GET("categories/global")
    suspend fun getGlobalCategories(@Header("Authorization") token: String): ApiResponse<CategoryData>

    @GET("categories/{id}")
    suspend fun getCategoryById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<CategoryData>

    @POST("categories")
    suspend fun createCategory(
        @Body request: CreateCategoryRequest,
        @Header("Authorization") token: String
    ): ApiResponse<CategoryData>

    @PATCH("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: UpdateCategoryRequest,
        @Header("Authorization") token: String
    ): ApiResponse<CategoryData>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): ApiResponse<Map<String, String>>
}

