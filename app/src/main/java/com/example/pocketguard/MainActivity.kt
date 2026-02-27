package com.example.pocketguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.pocketguard.components.BottomNavBar
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.LoginViewModel
import com.example.pocketguard.presentation.viewmodel.RegisterViewModel
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
    val sessionManager = ServiceLocator.getSessionManager()
    val isAuthenticated = remember { mutableStateOf(sessionManager.isSessionActive()) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("inicio", "suscripciones", "gastos", "alertas")

    LaunchedEffect(Unit) {
        if (!isAuthenticated.value) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
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
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated.value) "inicio" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = ServiceLocator.getLoginViewModelFactory()
                )

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        isAuthenticated.value = true
                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterLinkClick = { navController.navigate("register") },
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
                        isAuthenticated.value = true
                        navController.navigate("inicio") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onLoginLinkClick = { navController.navigate("login") },
                    onGoogleClick = { }
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
                        isAuthenticated.value = false
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
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
                        isAuthenticated.value = false
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(
                route = "add_subscription?id={id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getString("id")
                AddSubscriptionScreen(
                    subscriptionId = subscriptionId,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { navController.popBackStack() },
                    onAuthExpired = {
                        sessionManager.clearSession()
                        isAuthenticated.value = false
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("alertas") {
                AlertsScreen(
                    onAuthExpired = {
                        sessionManager.clearSession()
                        isAuthenticated.value = false
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("configuracion") {
                SettingsScreen(
                    onAuthExpired = {
                        sessionManager.clearSession()
                        isAuthenticated.value = false
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    onLogout = {
                        sessionManager.clearSession()
                        isAuthenticated.value = false
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}