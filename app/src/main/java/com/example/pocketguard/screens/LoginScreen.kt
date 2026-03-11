package com.example.pocketguard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketguard.components.PocketGuardTextField
import com.example.pocketguard.components.SocialButton
import com.example.pocketguard.presentation.viewmodel.LoginViewModel
import com.example.pocketguard.data.repository.AuthRepository
import com.example.pocketguard.ui.theme.PocketGuardTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterLinkClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onLoginSuccess()
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

        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp
                )
            }
        }

        PocketGuardTextField(
            formState.email,
            { viewModel.onEmailChanged(it) },
            "Correo Electrónico",
            Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            isError = formState.emailError.name != "NONE"
        )
        if (formState.emailError.name != "NONE") {
            Text(
                text = viewModel.getEmailErrorMessage(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PocketGuardTextField(
            formState.password,
            { viewModel.onPasswordChanged(it) },
            "Contraseña",
            Icons.Default.Lock,
            isPassword = true,
            isError = formState.passwordError.name != "NONE"
        )
        if (formState.passwordError.name != "NONE") {
            Text(
                text = viewModel.getPasswordErrorMessage(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onForgotPasswordClick) {
                Text("¿Olvidaste tu contraseña?", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !formState.isLoading
        ) {
            if (formState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
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

        SocialButton(text = "Iniciar con Google", onClick = onGoogleClick, enabled = !formState.isLoading)

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tienes cuenta?", color = MaterialTheme.colorScheme.secondary)
            TextButton(onClick = onRegisterLinkClick) {
                Text("Regístrate aquí", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

