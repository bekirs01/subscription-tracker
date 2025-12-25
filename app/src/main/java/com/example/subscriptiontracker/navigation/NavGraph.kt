package com.example.subscriptiontracker.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.ui.chat.ChatScreen
import com.example.subscriptiontracker.ui.home.HomeScreen
import com.example.subscriptiontracker.ui.home.BudgetStatsScreen
import com.example.subscriptiontracker.ui.premium.PremiumScreen
import com.example.subscriptiontracker.ui.premium.PremiumScreenMode
import com.example.subscriptiontracker.ui.settings.SettingsScreen
import com.example.subscriptiontracker.data.fx.ExchangeRateRepository
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.ui.add.PopularServicesScreen
import com.example.subscriptiontracker.ui.add.SubscriptionDetailsScreen
import com.example.subscriptiontracker.ui.add.ServiceItem
import com.example.subscriptiontracker.utils.SubscriptionReminderManager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
    data object Premium : Screen("premium") // Settings'ten açılan Premium
    data object PremiumPromo : Screen("premium_promo") // İlk açılışta açılan Premium
    data object Chat : Screen("chat")
    data object PopularServices : Screen("popular_services")
    data object SubscriptionDetails : Screen("subscription_details")
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
    
    // Get current route for bottom bar selection
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    
    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = currentRoute,
                onHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = false } } },
                onStats = { navController.navigate(Screen.Stats.route) },
                onAdd = { navController.navigate(Screen.PopularServices.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
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
                },
                mode = PremiumScreenMode.SETTINGS
            )
        }
        
        composable(Screen.PremiumPromo.route) {
            PremiumScreen(
                onNavigateBack = { navController.popBackStack() },
                onPurchaseComplete = {
                    navController.popBackStack()
                },
                mode = PremiumScreenMode.PROMO
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
}

@Composable
fun AppBottomBar(
    currentRoute: String,
    onHome: () -> Unit,
    onStats: () -> Unit,
    onAdd: () -> Unit,
    onSettings: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Dashboard") },
            selected = currentRoute == Screen.Home.route,
            onClick = onHome
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null) },
            label = { Text("Stats") },
            selected = currentRoute == Screen.Stats.route,
            onClick = onStats
        )
        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.nav_add),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            selected = false, // Add button is not a selectable route
            onClick = onAdd
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = currentRoute == Screen.Settings.route,
            onClick = onSettings
        )
    }
}

