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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketguard.data.models.Subscription
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.SubscriptionsViewModel
import com.example.pocketguard.ui.theme.*

fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "streaming", "entretenimiento" -> Icons.Default.Movie
        "música", "music" -> Icons.Default.MusicNote
        "transporte" -> Icons.Default.DirectionsCar
        "comida", "alimentos", "food" -> Icons.Default.Restaurant
        "gimnasio", "fitness", "gym" -> Icons.Default.FitnessCenter
        "educación", "education" -> Icons.Default.School
        "salud", "health" -> Icons.Default.Favorite
        "compras", "shopping" -> Icons.Default.ShoppingCart
        "hogar", "home" -> Icons.Default.Home
        "servicios", "services" -> Icons.Default.Build
        else -> Icons.Default.Category
    }
}

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
    onEditClick: (String) -> Unit = {},
    onAuthExpired: () -> Unit = {}
) {
    val viewModel: SubscriptionsViewModel = viewModel(
        factory = ServiceLocator.getSubscriptionsViewModelFactory()
    )
    val cardsViewModel: com.example.pocketguard.presentation.viewmodel.CardsViewModel = viewModel(
        factory = ServiceLocator.getCardsViewModelFactory()
    )
    val categoriesViewModel: com.example.pocketguard.presentation.viewmodel.CategoriesViewModel = viewModel(
        factory = ServiceLocator.getCategoriesViewModelFactory()
    )

    val state by viewModel.state.collectAsStateWithLifecycle()
    val cardsState by cardsViewModel.state.collectAsStateWithLifecycle()
    val categoriesState by categoriesViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        cardsViewModel.loadCards()
        categoriesViewModel.loadCategories()
    }

    LaunchedEffect(state.isUnauthorized) {
        if (state.isUnauthorized) {
            onAuthExpired()
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var subscriptionToDelete by remember { mutableStateOf<Subscription?>(null) }
    var showAddModal by remember { mutableStateOf(false) }

    // Mapear tarjetas
    val cards = remember(cardsState.cards) {
        cardsState.cards.map { card ->
            com.example.pocketguard.components.PaymentCard(
                id = card.card_id,
                name = if (card.alias.isNotEmpty()) card.alias else card.bank_name,
                last4 = card.last_4_digits ?: "",
                color = Color(android.graphics.Color.parseColor(card.color_hex ?: "#4A90E2"))
            )
        }
    }

    // Mapear categorías
    val categories = remember(categoriesState.categories) {
        categoriesState.categories.map { cat ->
            com.example.pocketguard.components.SubCategoryData(
                name = cat.name,
                icon = getCategoryIcon(cat.name),
                color = Color(android.graphics.Color.parseColor(cat.color_hex ?: "#4A90E2"))
            )
        }
    }

    val subscriptions = remember(state.subscriptions) {
        state.subscriptions.map { sub ->
            SubscriptionUI(
                id = sub.subscription_id,
                name = sub.service_name,
                price = String.format("%.2f", sub.amount),
                cycle = sub.billing_cycle,
                nextDate = sub.next_payment_date,
                categoryName = sub.category_name,
                daysLeft = sub.days_until_payment,
                icon = Icons.Default.CreditCard,
                color = Color(android.graphics.Color.parseColor(sub.category_color))
            )
        }
    }

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
                    text = "¿Estás seguro de que deseas eliminar ${subscriptionToDelete?.service_name}? Esta acción no se puede deshacer.",
                    color = TextGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubscription(subscriptionToDelete!!.subscription_id)
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

    // Modal de Nueva Suscripción
    if (showAddModal) {
        com.example.pocketguard.components.NewSubscriptionModal(
            cards = cards,
            categories = categories,
            onDismiss = { showAddModal = false },
            onSave = { name, price, category, cycle, date, cardId ->
                val billingCycleId = when (cycle) {
                    "Diario" -> 1
                    "Semanal" -> 2
                    "Anual" -> 4
                    else -> 3 // Mensual
                }
                val categoryId = categoriesState.categories.firstOrNull { it.name == category }?.id ?: ""

                // Convertir fecha de dd/MM/yyyy a YYYY-MM-DD
                val formattedDate = try {
                    val inputFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val outputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val localDate = java.time.LocalDate.parse(date, inputFormatter)
                    localDate.format(outputFormatter)
                } catch (e: Exception) {
                    date // Si falla, usar el original
                }

                // Crear la suscripción con el cardId de la tarjeta seleccionada
                viewModel.createSubscription(
                    serviceName = name,
                    amount = price.toDoubleOrNull() ?: 0.0,
                    nextPaymentDate = formattedDate,
                    billingCycleId = billingCycleId,
                    categoryId = categoryId,
                    cardId = cardId // ✅ Ahora usa el ID de la tarjeta seleccionada
                )
                showAddModal = false
            }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddModal = true },
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
            SubscriptionsHeader(total = "$$${String.format("%.2f", state.totalMonthly)}")

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (state.errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage, color = ErrorRed, fontSize = 14.sp)
                }
            } else {
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
                            onClick = { onEditClick(sub.id) },
                            onLongClick = {
                                subscriptionToDelete = state.subscriptions.find { it.subscription_id == sub.id }
                                showDeleteDialog = true
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
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