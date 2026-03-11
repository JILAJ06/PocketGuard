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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    initialEmailEnabled: Boolean = true,
    initialPushEnabled: Boolean = true,
    initialSubscriptionReminders: Boolean = true,
    initialDaysBeforeNotice: Int = 3,
    onSavePreferences: (Boolean, Boolean, Boolean, Boolean, Int) -> Unit = { _, _, _, _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var emailEnabled by remember { mutableStateOf(initialEmailEnabled) }
    var pushEnabled by remember { mutableStateOf(initialPushEnabled) }
    var subAlerts by remember { mutableStateOf(initialSubscriptionReminders) }
    var budgetAlerts by remember { mutableStateOf(true) }
    var daysBefore by remember { mutableStateOf(initialDaysBeforeNotice.toFloat()) }

    // Estado para controlar la visibilidad del botón Guardar
    var showSaveButton by remember { mutableStateOf(false) }

    // COLORES TEMÁTICOS
    val notifColor = Color(0xFF4A90E2) // Azul
    val alertTypeColor = Color(0xFF9B59B6) // Morado
    val daysColor = Color(0xFFFF9900) // Naranja

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
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
                        Text("Configuración", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                        Text("Personaliza tus alertas", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                // 1. Notificaciones
                SectionHeader("Canales de Notificación", Icons.Outlined.Notifications)
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

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Días de Anticipación
                SectionHeader("Días de Anticipación", Icons.Outlined.CalendarToday)
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, daysColor.copy(alpha = 0.2f))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Avisar con ${daysBefore.toInt()} días de antelación", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
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
    onSave: (String, String, String?, String?) -> Unit,
    initialBankName: String = "",
    initialAlias: String = "",
    initialDigits: String = "",
    initialColorHex: String = "#1976D2",
    isEditMode: Boolean = false
) {
    var bankName by remember { mutableStateOf(initialBankName) }
    var alias by remember { mutableStateOf(initialAlias) }
    var digits by remember { mutableStateOf(initialDigits) }
    val cardColors = listOf(Color(0xFF1976D2), Color(0xFF2E7D32), Color(0xFFC62828), Color(0xFFF9A825), Color(0xFF8E44AD), Color(0xFF34495E))

    val initialColor = try {
        Color(android.graphics.Color.parseColor(initialColorHex))
    } catch (e: Exception) {
        cardColors[0]
    }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var showBanksDropdown by remember { mutableStateOf(false) }

    // Filtrar bancos según lo que el usuario escriba
    val filteredBanks = remember(bankName, banks) {
        if (bankName.isBlank()) {
            banks
        } else {
            banks.filter { it.contains(bankName, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (isEditMode) "Editar Tarjeta" else "Nueva Tarjeta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Campo de banco con dropdown autocompletable
                Box {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = {
                            bankName = it
                            // NO cambiar showBanksDropdown aquí para evitar que el teclado se cierre
                        },
                        label = { Text("Nombre del Banco") },
                        placeholder = { Text("Escribe o selecciona un banco") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                // Solo abrir dropdown si hay bancos filtrados
                                if (filteredBanks.isNotEmpty()) {
                                    showBanksDropdown = !showBanksDropdown
                                }
                            }) {
                                Icon(
                                    imageVector = if (showBanksDropdown) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Toggle dropdown",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = showBanksDropdown && filteredBanks.isNotEmpty(),
                        onDismissRequest = { showBanksDropdown = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .heightIn(max = 200.dp)
                    ) {
                        filteredBanks.forEach { bank ->
                            DropdownMenuItem(
                                text = { Text(bank, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    bankName = bank
                                    showBanksDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Alias") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = digits,
                    onValueChange = { if(it.length <= 4) digits = it },
                    label = { Text("Últimos 4 dígitos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Color",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    cardColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color, CircleShape)
                                .clickable { selectedColor = color }
                                .border(
                                    2.dp,
                                    if(selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSave(bankName, alias, digits, colorToHex(selectedColor)) },
                    enabled = bankName.isNotEmpty() && alias.isNotEmpty() && digits.length == 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- DIÁLOGO EDITAR TARJETA ---
@Composable
fun EditCardDialog(
    bankName: String,
    last4Digits: String?,
    initialAlias: String,
    initialColorHex: String?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit  // (alias, colorHex)
) {
    var alias by remember { mutableStateOf(initialAlias) }
    val cardColors = listOf(
        Color(0xFF1976D2),
        Color(0xFF2E7D32),
        Color(0xFFC62828),
        Color(0xFFF9A825),
        Color(0xFF8E44AD),
        Color(0xFF34495E)
    )

    val initialColor = try {
        Color(android.graphics.Color.parseColor(initialColorHex))
    } catch (e: Exception) {
        cardColors[0]
    }
    var selectedColor by remember { mutableStateOf(initialColor) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Editar Tarjeta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mostrar info de la tarjeta (solo lectura)
                OutlinedTextField(
                    value = bankName,
                    onValueChange = {},
                    label = { Text("Banco") },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = "•••• ${last4Digits ?: ""}",
                    onValueChange = {},
                    label = { Text("Últimos 4 dígitos") },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Alias (editable)
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Alias") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Color",
                    fontSize = 12.sp,
                    color = TextGray,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    cardColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color, CircleShape)
                                .clickable { selectedColor = color }
                                .border(
                                    2.dp,
                                    if (selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = { onSave(alias, colorToHex(selectedColor)) },
                        enabled = alias.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
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
        Icon(icon, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun SettingsCard(title: String, subtitle: String, icon: ImageVector, themeColor: Color, hasSwitch: Boolean = false, checked: Boolean = false, onCheckedChange: (Boolean) -> Unit = {}) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).background(themeColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = themeColor, modifier = Modifier.size(22.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface); Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (hasSwitch) { Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = themeColor, uncheckedThumbColor = White, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant)) }
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
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(
                if(title.contains("Cerrar")) Icons.Outlined.ExitToApp else Icons.Outlined.Delete,
                null,
                tint = color
            )
        }
    }
}