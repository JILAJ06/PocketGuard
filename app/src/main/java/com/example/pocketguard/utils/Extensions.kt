package com.example.pocketguard.utils

import kotlinx.coroutines.delay

suspend fun <T> retryIO(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    backoff: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    var exception: Exception? = null

    repeat(times) {
        try {
            return block()
        } catch (e: Exception) {
            exception = e
            delay(currentDelay)
            currentDelay = (currentDelay * backoff).toLong().coerceAtMost(maxDelay)
        }
    }
    throw exception ?: Exception("Unknown error")
}

fun String.isValidEmail(): Boolean {
    val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    return this.matches(emailRegex)
}

fun String.isStrongPassword(): Boolean {
    return this.length >= 8 &&
            this.any { it.isUpperCase() } &&
            this.any { it.isDigit() }
}

