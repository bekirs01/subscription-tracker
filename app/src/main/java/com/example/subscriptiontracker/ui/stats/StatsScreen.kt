package com.example.subscriptiontracker.ui.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.Period
import com.example.subscriptiontracker.data.fx.FxState
import com.example.subscriptiontracker.utils.CurrencyManager
import java.util.Calendar
import java.util.Locale

enum class StatsPeriod {
    MONTHLY, YEARLY
}

@Composable
fun StatsScreen(
    subscriptions: List<Subscription>,
    baseCurrency: String,
    fxState: FxState,
    context: android.content.Context
) {
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.MONTHLY) }
    
    val baseCurrencyObj = CurrencyManager.getCurrency(baseCurrency)
    val fxRates = when (fxState) {
        is FxState.Ready -> fxState.fx.rates
        else -> null
    }
    
    // Convert subscriptions to base currency and calculate monthly amounts
    val convertedSubscriptions = remember(subscriptions, fxRates, baseCurrency) {
        subscriptions.mapNotNull { subscription ->
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
    
    // Calculate total
    val totalAmount = convertedSubscriptions.sumOf { it.second }
    val totalAmountAnimated by animateFloatAsState(
        targetValue = totalAmount.toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "totalAmount"
    )
    
    // Monthly data for chart (last 12 months or current year)
    val monthlyData = remember(convertedSubscriptions, selectedPeriod) {
        val currentDate = Calendar.getInstance()
        val months = mutableMapOf<Int, Double>()
        
        convertedSubscriptions.forEach { (subscription, amount) ->
            val renewalDate = try {
                if (subscription.renewalDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                    val parts = subscription.renewalDate.split("-")
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1
                    val day = parts[2].toInt()
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
            
            if (renewalDate != null) {
                val nextPayment = calculateNextPaymentDate(renewalDate, subscription.period, currentDate)
                val monthIndex = if (selectedPeriod == StatsPeriod.MONTHLY) {
                    // Last 12 months
                    val monthsDiff = (currentDate.get(Calendar.YEAR) - nextPayment.get(Calendar.YEAR)) * 12 +
                            (currentDate.get(Calendar.MONTH) - nextPayment.get(Calendar.MONTH))
                    if (monthsDiff in 0..11) 11 - monthsDiff else -1
                } else {
                    // Current year months
                    if (nextPayment.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)) {
                        nextPayment.get(Calendar.MONTH)
                    } else {
                        -1
                    }
                }
                
                if (monthIndex >= 0 && monthIndex < 12) {
                    months[monthIndex] = (months[monthIndex] ?: 0.0) + amount
                }
            }
        }
        
        (0..11).map { months[it] ?: 0.0 }
    }
    
    // Category data (using subscription name as category for now)
    val categoryData = remember(convertedSubscriptions) {
        convertedSubscriptions.groupBy { it.first.name }
            .mapValues { (_, list) -> list.sumOf { it.second } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
    }
    
    // Top 3 most expensive subscriptions
    val topExpensive = remember(convertedSubscriptions) {
        convertedSubscriptions.sortedByDescending { it.second }.take(3)
    }
    
    if (subscriptions.isEmpty()) {
        EmptyStatsState()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Period Selector
            item {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
            }
            
            // Total Spending Card
            item {
                TotalSpendingCard(
                    totalAmount = totalAmountAnimated.toDouble(),
                    activeCount = subscriptions.size,
                    baseCurrencyObj = baseCurrencyObj,
                    period = selectedPeriod
                )
            }
            
            // Spending Trend Chart
            item {
                SpendingTrendChart(
                    monthlyData = monthlyData,
                    baseCurrencyObj = baseCurrencyObj,
                    period = selectedPeriod
                )
            }
            
            // Category Distribution
            if (categoryData.isNotEmpty()) {
                item {
                    CategoryDistributionCard(
                        categoryData = categoryData,
                        totalAmount = totalAmount,
                        baseCurrencyObj = baseCurrencyObj
                    )
                }
            }
            
            // Active Subscriptions Summary
            item {
                ActiveSubscriptionsSummary(
                    subscriptions = subscriptions.take(3),
                    convertedSubscriptions = convertedSubscriptions.take(3),
                    baseCurrencyObj = baseCurrencyObj
                )
            }
            
            // Top 3 Most Expensive
            if (topExpensive.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.most_expensive_subscriptions),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(topExpensive) { (subscription, amount) ->
                    ExpensiveSubscriptionCard(
                        subscription = subscription,
                        monthlyAmount = amount,
                        baseCurrencyObj = baseCurrencyObj
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        SegmentedButton(
            selected = selectedPeriod == StatsPeriod.MONTHLY,
            onClick = { onPeriodSelected(StatsPeriod.MONTHLY) },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(stringResource(R.string.monthly))
        }
        SegmentedButton(
            selected = selectedPeriod == StatsPeriod.YEARLY,
            onClick = { onPeriodSelected(StatsPeriod.YEARLY) },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(stringResource(R.string.yearly))
        }
    }
}

@Composable
fun TotalSpendingCard(
    totalAmount: Double,
    activeCount: Int,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?,
    period: StatsPeriod
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.total_spending),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$activeCount ${stringResource(R.string.active_subscriptions).lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Text(
                    text = if (period == StatsPeriod.MONTHLY) {
                        stringResource(R.string.this_month)
                    } else {
                        stringResource(R.string.this_year)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SpendingTrendChart(
    monthlyData: List<Double>,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?,
    period: StatsPeriod
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                    text = stringResource(R.string.spending_trend),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val maxAmount = monthlyData.maxOrNull() ?: 1.0
                monthlyData.forEachIndexed { index, amount ->
                    val heightRatio = if (maxAmount > 0) amount / maxAmount else 0.0
                    val animatedHeight by animateFloatAsState(
                        targetValue = heightRatio.toFloat(),
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        label = "barHeight_$index"
                    )
                    
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
                                .fillMaxHeight(animatedHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (period == StatsPeriod.MONTHLY) {
                                "${index + 1}"
                            } else {
                                getMonthAbbreviation(index)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDistributionCard(
    categoryData: List<Pair<String, Double>>,
    totalAmount: Double,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.category_distribution),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Pie Chart
            val categoryTotal = categoryData.sumOf { it.second }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (categoryTotal > 0 && categoryData.isNotEmpty()) {
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
                            val sweepAngle = (amount / categoryTotal * 360f).toFloat()
                            val color = colors[index % colors.size]
                            drawArc(
                                color = color,
                                startAngle = currentAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 50.dp.toPx(), cap = StrokeCap.Round)
                            )
                            currentAngle += sweepAngle
                        }
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.total),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", categoryTotal)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Category List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categoryData.forEach { (name, amount) ->
                    val percentage = if (categoryTotal > 0) (amount / categoryTotal * 100).toInt() else 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", amount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveSubscriptionsSummary(
    subscriptions: List<Subscription>,
    convertedSubscriptions: List<Pair<Subscription, Double>>,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?
) {
    val totalMonthly = convertedSubscriptions.sumOf { it.second }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
            Text(
                text = stringResource(R.string.active_subscriptions),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${subscriptions.size} ${stringResource(R.string.subscriptions)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", totalMonthly)}/${stringResource(R.string.month)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                subscriptions.take(3).forEach { subscription ->
                    SubscriptionListItem(subscription = subscription, baseCurrencyObj = baseCurrencyObj)
                }
            }
        }
    }
}

@Composable
fun SubscriptionListItem(
    subscription: Subscription,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?
) {
    val context = LocalContext.current
    val monthlyAmount = if (subscription.period == Period.YEARLY) {
        (subscription.price.toDoubleOrNull() ?: 0.0) / 12.0
    } else {
        subscription.price.toDoubleOrNull() ?: 0.0
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val isDarkTheme = isSystemInDarkTheme()
            when {
                !subscription.emoji.isNullOrEmpty() -> {
                    Text(
                        text = subscription.emoji!!,
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
                        model = ImageRequest.Builder(context)
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (subscription.period == Period.MONTHLY) {
                    stringResource(R.string.monthly)
                } else {
                    stringResource(R.string.yearly)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", monthlyAmount)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ExpensiveSubscriptionCard(
    subscription: Subscription,
    monthlyAmount: Double,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                val isDarkTheme = isSystemInDarkTheme()
                when {
                    !subscription.emoji.isNullOrEmpty() -> {
                        Text(
                            text = subscription.emoji!!,
                            style = MaterialTheme.typography.displaySmall
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
                            model = ImageRequest.Builder(context)
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
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", monthlyAmount)}/${if (subscription.period == Period.MONTHLY) stringResource(R.string.month) else stringResource(R.string.year)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyStatsState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = stringResource(R.string.no_subscriptions_yet),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.add_first_subscription_to_start),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// Helper functions
private fun calculateNextPaymentDate(
    startDate: Calendar,
    period: Period,
    currentDate: Calendar
): Calendar {
    val nextPayment = startDate.clone() as Calendar
    while (nextPayment.before(currentDate) || nextPayment == currentDate) {
        when (period) {
            Period.MONTHLY -> nextPayment.add(Calendar.MONTH, 1)
            Period.YEARLY -> nextPayment.add(Calendar.YEAR, 1)
        }
    }
    return nextPayment
}

private fun getMonthAbbreviation(monthIndex: Int): String {
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return if (monthIndex in 0..11) months[monthIndex] else ""
}

