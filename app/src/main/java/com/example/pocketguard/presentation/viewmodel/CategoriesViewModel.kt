package com.example.pocketguard.presentation.viewmodel

import android.util.Log
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
    val userCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isUnauthorized: Boolean = false
)

class CategoriesViewModel(private val repository: CategoriesRepository) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state

    fun loadCategories() {
        viewModelScope.launch {
            Log.d("CategoriesViewModel", "loadCategories() - Iniciando carga...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getAllCategories().onSuccess { categories ->
                Log.d("CategoriesViewModel", "loadCategories() - ${categories.size} categorías totales")
                val userCategories = categories.filter { it.user_id != null }
                Log.d("CategoriesViewModel", "loadCategories() - ${userCategories.size} categorías personalizadas")
                _state.value = _state.value.copy(
                    categories = categories,
                    userCategories = userCategories,
                    isLoading = false
                )
            }.onFailure { error ->
                Log.e("CategoriesViewModel", "loadCategories() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar categorías"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun createCategory(name: String, iconUrl: String?, iconName: String?, colorHex: String?) {
        viewModelScope.launch {
            Log.d("CategoriesViewModel", "createCategory() - Nombre: $name, IconName: $iconName")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = CreateCategoryRequest(
                name = name,
                icon_url = iconUrl,
                icon_name = iconName,
                color_hex = colorHex
            )
            repository.createCategory(request).onSuccess { category ->
                Log.d("CategoriesViewModel", "createCategory() - Categoría creada: ${category.id}")
                val updatedCategories = _state.value.categories + category
                val updatedUserCategories = updatedCategories.filter { it.user_id != null }
                _state.value = _state.value.copy(
                    categories = updatedCategories,
                    userCategories = updatedUserCategories,
                    isLoading = false
                )
            }.onFailure { error ->
                Log.e("CategoriesViewModel", "createCategory() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al crear categoría"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun updateCategory(id: String, name: String?, iconUrl: String?, iconName: String?, colorHex: String?) {
        viewModelScope.launch {
            Log.d("CategoriesViewModel", "updateCategory() - ID: $id, IconName: $iconName")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = UpdateCategoryRequest(
                name = name,
                icon_url = iconUrl,
                icon_name = iconName,
                color_hex = colorHex
            )
            repository.updateCategory(id, request).onSuccess { category ->
                Log.d("CategoriesViewModel", "updateCategory() - Categoría actualizada")
                val updatedCategories = _state.value.categories.map { if (it.id == id) category else it }
                val updatedUserCategories = updatedCategories.filter { it.user_id != null }
                _state.value = _state.value.copy(
                    categories = updatedCategories,
                    userCategories = updatedUserCategories,
                    isLoading = false
                )
            }.onFailure { error ->
                Log.e("CategoriesViewModel", "updateCategory() - Error: ${error.message}")
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
            Log.d("CategoriesViewModel", "deleteCategory() - ID: $id")
            repository.deleteCategory(id).onSuccess {
                Log.d("CategoriesViewModel", "deleteCategory() - Categoría eliminada")
                val updatedCategories = _state.value.categories.filter { it.id != id }
                val updatedUserCategories = updatedCategories.filter { it.user_id != null }
                _state.value = _state.value.copy(
                    categories = updatedCategories,
                    userCategories = updatedUserCategories
                )
            }.onFailure { error ->
                Log.e("CategoriesViewModel", "deleteCategory() - Error: ${error.message}")
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

