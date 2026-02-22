package com.example.pocketguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketguard.components.PocketGuardTextField
import com.example.pocketguard.components.SocialButton
import com.example.pocketguard.presentation.viewmodels.AuthViewModel

@Composable
fun SignUpScreen(
    onRegisterClick: () -> Unit,
    onLoginLinkClick: () -> Unit,
    onGoogleClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()

    // Observar éxito de autenticación
    LaunchedEffect(authSuccess) {
        if (authSuccess != null) {
            onRegisterClick()
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
        Text(
            text = "Crea tu cuenta",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Deja de perder dinero en gastos hormiga.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Formulario ---
        PocketGuardTextField(
            name,
            { name = it },
            "Nombre Completo",
            Icons.Default.Person,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
        PocketGuardTextField(
            email,
            { email = it },
            "Correo Electrónico",
            Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
        PocketGuardTextField(
            password,
            { password = it },
            "Contraseña",
            Icons.Default.Lock,
            isPassword = true,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Checkbox Legal ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                enabled = !isLoading
            )
            Text(
                text = "Acepto el Aviso de Privacidad y Términos.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable(enabled = !isLoading) { /* Abrir PDF Legal */ }
            )
        }

        // --- Mensaje de error ---
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Botones ---
        Button(
            onClick = {
                if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && isChecked) {
                    viewModel.register(name, email, password)
                }
            },
            enabled = isChecked && name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.LightGray
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    "Comenzar a Ahorrar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SocialButton(text = "Registrarse con Google", onClick = onGoogleClick, enabled = !isLoading)

        Spacer(modifier = Modifier.height(24.dp))

        // --- Footer ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?", color = MaterialTheme.colorScheme.secondary)
            TextButton(onClick = onLoginLinkClick, enabled = !isLoading) {
                Text("Inicia Sesión", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}