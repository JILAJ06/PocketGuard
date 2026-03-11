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
import androidx.compose.material.icons.outlined.*
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.pocketguard.data.models.Subscription
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.SubscriptionsViewModel
import com.example.pocketguard.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.tooling.preview.Preview

/**
 * Obtener icono de categoría desde el campo icon_name guardado en el backend
 * Si no existe, usa mapeo por nombre de categoría como fallback
 */
fun getCategoryIcon(category: com.example.pocketguard.data.models.Category): ImageVector {
    // Prioridad 1: Usar icon_name si existe
    if (!category.icon_name.isNullOrEmpty()) {
        return com.example.pocketguard.utils.IconMapper.getIconByName(category.icon_name)
    }

    // Fallback: Mapeo por nombre de categoría (para categorías antiguas)
    return when (category.name.lowercase()) {
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

// Helper adicional para compatibilidad
fun getCategoryIconByName(categoryName: String): ImageVector {
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
    val color: Color,
    val cardAlias: String?,
    val cardLastDigits: String?
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Recargar datos cada vez que se muestra la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadSubscriptions()
        cardsViewModel.loadCards()
        categoriesViewModel.loadCategories()
    }

    LaunchedEffect(state.isUnauthorized) {
        if (state.isUnauthorized) {
            onAuthExpired()
        }
    }

    // Mostrar errores con Snackbar
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = state.errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(categoriesState.errorMessage) {
        if (categoriesState.errorMessage.isNotEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = categoriesState.errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Estados para Modals y Dialogs
    var showDeleteDialog by remember { mutableStateOf(false) }
    var subscriptionToDelete by remember { mutableStateOf<Subscription?>(null) }

    var showAddModal by remember { mutableStateOf(false) }
    var showEditModal by remember { mutableStateOf(false) }

    // Almacenamos la suscripción que se va a editar (UI Model)
    var subscriptionToEdit by remember { mutableStateOf<SubscriptionUI?>(null) }

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
                id = cat.id, // Agregar ID para referencia
                name = cat.name,
                icon = getCategoryIcon(cat), // Pasar objeto completo
                color = Color(android.graphics.Color.parseColor(cat.color_hex ?: "#4A90E2"))
            )
        }
    }

    val subscriptions = remember(state.subscriptions, categoriesState.categories) {
        state.subscriptions.map { sub ->
            val currentCategory = categoriesState.categories.firstOrNull { it.name == sub.category_name }
            val categoryColor = currentCategory?.color_hex ?: sub.category_color
            val categoryIcon = if (currentCategory != null) {
                getCategoryIcon(currentCategory)
            } else {
                getCategoryIconByName(sub.category_name)
            }

            // Calcular días correctamente - si es negativo, calcular próximo pago
            val actualDaysLeft = if (sub.days_until_payment < 0) {
                // La suscripción venció, calcular cuántos días faltan para el PRÓXIMO pago
                val today = LocalDate.now()
                val lastPayment = LocalDate.parse(sub.next_payment_date)

                // Calcular siguiente fecha de pago según el ciclo
                val nextPayment = when (sub.billing_cycle.lowercase()) {
                    "daily", "diario" -> lastPayment.plusDays(1)
                    "weekly", "semanal" -> lastPayment.plusWeeks(1)
                    "monthly", "mensual" -> lastPayment.plusMonths(1)
                    "yearly", "anual" -> lastPayment.plusYears(1)
                    else -> lastPayment.plusMonths(1) // Default mensual
                }

                // Si aún sigue vencido, seguir sumando ciclos hasta llegar al futuro
                var calculatedNext = nextPayment
                while (calculatedNext.isBefore(today)) {
                    calculatedNext = when (sub.billing_cycle.lowercase()) {
                        "daily", "diario" -> calculatedNext.plusDays(1)
                        "weekly", "semanal" -> calculatedNext.plusWeeks(1)
                        "monthly", "mensual" -> calculatedNext.plusMonths(1)
                        "yearly", "anual" -> calculatedNext.plusYears(1)
                        else -> calculatedNext.plusMonths(1)
                    }
                }

                java.time.temporal.ChronoUnit.DAYS.between(today, calculatedNext).toInt()
            } else {
                sub.days_until_payment
            }

            SubscriptionUI(
                id = sub.subscription_id,
                name = sub.service_name,
                price = String.format("%.2f", sub.amount),
                cycle = sub.billing_cycle,
                nextDate = sub.next_payment_date,
                categoryName = sub.category_name,
                daysLeft = actualDaysLeft,
                icon = categoryIcon,
                color = Color(android.graphics.Color.parseColor(categoryColor)),
                cardAlias = sub.card_alias,
                cardLastDigits = sub.card_last_digits
            )
        }
    }

    // --- DIÁLOGO DE ELIMINAR ---
    if (showDeleteDialog && subscriptionToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = ErrorRed) },
            title = {
                Text(
                    text = "Eliminar Suscripción",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
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
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- MODAL AGREGAR SUSCRIPCIÓN ---
    if (showAddModal) {
        val defaultCard = cardsState.cards.firstOrNull { it.is_default }
        val defaultCardId = defaultCard?.card_id ?: ""

        com.example.pocketguard.components.NewSubscriptionModal(
            cards = cards,
            categories = categories,
            initialCardId = defaultCardId,
            onDismiss = { showAddModal = false },
            onSave = { name, price, category, cycle, date, cardId ->
                // IMPORTANTE: Los IDs deben coincidir con los del backend
                // Backend: Daily=5, Weekly=6, Monthly=7, Yearly=8
                val billingCycleId = when (cycle) {
                    "Diario" -> 5    // Daily
                    "Semanal" -> 6   // Weekly
                    "Mensual" -> 7   // Monthly
                    "Anual" -> 8     // Yearly
                    else -> 7        // Default: Monthly
                }
                val categoryId = categoriesState.categories.firstOrNull { it.name == category }?.id ?: ""

                // Validar que la categoría sea válida
                if (categoryId.isEmpty()) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Por favor selecciona una categoría válida",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@NewSubscriptionModal
                }

                val formattedDate = try {
                    val inputFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val outputFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val localDate = java.time.LocalDate.parse(date, inputFormatter)
                    localDate.format(outputFormatter)
                } catch (e: Exception) { date }

                // Convertir cardId vacío a null (el backend espera null, no string vacío)
                val validCardId = if (cardId.isNullOrEmpty()) null else cardId

                viewModel.createSubscription(
                    serviceName = name,
                    amount = price.toDoubleOrNull() ?: 0.0,
                    nextPaymentDate = formattedDate,
                    billingCycleId = billingCycleId,
                    categoryId = categoryId,
                    cardId = validCardId
                )
                showAddModal = false
            },
            onCategoryCreated = {
                // Recargar categorías cuando se crea una nueva
                categoriesViewModel.loadCategories()
            },
            onCreateCategory = { name, colorHex, iconName ->
                // Crear categoría en el backend
                categoriesViewModel.createCategory(name, null, iconName, colorHex)
            }
        )
    }

    // --- MODAL EDITAR SUSCRIPCIÓN ---
    if (showEditModal && subscriptionToEdit != null) {
        val sub = subscriptionToEdit!!
        com.example.pocketguard.components.NewSubscriptionModal(
            // Pasar datos actuales
            initialName = sub.name,
            initialPrice = sub.price,
            initialCategory = sub.categoryName,
            initialCycle = sub.cycle,
            initialDate = try {
                val input = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val output = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                java.time.LocalDate.parse(sub.nextDate, input).format(output)
            } catch (e: Exception) { sub.nextDate },
            initialCardId = "",

            cards = cards,
            categories = categories,

            onDismiss = {
                showEditModal = false
                subscriptionToEdit = null
            },
            onSave = { name, price, category, cycle, date, cardId ->
                // IMPORTANTE: Los IDs deben coincidir con los del backend
                // Backend: Daily=5, Weekly=6, Monthly=7, Yearly=8
                val billingCycleId = when (cycle) {
                    "Diario" -> 5    // Daily
                    "Semanal" -> 6   // Weekly
                    "Mensual" -> 7   // Monthly
                    "Anual" -> 8     // Yearly
                    else -> 7        // Default: Monthly
                }
                val categoryId = categoriesState.categories.firstOrNull { it.name == category }?.id ?: ""

                // Validar que la categoría sea válida
                if (categoryId.isEmpty()) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Por favor selecciona una categoría válida",
                            duration = SnackbarDuration.Short
                        )
                    }
                    return@NewSubscriptionModal
                }

                val formattedDate = try {
                    val input = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val output = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    java.time.LocalDate.parse(date, input).format(output)
                } catch (e: Exception) { date }

                // Convertir cardId vacío a null (el backend espera null, no string vacío)
                val validCardId = if (cardId.isNullOrEmpty()) null else cardId

                viewModel.updateSubscription(
                    subscriptionId = sub.id,
                    serviceName = name,
                    amount = price.toDoubleOrNull() ?: 0.0,
                    nextPaymentDate = formattedDate,
                    billingCycleId = billingCycleId,
                    categoryId = categoryId,
                    cardId = validCardId
                )
                showEditModal = false
                subscriptionToEdit = null
            },
            onCategoryCreated = {
                // Recargar categorías cuando se crea una nueva
                categoriesViewModel.loadCategories()
            },
            onCreateCategory = { name, colorHex, iconName ->
                // Crear categoría en el backend
                categoriesViewModel.createCategory(name, null, iconName, colorHex)
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // Header con un solo $
            SubscriptionsHeader(total = "$${String.format("%.2f", state.totalMonthly)}")

            if (state.isLoading && subscriptions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (state.errorMessage.isNotEmpty() && subscriptions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.errorMessage, color = ErrorRed, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadSubscriptions() }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                var isRefreshing by remember { mutableStateOf(false) }

                androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        scope.launch {
                            viewModel.loadSubscriptions()
                            categoriesViewModel.loadCategories()
                            cardsViewModel.loadCards()
                            kotlinx.coroutines.delay(500)
                            isRefreshing = false
                        }
                    }
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("Mis Servicios", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                        }

                        if (subscriptions.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Outlined.Category,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No tienes suscripciones",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Agrega tu primera suscripción para\nempezar a gestionar tus gastos",
                                        fontSize = 14.sp,
                                        color = TextGray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { showAddModal = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Agregar Suscripción")
                                    }
                                }
                            }
                        }

                        items(subscriptions) { sub ->
                            SubscriptionPremiumCard(
                                subscription = sub,
                                onClick = {
                                // ABRIR MODAL DE EDICIÓN
                                subscriptionToEdit = sub
                                showEditModal = true
                            },
                            onLongClick = {
                                // ABRIR DIÁLOGO DE ELIMINAR
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
    onLongClick: () -> Unit // Parámetro para detección de pulsación larga
) {
    val isUrgent = subscription.daysLeft <= 5

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
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
                Text(subscription.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subscription.cycle, fontSize = 12.sp, color = TextGray)
                if (subscription.cardAlias != null || subscription.cardLastDigits != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = subscription.color,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (subscription.cardAlias != null) {
                                subscription.cardAlias
                            } else if (subscription.cardLastDigits != null) {
                                "•••• ${subscription.cardLastDigits}"
                            } else {
                                ""
                            },
                            fontSize = 11.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Precio y Días (Con un solo $)
            Column(horizontalAlignment = Alignment.End) {
                Text("-$${subscription.price}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isUrgent) ErrorRed.copy(alpha = 0.1f) else GreenPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val daysText = when (subscription.daysLeft) {
                            0 -> "Hoy"
                            1 -> "Mañana"
                            else -> "en ${subscription.daysLeft} días"
                        }
                        Text(
                            text = daysText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUrgent) ErrorRed else GreenPrimary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SubscriptionsScreenPreview() {
    PocketGuardTheme {
        SubscriptionsScreen(
            onAuthExpired = {},
            onAddClick = {},
            onEditClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SubscriptionsScreenDarkPreview() {
    PocketGuardTheme(darkTheme = true) {
        SubscriptionsScreen(
            onAuthExpired = {},
            onAddClick = {},
            onEditClick = {}
        )
    }
}

