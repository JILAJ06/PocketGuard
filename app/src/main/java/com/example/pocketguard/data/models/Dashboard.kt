package com.example.pocketguard.data.models

/**
 * Modelo de datos para la gráfica de barras de tendencia mensual
 * Representa un punto (día) en la gráfica
 */
data class DailyExpensePoint(
    val dayLabel: String,      // Etiqueta eje X: "Lun", "Mar", "1", "2"...
    val totalAmount: Double,   // Dato real: $1200.00 (para mostrar al tocar)
    val barHeight: Float       // Visual: 0.0f a 1.0f (calculado respecto al día de mayor gasto)
)

/**
 * Modelo de datos para la barra de distribución de gastos
 * Muestra cómo se distribuyen los gastos entre fijos, variables y ahorros
 */
data class SpendingDistribution(
    val fixedExpenses: Double,    // Total Gastos Fijos (Suscripciones, Renta) -> Color Verde
    val variableExpenses: Double, // Total Gastos Hormiga/Variables -> Color Amarillo
    val savingsAvailable: Double, // Lo que te sobra (Ingreso - Gastos) -> Color Verde Claro
    val fixedWeight: Float,       // Proporción de gastos fijos (0.0 - 1.0)
    val variableWeight: Float,    // Proporción de gastos variables (0.0 - 1.0)
    val savingsWeight: Float      // Proporción de ahorros (0.0 - 1.0)
)

/**
 * Modelo de datos para los insights financieros
 * Información sobre gastos hormiga y potencial de ahorro
 */
data class FinancialInsights(
    val antExpensesTotal: Double,     // Ej: $450.00 - Total de gastos hormiga
    val antExpensesCount: Int,        // Ej: 12 compras menores a $100
    val savingsPotentialPercent: Int  // Ej: 15% (calculado sobre el ingreso)
)

/**
 * Información del período del dashboard
 */
data class DashboardPeriod(
    val month: Int,
    val year: Int,
    val periodType: String  // "month" o "week"
)

/**
 * Estado completo del Dashboard para HomeScreen
 * Este es el modelo maestro que contiene toda la información necesaria
 */
data class HomeDashboardState(
    val totalBalance: Double,
    val monthlyTrend: List<DailyExpensePoint>, // Para la gráfica de barras
    val distribution: SpendingDistribution,    // Para la barra de colores
    val insights: FinancialInsights,           // Para los cuadros de texto de abajo
    val period: DashboardPeriod,               // Información del período
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Response del API para el Dashboard
 */
data class DashboardResponse(
    val success: Boolean,
    val data: DashboardData,
    val message: String?
)

data class DashboardData(
    val dashboard: DashboardInfo
)

data class DashboardInfo(
    val totalBalance: Double,
    val monthlyTrend: List<DailyExpensePoint>,
    val distribution: SpendingDistribution,
    val insights: FinancialInsights,
    val period: DashboardPeriod
)

