package com.example.pocketguard

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.pocketguard.utils.FCMTokenManager
import com.example.pocketguard.utils.GoogleSignInHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var fcmTokenManager: FCMTokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.initializeServices(applicationContext)

        // Inicializar FCMTokenManager
        fcmTokenManager = FCMTokenManager(applicationContext)

        // Inicializar FCM si hay sesión activa
        val sessionManager = ServiceLocator.getSessionManager()
        if (sessionManager.isSessionActive() && !sessionManager.isTokenExpired()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    fcmTokenManager.initializeFCM()
                    Log.d("MainActivity", "FCM inicializado correctamente en onCreate")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error al inicializar FCM en onCreate: ${e.message}", e)
                }
            }
        }

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
                PocketGuardNavigation(fcmTokenManager = fcmTokenManager)
            }
        }
    }
}

@Composable
fun PocketGuardNavigation(fcmTokenManager: FCMTokenManager) {
    val navController = rememberNavController()
    val sessionManager = ServiceLocator.getSessionManager()

    // Función para inicializar FCM desde composables
    val initializeFCM = remember {
        {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    fcmTokenManager.initializeFCM()
                    Log.d("PocketGuardNavigation", "FCM inicializado correctamente")
                } catch (e: Exception) {
                    Log.e("PocketGuardNavigation", "Error al inicializar FCM: ${e.message}", e)
                }
            }
        }
    }

    // Verificar si la sesión está activa Y el token no ha expirado
    val isValidSession = sessionManager.isSessionActive() && !sessionManager.isTokenExpired()
    val startDestination = if (isValidSession) "inicio" else "login"

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

                // Google Sign-In Helper
                val context = androidx.compose.ui.platform.LocalContext.current
                val googleSignInHelper = remember { GoogleSignInHelper(context) }

                // Launcher para Google Sign-In
                val googleSignInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    Log.d("MainActivity", "Google Sign-In result: resultCode=${result.resultCode}")

                    if (result.resultCode == Activity.RESULT_OK) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            val idToken = account?.idToken

                            Log.d("MainActivity", "Account: ${account?.email}, idToken: ${if (idToken != null) "presente" else "null"}")

                            if (idToken != null) {
                                Log.d("MainActivity", "Google Sign-In exitoso, enviando idToken al backend")
                                loginViewModel.loginWithGoogle(idToken)
                            } else {
                                Log.e("MainActivity", "No se pudo obtener idToken de Google")
                                loginViewModel.setError("Error: No se pudo obtener token de Google")
                            }
                        } catch (e: ApiException) {
                            Log.e("MainActivity", "Error en Google Sign-In: ${e.statusCode} - ${e.message}", e)
                            val errorMsg = when (e.statusCode) {
                                10 -> "Error de configuración. Verifica Google Cloud Console"
                                12501 -> "Login cancelado"
                                7 -> "Error de red. Verifica tu conexión"
                                else -> "Error al iniciar sesión con Google (${e.statusCode})"
                            }
                            loginViewModel.setError(errorMsg)
                        }
                    } else {
                        Log.d("MainActivity", "Google Sign-In cancelado o falló")
                    }
                }

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        // Inicializar FCM después de login exitoso
                        initializeFCM()

                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterLinkClick = { navController.navigate("register") },
                    onForgotPasswordClick = { navController.navigate("forgot_password") },
                    onGoogleClick = {
                        Log.d("MainActivity", "Iniciando Google Sign-In")
                        val signInIntent = googleSignInHelper.getSignInIntent()
                        googleSignInLauncher.launch(signInIntent)
                    },
                    onForgotPasswordClick = {
                        navController.navigate("forgot_password")
                    }
                )
            }

            composable("register") {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = ServiceLocator.getRegisterViewModelFactory()
                )

                // Google Sign-In Helper para registro
                val context = androidx.compose.ui.platform.LocalContext.current
                val googleSignInHelper = remember { GoogleSignInHelper(context) }

                // Launcher para Google Sign-In en registro
                val googleSignInLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    Log.d("MainActivity", "Google Sign-In result (registro): resultCode=${result.resultCode}")

                    if (result.resultCode == Activity.RESULT_OK) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            val idToken = account?.idToken

                            Log.d("MainActivity", "Account (registro): ${account?.email}, idToken: ${if (idToken != null) "presente" else "null"}")

                            if (idToken != null) {
                                Log.d("MainActivity", "Google Sign-In exitoso en registro, enviando idToken al backend")
                                registerViewModel.loginWithGoogle(idToken)
                            } else {
                                Log.e("MainActivity", "No se pudo obtener idToken de Google en registro")
                                registerViewModel.setError("Error: No se pudo obtener token de Google")
                            }
                        } catch (e: ApiException) {
                            Log.e("MainActivity", "Error en Google Sign-In en registro: ${e.statusCode} - ${e.message}", e)
                            val errorMsg = when (e.statusCode) {
                                10 -> "Error de configuración. Verifica Google Cloud Console"
                                12501 -> "Login cancelado"
                                7 -> "Error de red. Verifica tu conexión"
                                else -> "Error al iniciar sesión con Google (${e.statusCode})"
                            }
                            registerViewModel.setError(errorMsg)
                        }
                    } else {
                        Log.d("MainActivity", "Google Sign-In cancelado o falló en registro")
                    }
                }

                SignUpScreen(
                    viewModel = registerViewModel,
                    onRegisterSuccess = {
                        // Inicializar FCM después de registro exitoso
                        initializeFCM()

                        navController.navigate("inicio") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onLoginLinkClick = { navController.navigate("login") },
                    onGoogleClick = {
                        Log.d("MainActivity", "Iniciando Google Sign-In desde registro")
                        val signInIntent = googleSignInHelper.getSignInIntent()
                        googleSignInLauncher.launch(signInIntent)
                    }
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
                    },
                    onAuthExpired = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
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
                    onAuthExpired = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLogout = {
                        sessionManager.clearSession()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("forgot_password") {
                val authViewModel: com.example.pocketguard.presentation.viewmodel.AuthViewModel = viewModel(
                    factory = ServiceLocator.getAuthViewModelFactory()
                )
                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "reset_password/{token}",
                arguments = listOf(navArgument("token") { type = NavType.StringType })
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                val authViewModel: com.example.pocketguard.presentation.viewmodel.AuthViewModel = viewModel(
                    factory = ServiceLocator.getAuthViewModelFactory()
                )
                ResetPasswordScreen(
                    token = token,
                    viewModel = authViewModel,
                    onBackClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSuccess = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}


