package com.example.pocketguard.utils

/**
 * Sealed class para representar el resultado de una operación
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

