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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pocketguard.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ==========================================
// COMPONENTES AUXILIARES
// ==========================================

data class CategoryData(val name: String, val icon: ImageVector, val color: Color)

@Composable
fun ExpenseLabel(text: String, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
        if (icon != null) {
            Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
    }
}

@Composable
fun ExpenseInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(InputBackground, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) Text(placeholder, color = TextGray.copy(alpha = 0.5f), fontSize = 14.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = TextDark),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CategoryItem(data: CategoryData, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    ) {
        Box(
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) GreenPrimary else InputBackground,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(
                        color = data.color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = data.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .background(GreenPrimary, CircleShape)
                        .border(2.dp, White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = data.name,
            fontSize = 10.sp,
            color = if (isSelected) GreenPrimary else TextGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NewCategoryButton(onClick: () -> Unit) {
    val stroke = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(64.dp).padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(color = GreenPrimary, style = stroke, cornerRadius = CornerRadius(16.dp.toPx()))
            }
            Icon(Icons.Default.Add, contentDescription = null, tint = GreenPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Nueva", fontSize = 10.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun IconSelectorItem(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    // Aumentamos un poco el tamaño del selector de icono (de 40dp a 48dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, if (isSelected) GreenPrimary else InputBackground, RoundedCornerShape(12.dp))
            .background(if (isSelected) GreenPrimary.copy(alpha = 0.1f) else White, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = if(isSelected) GreenPrimary else TextGray, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun ColorSelectorItem(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    // Aumentamos el tamaño de la burbuja de color (de 36dp a 42dp)
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(color, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(24.dp))
        }
    }
}

// ==========================================
// 1. MODAL DE NUEVA CATEGORÍA (AMPLIADO)
// ==========================================
@Composable
fun NewCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, ImageVector, Color) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(Icons.Outlined.Coffee) }
    var selectedColor by remember { mutableStateOf(GreenPrimary) }

    val icons = listOf(Icons.Outlined.Coffee, Icons.Outlined.Fastfood, Icons.Outlined.DirectionsCar, Icons.Outlined.Flight, Icons.Outlined.ShoppingCart, Icons.Outlined.CardGiftcard, Icons.Outlined.Checkroom, Icons.Outlined.Movie, Icons.Outlined.FavoriteBorder, Icons.Outlined.Home, Icons.Outlined.Book, Icons.Outlined.Smartphone, Icons.Outlined.Pets, Icons.Outlined.SportsSoccer, Icons.Outlined.LocalHospital)
    val colors = listOf(Color(0xFF00A4EF), Color(0xFF2ECC71), Color(0xFFFF9900), Color(0xFF9146FF), Color(0xFFE50914), Color(0xFFE91E63), Color(0xFF34495E), Color(0xFF95A5A6), Color(0xFF1ABC9C))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(28.dp),
            // Hacemos el modal más ancho y alto
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp) // Reducimos el padding externo para ganar ancho
                .heightIn(min = 550.dp) // Altura mínima forzada para que no se vea aplastado
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()), // Agregamos scroll por si acaso en pantallas pequeñas
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).background(GreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Nueva Categoría", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                            Text("Personaliza tu estilo", fontSize = 13.sp, color = TextGray)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.background(InputBackground, CircleShape)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Input Nombre
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    ExpenseLabel("Nombre de la Categoría")
                    ExpenseInput(value = name, onValueChange = { name = it }, placeholder = "Ej: Mascotas, Deportes...")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selector Iconos (Más grande)
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    ExpenseLabel("Selecciona un Icono")
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(48.dp), // Celdas un poco más grandes
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.height(200.dp) // AUMENTADO de 120dp a 200dp
                    ) {
                        items(icons.size) { index ->
                            IconSelectorItem(icons[index], selectedIcon == icons[index]) { selectedIcon = icons[index] }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selector Colores (Más grande)
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    ExpenseLabel("Elige un Color")
                    // Usamos FlowRow o una Grid para que no se corten
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(42.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.height(100.dp)
                    ) {
                        items(colors.size) { index ->
                            ColorSelectorItem(colors[index], selectedColor == colors[index]) { selectedColor = colors[index] }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Botón Crear
                Button(
                    onClick = { onSave(name, selectedIcon, selectedColor) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = name.isNotEmpty()
                ) {
                    Text("Crear Categoría", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 2. MODAL DE NUEVO GASTO
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseModal(
    onDismiss: () -> Unit,
    onNewCategoryClick: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    val amountFocusRequester = remember { FocusRequester() }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedDateDisplay by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
    }

    val datePickerState = rememberDatePickerState()

    val categories = listOf(
        CategoryData("Alimentos", Icons.Outlined.Fastfood, Color(0xFFFFA500)),
        CategoryData("Transporte", Icons.Outlined.DirectionsCar, Color(0xFF2ECC71)),
        CategoryData("Compras", Icons.Outlined.ShoppingBag, Color(0xFF9146FF)),
        CategoryData("Hogar", Icons.Outlined.Home, Color(0xFF00A4EF)),
        CategoryData("Ocio", Icons.Outlined.Movie, Color(0xFFE74C3C)),
        CategoryData("Salud", Icons.Outlined.FavoriteBorder, Color(0xFFE91E63)),
        CategoryData("Educación", Icons.Outlined.School, Color(0xFF34495E)),
        CategoryData("Otros", Icons.Outlined.MoreHoriz, Color(0xFF95A5A6))
    )

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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxSize()) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(GreenPrimary, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column { Text("Nuevo Gasto", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark); Text("Registra tu compra", fontSize = 14.sp, color = TextGray) }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(InputBackground, CircleShape)) { Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray) }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                // Descripción
                ExpenseLabel("Descripción", Icons.Outlined.Description)
                ExpenseInput(value = description, onValueChange = { description = it }, placeholder = "Ej: Café con amigos, Uber...")

                Spacer(modifier = Modifier.height(24.dp))

                // Categorías
                ExpenseLabel("Selecciona Categoría", Icons.Outlined.Category)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(categories.size) { index ->
                        CategoryItem(data = categories[index], isSelected = selectedCategoryIndex == index, onClick = { selectedCategoryIndex = index })
                    }
                    item { NewCategoryButton(onClick = onNewCategoryClick) }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Monto
                ExpenseLabel("Monto del Gasto", Icons.Outlined.AttachMoney)
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp).background(InputBackground, RoundedCornerShape(16.dp))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { amountFocusRequester.requestFocus() }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("$ ", fontWeight = FontWeight.Bold, color = TextGray)
                        Box(modifier = Modifier.weight(1f)) {
                            if (amount.isEmpty()) Text("0.00", color = TextGray.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(
                                value = amount, onValueChange = { amount = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(fontSize = 18.sp, color = TextDark, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth().focusRequester(amountFocusRequester)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Fecha
                ExpenseLabel("Fecha del Gasto", Icons.Outlined.Event)
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(InputBackground)
                        .clickable { showDatePicker = true }.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedDateDisplay, color = TextDark, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Icon(Icons.Default.CalendarToday, null, tint = TextDark, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Botón
                Button(
                    onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = amount.isNotEmpty() && description.isNotEmpty()
                ) {
                    Text("Agregar Gasto", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}