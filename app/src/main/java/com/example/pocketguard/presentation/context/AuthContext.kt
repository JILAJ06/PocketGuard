package com.example.pocketguard.presentation.context

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.example.pocketguard.data.models.User

data class AuthContextState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val accessToken: String? = null,
    val isLoading: Boolean = false
)

val LocalAuthContext = compositionLocalOf<AuthContextState> {
    error("AuthContext not provided")
}

@Composable
fun ProvideAuthContext(
    authState: AuthContextState,
    content: @Composable () -> Unit
) {
    val value = authState
    androidx.compose.runtime.CompositionLocalProvider(
        LocalAuthContext provides value,
        content = content
    )
}

