package com.example.pocketguard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pocketguard.ui.theme.*

// Modelo de Tarjeta
data class PaymentCard(val id: String, val name: String, val last4: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingsModal(
    onDismiss: () -> Unit,
    // Actualizamos el callback para devolver TODOS los valores
    onSavePreferences: (Boolean, Boolean, Boolean, Boolean, Int) -> Unit = { _, _, _, _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estados Configuración de Alertas
    var emailEnabled by remember { mutableStateOf(true) }
    var pushEnabled by remember { mutableStateOf(true) }

    var subAlerts by remember { mutableStateOf(true) }
    var budgetAlerts by remember { mutableStateOf(true) }

    var daysBefore by remember { mutableStateOf(3f) }

    // Estado para controlar la visibilidad del botón Guardar
    var showSaveButton by remember { mutableStateOf(false) }

    // COLORES TEMÁTICOS
    val notifColor = Color(0xFF4A90E2) // Azul
    val alertTypeColor = Color(0xFF9B59B6) // Morado
    val daysColor = Color(0xFFFF9900) // Naranja

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundLight,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(GreenPrimary, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, null, tint = White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Configuración", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                        Text("Personaliza tus alertas", fontSize = 13.sp, color = TextGray)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(White, CircleShape)) {
                    Icon(Icons.Default.Close, null, tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                // 1. Notificaciones
                SectionHeader("Canales de Notificación", Icons.Outlined.Notifications)
                Spacer(modifier = Modifier.height(8.dp))
                SettingsCard("Email", "usuario@ejemplo.com", Icons.Outlined.Email, notifColor, true, emailEnabled) {
                    emailEnabled = it
                    showSaveButton = true
                }
                Spacer(modifier = Modifier.height(12.dp))
                SettingsCard("Push", "Notificaciones móviles", Icons.Outlined.Smartphone, notifColor, true, pushEnabled) {
                    pushEnabled = it
                    showSaveButton = true
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Tipos de Alerta
                SectionHeader("Tipos de Alertas", Icons.Outlined.Bolt)
                Spacer(modifier = Modifier.height(8.dp))
                SettingsCard("Suscripciones", "Próximos cargos", Icons.Outlined.CreditCard, alertTypeColor, true, subAlerts) {
                    subAlerts = it
                    showSaveButton = true
                }
                Spacer(modifier = Modifier.height(12.dp))
                SettingsCard("Presupuesto", "Límites de gasto", Icons.Outlined.AttachMoney, alertTypeColor, true, budgetAlerts) {
                    budgetAlerts = it
                    showSaveButton = true
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Días de Anticipación
                SectionHeader("Días de Anticipación", Icons.Outlined.CalendarToday)
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, daysColor.copy(alpha = 0.2f))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Avisar con ${daysBefore.toInt()} días de antelación", fontSize = 14.sp, color = TextDark, fontWeight = FontWeight.Bold)
                        Slider(
                            value = daysBefore,
                            onValueChange = {
                                daysBefore = it
                                showSaveButton = true
                            },
                            valueRange = 1f..7f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = daysColor, activeTrackColor = daysColor, inactiveTrackColor = daysColor.copy(0.2f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // BOTÓN GUARDAR (Animado)
                AnimatedVisibility(
                    visible = showSaveButton,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Button(
                        onClick = {
                            onSavePreferences(emailEnabled, pushEnabled, subAlerts, budgetAlerts, daysBefore.toInt())
                            showSaveButton = false
                            onDismiss() // Opcional: Cerrar al guardar
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Configuración", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- DIÁLOGO AGREGAR TARJETA ---
@Composable
fun AddCardDialog(
    banks: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String?) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var digits by remember { mutableStateOf("") }
    val cardColors = listOf(Color(0xFF1976D2), Color(0xFF2E7D32), Color(0xFFC62828), Color(0xFFF9A825), Color(0xFF8E44AD), Color(0xFF34495E))
    var selectedColor by remember { mutableStateOf(cardColors[0]) }
    var showBanksDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nueva Tarjeta", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                Spacer(modifier = Modifier.height(20.dp))
                Box {
                    OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Nombre del Banco") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().clickable { if (banks.isNotEmpty()) showBanksDropdown = true })
                    DropdownMenu(expanded = showBanksDropdown, onDismissRequest = { showBanksDropdown = false }) {
                        banks.forEach { bank ->
                            DropdownMenuItem(text = { Text(bank) }, onClick = { bankName = bank; showBanksDropdown = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = alias, onValueChange = { alias = it }, label = { Text("Alias") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = digits, onValueChange = { if(it.length <= 4) digits = it }, label = { Text("Últimos 4 dígitos") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(20.dp))
                Text("Color", fontSize = 12.sp, color = TextGray, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    cardColors.forEach { color -> Box(modifier = Modifier.size(36.dp).background(color, CircleShape).clickable { selectedColor = color }.border(2.dp, if(selectedColor == color) TextDark else Color.Transparent, CircleShape)) }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onSave(bankName, alias, digits, colorToHex(selectedColor)) }, enabled = bankName.isNotEmpty() && alias.isNotEmpty() && digits.length == 4, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) { Text("Guardar", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

// --- COMPONENTES VISUALES ---
@Composable
fun MiniCreditCard(card: PaymentCard) {
    Box(
        modifier = Modifier.width(120.dp).height(70.dp).background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(card.color, card.color.copy(alpha = 0.7f))), shape = RoundedCornerShape(10.dp)).padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(card.name, color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp, 8.dp).background(Color(0xFFFFD700), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Text("•••• ${card.last4}", color = White, fontSize = 10.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun SectionHeader(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextDark, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
    }
}

@Composable
fun SettingsCard(title: String, subtitle: String, icon: ImageVector, themeColor: Color, hasSwitch: Boolean = false, checked: Boolean = false, onCheckedChange: (Boolean) -> Unit = {}) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, InputBackground), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(themeColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = themeColor, modifier = Modifier.size(22.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark); Text(subtitle, fontSize = 12.sp, color = TextGray) }
            }
            if (hasSwitch) { Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = themeColor, uncheckedThumbColor = White, uncheckedTrackColor = InputBackground)) }
        }
    }
}

// --- ACTUALIZADO: AccountActionCard con onClick ---
@Composable
fun AccountActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit // Nuevo parámetro
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Acción al tocar
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(22.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark); Text(subtitle, fontSize = 12.sp, color = TextGray) }
            }
            Icon(if(title.contains("Cerrar")) Icons.Outlined.ExitToApp else Icons.Outlined.Delete, null, tint = color)
        }
    }
}