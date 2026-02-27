package com.example.pocketguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.PreferencesViewModel
import com.example.pocketguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onAuthExpired: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val preferencesViewModel: PreferencesViewModel = viewModel(
        factory = ServiceLocator.getPreferencesViewModelFactory()
    )

    val state by preferencesViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        preferencesViewModel.loadPreferences()
    }

    LaunchedEffect(state.isUnauthorized) {
        if (state.isUnauthorized) {
            onAuthExpired()
        }
    }

    var selectedTheme by remember { mutableStateOf("system") }
    var selectedLanguage by remember { mutableStateOf("es") }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Sincronizar con el estado cuando se carguen las preferencias
    LaunchedEffect(state.preferences) {
        state.preferences?.let { prefs ->
            selectedTheme = prefs.theme
            selectedLanguage = prefs.language
        }
    }

    // Mostrar mensaje de éxito cuando se actualicen las preferencias
    LaunchedEffect(state.preferences, state.errorMessage) {
        if (!state.isLoading && state.errorMessage.isEmpty() && state.preferences != null) {
            showSuccessMessage = true
            kotlinx.coroutines.delay(2000)
            showSuccessMessage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            // Tema
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tema", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box {
                        Button(
                            onClick = { showThemeMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = InputBackground)
                        ) {
                            Text(selectedTheme, color = TextDark)
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            listOf("system", "light", "dark").forEach { theme ->
                                DropdownMenuItem(
                                    text = { Text(theme) },
                                    onClick = {
                                        selectedTheme = theme
                                        preferencesViewModel.updatePreferences(theme = theme)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Idioma
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Idioma", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box {
                        Button(
                            onClick = { showLanguageMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = InputBackground)
                        ) {
                            Text(if (selectedLanguage == "es") "Español" else "English", color = TextDark)
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            listOf("es" to "Español", "en" to "English").forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedLanguage = code
                                        preferencesViewModel.updatePreferences(language = code)
                                        showLanguageMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Loading state
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Error message
            if (state.errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        state.errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFFC62828),
                        fontSize = 14.sp
                    )
                }
            }

            // Gestión de Cuenta
            Text(
                "Gestión de Cuenta",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextDark,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Cerrar Sesión
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable {
                        ServiceLocator.getSessionManager().clearSession()
                        onLogout()
                    },
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = TextDark)
                    Icon(Icons.Outlined.Logout, null, tint = Color(0xFFFF9800))
                }
            }

            // Eliminar Cuenta
            var showDeleteDialog by remember { mutableStateOf(false) }

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
                                showDeleteDialog = false
                                // TODO: Llamar al endpoint de eliminar cuenta
                                ServiceLocator.getSessionManager().clearSession()
                                onAuthExpired() // Redirigir al login
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { showDeleteDialog = true },
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Eliminar Cuenta", fontWeight = FontWeight.Medium, fontSize = 16.sp, color = TextDark)
                        Text("Acción permanente", fontSize = 12.sp, color = TextGray)
                    }
                    Icon(Icons.Outlined.Delete, null, tint = ErrorRed)
                }
            }
        }
    }
}

