package com.example.pocketguard.data.models

// Request para Login
data class LoginRequest(
    val email: String,
    val password: String
)

// Request para Registro
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)

