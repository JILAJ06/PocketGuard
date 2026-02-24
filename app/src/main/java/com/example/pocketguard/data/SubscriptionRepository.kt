package com.example.pocketguard.data // Asegúrate que el package sea correcto según tu estructura

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.util.UUID

// 1. MODELO DE DATOS (Adaptado a lo que usas en AddSubscriptionScreen)
data class Subscription(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val monthlyPrice: Double,
    val currentMonthPrice: Double, // Para cálculos anuales/mensuales
    val nextPaymentDate: LocalDate,
    val color: Color,
    val category: String,
    val icon: String = "", // Nombre del icono o identificador
    val isActive: Boolean = true
)

// 2. REPOSITORIO (Simula la base de datos)
object SubscriptionRepository {

    // Lista privada para guardar los datos en memoria
    private val _subscriptions = mutableListOf<Subscription>()

    // --- FUNCIONES CRUD (Crear, Leer, Actualizar, Borrar) ---

    // Obtener todas las suscripciones
    fun getAllSubscriptions(): List<Subscription> {
        return _subscriptions.toList()
    }

    // Obtener una suscripción por su ID (para editar)
    fun getSubscriptionById(id: String): Subscription? {
        return _subscriptions.find { it.id == id }
    }

    // Agregar una nueva suscripción
    fun addSubscription(subscription: Subscription) {
        _subscriptions.add(subscription)
    }

    // Actualizar una suscripción existente
    fun updateSubscription(updatedSubscription: Subscription) {
        val index = _subscriptions.indexOfFirst { it.id == updatedSubscription.id }
        if (index != -1) {
            _subscriptions[index] = updatedSubscription
        }
    }

    // Eliminar una suscripción
    fun deleteSubscription(id: String) {
        _subscriptions.removeIf { it.id == id }
    }

    // (Opcional) Datos de prueba iniciales para que no se vea vacío
    init {
        _subscriptions.add(
            Subscription(
                name = "Netflix",
                monthlyPrice = 199.0,
                currentMonthPrice = 199.0,
                nextPaymentDate = LocalDate.now().plusDays(5),
                color = Color(0xFFE50914),
                category = "Entretenimiento"
            )
        )
        _subscriptions.add(
            Subscription(
                name = "Spotify",
                monthlyPrice = 115.0,
                currentMonthPrice = 115.0,
                nextPaymentDate = LocalDate.now().plusDays(12),
                color = Color(0xFF1DB954),
                category = "Música"
            )
        )
    }
}