package com.example.pocketguard.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.components.NewExpenseModal
import com.example.pocketguard.ui.theme.*

// Modelo de datos para Gastos
data class ExpenseUI(
    val id: String,
    val title: String,
    val category: String,
    val amount: String,
    val amountValue: Double,
    val date: String,
    val icon: ImageVector,
    val color: Color
)

// Modelo para la Gráfica
data class ChartPoint(val day: String, val value: Float, val displayAmount: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen() {
    var showNewExpenseModal by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<ExpenseUI?>(null) }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    // Gastos Mock
    val expenses = remember {
        mutableStateListOf(
            ExpenseUI("1", "Starbucks", "Alimentos", "-$85", 85.0, "02-06", Icons.Outlined.LocalCafe, Color(0xFFFFA500)),
            ExpenseUI("2", "Uber", "Transporte", "-$120", 120.0, "02-06", Icons.Outlined.DirectionsCar, Color(0xFF2ECC71)),
            ExpenseUI("3", "Comida Subway", "Alimentos", "-$129", 129.0, "02-06", Icons.Outlined.Fastfood, Color(0xFFFFA500)),
            ExpenseUI("4", "Supermercado", "Compras", "-$450", 450.0, "02-05", Icons.Outlined.ShoppingCart, Color(0xFF9146FF)),
            ExpenseUI("5", "Cine", "Ocio", "-$180", 180.0, "02-04", Icons.Outlined.Movie, Color(0xFFE74C3C))
        )
    }

    // Datos Gráfica
    val weeklyData = listOf(
        ChartPoint("vie", 0f, "$0"),
        ChartPoint("sáb", 0f, "$0"),
        ChartPoint("dom", 0f, "$0"),
        ChartPoint("lun", 350f, "$350"),
        ChartPoint("mar", 420f, "$420"),
        ChartPoint("mié", 950f, "$950"),
        ChartPoint("jue", 300f, "$300")
    )

    // Filtros dinámicos
    val usedCategories by remember(expenses) { derivedStateOf { expenses.map { it.category }.distinct().sorted() } }
    val dynamicFilters by remember(usedCategories) { derivedStateOf { listOf("Todas") + usedCategories } }

    LaunchedEffect(dynamicFilters) {
        if (selectedFilter != "Todas" && selectedFilter !in usedCategories) selectedFilter = "Todas"
    }

    val filteredExpenses = if (selectedFilter == "Todas") expenses else expenses.filter { it.category == selectedFilter }
    val currentTotal = filteredExpenses.sumOf { it.amountValue }

    // Dialogo Eliminar
    if (showDeleteDialog && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = White,
            icon = { Icon(Icons.Outlined.Delete, null, tint = ErrorRed) },
            title = { Text("Eliminar Gasto", fontWeight = FontWeight.Bold, color = TextDark) },
            text = { Text("¿Eliminar '${expenseToDelete?.title}'?", color = TextGray) },
            confirmButton = { Button(onClick = { expenses.remove(expenseToDelete); showDeleteDialog = false; expenseToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("Eliminar", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = TextDark) } }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewExpenseModal = true }, containerColor = GreenPrimary, contentColor = White, shape = CircleShape, elevation = FloatingActionButtonDefaults.elevation(8.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Gasto")
            }
        },
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().background(GreenPrimary).padding(top = 40.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("PocketGuard", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White); Text("Gestor de Gastos", fontSize = 12.sp, color = White.copy(alpha = 0.8f)) }
                    Box(modifier = Modifier.size(36.dp).background(White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) { Text("UD", color = White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(20.dp)) {

            Text("Gastos Diarios", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("Registra tus gastos", fontSize = 13.sp, color = TextGray)
            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickActionButton("Café", "$50", Icons.Filled.LocalCafe)
                QuickActionButton("Comida", "$150", Icons.Filled.Restaurant)
                QuickActionButton("Transporte", "$100", Icons.Filled.DirectionsCar)
                QuickActionButton("Snack", "$30", Icons.Filled.ShoppingCart)
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryCardDark(title = "Hoy", amount = "$334", modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                SummaryCardLight(title = "Mes", amount = "$2064", icon = "$", color = GreenPrimary, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                SummaryCardLight(title = "Hormiga", amount = "$85", icon = "🏷️", color = TextDark, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))

            // --- GRÁFICA "HOME STYLE" ---
            Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tendencia Semanal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                        if (selectedDayIndex != null) Text(text = weeklyData[selectedDayIndex!!].displayAmount, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GreenPrimary)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // COMPONENTE DE GRÁFICA MEJORADO
                    ProfessionalBarChart(
                        data = weeklyData,
                        selectedIndex = selectedDayIndex,
                        onBarClick = { selectedDayIndex = if (selectedDayIndex == it) null else it }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dynamicFilters) { filter -> FilterChipUI(text = filter, isSelected = selectedFilter == filter, onClick = { selectedFilter = filter }) }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lista de Gastos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                        Text("$${String.format("%.2f", currentTotal)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (filteredExpenses.isEmpty()) Text("No hay gastos en esta categoría.", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally))
                    else {
                        filteredExpenses.forEach { expense ->
                            ExpenseListItem(expense = expense, onDelete = { expenseToDelete = expense; showDeleteDialog = true })
                            if (expense != filteredExpenses.last()) Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showNewExpenseModal) {
        NewExpenseModal(onDismiss = { showNewExpenseModal = false }, onNewCategoryClick = { }, onSave = { desc, amountStr, category, date ->
            val amountValue = amountStr.toDoubleOrNull() ?: 0.0
            expenses.add(0, ExpenseUI(id = System.currentTimeMillis().toString(), title = desc, category = category, amount = "-$$amountStr", amountValue = amountValue, date = date.substring(0,5), icon = Icons.Outlined.ShoppingCart, color = GreenPrimary))
            showNewExpenseModal = false
        })
    }
}

// ==========================================
// GRÁFICA PROFESIONAL (Estilo Home)
// ==========================================
@Composable
fun ProfessionalBarChart(
    data: List<ChartPoint>,
    selectedIndex: Int?,
    onBarClick: (Int) -> Unit
) {
    if (data.isEmpty()) return
    val maxValue = data.maxOf { it.value }.takeIf { it > 0 } ?: 100f

    // Altura fija de la gráfica
    val chartHeight = 180.dp

    Row(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
        // Eje Y (Etiquetas a la izquierda)
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text("${maxValue.toInt()}", fontSize = 10.sp, color = TextGray)
            Text("${(maxValue * 0.75).toInt()}", fontSize = 10.sp, color = TextGray)
            Text("${(maxValue * 0.5).toInt()}", fontSize = 10.sp, color = TextGray)
            Text("${(maxValue * 0.25).toInt()}", fontSize = 10.sp, color = TextGray)
            Text("0", fontSize = 10.sp, color = TextGray)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Área de Barras y Grid
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // 1. Grid (Líneas horizontales)
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                repeat(5) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                }
            }

            // 2. Barras
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEachIndexed { index, point ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { onBarClick(index) }
                    ) {
                        // Barra
                        Box(
                            modifier = Modifier
                                .width(28.dp) // Ancho estilo Home
                                .fillMaxHeight(0.9f * (if (point.value == 0f) 0.01f else (point.value / maxValue))) // 90% altura del área para dejar espacio al texto abajo
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (selectedIndex == index) GreenPrimary else if (point.value > 0) Color.LightGray.copy(alpha = 0.5f) else Color.Transparent) // Gris por defecto, Verde si select
                        ) {
                            // Si es estilo "Home", a veces tienen un tope de color.
                            // Aquí usamos: Gris base, y si seleccionas se pone Verde.
                            // O si prefieres: Siempre verde pero tenue, y fuerte al seleccionar.
                            // Usemos: Siempre Verde suave, Verde Fuerte al seleccionar.
                            Box(modifier = Modifier.fillMaxSize().background(if(selectedIndex == index) GreenPrimary else GreenPrimary.copy(alpha = 0.6f)))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Etiqueta X
                        Text(
                            text = point.day,
                            fontSize = 11.sp,
                            color = if (selectedIndex == index) GreenPrimary else TextGray,
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ... Resto de componentes (FilterChipUI, QuickActionButton, etc.) se mantienen igual ...
@Composable
fun FilterChipUI(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.background(color = if (isSelected) GreenPrimary else White, shape = RoundedCornerShape(20.dp)).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = text, color = if (isSelected) White else TextDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun QuickActionButton(title: String, price: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp).background(White, RoundedCornerShape(16.dp)).clickable { }.padding(vertical = 12.dp)) {
        Box(modifier = Modifier.size(40.dp).background(GreenPrimary.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.height(8.dp)); Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark); Text(price, fontSize = 10.sp, color = TextGray)
    }
}

@Composable
fun SummaryCardDark(title: String, amount: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(Color(0xFF1F2937), RoundedCornerShape(16.dp)).padding(16.dp).height(100.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Icon(Icons.Filled.TrendingDown, null, tint = White, modifier = Modifier.size(20.dp))
        Column { Text(title, fontSize = 12.sp, color = White.copy(0.7f)); Text(amount, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = White) }
    }
}

@Composable
fun SummaryCardLight(title: String, amount: String, icon: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(White, RoundedCornerShape(16.dp)).padding(16.dp).height(100.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Text(icon, fontSize = 20.sp, color = color, fontWeight = FontWeight.Bold)
        Column { Text(title, fontSize = 12.sp, color = TextGray); Text(amount, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark) }
    }
}

@Composable
fun ExpenseListItem(expense: ExpenseUI, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(expense.color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(expense.icon, null, tint = expense.color, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) { Text(expense.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark); Text(expense.date, fontSize = 11.sp, color = TextGray) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(expense.amount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = ErrorRed.copy(0.7f), modifier = Modifier.size(18.dp).clickable { onDelete() })
        }
    }
}