package com.example.pocketguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.ui.theme.*
import com.example.pocketguard.components.NewSubscriptionModal

// Modelo de datos para la UI
data class SubscriptionUI(
    val id: String,
    val name: String,
    val price: String,
    val cycle: String, // "Mensual", "Anual"
    val daysLeft: Int,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun SubscriptionsScreen(
    onAddClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    var showNewSubscriptionModal by remember { mutableStateOf(false) }
    // Datos Mock (Simulados)
    val subscriptions = listOf(
        SubscriptionUI("1", "Netflix Premium", "199", "Mensual", 4, Icons.Default.Movie, BrandNetflix),
        SubscriptionUI("2", "Spotify Duo", "149", "Mensual", 12, Icons.Default.MusicNote, BrandSpotify),
        SubscriptionUI("3", "Amazon Prime", "899", "Anual", 245, Icons.Default.ShoppingCart, BrandAmazon),
        SubscriptionUI("4", "HBO Max", "179", "Mensual", 2, Icons.Default.Movie, BrandHBO)
    )

    val totalMonthly = subscriptions
        .filter { it.cycle == "Mensual" }
        .sumOf { it.price.toIntOrNull() ?: 0 } +
            (subscriptions.filter { it.cycle == "Anual" }.sumOf { it.price.toIntOrNull() ?: 0 } / 12)

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewSubscriptionModal = true }, // <--- AHORA ABRE EL MODAL
                containerColor = GreenPrimary,
                contentColor = White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Suscripción")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Cabecera
            SubscriptionsHeader(total = "$$totalMonthly")

            // 2. Lista
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Mis Servicios", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                }
                items(subscriptions) { sub ->
                    SubscriptionPremiumCard(
                        subscription = sub,
                        onClick = { onEditClick(sub.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
        if (showNewSubscriptionModal) {
            NewSubscriptionModal(
                onDismiss = { showNewSubscriptionModal = false },
                onSave = { name, price, category, cycle ->
                    // Aquí iría la lógica para guardar en el repositorio
                    // Por ahora solo cerramos el modal
                    showNewSubscriptionModal = false
                }
            )
        }
    }
}

// ==========================================
// COMPONENTES UI
// ==========================================

@Composable
fun SubscriptionsHeader(total: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreenPrimary, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Suscripciones", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White)
                Text("Total mensual estimado", fontSize = 14.sp, color = White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(total, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = White)
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CreditCard, null, tint = White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun SubscriptionPremiumCard(subscription: SubscriptionUI, onClick: () -> Unit) {
    val isUrgent = subscription.daysLeft <= 5

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de Marca
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(subscription.color.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(subscription.icon, null, tint = subscription.color, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Central
            Column(modifier = Modifier.weight(1f)) {
                Text(subscription.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Text(subscription.cycle, fontSize = 12.sp, color = TextGray)
            }

            // Precio y Días
            Column(horizontalAlignment = Alignment.End) {
                Text("-$$${subscription.price}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                // Badge de días
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isUrgent) ErrorRed.copy(alpha = 0.1f) else GreenPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "en ${subscription.daysLeft} días",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUrgent) ErrorRed else GreenPrimary
                    )
                }
            }
        }
    }
}