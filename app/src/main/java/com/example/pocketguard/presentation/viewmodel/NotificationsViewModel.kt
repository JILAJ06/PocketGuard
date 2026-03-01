package com.example.pocketguard.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketguard.data.exceptions.AuthenticationException
import com.example.pocketguard.data.models.NotificationSettings
import com.example.pocketguard.data.models.UpdateNotificationSettingsRequest
import com.example.pocketguard.data.repository.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificationsState(
    val notifications: List<com.example.pocketguard.data.models.Notification> = emptyList(),
    val settings: NotificationSettings? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isUnauthorized: Boolean = false
)

class NotificationsViewModel(private val repository: NotificationsRepository) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state

    fun loadNotifications() {
        viewModelScope.launch {
            Log.d("NotificationsViewModel", "loadNotifications() - Iniciando carga...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getNotifications().onSuccess { notifications ->
                Log.d("NotificationsViewModel", "loadNotifications() - ${notifications.size} notificaciones cargadas")
                _state.value = _state.value.copy(notifications = notifications, isLoading = false)
            }.onFailure { error ->
                Log.e("NotificationsViewModel", "loadNotifications() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar notificaciones"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            Log.d("NotificationsViewModel", "markAsRead() - Marcando notificación $notificationId...")
            repository.markAsRead(notificationId).onSuccess {
                Log.d("NotificationsViewModel", "markAsRead() - Notificación marcada, recargando lista...")
                loadNotifications()
            }.onFailure { error ->
                Log.e("NotificationsViewModel", "markAsRead() - Error: ${error.message}")
            }
        }
    }

    fun loadNotificationSettings() {
        viewModelScope.launch {
            Log.d("NotificationsViewModel", "loadNotificationSettings() - Iniciando carga...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            repository.getNotificationSettings().onSuccess { settings ->
                Log.d("NotificationsViewModel", "loadNotificationSettings() - Configuración cargada")
                _state.value = _state.value.copy(settings = settings, isLoading = false)
            }.onFailure { error ->
                Log.e("NotificationsViewModel", "loadNotificationSettings() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al cargar configuración de notificaciones"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }

    fun updateNotificationSettings(
        emailEnabled: Boolean? = null,
        pushEnabled: Boolean? = null,
        subscriptionReminders: Boolean? = null,
        daysBeforeNotice: Int? = null
    ) {
        viewModelScope.launch {
            Log.d("NotificationsViewModel", "updateNotificationSettings() - Actualizando...")
            _state.value = _state.value.copy(isLoading = true, errorMessage = "", isUnauthorized = false)
            val request = UpdateNotificationSettingsRequest(
                email_enabled = emailEnabled,
                push_enabled = pushEnabled,
                subscription_reminders = subscriptionReminders,
                days_before_notice = daysBeforeNotice
            )
            repository.updateNotificationSettings(request).onSuccess { settings ->
                Log.d("NotificationsViewModel", "updateNotificationSettings() - Configuración actualizada, recargando...")
                _state.value = _state.value.copy(settings = settings, isLoading = false)
                loadNotificationSettings()
            }.onFailure { error ->
                Log.e("NotificationsViewModel", "updateNotificationSettings() - Error: ${error.message}")
                val unauthorized = error is AuthenticationException
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = if (unauthorized) "" else (error.message ?: "Error al actualizar configuración de notificaciones"),
                    isUnauthorized = unauthorized
                )
            }
        }
    }
}

class NotificationsViewModelFactory(private val repository: NotificationsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationsViewModel(repository) as T
    }
}
