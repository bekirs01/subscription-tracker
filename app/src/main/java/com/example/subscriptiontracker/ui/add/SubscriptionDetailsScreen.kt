package com.example.subscriptiontracker.ui.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.Period
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.utils.PeriodManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.YearMonth
import java.util.Calendar

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
    onSave: (Subscription) -> Unit,
    onNavigateToPremium: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currencyFlow = remember { CurrencyManager.getCurrencyFlow(context) }
    val currentCurrency by currencyFlow.collectAsState(initial = CurrencyManager.defaultCurrency)
    val currency = CurrencyManager.getCurrency(currentCurrency)
    
    val defaultPeriodFlow = remember { PeriodManager.getDefaultPeriodFlow(context) }
    val defaultPeriodString by defaultPeriodFlow.collectAsState(initial = PeriodManager.defaultPeriod)
    
    // Store original subscription snapshot for dirty checking (Edit mode only)
    val originalSubscription = remember(existingSubscription?.id) { existingSubscription }
    
    // Initialize fields based on existing subscription (edit mode) or predefined service/custom (new mode)
    var name by remember { mutableStateOf(existingSubscription?.name ?: predefinedService?.name ?: "") }
    var price by remember { mutableStateOf(existingSubscription?.price ?: "") }
    var selectedCurrencyCode by remember { mutableStateOf(existingSubscription?.currency ?: currentCurrency) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
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
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Reminder selection (single choice: 7, 3, or 1 day) - Default: 7 days
    // Note: Reminder days are not stored in Subscription model, so we use a default of 7
    // For edit mode, we'll assume 7 days (since it's not persisted)
    var selectedReminderDays by remember { mutableStateOf<Int>(7) } // Default: 7 days before
    var reminderExpanded by remember { mutableStateOf(false) }
    
    // Parse current date or use today
    val currentDate = remember(startDate) {
        try {
            if (startDate.isNotBlank() && startDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                val parts = startDate.split("-")
                LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } else {
                LocalDate.now()
            }
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
    
    // Initialize date picker state from currentDate, and update when startDate changes
    var selectedYear by remember(startDate) { mutableIntStateOf(currentDate.year) }
    var selectedMonth by remember(startDate) { mutableIntStateOf(currentDate.monthValue) }
    var selectedDay by remember(startDate) { mutableIntStateOf(currentDate.dayOfMonth) }
    
    // Update date picker state when startDate changes externally
    LaunchedEffect(startDate) {
        val date = try {
            if (startDate.isNotBlank() && startDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                val parts = startDate.split("-")
                LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } else {
                LocalDate.now()
            }
        } catch (e: Exception) {
            LocalDate.now()
        }
        selectedYear = date.year
        selectedMonth = date.monthValue
        selectedDay = date.dayOfMonth
    }
    
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
    
    // Determine if we're in Edit mode
    val isEditMode = originalSubscription != null
    
    // Save button enablement logic
    // CRITICAL: In Edit mode, Save button is ALWAYS enabled (override dirty check)
    // In Add mode, use form validation
    val isSaveButtonEnabled = if (isEditMode) {
        // Edit mode: Always enabled (ignore dirty check completely)
        true
    } else {
        // Add mode: Enable only if form is valid
        isFormValid
    }
    
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
                            // In Edit mode, always allow save (even if unchanged)
                            // In Add mode, only save if form is valid
                            if (isEditMode || isFormValid) {
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
                                        currency = selectedCurrencyCode,
                                        notes = notes.trim()
                                    )
                                )
                            }
                        },
                        enabled = isSaveButtonEnabled,
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
            
            // Price Section: Amount + Currency
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.price),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Amount Input (Left)
                    OutlinedTextField(
                        value = price,
                        onValueChange = { 
                            price = it
                            priceError = validatePrice(it, errorPriceInvalid)
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = priceError != null,
                        supportingText = priceError?.let { { Text(it) } },
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        placeholder = { Text("0.00") }
                    )
                    
                    // Currency Selector (Right)
                    val selectedCurrency = CurrencyManager.getCurrency(selectedCurrencyCode)
                    FilterChip(
                        selected = false,
                        onClick = { showCurrencyPicker = true },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCurrency?.flag ?: "🇹🇷",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = selectedCurrency?.code ?: "TRY",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
            
            // Start Date Field
            OutlinedTextField(
                value = startDate,
                onValueChange = { },
                label = { Text(stringResource(R.string.start_date)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        // Initialize picker with current date
                        val date = try {
                            if (startDate.isNotBlank() && startDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                                val parts = startDate.split("-")
                                LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                            } else {
                                LocalDate.now()
                            }
                        } catch (e: Exception) {
                            LocalDate.now()
                        }
                        selectedYear = date.year
                        selectedMonth = date.monthValue
                        selectedDay = date.dayOfMonth
                        showDatePicker = true 
                    },
                readOnly = true,
                singleLine = true,
                placeholder = { Text(stringResource(R.string.start_date_placeholder)) },
                isError = dateError != null,
                supportingText = dateError?.let { { Text(it) } },
                shape = MaterialTheme.shapes.medium,
                trailingIcon = {
                    IconButton(onClick = { 
                        val date = try {
                            if (startDate.isNotBlank() && startDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                                val parts = startDate.split("-")
                                LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                            } else {
                                LocalDate.now()
                            }
                        } catch (e: Exception) {
                            LocalDate.now()
                        }
                        selectedYear = date.year
                        selectedMonth = date.monthValue
                        selectedDay = date.dayOfMonth
                        showDatePicker = true 
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }
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
            
            // Reminder Selection (like Period selector)
            Box {
                OutlinedTextField(
                    value = when (selectedReminderDays) {
                        7 -> "7 days before"
                        3 -> "3 days before"
                        1 -> "1 day before"
                        else -> "7 days before"
                    },
                    onValueChange = {},
                    label = { Text("Reminder") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { reminderExpanded = true },
                    readOnly = true,
                    shape = MaterialTheme.shapes.medium,
                    trailingIcon = {
                        IconButton(onClick = { reminderExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = reminderExpanded,
                    onDismissRequest = { reminderExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { 
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("7 days before")
                                if (selectedReminderDays == 7) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            selectedReminderDays = 7
                            reminderExpanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (selectedReminderDays == 7) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("3 days before")
                                    Text(
                                        text = "🔒 Premium",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (selectedReminderDays == 3) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            selectedReminderDays = 3
                            reminderExpanded = false
                            // Navigate to Premium when 3 days reminder is selected
                            onNavigateToPremium()
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (selectedReminderDays == 3) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("1 day before")
                                    Text(
                                        text = "🔒 Premium",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (selectedReminderDays == 1) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            selectedReminderDays = 1
                            reminderExpanded = false
                            // Navigate to Premium when 1 day reminder is selected
                            onNavigateToPremium()
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = if (selectedReminderDays == 1) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    )
                }
            }
        }
        
        // Currency Picker Dialog
        if (showCurrencyPicker) {
            AlertDialog(
                onDismissRequest = { showCurrencyPicker = false },
                title = {
                    Text(
                        text = stringResource(R.string.currency),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(CurrencyManager.getAllCurrencies()) { currency ->
                            val isSelected = selectedCurrencyCode == currency.code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCurrencyCode = currency.code
                                        showCurrencyPicker = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currency.flag,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Column {
                                        Text(
                                            text = currency.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        Text(
                                            text = "${currency.code} (${currency.symbol})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCurrencyPicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            AlertDialog(
                onDismissRequest = { showDatePicker = false },
                title = {
                    Text(
                        text = stringResource(R.string.start_date),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    DatePickerWheel(
                        selectedYear = selectedYear,
                        selectedMonth = selectedMonth,
                        selectedDay = selectedDay,
                        onYearChanged = { selectedYear = it },
                        onMonthChanged = { 
                            selectedMonth = it
                            // Adjust day if needed (e.g., Feb 30 -> Feb 28)
                            val maxDay = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
                            if (selectedDay > maxDay) {
                                selectedDay = maxDay
                            }
                        },
                        onDayChanged = { selectedDay = it }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                val date = LocalDate.of(selectedYear, selectedMonth, selectedDay)
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val newStartDate = date.format(formatter)
                                // Update state explicitly to trigger recomposition
                                startDate = newStartDate
                                dateError = validateDate(newStartDate, errorDateFormat, errorDateInvalid)
                                showDatePicker = false
                            } catch (e: Exception) {
                                dateError = errorDateInvalid
                            }
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
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

@Composable
fun DatePickerWheel(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onYearChanged: (Int) -> Unit,
    onMonthChanged: (Int) -> Unit,
    onDayChanged: (Int) -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (2020..currentYear + 10).toList()
    val months = (1..12).toList()
    
    // Calculate max days for selected month/year
    val maxDay = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
    val days = remember(selectedYear, selectedMonth) { (1..maxDay).toList() }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Year Picker
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Year",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(years.size) { index ->
                        val year = years[index]
                        val isSelected = year == selectedYear
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onYearChanged(year) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = year.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Month Picker
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Month",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(months.size) { index ->
                        val month = months[index]
                        val isSelected = month == selectedMonth
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMonthChanged(month) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = month.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Day Picker
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Day",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(days.size) { index ->
                        val day = days[index]
                        val isSelected = day == selectedDay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDayChanged(day) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

