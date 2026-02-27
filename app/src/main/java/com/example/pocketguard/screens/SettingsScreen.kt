package com.example.pocketguard.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketguard.components.AddCardDialog
import com.example.pocketguard.components.MiniCreditCard
import com.example.pocketguard.components.PaymentCard
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.PreferencesViewModel
import com.example.pocketguard.presentation.viewmodel.CardsViewModel
import com.example.pocketguard.presentation.viewmodel.BanksViewModel
import com.example.pocketguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onAuthExpired: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val preferencesViewModel: PreferencesViewModel = viewModel(
        factory = ServiceLocator.getPreferencesViewModelFactory()
    )
    val cardsViewModel: CardsViewModel = viewModel(
        factory = ServiceLocator.getCardsViewModelFactory()
    )
    val banksViewModel: BanksViewModel = viewModel(
        factory = ServiceLocator.getBanksViewModelFactory()
    )

    val state by preferencesViewModel.state.collectAsStateWithLifecycle()
    val cardsState by cardsViewModel.state.collectAsStateWithLifecycle()
    val banksState by banksViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Log.d("SettingsScreen", "Cargando preferencias, tarjetas y bancos...")
        preferencesViewModel.loadPreferences()
        cardsViewModel.loadCards()
        banksViewModel.loadBanks()
    }

    LaunchedEffect(state.isUnauthorized || cardsState.isUnauthorized || banksState.isUnauthorized) {
        if (state.isUnauthorized || cardsState.isUnauthorized || banksState.isUnauthorized) {
            Log.d("SettingsScreen", "Usuario no autorizado, cerrando sesión...")
            onAuthExpired()
        }
    }

    var selectedTheme by remember { mutableStateOf("system") }
    var selectedLanguage by remember { mutableStateOf("es") }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Sincronizar con el estado cuando se carguen las preferencias
    LaunchedEffect(state.preferences) {
        state.preferences?.let { prefs ->
            Log.d("SettingsScreen", "Preferencias cargadas: theme=${prefs.theme}, language=${prefs.language}")
            selectedTheme = prefs.theme
            selectedLanguage = prefs.language
        }
    }

    // Mostrar mensaje de éxito
    var lastUpdateTime by remember { mutableStateOf(0L) }
    LaunchedEffect(state.preferences, state.isLoading, state.errorMessage) {
        if (!state.isLoading && state.errorMessage.isEmpty() && state.preferences != null) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime > 500) {
                Log.d("SettingsScreen", "Preferencias guardadas exitosamente")
                lastUpdateTime = currentTime
                snackbarHostState.showSnackbar(
                    message = "Cambios guardados",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Mapear tarjetas
    val cards = remember(cardsState.cards) {
        cardsState.cards.map { card ->
            PaymentCard(
                id = card.card_id,
                name = if (card.alias.isNotEmpty()) card.alias else card.bank_name,
                last4 = card.last_4_digits ?: "",
                color = try {
                    Color(android.graphics.Color.parseColor(card.color_hex))
                } catch (e: Exception) {
                    Color(0xFF4A90E2)
                }
            )
        }
    }

    val banks = remember(banksState.banks) {
        banksState.banks.map { it.name }
    }

    // Diálogo agregar tarjeta
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

    // Diálogo cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = White,
            icon = { Icon(Icons.Outlined.Logout, null, tint = Color(0xFFFF9800)) },
            title = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text("¿Estás seguro de que deseas salir de tu cuenta?", color = TextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("SettingsScreen", "Confirmando cerrar sesión")
                        showLogoutDialog = false
                        ServiceLocator.getSessionManager().clearSession()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) { Text("Cerrar Sesión", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = TextDark)
                }
            }
        )
    }

    // Diálogo eliminar cuenta
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = White,
            icon = { Icon(Icons.Outlined.Warning, null, tint = ErrorRed) },
            title = { Text("Eliminar Cuenta", fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text("¿Estás completamente seguro? Esta acción eliminará permanentemente todos tus datos. NO se puede deshacer.", color = TextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("SettingsScreen", "Confirmando eliminación de cuenta")
                        showDeleteDialog = false
                        ServiceLocator.getSessionManager().clearSession()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = TextDark)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Configuración",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
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
            // Apariencia
            SectionHeader("Apariencia")

            UserSettingsCard(
                icon = Icons.Default.Palette,
                iconColor = Color(0xFF9C27B0),
                iconBackground = Color(0xFFF3E5F5),
                title = "Tema de la aplicación",
                subtitle = when (selectedTheme) {
                    "system" -> "Automático"
                    "light" -> "Claro"
                    "dark" -> "Oscuro"
                    else -> selectedTheme
                }
            ) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text("Cambiar", color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf(
                            "system" to "Automático",
                            "light" to "Claro",
                            "dark" to "Oscuro"
                        ).forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    Log.d("SettingsScreen", "Cambiando tema a: $code")
                                    selectedTheme = code
                                    preferencesViewModel.updatePreferences(theme = code)
                                    expanded = false
                                },
                                leadingIcon = if (selectedTheme == code) {
                                    { Icon(Icons.Default.Check, null, tint = GreenPrimary) }
                                } else null
                            )
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
                    TextButton(onClick = { expanded = true }) {
                        Text("Cambiar", color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("es" to "Español", "en" to "English").forEach { (code, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    Log.d("SettingsScreen", "Cambiando idioma a: $code")
                                    selectedLanguage = code
                                    preferencesViewModel.updatePreferences(language = code)
                                    expanded = false
                                },
                                leadingIcon = if (selectedLanguage == code) {
                                    { Icon(Icons.Default.Check, null, tint = GreenPrimary) }
                                } else null
                            )
                        }
                    }
                }
            }

            // Métodos de Pago
            SectionHeader("Métodos de Pago")

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mis Tarjetas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GreenPrimary.copy(alpha = 0.1f))
                                .clickable { showAddCardDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Add,
                                    null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Agregar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(cards) { card ->
                            MiniCreditCard(card)
                        }
                        if (cards.isEmpty()) {
                            item {
                                Text(
                                    "No tienes tarjetas registradas.",
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }
            }

            // Gestión de Cuenta
            SectionHeader("Gestión de Cuenta")

            UserSettingsCard(
                icon = Icons.Default.ExitToApp,
                iconColor = Color(0xFFFF9800),
                iconBackground = Color(0xFFFFF3E0),
                title = "Cerrar Sesión",
                subtitle = "Salir de tu cuenta",
                onClick = { showLogoutDialog = true }
            )

            UserSettingsCard(
                icon = Icons.Default.DeleteForever,
                iconColor = ErrorRed,
                iconBackground = Color(0xFFFFEBEE),
                title = "Eliminar Cuenta",
                subtitle = "Acción permanente",
                onClick = { showDeleteDialog = true }
            )

            // Error message
            if (state.errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = ErrorRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(state.errorMessage, color = ErrorRed, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextGray,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = trailingContent == null) { onClick() },
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextDark
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(subtitle, fontSize = 13.sp, color = TextGray)
                    }
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

