package com.example.pocketguard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.models.HomeDashboardState
import com.example.pocketguard.data.models.DailyExpensePoint
import com.example.pocketguard.data.models.SpendingDistribution
import com.example.pocketguard.data.models.FinancialInsights
import com.example.pocketguard.data.models.DashboardPeriod
import com.example.pocketguard.data.repository.DashboardRepository
import com.example.pocketguard.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el Dashboard de HomeScreen
 */
class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeDashboardState(
            totalBalance = 0.0,
            monthlyTrend = emptyList(),
            distribution = SpendingDistribution(0.0, 0.0, 0.0, 0f, 0f, 0f),
            insights = FinancialInsights(0.0, 0, 0),
            period = DashboardPeriod(1, 2026, "month"),
            isLoading = false,
            error = null
        )
    )
    val state: StateFlow<HomeDashboardState> = _state.asStateFlow()

    private val _isUnauthorized = MutableStateFlow(false)
    val isUnauthorized: StateFlow<Boolean> = _isUnauthorized.asStateFlow()

    /**
     * Carga los datos del dashboard
     *
     * @param monthlyIncome Ingreso mensual del usuario (guardado localmente)
     * @param month Mes específico (opcional)
     * @param year Año específico (opcional)
     * @param period Tipo de período: "month" o "week"
     */
    fun loadDashboard(
        monthlyIncome: Double,
        month: Int? = null,
        year: Int? = null,
        period: String = "month"
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = repository.getDashboard(monthlyIncome, month, year, period)) {
                is Result.Success -> {
                    _state.value = result.data.copy(isLoading = false)
                }
                is Result.Error -> {
                    if (result.message == "UNAUTHORIZED") {
                        _isUnauthorized.value = true
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Limpia el error
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /**
     * Resetea el estado de unauthorized
     */
    fun resetUnauthorized() {
        _isUnauthorized.value = false
    }
}

/**
 * Factory para crear instancias de DashboardViewModel
 */
class DashboardViewModelFactory(
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

