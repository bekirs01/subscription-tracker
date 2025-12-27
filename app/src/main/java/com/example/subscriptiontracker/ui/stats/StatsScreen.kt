package com.example.subscriptiontracker.ui.stats

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.sp
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
import kotlin.random.Random

@Composable
fun StatsScreen(
    subscriptions: List<Subscription>,
    baseCurrency: String,
    fxState: FxState,
    @Suppress("UNUSED_PARAMETER") context: android.content.Context
) {
    val baseCurrencyObj = CurrencyManager.getCurrency(baseCurrency)
    val fxRates = when (fxState) {
        is FxState.Ready -> fxState.fx.rates
        else -> null
    }
    
    // Convert ALL subscriptions to base currency - TÜM fiyatlar seçili para birimine çevrilir
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
    
    // Calculate total monthly spending
    val totalAmount = convertedSubscriptions.sumOf { it.second }
    val totalAmountAnimated by animateFloatAsState(
        targetValue = totalAmount.toFloat(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "totalAmount"
    )
    
    // Last 12 months data for trend chart - Her ay için aktif aboneliklerin toplamı
    val monthlyData = remember(convertedSubscriptions, subscriptions) {
        val currentDate = Calendar.getInstance()
        val months = mutableListOf<Double>()
        
        // Son 12 ay için hesapla (0 = bu ay, 11 = 11 ay önce)
        (0..11).forEach { monthOffset ->
            var monthTotal = 0.0
            val targetMonth = Calendar.getInstance().apply {
                add(Calendar.MONTH, -monthOffset)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            convertedSubscriptions.forEach { (subscription, monthlyAmount) ->
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
                
                if (renewalDate != null) {
                    // Bu ay için ödeme var mı kontrol et
                    var checkDate = renewalDate.clone() as Calendar
                    while (checkDate.before(targetMonth) || checkDate == targetMonth) {
                        if (checkDate.get(Calendar.YEAR) == targetMonth.get(Calendar.YEAR) &&
                            checkDate.get(Calendar.MONTH) == targetMonth.get(Calendar.MONTH)) {
                            monthTotal += monthlyAmount
                            break
                        }
                        when (subscription.period) {
                            Period.MONTHLY -> checkDate.add(Calendar.MONTH, 1)
                            Period.YEARLY -> checkDate.add(Calendar.YEAR, 1)
                        }
                        // Sonsuz döngüyü önle
                        if (checkDate.after(currentDate) && checkDate.after(targetMonth)) break
                    }
                }
            }
            
            months.add(monthTotal)
        }
        
        months.reversed() // En eski ay ilk sırada (0 index = 11 ay önce, 11 index = bu ay)
    }
    
    // Category data (grouped by subscription name)
    val categoryData = remember(convertedSubscriptions) {
        convertedSubscriptions.groupBy { it.first.name }
            .mapValues { (_, list) -> list.sumOf { it.second } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
    }
    
    val categoryTotal = categoryData.sumOf { it.second }
    
    // Top 3 most expensive subscriptions
    val topExpensive = remember(convertedSubscriptions) {
        convertedSubscriptions.sortedByDescending { it.second }.take(3)
    }
    
    // Yearly cost insight - pick one subscription with stable randomness
    val yearlyCostInsight: Triple<Subscription, Double, String>? = remember(convertedSubscriptions) {
        if (convertedSubscriptions.isEmpty()) {
            null
        } else {
            val selectedIndex = Random.nextInt(convertedSubscriptions.size)
            val (subscription, monthlyAmount) = convertedSubscriptions[selectedIndex]
            val yearlyCost = monthlyAmount * 12
            if (yearlyCost > 0) {
                Triple(subscription, yearlyCost, getComparisonForAmount(yearlyCost))
            } else {
                null
            }
        }
    }
    
    // Screen entry animation
    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        screenVisible = true
    }
    
    if (subscriptions.isEmpty()) {
        EmptyStatsState()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Premium Summary Card (Aylık/Yıllık toggle KALDIRILDI)
            item {
                AnimatedVisibility(
                    visible = screenVisible,
                    enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )
                ) {
                    PremiumSummaryCard(
                        totalAmount = totalAmountAnimated.toDouble(),
                        activeCount = subscriptions.size,
                        baseCurrencyObj = baseCurrencyObj
                    )
                }
            }
            
            // Spending Trend Chart - Son 12 ay
            item {
                AnimatedVisibility(
                    visible = screenVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) + slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(600, delayMillis = 100, easing = FastOutSlowInEasing)
                    )
                ) {
                    SpendingTrendChart(
                        monthlyData = monthlyData,
                        baseCurrencyObj = baseCurrencyObj
                    )
                }
            }
            
            // Category Distribution - Geliştirilmiş Donut Chart
            if (categoryData.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = screenVisible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 200)) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(600, delayMillis = 200, easing = FastOutSlowInEasing)
                        )
                    ) {
                        CategoryDistributionCard(
                            categoryData = categoryData,
                            categoryTotal = categoryTotal,
                            baseCurrencyObj = baseCurrencyObj
                        )
                    }
                }
            }
            
            // Active Subscriptions Summary
            item {
                AnimatedVisibility(
                    visible = screenVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) + slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(600, delayMillis = 300, easing = FastOutSlowInEasing)
                    )
                ) {
                    ActiveSubscriptionsSummary(
                        subscriptions = subscriptions,
                        convertedSubscriptions = convertedSubscriptions,
                        baseCurrencyObj = baseCurrencyObj
                    )
                }
            }
            
            // Yearly Cost Insight Card
            if (yearlyCostInsight != null) {
                item {
                    AnimatedVisibility(
                        visible = screenVisible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 350)) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(600, delayMillis = 350, easing = FastOutSlowInEasing)
                        )
                    ) {
                        YearlyCostInsightCard(
                            subscription = yearlyCostInsight.first,
                            yearlyCost = yearlyCostInsight.second,
                            comparisonText = yearlyCostInsight.third,
                            baseCurrencyObj = baseCurrencyObj
                        )
                    }
                }
            }
            
            // Top 3 Most Expensive Subscriptions
            if (topExpensive.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = screenVisible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 400)) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(600, delayMillis = 400, easing = FastOutSlowInEasing)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.most_expensive_subscriptions),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                
                items(topExpensive) { (subscription, amount) ->
                    AnimatedVisibility(
                        visible = screenVisible,
                        enter = fadeIn(animationSpec = tween(600, delayMillis = 500)) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(600, delayMillis = 500, easing = FastOutSlowInEasing)
                        )
                    ) {
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
}

