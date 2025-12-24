package com.example.subscriptiontracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.SubscriptionItem
import com.example.subscriptiontracker.AddSubscriptionDialog
import com.example.subscriptiontracker.Period
import com.example.subscriptiontracker.utils.CurrencyManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class HomeTab {
    SUBSCRIPTIONS, BUDGET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    subscriptions: List<Subscription>,
    onSubscriptionsChanged: (List<Subscription>) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onAddSubscription: () -> Unit,
    onEditSubscription: (Int) -> Unit,
    onDeleteSubscription: (Int) -> Unit
) {
    val context = LocalContext.current
    val currencyFlow = remember { CurrencyManager.getCurrencyFlow(context) }
    val currentCurrency by currencyFlow.collectAsState(initial = CurrencyManager.defaultCurrency)
    val currency = CurrencyManager.getCurrency(currentCurrency)
    
    var selectedTab by remember { mutableStateOf(HomeTab.SUBSCRIPTIONS) }
    
    // Calculate total monthly cost
    val totalMonthlyCost = remember(subscriptions) {
        subscriptions.sumOf { subscription ->
            val price = subscription.price.toDoubleOrNull() ?: 0.0
            if (subscription.period == Period.YEARLY) {
                price / 12.0
            } else {
                price
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    OutlinedButton(
                        onClick = { /* Premium action */ }
                    ) {
                        Text(stringResource(R.string.premium))
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = true,
                    onClick = { }
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
                    selected = false,
                    onClick = onAddSubscription
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = false,
                    onClick = onNavigateToSettings
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToChat,
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Text(
                    text = "🤖",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Total Monthly Cost Card
            if (subscriptions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.total_monthly_cost),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${currency?.symbol ?: "₺"}${String.format("%.2f", totalMonthlyCost)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Segmented Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SegmentedButton(
                    selected = selectedTab == HomeTab.SUBSCRIPTIONS,
                    onClick = { selectedTab = HomeTab.SUBSCRIPTIONS },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.subscriptions_tab))
                }
                SegmentedButton(
                    selected = selectedTab == HomeTab.BUDGET,
                    onClick = { selectedTab = HomeTab.BUDGET },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.budget_tracking_tab))
                }
            }
            
            // Content
            when (selectedTab) {
                HomeTab.SUBSCRIPTIONS -> {
                    if (subscriptions.isEmpty()) {
                        EmptyState(
                            onAddClick = onAddSubscription
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(subscriptions) { subscription ->
                                SubscriptionItem(
                                    subscription = subscription,
                                    onClick = { onEditSubscription(subscription.id) },
                                    onDelete = { onDeleteSubscription(subscription.id) }
                                )
                            }
                            
                            // Upcoming Payments Section
                            if (subscriptions.isNotEmpty()) {
                                item {
                                    UpcomingPaymentsSection(subscriptions = subscriptions)
                                }
                            }
                        }
                    }
                }
                HomeTab.BUDGET -> {
                    // Budget tracking content - placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.budget_tracking_tab),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Illustration placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = stringResource(R.string.no_subscriptions_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = stringResource(R.string.no_subscriptions_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Down arrow pointing to Add button
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides contentColor
            ) {
                content()
            }
        }
    }
}

@Composable
fun UpcomingPaymentsSection(subscriptions: List<Subscription>) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    // Calculate upcoming payments for each subscription
    // Recalculate when subscriptions change
    val upcomingPayments = remember(subscriptions) {
        val currentDate = LocalDate.now()
        subscriptions.mapNotNull { subscription ->
            try {
                val startDate = LocalDate.parse(subscription.renewalDate, formatter)
                
                // Calculate next payment date
                val nextPaymentDate = calculateNextPaymentDate(startDate, subscription.period, currentDate)
                
                // Calculate days until payment
                val daysUntil = ChronoUnit.DAYS.between(currentDate, nextPaymentDate).toInt()
                
                // Only include payments within 30 days
                if (daysUntil >= 0 && daysUntil <= 30) {
                    Pair(subscription, daysUntil)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.second } // Sort by days until payment (soonest first)
    }
    
    if (upcomingPayments.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section Title
        Text(
            text = stringResource(R.string.upcoming_payments),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Upcoming Payments List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            upcomingPayments.forEach { (subscription, daysUntil) ->
                UpcomingPaymentRow(
                    subscriptionName = subscription.name,
                    daysUntil = daysUntil
                )
            }
        }
    }
}

@Composable
fun UpcomingPaymentRow(
    subscriptionName: String,
    daysUntil: Int
) {
    val paymentText = when {
        daysUntil == 0 -> stringResource(R.string.payment_today)
        daysUntil == 1 -> stringResource(R.string.payment_tomorrow)
        else -> stringResource(R.string.payment_in_days, daysUntil)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = subscriptionName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = paymentText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (daysUntil <= 3) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

/**
 * Calculate the next payment date based on start date, period, and current date.
 * If start date is in the past, calculate the next occurrence.
 * If start date is in the future, use that date.
 */
fun calculateNextPaymentDate(
    startDate: LocalDate,
    period: Period,
    currentDate: LocalDate
): LocalDate {
    // If start date is in the future, return it
    if (startDate.isAfter(currentDate)) {
        return startDate
    }
    
    // If start date is today or in the past, calculate next occurrence
    var nextDate = startDate
    
    when (period) {
        Period.MONTHLY -> {
            // Add months until we get a date in the future
            while (!nextDate.isAfter(currentDate)) {
                nextDate = nextDate.plusMonths(1)
            }
        }
        Period.YEARLY -> {
            // Add years until we get a date in the future
            while (!nextDate.isAfter(currentDate)) {
                nextDate = nextDate.plusYears(1)
            }
        }
    }
    
    return nextDate
}

