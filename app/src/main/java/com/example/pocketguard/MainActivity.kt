package com.example.pocketguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pocketguard.screens.HomeScreen
import com.example.pocketguard.screens.LoginScreen
import com.example.pocketguard.screens.SignUpScreen
import com.example.pocketguard.screens.SubscriptionsScreen
import com.example.pocketguard.ui.theme.GreenPrimary
import com.example.pocketguard.ui.theme.PocketGuardTheme
import com.example.pocketguard.ui.theme.TextGray

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

// ... existing code ...

@Composable
fun PocketGuardNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determinar si mostrar BottomNav
    val showBottomNav = currentDestination?.route in listOf("home", "subscriptions", "gastos", "alertas")

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (showBottomNav) Modifier.paddingFromBaseline(bottom = 80.dp)
                    else Modifier
                )
        ) {
            // --- RUTA: LOGIN ---
            composable("login") {
                LoginScreen(
                    onLoginClick = {
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
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onLoginLinkClick = {
                        navController.popBackStack()
                    },
                    onGoogleClick = { /* Futura lógica Google */ }
                )
            }

            // --- RUTA: HOME ---
            composable("home") {
                HomeScreen()
            }

            // --- RUTA: SUSCRIPCIONES ---
            composable("subscriptions") {
                SubscriptionsScreen()
            }

            // --- RUTA: GASTOS (Placeholder) ---
            composable("gastos") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8F9FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Gastos - Próximamente")
                }
            }

            // --- RUTA: ALERTAS (Placeholder) ---
            composable("alertas") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF8F9FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Alertas - Próximamente")
                }
            }
        }

        // BottomNavigationBar
        if (showBottomNav) {
            PocketGuardBottomNav(
                currentDestination = currentDestination,
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun PocketGuardBottomNav(
    currentDestination: androidx.navigation.NavDestination?,
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = "home",
            label = "Inicio",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route = "subscriptions",
            label = "Suscripciones",
            selectedIcon = Icons.Filled.ShoppingCart,
            unselectedIcon = Icons.Outlined.ShoppingCart
        ),
        BottomNavItem(
            route = "gastos",
            label = "Gastos",
            selectedIcon = Icons.Filled.Wallet,
            unselectedIcon = Icons.Outlined.Wallet
        ),
        BottomNavItem(
            route = "alertas",
            label = "Alertas",
            selectedIcon = Icons.Filled.NotificationsActive,
            unselectedIcon = Icons.Outlined.NotificationsNone
        )
    )

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenPrimary,
                    selectedTextColor = GreenPrimary,
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
