package com.example.pocketguard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
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
data class SubCategoryData(
    val id: String = "", // ID de la categoría
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
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
    onSave: (String, String, String, String, String, String?) -> Unit,
    onCategoryCreated: () -> Unit = {}, // Callback para cuando se crea una categoría
    onCreateCategory: (String, String, String) -> Unit = { _, _, _ -> } // callback para crear en backend: name, colorHex, iconName
) {
    // Estados
    var name by remember { mutableStateOf(initialName) }
    var price by remember { mutableStateOf(initialPrice) }
    var selectedCycle by remember { mutableStateOf(initialCycle) }
    var selectedDateDisplay by remember { mutableStateOf(initialDate) }
    var selectedCardId by remember { mutableStateOf(initialCardId) }
    var isSaving by remember { mutableStateOf(false) }

    // Validaciones
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(name) {
        nameError = when {
            name.isBlank() -> "El nombre es requerido"
            name.length < 2 -> "El nombre es muy corto"
            name.length > 100 -> "El nombre es muy largo"
            else -> null
        }
    }

    LaunchedEffect(price) {
        val priceValue = price.toDoubleOrNull()
        priceError = when {
            price.isBlank() -> "El precio es requerido"
            priceValue == null -> "Ingresa un precio válido"
            priceValue <= 0 -> "El precio debe ser mayor a 0"
            priceValue > 1000000 -> "El precio es demasiado alto"
            else -> null
        }
    }

    // CAMBIO: Usar categories directamente (ya es reactivo desde el parent)
    // Preselección de categoría
    var selectedCategoryIndex by remember(categories, initialCategory) {
        mutableStateOf(
            if (initialCategory.isNotEmpty()) {
                val index = categories.indexOfFirst { it.name == initialCategory }
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
    val modalSubtitle = if (initialName.isNotEmpty()) "Actualiza los detalles" else "Agrega un nuevo servicio"

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
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(selectedDayContainerColor = GreenPrimary, todayDateBorderColor = GreenPrimary, todayContentColor = GreenPrimary))
        }
    }

    // --- DIÁLOGO DE NUEVA CATEGORÍA ---
    if (showNewCategoryDialog) {
        SubNewCategoryDialog(
            onDismiss = {
                showNewCategoryDialog = false
                // Recargar categorías al cerrar el diálogo
                onCategoryCreated()
            },
            onSave = { catName, catIcon, catColor ->
                showNewCategoryDialog = false
                // Recargar categorías después de guardar
                onCategoryCreated()
            },
            onCreateCategoryAPI = { name: String, colorHex: String, iconName: String ->
                // Crear categoría en el backend con iconName
                onCreateCategory(name, colorHex, iconName)
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
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(GreenPrimary, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(if(initialName.isNotEmpty()) Icons.Default.Edit else Icons.Default.Add, contentDescription = null, tint = White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(modalTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(modalSubtitle, fontSize = 13.sp, color = TextGray)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
                // 1. Nombre
                ModalLabel("Nombre del Servicio", Icons.Outlined.Description)
                ModalInput(value = name, onValueChange = { name = it }, placeholder = "Ej: Netflix...")
                if (nameError != null && name.isNotEmpty()) {
                    Text(nameError!!, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Categorías
                ModalLabel("Selecciona Categoría", Icons.Outlined.Category)

                val rows = (categories.size + 1 + 2) / 3
                val gridHeight = (rows * 100).dp

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(gridHeight.coerceAtMost(360.dp))
                ) {
                    items(categories.size) { index ->
                        BigCategoryItem(
                            data = categories[index],
                            isSelected = selectedCategoryIndex == index,
                            onClick = { selectedCategoryIndex = index }
                        )
                    }
                    item {
                        // Botón Nueva
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .border(1.dp, GreenPrimary, RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                .clickable { showNewCategoryDialog = true },
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
                Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)).border(
                    width = if (priceError != null && price.isNotEmpty()) 1.dp else 0.dp,
                    color = if (priceError != null && price.isNotEmpty()) ErrorRed else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { priceFocusRequester.requestFocus() }.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("$ ", fontWeight = FontWeight.Bold, color = TextGray)
                        Box(modifier = Modifier.weight(1f)) {
                            if (price.isEmpty()) Text("0.00", color = TextGray.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            BasicTextField(value = price, onValueChange = { price = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth().focusRequester(priceFocusRequester))
                        }
                    }
                }
                if (priceError != null && price.isNotEmpty()) {
                    Text(priceError!!, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Frecuencia
                ModalLabel("Frecuencia", Icons.Outlined.DateRange)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    cycles.forEach { cycle ->
                        val isSelected = selectedCycle == cycle
                        Box(modifier = Modifier.weight(1f).height(45.dp).clip(RoundedCornerShape(12.dp)).background(if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant).clickable { selectedCycle = cycle }, contentAlignment = Alignment.Center) {
                            Text(text = cycle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) White else TextGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6. Fecha
                ModalLabel("Próximo Cargo", Icons.Outlined.Event)
                Box(modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showDatePicker = true }.padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = selectedDateDisplay, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Botón Guardar
                Button(
                    onClick = {
                        isSaving = true
                        onSave(
                            name,
                            price,
                            categories[selectedCategoryIndex].name,
                            selectedCycle,
                            selectedDateDisplay,
                            if (selectedCardId.isEmpty()) null else selectedCardId
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = !isSaving && name.isNotEmpty() && price.isNotEmpty() && nameError == null && priceError == null
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if(initialName.isNotEmpty()) "Guardar Cambios" else "Crear Suscripción", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- DIÁLOGO NUEVA CATEGORÍA (Diseño Grid) ---
@Composable
fun SubNewCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, ImageVector, Color) -> Unit,
    onCreateCategoryAPI: ((String, String, String) -> Unit)? = null // callback: name, colorHex, iconName
) {
    var catName by remember { mutableStateOf("") }

    val availableIcons = listOf(
        Icons.Outlined.LocalCafe, Icons.Outlined.Fastfood, Icons.Outlined.DirectionsCar, Icons.Outlined.Flight, Icons.Outlined.ShoppingCart,
        Icons.Outlined.CardGiftcard, Icons.Outlined.Checkroom, Icons.Outlined.Movie, Icons.Outlined.FavoriteBorder, Icons.Outlined.Home,
        Icons.Outlined.Book, Icons.Outlined.PhoneAndroid
    )
    var selectedIcon by remember { mutableStateOf(availableIcons[0]) }

    val availableColors = listOf(
        Color(0xFF03A9F4), Color(0xFF2ECC71), Color(0xFFFF9800), Color(0xFF9B59B6), Color(0xFFE74C3C),
        Color(0xFFE91E63), Color(0xFF34495E), Color(0xFF95A5A6), Color(0xFF1ABC9C)
    )
    var selectedColor by remember { mutableStateOf(availableColors[0]) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nueva Categoría", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(20.dp))

                // Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Nombre", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                        if (catName.isEmpty()) Text("Ej: Viajes...", color = TextGray.copy(alpha = 0.5f))
                        BasicTextField(value = catName, onValueChange = { catName = it }, textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp), modifier = Modifier.fillMaxWidth())
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Iconos
                Text("Icono", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(5), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(100.dp)) {
                    items(availableIcons) { icon ->
                        Box(modifier = Modifier.size(40.dp).background(if(selectedIcon == icon) GreenPrimary.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).border(1.dp, if(selectedIcon == icon) GreenPrimary else Color.Transparent, RoundedCornerShape(8.dp)).clickable { selectedIcon = icon }, contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = if(selectedIcon == icon) GreenPrimary else TextGray, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Colores
                Text("Color", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(5), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.height(80.dp)) {
                    items(availableColors) { color ->
                        Box(modifier = Modifier.size(40.dp).background(color, RoundedCornerShape(8.dp)).clickable { selectedColor = color }.border(2.dp, if(selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent, RoundedCornerShape(8.dp)))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Si hay callback de API, usarlo para crear en el backend
                        val hexColor = String.format("#%06X", (selectedColor.toArgb() and 0xFFFFFF))
                        val iconName = com.example.pocketguard.utils.IconMapper.getNameFromIcon(selectedIcon)
                        onCreateCategoryAPI?.invoke(catName, hexColor, iconName)
                        onSave(catName, selectedIcon, selectedColor)
                    },
                    enabled = catName.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Crear", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
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
fun ModalLabel(text: String, icon: ImageVector? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
        if (icon != null) { Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(6.dp)) }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ModalInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) Text(placeholder, color = TextGray.copy(alpha = 0.5f), fontSize = 14.sp)
        BasicTextField(value = value, onValueChange = onValueChange, textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun BigCategoryItem(data: SubCategoryData, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp))
            .background(if (isSelected) GreenPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.dp).background(data.color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) { Icon(data.icon, null, tint = data.color, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.height(8.dp))
            Text(data.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        if (isSelected) { Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(20.dp).background(GreenPrimary, CircleShape).border(2.dp, MaterialTheme.colorScheme.surface, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(12.dp)) } }
    }
}