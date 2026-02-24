package com.example.pocketguard.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.ui.theme.*

// Definimos los items del menú
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Inicio : BottomNavItem("inicio", "Inicio", Icons.Default.Home, Icons.Outlined.Home)
    object Suscripciones : BottomNavItem("suscripciones", "Suscripciones", Icons.Default.CreditCard, Icons.Outlined.CreditCard)
    object Gastos : BottomNavItem("gastos", "Gastos", Icons.Default.AttachMoney, Icons.Outlined.AttachMoney)
    object Alertas : BottomNavItem("alertas", "Alertas", Icons.Default.Notifications, Icons.Outlined.Notifications)
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Inicio,
        BottomNavItem.Suscripciones,
        BottomNavItem.Gastos,
        BottomNavItem.Alertas
    )

    NavigationBar(
        containerColor = White,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenPrimary, // Icono verde al seleccionar
                    selectedTextColor = GreenPrimary, // Texto verde al seleccionar
                    indicatorColor = GreenPrimary.copy(alpha = 0.15f), // La "píldora" de fondo verde suave
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray
                )
            )
        }
    }
}