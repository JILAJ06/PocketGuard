package com.example.pocketguard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pocketguard.ui.theme.*

// ==========================================
// 1. MODAL DE NUEVA CATEGORÍA (Nivel Superior)
// ==========================================
@Composable
fun NewCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, ImageVector, Color) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(Icons.Outlined.Coffee) }
    var selectedColor by remember { mutableStateOf(GreenPrimary) }

    val icons = listOf(
        Icons.Outlined.Coffee, Icons.Outlined.Fastfood, Icons.Outlined.DirectionsCar,
        Icons.Outlined.Flight, Icons.Outlined.ShoppingCart, Icons.Outlined.CardGiftcard,
        Icons.Outlined.Checkroom, Icons.Outlined.Movie, Icons.Outlined.FavoriteBorder,
        Icons.Outlined.Home, Icons.Outlined.Book, Icons.Outlined.Smartphone, Icons.Outlined.AutoAwesome
    )

    val colors = listOf(
        Color(0xFF00A4EF), Color(0xFF2ECC71), Color(0xFFFF9900),
        Color(0xFF9146FF), Color(0xFFE50914), Color(0xFFE91E63),
        Color(0xFF34495E), Color(0xFF95A5A6), Color(0xFF1ABC9C)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(GreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Nueva Categoría", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Personaliza tus categorías", fontSize = 12.sp, color = TextGray)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Input Nombre
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    LabelText("Nombre de la Categoría")
                    CustomTextField(value = name, onValueChange = { name = it }, placeholder = "Ej: Mascotas, Deportes...")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Selector Iconos
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    LabelText("Selecciona un Icono")
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(120.dp)
                    ) {
                        items(icons) { icon ->
                            IconSelectorItem(icon, selectedIcon == icon) { selectedIcon = icon }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Selector Colores
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    LabelText("Elige un Color")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colors.take(5).forEach { color ->
                            ColorSelectorItem(color, selectedColor == color) { selectedColor = color }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colors.drop(5).take(5).forEach { color ->
                            ColorSelectorItem(color, selectedColor == color) { selectedColor = color }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Botón Crear
                Button(
                    onClick = { onSave(name, selectedIcon, selectedColor) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Crear Categoría", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 2. MODAL DE NUEVO GASTO (Nivel Base)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseModal(
    onDismiss: () -> Unit,
    onNewCategoryClick: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    // Categorías (Datos visuales simulados para coincidir con el diseño)
    val categories = listOf(
        CategoryData("Alimentos", Icons.Outlined.Coffee, Color(0xFFFFA500)),
        CategoryData("Transporte", Icons.Outlined.DirectionsCar, Color(0xFF2ECC71)),
        CategoryData("Compras", Icons.Outlined.ShoppingCart, Color(0xFF9146FF)),
        CategoryData("Hogar", Icons.Outlined.Home, Color(0xFF00A4EF)),
        CategoryData("Entretenimiento", Icons.Outlined.Book, Color(0xFFE74C3C)),
        CategoryData("Salud", Icons.Outlined.FavoriteBorder, Color(0xFFE91E63)),
        CategoryData("Educación", Icons.Outlined.School, Color(0xFF34495E)),
        CategoryData("Otros", Icons.Outlined.MoreHoriz, Color(0xFF95A5A6))
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.9f) // Ocupa casi toda la pantalla
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
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
                        Text("Nuevo Gasto", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Registra tu compra", fontSize = 14.sp, color = TextGray)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.background(InputBackground, CircleShape)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Input: ¿Qué compraste?
            LabelText("¿Qué compraste?")
            CustomTextField(value = description, onValueChange = { description = it }, placeholder = "Ej: Café con amigos, Uber al trabajo...")

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Categoría
            LabelText("Selecciona Categoría")
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // 4 columnas como en el diseño
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(240.dp) // Altura fija para el grid
            ) {
                // Categorías normales
                items(categories.size) { index ->
                    CategoryItem(
                        data = categories[index],
                        isSelected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index }
                    )
                }
                // Botón "Nueva"
                item {
                    NewCategoryButton(onClick = onNewCategoryClick)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Fila Inferior: Monto y Fecha
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Input Monto
                Column(modifier = Modifier.weight(1f)) {
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
                                value = amount,
                                onValueChange = { amount = it },
                                textStyle = TextStyle(fontSize = 18.sp, color = TextDark, fontWeight = FontWeight.Bold)
                            )
                            if (amount.isEmpty()) Text("0.00", color = TextGray.copy(alpha = 0.5f))
                        }
                    }
                }
                // Input Fecha
                Column(modifier = Modifier.weight(1f)) {
                    LabelText("Fecha")
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
                            Text("06/02/2026", color = TextDark, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextDark, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// COMPONENTES AUXILIARES (ESTILOS)
// ==========================================

data class CategoryData(val name: String, val icon: ImageVector, val color: Color)

@Composable
fun LabelText(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        if(text == "Monto") Icon(Icons.Default.AttachMoney, null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
        else if(text == "Selecciona Categoría") Icon(Icons.Default.Dashboard, null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
        else if(text.contains("Qué")) Icon(Icons.Outlined.LocalOffer, null, tint = GreenPrimary, modifier = Modifier.size(16.dp))

        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
    }
}

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(InputBackground, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) Text(placeholder, color = TextGray.copy(alpha = 0.5f), fontSize = 14.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = TextDark),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CategoryItem(data: CategoryData, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) GreenPrimary else InputBackground,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(if (isSelected) GreenPrimary.copy(alpha = 0.1f) else White, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = if (isSelected) GreenPrimary else TextGray,
                modifier = Modifier.size(28.dp)
            )
            // Checkmark Badge
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(20.dp)
                        .background(GreenPrimary, CircleShape)
                        .border(2.dp, White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(12.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = data.name,
            fontSize = 10.sp,
            color = if (isSelected) GreenPrimary else TextGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NewCategoryButton(onClick: () -> Unit) {
    val stroke = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(color = GreenPrimary, style = stroke, cornerRadius = CornerRadius(16.dp.toPx()))
            }
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GreenPrimary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Nueva", fontSize = 10.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun IconSelectorItem(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(1.dp, if (isSelected) GreenPrimary else InputBackground, RoundedCornerShape(12.dp))
            .background(if (isSelected) GreenPrimary.copy(alpha = 0.1f) else White, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = if(isSelected) GreenPrimary else TextGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ColorSelectorItem(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(20.dp))
        }
    }
}