package com.example.pocketguard.utils

import java.util.Calendar
import java.util.Locale

object DateUtils {
    // Normalize a date (year, month (1-12), day) to epoch millis at local noon to avoid timezone off-by-one
    fun localDateToEpochMillis(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        // month in Calendar is 0-based
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        // set to noon to avoid timezone shifting the day
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun epochMillisToLocalDateParts(epochMillis: Long): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        return Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    // Produce ISO date string yyyy-MM-dd from year/month/day (month 1-12)
    fun localDateToIsoDate(year: Int, month: Int, day: Int): String {
        // Zero-pad month and day
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }
}
