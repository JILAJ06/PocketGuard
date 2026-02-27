package com.example.pocketguard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.models.Expense
import com.example.pocketguard.data.repository.ExpensesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ExpensesScreenState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val selectedFilter: String = "Todas",
    val isUnauthorized: Boolean = false
)

class ExpensesViewModel(private val repository: ExpensesRepository) : ViewModel() {

    private val _state = MutableStateFlow(ExpensesScreenState())
    val state: StateFlow<ExpensesScreenState> = _state

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getAllExpenses().onSuccess { expenses ->
                _state.value = _state.value.copy(
                    expenses = expenses,
                    isLoading = false
                )
            }.onFailure { error ->
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar gastos"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpense(id).onSuccess {
                _state.value = _state.value.copy(
                    expenses = _state.value.expenses.filter { it.id != id }
                )
            }.onFailure { error ->
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al eliminar gasto"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun setFilter(filter: String) {
        _state.value = _state.value.copy(selectedFilter = filter)
    }

    fun createExpense(name: String, amount: Double, expenseDate: String, categoryId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = com.example.pocketguard.data.models.CreateExpenseRequest(
                name = name,
                amount = amount,
                expense_date = expenseDate,
                category_id = categoryId
            )
            repository.createExpense(request).onSuccess { expense ->
                _state.value = _state.value.copy(
                    expenses = listOf(expense) + _state.value.expenses,
                    isLoading = false
                )
            }.onFailure { error ->
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al crear gasto"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }
}

class ExpensesViewModelFactory(private val repository: ExpensesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpensesViewModel(repository) as T
    }
}
