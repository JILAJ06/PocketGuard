package com.example.pocketguard.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Clase de datos Mock (Ponla aquí arriba o abajo, pero solo una vez)
data class SubscriptionMock(
    val name: String,
    val price: String,
    val color: Color,
    val category: String,
    val date: String,
    val cycle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    subscriptionId: String? = null,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    // 1. BASE DE DATOS SIMULADA
    val mockDatabase = mapOf(
        "1" to SubscriptionMock("Netflix", "199", Color(0xFFE50914), "Entretenimiento", "12 Feb 2026", "Mensual"),
        "2" to SubscriptionMock("Spotify", "115", Color(0xFF1DB954), "Música", "12 Feb 2026", "Mensual"),
        "3" to SubscriptionMock("Amazon Prime", "99", Color(0xFFFF9900), "Compras", "15 Feb 2026", "Mensual"),
        "4" to SubscriptionMock("HBO Max", "149", Color(0xFF9146FF), "Streaming", "09 Feb 2026", "Mensual"),
        "5" to SubscriptionMock("Adobe CC", "599", Color(0xFFFF0000), "Trabajo", "20 Feb 2026", "Mensual"),
        "6" to SubscriptionMock("YouTube Premium", "119", Color(0xFFFF0000), "Video", "18 Feb 2026", "Mensual")
    )

    // 2. ESTADOS DEL FORMULARIO (Declarados UNA SOLA VEZ)
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(GreenPrimary) }
    var category by remember { mutableStateOf("Entretenimiento") }
    var dateDisplay by remember { mutableStateOf("Hoy") }
    var billingCycle by remember { mutableStateOf("Mensual") }

    // Estados de UI (Dropdowns y Modales)
    var showDatePicker by remember { mutableStateOf(false) }
    var showCycleDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    // Listas de Opciones
    val cycles = listOf("Mensual", "Anual", "Semanal", "Trimestral")
    val categories = listOf("Entretenimiento", "Hogar", "Transporte", "Salud", "Educación", "Compras", "Trabajo", "Servicios")
    val availableColors = listOf(
        Color(0xFFE50914), Color(0xFF1DB954), Color(0xFF00A4EF),
        Color(0xFFFF9900), Color(0xFF9146FF), Color(0xFF000000)
    )

    // 3. EFECTO DE CARGA (Si es editar, llenamos los campos)
    LaunchedEffect(subscriptionId) {
        if (subscriptionId != null) {
            val data = mockDatabase[subscriptionId]
            if (data != null) {
                name = data.name
                price = data.price
                selectedColor = data.color
                category = data.category
                dateDisplay = data.date
                billingCycle = data.cycle
            }
        }
    }

    // Títulos dinámicos
    val screenTitle = if (subscriptionId != null) "Editar Suscripción" else "Nueva Suscripción"
    val buttonText = if (subscriptionId != null) "Guardar Cambios" else "Guardar Suscripción"

    // --- LÓGICA DEL CALENDARIO ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            dateDisplay = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        }
                        showDatePicker = false
                    }
                ) { Text("Aceptar", color = GreenPrimary) }
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
                    todayContentColor = GreenPrimary
                )
            )
        }
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold) }, // Título dinámico
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundLight)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // INPUT MONTO
            Text("Monto Mensual", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Spacer(modifier = Modifier.width(8.dp))
                Box {
                    if (price.isEmpty()) Text("0.00", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = TextGray.copy(alpha = 0.3f))
                    androidx.compose.foundation.text.BasicTextField(
                        value = price,
                        onValueChange = { input -> if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) price = input },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.displayLarge.copy(color = TextDark, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 48.sp),
                        modifier = Modifier.width(IntrinsicSize.Min)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // FORMULARIO
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {

                    CleanInput(label = "Nombre del servicio", value = name, onValueChange = { name = it }, placeholder = "Ej. Netflix")

                    // SELECTOR CATEGORÍA
                    Box {
                        CleanSelector(label = "Categoría", value = category, icon = Icons.Default.Category, onClick = { showCategoryDropdown = true })
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.background(White).heightIn(max = 250.dp)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; showCategoryDropdown = false })
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // FECHA
                        Box(modifier = Modifier.weight(1f)) {
                            CleanSelector(label = "Primer pago", value = dateDisplay, icon = Icons.Default.CalendarToday, onClick = { showDatePicker = true })
                        }
                        // CICLO
                        Box(modifier = Modifier.weight(1f)) {
                            CleanSelector(label = "Ciclo", value = billingCycle, icon = Icons.Default.KeyboardArrowDown, onClick = { showCycleDropdown = true })
                            DropdownMenu(
                                expanded = showCycleDropdown,
                                onDismissRequest = { showCycleDropdown = false },
                                modifier = Modifier.background(White)
                            ) {
                                cycles.forEach { cycle ->
                                    DropdownMenuItem(text = { Text(cycle) }, onClick = { billingCycle = cycle; showCycleDropdown = false })
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // COLOR
            Text("Color de etiqueta", style = MaterialTheme.typography.labelLarge, color = TextGray, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                availableColors.forEach { color ->
                    ColorSelectionCircle(color = color, isSelected = color == selectedColor, onClick = { selectedColor = color })
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // BOTÓN GUARDAR
            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                enabled = name.isNotEmpty() && price.isNotEmpty()
            ) {
                Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold) // Texto dinámico
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun CleanInput(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column {
        Text(label, fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextGray.copy(alpha = 0.5f), fontSize = 15.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = InputBackground, unfocusedContainerColor = InputBackground,
                disabledContainerColor = InputBackground, focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun CleanSelector(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column {
        Text(label, fontSize = 13.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(InputBackground)
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(value, color = TextDark, fontSize = 14.sp, maxLines = 1)
            Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ColorSelectionCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(modifier = Modifier.fillMaxSize().border(2.dp, White, CircleShape).padding(2.dp))
            Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(24.dp))
        }
    }
}