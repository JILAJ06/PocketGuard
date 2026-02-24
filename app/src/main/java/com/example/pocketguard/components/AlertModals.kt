package com.example.pocketguard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertConfigModal(
    onDismiss: () -> Unit
) {
    // Estados de los switches
    var emailEnabled by remember { mutableStateOf(true) }
    var pushEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(false) }

    var subAlerts by remember { mutableStateOf(true) }
    var budgetAlerts by remember { mutableStateOf(true) } // Rojo en la imagen
    var insightsAlerts by remember { mutableStateOf(true) }

    // Estado del Slider
    var sliderValue by remember { mutableStateOf(3f) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(GreenPrimary, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Settings, null, tint = White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Configuración", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                        Text("Personaliza tus alertas", fontSize = 14.sp, color = TextGray)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(InputBackground, CircleShape)) {
                    Icon(Icons.Default.Close, "Cerrar", tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // SECCIÓN 1: CANALES
            SectionTitle(Icons.Outlined.Notifications, "Canales de Notificación")
            ConfigSwitchRow(Icons.Outlined.Email, "Email", "usuario@ejemplo.com", emailEnabled, Color(0xFF4285F4)) { emailEnabled = it }
            ConfigSwitchRow(Icons.Outlined.Smartphone, "Push", "Notificaciones móviles", pushEnabled, Color(0xFF9146FF)) { pushEnabled = it }
            ConfigSwitchRow(Icons.Outlined.VolumeUp, "Sonido", "Reproducir alertas", soundEnabled, Color(0xFFFF9900)) { soundEnabled = it }

            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN 2: TIPOS DE ALERTAS
            SectionTitle(Icons.Outlined.Bolt, "Tipos de Alertas")
            ConfigSwitchRow(Icons.Outlined.CreditCard, "Suscripciones", "Próximos cargos", subAlerts, Color(0xFF9146FF)) { subAlerts = it }
            ConfigSwitchRow(Icons.Outlined.AttachMoney, "Presupuesto", "Límite de gasto", budgetAlerts, ErrorRed) { budgetAlerts = it }
            ConfigSwitchRow(Icons.Outlined.TrendingUp, "Insights", "Consejos de ahorro", insightsAlerts, GreenPrimary) { insightsAlerts = it }

            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN 3: DÍAS DE ANTICIPACIÓN (Slider)
            SectionTitle(Icons.Outlined.CalendarToday, "Días de Anticipación")
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                border = androidx.compose.foundation.BorderStroke(1.dp, InputBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Avisar con", fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Antes de cada cargo", fontSize = 12.sp, color = TextGray)
                        }
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFFFF9900), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${sliderValue.toInt()}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFFF9900))
                                Text("días", fontSize = 10.sp, color = Color(0xFFFF9900))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 1f..7f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF9900),
                            activeTrackColor = Color(0xFFFF9900),
                            inactiveTrackColor = InputBackground
                        )
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        (1..7).forEach { day ->
                            Text("$day", fontSize = 10.sp, color = TextGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN 4: INFO CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)), // Azul muy claro
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFF4285F4))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Configuración Activa", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 14.sp)
                        Text("Recibirás alertas ${sliderValue.toInt()} días antes de cada cargo por email y notificaciones push.", fontSize = 12.sp, color = Color(0xFF4285F4))
                    }
                }
            }

            // SECCIÓN 5: GESTIÓN DE CUENTA
            SectionTitle(Icons.Outlined.Settings, "Gestión de Cuenta")

            // Botón Cerrar Sesión
            OutlinedButton(
                onClick = { /* Logout */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE67E22)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE67E22).copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // CORRECCIÓN 1: Usamos ExitToApp en lugar de AutoMirrored.Logout
                        Icon(Icons.Default.ExitToApp, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                            Text("Salir de tu cuenta", fontSize = 11.sp, fontWeight = FontWeight.Normal)
                        }
                    }
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Eliminar Cuenta
            OutlinedButton(
                onClick = { /* Delete */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Delete, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Eliminar Cuenta", fontWeight = FontWeight.Bold)
                            Text("Acción permanente", fontSize = 11.sp, fontWeight = FontWeight.Normal)
                        }
                    }
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Componente Auxiliar para las filas de configuración
@Composable
fun ConfigSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    color: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, InputBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = TextGray)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = color, // El track toma el color del icono
                    uncheckedThumbColor = White,
                    uncheckedTrackColor = InputBackground
                )
            )
        }
    }
}

@Composable
fun SectionTitle(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
        Icon(icon, null, tint = TextGray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
    }
}