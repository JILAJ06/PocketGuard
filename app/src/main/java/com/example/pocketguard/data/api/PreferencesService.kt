package com.example.pocketguard.data.api

import com.example.pocketguard.data.models.*
import retrofit2.http.*

interface PreferencesService {

    @GET("preferences")
    suspend fun getPreferences(@Header("Authorization") token: String): ApiResponse<Preference>

    @PATCH("preferences")
    suspend fun updatePreferences(
        @Body request: UpdatePreferenceRequest,
        @Header("Authorization") token: String
    ): ApiResponse<Preference>
}

