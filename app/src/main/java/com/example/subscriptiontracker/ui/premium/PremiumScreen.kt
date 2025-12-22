package com.example.subscriptiontracker.ui.premium

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.utils.CurrencyManager

data class PremiumPackage(
    val id: String,
    val name: String,
    val price: String,
    val period: String,
    val isPopular: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    onPurchase: (PremiumPackage) -> Unit = {}
) {
    val context = LocalContext.current
    val currencyFlow = remember { CurrencyManager.getCurrencyFlow(context) }
    val currentCurrency by currencyFlow.collectAsState(initial = CurrencyManager.defaultCurrency)
    val currency = CurrencyManager.getCurrency(currentCurrency)
    val currencySymbol = currency?.symbol ?: "₺"
    
    val packages = remember {
        listOf(
            PremiumPackage("monthly", "Monthly", "50", "month", false),
            PremiumPackage("3months", "3 Months", "100", "total", false),
            PremiumPackage("yearly", "Yearly", "150", "year", true),
            PremiumPackage("lifetime", "Lifetime", "250", "one-time", false)
        )
    }
    
    var selectedPackage by remember { mutableStateOf<PremiumPackage?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.premium_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            selectedPackage?.let { onPurchase(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedPackage != null,
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.get_premium),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.upgrade_to_premium),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.premium_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            // Packages
            items(packages.size) { index ->
                val packageItem = packages[index]
                PremiumPackageCard(
                    packageItem = packageItem,
                    currencySymbol = currencySymbol,
                    isSelected = selectedPackage?.id == packageItem.id,
                    onClick = { selectedPackage = packageItem }
                )
            }
        }
    }
}

@Composable
fun PremiumPackageCard(
    packageItem: PremiumPackage,
    currencySymbol: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = packageItem.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (packageItem.isPopular) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = stringResource(R.string.popular),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                Text(
                    text = "$currencySymbol${packageItem.price} / ${packageItem.period}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

