package com.example.pocketguard.data.models

// Enum para validaciones de formularios
enum class ValidationError {
    EMPTY_EMAIL,
    INVALID_EMAIL,
    EMPTY_PASSWORD,
    PASSWORD_TOO_SHORT,
    PASSWORD_NO_UPPERCASE,
    PASSWORD_NO_NUMBER,
    EMPTY_FULL_NAME,
    FULL_NAME_TOO_SHORT,
    FULL_NAME_TOO_LONG,
    PASSWORDS_NOT_MATCH,
    NONE
}

// Enum para estados de solicitud HTTP
enum class RequestStatus {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

