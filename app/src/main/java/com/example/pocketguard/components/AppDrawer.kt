package com.example.pocketguard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.ui.theme.*

@Composable
fun AppDrawer(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = White,
        drawerContentColor = TextDark,
        modifier = Modifier.width(320.dp) // Un poco más ancho para elegancia
    ) {
        // 1. CABECERA CURVA (Estilo PocketGuard)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = GreenPrimary,
                    shape = RoundedCornerShape(bottomEnd = 32.dp) // Curva característica solo en la esquina
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 32.dp)
            ) {
                // Fila Avatar + Botón Cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar con Borde
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(White)
                            .border(2.dp, White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "UD",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }

                    // Icono cerrar (discreto)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = White.copy(alpha = 0.7f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Textos
                Text(
                    text = "Alexander Jiménez",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Text(
                    text = "Gestor de Gastos",
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.8f)
                )
            }
        }

        // 2. CUERPO DEL MENÚ
        Column(
            modifier = Modifier
                .padding(24.dp)
                .weight(1f)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {

            DrawerSectionTitle("NAVEGACIÓN")

            DrawerItem(Icons.Default.Home, "Inicio", currentRoute == "inicio") { onNavigate("inicio"); onClose() }
            DrawerItem(Icons.Default.CreditCard, "Suscripciones", currentRoute == "suscripciones") { onNavigate("suscripciones"); onClose() }
            DrawerItem(Icons.Default.AttachMoney, "Gastos", currentRoute == "gastos") { onNavigate("gastos"); onClose() }
            DrawerItem(Icons.Default.Notifications, "Alertas", currentRoute == "alertas") { onNavigate("alertas"); onClose() }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = InputBackground, thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            DrawerSectionTitle("CUENTA")

            DrawerItem(Icons.Outlined.Person, "Mi Perfil", false) { onClose() }
            DrawerItem(Icons.Outlined.Settings, "Configuración", currentRoute == "configuracion") {
                onNavigate("configuracion")
                onClose()
            }
            DrawerItem(Icons.AutoMirrored.Filled.Help, "Ayuda y Soporte", false) { onClose() }
        }

        // 3. FOOTER (Botón Salir)
        Box(modifier = Modifier.padding(24.dp)) {
            Button(
                onClick = { /* Logout */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.1f),
                    contentColor = ErrorRed
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun DrawerSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextGray.copy(alpha = 0.7f),
        modifier = Modifier.padding(bottom = 12.dp, start = 12.dp)
    )
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) GreenPrimary else Color.Transparent
    val contentColor = if (isSelected) White else TextDark
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp), // Forma de píldora moderna
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(56.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) White else TextGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = fontWeight,
                color = if (isSelected) White else TextDark
            )
        }
    }
}