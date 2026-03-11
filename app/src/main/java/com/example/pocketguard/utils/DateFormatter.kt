package com.example.pocketguard.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utilidad para formatear fechas de manera consistente en toda la app
 */
object DateFormatter {

    // Formatos comunes
    private val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "MX"))
    private val apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val shortFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /**
     * Convierte fecha ISO (2026-03-15) a formato de display (15 Mar 2026)
     */
    fun formatForDisplay(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate, apiFormatter)
            date.format(displayFormatter)
        } catch (e: Exception) {
            isoDate // Retornar original si falla
        }
    }

    /**
     * Convierte fecha display (15 Mar 2026) a formato API (2026-03-15)
     */
    fun formatForAPI(displayDate: String): String {
        return try {
            val date = LocalDate.parse(displayDate, displayFormatter)
            date.format(apiFormatter)
        } catch (e: Exception) {
            displayDate // Retornar original si falla
        }
    }

    /**
     * Convierte fecha corta (15/03/2026) a formato API (2026-03-15)
     */
    fun shortToAPI(shortDate: String): String {
        return try {
            val date = LocalDate.parse(shortDate, shortFormatter)
            date.format(apiFormatter)
        } catch (e: Exception) {
            shortDate
        }
    }

    /**
     * Convierte fecha API (2026-03-15) a formato corto (15/03/2026)
     */
    fun apiToShort(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate, apiFormatter)
            date.format(shortFormatter)
        } catch (e: Exception) {
            isoDate
        }
    }

    /**
     * Obtiene fecha de hoy en formato API
     */
    fun todayAPI(): String {
        return LocalDate.now().format(apiFormatter)
    }

    /**
     * Obtiene fecha de hoy en formato display
     */
    fun todayDisplay(): String {
        return LocalDate.now().format(displayFormatter)
    }
}

