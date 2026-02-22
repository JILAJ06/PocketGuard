package com.example.pocketguard

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
        startDestination = "login" // Empezamos en login para probar el flujo
    ) {

        // LOGIN
        composable("login") {
            LoginScreen(
                onLoginClick = {
                    // Navegar al HOME y borrar el historial de login
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterLinkClick = { navController.navigate("registro") },
                onGoogleClick = { /* ... */ }
            )
        }

        // REGISTRO
        composable("registro") {
            SignUpScreen(
                onRegisterClick = {
                    // Al registrarse, vamos directo al HOME
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onLoginLinkClick = { navController.popBackStack() },
                onGoogleClick = { /* ... */ }
            )
        }

        // --- NUEVA RUTA: HOME ---
        composable("home") {
            HomeScreen()
        }
    }
}