package com.example.subscriptiontracker.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.ui.chat.ChatScreen
import com.example.subscriptiontracker.ui.home.HomeScreen
import com.example.subscriptiontracker.ui.premium.PremiumScreen
import com.example.subscriptiontracker.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Premium : Screen("premium")
    object Chat : Screen("chat")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    onThemeChanged: () -> Unit = {},
    onLanguageChanged: () -> Unit = {}
) {
    // Shared subscription state (HomeScreen'den erişilebilir)
    var sharedSubscriptionState by remember { mutableStateOf<com.example.subscriptiontracker.Subscription?>(null) }
    var nextId by remember { mutableIntStateOf(1) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
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
                onPurchaseComplete = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddSubscription = { subscription ->
                    // Subscription'ı HomeScreen'e eklemek için state güncelle
                    // Not: Bu basit bir çözüm, gerçek uygulamada ViewModel veya state management kullanılmalı
                    sharedSubscriptionState = subscription.copy(id = nextId)
                    nextId++
                },
                onThemeChanged = onThemeChanged,
                onLanguageChanged = onLanguageChanged
            )
        }
    }
}

