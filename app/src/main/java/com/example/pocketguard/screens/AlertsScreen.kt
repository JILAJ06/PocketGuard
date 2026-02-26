package com.example.pocketguard.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.components.AlertSettingsModal
import com.example.pocketguard.ui.theme.*

// --- MODELO DE DATOS MEJORADO ---
enum class AlertCategory {
    SUBSCRIPTION, BUDGET, SECURITY, GENERAL
}

data class AlertItemUI(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector,
    val category: AlertCategory,
    val isRead: Boolean,
    val isHighPriority: Boolean = false
)

@Composable
fun AlertsScreen() {
    var showSettingsModal by remember { mutableStateOf(false) }

    // Estado de Filtro
    var selectedFilter by remember { mutableStateOf("Todas") } // "Todas", "Sin leer", "Suscripciones"

    // --- LISTA MUTABLE DE ALERTAS (Estado) ---
    // Usamos mutableStateListOf para que la UI reaccione a los cambios (marcar como leído)
    val alerts = remember {
        mutableStateListOf(
            AlertItemUI("1", "Próximo cargo: Netflix", "Tu suscripción de Netflix ($199) se cargará en 4 días.", "02-06 • 08:00", Icons.Outlined.Info, AlertCategory.SUBSCRIPTION, isRead = false),
            AlertItemUI("2", "Próximo cargo: Spotify", "Tu suscripción de Spotify ($115) se cargará en 6 días.", "02-06 • 08:00", Icons.Outlined.Info, AlertCategory.SUBSCRIPTION, isRead = false),
            AlertItemUI("3", "Límite alcanzado", "Has gastado $3,600 este mes, el 90% de tu presupuesto.", "02-06 • 07:30", Icons.Outlined.Warning, AlertCategory.BUDGET, isRead = true, isHighPriority = true),
            AlertItemUI("4", "Gasto hormiga detectado", "Has realizado 15 compras menores a $100 por $1,240.", "02-05 • 18:00", Icons.Outlined.ErrorOutline, AlertCategory.BUDGET, isRead = false, isHighPriority = true),
            AlertItemUI("5", "Oportunidad de ahorro", "Podrías ahorrar $3,600 al año cancelando 2 suscripciones.", "02-05 • 12:00", Icons.Outlined.Lightbulb, AlertCategory.GENERAL, isRead = true)
        )
    }

    // --- CÁLCULOS DINÁMICOS ---
    val unreadCount = alerts.count { !it.isRead }
    val highPriorityCount = alerts.count { it.isHighPriority }
    val subscriptionCount = alerts.count { it.category == AlertCategory.SUBSCRIPTION }

    // --- LÓGICA DE FILTRADO ---
    val filteredAlerts = when (selectedFilter) {
        "Sin leer" -> alerts.filter { !it.isRead }
        "Suscripciones" -> alerts.filter { it.category == AlertCategory.SUBSCRIPTION }
        else -> alerts
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            // Cabecera Verde
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenPrimary)
                    .padding(top = 40.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Alertas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)
                        Text("$unreadCount sin leer", fontSize = 14.sp, color = White.copy(alpha = 0.8f))
                    }
                    IconButton(
                        onClick = { showSettingsModal = true },
                        modifier = Modifier.background(White.copy(0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, null, tint = White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            // 1. Tarjetas de Resumen (Dinámicas)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tarjeta Roja (Sin Leer) - Clic para filtrar rápido
                StatsCard(
                    title = "Sin leer",
                    count = unreadCount.toString(),
                    icon = Icons.Outlined.Notifications,
                    backgroundColor = Color(0xFFE74C3C),
                    contentColor = White,
                    modifier = Modifier.weight(1f).clickable { selectedFilter = "Sin leer" }
                )
                // Tarjeta Blanca (Alta Prioridad)
                StatsCard(
                    title = "Alta",
                    count = highPriorityCount.toString(),
                    icon = Icons.Outlined.ErrorOutline,
                    backgroundColor = White,
                    contentColor = TextDark,
                    modifier = Modifier.weight(1f)
                )
                // Tarjeta Blanca (Cargos/Suscripciones)
                StatsCard(
                    title = "Cargos",
                    count = subscriptionCount.toString(),
                    icon = Icons.Outlined.CalendarToday,
                    backgroundColor = White,
                    contentColor = TextDark,
                    modifier = Modifier.weight(1f).clickable { selectedFilter = "Suscripciones" }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Filtros y Acción de "Marcar todo"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lista de Filtros
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item { FilterChip(text = "Todas", isSelected = selectedFilter == "Todas", onClick = { selectedFilter = "Todas" }) }
                    item { FilterChip(text = "Sin leer", isSelected = selectedFilter == "Sin leer", onClick = { selectedFilter = "Sin leer" }) }
                    item { FilterChip(text = "Suscripciones", isSelected = selectedFilter == "Suscripciones", onClick = { selectedFilter = "Suscripciones" }) }
                }

                // Botón "Marcar todo como leído" (Solo si hay no leídos)
                if (unreadCount > 0) {
                    TextButton(
                        onClick = {
                            // Actualizamos todas las alertas a isRead = true
                            for (i in alerts.indices) {
                                alerts[i] = alerts[i].copy(isRead = true)
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(16.dp), tint = GreenPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Marcar leídos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Lista de Alertas
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (filteredAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tienes alertas en esta categoría.", color = TextGray, fontSize = 14.sp)
                    }
                } else {
                    filteredAlerts.forEach { alert ->
                        AlertItemCard(
                            alert = alert,
                            onClick = {
                                // Al hacer clic, marcar individualmente como leída
                                val index = alerts.indexOfFirst { it.id == alert.id }
                                if (index != -1) {
                                    alerts[index] = alerts[index].copy(isRead = true)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Banner Inferior (Preventivo)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF27AE60), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text("💡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Alertas Preventivas", fontWeight = FontWeight.Bold, color = White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Recibirás notificaciones 72h antes de cada cargo para tomar decisiones informadas sobre tus suscripciones.",
                            color = White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showSettingsModal) {
        AlertSettingsModal(onDismiss = { showSettingsModal = false })
    }
}

// --- COMPONENTES UI ---

@Composable
fun StatsCard(title: String, count: String, icon: ImageVector, backgroundColor: Color, contentColor: Color, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
            Column {
                Text(title, fontSize = 12.sp, color = contentColor.copy(alpha = 0.8f))
                Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = contentColor)
            }
        }
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (isSelected) GreenPrimary else White, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isSelected) White else TextGray)
    }
}

@Composable
fun AlertItemCard(alert: AlertItemUI, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Marcar como leída al tocar
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Indicador de "No leído" (Punto rojo)
            if (!alert.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, end = 8.dp)
                        .size(8.dp)
                        .background(Color(0xFFE74C3C), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(InputBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Color del icono cambia si es prioridad alta o normal
                val iconTint = if (alert.isHighPriority) Color(0xFFE74C3C) else TextGray
                Icon(alert.icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = alert.title,
                    fontWeight = if(!alert.isRead) FontWeight.Bold else FontWeight.Medium, // Negrita si no se ha leído
                    fontSize = 15.sp,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(alert.description, fontSize = 13.sp, color = TextGray, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null, tint = TextGray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(alert.time, fontSize = 11.sp, color = TextGray)
                }
            }
        }
    }
}