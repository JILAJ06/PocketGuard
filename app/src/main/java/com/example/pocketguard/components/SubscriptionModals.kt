package com.example.pocketguard.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.example.pocketguard.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class SubCategoryData(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NewSubscriptionModal(
    // Parámetros para EDITAR (Opcionales)
    initialName: String = "",
    initialPrice: String = "",
    initialCategory: String = "",
    initialCycle: String = "Mensual",
    initialDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),

    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    // --- ESTADOS INICIALIZADOS CON LOS PARÁMETROS ---
    var name by remember { mutableStateOf(initialName) }
    var price by remember { mutableStateOf(initialPrice) }
    var selectedCycle by remember { mutableStateOf(initialCycle) }
    var selectedDateDisplay by remember { mutableStateOf(initialDate) }

    // --- LISTA DE CATEGORÍAS ---
    val categories = remember {
        mutableStateListOf(
            SubCategoryData("Alimentos", Icons.Outlined.Fastfood, Color(0xFFFFA500)),
            SubCategoryData("Transporte", Icons.Outlined.DirectionsCar, Color(0xFF2ECC71)),
            SubCategoryData("Compras", Icons.Outlined.ShoppingBag, Color(0xFF9146FF)),
            SubCategoryData("Hogar", Icons.Outlined.Home, Color(0xFF00A4EF)),
            SubCategoryData("Entretenimiento", Icons.Outlined.Movie, Color(0xFFE74C3C)),
            SubCategoryData("Salud", Icons.Outlined.FavoriteBorder, Color(0xFFE91E63)),
            SubCategoryData("Educación", Icons.Outlined.School, Color(0xFF34495E)),
            SubCategoryData("Otros", Icons.Outlined.MoreHoriz, Color(0xFF95A5A6))
        )
    }

    // --- LÓGICA PARA ENCONTRAR LA CATEGORÍA INICIAL ---
    // Si estamos editando, buscamos el índice de la categoría que tenía guardada
    var selectedCategoryIndex by remember {
        mutableStateOf(
            if (initialCategory.isNotEmpty()) {
                val index = categories.indexOfFirst { it.name == initialCategory }
                if (index != -1) index else 0
            } else 0
        )
    }

    // Estados de UI
    var menuExpandedIndex by remember { mutableStateOf(-1) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEditIndex by remember { mutableStateOf(-1) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var categoryToDeleteIndex by remember { mutableStateOf(-1) }

    val priceFocusRequester = remember { FocusRequester() }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val scrollState = rememberScrollState()
    val cycles = listOf("Diario", "Semanal", "Mensual", "Anual")

    // Titulo dinámico
    val modalTitle = if (initialName.isNotEmpty()) "Editar Suscripción" else "Nueva Suscripción"
    val buttonText = if (initialName.isNotEmpty()) "Guardar Cambios" else "Guardar Suscripción"

    // --- LÓGICA CALENDARIO ---
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

    // --- DIÁLOGOS DE CATEGORÍA ---
    if (showCategoryDialog) {
        val isEditing = categoryToEditIndex != -1
        val initialData = if (isEditing) categories[categoryToEditIndex] else null
        CategoryFormDialog(
            initialName = initialData?.name ?: "",
            initialIcon = initialData?.icon,
            initialColor = initialData?.color,
            isEditing = isEditing,
            onDismiss = { showCategoryDialog = false; categoryToEditIndex = -1 },
            onSave = { catName, catIcon, catColor ->
                if (isEditing) categories[categoryToEditIndex] = SubCategoryData(catName, catIcon, catColor)
                else { categories.add(SubCategoryData(catName, catIcon, catColor)); selectedCategoryIndex = categories.lastIndex }
                showCategoryDialog = false; categoryToEditIndex = -1
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = White,
            icon = { Icon(Icons.Outlined.Delete, null, tint = ErrorRed) },
            title = { Text("Eliminar Categoría", fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text("¿Estás seguro? Esta acción no se puede deshacer.", color = TextGray) },
            confirmButton = {
                Button(onClick = {
                    if (categoryToDeleteIndex != -1) {
                        if (selectedCategoryIndex == categoryToDeleteIndex) selectedCategoryIndex = 0
                        else if (selectedCategoryIndex > categoryToDeleteIndex) selectedCategoryIndex--
                        categories.removeAt(categoryToDeleteIndex)
                    }
                    showDeleteConfirmDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("Eliminar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancelar", color = TextDark) } }
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
                        Text(if(initialName.isNotEmpty()) "Modifica tu servicio" else "Agrega un nuevo servicio", fontSize = 14.sp, color = TextGray)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(InputBackground, CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
                // Nombre
                ModalLabel("Nombre del Servicio", Icons.Outlined.Description)
                ModalInput(value = name, onValueChange = { name = it }, placeholder = "Ej: Netflix, Spotify...")

                Spacer(modifier = Modifier.height(24.dp))

                // Categorías
                ModalLabel("Selecciona Categoría", Icons.Outlined.Category)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(360.dp)
                ) {
                    items(categories.size) { index ->
                        Box {
                            BigCategoryItem(
                                data = categories[index],
                                isSelected = selectedCategoryIndex == index,
                                onClick = { selectedCategoryIndex = index },
                                onLongClick = { menuExpandedIndex = index }
                            )
                            DropdownMenu(
                                expanded = menuExpandedIndex == index,
                                onDismissRequest = { menuExpandedIndex = -1 },
                                modifier = Modifier.background(White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar", color = TextDark) },
                                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = GreenPrimary) },
                                    onClick = { menuExpandedIndex = -1; categoryToEditIndex = index; showCategoryDialog = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = ErrorRed) },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) },
                                    onClick = { menuExpandedIndex = -1; categoryToDeleteIndex = index; showDeleteConfirmDialog = true }
                                )
                            }
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .border(1.dp, GreenPrimary, RoundedCornerShape(16.dp))
                                .background(White, RoundedCornerShape(16.dp))
                                .clickable { categoryToEditIndex = -1; showCategoryDialog = true },
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

                // Monto
                ModalLabel("Monto", Icons.Outlined.AttachMoney)
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp).background(InputBackground, RoundedCornerShape(16.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { priceFocusRequester.requestFocus() }.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("$ ", fontWeight = FontWeight.Bold, color = TextGray)
                        Box(modifier = Modifier.weight(1f)) {
                            if (price.isEmpty()) Text("0.00", color = TextGray.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(value = price, onValueChange = { price = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = TextStyle(fontSize = 18.sp, color = TextDark, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth().focusRequester(priceFocusRequester))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Frecuencia
                ModalLabel("Frecuencia de Pago", Icons.Outlined.DateRange)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    cycles.forEach { cycle ->
                        val isSelected = selectedCycle == cycle
                        Box(
                            modifier = Modifier.weight(1f).height(45.dp).shadow(if(isSelected) 4.dp else 0.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(if (isSelected) GreenPrimary else White).border(width = if (isSelected) 0.dp else 1.dp, color = if (isSelected) Color.Transparent else Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)).clickable { selectedCycle = cycle },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = cycle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) White else TextGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Fecha
                ModalLabel("Próxima Fecha de Cargo", Icons.Outlined.Event)
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(InputBackground).clickable { showDatePicker = true }.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedDateDisplay, color = TextDark, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Icon(Icons.Default.CalendarToday, null, tint = TextDark, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Botón Guardar
                Button(
                    onClick = { onSave(name, price, categories[selectedCategoryIndex].name, selectedCycle, selectedDateDisplay) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, disabledContainerColor = GreenPrimary.copy(alpha = 0.5f)),
                    enabled = name.isNotEmpty() && price.isNotEmpty()
                ) { Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ... (Los Componentes Auxiliares BigCategoryItem, CategoryFormDialog, etc. se mantienen igual) ...
// Asegúrate de tener los componentes auxiliares que definimos en el mensaje anterior aquí abajo.

// ==========================================
// COMPONENTES AUXILIARES
// ==========================================

@Composable
fun ModalLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
        if (icon != null) {
            Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(6.dp))
        }
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
            // Usamos combinedClickable para detectar el click largo
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.dp).background(data.color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(data.icon, null, tint = data.color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(data.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = TextDark)
        }
        if (isSelected) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp).background(GreenPrimary, CircleShape).border(2.dp, White, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

// ==========================================
// DIÁLOGO PARA CREAR / EDITAR CATEGORÍA
// ==========================================
@Composable
fun CategoryFormDialog(
    initialName: String = "",
    initialIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    initialColor: Color? = null,
    isEditing: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, androidx.compose.ui.graphics.vector.ImageVector, Color) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedIcon by remember { mutableStateOf(initialIcon ?: Icons.Outlined.Coffee) }
    var selectedColor by remember { mutableStateOf(initialColor ?: GreenPrimary) }

    val icons = listOf(Icons.Outlined.Coffee, Icons.Outlined.Fastfood, Icons.Outlined.DirectionsCar, Icons.Outlined.Flight, Icons.Outlined.ShoppingCart, Icons.Outlined.CardGiftcard, Icons.Outlined.Checkroom, Icons.Outlined.Movie, Icons.Outlined.FavoriteBorder, Icons.Outlined.Home, Icons.Outlined.Book, Icons.Outlined.Smartphone, Icons.Outlined.Pets, Icons.Outlined.SportsSoccer, Icons.Outlined.LocalHospital)
    val colors = listOf(Color(0xFF00A4EF), Color(0xFF2ECC71), Color(0xFFFF9900), Color(0xFF9146FF), Color(0xFFE50914), Color(0xFFE91E63), Color(0xFF34495E), Color(0xFF95A5A6), Color(0xFF1ABC9C))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().padding(4.dp).heightIn(min = 550.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).background(if(isEditing) Color(0xFFFFA500) else GreenPrimary, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(if(isEditing) Icons.Default.Edit else Icons.Default.AutoAwesome, null, tint = White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(if(isEditing) "Editar Categoría" else "Nueva Categoría", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                            Text("Personaliza tu estilo", fontSize = 13.sp, color = TextGray)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.background(InputBackground, CircleShape)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    ModalLabel("Nombre")
                    ModalInput(value = name, onValueChange = { name = it }, placeholder = "Ej: Viajes...")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    ModalLabel("Icono")
                    LazyVerticalGrid(columns = GridCells.Adaptive(48.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.height(200.dp)) {
                        items(icons.size) { index ->
                            Box(modifier = Modifier.size(48.dp).border(1.dp, if (selectedIcon == icons[index]) GreenPrimary else InputBackground, RoundedCornerShape(12.dp)).background(if (selectedIcon == icons[index]) GreenPrimary.copy(alpha = 0.1f) else White, RoundedCornerShape(12.dp)).clickable { selectedIcon = icons[index] }, contentAlignment = Alignment.Center) {
                                Icon(icons[index], null, tint = if(selectedIcon == icons[index]) GreenPrimary else TextGray, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    ModalLabel("Color")
                    LazyVerticalGrid(columns = GridCells.Adaptive(42.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.height(100.dp)) {
                        items(colors.size) { index ->
                            Box(modifier = Modifier.size(42.dp).background(colors[index], RoundedCornerShape(12.dp)).clickable { selectedColor = colors[index] }, contentAlignment = Alignment.Center) {
                                if (selectedColor == colors[index]) Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = { onSave(name, selectedIcon, selectedColor) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if(isEditing) Color(0xFFFFA500) else GreenPrimary),
                    enabled = name.isNotEmpty()
                ) { Text(if(isEditing) "Guardar Cambios" else "Crear Categoría", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}