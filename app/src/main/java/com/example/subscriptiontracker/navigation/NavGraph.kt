package com.example.subscriptiontracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.subscriptiontracker.ui.home.HomeScreen
import com.example.subscriptiontracker.ui.premium.PremiumScreen
import com.example.subscriptiontracker.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Premium : Screen("premium")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    onThemeChanged: () -> Unit = {},
    onLanguageChanged: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onAddSubscription = { /* Handled in HomeScreen */ }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onThemeChanged = onThemeChanged,
                onLanguageChanged = onLanguageChanged,
                onNavigateToPremium = { navController.navigate(Screen.Premium.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Premium.route) {
            PremiumScreen(
                onNavigateBack = { navController.popBackStack() },
                onPurchase = { packageItem ->
                    // TODO: Premium purchase logic
                }
            )
        }
    }
}

