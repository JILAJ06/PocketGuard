package com.example.pocketguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
                    // El color de fondo vendrá del tema automáticamente
                ) {
                    PocketGuardNavigation()
                }
            }
        }
    }
}

// ... (El resto de PocketGuardNavigation queda igual)

@Composable
fun PocketGuardNavigation() {
    // Estado para controlar la pantalla actual: "login" o "registro"
    var currentScreen by remember { mutableStateOf("login") }

    when (currentScreen) {
        "login" -> {
            LoginScreen(
                onLoginClick = {
                    // Aquí iría la lógica para entrar a la App (Home)
                    println("Navegando al Home...")
                },
                onRegisterLinkClick = {
                    // CAMBIO DE VISTA: Vamos a Registro
                    currentScreen = "registro"
                },
                onGoogleClick = { println("Google Login") }
            )
        }
        "registro" -> {
            SignUpScreen(
                onRegisterClick = { println("Registrando usuario...") },
                onLoginLinkClick = {
                    // CAMBIO DE VISTA: Volvemos a Login
                    currentScreen = "login"
                },
                onGoogleClick = { println("Google Registro") }
            )
        }
    }
}