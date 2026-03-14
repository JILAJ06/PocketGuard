package com.example.pocketguard.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pocketguard.components.PocketGuardTextField
import com.example.pocketguard.components.SocialButton
import com.example.pocketguard.presentation.viewmodel.RegisterViewModel

@Composable
fun SignUpScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onLoginLinkClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    val termsUrl = "https://pocketguard-pi.vercel.app/terminos"
    val uriHandler = LocalUriHandler.current

    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onRegisterSuccess()
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
            formState.fullName,
            { viewModel.onFullNameChanged(it) },
            "Nombre Completo",
            Icons.Default.Person,
            isError = formState.fullNameError.name != "NONE",
            enabled = !formState.isLoading
        )
        if (formState.fullNameError.name != "NONE") {
            Text(
                text = viewModel.getFullNameErrorMessage(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PocketGuardTextField(
            formState.email,
            { viewModel.onEmailChanged(it) },
            "Correo Electrónico",
            Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            isError = formState.emailError.name != "NONE",
            enabled = !formState.isLoading
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
            isError = formState.passwordError.name != "NONE",
            enabled = !formState.isLoading
        )
        if (formState.passwordError.name != "NONE") {
            Text(
                text = viewModel.getPasswordErrorMessage(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PocketGuardTextField(
            formState.confirmPassword,
            { viewModel.onConfirmPasswordChanged(it) },
            "Confirmar Contraseña",
            Icons.Default.Lock,
            isPassword = true,
            isError = formState.confirmPasswordError.name != "NONE",
            enabled = !formState.isLoading
        )
        if (formState.confirmPasswordError.name != "NONE") {
            Text(
                text = viewModel.getConfirmPasswordErrorMessage(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = formState.acceptedTerms,
                onCheckedChange = { viewModel.onAcceptedTermsChanged(it) },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                enabled = !formState.isLoading
            )
            val termsText = buildAnnotatedString {
                append("Acepto el Aviso de Privacidad y ")
                withLink(
                    link = LinkAnnotation.Url(
                        url = termsUrl,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.SemiBold
                            )
                        ),
                        linkInteractionListener = { uriHandler.openUri(termsUrl) }
                    )
                ) {
                    append("Términos")
                }
                append(".")
            }

            Text(
                text = termsText,
                style = LocalTextStyle.current.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.register() },
            enabled = formState.isValid && !formState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (formState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Comenzar a Ahorrar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SocialButton(text = "Registrarse con Google", onClick = onGoogleClick, enabled = !formState.isLoading)

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?", color = MaterialTheme.colorScheme.secondary)
            TextButton(onClick = onLoginLinkClick) {
                Text("Inicia Sesión", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
