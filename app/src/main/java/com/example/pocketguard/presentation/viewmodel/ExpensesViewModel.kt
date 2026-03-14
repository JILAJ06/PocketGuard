package com.example.pocketguard.presentation.viewmodel

import android.util.Log
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
            Log.d("ExpensesViewModel", "loadExpenses() - Iniciando carga...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getAllExpenses().onSuccess { expenses ->
                Log.d("ExpensesViewModel", "loadExpenses() - ${expenses.size} gastos")
                _state.value = _state.value.copy(
                    expenses = expenses,
                    isLoading = false
                )
            }.onFailure { error ->
                Log.e("ExpensesViewModel", "loadExpenses() - Error: ${error.message}")
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
            Log.d("ExpensesViewModel", "deleteExpense() - ID: $id")
            repository.deleteExpense(id).onSuccess {
                _state.value = _state.value.copy(
                    expenses = _state.value.expenses.filter { it.id != id }
                )
            }.onFailure { error ->
                Log.e("ExpensesViewModel", "deleteExpense() - Error: ${error.message}")
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
            Log.d("ExpensesViewModel", "createExpense() - Nombre: $name, Monto: $amount")
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
                Log.e("ExpensesViewModel", "createExpense() - Error: ${error.message}")
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al crear gasto"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun updateExpense(id: String, name: String?, amount: Double?, expenseDate: String?, categoryId: String?) {
        viewModelScope.launch {
            Log.d("ExpensesViewModel", "updateExpense() - ID: $id")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = com.example.pocketguard.data.models.UpdateExpenseRequest(
                name = name,
                amount = amount,
                expense_date = expenseDate,
                category_id = categoryId
            )
            repository.updateExpense(id, request).onSuccess { expense ->
                Log.d("ExpensesViewModel", "updateExpense() - Gasto actualizado")
                _state.value = _state.value.copy(
                    expenses = _state.value.expenses.map { if (it.id == id) expense else it },
                    isLoading = false
                )
            }.onFailure { error ->
                Log.e("ExpensesViewModel", "updateExpense() - Error: ${error.message}")
                val unauthorized = error is com.example.pocketguard.data.exceptions.AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al actualizar gasto"),
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
