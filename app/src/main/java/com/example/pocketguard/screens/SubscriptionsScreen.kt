package com.example.pocketguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.data.models.Subscription
import com.example.pocketguard.ui.theme.GreenPrimary
import com.example.pocketguard.ui.theme.TextGray
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SubscriptionsScreen() {
    // Estado de las suscripciones (se reemplazará con datos de la API)
    val subscriptions = remember {
        mutableStateOf(
            listOf(
                Subscription(
                    id = "1",
                    name = "Netflix",
                    category = "Entretenimiento",
                    monthlyPrice = 199.0,
                    currentMonthPrice = 2388.0,
                    nextPaymentDate = LocalDate.of(2026, 2, 12),
                    isActive = true,
                    color = Color(0xFFE50914),
                    icon = "🎬"
                ),
                Subscription(
                    id = "2",
                    name = "Spotify",
                    category = "Entretenimiento",
                    monthlyPrice = 115.0,
                    currentMonthPrice = 1380.0,
                    nextPaymentDate = LocalDate.of(2026, 2, 12),
                    isActive = true,
                    color = Color(0xFF1DB954),
                    icon = "🎵"
                ),
                Subscription(
                    id = "3",
                    name = "Amazon Prime",
                    category = "Compras",
                    monthlyPrice = 99.0,
                    currentMonthPrice = 1188.0,
                    nextPaymentDate = LocalDate.of(2026, 2, 15),
                    isActive = true,
                    color = Color(0xFFFF9900),
                    icon = "📦"
                ),
                Subscription(
                    id = "4",
                    name = "HBO Max",
                    category = "Entretenimiento",
                    monthlyPrice = 149.0,
                    currentMonthPrice = 1788.0,
                    nextPaymentDate = LocalDate.of(2026, 2, 9),
                    isActive = true,
                    color = Color(0xFF9146FF),
                    icon = "📺"
                ),
                Subscription(
                    id = "5",
                    name = "Adobe CC",
                    category = "Trabajo",
                    monthlyPrice = 599.0,
                    currentMonthPrice = 7188.0,
                    nextPaymentDate = LocalDate.of(2026, 2, 20),
                    isActive = true,
                    color = Color(0xFFFF0000),
                    icon = "🎨"
                ),
                Subscription(
                    id = "6",
                    name = "iCloud",
                    category = "Almacenamiento",
                    monthlyPrice = 17.0,
                    currentMonthPrice = 204.0,
                    nextPaymentDate = LocalDate.of(2026, 2, 8),
                    isActive = true,
                    color = Color(0xFF00A4EF),
                    icon = "☁️"
                ),
                Subscription(
                    id = "7",
                    name = "YouTube",
                    category = "Entretenimiento",
                    monthlyPrice = 119.0,
                    currentMonthPrice = 1428.0,
                    nextPaymentDate = LocalDate.of(2026, 2, 18),
                    isActive = false,
                    color = Color(0xFFFF0000),
                    icon = "▶️"
                ),
                Subscription(
                    id = "8",
                    name = "Crunchyroll",
                    category = "Entretenimiento",
                    monthlyPrice = 99.0,
                    currentMonthPrice = 1188.0,
                    nextPaymentDate = LocalDate.of(2026, 2, 25),
                    isActive = true,
                    color = Color(0xFFFFA500),
                    icon = "🎌"
                )
            )
        )
    }

    // Calcular estadísticas
    val totalMonthly = subscriptions.value.sumOf { it.monthlyPrice }
    val totalCurrent = subscriptions.value.sumOf { it.currentMonthPrice }
    val activeCount = subscriptions.value.count { it.isActive }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenPrimary)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PocketGuard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "UD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gestor de Gastos",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Contenido principal
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sección de título
            item {
                Text(
                    text = "Suscripciones",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Controla tus servicios",
                    fontSize = 14.sp,
                    color = TextGray
                )
            }

            // Botón de agregar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Activas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            text = activeCount.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }
                    FloatingActionButton(
                        onClick = { /* TODO: Navegar a agregar suscripción */ },
                        containerColor = GreenPrimary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Lista de suscripciones
            items(subscriptions.value) { subscription ->
                SubscriptionCard(
                    subscription = subscription,
                    onToggleActive = { /* TODO: Actualizar estado */ },
                    onEdit = { /* TODO: Editar suscripción */ },
                    onDelete = { /* TODO: Eliminar suscripción */ }
                )
            }

            // Resumen anual
            item {
                AnnualSummaryCard(
                    total = totalCurrent,
                    activeSubscriptions = activeCount
                )
            }

            // Espaciado inferior
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado de la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar con icono
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                subscription.color.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subscription.icon,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subscription.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            text = subscription.category,
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }

                // Toggle estado
                Switch(
                    checked = subscription.isActive,
                    onCheckedChange = { onToggleActive() },
                    modifier = Modifier.scale(0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Precios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Mensual",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Text(
                        text = "$${String.format("%.2f", subscription.monthlyPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Actualizado",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Text(
                        text = "$${String.format("%.0f", subscription.currentMonthPrice)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = subscription.color
                    )
                }
            }

            // Información adicional (expandible)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Próximo cargo",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                        Text(
                            text = subscription.nextPaymentDate.format(dateFormatter),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }

                    // Botones de acciones
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFE74C3C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnualSummaryCard(
    total: Double,
    activeSubscriptions: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GreenPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Impacto Anual",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${String.format("%.0f", total * 12)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "en $activeSubscriptions servicios",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}



