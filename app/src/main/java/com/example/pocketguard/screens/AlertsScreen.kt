package com.example.pocketguard.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.components.AlertConfigModal
import com.example.pocketguard.ui.theme.*

// 1. CREAMOS UN MODELO DE DATOS PARA LAS ALERTAS
data class AlertUiModel(
    val id: Int,
    val title: String,
    val message: String,
    val time: String,
    val typeColor: Color,
    var isRead: Boolean = false // Nuevo campo para saber si está leída
)

@Composable
fun AlertsScreen() {
    var showConfigModal by remember { mutableStateOf(false) }

    // Filtros
    val filters = listOf("Todas", "Sin leer", "Suscripciones")
    var selectedFilter by remember { mutableStateOf("Todas") }

    // 2. LISTA DE ESTADO (MUTEABLE) CON LOS DATOS
    // Usamos 'remember { mutableStateListOf(...) }' para que la UI reaccione a los cambios
    val alerts = remember {
        mutableStateListOf(
            AlertUiModel(1, "Próximo cargo: Netflix", "Tu suscripción de Netflix ($199) se cargará en 4 días.", "02-06 • 08:00", ErrorRed),
            AlertUiModel(2, "Próximo cargo: Spotify", "Tu suscripción de Spotify ($115) se cargará en 6 días.", "02-06 • 08:00", ErrorRed),
            AlertUiModel(3, "Límite alcanzado", "Has gastado $3,600 este mes, el 90% de tu presupuesto.", "02-06 • 07:30", ErrorRed),
            AlertUiModel(4, "Nuevo Inicio de Sesión", "Se detectó un nuevo inicio en Chrome Windows.", "02-05 • 14:20", Color(0xFFFFA500), isRead = true) // Una ya leída de ejemplo
        )
    }

    // 3. CÁLCULO DINÁMICO DE CONTADORES
    val unreadCount = alerts.count { !it.isRead } // Cuenta cuántas son falsas

    // Filtramos la lista según el chip seleccionado
    val displayedAlerts = when(selectedFilter) {
        "Sin leer" -> alerts.filter { !it.isRead }
        "Suscripciones" -> alerts.filter { it.title.contains("cargo") }
        else -> alerts // "Todas"
    }

    Scaffold(
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Cabecera con contador dinámico
            AlertsHeader(
                unreadCount = unreadCount,
                onSettingsClick = { showConfigModal = true }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjetas de Resumen
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Sin leer",
                            count = unreadCount.toString(), // <--- DATO REAL
                            icon = Icons.Default.NotificationsActive,
                            backgroundColor = if(unreadCount > 0) ErrorRed else Color.LightGray,
                            contentColor = White,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard("Alta", "3", Icons.Outlined.ErrorOutline, White, ErrorRed, Modifier.weight(1f))
                        SummaryCard("Cargos", "2", Icons.Outlined.CalendarToday, White, Color(0xFF9146FF), Modifier.weight(1f))
                    }
                }

                // Filtros
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters.size) { index ->
                            val filter = filters[index]
                            val isSelected = filter == selectedFilter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) GreenPrimary else White)
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(filter, color = if (isSelected) White else TextGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 4. LISTA DINÁMICA DE ALERTAS
                items(displayedAlerts) { alert ->
                    DetailedAlertCard(
                        alert = alert,
                        onMarkAsRead = {
                            // Lógica para marcar como leída
                            val index = alerts.indexOf(alert)
                            if (index != -1) {
                                // Copiamos el objeto cambiando isRead a true
                                alerts[index] = alerts[index].copy(isRead = true)
                            }
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        }
    }

    if (showConfigModal) {
        AlertConfigModal(onDismiss = { showConfigModal = false })
    }
}

// ==========================================
// COMPONENTES UI ACTUALIZADOS
// ==========================================

@Composable
fun AlertsHeader(unreadCount: Int, onSettingsClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(GreenPrimary, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Alertas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)
                Text(
                    text = if(unreadCount == 0) "Estás al día" else "$unreadCount sin leer",
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.8f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircleOutline, null, tint = White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(White.copy(alpha = 0.2f)).clickable { onSettingsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, null, tint = White)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, backgroundColor: Color, contentColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.Start) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(title, fontSize = 12.sp, color = contentColor.copy(alpha = 0.9f))
            Text(count, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun DetailedAlertCard(
    alert: AlertUiModel,
    onMarkAsRead: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if(alert.isRead) BackgroundLight else White), // Fondo más oscuro si ya se leyó
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if(alert.isRead) 0.dp else 2.dp),
        border = BorderStroke(1.dp, InputBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // CORRECCIÓN: Alineación superior
            Row(verticalAlignment = Alignment.Top) {

                // Icono (Gris si está leída, Color si no)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(1.dp, if(alert.isRead) TextGray else alert.typeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PriorityHigh,
                        null,
                        tint = if(alert.isRead) TextGray else alert.typeColor,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            alert.title,
                            fontWeight = if(alert.isRead) FontWeight.Normal else FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if(alert.isRead) TextGray else TextDark
                        )

                        // PUNTO VERDE: Solo se muestra si NO está leída
                        if (!alert.isRead) {
                            Box(modifier = Modifier.size(8.dp).background(GreenPrimary, CircleShape))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(alert.message, fontSize = 12.sp, color = TextGray, lineHeight = 16.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Schedule, null, tint = TextGray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(alert.time, fontSize = 11.sp, color = TextGray)
                    }

                    // BOTÓN: Solo se muestra si NO está leída
                    if (!alert.isRead) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(GreenPrimary)
                                .clickable { onMarkAsRead() } // Acción al hacer clic
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Marcar como leída", color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}