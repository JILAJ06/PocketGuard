package com.example.pocketguard.data.repository

import com.example.pocketguard.data.models.HomeDashboardState
import com.example.pocketguard.data.remote.DashboardService
import com.example.pocketguard.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para manejar las operaciones del Dashboard
 */
class DashboardRepository(private val dashboardService: DashboardService) {

    /**
     * Obtiene los datos del dashboard
     *
     * @param monthlyIncome Ingreso mensual (guardado localmente, NO en DB)
     * @param month Mes específico (opcional)
     * @param year Año específico (opcional)
     * @param period Tipo de período: "month" o "week"
     */
    suspend fun getDashboard(
        monthlyIncome: Double,
        month: Int? = null,
        year: Int? = null,
        period: String = "month"
    ): Result<HomeDashboardState> = withContext(Dispatchers.IO) {
        try {
            val response = dashboardService.getDashboard(
                monthlyIncome = monthlyIncome,
                month = month,
                year = year,
                period = period
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val dashboard = body.data.dashboard
                    Result.Success(
                        HomeDashboardState(
                            totalBalance = dashboard.totalBalance,
                            monthlyTrend = dashboard.monthlyTrend,
                            distribution = dashboard.distribution,
                            insights = dashboard.insights,
                            period = dashboard.period,
                            isLoading = false,
                            error = null
                        )
                    )
                } else {
                    Result.Error(body?.message ?: "Error al obtener dashboard")
                }
            } else {
                when (response.code()) {
                    400 -> Result.Error("El ingreso mensual es requerido")
                    401 -> Result.Error("UNAUTHORIZED")
                    else -> Result.Error("Error del servidor: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error de conexión")
        }
    }
}

