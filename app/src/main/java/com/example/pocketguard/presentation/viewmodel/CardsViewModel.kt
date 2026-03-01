package com.example.pocketguard.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.Card
import com.example.pocketguard.data.models.CreateCardRequest
import com.example.pocketguard.data.models.UpdateCardRequest
import com.example.pocketguard.data.repository.CardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CardsState(
    val cards: List<Card> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isUnauthorized: Boolean = false
)

class CardsViewModel(private val repository: CardsRepository) : ViewModel() {

    private val _state = MutableStateFlow(CardsState())
    val state: StateFlow<CardsState> = _state

    fun loadCards() {
        viewModelScope.launch {
            Log.d("CardsViewModel", "loadCards() - Iniciando carga...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getAllCards().onSuccess { cards ->
                Log.d("CardsViewModel", "loadCards() - Éxito: ${cards.size} tarjetas")
                _state.value = _state.value.copy(cards = cards, isLoading = false)
            }.onFailure { error ->
                Log.e("CardsViewModel", "loadCards() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar tarjetas"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun createCard(bankName: String, alias: String, last4: String?, colorHex: String?, isDefault: Boolean?) {
        viewModelScope.launch {
            Log.d("CardsViewModel", "createCard() - Alias: $alias")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = CreateCardRequest(
                bank_name = bankName,
                alias = alias,
                last_4_digits = last4,
                color_hex = colorHex,
                is_default = isDefault
            )
            repository.createCard(request).onSuccess { card ->
                Log.d("CardsViewModel", "createCard() - Tarjeta creada: ${card.card_id}")
                _state.value = _state.value.copy(cards = _state.value.cards + card, isLoading = false)
            }.onFailure { error ->
                Log.e("CardsViewModel", "createCard() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al crear tarjeta"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun updateCard(id: String, bankName: String?, alias: String?, last4: String?, colorHex: String?, isDefault: Boolean?) {
        viewModelScope.launch {
            Log.d("CardsViewModel", "updateCard() - ID: $id")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = UpdateCardRequest(
                bank_name = bankName,
                alias = alias,
                last_4_digits = last4,
                color_hex = colorHex,
                is_default = isDefault
            )
            repository.updateCard(id, request).onSuccess { card ->
                Log.d("CardsViewModel", "updateCard() - Tarjeta actualizada")
                _state.value = _state.value.copy(cards = _state.value.cards.map { if (it.card_id == id) card else it }, isLoading = false)
            }.onFailure { error ->
                Log.e("CardsViewModel", "updateCard() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al actualizar tarjeta"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun deleteCard(id: String) {
        viewModelScope.launch {
            Log.d("CardsViewModel", "deleteCard() - ID: $id")
            repository.deleteCard(id).onSuccess {
                Log.d("CardsViewModel", "deleteCard() - Tarjeta eliminada exitosamente")
                _state.value = _state.value.copy(cards = _state.value.cards.filter { it.card_id != id })
            }.onFailure { error ->
                Log.e("CardsViewModel", "deleteCard() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al eliminar tarjeta"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun setDefaultCard(id: String) {
        viewModelScope.launch {
            Log.d("CardsViewModel", "setDefaultCard() - ID: $id")
            repository.setDefaultCard(id).onSuccess { card ->
                Log.d("CardsViewModel", "setDefaultCard() - Tarjeta predeterminada actualizada")
                _state.value = _state.value.copy(cards = _state.value.cards.map { if (it.card_id == id) card else it })
            }.onFailure { error ->
                Log.e("CardsViewModel", "setDefaultCard() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al actualizar tarjeta"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }
}

class CardsViewModelFactory(private val repository: CardsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardsViewModel(repository) as T
    }
}

