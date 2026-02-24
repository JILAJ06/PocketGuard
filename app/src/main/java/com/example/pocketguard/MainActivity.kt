package com.example.pocketguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.pocketguard.components.BottomNavBar
import com.example.pocketguard.screens.*
import com.example.pocketguard.ui.theme.PocketGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketGuardTheme {
                PocketGuardNavigation()
            }
        }
    }
}

@Composable
fun PocketGuardNavigation() {
    val navController = rememberNavController()

    // Obtenemos la ruta actual para saber qué icono pintar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Lógica para mostrar/ocultar la barra (solo visible en las 4 pantallas principales)
    val showBottomBar = currentRoute in listOf("inicio", "suscripciones", "gastos", "alertas")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Evita acumular pantallas en la pila al navegar entre tabs
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        // El NavHost ahora recibe el padding del Scaffold para no tapar contenido con la barra
        NavHost(
            navController = navController,
            startDestination = "inicio",
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- 1. INICIO ---
            composable("inicio") {
                HomeScreen() // Ya no necesita onOpenDrawer
            }

            // --- 2. SUSCRIPCIONES ---
            composable("suscripciones") {
                SubscriptionsScreen(
                    onAddClick = { navController.navigate("add_subscription") },
                    onEditClick = { id -> navController.navigate("add_subscription?id=$id") }
                )
            }

            // --- 3. GASTOS ---
            composable("gastos") {
                ExpensesScreen()
            }

            // --- 4. ALERTAS ---
            composable("alertas") {
                AlertsScreen()
            }

            // --- PANTALLAS SECUNDARIAS (Sin barra inferior) ---
            composable(
                route = "add_subscription?id={id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getString("id")
                AddSubscriptionScreen(
                    subscriptionId = subscriptionId,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { navController.popBackStack() }
                )
            }
        }
    }
}