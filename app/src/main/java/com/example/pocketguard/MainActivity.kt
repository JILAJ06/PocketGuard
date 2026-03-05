package com.example.pocketguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.pocketguard.components.BottomNavBar
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.LoginViewModel
import com.example.pocketguard.presentation.viewmodel.PreferencesViewModel
import com.example.pocketguard.presentation.viewmodel.RegisterViewModel
import com.example.pocketguard.screens.*
import com.example.pocketguard.ui.theme.PocketGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.initializeServices(applicationContext)
        setContent {
            val preferencesViewModel: PreferencesViewModel = viewModel(
                factory = ServiceLocator.getPreferencesViewModelFactory()
            )
            val preferencesState by preferencesViewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                preferencesViewModel.loadPreferences()
            }

            val darkTheme = when (preferencesState.preferences?.theme) {
                "light" -> false
                "dark" -> true
                "system", null -> isSystemInDarkTheme()
                else -> isSystemInDarkTheme()
            }

            PocketGuardTheme(darkTheme = darkTheme) {
                PocketGuardNavigation()
            }
        }
    }
}

@Composable
fun PocketGuardNavigation() {
    val navController = rememberNavController()
    val sessionManager = ServiceLocator.getSessionManager()
    val startDestination = if (sessionManager.isSessionActive()) "inicio" else "login"

    val preferencesViewModel: PreferencesViewModel = viewModel(
        factory = ServiceLocator.getPreferencesViewModelFactory()
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("inicio", "suscripciones", "gastos", "alertas", "configuracion")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("inicio") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = ServiceLocator.getLoginViewModelFactory()
                )

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterLinkClick = { navController.navigate("register") },
                    onForgotPasswordClick = { navController.navigate("forgot_password") },
                    onGoogleClick = { }
                )
            }

            composable("register") {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = ServiceLocator.getRegisterViewModelFactory()
                )

                SignUpScreen(
                    viewModel = registerViewModel,
                    onRegisterSuccess = {
                        navController.navigate("inicio") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onLoginLinkClick = { navController.navigate("login") },
                    onGoogleClick = { }
                )
            }

            composable("forgot_password") {
                ForgotPasswordScreen(
                    onBackClick = { navController.popBackStack() },
                    onSendResetLink = { email ->
                        // Aquí iría la lógica para enviar el correo (ViewModel)
                    }
                )
            }

            composable("inicio") {
                HomeScreen(
                    onNavigateToSettings = {
                        navController.navigate("configuracion")
                    }
                )
            }

            composable("gastos") {
                ExpensesScreen(
                    onAuthExpired = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("suscripciones") {
                SubscriptionsScreen(
                    onAddClick = { navController.navigate("add_subscription") },
                    onEditClick = { id -> navController.navigate("add_subscription?id=$id") },
                    onAuthExpired = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "add_subscription?id={id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getString("id")
                val addSubViewModel: com.example.pocketguard.presentation.viewmodel.AddSubscriptionViewModel = viewModel(
                    factory = ServiceLocator.getAddSubscriptionViewModelFactory()
                )
                AddSubscriptionScreen(
                    subscriptionId = subscriptionId,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { navController.popBackStack() },
                    onAuthExpired = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("alertas") {
                AlertsScreen(
                    onAuthExpired = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("configuracion") {
                SettingsScreen(
                    preferencesViewModel = preferencesViewModel,
                    onLogout = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
