package com.example.pocketguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    subscriptionId: String? = null,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    // --- ESTADOS DEL FORMULARIO ---
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(BrandNetflix) } // Color por defecto
    var category by remember { mutableStateOf("Entretenimiento") }
    var dateDisplay by remember { mutableStateOf("Seleccionar fecha") }
    var billingCycle by remember { mutableStateOf("Mensual") }

    // Estados de UI (Dropdowns y Modales)
    var showDatePicker by remember { mutableStateOf(false) }
    var showCycleDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Listas de Opciones
    val cycles = listOf("Mensual", "Anual", "Semanal", "Trimestral")
    val categories = listOf("Entretenimiento", "Música", "Hogar", "Transporte", "Salud", "Educación", "Compras", "Trabajo", "Otros")
    // Usamos los colores de marca definidos en Color.kt
    val availableColors = listOf(BrandNetflix, BrandSpotify, BrandAmazon, BrandHBO, BrandDisney, BrandApple, GreenPrimary)

    // Títulos dinámicos
    val screenTitle = if (subscriptionId != null) "Editar Suscripción" else "Nueva Suscripción"
    val buttonText = if (subscriptionId != null) "Guardar Cambios" else "Guardar Suscripción"

    // --- LÓGICA DEL CALENDARIO ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        dateDisplay = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    }
                    showDatePicker = false
                }) { Text("Aceptar", color = GreenPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = TextGray) }
            },
            colors = DatePickerDefaults.colors(containerColor = White)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = GreenPrimary,
                    todayDateBorderColor = GreenPrimary,
                    todayContentColor = GreenPrimary,
                    currentYearContentColor = GreenPrimary,
                    selectedYearContainerColor = GreenPrimary
                )
            )
        }
    }

    Scaffold(
        containerColor = White, // Fondo blanco para el formulario
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.background(InputBackground, CircleShape)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // 1. INPUT PRECIO GRANDE
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Monto del pago", color = TextGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box {
                        if (price.isEmpty()) Text("0.00", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = TextGray.copy(alpha = 0.3f))
                        BasicTextField(
                            value = price,
                            onValueChange = { input -> if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) price = input },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(color = TextDark, fontWeight = FontWeight.Bold, fontSize = 48.sp),
                            modifier = Modifier.width(IntrinsicSize.Min)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 2. CAMPOS DEL FORMULARIO
            FormLabel("Nombre del servicio")
            FormInput(value = name, onValueChange = { name = it }, placeholder = "Ej. Netflix, Spotify...")

            Spacer(modifier = Modifier.height(24.dp))

            FormLabel("Categoría")
            Box {
                FormSelector(value = category, icon = Icons.Default.Category, onClick = { showCategoryDropdown = true })
                CustomDropdown(expanded = showCategoryDropdown, onDismiss = { showCategoryDropdown = false }, items = categories) { category = it }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Ciclo de cobro")
                    Box {
                        FormSelector(value = billingCycle, icon = Icons.Default.Repeat, onClick = { showCycleDropdown = true })
                        CustomDropdown(expanded = showCycleDropdown, onDismiss = { showCycleDropdown = false }, items = cycles) { billingCycle = it }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Próximo pago")
                    FormSelector(value = dateDisplay, icon = Icons.Default.CalendarToday, onClick = { showDatePicker = true })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. SELECTOR DE COLOR
            FormLabel("Color de marca")
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                availableColors.forEach { color ->
                    ColorCircle(color = color, isSelected = color == selectedColor, onClick = { selectedColor = color })
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón al final
            Spacer(modifier = Modifier.height(40.dp))

            // 4. BOTÓN GUARDAR
            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.5f)
                ),
                enabled = name.isNotEmpty() && price.isNotEmpty() && dateDisplay != "Seleccionar fecha"
            ) {
                Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// COMPONENTES DEL FORMULARIO
// ==========================================

@Composable
fun FormLabel(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark, modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
fun FormInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
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
            textStyle = TextStyle(fontSize = 16.sp, color = TextDark, fontWeight = FontWeight.Medium),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FormSelector(value: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(InputBackground)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(value, color = if(value.startsWith("Seleccionar")) TextGray.copy(alpha = 0.5f) else TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun CustomDropdown(expanded: Boolean, onDismiss: () -> Unit, items: List<String>, onSelected: (String) -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(White).width(200.dp).heightIn(max = 250.dp)
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item, color = TextDark) },
                onClick = { onSelected(item); onDismiss() },
                colors = MenuDefaults.itemColors(textColor = TextDark)
            )
        }
    }
}

@Composable
fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, White, CircleShape)
                    .padding(3.dp)
            )
            Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
        }
    }
}