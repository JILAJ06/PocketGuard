package com.example.pocketguard.data.models

data class Bank(
    val id: String,
    val name: String,
    val is_global: Boolean,
    val user_id: String?,
    val created_at: String,
    val updated_at: String
)

data class BankData(
    val banks: List<Bank>? = null,
    val bank: Bank? = null
)

data class BankResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: BankData
)

data class CreateBankRequest(
    val name: String
)

data class UpdateBankRequest(
    val name: String? = null
)

