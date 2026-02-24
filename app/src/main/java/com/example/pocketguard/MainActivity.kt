package com.example.pocketguard

import androidx.navigation.navArgument
import androidx.navigation.NavType
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pocketguard.screens.HomeScreen
import com.example.pocketguard.screens.LoginScreen
import com.example.pocketguard.screens.SignUpScreen
import com.example.pocketguard.screens.SubscriptionsScreen
import com.example.pocketguard.screens.AddSubscriptionScreen
import com.example.pocketguard.screens.AlertsScreen
import com.example.pocketguard.screens.ExpensesScreen
import com.example.pocketguard.ui.theme.PocketGuardTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PocketGuardNavigation()
                }
            }
        }
    }
}

@Composable
fun PocketGuardNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        // --- RUTA: LOGIN ---
        composable("login") {
            LoginScreen(
                onLoginClick = {
                    // Al hacer login, vamos al Home y borramos el historial para no volver al login con "Atrás"
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterLinkClick = {
                    navController.navigate("registro")
                },
                onGoogleClick = { /* Futura lógica Google */ }
            )
        }

        // --- RUTA: REGISTRO ---
        composable("registro") {
            SignUpScreen(
                onRegisterClick = {
                    // Al registrarse, también vamos al Home
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onLoginLinkClick = {
                    // Volver al login (popBackStack es como pulsar el botón Atrás)
                    navController.popBackStack()
                },
                onGoogleClick = { /* Futura lógica Google */ }
            )
        }

        // --- RUTA: HOME (DASHBOARD) ---
        composable("home") {
            HomeScreen()
        }
        // 1. ACTUALIZAR RUTA SUSCRIPCIONES
        composable("suscripciones") {
            SubscriptionsScreen(
                onAddClick = { navController.navigate("add_subscription") },
                onEditClick = { id ->
                    // Navegar pasando el ID
                    navController.navigate("add_subscription?id=$id")
                }
            )
        }

        // 2. ACTUALIZAR RUTA AGREGAR/EDITAR (Con argumento opcional)
        composable(
            route = "add_subscription?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            // Extraer el ID (será null si es nuevo, o un String si es editar)
            val subscriptionId = backStackEntry.arguments?.getString("id")

            AddSubscriptionScreen(
                subscriptionId = subscriptionId, // Pasamos el ID a la pantalla
                onBackClick = { navController.popBackStack() },
                onSaveClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("gastos") {
            ExpensesScreen()
        }
        composable("alertas") {
            AlertsScreen()
        }
    }
}