package com.example.pocketguard.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
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
import com.example.pocketguard.components.NewSubscriptionModal
import com.example.pocketguard.ui.theme.*

// Modelo de datos para la UI
data class SubscriptionUI(
    val id: String,
    val name: String,
    val price: String,
    val cycle: String,
    val nextDate: String,
    val categoryName: String,
    val daysLeft: Int,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun SubscriptionsScreen(
    onAddClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {}
) {
    // --- ESTADOS LOCALES ---
    var showNewSubscriptionModal by remember { mutableStateOf(false) }
    var selectedSubscription by remember { mutableStateOf<SubscriptionUI?>(null) }

    // Estados para Eliminación
    var showDeleteDialog by remember { mutableStateOf(false) }
    var subscriptionToDelete by remember { mutableStateOf<SubscriptionUI?>(null) }

    // Datos Mock
    val subscriptions = remember {
        mutableStateListOf(
            SubscriptionUI("1", "Netflix Premium", "199", "Mensual", "10/03/2026", "Entretenimiento", 4, Icons.Default.Movie, BrandNetflix),
            SubscriptionUI("2", "Spotify Duo", "149", "Mensual", "15/03/2026", "Música", 12, Icons.Default.MusicNote, BrandSpotify),
            SubscriptionUI("3", "Amazon Prime", "899", "Anual", "20/10/2026", "Compras", 245, Icons.Default.ShoppingCart, BrandAmazon),
            SubscriptionUI("4", "HBO Max", "179", "Mensual", "05/03/2026", "Entretenimiento", 2, Icons.Default.Movie, BrandHBO)
        )
    }

    val totalMonthly = subscriptions
        .filter { it.cycle == "Mensual" }
        .sumOf { it.price.toIntOrNull() ?: 0 } +
            (subscriptions.filter { it.cycle == "Anual" }.sumOf { it.price.toIntOrNull() ?: 0 } / 12)

    // --- ALERTA DE ELIMINACIÓN ---
    if (showDeleteDialog && subscriptionToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = White,
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = ErrorRed) },
            title = {
                Text(
                    text = "Eliminar Suscripción",
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar ${subscriptionToDelete?.name}? Esta acción no se puede deshacer.",
                    color = TextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Lógica de eliminación
                        subscriptions.remove(subscriptionToDelete)
                        showDeleteDialog = false
                        subscriptionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar", color = TextDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedSubscription = null
                    showNewSubscriptionModal = true
                },
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
                        onClick = {
                            selectedSubscription = sub
                            showNewSubscriptionModal = true
                        },
                        onLongClick = { // <--- NUEVO EVENTO
                            subscriptionToDelete = sub
                            showDeleteDialog = true
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // --- LOGICA DEL MODAL DE CREACIÓN/EDICIÓN ---
    if (showNewSubscriptionModal) {
        NewSubscriptionModal(
            initialName = selectedSubscription?.name ?: "",
            initialPrice = selectedSubscription?.price ?: "",
            initialCategory = selectedSubscription?.categoryName ?: "",
            initialCycle = selectedSubscription?.cycle ?: "Mensual",
            initialDate = selectedSubscription?.nextDate ?: "dd/MM/yyyy",

            onDismiss = { showNewSubscriptionModal = false },
            onSave = { name, price, category, cycle, date ->
                if (selectedSubscription != null) {
                    val index = subscriptions.indexOfFirst { it.id == selectedSubscription!!.id }
                    if (index != -1) {
                        subscriptions[index] = subscriptions[index].copy(
                            name = name, price = price, cycle = cycle, nextDate = date, categoryName = category
                        )
                    }
                } else {
                    subscriptions.add(
                        SubscriptionUI(
                            id = (subscriptions.size + 1).toString(),
                            name = name,
                            price = price,
                            cycle = cycle,
                            nextDate = date,
                            categoryName = category,
                            daysLeft = 30,
                            icon = Icons.Default.CreditCard,
                            color = GreenPrimary
                        )
                    )
                }
                showNewSubscriptionModal = false
            }
        )
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

@OptIn(ExperimentalFoundationApi::class) // Necesario para combinedClickable
@Composable
fun SubscriptionPremiumCard(
    subscription: SubscriptionUI,
    onClick: () -> Unit,
    onLongClick: () -> Unit // <--- Nuevo parámetro
) {
    val isUrgent = subscription.daysLeft <= 5

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)) // Importante para el ripple effect
            // Usamos combinedClickable en lugar de clickable simple
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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