package com.example.pocketguard.data.models

data class Card(
    val card_id: String,
    val user_id: String,
    val bank_id: String? = null,
    val bank_name: String,
    val alias: String,
    val last_4_digits: String?,
    val color_hex: String?,
    val is_default: Boolean,
    val created_at: String? = null,
    val updated_at: String
)

data class CardData(
    val cards: List<Card>? = null,
    val card: Card? = null
)

data class CardResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: CardData
)

data class CreateCardRequest(
    val bank_name: String,
    val alias: String,
    val last_4_digits: String? = null,
    val color_hex: String? = null,
    val is_default: Boolean? = null
)

data class UpdateCardRequest(
    val bank_name: String? = null,
    val alias: String? = null,
    val last_4_digits: String? = null,
    val color_hex: String? = null,
    val is_default: Boolean? = null
)

