package com.example.pocketguard.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.models.Subscription
import com.example.pocketguard.data.repository.SubscriptionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SubscriptionsScreenState(
    val subscriptions: List<Subscription> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val totalMonthly: Double = 0.0,
    val isUnauthorized: Boolean = false
)

class SubscriptionsViewModel(private val repository: SubscriptionsRepository) : ViewModel() {

    private val _state = MutableStateFlow(SubscriptionsScreenState())
    val state: StateFlow<SubscriptionsScreenState> = _state

    init {
        loadSubscriptions()
    }

    fun loadSubscriptions() {
        viewModelScope.launch {
            Log.d("SubscriptionsViewModel", "loadSubscriptions() - Iniciando carga...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getAllSubscriptions().onSuccess { subscriptions ->
                Log.d("SubscriptionsViewModel", "loadSubscriptions() - ${subscriptions.size} suscripciones")
                val totalMonthly = calculateTotalMonthly(subscriptions)
                _state.value = _state.value.copy(
                    subscriptions = subscriptions,
                    isLoading = false,
                    totalMonthly = totalMonthly
                )
            }.onFailure { error ->
                Log.e("SubscriptionsViewModel", "loadSubscriptions() - Error: ${error.message}")
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar suscripciones"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun deleteSubscription(id: String) {
        viewModelScope.launch {
            Log.d("SubscriptionsViewModel", "deleteSubscription() - ID: $id")
            repository.deleteSubscription(id).onSuccess {
                _state.value = _state.value.copy(
                    subscriptions = _state.value.subscriptions.filter { it.subscription_id != id }
                )
                val totalMonthly = calculateTotalMonthly(_state.value.subscriptions)
                _state.value = _state.value.copy(totalMonthly = totalMonthly)
            }.onFailure { error ->
                Log.e("SubscriptionsViewModel", "deleteSubscription() - Error: ${error.message}")
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al eliminar suscripción"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun createSubscription(
        serviceName: String,
        amount: Double,
        nextPaymentDate: String,
        billingCycleId: Int,
        categoryId: String,
        cardId: String?
    ) {
        viewModelScope.launch {
            Log.d("SubscriptionsViewModel", "createSubscription() - Servicio: $serviceName, CardId: $cardId")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = com.example.pocketguard.data.models.CreateSubscriptionRequest(
                service_name = serviceName,
                amount = amount,
                next_payment_date = nextPaymentDate,
                billing_cycle_id = billingCycleId,
                category_id = categoryId,
                used_card_id = cardId
            )
            repository.createSubscription(request).onSuccess { subscription ->
                _state.value = _state.value.copy(
                    subscriptions = _state.value.subscriptions + subscription,
                    isLoading = false
                )
                val totalMonthly = calculateTotalMonthly(_state.value.subscriptions)
                _state.value = _state.value.copy(totalMonthly = totalMonthly)
            }.onFailure { error ->
                Log.e("SubscriptionsViewModel", "createSubscription() - Error: ${error.message}")
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
        subscriptionId: String,
        serviceName: String? = null,
        amount: Double? = null,
        nextPaymentDate: String? = null,
        billingCycleId: Int? = null,
        categoryId: String? = null,
        cardId: String? = null
    ) {
        viewModelScope.launch {
            Log.d("SubscriptionsViewModel", "updateSubscription() - ID: $subscriptionId")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = com.example.pocketguard.data.models.UpdateSubscriptionRequest(
                service_name = serviceName,
                amount = amount,
                next_payment_date = nextPaymentDate,
                billing_cycle_id = billingCycleId,
                category_id = categoryId,
                used_card_id = cardId
            )
            repository.updateSubscription(subscriptionId, request).onSuccess {
                loadSubscriptions()
            }.onFailure { error ->
                Log.e("SubscriptionsViewModel", "updateSubscription() - Error: ${error.message}")
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al actualizar suscripción"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    private fun calculateTotalMonthly(subscriptions: List<Subscription>): Double {
        return subscriptions.sumOf { sub ->
            when (sub.billing_cycle) {
                "Daily" -> sub.amount * 30
                "Weekly" -> sub.amount * 4.33
                "Yearly" -> sub.amount / 12
                else -> sub.amount
            }
        }
    }
}

class SubscriptionsViewModelFactory(private val repository: SubscriptionsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SubscriptionsViewModel(repository) as T
    }
}
