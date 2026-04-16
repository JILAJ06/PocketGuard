package com.example.pocketguard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pocketguard.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Estructura de datos
data class ExpenseCategoryData(val id: String, val name: String, val icon: ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseModal(
    categories: List<ExpenseCategoryData>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit, // Desc, Monto, Categoria, Fecha
    onCreateCategory: (String, String?, String?) -> Unit = { _, _, _ -> }, // name, colorHex, iconName
    // NUEVOS PARÁMETROS PARA EDICIÓN
    initialExpenseId: String? = null,
    initialDescription: String = "",
    initialAmount: String = "",
    initialCategoryId: String = "",
    initialDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
) {
    var description by remember { mutableStateOf(initialDescription) }
    var amount by remember { mutableStateOf(initialAmount) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    // Preseleccionar categoría si estamos editando
    var selectedCategoryIndex by remember {
        mutableStateOf(
            if (initialCategoryId.isNotEmpty()) {
                categories.indexOfFirst { it.id == initialCategoryId }.takeIf { it >= 0 } ?: 0
            } else 0
        )
    }

    // --- ESTADO PARA NUEVA CATEGORÍA ---
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    val amountFocusRequester = remember { FocusRequester() }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedDateDisplay by remember {
        mutableStateOf(initialDate)
    }

    LaunchedEffect(description, amount, categories, selectedCategoryIndex, selectedDateDisplay) {
        descriptionError = when {
            description.isBlank() -> "La descripción es requerida"
            description.trim().length < 2 -> "La descripción es muy corta"
            description.trim().length > 100 -> "La descripción es muy larga"
            else -> null
        }

        val amountValue = amount.replace(",", ".").toDoubleOrNull()
        amountError = when {
            amount.isBlank() -> "El monto es requerido"
            amountValue == null -> "Ingresa un monto válido"
            amountValue <= 0 -> "El monto debe ser mayor a 0"
            amountValue > 1_000_000 -> "El monto es demasiado alto"
            else -> null
        }

        categoryError = if (categories.getOrNull(selectedCategoryIndex) == null) {
            "Selecciona una categoría"
        } else {
            null
        }

        dateError = try {
            LocalDate.parse(selectedDateDisplay, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            null
        } catch (_: DateTimeParseException) {
            "Selecciona una fecha válida"
        }
    }

    val datePickerState = rememberDatePickerState()

    val isEditMode = initialExpenseId != null
    val modalTitle = if (isEditMode) "Editar Gasto" else "Nuevo Gasto"
    val modalSubtitle = if (isEditMode) "Actualiza la información" else "Registra tu compra"
    val buttonText = if (isEditMode) "Guardar Cambios" else "Agregar Gasto"

    // Calendario Logica
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convertir directamente sin zona horaria
                        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = millis
                        val year = calendar.get(java.util.Calendar.YEAR)
                        val month = calendar.get(java.util.Calendar.MONTH) + 1 // Calendar.MONTH es 0-indexed
                        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        selectedDateDisplay = String.format("%04d-%02d-%02d", year, month, day)
                    }
                    showDatePicker = false
                }) { Text("Aceptar", fontWeight = FontWeight.Bold, color = GreenPrimary) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = TextGray) } },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- DIÁLOGO DE CREAR CATEGORÍA (Integrado) ---
    if (showNewCategoryDialog) {
        NewCategoryDialog(
            onDismiss = { showNewCategoryDialog = false },
            onSave = { name, icon, color ->
                val iconName = com.example.pocketguard.utils.IconMapper.getNameFromIcon(icon)
                onCreateCategory(name, colorToHex(color), iconName)
                showNewCategoryDialog = false
            }
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxSize()) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(GreenPrimary, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(if (isEditMode) Icons.Default.Edit else Icons.Default.Add, contentDescription = null, tint = White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(modalTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(modalSubtitle, fontSize = 14.sp, color = TextGray)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                // 1. Descripción
                ExpenseLabel("Descripción", Icons.Outlined.Description)
                ExpenseInput(value = description, onValueChange = { description = it }, placeholder = "Ej: Café, Uber, Supermercado...")
                if (descriptionError != null && description.isNotEmpty()) {
                    Text(descriptionError!!, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Categorías (Grid 3 columnas + Botón Nueva)
                ExpenseLabel("Selecciona Categoría", Icons.Outlined.Category)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(360.dp)
                ) {
                    items(categories.size) { index ->
                        BigExpenseCategoryItem(
                            data = categories[index],
                            isSelected = selectedCategoryIndex == index,
                            onClick = { selectedCategoryIndex = index }
                        )
                    }
                    // Botón para agregar categoría
                    item {
                        NewCategorySquareButton(onClick = { showNewCategoryDialog = true })
                    }
                }
                if (categoryError != null && categories.isNotEmpty()) {
                    Text(categoryError!!, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Monto
                ExpenseLabel("Monto del Gasto", Icons.Outlined.AttachMoney)
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                        .border(
                            width = 1.dp,
                            color = if (amountError != null && amount.isNotEmpty()) ErrorRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { amountFocusRequester.requestFocus() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("$ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(modifier = Modifier.weight(1f)) {
                            if (amount.isEmpty()) Text("0.00", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(
                                value = amount, onValueChange = { amount = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth().focusRequester(amountFocusRequester)
                            )
                        }
                    }
                }
                if (amountError != null && amount.isNotEmpty()) {
                    Text(amountError!!, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Fecha
                ExpenseLabel("Fecha del Gasto", Icons.Outlined.Event)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = if (dateError != null) ErrorRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedDateDisplay, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                }
                if (dateError != null) {
                    Text(dateError!!, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Botón
                Button(
                    onClick = {
                        val selected = categories.getOrNull(selectedCategoryIndex)
                        if (selected == null || descriptionError != null || amountError != null || dateError != null) return@Button
                        onSave(description.trim(), amount.replace(",", "."), selected.id, selectedDateDisplay)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = descriptionError == null && amountError == null && dateError == null && categories.getOrNull(selectedCategoryIndex) != null
                ) {
                    Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

fun colorToHex(color: Color): String {
    val value = color.toArgb() and 0xFFFFFF
    return String.format("#%06X", value)
}

// ==========================================
// COMPONENTES VISUALES
// ==========================================

@Composable
fun BigExpenseCategoryItem(data: ExpenseCategoryData, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
            .background(color = if (isSelected) GreenPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.dp).background(data.color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(data.icon, null, tint = data.color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(data.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        if (isSelected) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp).background(GreenPrimary, CircleShape).border(2.dp, White, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun NewCategorySquareButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(1.dp, GreenPrimary, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nueva", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
        }
    }
}

// ... (NewCategoryDialog, ExpenseLabel, ExpenseInput se mantienen igual o se reutilizan)
// NOTA: Asegúrate de tener NewCategoryDialog en este archivo o importado. Si falta, cópialo del paso anterior.
@Composable
fun NewCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, ImageVector, Color) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(Icons.Outlined.Coffee) }
    var selectedColor by remember { mutableStateOf(GreenPrimary) }

    val icons = listOf(Icons.Outlined.Coffee, Icons.Outlined.Fastfood, Icons.Outlined.DirectionsCar, Icons.Outlined.Flight, Icons.Outlined.ShoppingCart, Icons.Outlined.CardGiftcard, Icons.Outlined.Checkroom, Icons.Outlined.Movie, Icons.Outlined.FavoriteBorder, Icons.Outlined.Home, Icons.Outlined.Book, Icons.Outlined.Smartphone)
    val colors = listOf(Color(0xFF00A4EF), Color(0xFF2ECC71), Color(0xFFFF9900), Color(0xFF9146FF), Color(0xFFE50914), Color(0xFFE91E63), Color(0xFF34495E), Color(0xFF95A5A6), Color(0xFF1ABC9C))

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nueva Categoría", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(24.dp))
                ExpenseLabel("Nombre")
                ExpenseInput(value = name, onValueChange = { name = it }, placeholder = "Ej: Viajes...")
                Spacer(modifier = Modifier.height(24.dp))
                ExpenseLabel("Icono")
                LazyVerticalGrid(columns = GridCells.Adaptive(40.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(120.dp)) {
                    items(icons.size) { index -> Box(modifier = Modifier.size(40.dp).background(if (selectedIcon == icons[index]) GreenPrimary.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).clickable { selectedIcon = icons[index] }, contentAlignment = Alignment.Center) { Icon(icons[index], null, tint = if(selectedIcon == icons[index]) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant) } }
                }
                Spacer(modifier = Modifier.height(24.dp))
                ExpenseLabel("Color")
                LazyVerticalGrid(columns = GridCells.Adaptive(40.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(100.dp)) {
                    items(colors.size) { index -> Box(modifier = Modifier.size(40.dp).background(colors[index], RoundedCornerShape(8.dp)).clickable { selectedColor = colors[index] }) }
                }
                Spacer(modifier = Modifier.height(30.dp))
                Button(onClick = { onSave(name, selectedIcon, selectedColor) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) { Text("Crear", fontSize = 16.sp) }
            }
        }
    }
}

// ... Auxiliares (ExpenseLabel, ExpenseInput) ...
@Composable
fun ExpenseLabel(text: String, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
        if (icon != null) { Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(6.dp)) }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ExpenseInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(56.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
        )
    }
}