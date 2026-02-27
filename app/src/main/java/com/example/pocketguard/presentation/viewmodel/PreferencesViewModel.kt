package com.example.pocketguard.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.Preference
import com.example.pocketguard.data.models.UpdatePreferenceRequest
import com.example.pocketguard.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PreferencesState(
    val preferences: Preference? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isUnauthorized: Boolean = false
)

class PreferencesViewModel(private val repository: PreferencesRepository) : ViewModel() {

    private val _state = MutableStateFlow(PreferencesState())
    val state: StateFlow<PreferencesState> = _state

    fun loadPreferences() {
        viewModelScope.launch {
            Log.d("PreferencesViewModel", "loadPreferences() - Iniciando carga...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getPreferences().onSuccess { preferences ->
                Log.d("PreferencesViewModel", "loadPreferences() - Éxito: $preferences")
                _state.value = _state.value.copy(preferences = preferences, isLoading = false)
            }.onFailure { error ->
                val unauthorized = error is AuthenticationException
                Log.e("PreferencesViewModel", "loadPreferences() - Error: ${error.message}, unauthorized=$unauthorized")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar preferencias"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun updatePreferences(theme: String? = null, language: String? = null) {
        viewModelScope.launch {
            Log.d("PreferencesViewModel", "updatePreferences() - theme=$theme, language=$language")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = UpdatePreferenceRequest(theme = theme, language = language)
            Log.d("PreferencesViewModel", "updatePreferences() - Request: $request")
            repository.updatePreferences(request).onSuccess { preferences ->
                Log.d("PreferencesViewModel", "updatePreferences() - Éxito: $preferences")
                _state.value = _state.value.copy(preferences = preferences, isLoading = false)
            }.onFailure { error ->
                val unauthorized = error is AuthenticationException
                Log.e("PreferencesViewModel", "updatePreferences() - Error: ${error.message}, unauthorized=$unauthorized")
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al actualizar preferencias"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }
}

class PreferencesViewModelFactory(private val repository: PreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PreferencesViewModel(repository) as T
    }
}

