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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.pocketguard.components.AddCardDialog
import com.example.pocketguard.components.EditCardDialog
import com.example.pocketguard.components.MiniCreditCard
import com.example.pocketguard.components.PaymentCard
import com.example.pocketguard.data.models.AuthResult
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.BanksViewModel
import com.example.pocketguard.presentation.viewmodel.CardsViewModel
import com.example.pocketguard.presentation.viewmodel.PreferencesViewModel
import com.example.pocketguard.ui.theme.*
import java.util.UUID
import androidx.compose.ui.tooling.preview.Preview

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
    modifier: Modifier = Modifier,
    preferencesViewModel: PreferencesViewModel = viewModel(factory = ServiceLocator.getPreferencesViewModelFactory()),
    onAuthExpired: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    val cardsViewModel: CardsViewModel = viewModel(factory = ServiceLocator.getCardsViewModelFactory())
    val banksViewModel: BanksViewModel = viewModel(factory = ServiceLocator.getBanksViewModelFactory())
    val categoriesViewModel: com.example.pocketguard.presentation.viewmodel.CategoriesViewModel = viewModel(factory = ServiceLocator.getCategoriesViewModelFactory())

    val state by preferencesViewModel.state.collectAsStateWithLifecycle()
    val cardsState by cardsViewModel.state.collectAsStateWithLifecycle()
    val banksState by banksViewModel.state.collectAsStateWithLifecycle()
    val categoriesState by categoriesViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var monthlyIncome by remember { mutableStateOf(state.monthlyIncome.toString()) }
    var showSaveButton by remember { mutableStateOf(false) }

    // Actualizar monthlyIncome cuando cambie el estado
    LaunchedEffect(state.monthlyIncome) {
        monthlyIncome = state.monthlyIncome.toString()
    }

    var isAddingCategory by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var newCatColor by remember { mutableStateOf(Color(0xFFE74C3C)) }
    var newCatIcon by remember { mutableStateOf(Icons.Outlined.Category) }
    var editingCategoryId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        preferencesViewModel.loadPreferences()
        cardsViewModel.loadCards()
        banksViewModel.loadBanks()
        categoriesViewModel.loadCategories()
    }

    LaunchedEffect(state.isUnauthorized || cardsState.isUnauthorized || banksState.isUnauthorized || categoriesState.isUnauthorized) {
        if (state.isUnauthorized || cardsState.isUnauthorized || banksState.isUnauthorized || categoriesState.isUnauthorized) {
            onAuthExpired()
        }
    }

    var selectedTheme by remember { mutableStateOf("system") }
    var selectedLanguage by remember { mutableStateOf("es") }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showEditCardDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<com.example.pocketguard.data.models.Card?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var deletePasswordInput by remember { mutableStateOf("") }
    var deleteErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(state.preferences) {
        state.preferences?.let { prefs ->
            selectedTheme = prefs.theme
            selectedLanguage = prefs.language
        }
    }

    // --- Manejo de Cards ---
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

    if (showEditCardDialog && cardToEdit != null) {
        val card = cardToEdit!!  // Variable local para evitar smart cast issues
        EditCardDialog(
            bankName = card.bank_name,
            last4Digits = card.last_4_digits,
            initialAlias = card.alias,
            initialColorHex = card.color_hex ?: "#1976D2",
            onDismiss = {
                showEditCardDialog = false
                cardToEdit = null
            },
            onSave = { alias, colorHex ->
                cardsViewModel.updateCard(
                    id = card.card_id,
                    bankName = null,
                    alias = alias,
                    last4 = null,
                    colorHex = colorHex,
                    isDefault = null
                )
                showEditCardDialog = false
                cardToEdit = null
            }
        )
    }

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
                        Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configuración", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showSaveButton,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        showSaveButton = false
                        Log.d("Settings", "Ingreso mensual guardado: $monthlyIncome")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Save, null) },
                    text = { Text("Guardar Cambios") }
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. SECCIÓN FINANZAS
            SectionHeader("Finanzas")

            // Tarjeta de Ingresos
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AttachMoney, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Ingreso Mensual", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Base para tu presupuesto", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        leadingIcon = { Text("$", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Botón de guardar
                    AnimatedVisibility(
                        visible = showSaveButton,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Button(
                            onClick = {
                                val income = monthlyIncome.toDoubleOrNull()
                                if (income != null && income > 0) {
                                    preferencesViewModel.saveMonthlyIncome(income)
                                    showSaveButton = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Ingreso mensual guardado")
                                    }
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Ingresa un monto válido")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Ingreso Mensual")
                        }
                    }
                }
            }

            // --- Leyenda para gastos ---
            Text(
                text = "Esta información es únicamente para tu referencia personal y se almacena de forma local en tu dispositivo. Nosotros no guardamos estos datos en nuestros servidores.",
                fontSize = 11.sp,
                color = TextGray,
                lineHeight = 14.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            // -------------------------------------

            // 2. SECCIÓN CATEGORÍAS
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Categorías", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)

                        IconButton(
                            onClick = { isAddingCategory = !isAddingCategory },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if(isAddingCategory) ErrorRed.copy(0.1f) else MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if(isAddingCategory) ErrorRed else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if(isAddingCategory) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = "Toggle Add"
                            )
                        }
                    }

                    // Formulario Inline
                    AnimatedVisibility(visible = isAddingCategory) {
                        Column(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.3f), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text("Nueva Categoría", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(12.dp))

                            BasicTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (newCatName.isEmpty()) Text("Nombre...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f))
                                        innerTextField()
                                    }
                                },
                                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Icono", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            val availableIcons = listOf(
                                Icons.Outlined.Restaurant, Icons.Outlined.DirectionsCar, Icons.Outlined.ShoppingCart,
                                Icons.Outlined.Home, Icons.Outlined.Movie, Icons.Outlined.MusicNote,
                                Icons.Outlined.FitnessCenter, Icons.Outlined.School, Icons.Outlined.LocalCafe,
                                Icons.Outlined.Flight, Icons.Outlined.Category
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(availableIcons) { icon ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if(newCatIcon == icon) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                            .border(
                                                width = if(newCatIcon == icon) 2.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                            .clickable { newCatIcon = icon },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            tint = if(newCatIcon == icon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Color", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            val colors = listOf(Color(0xFFE74C3C), Color(0xFF9B59B6), Color(0xFF2ECC71), Color(0xFF3498DB), Color(0xFFF1C40F))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(colors) { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(color, CircleShape)
                                            .border(
                                                width = if(newCatColor == color) 3.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                shape = CircleShape
                                            )
                                            .clickable { newCatColor = color }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if(newCatName.isNotEmpty()){
                                        val argb = newCatColor.toArgb()
                                        val colorHex = String.format("#%06X", (argb and 0xFFFFFF))
                                        val iconName = com.example.pocketguard.utils.IconMapper.getNameFromIcon(newCatIcon)

                                        if (editingCategoryId != null) {
                                            // Editar categoría existente
                                            categoriesViewModel.updateCategory(
                                                id = editingCategoryId!!,
                                                name = newCatName,
                                                iconUrl = null,
                                                iconName = iconName,
                                                colorHex = colorHex
                                            )
                                            editingCategoryId = null
                                        } else {
                                            // Crear nueva categoría
                                            categoriesViewModel.createCategory(
                                                name = newCatName,
                                                iconUrl = null,
                                                iconName = iconName,
                                                colorHex = colorHex
                                            )
                                        }
                                        newCatName = ""
                                        newCatIcon = Icons.Outlined.Category
                                        isAddingCategory = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = newCatName.isNotEmpty()
                            ) { Text(if (editingCategoryId != null) "Actualizar Categoría" else "Crear Categoría", fontWeight = FontWeight.Bold) }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(categoriesState.userCategories) { cat ->
                            var showMenu by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .clickable { showMenu = true }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                color = try {
                                                    Color(android.graphics.Color.parseColor(cat.color_hex))
                                                } catch (_: Exception) {
                                                    Color(0xFF9B59B6)
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(cat.name, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Eliminar") },
                                        onClick = {
                                            categoriesViewModel.deleteCategory(cat.id)
                                            showMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) },
                                        colors = MenuDefaults.itemColors(textColor = ErrorRed)
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Editar") },
                                        onClick = {
                                            newCatName = cat.name
                                            newCatColor = try {
                                                Color(android.graphics.Color.parseColor(cat.color_hex))
                                            } catch (_: Exception) {
                                                Color(0xFF9B59B6)
                                            }
                                            editingCategoryId = cat.id
                                            isAddingCategory = true
                                            showMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurface) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. APARIENCIA
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

            // 4. MÉTODOS DE PAGO
            SectionHeader("Métodos de Pago")
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Mis Tarjetas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer).clickable { showAddCardDialog = true }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Agregar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(cardsState.cards) { card ->
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(70.dp)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(
                                                    try { Color(android.graphics.Color.parseColor(card.color_hex)) } catch (e: Exception) { Color(0xFF4A90E2) },
                                                    try { Color(android.graphics.Color.parseColor(card.color_hex)).copy(alpha = 0.7f) } catch (e: Exception) { Color(0xFF4A90E2).copy(alpha = 0.7f) }
                                                )
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { showMenu = true }
                                        .padding(10.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(card.alias.ifEmpty { card.bank_name }, color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                            if (card.is_default) {
                                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(12.dp, 8.dp).background(Color(0xFFFFD700), RoundedCornerShape(2.dp)))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("•••• ${card.last_4_digits}", color = White, fontSize = 10.sp, letterSpacing = 1.sp)
                                        }
                                    }
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    if (!card.is_default) {
                                        DropdownMenuItem(
                                            text = { Text("Marcar como predeterminada") },
                                            onClick = {
                                                cardsViewModel.setDefaultCard(card.card_id)
                                                showMenu = false
                                            },
                                            leadingIcon = { Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700)) }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Editar") },
                                        onClick = {
                                            cardToEdit = card
                                            showEditCardDialog = true
                                            showMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurface) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Eliminar") },
                                        onClick = {
                                            cardsViewModel.deleteCard(card.card_id)
                                            showMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) },
                                        colors = MenuDefaults.itemColors(textColor = ErrorRed)
                                    )
                                }
                            }
                        }
                        if (cardsState.cards.isEmpty()) { item { Text("No tienes tarjetas registradas.", fontSize = 13.sp, color = TextGray) } }
                    }
                }
            }

            // 5. GESTIÓN DE CUENTA
            SectionHeader("Gestión de Cuenta")
            UserSettingsCard(icon = Icons.Default.ExitToApp, iconColor = Color(0xFFFF9800), iconBackground = Color(0xFFFFF3E0), title = "Cerrar Sesión", subtitle = "Salir de tu cuenta", onClick = { showLogoutDialog = true })
            UserSettingsCard(icon = Icons.Default.DeleteForever, iconColor = ErrorRed, iconBackground = Color(0xFFFFEBEE), title = "Eliminar Cuenta", subtitle = "Acción permanente", onClick = { showDeleteDialog = true })

            // Error message
            if (state.errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = ErrorRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(state.errorMessage, color = ErrorRed, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- DIÁLOGOS DE CONFIRMACIÓN ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(imageVector = Icons.AutoMirrored.Outlined.Logout, null, tint = Color(0xFFFF9800)) },
            title = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("¿Estás seguro de que deseas salir de tu cuenta?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = { Button(onClick = { showLogoutDialog = false; ServiceLocator.getSessionManager().clearSession(); onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) { Text("Cerrar Sesión", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurface) } }
        )
    }

    if (showDeleteDialog) {
        val authRepository = ServiceLocator.getAuthRepository()
        val isGoogleUser = authRepository.isGoogleUser()

        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletePasswordInput = ""
                deleteErrorMessage = ""
            },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Outlined.Warning, null, tint = ErrorRed) },
            title = { Text("Eliminar Cuenta", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text(
                        "¿Estás completamente seguro? Esta acción eliminará permanentemente todos tus datos.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isGoogleUser) {
                        // Solo pedir password si NO es usuario de Google
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Por seguridad, ingresa tu contraseña:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = deletePasswordInput,
                            onValueChange = {
                                deletePasswordInput = it
                                deleteErrorMessage = ""
                            },
                            placeholder = { Text("Contraseña") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            isError = deleteErrorMessage.isNotEmpty()
                        )
                        if (deleteErrorMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                deleteErrorMessage,
                                color = ErrorRed,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Usuario de Google: No se requiere password
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Cuenta vinculada con Google",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Validar password solo si NO es Google
                        if (!isGoogleUser && deletePasswordInput.isEmpty()) {
                            deleteErrorMessage = "La contraseña es requerida"
                            return@Button
                        }

                        coroutineScope.launch {
                            // Para usuarios de Google, enviar password vacío o "google_auth"
                            val passwordToSend = if (isGoogleUser) "google_auth" else deletePasswordInput
                            val result = authRepository.deleteAccount(passwordToSend)
                            when (result) {
                                is AuthResult.Success -> {
                                    ServiceLocator.getSessionManager().clearSession()
                                    showDeleteDialog = false
                                    deletePasswordInput = ""
                                    deleteErrorMessage = ""
                                    onLogout()
                                }
                                is AuthResult.Error -> {
                                    deleteErrorMessage = result.message
                                }
                                else -> {}
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    enabled = isGoogleUser || deletePasswordInput.isNotEmpty()
                ) {
                    Text("Eliminar Permanentemente", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deletePasswordInput = ""
                    deleteErrorMessage = ""
                }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

// --- COMPONENTES AUXILIARES ---
@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBackground), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    if (subtitle != null) { Spacer(modifier = Modifier.height(2.dp)); Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            if (trailingContent != null) trailingContent()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    PocketGuardTheme {
        SettingsScreen(onAuthExpired = {}, onLogout = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenDarkPreview() {
    PocketGuardTheme(darkTheme = true) {
        SettingsScreen(onAuthExpired = {}, onLogout = {})
    }
}

