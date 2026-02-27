package com.example.pocketguard.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.components.TransactionItem
import com.example.pocketguard.ui.theme.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically

// Cambia la firma de HomeScreen para recibir el callback
@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit = {}, // Nuevo parámetro con valor por defecto
    onNavigateToSettings: () -> Unit = {} // Agregar callback para configuración
) {
    Scaffold(
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Pasamos el evento al Header
            HomeHeaderSection(onNavigateToSettings = onNavigateToSettings)

            // ... (Resto del contenido igual: Spacer, QuickStatsRow, etc.) ...
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                QuickStatsRow()
                Spacer(modifier = Modifier.height(20.dp))
                UpcomingChargesSection()
                Spacer(modifier = Modifier.height(20.dp))
                MonthlyTrendSection()
                Spacer(modifier = Modifier.height(20.dp))
                RecentActivitySection()
                Spacer(modifier = Modifier.height(24.dp))
                SmartAnalysisSection()
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// Actualiza el Header para tener el icono de Menú
@Composable
fun HomeHeaderSection(onNavigateToSettings: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreenPrimary, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header Top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lado Izquierdo: SOLO TEXTO (Sin icono menú)
                Column {
                    Text("PocketGuard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
                    Text("Gestor de Gastos", fontSize = 12.sp, color = White.copy(alpha = 0.8f))
                }

                // Lado Derecho: Botón de configuración
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de configuración
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Configuración",
                            tint = White
                        )
                    }

                    // Avatar
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("UD", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ... (El resto de la tarjeta de balance se queda IGUAL) ...
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Saldo Disponible", fontSize = 14.sp, color = TextGray)
                            Text("$0.00", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        Surface(
                            color = GreenPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("0.0%", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Text("Después de gastos y suscripciones", fontSize = 12.sp, color = TextGray.copy(alpha = 0.7f))

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatColumn("Ingresos", "$0.00")
                        StatColumn("Gastos", "$0.00")
                        StatColumn("Suscripciones", "$0.00")
                    }
                }
            }
        }
    }
}

// ==========================================
// SECCIONES (COMPONENTES)
// ==========================================
@Composable
fun QuickStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(Modifier.weight(1f), Icons.Default.TrendingUp, "Ingresos", "$0.00")
        QuickStatCard(Modifier.weight(1f), Icons.Default.CreditCard, "0 Suscripciones", "$0.00")
    }
}

@Composable
fun UpcomingChargesSection() {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Próximos Cargos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Icon(Icons.Default.CalendarToday, null, tint = TextGray, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(16.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarToday, null, tint = TextGray.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay cargos próximos", color = TextGray, fontSize = 14.sp)
                }
            }
        }
    }
}

// --- CLASE DE DATOS PARA LA GRÁFICA ---
data class ChartData(
    val month: String,
    val expenses: Int,
    val subscriptions: Int,
    val fill: Float
)

@Composable
fun MonthlyTrendSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Tendencia Mensual", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.TrendingUp, null, tint = TextGray.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay datos suficientes", color = TextGray, fontSize = 14.sp)
                    Text("Comienza a registrar tus gastos", color = TextGray.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection() {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Actividad Reciente", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Icon(Icons.Default.ChevronRight, null, tint = TextGray)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(16.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, null, tint = TextGray.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sin actividad reciente", color = TextGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SmartAnalysisSection() {
    Card(colors = CardDefaults.cardColors(containerColor = DarkCardBackground), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFFC107))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Análisis Inteligente", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gasto Hormiga", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$0.00", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)
                    Text("En compras menores a $100", fontSize = 12.sp, color = White.copy(alpha = 0.5f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF163E30)), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Potencial de Ahorro", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("0%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)
                    Text("Reduciendo gastos innecesarios", fontSize = 12.sp, color = White.copy(alpha = 0.5f))
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES AUXILIARES
// ==========================================

@Composable
fun StatColumn(label: String, amount: String) {
    Column { Text(label, fontSize = 12.sp, color = TextGray); Text(amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark) }
}

@Composable
fun QuickStatCard(modifier: Modifier, icon: ImageVector, title: String, amount: String) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) { Icon(icon, null, tint = GreenPrimary); Spacer(modifier = Modifier.height(12.dp)); Text(title, fontSize = 12.sp, color = TextGray); Text(amount, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark) }
    }
}

@Composable
fun UpcomingChargeItem(name: String, date: String, amount: String, daysLeft: String, icon: ImageVector) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).border(1.dp, InputBackground, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.SemiBold, color = TextDark); Text(date, fontSize = 12.sp, color = TextGray) }
            Column(horizontalAlignment = Alignment.End) { Text(amount, fontWeight = FontWeight.Bold, color = TextDark); Text(daysLeft, fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun ChartBar(label: String, fill: Float, isSelected: Boolean, onClick: () -> Unit) {
    // Animación suave de altura
    val animatedFill by animateFloatAsState(targetValue = fill, label = "barFill")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            indication = null, // Sin efecto ripple para que sea más limpio
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .width(24.dp) // Barra un poco más ancha para mejor tacto
                .fillMaxHeight(0.85f)
                .background(InputBackground, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedFill)
                    .align(Alignment.BottomCenter)
                    // Si está seleccionado, se oscurece un poco, si no es gris normal
                    .background(if(isSelected) Color(0xFF6B7280) else Color(0xFF9CA3AF), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
            // Punta Verde
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-(180 * 0.85 * animatedFill).toInt()).dp + 6.dp)
                    .background(GreenPrimary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Texto en negrita si está seleccionado
        Text(
            text = label,
            fontSize = 10.sp,
            color = if(isSelected) TextDark else TextGray,
            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}