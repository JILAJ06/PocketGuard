package com.example.pocketguard.data.models

import com.google.gson.annotations.SerializedName

data class Preference(
    val theme: String,
    val language: String
)

data class PreferenceData(
    val preferences: Preference? = null
)

data class PreferenceResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: Preference? = null
)

data class UpdatePreferenceRequest(
    val theme: String? = null,
    val language: String? = null
)

