package com.example.pocketguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketguard.components.NewCategoryDialog
import com.example.pocketguard.components.colorToHex
import com.example.pocketguard.presentation.di.ServiceLocator
import com.example.pocketguard.presentation.viewmodel.AddSubscriptionViewModel
import com.example.pocketguard.presentation.viewmodel.CategoriesViewModel
import com.example.pocketguard.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    subscriptionId: String? = null,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAuthExpired: () -> Unit = {}
) {
    val viewModel: AddSubscriptionViewModel = viewModel(
        factory = ServiceLocator.getAddSubscriptionViewModelFactory()
    )
    val categoriesViewModel: CategoriesViewModel = viewModel(
        factory = ServiceLocator.getCategoriesViewModelFactory()
    )
    val cardsViewModel: com.example.pocketguard.presentation.viewmodel.CardsViewModel = viewModel(
        factory = ServiceLocator.getCardsViewModelFactory()
    )

    val state by viewModel.state.collectAsStateWithLifecycle()
    val categoriesState by categoriesViewModel.state.collectAsStateWithLifecycle()
    val cardsState by cardsViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        categoriesViewModel.loadCategories()
        cardsViewModel.loadCards()
    }

    LaunchedEffect(state.isUnauthorized || categoriesState.isUnauthorized) {
        if (state.isUnauthorized || categoriesState.isUnauthorized) {
            onAuthExpired()
        }
    }

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(BrandNetflix) }
    var categoryId by remember { mutableStateOf("") }
    var categoryName by remember { mutableStateOf("") }
    var dateDisplay by remember { mutableStateOf("Seleccionar fecha") }
    var billingCycleId by remember { mutableStateOf(3) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showCycleDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val availableColors = listOf(BrandNetflix, BrandSpotify, BrandAmazon, BrandHBO, BrandDisney, BrandApple, GreenPrimary)

    val screenTitle = if (subscriptionId != null) "Editar Suscripción" else "Nueva Suscripción"
    val buttonText = if (subscriptionId != null) "Guardar Cambios" else "Guardar Suscripción"

    LaunchedEffect(subscriptionId) {
        if (subscriptionId != null) {
            viewModel.loadSubscription(subscriptionId)
        }
    }

    LaunchedEffect(state.subscription, categoriesState.categories) {
        state.subscription?.let { sub ->
            name = sub.service_name
            price = sub.amount.toString()
            categoryName = sub.category_name
            categoryId = categoriesState.categories.firstOrNull { it.name == sub.category_name }?.id ?: ""
            dateDisplay = sub.next_payment_date
            billingCycleId = when (sub.billing_cycle) {
                "Daily" -> 1
                "Weekly" -> 2
                "Yearly" -> 4
                else -> 3
            }
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.resetSuccess()
            onSaveClick()
        }
    }

    if (showNewCategoryDialog) {
        NewCategoryDialog(
            onDismiss = { showNewCategoryDialog = false },
            onSave = { name, _, color ->
                categoriesViewModel.createCategory(name, null, colorToHex(color))
                showNewCategoryDialog = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        dateDisplay = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                    showDatePicker = false
                }) { Text("Aceptar", color = GreenPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = TextGray) }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = GreenPrimary,
                    todayDateBorderColor = GreenPrimary,
                    todayContentColor = GreenPrimary,
                    currentYearContentColor = GreenPrimary,
                    selectedYearContainerColor = GreenPrimary
                )
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Monto del pago", color = TextGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box {
                        if (price.isEmpty()) Text("0.00", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                        BasicTextField(
                            value = price,
                            onValueChange = { input ->
                                if (input.all { char -> char.isDigit() || char == '.' } && input.count { char -> char == '.' } <= 1) {
                                    price = input
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 48.sp),
                            modifier = Modifier.width(IntrinsicSize.Min)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            FormLabel("Nombre del servicio")
            FormInput(value = name, onValueChange = { name = it }, placeholder = "Ej. Netflix, Spotify...")

            Spacer(modifier = Modifier.height(24.dp))

            FormLabel("Selecciona Categoría")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(categoriesState.categories.size) { index ->
                    val category = categoriesState.categories[index]
                    val isSelected = categoryId == category.id

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                color = if (isSelected) GreenPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                categoryId = category.id
                                categoryName = category.name
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Color(android.graphics.Color.parseColor(category.color_hex ?: "#95A5A6")).copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Category,
                                    null,
                                    tint = Color(android.graphics.Color.parseColor(category.color_hex ?: "#95A5A6")),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                category.name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(20.dp)
                                    .background(GreenPrimary, CircleShape)
                                    .border(2.dp, White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
                // Botón Nueva Categoría
                item {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .border(1.dp, GreenPrimary, RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .clickable { showNewCategoryDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nueva", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Ciclo de cobro")
                    Box {
                        FormSelector(value = state.billingCycles.find { it.id == billingCycleId }?.name ?: "Monthly", icon = Icons.Default.Repeat, onClick = { showCycleDropdown = true })
                        CustomDropdown(expanded = showCycleDropdown, onDismiss = { showCycleDropdown = false }, items = state.billingCycles.map { it.name }) { selectedName: String ->
                            billingCycleId = state.billingCycles.find { it.name == selectedName }?.id ?: 3
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Próximo pago")
                    FormSelector(value = dateDisplay, icon = Icons.Default.CalendarToday, onClick = { showDatePicker = true })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FormLabel("Color de marca")
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                availableColors.forEach { color ->
                    ColorCircle(color = color, isSelected = color == selectedColor, onClick = { selectedColor = color })
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(40.dp))

            if (state.errorMessage.isNotEmpty()) {
                Text(state.errorMessage, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
            }

            Button(
                onClick = {
                    if (subscriptionId != null) {
                        viewModel.updateSubscription(
                            id = subscriptionId,
                            serviceName = name,
                            amount = price.toDoubleOrNull() ?: 0.0,
                            nextPaymentDate = dateDisplay,
                            billingCycleId = billingCycleId,
                            categoryId = categoryId,
                            cardId = null,
                            keepCurrentCard = true
                        )
                    } else {
                        viewModel.createSubscription(
                            serviceName = name,
                            amount = price.toDoubleOrNull() ?: 0.0,
                            nextPaymentDate = dateDisplay,
                            billingCycleId = billingCycleId,
                            categoryId = categoryId,
                            cardId = null,
                            cards = cardsState.cards
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.5f)
                ),
                enabled = name.isNotEmpty() && price.isNotEmpty() && dateDisplay != "Seleccionar fecha" && categoryId.isNotEmpty() && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp))
                } else {
                    Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FormLabel(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
fun FormInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontSize = 14.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FormSelector(value: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            value,
            color = if(value.startsWith("Seleccionar")) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun CustomDropdown(expanded: Boolean, onDismiss: () -> Unit, items: List<String>, onSelected: (String) -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface).width(200.dp).heightIn(max = 250.dp)
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item, color = MaterialTheme.colorScheme.onSurface) },
                onClick = { onSelected(item); onDismiss() },
                colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@Composable
fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(3.dp, White, CircleShape)
                    .padding(3.dp)
            )
            Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(20.dp))
        }
    }
}