package com.example.pocketguard.data.models

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("category_id")
    val id: String,
    val name: String,
    val icon_url: String?,
    val icon_name: String?, // Nombre del icono Material (ej: "restaurant", "car")
    val color_hex: String?,
    val is_global: Boolean,
    val user_id: String?,
    val created_at: String,
    val updated_at: String
)

data class CategoryData(
    val categories: List<Category>? = null,
    val category: Category? = null
)

data class CategoryResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: CategoryData
)

data class CreateCategoryRequest(
    val name: String,
    val icon_url: String? = null,
    val icon_name: String? = null, // Nombre del icono Material
    val color_hex: String? = null
)

data class UpdateCategoryRequest(
    val name: String? = null,
    val icon_url: String? = null,
    val icon_name: String? = null, // Nombre del icono Material
    val color_hex: String? = null
)

