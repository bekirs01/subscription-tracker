package com.example.subscriptiontracker.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.SubscriptionItem
import com.example.subscriptiontracker.Period
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.data.fx.ExchangeRateRepository
import com.example.subscriptiontracker.data.fx.FxState
import java.util.Calendar
import java.util.Locale


data class TotalCostResult(
    val total: Double?,
    val hasMultipleCurrencies: Boolean,
    val showConversionWarning: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    subscriptions: List<Subscription>,
    onSubscriptionsChanged: (List<Subscription>) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onAddSubscription: () -> Unit,
    onEditSubscription: (Int) -> Unit,
    onDeleteSubscription: (Int) -> Unit
) {
    val context = LocalContext.current
    val currencyFlow = remember { CurrencyManager.getCurrencyFlow(context) }
    val currentCurrency by currencyFlow.collectAsState(initial = CurrencyManager.defaultCurrency)
    val baseCurrency = CurrencyManager.getCurrency(currentCurrency)
    
    val fxStateFlow = remember(currentCurrency) {
        ExchangeRateRepository.ratesFlow(context, currentCurrency)
    }
    val fxState by fxStateFlow.collectAsState()
    
    LaunchedEffect(subscriptions) {
        onSubscriptionsChanged(subscriptions)
    }
    
    // Calculate total monthly cost - TÜM abonelikleri base currency'ye dönüştürerek topla
    val totals = remember(subscriptions, currentCurrency, fxState) {
        val currencies = subscriptions.map { it.currency }.distinct()
        val hasMultiple = currencies.size > 1
        
        if (!hasMultiple) {
            // TEK para birimi varsa: direkt topla
            val total = subscriptions.sumOf { subscription ->
                val price = subscription.price.toDoubleOrNull() ?: 0.0
                if (subscription.period == Period.YEARLY) {
                    price / 12.0
                } else {
                    price
                }
            }
            TotalCostResult(total, hasMultipleCurrencies = false, showConversionWarning = false)
        } else {
            // BİRDEN FAZLA para birimi var
            when (val currentFxState = fxState) {
                is FxState.Ready -> {
                    val fx = currentFxState.fx
                    if (fx.base != currentCurrency) {
                        TotalCostResult(0.0, hasMultipleCurrencies = true, showConversionWarning = false)
                    } else {
                        var total = 0.0
                        
                        subscriptions.forEach { subscription ->
                            val price = subscription.price.toDoubleOrNull() ?: 0.0
                            val monthlyPrice = if (subscription.period == Period.YEARLY) {
                                price / 12.0
                            } else {
                                price
                            }
                            
                            if (subscription.currency == currentCurrency) {
                                total += monthlyPrice
                            } else {
                                val fromRate = fx.rates[subscription.currency]
                                if (fromRate != null && fromRate > 0.0) {
                                    val baseAmount = monthlyPrice / fromRate
                                    total += baseAmount
                                }
                            }
                        }
                        
                        TotalCostResult(total, hasMultipleCurrencies = true, showConversionWarning = false)
                    }
                }
                is FxState.Unavailable, is FxState.Loading -> {
                    TotalCostResult(0.0, hasMultipleCurrencies = true, showConversionWarning = false)
                }
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HomeHeader(
            appTitle = stringResource(R.string.app_title),
            onAddClick = onAddSubscription
        )
        if (subscriptions.isEmpty()) {
            // Boş durumda içeriği ekranın tam ortasında göster
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    onAddClick = onAddSubscription
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Monthly Summary Card
                item {
                    MonthlySummaryCard(
                        totalMonthlyCost = totals.total ?: 0.0,
                        activeCount = subscriptions.size,
                        baseCurrency = baseCurrency
                    )
                }
                
                // Active Subscriptions Row
                item {
                    ActiveSubscriptionsRow(
                        subscriptions = subscriptions,
                        onEditSubscription = onEditSubscription,
                        onDeleteSubscription = onDeleteSubscription
                    )
                }
                
                // Upcoming Payments List
                item {
                    UpcomingPaymentsList(
                        subscriptions = subscriptions
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(
    appTitle: String,
    onAddClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = appTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                    imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.nav_add),
                                modifier = Modifier.size(24.dp)
                            )
                        }
        }
    }
}

@Composable
fun MonthlySummaryCard(
    totalMonthlyCost: Double,
    activeCount: Int,
    baseCurrency: com.example.subscriptiontracker.utils.Currency?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Monthly spend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val symbol = baseCurrency?.symbol ?: "₺"
                Text(
                    text = "$symbol${String.format(Locale.getDefault(), "%.2f", totalMonthlyCost)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = activeCount.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ActiveSubscriptionsRow(
    subscriptions: List<Subscription>,
    onEditSubscription: (Int) -> Unit,
    onDeleteSubscription: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Active Subscriptions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(subscriptions) { subscription ->
                ActiveSubscriptionCard(
                    subscription = subscription,
                    onClick = { onEditSubscription(subscription.id) },
                    onDelete = { onDeleteSubscription(subscription.id) }
                )
            }
        }
    }
}

@Composable
fun ActiveSubscriptionCard(
    subscription: Subscription,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val currency = CurrencyManager.getCurrency(subscription.currency)
    val monthlyPrice = remember(subscription) {
        val price = subscription.price.toDoubleOrNull() ?: 0.0
        if (subscription.period == Period.YEARLY) {
            price / 12.0
        } else {
            price
        }
    }
    
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                val isDarkTheme = isSystemInDarkTheme()
                when {
                    !subscription.emoji.isNullOrEmpty() -> {
                        Text(
                            text = subscription.emoji ?: "",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                    subscription.logoResId != null -> {
                        Image(
                            painter = painterResource(id = subscription.logoResId),
                            contentDescription = subscription.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                    !subscription.logoUrl.isNullOrEmpty() -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(subscription.logoUrl)
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .allowHardware(false)
                                .build(),
                            contentDescription = subscription.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit,
                            colorFilter = if (isDarkTheme) {
                                ColorFilter.tint(Color.White)
                            } else {
                                null
                            }
                        )
                    }
                    else -> {
                        Text(
                            text = subscription.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // App Name
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Price
            Text(
                text = "${currency?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", monthlyPrice)} / month",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UpcomingPaymentsList(
    subscriptions: List<Subscription>
) {
    // Helper function to parse date string to Calendar
    fun parseDateString(dateString: String): Calendar? {
        return try {
            if (dateString.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                val parts = dateString.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1
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
    
    fun daysBetween(cal1: Calendar, cal2: Calendar): Int {
        val diff = cal2.timeInMillis - cal1.timeInMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
    
    fun calculateNextPaymentDate(
        startDate: Calendar,
        period: Period,
        currentDate: Calendar
    ): Calendar {
        val nextDate = startDate.clone() as Calendar
        if (nextDate.after(currentDate)) {
            return nextDate
        }
        when (period) {
            Period.MONTHLY -> {
                while (!nextDate.after(currentDate)) {
                    nextDate.add(Calendar.MONTH, 1)
                }
            }
            Period.YEARLY -> {
                while (!nextDate.after(currentDate)) {
                    nextDate.add(Calendar.YEAR, 1)
                }
            }
        }
        return nextDate
    }
    
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
                val nextPaymentDate = calculateNextPaymentDate(startDate, subscription.period, currentDate)
                val daysUntil = daysBetween(currentDate, nextPaymentDate)
                if (daysUntil in 0..30) {
                    Pair(subscription, daysUntil)
                    } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.second }
    }
    
    if (upcomingPayments.isNotEmpty()) {
        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
            Text(
                text = "Upcoming Payments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                upcomingPayments.forEach { (subscription, daysUntil) ->
                    UpcomingPaymentCard(
                        subscription = subscription,
                        daysUntil = daysUntil
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingPaymentCard(
    subscription: Subscription,
    daysUntil: Int
) {
    val currency = CurrencyManager.getCurrency(subscription.currency)
    val renewText = when (daysUntil) {
        0 -> stringResource(R.string.payment_today)
        1 -> stringResource(R.string.payment_tomorrow)
        else -> stringResource(R.string.payment_in_days, daysUntil)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                    val isDarkTheme = isSystemInDarkTheme()
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
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                        !subscription.logoUrl.isNullOrEmpty() -> {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(subscription.logoUrl)
                                    .decoderFactory(SvgDecoder.Factory())
                                    .crossfade(true)
                                    .allowHardware(false)
                                    .build(),
                                contentDescription = subscription.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Fit,
                                colorFilter = if (isDarkTheme) {
                                    ColorFilter.tint(Color.White)
                                } else {
                                    null
                                }
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
                
                // Name + Days
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = renewText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Price
            Text(
                text = "${currency?.symbol ?: "₺"}${subscription.price}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmptyState(
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onAddClick),
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
                if (daysUntil in 0..30) {
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
    
    val renewText = when (daysUntil) {
        0 -> stringResource(R.string.payment_today)
        1 -> stringResource(R.string.payment_tomorrow)
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
                val isDarkTheme = isSystemInDarkTheme()
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
                    !subscription.logoUrl.isNullOrEmpty() -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(subscription.logoUrl)
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .allowHardware(false)
                                .build(),
                            contentDescription = subscription.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit,
                            colorFilter = if (isDarkTheme) {
                                ColorFilter.tint(Color.White)
                            } else {
                                null
                            }
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

private fun parseDateString(dateString: String): Calendar? {
    return try {
        if (dateString.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            val parts = dateString.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
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

enum class BudgetPeriod {
    MONTHLY, YEARLY
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun BudgetStatsScreen(
    subscriptions: List<Subscription>,
    baseCurrency: String,
    fxState: FxState,
    context: android.content.Context
) {
    // context parameter is kept for future use
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    
    val baseCurrencyObj = CurrencyManager.getCurrency(baseCurrency)
    val fxRates = when (fxState) {
        is FxState.Ready -> fxState.fx.rates
        else -> null
    }
    
    val filteredSubscriptions = remember(subscriptions, selectedPeriod, selectedYear) {
        val currentDate = Calendar.getInstance()
        subscriptions.filter { subscription ->
            val renewalDate = parseDateString(subscription.renewalDate) ?: return@filter false
            val nextPayment = calculateNextPaymentDate(renewalDate, subscription.period, currentDate)
            
            when (selectedPeriod) {
                BudgetPeriod.MONTHLY -> {
                    nextPayment.get(Calendar.YEAR) == selectedYear
                }
                BudgetPeriod.YEARLY -> {
                    nextPayment.get(Calendar.YEAR) == selectedYear
                }
            }
        }
    }
    
    val convertedSubscriptions = remember(filteredSubscriptions, fxRates, baseCurrency) {
        filteredSubscriptions.mapNotNull { subscription ->
            val price = subscription.price.toDoubleOrNull() ?: return@mapNotNull null
            val monthlyPrice = if (subscription.period == Period.YEARLY) {
                price / 12.0
            } else {
                price
            }
            
            val convertedAmount = if (subscription.currency == baseCurrency) {
                monthlyPrice
            } else {
                if (fxRates != null) {
                    val fromRate = fxRates[subscription.currency]
                    if (fromRate != null && fromRate > 0.0) {
                        monthlyPrice / fromRate
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            
            if (convertedAmount != null) {
                Pair(subscription, convertedAmount)
            } else {
                null
            }
        }
    }
    
    val totalAmount = convertedSubscriptions.sumOf { it.second }
    
    val monthlyData = remember(convertedSubscriptions, selectedYear) {
        val months = mutableMapOf<Int, Double>()
        convertedSubscriptions.forEach { (subscription, amount) ->
            val renewalDate = parseDateString(subscription.renewalDate) ?: return@forEach
            val currentDate = Calendar.getInstance()
            val nextPayment = calculateNextPaymentDate(renewalDate, subscription.period, currentDate)
            
            if (nextPayment.get(Calendar.YEAR) == selectedYear) {
                val month = nextPayment.get(Calendar.MONTH)
                months[month] = (months[month] ?: 0.0) + amount
            }
        }
        (0..11).map { month ->
            months[month] ?: 0.0
        }
    }
    
    val categoryData = remember(convertedSubscriptions) {
        convertedSubscriptions.groupBy { it.first.name }
            .mapValues { (_, list) -> list.sumOf { it.second } }
            .toList()
            .sortedByDescending { it.second }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SegmentedButton(
                    selected = selectedPeriod == BudgetPeriod.MONTHLY,
                    onClick = { selectedPeriod = BudgetPeriod.MONTHLY },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aylık")
                }
                SegmentedButton(
                    selected = selectedPeriod == BudgetPeriod.YEARLY,
                    onClick = { selectedPeriod = BudgetPeriod.YEARLY },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yıllık")
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Harcama Trendi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        var yearExpanded by remember { mutableStateOf(false) }
                        Box {
                            FilterChip(
                                selected = false,
                                onClick = { yearExpanded = true },
                                label = { Text(selectedYear.toString()) }
                            )
                            DropdownMenu(
                                expanded = yearExpanded,
                                onDismissRequest = { yearExpanded = false }
                            ) {
                                ((currentYear - 2)..(currentYear + 2)).forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text(year.toString()) },
                                        onClick = {
                                            selectedYear = year
                                            yearExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    if (selectedPeriod == BudgetPeriod.MONTHLY) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            monthlyData.forEachIndexed { index, amount ->
                                val maxAmount = monthlyData.maxOrNull() ?: 1.0
                                val height = if (maxAmount > 0) (amount / maxAmount * 100).dp else 0.dp
                                
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(maxOf(height, 4.dp))
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        val yearlyTotal = monthlyData.sum()
                        Text(
                            text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", yearlyTotal)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Kategori Dağılımı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val total = categoryData.sumOf { it.second }
                        if (total > 0 && categoryData.isNotEmpty()) {
                            val colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                var currentAngle = -90f
                                categoryData.forEachIndexed { index, (_, amount) ->
                                    val sweepAngle = (amount / total * 360f).toFloat()
                                    val color = colors[index % colors.size]
                                    drawArc(
                                        color = color,
                                        startAngle = currentAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    currentAngle += sweepAngle
                                }
                            }
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryData.take(5).forEach { (name, amount) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", amount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Text(
                text = "Active Subscriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(convertedSubscriptions) { (subscription, amount) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        val isDarkTheme = isSystemInDarkTheme()
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
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            !subscription.logoUrl.isNullOrEmpty() -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(subscription.logoUrl)
                                        .decoderFactory(SvgDecoder.Factory())
                                        .crossfade(true)
                                        .allowHardware(false)
                                        .build(),
                                    contentDescription = subscription.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = if (isDarkTheme) {
                                        ColorFilter.tint(Color.White)
                                    } else {
                                        null
                                    }
                                )
                            }
                            else -> {
                                Text(
                                    text = subscription.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = subscription.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (subscription.period == Period.MONTHLY) "Monthly" else "Yearly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", amount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
