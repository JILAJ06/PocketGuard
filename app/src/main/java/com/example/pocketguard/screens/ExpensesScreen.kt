package com.example.pocketguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.pocketguard.ui.theme.*
import com.example.pocketguard.components.NewExpenseModal
import com.example.pocketguard.components.NewCategoryDialog

@Composable
fun ExpensesScreen() {
    // Estado de filtros
    var selectedCategory by remember { mutableStateOf("Todas") }
    var showNewExpenseModal by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    val categories = listOf("Todas", "Alimentos", "Transporte", "Compras", "Servicios")

    // Datos Mock de gastos
    val expenses = listOf(
        ExpenseItemData("Starbucks", "02-06", "-$85", Icons.Default.Coffee, "Alimentos"),
        ExpenseItemData("Uber", "02-06", "-$120", Icons.Default.DirectionsCar, "Transporte"),
        ExpenseItemData("Comida Subway", "02-06", "-$129", Icons.Default.Fastfood, "Alimentos"),
        ExpenseItemData("Supermercado", "02-05", "-$450", Icons.Default.ShoppingCart, "Compras"),
        ExpenseItemData("Gasolina", "02-05", "-$500", Icons.Default.LocalGasStation, "Transporte"),
        ExpenseItemData("Cine", "02-04", "-$180", Icons.Default.Movie, "Ocio")
    )

    // Filtrado
    val filteredList = if (selectedCategory == "Todas") expenses else expenses.filter { it.category == selectedCategory }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = { // <--- Sin el signo igual antes de la llave
            FloatingActionButton(
                onClick = { showNewExpenseModal = true },
                containerColor = GreenPrimary,
                contentColor = White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Gasto")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Cabecera Verde (PocketGuard)
            ExpensesHeader()

            // Contenedor principal con padding
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Tarjeta "Gastos Diarios" (Accesos Rápidos)
                DailyExpensesCard()

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Fila de Resumen (Hoy, Mes, Hormiga)
                SummaryRow()

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Gráfica Semanal
                WeeklyTrendChart()

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Filtros (Chips)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChipComponent(
                            label = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 6. Lista de Gastos (Header + Items)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lista de Gastos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Text("$2064", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredList.forEach { expense ->
                        ExpenseRowItem(expense)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 7. Tarjeta Alerta "Gasto Hormiga" (Roja)
                AntExpenseAlert()

                Spacer(modifier = Modifier.height(80.dp)) // Espacio final
            }
        }
    }
    if (showNewExpenseModal) {
        NewExpenseModal(
            onDismiss = { showNewExpenseModal = false },
            onNewCategoryClick = {
                // Mantenemos el modal de gasto abierto y mostramos el diálogo encima
                showNewCategoryDialog = true
            }
        )
    }

    if (showNewCategoryDialog) {
        NewCategoryDialog(
            onDismiss = { showNewCategoryDialog = false },
            onSave = { name, icon, color ->
                // Aquí iría la lógica para guardar la categoría
                showNewCategoryDialog = false
            }
        )
    }
}

// ==========================================
// COMPONENTES UI ESPECÍFICOS DE ESTA VISTA
// ==========================================

@Composable
fun ExpensesHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp) // Altura reducida para solo mostrar logo y avatar
            .background(
                color = GreenPrimary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("PocketGuard", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = White)
                Text("Gestor de Gastos", fontSize = 12.sp, color = White.copy(alpha = 0.8f))
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("UD", color = White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DailyExpensesCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gastos Diarios", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Registra tus gastos", color = TextGray, fontSize = 12.sp)
                }
                // Botón + Verde
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary)
                        .clickable { /* Add */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = White)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Botones Rápidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionItem(Icons.Default.Coffee, "Café", "$50")
                QuickActionItem(Icons.Default.Fastfood, "Comida", "$150")
                QuickActionItem(Icons.Default.DirectionsCar, "Transporte", "$100")
                QuickActionItem(Icons.Default.Cookie, "Snack", "$30")
            }
        }
    }
}

@Composable
fun QuickActionItem(icon: ImageVector, label: String, price: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(1.dp, InputBackground, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 11.sp, color = TextGray)
        Text(price, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
fun SummaryRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card Oscura (HOY)
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Hoy", color = White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text("$334", color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        // Card Blanca (MES)
        SummaryMiniCard(modifier = Modifier.weight(1f), icon = Icons.Default.AttachMoney, title = "Mes", amount = "$2064", color = GreenPrimary)

        // Card Blanca (HORMIGA)
        SummaryMiniCard(modifier = Modifier.weight(1f), icon = Icons.Default.LocalOffer, title = "Hormiga", amount = "$85", color = TextDark)
    }
}

@Composable
fun SummaryMiniCard(modifier: Modifier, icon: ImageVector, title: String, amount: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = TextGray, fontSize = 12.sp)
            Text(amount, color = TextDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun WeeklyTrendChart() {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tendencia Semanal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Barras simuladas
                SimpleBar("vie", 0.2f)
                SimpleBar("sáb", 0.3f)
                SimpleBar("dom", 0.1f)
                SimpleBar("lun", 0.4f)
                SimpleBar("mar", 0.5f)
                SimpleBar("mié", 0.8f) // Pico
                SimpleBar("jue", 0.4f)
            }
        }
    }
}

@Composable
fun SimpleBar(day: String, heightPerc: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(heightPerc)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(GreenPrimary)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(day, fontSize = 10.sp, color = TextGray)
    }
}

@Composable
fun FilterChipComponent(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) GreenPrimary else White)
            .border(1.dp, if (isSelected) GreenPrimary else Color.Transparent, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) White else TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ExpenseRowItem(item: ExpenseItemData) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(BeigeItem, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = Color(0xFFE67E22), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold, color = TextDark)
                Text(item.date, fontSize = 12.sp, color = TextGray)
            }
            Text(item.amount, fontWeight = FontWeight.Bold, color = ErrorRed) // Rojo para gastos
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = ErrorRed.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun AntExpenseAlert() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF3B30)), // Rojo Brillante
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gasto Hormiga", color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Compras Pequeñas", color = White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text("$85", color = White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text("1 compras < $100", color = White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

// Data Class Local
data class ExpenseItemData(val name: String, val date: String, val amount: String, val icon: ImageVector,
                           val category: String)