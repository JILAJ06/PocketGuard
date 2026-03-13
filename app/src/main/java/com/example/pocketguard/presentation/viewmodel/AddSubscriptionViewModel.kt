package com.example.pocketguard.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.models.BillingCycle
import com.example.pocketguard.data.models.CreateSubscriptionRequest
import com.example.pocketguard.data.models.Subscription
import com.example.pocketguard.data.models.UpdateSubscriptionRequest
import com.example.pocketguard.data.repository.SubscriptionsRepository
import com.example.pocketguard.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddSubscriptionScreenState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isSuccess: Boolean = false,
    val billingCycles: List<BillingCycle> = emptyList(),
    val subscription: Subscription? = null,
    val isUnauthorized: Boolean = false
)

class AddSubscriptionViewModel(private val repository: SubscriptionsRepository) : ViewModel() {

    private val _state = MutableStateFlow(AddSubscriptionScreenState())
    val state: StateFlow<AddSubscriptionScreenState> = _state

    init {
        loadBillingCycles()
    }

    private fun loadBillingCycles() {
        viewModelScope.launch {
            repository.getBillingCycles().onSuccess { cycles ->
                _state.value = _state.value.copy(billingCycles = cycles)
            }.onFailure { error ->
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar ciclos"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun loadSubscription(id: String) {
        if (id.isEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getSubscriptionById(id).onSuccess { subscription ->
                _state.value = _state.value.copy(
                    subscription = subscription,
                    isLoading = false
                )
            }.onFailure { error ->
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar suscripción"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun createSubscription(
        serviceName: String,
        amount: Double,
        year: Int,
        month: Int,
        day: Int,
        billingCycleId: Int,
        categoryId: String,
        cardId: String?,
        cards: List<com.example.pocketguard.data.models.Card> = emptyList()
    ) {
        viewModelScope.launch {
            val finalCardId = cardId ?: cards.firstOrNull { it.is_default }?.card_id
            Log.d("AddSubscriptionViewModel", "createSubscription() - Servicio: $serviceName, CardId original: $cardId, CardId final: $finalCardId")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            // Normalize date to ISO date string to prevent timezone shifting the date back by one
            val isoDate = com.example.pocketguard.utils.DateUtils.localDateToIsoDate(year, month, day)
            val request = CreateSubscriptionRequest(
                service_name = serviceName,
                amount = amount,
                next_payment_date = isoDate,
                billing_cycle_id = billingCycleId,
                category_id = categoryId,
                used_card_id = finalCardId
            )
            repository.createSubscription(request).onSuccess { subscription ->
                Log.d("AddSubscriptionViewModel", "createSubscription() - Suscripción creada exitosamente")
                _state.value = _state.value.copy(
                    subscription = subscription,
                    isLoading = false,
                    isSuccess = true
                )
            }.onFailure { error ->
                Log.e("AddSubscriptionViewModel", "createSubscription() - Error: ${error.message}")
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al crear suscripción"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    // Backwards-compatible overload: accept nextPaymentDate as String (ISO date) for existing UI callers
    fun createSubscription(
        serviceName: String,
        amount: Double,
        nextPaymentDate: String,
        billingCycleId: Int,
        categoryId: String,
        cardId: String?,
        cards: List<com.example.pocketguard.data.models.Card> = emptyList()
    ) {
        viewModelScope.launch {
            val finalCardId = cardId ?: cards.firstOrNull { it.is_default }?.card_id
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = CreateSubscriptionRequest(
                service_name = serviceName,
                amount = amount,
                next_payment_date = nextPaymentDate,
                billing_cycle_id = billingCycleId,
                category_id = categoryId,
                used_card_id = finalCardId
            )
            repository.createSubscription(request).onSuccess { subscription ->
                _state.value = _state.value.copy(subscription = subscription, isLoading = false, isSuccess = true)
            }.onFailure { error ->
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al crear suscripción"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun updateSubscription(
        id: String,
        serviceName: String?,
        amount: Double?,
        nextPaymentDate: String?,
        billingCycleId: Int?,
        categoryId: String?,
        cardId: String?,
        keepCurrentCard: Boolean = false
    ) {
        viewModelScope.launch {
            val finalCardId: String? = if (keepCurrentCard) {
                null
            } else {
                cardId
            }
            Log.d("AddSubscriptionViewModel", "updateSubscription() - ID: $id, CardId: $finalCardId, keepCurrent: $keepCurrentCard")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = UpdateSubscriptionRequest(
                service_name = serviceName,
                amount = amount,
                next_payment_date = nextPaymentDate,
                billing_cycle_id = billingCycleId,
                category_id = categoryId,
                used_card_id = finalCardId
            )
            repository.updateSubscription(id, request).onSuccess { subscription ->
                _state.value = _state.value.copy(
                    subscription = subscription,
                    isLoading = false,
                    isSuccess = true
                )
            }.onFailure { error ->
                Log.e("AddSubscriptionViewModel", "updateSubscription() - Error: ${error.message}")
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al actualizar suscripción"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(isSuccess = false)
    }
}

class AddSubscriptionViewModelFactory(private val repository: SubscriptionsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddSubscriptionViewModel(repository) as T
    }
}
