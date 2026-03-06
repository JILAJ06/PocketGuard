package com.example.pocketguard.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketguard.data.models.DailyExpensePoint
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.DashboardViewModel
import com.example.pocketguard.presentation.viewmodel.PreferencesViewModel
import com.example.pocketguard.ui.theme.*

// Cambia la firma de HomeScreen para recibir el callback
@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit = {}, // Nuevo parámetro con valor por defecto
    onNavigateToSettings: () -> Unit = {}, // Agregar callback para configuración
    onAuthExpired: () -> Unit = {}
) {
    // ViewModels
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = ServiceLocator.getDashboardViewModelFactory()
    )
    val preferencesViewModel: PreferencesViewModel = viewModel(
        factory = ServiceLocator.getPreferencesViewModelFactory()
    )
    val subscriptionsViewModel: com.example.pocketguard.presentation.viewmodel.SubscriptionsViewModel = viewModel(
        factory = ServiceLocator.getSubscriptionsViewModelFactory()
    )
    val expensesViewModel: com.example.pocketguard.presentation.viewmodel.ExpensesViewModel = viewModel(
        factory = ServiceLocator.getExpensesViewModelFactory()
    )

    val dashboardState by dashboardViewModel.state.collectAsStateWithLifecycle()
    val preferencesState by preferencesViewModel.state.collectAsStateWithLifecycle()
    val subscriptionsState by subscriptionsViewModel.state.collectAsStateWithLifecycle()
    val expensesState by expensesViewModel.state.collectAsStateWithLifecycle()
    val isUnauthorized by dashboardViewModel.isUnauthorized.collectAsStateWithLifecycle()

    // Cargar datos
    LaunchedEffect(Unit) {
        preferencesViewModel.loadPreferences()
        subscriptionsViewModel.loadSubscriptions()
        expensesViewModel.loadExpenses()
    }

    LaunchedEffect(preferencesState.monthlyIncome) {
        if (preferencesState.monthlyIncome > 0) {
            dashboardViewModel.loadDashboard(
                monthlyIncome = preferencesState.monthlyIncome,
                period = "month"
            )
        }
    }

    LaunchedEffect(isUnauthorized || subscriptionsState.isUnauthorized || expensesState.isUnauthorized) {
        if (isUnauthorized || subscriptionsState.isUnauthorized || expensesState.isUnauthorized) {
            onAuthExpired()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (dashboardState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Pasamos el evento al Header
                HomeHeaderSection(
                    totalBalance = dashboardState.totalBalance,
                    distribution = dashboardState.distribution,
                    monthlyIncome = preferencesState.monthlyIncome,
                    totalExpenses = expensesState.expenses.sumOf { it.amount },
                    totalSubscriptions = subscriptionsState.subscriptions.sumOf { it.amount },
                    onNavigateToSettings = onNavigateToSettings
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    QuickStatsRow(
                        monthlyIncome = preferencesState.monthlyIncome,
                        subscriptionsCount = subscriptionsState.subscriptions.size,
                        subscriptionsTotal = subscriptionsState.subscriptions.sumOf { it.amount }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    UpcomingChargesSection(
                        subscriptions = subscriptionsState.subscriptions
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    MonthlyTrendSection(
                        data = dashboardState.monthlyTrend,
                        isLoading = dashboardState.isLoading
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    RecentActivitySection(
                        expenses = expensesState.expenses
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SmartAnalysisSection(
                        distribution = dashboardState.distribution,
                        insights = dashboardState.insights
                    )
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// Actualiza el Header para tener el icono de Menú
@Composable
fun HomeHeaderSection(
    totalBalance: Double = 0.0,
    distribution: com.example.pocketguard.data.models.SpendingDistribution = com.example.pocketguard.data.models.SpendingDistribution(0.0, 0.0, 0.0, 0f, 0f, 0f),
    monthlyIncome: Double = 0.0,
    totalExpenses: Double = 0.0,
    totalSubscriptions: Double = 0.0,
    onNavigateToSettings: () -> Unit = {}
) {
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

                // Lado Derecho: Botón de configuración
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("UD", color = White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Saldo Disponible", fontSize = 14.sp, color = TextGray)
                            Text(
                                "$${String.format("%.2f", totalBalance)}",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                        Surface(
                            color = GreenPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "0.0%",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text("Después de gastos y suscripciones", fontSize = 12.sp, color = TextGray.copy(alpha = 0.7f))

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatColumn("Ingresos", "$${String.format("%.2f", monthlyIncome)}")
                        StatColumn("Gastos", "$${String.format("%.2f", totalExpenses)}")
                        StatColumn("Suscripciones", "$${String.format("%.2f", totalSubscriptions)}")
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
fun QuickStatsRow(
    monthlyIncome: Double = 0.0,
    subscriptionsCount: Int = 0,
    subscriptionsTotal: Double = 0.0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            Modifier.weight(1f),
            Icons.Default.TrendingUp,
            "Ingresos",
            "$${String.format("%.2f", monthlyIncome)}"
        )
        QuickStatCard(
            Modifier.weight(1f),
            Icons.Default.CreditCard,
            "$subscriptionsCount Suscripciones",
            "$${String.format("%.2f", subscriptionsTotal)}"
        )
    }
}

@Composable
fun UpcomingChargesSection(
    subscriptions: List<com.example.pocketguard.data.models.Subscription> = emptyList()
) {
    // Filtrar suscripciones con próximos pagos en los próximos 7 días
    val upcomingSubscriptions = remember(subscriptions) {
        val today = java.time.LocalDate.now()
        val nextWeek = today.plusDays(7)

        subscriptions.filter { subscription ->
            try {
                val nextPaymentDate = java.time.LocalDate.parse(subscription.next_payment_date)
                !nextPaymentDate.isBefore(today) && !nextPaymentDate.isAfter(nextWeek)
            } catch (e: Exception) {
                false
            }
        }.sortedBy { it.next_payment_date }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Próximos Cargos",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                Icons.Default.CalendarToday,
                null,
                tint = TextGray,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (upcomingSubscriptions.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CalendarToday,
                            null,
                            tint = TextGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No hay cargos próximos", color = TextGray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                upcomingSubscriptions.take(3).forEach { subscription ->
                    UpcomingChargeCard(subscription = subscription)
                }
            }
        }
    }
}

@Composable
fun UpcomingChargeCard(subscription: com.example.pocketguard.data.models.Subscription) {
    // Usar el campo days_until_payment que viene del backend
    val daysLeft = subscription.days_until_payment

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(GreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CreditCard,
                    null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    subscription.service_name,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )
                Text(
                    subscription.next_payment_date,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$${String.format("%.2f", subscription.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )
                Text(
                    if (daysLeft == 0) "Hoy" else if (daysLeft == 1) "Mañana" else "en $daysLeft días",
                    fontSize = 12.sp,
                    color = if (daysLeft <= 1) ErrorRed else GreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MonthlyTrendSection(
    data: List<DailyExpensePoint> = emptyList(),
    isLoading: Boolean = false
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tendencia Mensual",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (selectedIndex != null && data.isNotEmpty()) {
                    Text(
                        text = "$${String.format("%.2f", data[selectedIndex!!].totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary, modifier = Modifier.size(32.dp))
                }
            } else if (data.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TrendingUp, null, tint = TextGray.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No hay datos suficientes", color = TextGray, fontSize = 14.sp)
                        Text("Comienza a registrar tus gastos", color = TextGray.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            } else {
                ProfessionalBarChartHome(
                    data = data,
                    selectedIndex = selectedIndex,
                    onBarClick = { selectedIndex = if (selectedIndex == it) null else it }
                )
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    expenses: List<com.example.pocketguard.data.models.Expense> = emptyList()
) {
    // Ordenar por fecha descendente y tomar los últimos 5
    val recentExpenses = remember(expenses) {
        expenses.sortedByDescending { it.expenseDate }.take(5)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Actividad Reciente",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(Icons.Default.ChevronRight, null, tint = TextGray)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (recentExpenses.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Receipt,
                            null,
                            tint = TextGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sin actividad reciente", color = TextGray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    recentExpenses.forEachIndexed { index, expense ->
                        RecentExpenseItem(expense = expense)
                        if (index < recentExpenses.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentExpenseItem(expense: com.example.pocketguard.data.models.Expense) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Color(0xFFF39C12).copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ShoppingBag,
                null,
                tint = Color(0xFFF39C12),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                expense.name,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                maxLines = 1
            )
            Text(
                expense.expenseDate,
                fontSize = 12.sp,
                color = TextGray
            )
        }
        Text(
            "-$${String.format("%.2f", expense.amount)}",
            fontWeight = FontWeight.Bold,
            color = ErrorRed,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SmartAnalysisSection(
    distribution: com.example.pocketguard.data.models.SpendingDistribution = com.example.pocketguard.data.models.SpendingDistribution(0.0, 0.0, 0.0, 0f, 0f, 0f),
    insights: com.example.pocketguard.data.models.FinancialInsights = com.example.pocketguard.data.models.FinancialInsights(0.0, 0, 0)
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFFC107))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Análisis Inteligente", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === BARRA DE DISTRIBUCIÓN SEGMENTADA ===
            if (distribution.fixedExpenses > 0 || distribution.variableExpenses > 0 || distribution.savingsAvailable > 0) {
                Text("Distribución de Gastos", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(8.dp))

                // Barra horizontal segmentada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2C3E50))
                ) {
                    // Gastos Fijos - Verde Oscuro
                    if (distribution.fixedWeight > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(distribution.fixedWeight)
                                .background(Color(0xFF27AE60))
                        )
                    }
                    // Gastos Variables - Amarillo/Naranja
                    if (distribution.variableWeight > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(distribution.variableWeight)
                                .background(Color(0xFFF39C12))
                        )
                    }
                    // Ahorros - Verde Claro
                    if (distribution.savingsWeight > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(distribution.savingsWeight)
                                .background(Color(0xFF2ECC71))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Leyendas
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DistributionLegendItem(
                        color = Color(0xFF27AE60),
                        label = "Gastos Fijos",
                        amount = distribution.fixedExpenses,
                        percentage = (distribution.fixedWeight * 100).toInt()
                    )
                    DistributionLegendItem(
                        color = Color(0xFFF39C12),
                        label = "Gastos Variables",
                        amount = distribution.variableExpenses,
                        percentage = (distribution.variableWeight * 100).toInt()
                    )
                    DistributionLegendItem(
                        color = Color(0xFF2ECC71),
                        label = "Ahorros Disponibles",
                        amount = distribution.savingsAvailable,
                        percentage = (distribution.savingsWeight * 100).toInt()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // === CARDS DE INSIGHTS ===
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gasto Hormiga", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$${String.format("%.2f", insights.antExpensesTotal)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        "En ${insights.antExpensesCount} compras menores a $100",
                        fontSize = 12.sp,
                        color = White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF163E30)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Potencial de Ahorro", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${insights.savingsPotentialPercent}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        "Reduciendo gastos innecesarios",
                        fontSize = 12.sp,
                        color = White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun DistributionLegendItem(
    color: Color,
    label: String,
    amount: Double,
    percentage: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, color = White.copy(alpha = 0.9f))
        }
        Text(
            "$${String.format("%.0f", amount)} ($percentage%)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )
    }
}

// ==========================================
// COMPONENTES AUXILIARES
// ==========================================

@Composable
fun StatColumn(label: String, amount: String) {
    Column {
        Text(label, fontSize = 12.sp, color = TextGray)
        Text(amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun QuickStatCard(modifier: Modifier, icon: ImageVector, title: String, amount: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = GreenPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 12.sp, color = TextGray)
            Text(amount, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun UpcomingChargeItem(name: String, date: String, amount: String, daysLeft: String, icon: ImageVector) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Text(date, fontSize = 12.sp, color = TextGray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(amount, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(daysLeft, fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChartBar(label: String, fill: Float, isSelected: Boolean, onClick: () -> Unit) {
    // Animación suave de altura
    val animatedFill by animateFloatAsState(targetValue = fill, label = "barFill")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            indication = null, // Sin efecto ripple para que sea más limpio
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .width(24.dp) // Barra un poco más ancha para mejor tacto
                .fillMaxHeight(0.85f)
                .background(InputBackground, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedFill)
                    .align(Alignment.BottomCenter)
                    // Si está seleccionado, se oscurece un poco, si no es gris normal
                    .background(if(isSelected) Color(0xFF6B7280) else Color(0xFF9CA3AF), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
            // Punta Verde
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-(180 * 0.85 * animatedFill).toInt()).dp + 6.dp)
                    .background(GreenPrimary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if(isSelected) MaterialTheme.colorScheme.onBackground else TextGray,
            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ==========================================
// GRÁFICA DE BARRAS PROFESIONAL (HOME)
// ==========================================
@Composable
fun ProfessionalBarChartHome(
    data: List<DailyExpensePoint>,
    selectedIndex: Int?,
    onBarClick: (Int) -> Unit
) {
    if (data.isEmpty()) return
    val maxValue = data.maxOf { it.totalAmount }.takeIf { it > 0 } ?: 100.0

    val chartHeight = 220.dp

    Column(modifier = Modifier.fillMaxWidth()) {
        // Eje Y (Etiquetas arriba)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("${(maxValue * 0.25).toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("${(maxValue * 0.5).toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("${(maxValue * 0.75).toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text("${maxValue.toInt()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Área scrollable con barras
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(data.size) { index ->
                val point = data[index]
                val barHeightFraction = if (point.totalAmount == 0.0) 0.02f else (point.totalAmount / maxValue).toFloat()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(40.dp)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBarClick(index) }
                ) {
                    // Grid (Líneas horizontales) - Fondo
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .weight(1f)
                    ) {
                        // Líneas de referencia
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(5) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                )
                            }
                        }

                        // Barra
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .fillMaxHeight(barHeightFraction)
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (selectedIndex == index) GreenPrimary else GreenPrimary.copy(alpha = 0.7f)
                                )
                                .shadow(
                                    elevation = if (selectedIndex == index) 6.dp else 2.dp,
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                                    clip = false
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Etiqueta del día
                    Text(
                        text = point.dayLabel,
                        fontSize = 11.sp,
                        color = if (selectedIndex == index) GreenPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium
                    )

                    // Mostrar monto si está seleccionado
                    if (selectedIndex == index) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$${String.format("%.0f", point.totalAmount)}",
                            fontSize = 10.sp,
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Previews
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    PocketGuardTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    PocketGuardTheme(darkTheme = true) {
        HomeScreen()
    }
}

