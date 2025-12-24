package com.example.subscriptiontracker.ui.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.Period
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.utils.PeriodManager
import kotlinx.coroutines.launch

// Emoji list for custom subscriptions
private val emojiList = listOf(
    "💡", "🎮", "🎵", "📺", "📦", "💻", "🎧", "📱", "⭐", "🔔",
    "🎬", "📚", "🏋️", "🍔", "☕", "✈️", "🏠", "🚗", "🎨", "📷",
    "🎯", "🏆", "💎", "🎪", "🎭", "🎤", "🎸", "🎹", "🎺", "🥁",
    "🎲", "🧩", "🎨", "🖼️", "📝", "📊", "📈", "💼", "🎓", "🔬",
    "⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🏉", "🏓", "🏸", "🥊",
    "🌍", "🗺️", "🏔️", "🌊", "🌅", "🌄", "🌈", "⭐", "🌟", "✨",
    "🔥", "💧", "❄️", "☀️", "🌙", "⭐", "🌠", "🌌", "🌉", "🌆",
    "🍎", "🍌", "🍇", "🍓", "🍊", "🍋", "🍉", "🍑", "🍒", "🥝",
    "🍕", "🍟", "🍔", "🌭", "🍿", "🧂", "🥤", "🍷", "🍺", "☕",
    "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailsScreen(
    predefinedService: ServiceItem? = null, // null if custom
    existingSubscription: Subscription? = null, // null if new, not null if editing
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
    
    // Initialize fields based on existing subscription (edit mode) or predefined service/custom (new mode)
    var name by remember { mutableStateOf(existingSubscription?.name ?: predefinedService?.name ?: "") }
    var price by remember { mutableStateOf(existingSubscription?.price ?: "") }
    var selectedPeriod by remember { 
        mutableStateOf(
            existingSubscription?.period ?: if (defaultPeriodString == "YEARLY") Period.YEARLY else Period.MONTHLY
        )
    }
    var startDate by remember { mutableStateOf(existingSubscription?.renewalDate ?: "") }
    var notes by remember { mutableStateOf(existingSubscription?.notes ?: "") }
    var periodExpanded by remember { mutableStateOf(false) }
    var selectedLogoResId by remember { mutableStateOf<Int?>(existingSubscription?.logoResId ?: predefinedService?.drawableResId) }
    var selectedEmoji by remember { mutableStateOf<String?>(existingSubscription?.emoji) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    
    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    
    // String resources
    val errorNameInvalid = stringResource(R.string.error_name_invalid)
    val errorPriceInvalid = stringResource(R.string.error_price_invalid)
    val errorDateFormat = stringResource(R.string.error_date_format)
    val errorDateInvalid = stringResource(R.string.error_date_invalid)
    
    // Form validity
    val logoId = selectedLogoResId
    val isFormValid = name.isNotBlank() && 
                      price.isNotBlank() && 
                      startDate.isNotBlank() &&
                      nameError == null && 
                      priceError == null && 
                      dateError == null &&
                      (predefinedService != null || selectedEmoji != null) // Emoji required for custom
    
    // Validation functions
    fun validateName(input: String, errorMsg: String): String? {
        return when {
            input.isBlank() -> null
            !input.matches(Regex("^[a-zA-ZçğıöşüÇĞIİÖŞÜ0-9\\s]+$")) -> errorMsg
            else -> null
        }
    }
    
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
                        text = if (existingSubscription != null) {
                            "Edit Subscription"
                        } else {
                            stringResource(R.string.subscription_details_title)
                        },
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
                                        id = existingSubscription?.id ?: 0,
                                        name = name.trim(),
                                        price = price,
                                        period = selectedPeriod,
                                        renewalDate = startDate,
                                        logoUrl = existingSubscription?.logoUrl,
                                        logoResId = selectedLogoResId,
                                        emoji = selectedEmoji,
                                        currency = currentCurrency,
                                        notes = notes.trim()
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
            // Logo selector (only for custom, or show logo for predefined, or show existing logo/emoji in edit mode)
            if (existingSubscription != null) {
                // Edit mode: show existing logo or emoji (clickable to change emoji if custom)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable(enabled = !existingSubscription.emoji.isNullOrEmpty()) {
                            if (!existingSubscription.emoji.isNullOrEmpty()) {
                                showEmojiPicker = true
                            }
                        },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            !existingSubscription.emoji.isNullOrEmpty() -> {
                                Text(
                                    text = selectedEmoji ?: existingSubscription.emoji!!,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                            existingSubscription.logoResId != null -> {
                                Image(
                                    painter = painterResource(id = existingSubscription.logoResId),
                                    contentDescription = existingSubscription.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            else -> {
                                Text(
                                    text = existingSubscription.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.displayLarge
                                )
                            }
                        }
                    }
                }
            } else if (predefinedService != null) {
                // Show predefined logo
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
                        Image(
                            painter = painterResource(id = predefinedService.drawableResId),
                            contentDescription = predefinedService.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            } else {
                // Custom subscription - emoji picker (can be edited)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable { showEmojiPicker = true },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedEmoji != null) {
                            Text(
                                text = selectedEmoji!!,
                                style = MaterialTheme.typography.displayLarge,
                                modifier = Modifier.size(80.dp)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.select_logo),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = validateName(it, errorNameInvalid)
                },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                shape = MaterialTheme.shapes.medium
            )
            
            // Billing Period Selection
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
                    label = { Text(stringResource(R.string.billing_period)) },
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
                        text = "${currency?.symbol ?: "₺"} ",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            
            // Start Date Field
            OutlinedTextField(
                value = startDate,
                onValueChange = { 
                    startDate = it
                    dateError = validateDate(it, errorDateFormat, errorDateInvalid)
                },
                label = { Text(stringResource(R.string.start_date)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.start_date_placeholder)) },
                isError = dateError != null,
                supportingText = dateError?.let { { Text(it) } },
                shape = MaterialTheme.shapes.medium
            )
            
            // Notes Field (Optional)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = MaterialTheme.shapes.medium
            )
        }
        
        // Emoji Picker Dialog
        if (showEmojiPicker) {
            AlertDialog(
                onDismissRequest = { showEmojiPicker = false },
                title = {
                    Text(
                        text = stringResource(R.string.select_logo),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(emojiList) { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable {
                                        selectedEmoji = emoji
                                        showEmojiPicker = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    style = MaterialTheme.typography.displayMedium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showEmojiPicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

