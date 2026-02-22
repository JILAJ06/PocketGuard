package com.example.pocketguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.ui.theme.*

@Composable
fun HomeScreen() {
    // Scaffold nos permite agregar la Barra Inferior (BottomBar) más adelante fácilmente
    Scaffold(
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // Scroll vertical para toda la pantalla
        ) {
            HomeHeader()

            // Contenido principal con padding lateral
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Tarjeta de alerta flotante (negativa en Y para solaparse con el header)
                AlertBanner(modifier = Modifier.offset(y = (-16).dp))

                // Saldo Principal
                BalanceCard()
                Spacer(modifier = Modifier.height(16.dp))

                // Widgets pequeños (Ingresos / Suscripciones)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmallWidget(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingUp,
                        label = "Ingresos",
                        amount = "$5000"
                    )
                    SmallWidget(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Subscriptions,
                        label = "8 Suscripciones",
                        amount = "$800"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sección Próximos Cargos
                SectionTitle("Próximos Cargos", Icons.Default.CalendarToday)
                UpcomingChargesCard()

                Spacer(modifier = Modifier.height(24.dp))

                // Sección Actividad Reciente
                SectionTitle("Actividad Reciente", Icons.Default.ChevronRight)
                RecentActivityList()

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta Oscura (Análisis)
                SmartAnalysisCard()

                Spacer(modifier = Modifier.height(30.dp)) // Espacio final
            }
        }
    }
}

// --- SUB-COMPONENTES DE LA PANTALLA ---

@Composable
private fun HomeHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp) // Altura para dar espacio al diseño
            .background(
                color = GreenPrimary,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text("PocketGuard", style = MaterialTheme.typography.titleLarge, color = White, fontWeight = FontWeight.Bold)
                Text("Gestor de Gastos", style = MaterialTheme.typography.bodyMedium, color = White.copy(alpha = 0.8f))
            }
            // Avatar UD
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
private fun AlertBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = ErrorRed)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Próximos cargos en 72h", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("3 suscripciones por $413 MXN", fontSize = 12.sp, color = TextGray)
            }
        }
    }
}

@Composable
private fun BalanceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GreenPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat style según diseño
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saldo Disponible", color = White.copy(alpha = 0.9f))
                Surface(
                    color = White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("12.0%", color = White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Text("$600", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = White)
            Text("Después de gastos y suscripciones", fontSize = 12.sp, color = White.copy(alpha = 0.7f))

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Ingresos", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    Text("$5000", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                }
                Column {
                    Text("Gastos", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    Text("$3600", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                }
                Column {
                    Text("Suscripciones", fontSize = 12.sp, color = White.copy(alpha = 0.7f))
                    Text("$800", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                }
            }
        }
    }
}

@Composable
private fun SmallWidget(modifier: Modifier = Modifier, icon: ImageVector, label: String, amount: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = GreenPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = TextGray)
            Text(amount, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun UpcomingChargesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título de la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Próximos Cargos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista vertical de items
            UpcomingItem(name = "Netflix", date = "10 Feb", amount = "$199", daysLeft = "4d")
            Spacer(modifier = Modifier.height(8.dp))
            UpcomingItem(name = "Spotify", date = "12 Feb", amount = "$115", daysLeft = "6d")
            Spacer(modifier = Modifier.height(8.dp))
            UpcomingItem(name = "Amazon Prime", date = "15 Feb", amount = "$99", daysLeft = "9d")
        }
    }
}

@Composable
fun UpcomingItem(name: String, date: String, amount: String, daysLeft: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = BeigeItem, shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de tarjeta (simulado)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CreditCard, contentDescription = null, tint = GreenPrimary)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Texto Central
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(text = date, fontSize = 12.sp, color = TextGray)
        }

        // Monto y Días
        Column(horizontalAlignment = Alignment.End) {
            Text(text = amount, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = daysLeft, fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentActivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Actividad Reciente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Items
            ActivityItem(name = "Starbucks", date = "Hoy", amount = "-$85", icon = Icons.Default.ShowChart) // Icono tendencia
            ActivityItem(name = "Uber", date = "Hoy", amount = "-$120", icon = Icons.Default.ShowChart)
            ActivityItem(name = "HBO Max", date = "Ayer", amount = "-$149", icon = Icons.Default.CreditCard)
            ActivityItem(name = "Supermercado", date = "Ayer", amount = "-$450", icon = Icons.Default.ShowChart)
        }
    }
}

// Nota: Asegúrate de que ActivityItem use un fondo transparente o blanco según necesites.
// En este diseño parece que los items están sobre el blanco de la tarjeta, así que:
@Composable
fun ActivityItem(name: String, date: String, amount: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Sin background propio, usan el de la Card
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BeigeItem, RoundedCornerShape(12.dp)), // Fondo beige suave para el icono
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(date, fontSize = 12.sp, color = TextGray)
        }
        Text(amount, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
fun SmartAnalysisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Análisis Inteligente", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Bloque Gasto Hormiga (Gris oscuro)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Gasto Hormiga", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text("$1,240", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("En compras menores a $100", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Bloque Potencial de Ahorro (Verde oscuro)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SavingsDarkGreen, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Potencial de Ahorro", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    Text("20%", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Reduciendo gastos innecesarios", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tendencia Mensual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Simulación de Gráfica de Barras (Row con Boxes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom // Alineadas abajo
            ) {
                // Mes 1
                ChartBarGroup(label = "Mar", greyHeight = 0.7f, greenHeight = 0.3f)
                // Mes 2
                ChartBarGroup(label = "Abr", greyHeight = 0.9f, greenHeight = 0.3f)
                // Mes 3
                ChartBarGroup(label = "May", greyHeight = 0.8f, greenHeight = 0.3f)
                // Mes 4
                ChartBarGroup(label = "Jun", greyHeight = 1.0f, greenHeight = 0.3f)
            }
        }
    }
}

@Composable
fun ChartBarGroup(label: String, greyHeight: Float, greenHeight: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Barra Gris (Ingresos)
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight(greyHeight) // Porcentaje de altura
                    .background(ChartBarGrey, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
            // Barra Verde (Gastos/Saldo)
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight(greenHeight)
                    .background(ChartBarGreen, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = TextGray)
    }
}
@Preview
@Composable
fun HomePreview() {
    PocketGuardTheme {
        HomeScreen()
    }
}