@Composable
fun PremiumSummaryCard(
    totalAmount: Double,
    activeCount: Int,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.total_spending),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "$activeCount ${stringResource(R.string.active_subscriptions).lowercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun SpendingTrendChart(
    monthlyData: List<Double>,
    @Suppress("UNUSED_PARAMETER") baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Bar Chart - 6 ay göster, yatay scroll ile diğer 6 ay
            val maxAmount = monthlyData.maxOrNull() ?: 1.0
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(monthlyData.size) { index ->
                    val amount = monthlyData[index]
                    val heightRatio = if (maxAmount > 0) amount / maxAmount else 0.0
                    val animatedHeight by animateFloatAsState(
                        targetValue = heightRatio.toFloat(),
                        animationSpec = tween(800, delayMillis = index * 50, easing = FastOutSlowInEasing),
                        label = "barHeight_$index"
                    )
                    
                    // 11 ay önceden başlayarak index'e göre ay hesapla
                    val monthOffset = 11 - index
                    val targetCalendar = Calendar.getInstance().apply {
                        add(Calendar.MONTH, -monthOffset)
                    }
                    val monthAbbr = getMonthAbbreviationFromCalendar(targetCalendar)
                    
                    Column(
                        modifier = Modifier
                            .width(40.dp)
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
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = monthAbbr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
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
    categoryTotal: Double,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?
) {
    var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            // Donut Chart - Küçültülmüş
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
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
                            val color = if (selectedCategoryIndex == index) {
                                colors[index % colors.size].copy(alpha = 0.8f)
                            } else {
                                colors[index % colors.size]
                            }
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
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", categoryTotal)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Category List - Renk noktası, % oran, tutar
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                categoryData.forEachIndexed { index, (name, amount) ->
                    val percentage = if (categoryTotal > 0) (amount / categoryTotal * 100).toInt() else 0
                    val colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                    val categoryColor = colors[index % colors.size]
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategoryIndex = if (selectedCategoryIndex == index) null else index
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                subscriptions.forEach { subscription ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { }),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        SubscriptionListItem(
                            subscription = subscription,
                            convertedSubscriptions = convertedSubscriptions,
                            baseCurrencyObj = baseCurrencyObj
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionListItem(
    subscription: Subscription,
    convertedSubscriptions: List<Pair<Subscription, Double>>,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?
) {
    val context = LocalContext.current
    val monthlyAmount = convertedSubscriptions.find { it.first.id == subscription.id }?.second ?: 0.0
    
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    val isDarkTheme = isSystemInDarkTheme()
                    when {
                        !subscription.emoji.isNullOrEmpty() -> {
                            Text(
                                text = subscription.emoji!!,
                                style = MaterialTheme.typography.displayMedium
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
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", monthlyAmount)}/${if (subscription.period == Period.MONTHLY) stringResource(R.string.month) else stringResource(R.string.year)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun YearlyCostInsightCard(
    subscription: Subscription,
    yearlyCost: Double,
    comparisonText: String,
    baseCurrencyObj: com.example.subscriptiontracker.utils.Currency?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title with icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "If you keep ${subscription.name} for 1 year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Yearly cost
                Text(
                    text = "${baseCurrencyObj?.symbol ?: "₺"}${String.format(Locale.getDefault(), "%.2f", yearlyCost)} / year",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Comparison text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "With that money, you could buy:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "👉 $comparisonText",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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

// Response pool for humorous comparisons
private data class ComparisonResponse(
    val minAmount: Double,
    val maxAmount: Double,
    val text: String
)

private val comparisonResponses = listOf(
    // LOW RANGE (0-1.000)
    ComparisonResponse(0.0, 50.0, "A nice coffee subscription ☕"),
    ComparisonResponse(50.0, 100.0, "A few fancy restaurant meals 🍽️"),
    ComparisonResponse(100.0, 200.0, "A pair of wireless earbuds 🎧"),
    ComparisonResponse(200.0, 300.0, "A nice dinner for two 🍝"),
    ComparisonResponse(300.0, 400.0, "A premium gym membership 💪"),
    ComparisonResponse(400.0, 500.0, "Several cinema nights 🎬"),
    ComparisonResponse(500.0, 600.0, "A weekend city break 🏙️"),
    ComparisonResponse(600.0, 700.0, "A quality pair of headphones 🎧"),
    ComparisonResponse(700.0, 800.0, "A smartwatch ⌚"),
    ComparisonResponse(800.0, 900.0, "A tablet device 📱"),
    ComparisonResponse(900.0, 1000.0, "A bicycle 🚲"),
    
    // MID RANGE (1.000-5.000)
    ComparisonResponse(1000.0, 1200.0, "A mid-range smartphone 📱"),
    ComparisonResponse(1200.0, 1500.0, "A weekend trip to Europe ✈️"),
    ComparisonResponse(1500.0, 1800.0, "A gaming console + games 🎮"),
    ComparisonResponse(1800.0, 2000.0, "A premium camera 📷"),
    ComparisonResponse(2000.0, 2200.0, "A home entertainment system 🎬"),
    ComparisonResponse(2200.0, 2500.0, "A nice vacation package 🏝️"),
    ComparisonResponse(2500.0, 2800.0, "A high-end laptop 💻"),
    ComparisonResponse(2800.0, 3000.0, "A motorcycle 🏍️"),
    ComparisonResponse(3000.0, 3300.0, "A complete home office setup 🖥️"),
    ComparisonResponse(3300.0, 3600.0, "A luxury watch ⌚"),
    ComparisonResponse(3600.0, 4000.0, "A family vacation 🌴"),
    ComparisonResponse(4000.0, 4500.0, "A used car down payment 🚗"),
    ComparisonResponse(4500.0, 5000.0, "A professional camera setup 📸"),
    
    // HIGH RANGE (5.000-15.000)
    ComparisonResponse(5000.0, 6000.0, "A premium laptop + accessories 💻"),
    ComparisonResponse(6000.0, 7000.0, "A long vacation abroad 🏖️"),
    ComparisonResponse(7000.0, 8000.0, "Half a motorcycle 🏍️"),
    ComparisonResponse(8000.0, 9000.0, "A used car 🚗"),
    ComparisonResponse(9000.0, 10000.0, "A home renovation project 🏠"),
    ComparisonResponse(10000.0, 11000.0, "A full home office setup 🖥️"),
    ComparisonResponse(11000.0, 12000.0, "A year of rent in some cities 🏠"),
    ComparisonResponse(12000.0, 13000.0, "A down payment on a car 🚙"),
    ComparisonResponse(13000.0, 14000.0, "A luxury vacation package ✈️"),
    ComparisonResponse(14000.0, 15000.0, "A complete home gym 🏋️"),
    
    // VERY HIGH RANGE (15.000+)
    ComparisonResponse(15000.0, 20000.0, "A used car 🚗"),
    ComparisonResponse(20000.0, 25000.0, "A new car down payment 🚙"),
    ComparisonResponse(25000.0, 30000.0, "A year of university tuition 🎓"),
    ComparisonResponse(30000.0, 35000.0, "A small apartment deposit 🏘️"),
    ComparisonResponse(35000.0, 40000.0, "A luxury car down payment 🏎️"),
    ComparisonResponse(40000.0, 50000.0, "A year of rent in major cities 🏙️"),
    ComparisonResponse(50000.0, 60000.0, "A down payment on a house 🏡"),
    ComparisonResponse(60000.0, 75000.0, "A luxury car 🏎️"),
    ComparisonResponse(75000.0, 100000.0, "A year of MBA tuition 🎓"),
    ComparisonResponse(100000.0, Double.MAX_VALUE, "A significant investment portfolio 💰")
)

private fun getComparisonForAmount(yearlyCost: Double): String {
    val matchingResponses = comparisonResponses.filter {
        yearlyCost >= it.minAmount && yearlyCost < it.maxAmount
    }
    return if (matchingResponses.isNotEmpty()) {
        matchingResponses.random().text
    } else {
        "Something nice 🎁"
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

private fun getMonthAbbreviationFromCalendar(calendar: Calendar): String {
    val month = calendar.get(Calendar.MONTH)
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return if (month in 0..11) months[month] else ""
}
