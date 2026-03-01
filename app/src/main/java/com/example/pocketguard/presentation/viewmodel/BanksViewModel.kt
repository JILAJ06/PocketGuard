package com.example.pocketguard.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.Bank
import com.example.pocketguard.data.models.CreateBankRequest
import com.example.pocketguard.data.models.UpdateBankRequest
import com.example.pocketguard.data.repository.BanksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BanksState(
    val banks: List<Bank> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isUnauthorized: Boolean = false
)

class BanksViewModel(private val repository: BanksRepository) : ViewModel() {

    private val _state = MutableStateFlow(BanksState())
    val state: StateFlow<BanksState> = _state

    fun loadBanks() {
        viewModelScope.launch {
            Log.d("BanksViewModel", "loadBanks() - Iniciando carga...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getAllBanks().onSuccess { banks ->
                Log.d("BanksViewModel", "loadBanks() - ${banks.size} bancos cargados")
                _state.value = _state.value.copy(banks = banks, isLoading = false)
            }.onFailure { error ->
                Log.e("BanksViewModel", "loadBanks() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar bancos"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun createBank(name: String) {
        viewModelScope.launch {
            Log.d("BanksViewModel", "createBank() - Nombre: $name")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = CreateBankRequest(name = name)
            repository.createBank(request).onSuccess { bank ->
                _state.value = _state.value.copy(banks = _state.value.banks + bank, isLoading = false)
            }.onFailure { error ->
                Log.e("BanksViewModel", "createBank() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al crear banco"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun updateBank(id: String, name: String?) {
        viewModelScope.launch {
            Log.d("BanksViewModel", "updateBank() - ID: $id, Nombre: $name")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = UpdateBankRequest(name = name)
            repository.updateBank(id, request).onSuccess { bank ->
                _state.value = _state.value.copy(
                    banks = _state.value.banks.map { if (it.id == id) bank else it },
                    isLoading = false
                )
            }.onFailure { error ->
                Log.e("BanksViewModel", "updateBank() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al actualizar banco"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun deleteBank(id: String) {
        viewModelScope.launch {
            Log.d("BanksViewModel", "deleteBank() - ID: $id")
            repository.deleteBank(id).onSuccess {
                _state.value = _state.value.copy(banks = _state.value.banks.filter { it.id != id })
            }.onFailure { error ->
                Log.e("BanksViewModel", "deleteBank() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al eliminar banco"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }
}

class BanksViewModelFactory(private val repository: BanksRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BanksViewModel(repository) as T
    }
}
