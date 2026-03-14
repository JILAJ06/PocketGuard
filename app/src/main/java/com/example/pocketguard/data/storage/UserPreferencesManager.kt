package com.example.pocketguard.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manager para guardar preferencias del usuario localmente
 * Incluye el Monthly Income que NO se guarda en el backend
 */
class UserPreferencesManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        private val MONTHLY_INCOME_KEY = doublePreferencesKey("monthly_income")
        private const val DEFAULT_MONTHLY_INCOME = 15000.0
    }

    /**
     * Guarda el ingreso mensual del usuario localmente
     * Este dato NO se envía al backend, solo se usa para cálculos
     */
    suspend fun saveMonthlyIncome(amount: Double) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_INCOME_KEY] = amount
        }
    }

    /**
     * Obtiene el ingreso mensual guardado localmente
     * Si no existe, devuelve el valor por defecto
     */
    fun getMonthlyIncome(): Flow<Double> {
        return context.dataStore.data.map { preferences ->
            preferences[MONTHLY_INCOME_KEY] ?: DEFAULT_MONTHLY_INCOME
        }
    }

    /**
     * Obtiene el ingreso mensual de forma síncrona (suspend)
     */
    suspend fun getMonthlyIncomeValue(): Double {
        var value = DEFAULT_MONTHLY_INCOME
        context.dataStore.data.map { preferences ->
            value = preferences[MONTHLY_INCOME_KEY] ?: DEFAULT_MONTHLY_INCOME
        }.collect { }
        return value
    }

    /**
     * Limpia el ingreso mensual (útil al hacer logout)
     */
    suspend fun clearMonthlyIncome() {
        context.dataStore.edit { preferences ->
            preferences.remove(MONTHLY_INCOME_KEY)
        }
    }

    /**
     * Limpia todas las preferencias locales
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

