package com.example.pocketguard.data.model

import com.google.gson.annotations.SerializedName

// --- REQUEST MODELS ---

data class RegisterRequest(
    @SerializedName("fullName")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

// --- RESPONSE MODELS ---

data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: AuthData? = null
)

data class AuthData(
    @SerializedName("user")
    val user: User,
    @SerializedName("tokens")
    val tokens: Tokens
)

data class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class Tokens(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class UserResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: User? = null,
    @SerializedName("message")
    val message: String? = null
)

data class LogoutResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

// --- ERROR MODEL ---

data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String,
    @SerializedName("errors")
    val errors: List<String>? = null
)

