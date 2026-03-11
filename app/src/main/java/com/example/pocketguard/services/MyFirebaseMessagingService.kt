package com.example.pocketguard.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pocketguard.MainActivity
import com.example.pocketguard.R
import com.example.pocketguard.utils.FCMTokenManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "pocketguard_notifications"
        private const val CHANNEL_NAME = "PocketGuard Notifications"
    }

    /**
     * Se llama cuando se recibe un nuevo token de FCM
     * Este token debe ser enviado al backend para que pueda enviar notificaciones
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo FCM Token: $token")

        // Usar FCMTokenManager para manejar el nuevo token
        val fcmTokenManager = FCMTokenManager(applicationContext)
        fcmTokenManager.onTokenRefreshed(token)
    }

    /**
     * Se llama cuando llega una notificación push
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        // Verificar si el mensaje contiene datos
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Verificar si el mensaje contiene una notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Título: ${it.title}, Cuerpo: ${it.body}")
            showNotification(it.title ?: "PocketGuard", it.body ?: "")
        }
    }

    /**
     * Maneja mensajes de datos personalizados
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "PocketGuard"
        val body = data["body"] ?: ""
        val type = data["type"] // Puede ser: "subscription_due", "expense_alert", etc.

        showNotification(title, body)
    }

    /**
     * Muestra una notificación local
     */
    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificaciones para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de gastos y suscripciones"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construir la notificación
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Cambia esto por tu ícono
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}



