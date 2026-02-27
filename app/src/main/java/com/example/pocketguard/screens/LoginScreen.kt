package com.example.pocketguard.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketguard.components.PocketGuardTextField
import com.example.pocketguard.components.SocialButton
import com.example.pocketguard.data.repository.AuthRepository
import com.example.pocketguard.data.store.TokenStore
import com.example.pocketguard.network.GoogleAuthHelper
import com.example.pocketguard.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterLinkClick: () -> Unit,
    onGoogleClick: () -> Unit,
    viewModel: AuthViewModel? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel?.authUiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Mostrar error si existe
    if (authState?.errorMessage != null) {
        LaunchedEffect(authState?.errorMessage) {
            Toast.makeText(context, authState?.errorMessage, Toast.LENGTH_LONG).show()
            viewModel?.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Header ---
        Icon(
            imageVector = Icons.Default.AttachMoney,
            contentDescription = "Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bienvenido de nuevo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tu saldo real disponible te espera.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Formulario ---
        PocketGuardTextField(email, { email = it }, "Correo Electrónico", Icons.Default.Email, keyboardType = KeyboardType.Email)
        Spacer(modifier = Modifier.height(16.dp))
        PocketGuardTextField(password, { password = it }, "Contraseña", Icons.Default.Lock, isPassword = true)

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { /* Lógica recuperar */ }) {
                Text("¿Olvidaste tu contraseña?", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Botones ---
        Button(
            onClick = {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    viewModel?.login(email, password)
                } else {
                    onLoginClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = authState?.isLoading != true
        ) {
            if (authState?.isLoading == true) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("  O continúa con  ", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        SocialButton(
            text = "Iniciar con Google",
            onClick = {
                val activity = context as? Activity
                if (activity != null) {
                    GoogleAuthHelper(activity).startGoogleSignIn()
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Footer ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tienes cuenta?", color = MaterialTheme.colorScheme.secondary)
            TextButton(onClick = onRegisterLinkClick) {
                Text("Regístrate aquí", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

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
        label = { Text(label, color = MaterialTheme.colorScheme.secondary) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Toggle Password",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.Companion.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant // Color InputBackground definido en theme
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
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Unspecified)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = MaterialTheme.colorScheme.onBackground)
    }
}