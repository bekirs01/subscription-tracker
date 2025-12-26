package com.example.subscriptiontracker.ui.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.Period
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.utils.PeriodManager
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

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

// Helper functions for Calendar-based date operations
private fun getTodayCalendar(): Calendar {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

private fun parseDateString(dateString: String): Calendar? {
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

private fun formatDateString(calendar: Calendar): String {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-based
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
}

private fun isDateBeforeToday(calendar: Calendar): Boolean {
    val today = getTodayCalendar()
    return calendar.before(today)
}

private fun getMaxDaysInMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month - 1) // Calendar months are 0-based
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

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
    
    val defaultPeriodFlow = remember { PeriodManager.getDefaultPeriodFlow(context) }
    val defaultPeriodString by defaultPeriodFlow.collectAsState(initial = PeriodManager.defaultPeriod)
    
    // Store original subscription snapshot for dirty checking (Edit mode only)
    val originalSubscription = remember(existingSubscription?.id) { existingSubscription }
    
    // Initialize fields based on existing subscription (edit mode) or predefined service/custom (new mode)
    var name by remember { mutableStateOf(existingSubscription?.name ?: predefinedService?.name ?: "") }
    var price by remember { mutableStateOf(existingSubscription?.price ?: "") }
    // Para birimi: Edit modunda mevcut aboneliğin para birimi, yeni abonelikte ayarlardan gelen para birimi
    var selectedCurrencyCode by remember(currentCurrency, existingSubscription?.id) { 
        mutableStateOf(existingSubscription?.currency ?: currentCurrency) 
    }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var selectedPeriod by remember { 
        mutableStateOf(
            existingSubscription?.period ?: if (defaultPeriodString == "YEARLY") Period.YEARLY else Period.MONTHLY
        )
    }
    var startDate by remember { mutableStateOf(existingSubscription?.renewalDate ?: "") }
    var notes by remember { mutableStateOf(existingSubscription?.notes ?: "") }
    var periodExpanded by remember { mutableStateOf(false) }
    val selectedLogoResId = remember { existingSubscription?.logoResId ?: predefinedService?.drawableResId }
    var selectedEmoji by remember { mutableStateOf<String?>(existingSubscription?.emoji) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Reminder selection (single choice: 7, 3, or 1 day) - Default: 7 days
    // Note: Reminder days are not stored in Subscription model, so we use a default of 7
    // For edit mode, we'll assume 7 days (since it's not persisted)
    var selectedReminderDays by remember { mutableIntStateOf(7) } // Default: 7 days before
    var reminderExpanded by remember { mutableStateOf(false) }
    
    
    // Validation states
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    
    // String resources
    val errorNameInvalid = stringResource(R.string.error_name_invalid)
    val errorPriceInvalid = stringResource(R.string.error_price_invalid)
    val errorDateFormat = stringResource(R.string.error_date_format)
    val errorDateInvalid = stringResource(R.string.error_date_invalid)
    val errorDatePast = stringResource(R.string.error_date_past)
    
    // Validation functions (must be defined before use)
    fun validateDate(input: String, formatError: String, invalidError: String, pastDateError: String): String? {
        return when {
            input.isBlank() -> null
            !input.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> formatError
            else -> {
                try {
                    val parsedDate = parseDateString(input)
                    if (parsedDate == null) {
                        invalidError
                    } else {
                        // Check if date is in the past
                        if (isDateBeforeToday(parsedDate)) {
                            pastDateError
                        } else {
                            val year = parsedDate.get(Calendar.YEAR)
                            val month = parsedDate.get(Calendar.MONTH) + 1
                            val day = parsedDate.get(Calendar.DAY_OF_MONTH)
                            if (month !in 1..12 || day !in 1..31 || year < 2020) {
                                invalidError
                            } else {
                                null
                            }
                        }
                    }
                } catch (e: Exception) {
                    invalidError
                }
            }
        }
    }
    
    // Compute date validation state (reactive to startDate changes)
    // MUST be defined before isFormValid
    val isDateValid = remember(startDate) {
        if (startDate.isBlank()) {
            false
        } else {
            val error = validateDate(startDate, errorDateFormat, errorDateInvalid, errorDatePast)
            error == null
        }
    }
    
    // Update dateError when startDate changes
    LaunchedEffect(startDate) {
        if (startDate.isNotBlank()) {
            dateError = validateDate(startDate, errorDateFormat, errorDateInvalid, errorDatePast)
        } else {
            dateError = null
        }
    }
    
    // Form validity
    val isFormValid = name.isNotBlank() && 
                      price.isNotBlank() && 
                      startDate.isNotBlank() &&
                      nameError == null && 
                      priceError == null && 
                      dateError == null &&
                      isDateValid && // Date must be today or future
                      (predefinedService != null || selectedEmoji != null) // Emoji required for custom
    
    // Determine if we're in Edit mode
    val isEditMode = originalSubscription != null
    
    // Save button enablement logic
    // CRITICAL: In Edit mode, Save button is ALWAYS enabled (user can save even without changes)
    // In Add mode, Save button is enabled only if form is valid
    val isSaveButtonEnabled = if (isEditMode) {
        // Edit mode: Always enabled (no dirty check, no validation requirement)
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                        enabled = isSaveButtonEnabled, // Edit mode: always true, Add mode: isFormValid
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
                                val emoji = selectedEmoji ?: existingSubscription.emoji
                                if (emoji != null) {
                                    Text(
                                        text = emoji,
                                        style = MaterialTheme.typography.displayLarge,
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
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
                        val isDarkTheme = isSystemInDarkTheme()
                        when {
                            // Dark mode: use logoUrlLight with white colorFilter to make it white
                            // Light mode: use logoUrlLight as is (original dark/colored version)
                            !predefinedService.logoUrlLight.isNullOrEmpty() -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(predefinedService.logoUrlLight)
                                        .decoderFactory(SvgDecoder.Factory())
                                        .crossfade(true)
                                        .allowHardware(false)
                                        .build(),
                                    contentDescription = predefinedService.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = if (isDarkTheme) {
                                        ColorFilter.tint(Color.White)
                                    } else {
                                        null
                                    }
                                )
                            }
                            predefinedService.drawableResId != null -> {
                                Image(
                                    painter = painterResource(id = predefinedService.drawableResId),
                                    contentDescription = predefinedService.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            else -> {
                                // Default icon fallback
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = predefinedService.name,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                        val emoji = selectedEmoji
                        if (emoji != null) {
                            Text(
                                text = emoji,
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
                    val defaultCurrency = CurrencyManager.getCurrency(CurrencyManager.defaultCurrency)
                    FilterChip(
                        selected = false,
                        onClick = { showCurrencyPicker = true },
                        label = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCurrency?.flag ?: defaultCurrency?.flag ?: "🇹🇷",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = selectedCurrency?.code ?: defaultCurrency?.code ?: CurrencyManager.defaultCurrency,
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
        
        // Modern Date Picker Modal - Centered
        if (showDatePicker) {
            ModernDatePickerModal(
                initialDate = startDate,
                onDateSelected = { newDate ->
                    startDate = newDate
                    dateError = validateDate(newDate, errorDateFormat, errorDateInvalid, errorDatePast)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
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
fun ModernDatePickerModal(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val today = remember { getTodayCalendar() }
    val currentYear = remember { today.get(Calendar.YEAR) }
    val currentMonth = remember { today.get(Calendar.MONTH) + 1 }
    val currentDay = remember { today.get(Calendar.DAY_OF_MONTH) }
    
    // Initialize picker state from initialDate or today
    val initialCalendar = remember(initialDate) {
        parseDateString(initialDate) ?: today
    }
    
    var selectedYear by remember { mutableIntStateOf(initialCalendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(initialCalendar.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableIntStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH)) }
    
    // Recalculate when month/year changes to adjust day if needed
    LaunchedEffect(selectedYear, selectedMonth) {
        val maxDay = getMaxDaysInMonth(selectedYear, selectedMonth)
        if (selectedDay > maxDay) {
            selectedDay = maxDay
        }
    }
    
    // Validate selected date
    val selectedDateCalendar = remember(selectedYear, selectedMonth, selectedDay) {
        try {
            Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth - 1)
                set(Calendar.DAY_OF_MONTH, selectedDay)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    val isDateValid = remember(selectedDateCalendar, today) {
        selectedDateCalendar != null && !isDateBeforeToday(selectedDateCalendar)
    }
    
    // Backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        // Centered Modal Card
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.start_date),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                }
                
                // Date Picker Wheel
                DatePickerWheelContent(
                    selectedYear = selectedYear,
                    selectedMonth = selectedMonth,
                    selectedDay = selectedDay,
                    currentYear = currentYear,
                    currentMonth = currentMonth,
                    currentDay = currentDay,
                    onYearChanged = { selectedYear = it },
                    onMonthChanged = { selectedMonth = it },
                    onDayChanged = { selectedDay = it }
                )
                
                // Error message if date is invalid
                if (!isDateValid && selectedDateCalendar != null) {
                    Text(
                        text = stringResource(R.string.error_date_past),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Bottom buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            if (isDateValid && selectedDateCalendar != null) {
                                val formattedDate = formatDateString(selectedDateCalendar)
                                onDateSelected(formattedDate)
                            }
                        },
                        enabled = isDateValid,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
fun DatePickerWheelContent(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    currentYear: Int,
    currentMonth: Int,
    currentDay: Int,
    onYearChanged: (Int) -> Unit,
    onMonthChanged: (Int) -> Unit,
    onDayChanged: (Int) -> Unit
) {
    // Only show current year and future years
    val years = remember(currentYear) { (currentYear..currentYear + 10).toList() }
    
    // Only show current month and future months if year is current year
    val months = remember(selectedYear, currentYear, currentMonth) {
        if (selectedYear == currentYear) {
            (currentMonth..12).toList()
        } else {
            (1..12).toList()
        }
    }
    
    // Calculate max days for selected month/year
    val maxDay = remember(selectedYear, selectedMonth) { 
        getMaxDaysInMonth(selectedYear, selectedMonth)
    }
    val days = remember(selectedYear, selectedMonth, selectedDay) { (1..maxDay).toList() }
    
    // Month names for display
    val monthNames = remember {
        arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Year Picker
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Year",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(years.size) { index ->
                        val year = years[index]
                        val isSelected = year == selectedYear
                        val isPastYear = year < currentYear
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isPastYear) { 
                                    if (!isPastYear) onYearChanged(year) 
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = year.toString(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Text(
                                    text = year.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal,
                                    color = when {
                                        isPastYear -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(months.size) { index ->
                        val month = months[index]
                        val isSelected = month == selectedMonth
                        val isPastMonth = selectedYear == currentYear && month < currentMonth
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isPastMonth) { 
                                    if (!isPastMonth) onMonthChanged(month) 
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = monthNames[month - 1],
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Text(
                                    text = monthNames[month - 1],
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal,
                                    color = when {
                                        isPastMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(days.size) { index ->
                        val day = days[index]
                        val isSelected = day == selectedDay
                        val isPastDay = selectedYear == currentYear && 
                                      selectedMonth == currentMonth && 
                                      day < currentDay
                        val isValidDay = day <= maxDay
                        val isDisabled = isPastDay || !isValidDay
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isDisabled) { 
                                    if (!isDisabled) onDayChanged(day) 
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = day.toString().padStart(2, '0'),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Text(
                                    text = day.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Normal,
                                    color = when {
                                        isDisabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
