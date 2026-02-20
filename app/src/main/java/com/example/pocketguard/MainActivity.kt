package com.example.pocketguard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 1. DEFINICIÓN DE COLORES (Basado en PocketGuard.pdf) ---
val GreenPrimary = Color(0xFF2ECC71) // Verde vibrante similar al diseño
val BackgroundLight = Color(0xFFF8F9FA) // Crema/Gris muy suave
val TextDark = Color(0xFF1E1E1E)
val TextGray = Color(0xFF757575)
val InputBackground = Color(0xFFF0F2F5)

// --- 2. COMPONENTES REUTILIZABLES ---

@Composable
fun PocketGuardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = GreenPrimary) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Toggle Password",
                        tint = TextGray
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = InputBackground
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SocialButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
    ) {
        // Aquí iría el logo de Google (Drawable)
        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Unspecified)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextDark)
    }
}

// --- 3. PANTALLA DE LOGIN ---

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterLinkClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo y Header
        Icon(
            imageVector = Icons.Default.AttachMoney, // Icono placeholder del logo
            contentDescription = "Logo",
            tint = GreenPrimary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bienvenido de nuevo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Tu saldo real disponible te espera.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Formulario
        PocketGuardTextField(email, { email = it }, "Correo Electrónico", Icons.Default.Email, keyboardType = KeyboardType.Email)
        Spacer(modifier = Modifier.height(16.dp))
        PocketGuardTextField(password, { password = it }, "Contraseña", Icons.Default.Lock, isPassword = true)

        // Olvidé contraseña
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { /* Navegar a recuperar */ }) {
                Text("¿Olvidaste tu contraseña?", color = TextGray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Principal
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Separador
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("  O continúa con  ", color = TextGray, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Google Button
        SocialButton(text = "Iniciar con Google", onClick = onGoogleClick)

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tienes cuenta?", color = TextGray)
            TextButton(onClick = onRegisterLinkClick) {
                Text("Regístrate aquí", color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 4. PANTALLA DE REGISTRO ---

@Composable
fun SignUpScreen(
    onRegisterClick: () -> Unit,
    onLoginLinkClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crea tu cuenta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Text(
            text = "Deja de perder dinero en gastos hormiga.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        PocketGuardTextField(name, { name = it }, "Nombre Completo", Icons.Default.Person)
        Spacer(modifier = Modifier.height(16.dp))
        PocketGuardTextField(email, { email = it }, "Correo Electrónico", Icons.Default.Email, keyboardType = KeyboardType.Email)
        Spacer(modifier = Modifier.height(16.dp))
        PocketGuardTextField(password, { password = it }, "Contraseña", Icons.Default.Lock, isPassword = true)

        Spacer(modifier = Modifier.height(16.dp))

        // Checkbox Legal (LFPDPPP)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(checkedColor = GreenPrimary)
            )
            Text(
                text = "Acepto el Aviso de Privacidad y Términos.",
                fontSize = 12.sp,
                color = TextGray,
                modifier = Modifier.clickable { /* Abrir PDF Legal */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegisterClick,
            enabled = isChecked, // Bloqueado hasta aceptar privacidad
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                disabledContainerColor = Color.LightGray
            )
        ) {
            Text("Comenzar a Ahorrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        SocialButton(text = "Registrarse con Google", onClick = onGoogleClick)

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?", color = TextGray)
            TextButton(onClick = onLoginLinkClick) {
                Text("Inicia Sesión", color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- PREVIEW PARA VER EL FLUJO ---
@Preview
@Composable
fun AuthFlowPreview() {
    // Estado simple para simular navegación en el Preview
    var isLogin by remember { mutableStateOf(true) }

    if (isLogin) {
        LoginScreen(
            onLoginClick = {},
            onRegisterLinkClick = { isLogin = false },
            onGoogleClick = {}
        )
    } else {
        SignUpScreen(
            onRegisterClick = {},
            onLoginLinkClick = { isLogin = true },
            onGoogleClick = {}
        )
    }
}