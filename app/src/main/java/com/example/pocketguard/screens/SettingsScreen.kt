package com.example.pocketguard.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketguard.components.AddCardDialog
import com.example.pocketguard.components.MiniCreditCard
import com.example.pocketguard.components.PaymentCard
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.BanksViewModel
import com.example.pocketguard.presentation.viewmodel.CardsViewModel
import com.example.pocketguard.presentation.viewmodel.PreferencesViewModel
import com.example.pocketguard.ui.theme.*
import java.util.UUID

// Modelo local para categorías en Configuración
data class SettingsCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val isDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onAuthExpired: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // --- ViewModels Existentes (NO MODIFICADOS) ---
    val preferencesViewModel: PreferencesViewModel = viewModel(factory = ServiceLocator.getPreferencesViewModelFactory())
    val cardsViewModel: CardsViewModel = viewModel(factory = ServiceLocator.getCardsViewModelFactory())
    val banksViewModel: BanksViewModel = viewModel(factory = ServiceLocator.getBanksViewModelFactory())

    val state by preferencesViewModel.state.collectAsStateWithLifecycle()
    val cardsState by cardsViewModel.state.collectAsStateWithLifecycle()
    val banksState by banksViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Estados Locales para Nuevas Funcionalidades ---
    var monthlyIncome by remember { mutableStateOf("15000") } // Estado para Ingresos
    var showSaveButton by remember { mutableStateOf(false) } // Controla visibilidad del botón guardar

    // Lista de Categorías (Mock Inicial + Estado Dinámico)
    val categoriesList = remember {
        mutableStateListOf(
            SettingsCategory(name = "Alimentos", icon = Icons.Outlined.Fastfood, color = Color(0xFFF1C40F), isDefault = true),
            SettingsCategory(name = "Transporte", icon = Icons.Outlined.DirectionsCar, color = Color(0xFF2ECC71), isDefault = true),
            SettingsCategory(name = "Hogar", icon = Icons.Outlined.Home, color = Color(0xFF3498DB), isDefault = true),
            SettingsCategory(name = "Personal", icon = Icons.Outlined.Person, color = Color(0xFF9B59B6), isDefault = false) // Ejemplo de creada por usuario
        )
    }

    // Estados para el formulario inline de Categorías
    var isAddingCategory by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var newCatIcon by remember { mutableStateOf(Icons.Outlined.LocalCafe) }
    var newCatColor by remember { mutableStateOf(Color(0xFF03A9F4)) }

    // --- Lógica de Carga (Existente) ---
    LaunchedEffect(Unit) {
        preferencesViewModel.loadPreferences()
        cardsViewModel.loadCards()
        banksViewModel.loadBanks()
    }

    LaunchedEffect(state.isUnauthorized || cardsState.isUnauthorized || banksState.isUnauthorized) {
        if (state.isUnauthorized || cardsState.isUnauthorized || banksState.isUnauthorized) {
            onAuthExpired()
        }
    }

    var selectedTheme by remember { mutableStateOf("system") }
    var selectedLanguage by remember { mutableStateOf("es") }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.preferences) {
        state.preferences?.let { prefs ->
            selectedTheme = prefs.theme
            selectedLanguage = prefs.language
        }
    }

    // --- Manejo de Cards (Existente) ---
    val cards = remember(cardsState.cards) {
        cardsState.cards.map { card ->
            PaymentCard(
                id = card.card_id,
                name = if (card.alias.isNotEmpty()) card.alias else card.bank_name,
                last4 = card.last_4_digits ?: "",
                color = try { Color(android.graphics.Color.parseColor(card.color_hex)) } catch (e: Exception) { Color(0xFF4A90E2) }
            )
        }
    }
    val banks = remember(banksState.banks) { banksState.banks.map { it.name } }

    if (showAddCardDialog) {
        AddCardDialog(
            banks = banks,
            onDismiss = { showAddCardDialog = false },
            onSave = { bankName, alias, digits, colorHex ->
                cardsViewModel.createCard(bankName, alias, digits, colorHex, false)
                showAddCardDialog = false
            }
        )
    }

    // --- UI Principal ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configuración", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        floatingActionButton = {
            // NUEVO: Botón Flotante para Guardar Cambios Generales
            AnimatedVisibility(
                visible = showSaveButton || monthlyIncome != "15000", // Aparece si hay cambios (simulado)
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // Aquí iría la lógica para guardar Ingresos y Categorías en el Backend
                        // preferencesViewModel.saveIncome(monthlyIncome)
                        showSaveButton = false
                        Log.d("Settings", "Guardando cambios: Ingreso=$monthlyIncome, Cats=${categoriesList.size}")
                    },
                    containerColor = GreenPrimary,
                    contentColor = White,
                    icon = { Icon(Icons.Default.Save, null) },
                    text = { Text("Guardar Cambios") }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. SECCIÓN FINANZAS (NUEVA)
            SectionHeader("Finanzas")

            // Tarjeta de Ingresos
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE8F5E9)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AttachMoney, null, tint = GreenPrimary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Ingreso Mensual", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextDark)
                            Text("Base para tu presupuesto", fontSize = 12.sp, color = TextGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Input de Ingreso
                    OutlinedTextField(
                        value = monthlyIncome,
                        onValueChange = {
                            monthlyIncome = it
                            showSaveButton = true
                        },
                        label = { Text("Monto") },
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold, color = TextDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            // 2. SECCIÓN CATEGORÍAS (NUEVA IMPLEMENTACIÓN INLINE)
            // Tarjeta de Gestión de Categorías
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Categorías", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)

                        // Botón Toggle para Agregar
                        IconButton(
                            onClick = { isAddingCategory = !isAddingCategory },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if(isAddingCategory) ErrorRed.copy(0.1f) else GreenPrimary.copy(0.1f),
                                contentColor = if(isAddingCategory) ErrorRed else GreenPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if(isAddingCategory) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Toggle Add"
                            )
                        }
                    }

                    // --- FORMULARIO INLINE (Diseño del Modal) ---
                    AnimatedVisibility(visible = isAddingCategory) {
                        Column(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                                .border(1.dp, GreenPrimary.copy(0.3f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text("Nueva Categoría", fontWeight = FontWeight.Bold, color = TextDark)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Input Nombre
                            BasicTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(White, RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (newCatName.isEmpty()) Text("Nombre...", color = TextGray.copy(0.5f))
                                        innerTextField()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Icono", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid Iconos
                            val icons = listOf(Icons.Outlined.Star, Icons.Outlined.Face, Icons.Outlined.Pets, Icons.Outlined.Work, Icons.Outlined.Flight)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(icons) { icon ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(if(newCatIcon == icon) GreenPrimary.copy(0.2f) else White, RoundedCornerShape(8.dp))
                                            .border(1.dp, if(newCatIcon == icon) GreenPrimary else Color.LightGray, RoundedCornerShape(8.dp))
                                            .clickable { newCatIcon = icon },
                                        contentAlignment = Alignment.Center
                                    ) { Icon(icon, null, tint = if(newCatIcon == icon) GreenPrimary else TextGray) }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Color", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid Colores
                            val colors = listOf(Color(0xFFE74C3C), Color(0xFF9B59B6), Color(0xFF2ECC71), Color(0xFF3498DB), Color(0xFFF1C40F))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(colors) { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(color, CircleShape)
                                            .border(2.dp, if(newCatColor == color) TextDark else Color.Transparent, CircleShape)
                                            .clickable { newCatColor = color }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if(newCatName.isNotEmpty()){
                                        categoriesList.add(SettingsCategory(name = newCatName, icon = newCatIcon, color = newCatColor, isDefault = false))
                                        newCatName = ""
                                        isAddingCategory = false
                                        showSaveButton = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                enabled = newCatName.isNotEmpty()
                            ) { Text("Crear Categoría", fontWeight = FontWeight.Bold) }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de Categorías Existentes
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(categoriesList) { cat ->
                            var showMenu by remember { mutableStateOf(false) }

                            // Item de Categoría
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .background(Color(0xFFF5F6FA), RoundedCornerShape(12.dp))
                                    .clickable { showMenu = true } // Al hacer click muestra opciones
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(cat.icon, null, tint = cat.color, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(cat.name, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, color = TextDark)
                                }

                                // Menú Contextual (Solo para Usuario)
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    if (cat.isDefault) {
                                        DropdownMenuItem(
                                            text = { Text("Por defecto (No editable)", color = TextGray) },
                                            onClick = { showMenu = false },
                                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextGray) }
                                        )
                                    } else {
                                        DropdownMenuItem(
                                            text = { Text("Eliminar") },
                                            onClick = {
                                                categoriesList.remove(cat)
                                                showMenu = false
                                                showSaveButton = true
                                            },
                                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) },
                                            colors = MenuDefaults.itemColors(textColor = ErrorRed)
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Editar") },
                                            onClick = {
                                                // Aquí cargarías los datos en el form
                                                newCatName = cat.name
                                                newCatIcon = cat.icon
                                                newCatColor = cat.color
                                                isAddingCategory = true
                                                showMenu = false
                                            },
                                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = TextDark) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. APARIENCIA (EXISTENTE)
            SectionHeader("Apariencia")
            UserSettingsCard(
                icon = Icons.Default.Palette,
                iconColor = Color(0xFF9C27B0),
                iconBackground = Color(0xFFF3E5F5),
                title = "Tema de la aplicación",
                subtitle = when (selectedTheme) { "system" -> "Automático"; "light" -> "Claro"; "dark" -> "Oscuro"; else -> selectedTheme }
            ) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) { Text("Cambiar", color = GreenPrimary, fontWeight = FontWeight.Bold) }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("system" to "Automático", "light" to "Claro", "dark" to "Oscuro").forEach { (code, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { selectedTheme = code; preferencesViewModel.updatePreferences(theme = code); expanded = false }, leadingIcon = if (selectedTheme == code) { { Icon(Icons.Default.Check, null, tint = GreenPrimary) } } else null)
                        }
                    }
                }
            }

            UserSettingsCard(
                icon = Icons.Default.Language,
                iconColor = Color(0xFF2196F3),
                iconBackground = Color(0xFFE3F2FD),
                title = "Idioma",
                subtitle = if (selectedLanguage == "es") "Español" else "English"
            ) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) { Text("Cambiar", color = GreenPrimary, fontWeight = FontWeight.Bold) }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("es" to "Español", "en" to "English").forEach { (code, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { selectedLanguage = code; preferencesViewModel.updatePreferences(language = code); expanded = false }, leadingIcon = if (selectedLanguage == code) { { Icon(Icons.Default.Check, null, tint = GreenPrimary) } } else null)
                        }
                    }
                }
            }

            // 4. MÉTODOS DE PAGO (EXISTENTE)
            SectionHeader("Métodos de Pago")
            Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Mis Tarjetas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(GreenPrimary.copy(alpha = 0.1f)).clickable { showAddCardDialog = true }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Add, null, tint = GreenPrimary, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Agregar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenPrimary) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(cards) { card -> MiniCreditCard(card) }
                        if (cards.isEmpty()) { item { Text("No tienes tarjetas registradas.", fontSize = 13.sp, color = TextGray) } }
                    }
                }
            }

            // 5. GESTIÓN DE CUENTA (EXISTENTE)
            SectionHeader("Gestión de Cuenta")
            UserSettingsCard(icon = Icons.Default.ExitToApp, iconColor = Color(0xFFFF9800), iconBackground = Color(0xFFFFF3E0), title = "Cerrar Sesión", subtitle = "Salir de tu cuenta", onClick = { showLogoutDialog = true })
            UserSettingsCard(icon = Icons.Default.DeleteForever, iconColor = ErrorRed, iconBackground = Color(0xFFFFEBEE), title = "Eliminar Cuenta", subtitle = "Acción permanente", onClick = { showDeleteDialog = true })

            // Espacio final para que el FAB no tape contenido
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- DIÁLOGOS DE CONFIRMACIÓN (EXISTENTES) ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = White,
            icon = { Icon(Icons.Outlined.Logout, null, tint = Color(0xFFFF9800)) },
            title = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text("¿Estás seguro de que deseas salir de tu cuenta?", color = TextGray) },
            confirmButton = { Button(onClick = { showLogoutDialog = false; ServiceLocator.getSessionManager().clearSession(); onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) { Text("Cerrar Sesión", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar", color = TextDark) } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = White,
            icon = { Icon(Icons.Outlined.Warning, null, tint = ErrorRed) },
            title = { Text("Eliminar Cuenta", fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text("¿Estás completamente seguro? Esta acción eliminará permanentemente todos tus datos.", color = TextGray) },
            confirmButton = { Button(onClick = { showDeleteDialog = false; ServiceLocator.getSessionManager().clearSession(); onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("Eliminar", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = TextDark) } }
        )
    }
}

// --- COMPONENTES AUXILIARES (EXISTENTES) ---
@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextGray, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
}

@Composable
fun UserSettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    trailingContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = trailingContent == null) { onClick() },
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBackground), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TextDark)
                    if (subtitle != null) { Spacer(modifier = Modifier.height(2.dp)); Text(subtitle, fontSize = 13.sp, color = TextGray) }
                }
            }
            if (trailingContent != null) trailingContent()
        }
    }
}
