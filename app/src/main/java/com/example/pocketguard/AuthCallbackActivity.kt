package com.example.pocketguard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pocketguard.data.repository.AuthRepository
import com.example.pocketguard.data.store.TokenStore
import com.example.pocketguard.data.models.User
import com.example.pocketguard.network.GoogleAuthHelper
import com.example.pocketguard.ui.theme.PocketGuardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthCallbackActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostrar UI de carga mientras procesamos el callback
        setContent {
            PocketGuardTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Completando autenticación...")
                    }
                }
            }
        }

        // Procesar el deep link
        val googleAuthHelper = GoogleAuthHelper(this)
        val token = googleAuthHelper.handleDeepLink(intent)

        CoroutineScope(Dispatchers.Main).launch {
            if (token != null) {
                handleSuccessfulAuth(token)
            } else {
                handleAuthError("Token no recibido")
            }
            // Cerrar esta activity después de procesar
            finish()
        }
    }

    private suspend fun handleSuccessfulAuth(accessToken: String) {
        try {
            // Guardar token
            val tokenStore = TokenStore(this)
            tokenStore.saveAccessToken(accessToken)

            // Obtener datos del usuario actual
            val authRepository = AuthRepository(tokenStore)
            val userResult = authRepository.getCurrentUser()

            userResult.onSuccess { user ->
                // Guardar datos del usuario
                tokenStore.saveUser(user.id, user.email, user.fullName, user.authProvider)

                // Navegar a MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }.onFailure { error ->
                handleAuthError("Error al obtener datos del usuario: ${error.message}")
            }
        } catch (e: Exception) {
            handleAuthError("Error: ${e.message}")
        }
    }

    private fun handleAuthError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Redirigir a LoginScreen
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("show_login", true)
        startActivity(intent)
    }
}

