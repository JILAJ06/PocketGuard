package com.example.pocketguard.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val POCKETGUARD_STORE_NAME = "pocketguard_store"
private val Context.dataStore by preferencesDataStore(name = POCKETGUARD_STORE_NAME)

object TokenStoreKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val USER_ID = stringPreferencesKey("user_id")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_NAME = stringPreferencesKey("user_name")
    val AUTH_PROVIDER = stringPreferencesKey("auth_provider")
}

class TokenStore(private val context: Context) {

    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TokenStoreKeys.ACCESS_TOKEN]
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TokenStoreKeys.USER_ID]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TokenStoreKeys.USER_EMAIL]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TokenStoreKeys.USER_NAME]
    }

    val authProvider: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TokenStoreKeys.AUTH_PROVIDER]
    }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TokenStoreKeys.ACCESS_TOKEN] = token
        }
    }

    suspend fun saveUser(userId: String, email: String, name: String?, provider: String) {
        context.dataStore.edit { preferences ->
            preferences[TokenStoreKeys.USER_ID] = userId
            preferences[TokenStoreKeys.USER_EMAIL] = email
            preferences[TokenStoreKeys.USER_NAME] = name ?: ""
            preferences[TokenStoreKeys.AUTH_PROVIDER] = provider
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun getAccessTokenSync(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[TokenStoreKeys.ACCESS_TOKEN] }
            .first()
    }
}
