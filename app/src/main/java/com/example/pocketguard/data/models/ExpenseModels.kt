package com.example.pocketguard.data.models

data class Expense(
    val id: String,
    val userId: String,
    val categoryId: String,
    val categoryName: String,
    val categoryColor: String,
    val name: String,
    val amount: Double,
    val expenseDate: String,
    val createdAt: String
)

data class CreateExpenseRequest(
    val name: String,
    val amount: Double,
    val expense_date: String,
    val category_id: String
)

data class UpdateExpenseRequest(
    val name: String? = null,
    val amount: Double? = null,
    val expense_date: String? = null,
    val category_id: String? = null
)

data class ExpenseResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: ExpenseData
)

data class ExpenseData(
    val expenses: List<Expense>? = null,
    val expense: Expense? = null
)

data class ExpenseSummary(
    val total: Double,
    val count: Int,
    val byCategory: List<CategorySummary>,
    val period: PeriodInfo
)

data class CategorySummary(
    val categoryId: String,
    val categoryName: String,
    val total: Double
)

data class PeriodInfo(
    val month: Int,
    val year: Int
)

data class ExpenseSummaryResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: ExpenseSummaryData
)

data class ExpenseSummaryData(
    val summary: ExpenseSummary
)

