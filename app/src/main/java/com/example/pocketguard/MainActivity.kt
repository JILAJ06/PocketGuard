package com.example.pocketguard

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.pocketguard.components.BottomNavBar
import com.example.pocketguard.data.models.AuthResult
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.AuthViewModel
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

        // Solicitar permiso de notificaciones para Android 13+
        requestNotificationPermission()

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

    /**
     * Solicita el permiso de notificaciones para Android 13+ (API 33+)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("MainActivity", "Solicitando permiso de notificaciones...")
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            } else {
                Log.d("MainActivity", "Permiso de notificaciones ya concedido")
            }
        } else {
            Log.d("MainActivity", "Android < 13, no se requiere solicitar permiso de notificaciones")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "✅ Permiso de notificaciones concedido")
                // Inicializar FCM después de obtener el permiso
                val sessionManager = ServiceLocator.getSessionManager()
                if (sessionManager.isSessionActive() && !sessionManager.isTokenExpired()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            fcmTokenManager.initializeFCM()
                            Log.d("MainActivity", "FCM inicializado después de obtener permiso")
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error al inicializar FCM: ${e.message}", e)
                        }
                    }
                }
            } else {
                Log.w("MainActivity", "❌ Permiso de notificaciones denegado")
            }
        }
    }
}

@Composable
fun PocketGuardNavigation(fcmTokenManager: FCMTokenManager) {
    val navController = rememberNavController()
    val sessionManager = ServiceLocator.getSessionManager()

    fun navigateToLogin() {
        sessionManager.clearSession()
        navController.navigate("login") {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

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

    var isBootstrappingSession by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf("login") }

    // En arranque, intenta refresh silencioso si el access token expiró pero existe refresh token.
    LaunchedEffect(Unit) {
        val hasSession = sessionManager.isSessionActive()
        if (!hasSession) {
            startDestination = "login"
            isBootstrappingSession = false
            return@LaunchedEffect
        }

        if (!sessionManager.isTokenExpired()) {
            startDestination = "inicio"
            isBootstrappingSession = false
            return@LaunchedEffect
        }

        val hasRefreshToken = !ServiceLocator.getTokenManager().getRefreshToken().isNullOrEmpty()
        if (!hasRefreshToken) {
            sessionManager.clearSession()
            startDestination = "login"
            isBootstrappingSession = false
            return@LaunchedEffect
        }

        when (ServiceLocator.getAuthRepository().refreshToken()) {
            is AuthResult.Success -> startDestination = "inicio"
            is AuthResult.Error -> {
                sessionManager.clearSession()
                startDestination = "login"
            }
            is AuthResult.Loading -> startDestination = "login"
        }

        isBootstrappingSession = false
    }

    val preferencesViewModel: PreferencesViewModel = viewModel(
        factory = ServiceLocator.getPreferencesViewModelFactory()
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("inicio", "suscripciones", "gastos", "alertas", "configuracion")

    if (isBootstrappingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

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
                val authViewModel: AuthViewModel = viewModel(
                    factory = ServiceLocator.getAuthViewModelFactory()
                )

                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }

            composable("inicio") {
                HomeScreen(
                    onNavigateToSettings = {
                        navController.navigate("configuracion")
                    },
                    onAuthExpired = { navigateToLogin() }
                )
            }

            composable("gastos") {
                ExpensesScreen(
                    onAuthExpired = { navigateToLogin() }
                )
            }

            composable("suscripciones") {
                SubscriptionsScreen(
                    onAddClick = { navController.navigate("add_subscription") },
                    onEditClick = { id -> navController.navigate("add_subscription?id=$id") },
                    onAuthExpired = { navigateToLogin() }
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
                    onAuthExpired = { navigateToLogin() }
                )
            }

            composable("alertas") {
                AlertsScreen(
                    onAuthExpired = { navigateToLogin() }
                )
            }

            composable("configuracion") {
                SettingsScreen(
                    onAuthExpired = { navigateToLogin() },
                    onLogout = { navigateToLogin() }
                )
            }

            composable(
                route = "reset_password/{token}",
                arguments = listOf(navArgument("token") { type = NavType.StringType })
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                val authViewModel: AuthViewModel = viewModel(
                    factory = ServiceLocator.getAuthViewModelFactory()
                )
                ResetPasswordScreen(
                    token = token,
                    viewModel = authViewModel,
                    onBackClick = {
                        navController.navigate("login") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onSuccess = {
                        navController.navigate("login") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}


