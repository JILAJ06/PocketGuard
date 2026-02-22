package com.example.pocketguard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pocketguard.LoginScreen
import com.example.pocketguard.SignUpScreen
import com.example.pocketguard.presentation.screens.HomeScreen
import com.example.pocketguard.presentation.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onRegisterLinkClick = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.Login.route) { inclusive = false }
                    }
                },
                onGoogleClick = {
                    // TODO: Implementar Google Sign In
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onRegisterClick = { name, email, password ->
                    authViewModel.register(name, email, password)
                },
                onLoginLinkClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onGoogleClick = {
                    // TODO: Implementar Google Sign In
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onLogoutClick = {
                    authViewModel.logout()
                }
            )
        }
    }
}

