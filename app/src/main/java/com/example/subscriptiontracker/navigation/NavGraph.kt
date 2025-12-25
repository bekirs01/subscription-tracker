package com.example.subscriptiontracker.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.ui.chat.ChatScreen
import com.example.subscriptiontracker.ui.home.HomeScreen
import com.example.subscriptiontracker.ui.home.BudgetStatsScreen
import com.example.subscriptiontracker.ui.premium.PremiumScreen
import com.example.subscriptiontracker.ui.settings.SettingsScreen
import com.example.subscriptiontracker.data.fx.ExchangeRateRepository
import com.example.subscriptiontracker.data.fx.FxState
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.ui.add.PopularServicesScreen
import com.example.subscriptiontracker.ui.add.SubscriptionDetailsScreen
import com.example.subscriptiontracker.ui.add.ServiceItem
import com.example.subscriptiontracker.utils.SubscriptionReminderManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Stats : Screen("stats")
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
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
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onAddSubscription = { navController.navigate(Screen.PopularServices.route) },
                onEditSubscription = { subscriptionId ->
                    navController.navigate(Screen.EditSubscription.createRoute(subscriptionId))
                },
                onDeleteSubscription = { subscriptionId ->
                    // Cancel reminders before deleting
                    scope.launch {
                        SubscriptionReminderManager.cancelReminders(context, subscriptionId)
                    }
                    subscriptions = subscriptions.filter { it.id != subscriptionId }
                }
            )
        }
        
        composable(Screen.Stats.route) {
            val currencyFlow = remember { CurrencyManager.getCurrencyFlow(context) }
            val currentCurrency by currencyFlow.collectAsState(initial = CurrencyManager.defaultCurrency)
            val fxStateFlow = remember(currentCurrency) {
                ExchangeRateRepository.ratesFlow(context, currentCurrency)
            }
            val fxState by fxStateFlow.collectAsState()
            
            BudgetStatsScreen(
                subscriptions = subscriptions,
                baseCurrency = currentCurrency,
                fxState = fxState,
                context = context
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
                    val newSubscription = subscription.copy(id = nextId)
                    subscriptions = subscriptions + newSubscription
                    nextId++
                    // Schedule reminders for subscription added from chat
                    scope.launch {
                        SubscriptionReminderManager.scheduleReminders(context, newSubscription)
                    }
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
                    val newSubscription = subscription.copy(id = nextId)
                    subscriptions = subscriptions + newSubscription
                    nextId++
                    selectedService = null
                    // Schedule reminders for new subscription
                    scope.launch {
                        SubscriptionReminderManager.scheduleReminders(context, newSubscription)
                    }
                    // Navigate directly to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
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
                    val finalSubscription = updatedSubscription.copy(id = subscriptionId)
                    subscriptions = subscriptions.map { 
                        if (it.id == subscriptionId) {
                            finalSubscription
                        } else {
                            it
                        }
                    }
                    // Update reminders for edited subscription
                    scope.launch {
                        SubscriptionReminderManager.updateReminders(context, finalSubscription)
                    }
                    // Navigate back to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Premium.route)
                }
            )
        }
    }
}

