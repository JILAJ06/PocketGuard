package com.example.pocketguard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.Category
import com.example.pocketguard.data.models.CreateCategoryRequest
import com.example.pocketguard.data.models.UpdateCategoryRequest
import com.example.pocketguard.data.repository.CategoriesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoriesState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isUnauthorized: Boolean = false
)

class CategoriesViewModel(private val repository: CategoriesRepository) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state

    fun loadCategories() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getAllCategories().onSuccess { categories ->
                _state.value = _state.value.copy(categories = categories, isLoading = false)
            }.onFailure { error ->
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar categorías"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun createCategory(name: String, iconUrl: String?, colorHex: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = CreateCategoryRequest(name = name, icon_url = iconUrl, color_hex = colorHex)
            repository.createCategory(request).onSuccess { category ->
                _state.value = _state.value.copy(
                    categories = _state.value.categories + category,
                    isLoading = false
                )
            }.onFailure { error ->
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al crear categoría"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun updateCategory(id: String, name: String?, iconUrl: String?, colorHex: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = UpdateCategoryRequest(name = name, icon_url = iconUrl, color_hex = colorHex)
            repository.updateCategory(id, request).onSuccess { category ->
                _state.value = _state.value.copy(
                    categories = _state.value.categories.map { if (it.id == id) category else it },
                    isLoading = false
                )
            }.onFailure { error ->
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al actualizar categoría"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id).onSuccess {
                _state.value = _state.value.copy(categories = _state.value.categories.filter { it.id != id })
            }.onFailure { error ->
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al eliminar categoría"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }
}

class CategoriesViewModelFactory(private val repository: CategoriesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoriesViewModel(repository) as T
    }
}

