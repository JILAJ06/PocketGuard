package com.example.pocketguard.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.components.TransactionItem
import com.example.pocketguard.ui.theme.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import java.util.Locale

// Cambia la firma de HomeScreen para recibir el callback
@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit = {},
    authViewModel: com.example.pocketguard.viewmodels.AuthViewModel? = null,
    subscriptionViewModel: com.example.pocketguard.viewmodels.SubscriptionViewModel? = null,
    expenseViewModel: com.example.pocketguard.viewmodels.ExpenseViewModel? = null
) {
    val authState by authViewModel?.authUiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val subscriptionState by subscriptionViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val expenseState by expenseViewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        subscriptionViewModel?.loadSubscriptions()
        expenseViewModel?.loadExpensesAndCategories()
    }

    val monthlySubscriptions = subscriptionState?.metrics?.monthlyTotal ?: 0.0
    val monthlyExpenses = expenseState?.summary?.totalExpenses ?: 0.0
    val totalOut = monthlySubscriptions + monthlyExpenses
    val userInitials = authState?.user?.fullName
        ?.split(" ")
        ?.take(2)
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.joinToString("")
        ?: "UD"

    Scaffold(
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            HomeHeaderSection(
                userInitials = userInitials,
                totalOut = totalOut,
                monthlyExpenses = monthlyExpenses,
                monthlySubscriptions = monthlySubscriptions
            )

            // ... (Resto del contenido igual: Spacer, QuickStatsRow, etc.) ...
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                QuickStatCard(
                    Modifier.fillMaxWidth(),
                    Icons.Default.CreditCard,
                    "${subscriptionState?.metrics?.totalSubscriptions ?: 0} Suscripciones Activas",
                    "$${monthlySubscriptions.toInt()}/mes"
                )
                Spacer(modifier = Modifier.height(20.dp))
                UpcomingChargesSection(subscriptions = subscriptionState?.subscriptions ?: emptyList())
                Spacer(modifier = Modifier.height(20.dp))
                RecentActivitySection(expenses = expenseState?.expenses ?: emptyList())
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// Actualiza el Header para tener el icono de Menú
@Composable
fun HomeHeaderSection(
    userInitials: String,
    totalOut: Double,
    monthlyExpenses: Double,
    monthlySubscriptions: Double
) {
    val totalOutFormatted = String.format(Locale.getDefault(), "%.2f", totalOut)
    val monthlyExpensesFormatted = String.format(Locale.getDefault(), "%.2f", monthlyExpenses)
    val monthlySubscriptionsFormatted = String.format(Locale.getDefault(), "%.2f", monthlySubscriptions)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreenPrimary, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header Top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lado Izquierdo: SOLO TEXTO (Sin icono menú)
                Column {
                    Text("PocketGuard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
                    Text("Gestor de Gastos", fontSize = 12.sp, color = White.copy(alpha = 0.8f))
                }

                // Lado Derecho: Avatar
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(userInitials, color = White, fontWeight = FontWeight.Bold)
                }
            }

            // ... (El resto de la tarjeta de balance se queda IGUAL) ...
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("Total Mensual", fontSize = 14.sp, color = TextGray)
                            Text("$$totalOutFormatted", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                    }
                    Text("Gastos y suscripciones del mes", fontSize = 12.sp, color = TextGray.copy(alpha = 0.7f))

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatColumn("Gastos", "$$monthlyExpensesFormatted")
                        StatColumn("Suscripciones", "$$monthlySubscriptionsFormatted")
                    }
                }
            }
        }
    }
}

// ==========================================
// SECCIONES (COMPONENTES)
// ==========================================

@Composable
fun UpcomingChargesSection(subscriptions: List<com.example.pocketguard.data.models.Subscription>) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Próximos Cargos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Icon(Icons.Default.CalendarToday, null, tint = TextGray, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (subscriptions.isNotEmpty()) {
            val upcomingCharges = subscriptions.take(3)
            for (subscription in upcomingCharges) {
                val daysLeft = try {
                    val targetDate = java.time.LocalDate.parse(subscription.next_payment_date)
                    val today = java.time.LocalDate.now()
                    java.time.temporal.ChronoUnit.DAYS.between(today, targetDate).toInt().coerceAtLeast(0)
                } catch (e: Exception) {
                    0
                }

                UpcomingChargeItem(
                    name = subscription.service_name,
                    date = subscription.next_payment_date,
                    amount = "$${subscription.amount}",
                    daysLeft = "${daysLeft}d",
                    icon = Icons.Default.MusicNote
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text("No hay próximos cargos", color = TextGray, fontSize = 14.sp)
        }
    }
}

@Composable
fun RecentActivitySection(expenses: List<com.example.pocketguard.data.models.Expense>) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Actividad Reciente", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Icon(Icons.Default.ChevronRight, null, tint = TextGray)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (expenses.isEmpty()) {
                    Text("No hay actividad reciente", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 20.dp))
                } else {
                    expenses.take(4).forEach { expense ->
                        TransactionItem(
                            expense.name,
                            expense.expense_date,
                            "-$${expense.amount}",
                            Icons.Default.ShoppingCart
                        )
                        if (expense != expenses.take(4).last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENTES AUXILIARES
// ==========================================

@Composable
fun StatColumn(label: String, amount: String) {
    Column { Text(label, fontSize = 12.sp, color = TextGray); Text(amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark) }
}

@Composable
fun QuickStatCard(modifier: Modifier, icon: ImageVector, title: String, amount: String) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) { Icon(icon, null, tint = GreenPrimary); Spacer(modifier = Modifier.height(12.dp)); Text(title, fontSize = 12.sp, color = TextGray); Text(amount, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark) }
    }
}

@Composable
fun UpcomingChargeItem(name: String, date: String, amount: String, daysLeft: String, icon: ImageVector) {
    Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).border(1.dp, InputBackground, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.SemiBold, color = TextDark); Text(date, fontSize = 12.sp, color = TextGray) }
            Column(horizontalAlignment = Alignment.End) { Text(amount, fontWeight = FontWeight.Bold, color = TextDark); Text(daysLeft, fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold) }
        }
    }
}
