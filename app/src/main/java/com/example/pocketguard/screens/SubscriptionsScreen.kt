package com.example.pocketguard.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.pocketguard.data.SubscriptionRepository

@Composable
fun SubscriptionsScreen(
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    // Datos de prueba (State)

    val subscriptions = remember {
        mutableStateOf(
            listOf(
                Subscription("1", "Netflix", "Entretenimiento", 199.0, 2388.0, LocalDate.of(2026, 2, 12), true, Color(0xFFE50914), "🎬"),
                Subscription("2", "Spotify", "Música", 115.0, 1380.0, LocalDate.of(2026, 2, 12), true, Color(0xFF1DB954), "🎵"),
                Subscription("3", "Amazon Prime", "Compras", 99.0, 1188.0, LocalDate.of(2026, 2, 15), true, Color(0xFFFF9900), "📦"),
                Subscription("4", "HBO Max", "Streaming", 149.0, 1788.0, LocalDate.of(2026, 2, 9), true, Color(0xFF9146FF), "📺"),
                Subscription("5", "Adobe CC", "Trabajo", 599.0, 7188.0, LocalDate.of(2026, 2, 20), true, Color(0xFFFF0000), "🎨"),
                Subscription("6", "YouTube Premium", "Video", 119.0, 1428.0, LocalDate.of(2026, 2, 18), false, Color(0xFFFF0000), "▶️")
            )
        )
    }

    val totalMonthly = subscriptions.value.filter { it.isActive }.sumOf { it.monthlyPrice }
    val totalYearly = subscriptions.value.filter { it.isActive }.sumOf { it.currentMonthPrice }
    val activeCount = subscriptions.value.count { it.isActive }

    // Scaffold para el color de fondo general
    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = GreenPrimary,
                contentColor = White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Suscripción")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Cabecera Estilo PocketGuard (Verde Curva)
            SubscriptionHeader()

            // 2. Contenido con Scroll
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp), // Espacio para el FAB
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título y Contador
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tus Suscripciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("$activeCount servicios activos", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                        }
                        // Badge de Total Mensual
                        Surface(
                            color = GreenPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$${String.format("%.0f", totalMonthly)}/mes",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Tarjeta de Resumen Anual (Diseño destacado)
                item {
                    AnnualSummaryCard(totalYearly, activeCount)
                }

                // Lista de Suscripciones
                items(subscriptions.value) { sub ->
                    SubscriptionCard(
                        subscription = sub,
                        onToggle = { /* lógica toggle */ },
                        onEdit = { onEditClick(sub.id) } // <--- ¡AQUÍ CONECTAMOS LA ACCIÓN!
                    )
                }
            }
        }
    }
}

// --- COMPONENTES VISUALES ---

@Composable
fun SubscriptionHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp) // Altura reducida comparada con el Home
            .background(
                color = GreenPrimary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("PocketGuard", style = MaterialTheme.typography.titleLarge, color = White, fontWeight = FontWeight.Bold)
                Text("Gestor de Suscripciones", style = MaterialTheme.typography.bodyMedium, color = White.copy(alpha = 0.8f))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("UD", color = White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }, // Expandir al tocar
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila Principal (Siempre visible)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icono con fondo de color suave
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(subscription.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(subscription.icon, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Nombre y Categoría
                Column(modifier = Modifier.weight(1f)) {
                    Text(subscription.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Text(subscription.category, fontSize = 12.sp, color = TextGray)
                }

                // Precio y Switch
                Column(horizontalAlignment = Alignment.End) {
                    Text("$${subscription.monthlyPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Switch(
                        checked = subscription.isActive,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.scale(0.7f).height(30.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = White,
                            checkedTrackColor = GreenPrimary,
                            uncheckedThumbColor = White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }

            // Sección Expandible (Detalles)
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BackgroundLight, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailItem("Próximo pago", subscription.nextPaymentDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                        DetailItem("Costo Anual", "$${subscription.currentMonthPrice.toInt()}")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones de acción
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = onEdit) {
                            Text("Editar", color = TextGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { /* Eliminar */ },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f), contentColor = ErrorRed),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = TextGray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

@Composable
fun AnnualSummaryCard(total: Double, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground) // Usamos el color oscuro del tema
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Impacto Anual", color = White.copy(alpha = 0.7f), fontSize = 14.sp)
                Text("Estimado", color = White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${String.format("%,.0f", total)}", color = White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("en $count suscripciones", color = GreenPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- CLASE DE DATOS ---
data class Subscription(
    val id: String,
    val name: String,
    val category: String,
    val monthlyPrice: Double,
    val currentMonthPrice: Double,
    val nextPaymentDate: LocalDate,
    val isActive: Boolean,
    val color: Color,
    val icon: String
)