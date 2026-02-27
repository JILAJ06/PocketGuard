package com.example.pocketguard.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pocketguard.components.PaymentCard
import com.example.pocketguard.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// --- DATOS MOCK ---
// NOTA: Eliminé 'data class PaymentCard' de aquí porque ya existe en AlertModals.kt
// Solo dejamos SubCategoryData porque es específica de aquí (o podrías moverla también)
data class SubCategoryData(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NewSubscriptionModal(
    initialName: String = "",
    initialPrice: String = "",
    initialCategory: String = "",
    initialCycle: String = "Mensual",
    initialDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
    initialCardId: String = "",
    cards: List<PaymentCard> = emptyList(),
    categories: List<SubCategoryData> = emptyList(),

    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String?) -> Unit // Agregado cardId como sexto parámetro
) {
    // Estados
    var name by remember { mutableStateOf(initialName) }
    var price by remember { mutableStateOf(initialPrice) }
    var selectedCycle by remember { mutableStateOf(initialCycle) }
    var selectedDateDisplay by remember { mutableStateOf(initialDate) }

    // Estado de Tarjeta Seleccionada
    var selectedCardId by remember { mutableStateOf(initialCardId) }

    // Estados de UI y Categorías
    var mutableCategories by remember {
        mutableStateOf(categories.toMutableList())
    }

    // Lógica para preseleccionar categoría si estamos editando
    var selectedCategoryIndex by remember {
        mutableStateOf(
            if (initialCategory.isNotEmpty()) {
                val index = mutableCategories.indexOfFirst { it.name == initialCategory }
                if (index != -1) index else 0
            } else 0
        )
    }

    // Lógica adicional (Diálogos, Foco, Calendario)
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    val priceFocusRequester = remember { FocusRequester() }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val scrollState = rememberScrollState()
    val cycles = listOf("Diario", "Semanal", "Mensual", "Anual")

    val modalTitle = if (initialName.isNotEmpty()) "Editar Suscripción" else "Nueva Suscripción"

    // --- CALENDARIO ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        selectedDateDisplay = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }
                    showDatePicker = false
                }) { Text("Aceptar", fontWeight = FontWeight.Bold, color = GreenPrimary) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = TextGray) } },
            colors = DatePickerDefaults.colors(containerColor = White)
        ) {
            DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(selectedDayContainerColor = GreenPrimary, todayDateBorderColor = GreenPrimary, todayContentColor = GreenPrimary))
        }
    }

    // --- DIÁLOGO DE NUEVA CATEGORÍA ---
    if (showNewCategoryDialog) {
        NewCategoryDialog(
            onDismiss = { showNewCategoryDialog = false },
            onSave = { catName, catIcon, catColor ->
                mutableCategories.add(SubCategoryData(catName, catIcon, catColor))
                selectedCategoryIndex = mutableCategories.lastIndex
                showNewCategoryDialog = false
            }
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(GreenPrimary, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(if(initialName.isNotEmpty()) Icons.Default.Edit else Icons.Default.Add, contentDescription = null, tint = White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(modalTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                        Text("Configura los detalles", fontSize = 14.sp, color = TextGray)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(InputBackground, CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
                // 1. Nombre
                ModalLabel("Nombre del Servicio", Icons.Outlined.Description)
                ModalInput(value = name, onValueChange = { name = it }, placeholder = "Ej: Netflix...")

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Categorías
                ModalLabel("Selecciona Categoría", Icons.Outlined.Category)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(360.dp) // Espacio suficiente para 3 filas
                ) {
                    items(mutableCategories.size) { index ->
                        BigCategoryItem(
                            data = mutableCategories[index],
                            isSelected = selectedCategoryIndex == index,
                            onClick = { selectedCategoryIndex = index },
                            onLongClick = { } // Dejar vacío si no quieres menú aquí
                        )
                    }
                    item {
                        // Botón "Nueva" reutilizando el estilo cuadrado
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .border(1.dp, GreenPrimary, RoundedCornerShape(16.dp))
                                .background(White, RoundedCornerShape(16.dp))
                                .clickable { showNewCategoryDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(modifier = Modifier.size(48.dp).background(InputBackground, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Nueva", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Selección de Tarjeta
                ModalLabel("Método de Pago", Icons.Outlined.CreditCard)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(cards) { card ->
                        SelectableCardItem(
                            card = card,
                            isSelected = selectedCardId == card.id,
                            onClick = {
                                selectedCardId = if(selectedCardId == card.id) "" else card.id
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Monto
                ModalLabel("Monto", Icons.Outlined.AttachMoney)
                Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(InputBackground, RoundedCornerShape(16.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { priceFocusRequester.requestFocus() }.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("$ ", fontWeight = FontWeight.Bold, color = TextGray)
                        Box(modifier = Modifier.weight(1f)) {
                            if (price.isEmpty()) Text("0.00", color = TextGray.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(value = price, onValueChange = { price = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = TextStyle(fontSize = 18.sp, color = TextDark, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth().focusRequester(priceFocusRequester))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Frecuencia
                ModalLabel("Frecuencia", Icons.Outlined.DateRange)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    cycles.forEach { cycle ->
                        val isSelected = selectedCycle == cycle
                        Box(modifier = Modifier.weight(1f).height(45.dp).clip(RoundedCornerShape(12.dp)).background(if (isSelected) GreenPrimary else InputBackground).clickable { selectedCycle = cycle }, contentAlignment = Alignment.Center) {
                            Text(text = cycle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) White else TextGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6. Fecha
                ModalLabel("Próximo Cargo", Icons.Outlined.Event)
                Box(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(InputBackground).clickable { showDatePicker = true }.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedDateDisplay, color = TextDark, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Icon(Icons.Default.CalendarToday, null, tint = TextDark, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Botón Guardar
                Button(
                    onClick = {
                        onSave(
                            name,
                            price,
                            categories[selectedCategoryIndex].name,
                            selectedCycle,
                            selectedDateDisplay,
                            if (selectedCardId.isEmpty()) null else selectedCardId // Pasar el ID de la tarjeta seleccionada
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = name.isNotEmpty() && price.isNotEmpty()
                ) { Text(if(initialName.isNotEmpty()) "Guardar Cambios" else "Crear Suscripción", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- COMPONENTES VISUALES ---

@Composable
fun SelectableCardItem(card: PaymentCard, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(70.dp)
            .border(width = if(isSelected) 3.dp else 0.dp, color = if(isSelected) GreenPrimary else Color.Transparent, shape = RoundedCornerShape(12.dp))
            .padding(if(isSelected) 3.dp else 0.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(card.color, card.color.copy(alpha = 0.7f))))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(card.name, color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if(isSelected) Icon(Icons.Default.CheckCircle, null, tint = White, modifier = Modifier.size(14.dp))
            }
            Text("•••• ${card.last4}", color = White, fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ModalLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
        if (icon != null) { Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(6.dp)) }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
    }
}

@Composable
fun ModalInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(InputBackground, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) Text(placeholder, color = TextGray.copy(alpha = 0.5f), fontSize = 14.sp)
        BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(fontSize = 16.sp, color = TextDark), modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BigCategoryItem(data: SubCategoryData, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) GreenPrimary else InputBackground, shape = RoundedCornerShape(16.dp))
            .background(if (isSelected) GreenPrimary.copy(alpha = 0.05f) else White, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            // Corrección: combinedClickable ahora está bien importado y usado
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.dp).background(data.color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { Icon(data.icon, null, tint = data.color, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.height(8.dp))
            Text(data.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = TextDark)
        }
        if (isSelected) { Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp).background(GreenPrimary, CircleShape).border(2.dp, White, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(12.dp)) } }
    }
}