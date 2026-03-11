package com.example.pocketguard.data.remote

import com.example.pocketguard.data.models.DashboardResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Servicio API para el Dashboard
 * Endpoint: GET /api/v1/dashboard
 */
interface DashboardService {

    /**
     * Obtiene los datos del dashboard
     *
     * @param monthlyIncome Ingreso mensual del usuario (REQUERIDO, NO se guarda en DB)
     * @param month Mes específico (1-12), default: mes actual
     * @param year Año específico, default: año actual
     * @param period Tipo de período: "month" o "week", default: "month"
     * @return DashboardResponse con toda la información para las gráficas
     */
    @GET("/api/v1/dashboard")
    suspend fun getDashboard(
        @Query("monthly_income") monthlyIncome: Double,
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null,
        @Query("period") period: String? = null
    ): Response<DashboardResponse>
}

