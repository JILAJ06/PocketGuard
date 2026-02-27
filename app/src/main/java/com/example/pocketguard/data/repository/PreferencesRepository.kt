package com.example.pocketguard.data.repository

import android.content.Context
import android.util.Log
import com.example.pocketguard.data.api.PreferencesService
import com.example.pocketguard.data.api.RetrofitClient
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.Preference
import com.example.pocketguard.data.models.UpdatePreferenceRequest
import com.example.pocketguard.data.storage.TokenManager
import retrofit2.HttpException

class PreferencesRepository(private val context: Context) {

    private val preferencesService: PreferencesService = RetrofitClient.getPreferencesService(context)
    private val tokenManager = TokenManager(context)

    private fun getAuthHeader(): String {
        val token = tokenManager.getAccessToken() ?: ""
        return "Bearer $token"
    }

    suspend fun getPreferences(): Result<Preference> = try {
        Log.d("PreferencesRepository", "getPreferences() - Iniciando petición...")
        val response = preferencesService.getPreferences(getAuthHeader())
        Log.d("PreferencesRepository", "getPreferences() - Respuesta recibida: success=${response.success}, data=${response.data}")
        if (response.success && response.data != null) {
            Result.success(response.data!!)
        } else {
            Log.e("PreferencesRepository", "getPreferences() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("PreferencesRepository", "getPreferences() - Excepción: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }

    suspend fun updatePreferences(request: UpdatePreferenceRequest): Result<Preference> = try {
        Log.d("PreferencesRepository", "updatePreferences() - Iniciando petición con: $request")
        val response = preferencesService.updatePreferences(request, getAuthHeader())
        Log.d("PreferencesRepository", "updatePreferences() - Respuesta recibida: success=${response.success}, data=${response.data}")
        if (response.success && response.data != null) {
            Result.success(response.data!!)
        } else {
            Log.e("PreferencesRepository", "updatePreferences() - Error: ${response.message}")
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Log.e("PreferencesRepository", "updatePreferences() - Excepción: ${e.message}", e)
        val mapped = if (e is HttpException && e.code() == 401) AuthenticationException() else e
        Result.failure(mapped)
    }
}

