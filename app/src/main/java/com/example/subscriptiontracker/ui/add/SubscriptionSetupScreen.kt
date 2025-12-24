package com.example.subscriptiontracker.ui.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.PopularService
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.Period
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.utils.PeriodManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionSetupScreen(
    service: PopularService,
    onNavigateBack: () -> Unit,
    onSave: (Subscription) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currencyFlow = remember { CurrencyManager.getCurrencyFlow(context) }
    val currentCurrency by currencyFlow.collectAsState(initial = CurrencyManager.defaultCurrency)
    val currency = CurrencyManager.getCurrency(currentCurrency)
    
    val defaultPeriodFlow = remember { PeriodManager.getDefaultPeriodFlow(context) }
    val defaultPeriodString by defaultPeriodFlow.collectAsState(initial = PeriodManager.defaultPeriod)
    
    var price by remember { mutableStateOf("") }
    var selectedPeriod by remember { 
        mutableStateOf(
            if (defaultPeriodString == "YEARLY") Period.YEARLY else Period.MONTHLY
        )
    }
    var renewalDate by remember { mutableStateOf("") }
    var periodExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }
    
    // Validation states
    var priceError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    
    // String resources
    val errorPriceInvalid = stringResource(R.string.error_price_invalid)
    val errorDateFormat = stringResource(R.string.error_date_format)
    val errorDateInvalid = stringResource(R.string.error_date_invalid)
    
    // Form validity
    val isFormValid = price.isNotBlank() && 
                      renewalDate.isNotBlank() &&
                      priceError == null && 
                      dateError == null
    
    // Validation functions
    fun validatePrice(input: String, errorMsg: String): String? {
        return when {
            input.isBlank() -> null
            !input.matches(Regex("^\\d+(\\.\\d{1,2})?$")) -> errorMsg
            input.toDoubleOrNull() == null -> errorMsg
            else -> null
        }
    }
    
    fun validateDate(input: String, formatError: String, invalidError: String): String? {
        return when {
            input.isBlank() -> null
            !input.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> formatError
            else -> {
                try {
                    val parts = input.split("-")
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    val day = parts[2].toInt()
                    if (month !in 1..12 || day !in 1..31 || year < 2020) {
                        invalidError
                    } else null
                } catch (e: Exception) {
                    invalidError
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = service.name,
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
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            if (isFormValid) {
                                scope.launch {
                                    PeriodManager.saveDefaultPeriod(
                                        context, 
                                        if (selectedPeriod == Period.MONTHLY) "MONTHLY" else "YEARLY"
                                    )
                                }
                                onSave(
                                    Subscription(
                                        id = 0,
                                        name = service.name,
                                        price = price,
                                        period = selectedPeriod,
                                        renewalDate = renewalDate,
                                        logoUrl = service.logoUrl,
                                        currency = selectedCurrency
                                    )
                                )
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Service Logo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = service.logoUrl,
                        contentDescription = service.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            // Period Selection
            Text(
                text = stringResource(R.string.select_period_first),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Box {
                OutlinedTextField(
                    value = if (selectedPeriod == Period.MONTHLY) stringResource(R.string.monthly) else stringResource(R.string.yearly),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.period)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { periodExpanded = true },
                    readOnly = true,
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        IconButton(onClick = { periodExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = periodExpanded,
                    onDismissRequest = { periodExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.monthly)) },
                        onClick = {
                            selectedPeriod = Period.MONTHLY
                            periodExpanded = false
                            scope.launch {
                                PeriodManager.saveDefaultPeriod(context, "MONTHLY")
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.yearly)) },
                        onClick = {
                            selectedPeriod = Period.YEARLY
                            periodExpanded = false
                            scope.launch {
                                PeriodManager.saveDefaultPeriod(context, "YEARLY")
                            }
                        }
                    )
                }
            }
            
            // Price Field
            OutlinedTextField(
                value = price,
                onValueChange = { 
                    price = it
                    priceError = validatePrice(it, errorPriceInvalid)
                },
                label = { Text(stringResource(R.string.price)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = priceError != null,
                supportingText = priceError?.let { { Text(it) } },
                shape = MaterialTheme.shapes.medium,
                prefix = {
                    Text(
                        text = "${CurrencyManager.getCurrency(selectedCurrency)?.symbol ?: "₺"} ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            
            // Currency Selection
            Box {
                OutlinedTextField(
                    value = CurrencyManager.getCurrency(selectedCurrency)?.name ?: selectedCurrency,
                    onValueChange = {},
                    label = { Text(stringResource(R.string.currency)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { currencyExpanded = true },
                    readOnly = true,
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        IconButton(onClick = { currencyExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false }
                ) {
                    CurrencyManager.getAllCurrencies().forEach { curr ->
                        DropdownMenuItem(
                            text = { Text("${curr.symbol} ${curr.name}") },
                            onClick = {
                                selectedCurrency = curr.code
                                currencyExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Renewal Date Field
            OutlinedTextField(
                value = renewalDate,
                onValueChange = { 
                    renewalDate = it
                    dateError = validateDate(it, errorDateFormat, errorDateInvalid)
                },
                label = { Text(stringResource(R.string.renewal_date)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.renewal_date_placeholder)) },
                isError = dateError != null,
                supportingText = dateError?.let { { Text(it) } },
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

