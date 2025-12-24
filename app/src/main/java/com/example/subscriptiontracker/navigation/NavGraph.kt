package com.example.subscriptiontracker.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.ui.chat.ChatScreen
import com.example.subscriptiontracker.ui.home.HomeScreen
import com.example.subscriptiontracker.ui.premium.PremiumScreen
import com.example.subscriptiontracker.ui.settings.SettingsScreen
import com.example.subscriptiontracker.ui.add.PopularServicesScreen
import com.example.subscriptiontracker.ui.add.SubscriptionDetailsScreen
import com.example.subscriptiontracker.ui.add.ServiceItem

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Premium : Screen("premium")
    object Chat : Screen("chat")
    object PopularServices : Screen("popular_services")
    object SubscriptionDetails : Screen("subscription_details")
    object EditSubscription : Screen("edit_subscription/{subscriptionId}") {
        fun createRoute(subscriptionId: Int) = "edit_subscription/$subscriptionId"
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
    
    // Selected service state for navigation
    var selectedService by remember { mutableStateOf<ServiceItem?>(null) }
    
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
                onAddSubscription = { navController.navigate(Screen.PopularServices.route) },
                onEditSubscription = { subscriptionId ->
                    navController.navigate(Screen.EditSubscription.createRoute(subscriptionId))
                },
                onDeleteSubscription = { subscriptionId ->
                    subscriptions = subscriptions.filter { it.id != subscriptionId }
                }
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
                    // Set selected service and navigate
                    selectedService = service
                    navController.navigate(Screen.SubscriptionDetails.route)
                },
                onCustomSelected = {
                    // Clear selected service for custom
                    selectedService = null
                    navController.navigate(Screen.SubscriptionDetails.route)
                }
            )
        }
        
        composable(Screen.SubscriptionDetails.route) {
            SubscriptionDetailsScreen(
                predefinedService = selectedService,
                existingSubscription = null,
                onNavigateBack = { 
                    selectedService = null
                    navController.popBackStack() 
                },
                onSave = { subscription ->
                    subscriptions = subscriptions + subscription.copy(id = nextId)
                    nextId++
                    selectedService = null
                    // Navigate directly to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
        
        composable(
            route = Screen.EditSubscription.route,
            arguments = listOf(navArgument("subscriptionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getInt("subscriptionId") ?: 0
            val subscriptionToEdit = subscriptions.find { it.id == subscriptionId }
            
            SubscriptionDetailsScreen(
                predefinedService = null,
                existingSubscription = subscriptionToEdit,
                onNavigateBack = { navController.popBackStack() },
                onSave = { updatedSubscription ->
                    subscriptions = subscriptions.map { 
                        if (it.id == subscriptionId) {
                            updatedSubscription.copy(id = subscriptionId)
                        } else {
                            it
                        }
                    }
                    // Navigate back to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }
    }
}

