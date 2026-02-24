package com.example.pocketguard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSubscriptionModal(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit // name, price, category, cycle
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var selectedCycle by remember { mutableStateOf("Mensual") }
    var nextPaymentDate by remember { mutableStateOf("dd/mm/aaaa") }

    // Datos visuales de categorías (Coincidentes con tu imagen)
    val categories = listOf(
        CategoryData("Entretenimiento", Icons.Outlined.Movie, GreenPrimary),
        CategoryData("Música", Icons.Outlined.MusicNote, Color(0xFF9146FF)),
        CategoryData("Fitness", Icons.Outlined.FitnessCenter, Color(0xFFFF9900)),
        CategoryData("Noticias", Icons.Outlined.Article, Color(0xFF00A4EF)),
        CategoryData("Almacenamiento", Icons.Outlined.Cloud, Color(0xFF7F8C8D)),
        CategoryData("Compras", Icons.Outlined.ShoppingBag, Color(0xFFE91E63)),
        CategoryData("Educación", Icons.Outlined.School, Color(0xFF2ECC71)),
        CategoryData("Gaming", Icons.Outlined.Gamepad, Color(0xFF9B59B6)),
        CategoryData("Trabajo", Icons.Outlined.Work, Color(0xFF34495E)),
        CategoryData("Streaming", Icons.Outlined.Tv, GreenPrimary)
    )

    val cycles = listOf("Diario", "Semanal", "Mensual", "Anual")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(GreenPrimary, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Nueva Suscripción", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                        Text("Agrega un nuevo servicio", fontSize = 14.sp, color = TextGray)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(InputBackground, CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Input: Nombre del Servicio
            LabelText("Nombre del Servicio")
            CustomTextField(value = name, onValueChange = { name = it }, placeholder = "Ej: Netflix, Spotify, Disney+...")

            Spacer(modifier = Modifier.height(24.dp))

            // Selector Categoría (Grid)
            LabelText("Selecciona Categoría")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // 3 columnas para iconos grandes
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(260.dp) // Altura suficiente para mostrar varias filas
            ) {
                items(categories.size) { index ->
                    BigCategoryItem(
                        data = categories[index],
                        isSelected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input: Monto
            LabelText("Monto")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(InputBackground, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$ ", fontWeight = FontWeight.Bold, color = TextGray)
                    BasicTextField(
                        value = price,
                        onValueChange = { price = it },
                        textStyle = TextStyle(fontSize = 18.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    )
                    if (price.isEmpty()) Text("0.00", color = TextGray.copy(alpha = 0.5f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Frecuencia de Pago (Chips)
            LabelText("Frecuencia de Pago")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                cycles.forEach { cycle ->
                    val isSelected = selectedCycle == cycle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) GreenPrimary else InputBackground)
                            .clickable { selectedCycle = cycle },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cycle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) White else TextGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Próxima Fecha
            LabelText("Próxima Fecha de Cargo")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(InputBackground, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(nextPaymentDate, color = if(nextPaymentDate == "dd/mm/aaaa") TextGray else TextDark)
                    Icon(Icons.Default.CalendarToday, null, tint = TextDark, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // BOTÓN GUARDAR
            Button(
                onClick = { onSave(name, price, categories[selectedCategoryIndex].name, selectedCycle) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Guardar Suscripción", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- COMPONENTE VISUAL PARA CATEGORÍA GRANDE (Como en tu imagen) ---
@Composable
fun BigCategoryItem(data: CategoryData, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Cuadrado perfecto
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GreenPrimary else InputBackground,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = if (isSelected) GreenPrimary.copy(alpha = 0.05f) else White,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Icono en círculo de color
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(data.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(data.icon, null, tint = White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.name,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = TextDark
            )
        }

        // Check verde en la esquina si está seleccionado
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(16.dp)
                    .background(GreenPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(10.dp))
            }
        }
    }
}