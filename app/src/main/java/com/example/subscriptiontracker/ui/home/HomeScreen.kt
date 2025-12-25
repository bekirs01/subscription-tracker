package com.example.subscriptiontracker.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.SubscriptionItem
import com.example.subscriptiontracker.AddSubscriptionDialog
import com.example.subscriptiontracker.Period
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.utils.ExchangeRateService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    val baseCurrency = CurrencyManager.getCurrency(currentCurrency)
    
    var selectedTab by remember { mutableStateOf(HomeTab.SUBSCRIPTIONS) }
    
    // Exchange rates state
    var exchangeRates by remember { mutableStateOf<Map<String, Double>?>(null) }
    var ratesLoading by remember { mutableStateOf(false) }
    var ratesError by remember { mutableStateOf(false) }
    
    // Kurları yükle
    LaunchedEffect(currentCurrency) {
        ratesLoading = true
        ratesError = false
        exchangeRates = ExchangeRateService.getExchangeRates(currentCurrency)
        ratesLoading = false
        if (exchangeRates == null) {
            ratesError = true
        }
    }
    
    // Calculate total monthly cost - TÜM abonelikleri base currency'ye dönüştürerek topla
    val (totalMonthlyCost, hasMultipleCurrencies, conversionWarning) = remember(subscriptions, currentCurrency, exchangeRates) {
        val currencies = subscriptions.map { it.currency }.distinct()
        val hasMultiple = currencies.size > 1
        
        if (hasMultiple && exchangeRates != null) {
            // Farklı para birimleri varsa ve kurlar yüklendiyse, tümünü base currency'ye dönüştür
            var total = 0.0
            var allConverted = true
            
            subscriptions.forEach { subscription ->
                val price = subscription.price.toDoubleOrNull() ?: 0.0
                val monthlyPrice = if (subscription.period == Period.YEARLY) {
                    price / 12.0
                } else {
                    price
                }
                
                if (subscription.currency == currentCurrency) {
                    // Zaten base currency, direkt ekle
                    total += monthlyPrice
                } else {
                    // Dönüştür
                    val converted = ExchangeRateService.convertCurrency(
                        monthlyPrice,
                        subscription.currency,
                        currentCurrency,
                        exchangeRates
                    )
                    if (converted != null) {
                        total += converted
                    } else {
                        allConverted = false
                    }
                }
            }
            
            Pair(total, true, allConverted)
        } else if (hasMultiple && exchangeRates == null) {
            // Farklı para birimleri var ama kurlar yüklenemedi
            Pair(0.0, true, false)
        } else {
            // Tek para birimi varsa, tüm abonelikleri topla
            val total = subscriptions.sumOf { subscription ->
                val price = subscription.price.toDoubleOrNull() ?: 0.0
                if (subscription.period == Period.YEARLY) {
                    price / 12.0
                } else {
                    price
                }
            }
            Pair(total, false, true)
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
                        if (hasMultipleCurrencies && exchangeRates == null) {
                            // Farklı para birimleri var ama kurlar yüklenemedi
                            Text(
                                text = "Toplam hesaplanamıyor: Döviz kurları alınamadı",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        } else if (hasMultipleCurrencies && !conversionWarning) {
                            // Farklı para birimleri var ama bazıları dönüştürülemedi
                            Text(
                                text = "Toplam hesaplanamıyor: Bazı para birimleri için kur bulunamadı",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            // Toplam göster
                            Text(
                                text = "${baseCurrency?.symbol ?: "₺"}${String.format("%.2f", totalMonthlyCost)}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Uyarı mesajı (farklı para birimleri varsa)
                if (hasMultipleCurrencies && conversionWarning && exchangeRates != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Toplam tutar güncel döviz kuru ile hesaplanmıştır. Kur farkları değişiklik gösterebilir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
    // Helper function to parse date string to Calendar
    fun parseDateString(dateString: String): Calendar? {
        return try {
            if (dateString.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                val parts = dateString.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1 // Calendar months are 0-based
                val day = parts[2].toInt()
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Helper function to calculate days between two calendars
    fun daysBetween(cal1: Calendar, cal2: Calendar): Int {
        val diff = cal2.timeInMillis - cal1.timeInMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
    
    // Calculate upcoming payments for each subscription
    // Recalculate when subscriptions change
    val upcomingPayments = remember(subscriptions) {
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        subscriptions.mapNotNull { subscription ->
            try {
                val startDate = parseDateString(subscription.renewalDate) ?: return@mapNotNull null
                
                // Calculate next payment date
                val nextPaymentDate = calculateNextPaymentDate(startDate, subscription.period, currentDate)
                
                // Calculate days until payment
                val daysUntil = daysBetween(currentDate, nextPaymentDate)
                
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            upcomingPayments.forEach { (subscription, daysUntil) ->
                UpcomingPaymentRow(
                    subscription = subscription,
                    daysUntil = daysUntil
                )
            }
        }
    }
}

@Composable
fun UpcomingPaymentRow(
    subscription: Subscription,
    daysUntil: Int
) {
    // Her abonelik kendi para birimini gösterir
    val currency = CurrencyManager.getCurrency(subscription.currency)
    
    val renewText = when {
        daysUntil == 0 -> stringResource(R.string.payment_today)
        daysUntil == 1 -> stringResource(R.string.payment_tomorrow)
        else -> stringResource(R.string.payment_in_days, daysUntil)
    }
    
    // Thin, horizontal card - completely different from Active Subscriptions
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small logo (left) - much smaller than Active Subscriptions
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !subscription.emoji.isNullOrEmpty() -> {
                        Text(
                            text = subscription.emoji ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    subscription.logoResId != null -> {
                        Image(
                            painter = painterResource(id = subscription.logoResId),
                            contentDescription = subscription.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        Text(
                            text = subscription.name.take(1).uppercase(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Middle section - Name + Renew text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Subscription name (bold)
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // "Renew in X days" - small gray text
                Text(
                    text = renewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Right section - Price (smaller, bold)
            Text(
                text = "${currency?.symbol ?: "₺"}${subscription.price}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Calculate the next payment date based on start date, period, and current date.
 * If start date is in the past, calculate the next occurrence.
 * If start date is in the future, use that date.
 */
fun calculateNextPaymentDate(
    startDate: Calendar,
    period: Period,
    currentDate: Calendar
): Calendar {
    // Create a copy of startDate to avoid modifying the original
    val nextDate = startDate.clone() as Calendar
    
    // If start date is in the future, return it
    if (nextDate.after(currentDate)) {
        return nextDate
    }
    
    // If start date is today or in the past, calculate next occurrence
    when (period) {
        Period.MONTHLY -> {
            // Add months until we get a date in the future
            while (!nextDate.after(currentDate)) {
                nextDate.add(Calendar.MONTH, 1)
            }
        }
        Period.YEARLY -> {
            // Add years until we get a date in the future
            while (!nextDate.after(currentDate)) {
                nextDate.add(Calendar.YEAR, 1)
            }
        }
    }
    
    return nextDate
}

