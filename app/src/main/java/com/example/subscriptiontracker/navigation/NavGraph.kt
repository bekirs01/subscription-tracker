package com.example.subscriptiontracker.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.PopularService
import com.example.subscriptiontracker.ui.chat.ChatScreen
import com.example.subscriptiontracker.ui.home.HomeScreen
import com.example.subscriptiontracker.ui.premium.PremiumScreen
import com.example.subscriptiontracker.ui.settings.SettingsScreen
import com.example.subscriptiontracker.ui.add.PopularServicesScreen
import com.example.subscriptiontracker.ui.add.SubscriptionSetupScreen
import com.example.subscriptiontracker.data.PopularServices

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Premium : Screen("premium")
    object Chat : Screen("chat")
    object PopularServices : Screen("popular_services")
    object SubscriptionSetup : Screen("subscription_setup/{serviceId}") {
        fun createRoute(serviceId: String) = "subscription_setup/$serviceId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    onThemeChanged: () -> Unit = {},
    onLanguageChanged: () -> Unit = {}
) {
    // Shared subscription state
    var subscriptions by remember { mutableStateOf<List<Subscription>>(emptyList()) }
    var nextId by remember { mutableIntStateOf(1) }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                subscriptions = subscriptions,
                onSubscriptionsChanged = { subscriptions = it },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onAddSubscription = { navController.navigate(Screen.PopularServices.route) }
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
                    subscriptions = subscriptions + subscription.copy(id = nextId)
                    nextId++
                },
                onThemeChanged = onThemeChanged,
                onLanguageChanged = onLanguageChanged
            )
        }
        
        composable(Screen.PopularServices.route) {
            PopularServicesScreen(
                onNavigateBack = { navController.popBackStack() },
                onServiceSelected = { service ->
                    navController.navigate(Screen.SubscriptionSetup.createRoute(service.id))
                }
            )
        }
        
        composable(
            route = Screen.SubscriptionSetup.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            val service = PopularServices.top100.find { it.id == serviceId }
                ?: PopularServices.top100.first()
            
            SubscriptionSetupScreen(
                service = service,
                onNavigateBack = { navController.popBackStack() },
                onSave = { subscription ->
                    subscriptions = subscriptions + subscription.copy(id = nextId)
                    nextId++
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}

