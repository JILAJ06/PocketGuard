package com.example.pocketguard.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import com.example.pocketguard.screens.Subscription // Importa tu modelo de datos
import java.time.LocalDate

object SubscriptionRepository {
    // Lista mutable que notifica cambios a la UI automáticamente
    private val _subscriptions = mutableStateListOf(
        Subscription("1", "Netflix", "Entretenimiento", 199.0, 2388.0, LocalDate.of(2026, 2, 12), true, Color(0xFFE50914), "🎬"),
        Subscription("2", "Spotify", "Música", 115.0, 1380.0, LocalDate.of(2026, 2, 12), true, Color(0xFF1DB954), "🎵"),
        Subscription("3", "Amazon Prime", "Compras", 99.0, 1188.0, LocalDate.of(2026, 2, 15), true, Color(0xFFFF9900), "📦"),
        Subscription("4", "HBO Max", "Streaming", 149.0, 1788.0, LocalDate.of(2026, 2, 9), true, Color(0xFF9146FF), "📺")
    )

    val subscriptions: List<Subscription> get() = _subscriptions

    fun getSubscription(id: String): Subscription? {
        return _subscriptions.find { it.id == id }
    }

    fun addSubscription(subscription: Subscription) {
        _subscriptions.add(subscription)
    }

    fun updateSubscription(updatedSubscription: Subscription) {
        val index = _subscriptions.indexOfFirst { it.id == updatedSubscription.id }
        if (index != -1) {
            _subscriptions[index] = updatedSubscription
        }
    }

    fun deleteSubscription(id: String) {
        _subscriptions.removeAll { it.id == id }
    }
}