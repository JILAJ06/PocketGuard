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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.pocketguard.components.ExpenseCategoryData
import com.example.pocketguard.components.NewExpenseModal
import com.example.pocketguard.data.models.Expense
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.CategoriesViewModel
import com.example.pocketguard.presentation.viewmodel.ExpensesViewModel
import com.example.pocketguard.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

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

data class ChartPoint(val day: String, val value: Float, val displayAmount: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    modifier: Modifier = Modifier,
    onAuthExpired: () -> Unit = {}
) {
    val viewModel: ExpensesViewModel = viewModel(
        factory = ServiceLocator.getExpensesViewModelFactory()
    )
    val categoriesViewModel: CategoriesViewModel = viewModel(
        factory = ServiceLocator.getCategoriesViewModelFactory()
    )

    val state by viewModel.state.collectAsStateWithLifecycle()
    val categoriesState by categoriesViewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadExpenses()
        categoriesViewModel.loadCategories()
    }

    LaunchedEffect(state.isUnauthorized || categoriesState.isUnauthorized) {
        if (state.isUnauthorized || categoriesState.isUnauthorized) {
            onAuthExpired()
        }
    }

    // Snackbars para errores
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

    var showNewExpenseModal by remember { mutableStateOf(false) }
    var showEditExpenseModal by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseUI?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val categoryItems: List<ExpenseCategoryData> = remember(categoriesState.categories) {
        categoriesState.categories.map { category ->
            ExpenseCategoryData(
                id = category.id,
                name = category.name,
                icon = Icons.Outlined.Category,
                color = Color(android.graphics.Color.parseColor(category.color_hex ?: "#95A5A6"))
            )
        }
    }

    val expenses = remember(state.expenses, categoriesState.categories) {
        state.expenses.map { expense ->
            val currentCategory = categoriesState.categories.firstOrNull { it.name == expense.categoryName }
            val categoryColor = currentCategory?.color_hex ?: expense.categoryColor
            val categoryIcon = if (currentCategory != null) {
                com.example.pocketguard.utils.IconMapper.getIconByName(currentCategory.icon_name)
            } else {
                Icons.Outlined.Category
            }

            ExpenseUI(
                id = expense.id,
                title = expense.name,
                category = expense.categoryName,
                amount = "-$${String.format("%.2f", expense.amount)}",
                amountValue = expense.amount,
                date = expense.expenseDate.substring(5, 10).replace("-", "-"),
                icon = categoryIcon,
                color = Color(android.graphics.Color.parseColor(categoryColor))
            )
        }
    }

    val usedCategories by remember(expenses) { derivedStateOf { expenses.map { it.category }.distinct().sorted() } }
    val dynamicFilters by remember(usedCategories) { derivedStateOf { listOf("Todas") + usedCategories } }

    var selectedFilter by remember { mutableStateOf("Todas") }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(dynamicFilters) {
        if (selectedFilter != "Todas" && selectedFilter !in usedCategories) selectedFilter = "Todas"
    }

    val filteredExpenses = remember(expenses, selectedFilter, searchQuery) {
        var result = if (selectedFilter == "Todas") expenses else expenses.filter { it.category == selectedFilter }
        if (searchQuery.isNotEmpty()) {
            result = result.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
        result
    }
    val currentTotal = filteredExpenses.sumOf { it.amountValue }

    val weeklyData = remember(filteredExpenses) {
        val daysOfWeek = listOf("dom", "lun", "mar", "mié", "jue", "vie", "sáb")
        val today = java.time.LocalDate.now()
        daysOfWeek.mapIndexed { index, day ->
            val targetDate = today.minusDays((6 - index).toLong())
            val total = filteredExpenses
                .filter { it.date.startsWith(targetDate.toString().substring(5, 10)) }
                .sumOf { it.amountValue }
            ChartPoint(day, total.toFloat(), "$${"%.0f".format(total)}")
        }
    }

    if (showDeleteDialog && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Outlined.Delete, null, tint = ErrorRed) },
            title = { Text("Eliminar Gasto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("¿Eliminar '${expenseToDelete?.name}'?", color = TextGray) },
            confirmButton = { Button(onClick = { viewModel.deleteExpense(expenseToDelete!!.id); showDeleteDialog = false; expenseToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("Eliminar", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurface) } }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (state.errorMessage.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(state.errorMessage, color = ErrorRed, fontSize = 14.sp)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(20.dp)) {


                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(0.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Tendencia Semanal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                            if (selectedDayIndex != null) Text(text = weeklyData[selectedDayIndex!!].displayAmount, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GreenPrimary)
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        ProfessionalBarChart(
                            data = weeklyData,
                            selectedIndex = selectedDayIndex,
                            onBarClick = { selectedDayIndex = if (selectedDayIndex == it) null else it }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                var isRefreshing by remember { mutableStateOf(false) }

                androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        scope.launch {
                            viewModel.loadExpenses()
                            categoriesViewModel.loadCategories()
                            kotlinx.coroutines.delay(500)
                            isRefreshing = false
                        }
                    }
                ) {
                    Column {
                        // Barra de búsqueda
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar gastos...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(dynamicFilters) { filter -> FilterChipUI(text = filter, isSelected = selectedFilter == filter, onClick = { selectedFilter = filter; viewModel.setFilter(filter) }) }
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Lista de Gastos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                    Text("$${String.format("%.2f", currentTotal)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                if (filteredExpenses.isEmpty()) Text("No hay gastos en esta categoría.", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally))
                                else {
                                    filteredExpenses.forEach { expense ->
                                        ExpenseListItem(
                                            expense = expense,
                                    onDelete = {
                                        expenseToDelete = state.expenses.find { it.id == expense.id }
                                        showDeleteDialog = true
                                    },
                                    onEdit = {
                                        expenseToEdit = expense
                                        showEditExpenseModal = true
                                    }
                                )
                                if (expense != filteredExpenses.last()) Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showNewExpenseModal) {
        NewExpenseModal(
            categories = categoryItems,
            onDismiss = { showNewExpenseModal = false },
            onSave = { desc, amountStr, categoryId, date ->
                val amountValue = amountStr.toDoubleOrNull() ?: 0.0
                viewModel.createExpense(desc, amountValue, date, categoryId)
                showNewExpenseModal = false
            },
            onCreateCategory = { name, colorHex, iconName ->
                categoriesViewModel.createCategory(name, null, iconName, colorHex)
            }
        )
    }

    if (showEditExpenseModal && expenseToEdit != null) {
        NewExpenseModal(
            categories = categoryItems,
            onDismiss = {
                showEditExpenseModal = false
                expenseToEdit = null
            },
            onSave = { desc, amountStr, categoryId, date ->
                val amountValue = amountStr.toDoubleOrNull() ?: 0.0
                viewModel.updateExpense(
                    id = expenseToEdit!!.id,
                    name = desc,
                    amount = amountValue,
                    expenseDate = date,
                    categoryId = categoryId
                )
                showEditExpenseModal = false
                expenseToEdit = null
            },
            onCreateCategory = { name, colorHex, iconName ->
                categoriesViewModel.createCategory(name, null, iconName, colorHex)
            },
            // VALORES INICIALES PARA EDICIÓN
            initialExpenseId = expenseToEdit!!.id,
            initialDescription = expenseToEdit!!.title,
            initialAmount = expenseToEdit!!.amountValue.toString(),
            initialCategoryId = categoryItems.find { it.name == expenseToEdit!!.category }?.id ?: "",
            initialDate = "2026-${expenseToEdit!!.date}"
        )
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
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun QuickActionButton(title: String, price: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clickable { }
            .padding(vertical = 12.dp)
    ) {
        Box(modifier = Modifier.size(40.dp).background(GreenPrimary.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(price, fontSize = 10.sp, color = TextGray)
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
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .height(100.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(icon, fontSize = 20.sp, color = color, fontWeight = FontWeight.Bold)
        Column {
            Text(title, fontSize = 12.sp, color = TextGray)
            Text(amount, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun ExpenseListItem(expense: ExpenseUI, onDelete: () -> Unit, onEdit: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(expense.color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(expense.icon, null, tint = expense.color, modifier = Modifier.size(20.dp)) }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(expense.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(expense.date, fontSize = 11.sp, color = TextGray)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(expense.amount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = ErrorRed.copy(0.7f), modifier = Modifier.size(18.dp).clickable { onDelete() })
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExpensesScreenPreview() {
    PocketGuardTheme {
        ExpensesScreen(onAuthExpired = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ExpensesScreenDarkPreview() {
    PocketGuardTheme(darkTheme = true) {
        ExpensesScreen(onAuthExpired = {})
    }
}
