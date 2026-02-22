package com.example.pocketguard.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Clave para el token JWT
private val TOKEN_KEY = stringPreferencesKey("jwt_token")
private val USER_ID_KEY = stringPreferencesKey("user_id")

// Extension para acceder a DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pocketguard_prefs")

/**
 * Maneja el almacenamiento local de credenciales y sesión
 */
class TokenRepository(private val context: Context) {

    /**
     * Obtiene el token JWT guardado
     */
    fun getToken(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    /**
     * Guarda el token JWT
     */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    /**
     * Obtiene el ID del usuario
     */
    fun getUserId(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    /**
     * Guarda el ID del usuario
     */
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    /**
     * Limpia todas las credenciales (logout)
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

