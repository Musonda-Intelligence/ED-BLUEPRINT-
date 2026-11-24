package com.edblueprint.app.ui.auth

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"

/** The two screens a signed-out person can reach. */
@Composable
fun AuthNavHost(viewModelFactory: ViewModelProvider.Factory) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_LOGIN) {
        composable(ROUTE_LOGIN) {
            val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
            LoginScreen(
                viewModel = viewModel,
                onCreateAccount = { navController.navigate(ROUTE_REGISTER) }
            )
        }
        composable(ROUTE_REGISTER) {
            val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
            RegisterScreen(
                viewModel = viewModel,
                onBackToLogin = { navController.popBackStack() }
            )
        }
    }
}
