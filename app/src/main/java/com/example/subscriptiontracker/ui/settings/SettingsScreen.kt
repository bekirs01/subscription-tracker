package com.example.subscriptiontracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.size
import com.example.subscriptiontracker.R
import android.Manifest
import android.os.Build
import androidx.compose.material.icons.filled.Notifications
import com.example.subscriptiontracker.utils.AppTheme
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.utils.LocaleManager
import com.example.subscriptiontracker.utils.NotificationManager
import com.example.subscriptiontracker.utils.PremiumManager
import com.example.subscriptiontracker.utils.ReminderManager
import com.example.subscriptiontracker.utils.ThemeManager
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemeChanged: () -> Unit = {},
    onLanguageChanged: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State'ler
    val themeFlow = remember(context) { ThemeManager.getThemeFlow(context) }
    val currentTheme by themeFlow.collectAsState(initial = AppTheme.SYSTEM)
    val languageFlow = remember(context) { LocaleManager.getLanguageFlow(context) }
    val currentLanguage by languageFlow.collectAsState(initial = LocaleManager.defaultLanguage)
    val currencyFlow = remember(context) { CurrencyManager.getCurrencyFlow(context) }
    val currentCurrency by currencyFlow.collectAsState(initial = CurrencyManager.defaultCurrency)
    val notificationsFlow = remember(context) { NotificationManager.getNotificationsEnabledFlow(context) }
    val notificationsEnabled by notificationsFlow.collectAsState(initial = false)
    val reminderFlow = remember(context) { ReminderManager.getReminderDaysFlow(context) }
    val currentReminderDays by reminderFlow.collectAsState(initial = ReminderManager.defaultReminderDays)
    val premiumFlow = remember(context) { PremiumManager.isPremiumFlow(context) }
    val isPremium by premiumFlow.collectAsState(initial = false)
    
    var themeExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var reminderExpanded by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    
    // Bildirim izni launcher (Android 13+)
    val notificationPermissionLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            scope.launch {
                if (isGranted) {
                    NotificationManager.saveNotificationsEnabled(context, true)
                } else {
                    // İzin reddedildi, switch'i kapat
                    NotificationManager.saveNotificationsEnabled(context, false)
                }
            }
        }
    } else {
        null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Premium Banner (EN ÜSTTE) - Sadece premium değilse göster
            if (!isPremium) {
                item {
                    PremiumBannerCard(
                        onClick = { onNavigateToPremium() },
                        isPremium = isPremium
                    )
                }
            }
            
            // Görünüm Bölümü
            item {
                SettingsSection(title = stringResource(R.string.section_appearance)) {
                    // Tema Seçimi
                    SettingItem(
                        label = stringResource(R.string.theme),
                        value = when (currentTheme) {
                            AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                            AppTheme.LIGHT -> stringResource(R.string.theme_light)
                            AppTheme.DARK -> stringResource(R.string.theme_dark)
                        },
                        onClick = { themeExpanded = true }
                    ) {
                        DropdownMenu(
                            expanded = themeExpanded,
                            onDismissRequest = { themeExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.theme_system)) },
                                onClick = {
                                    scope.launch {
                                        ThemeManager.saveTheme(context, AppTheme.SYSTEM)
                                        themeExpanded = false
                                        onThemeChanged()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.theme_light)) },
                                onClick = {
                                    scope.launch {
                                        ThemeManager.saveTheme(context, AppTheme.LIGHT)
                                        themeExpanded = false
                                        onThemeChanged()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.theme_dark)) },
                                onClick = {
                                    scope.launch {
                                        ThemeManager.saveTheme(context, AppTheme.DARK)
                                        themeExpanded = false
                                        onThemeChanged()
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Dil Bölümü
            item {
                SettingsSection(title = stringResource(R.string.section_language)) {
                    val currentLang = LocaleManager.getLanguage(currentLanguage)
                    SettingItem(
                        label = stringResource(R.string.language),
                        value = "${currentLang?.flag ?: "🇹🇷"} ${currentLang?.name ?: "Turkish"}",
                        onClick = { languageExpanded = true }
                    ) {
                        DropdownMenu(
                            expanded = languageExpanded,
                            onDismissRequest = { languageExpanded = false }
                        ) {
                            LocaleManager.supportedLanguages.forEach { language ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = language.flag,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            Text(
                                                text = language.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    },
                                    onClick = {
                                        scope.launch {
                                            LocaleManager.saveLanguage(context, language.code)
                                            languageExpanded = false
                                            onLanguageChanged()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Para Birimi Bölümü
            item {
                SettingsSection(title = stringResource(R.string.section_currency)) {
                    val currentCurr = CurrencyManager.getCurrency(currentCurrency)
                    SettingItem(
                        label = stringResource(R.string.currency),
                        value = "${currentCurr?.flag ?: "₺"} ${currentCurr?.symbol ?: "₺"} ${currentCurr?.name ?: "Turkish Lira"}",
                        onClick = { currencyExpanded = true }
                    ) {
                        DropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            CurrencyManager.supportedCurrencies.forEach { currency ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = currency.flag,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${currency.symbol} ${currency.name}",
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = currency.code,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        scope.launch {
                                            CurrencyManager.saveCurrency(context, currency.code)
                                            currencyExpanded = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Bildirimler Bölümü
            item {
                SettingsSection(title = stringResource(R.string.section_notifications)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.notifications),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.notifications_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    if (enabled) {
                                        // Android 13+ için izin iste
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            // Android 13 altı için izin gerekmez
                                            NotificationManager.saveNotificationsEnabled(context, true)
                                        }
                                    } else {
                                        NotificationManager.saveNotificationsEnabled(context, false)
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            // Billing Cycle Reminder Bölümü
            item {
                SettingsSection(title = stringResource(R.string.section_billing_reminder)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentReminder = ReminderManager.getReminderOption(currentReminderDays)
                            ?: ReminderManager.reminderOptions.first()
                        
                        val reminderLabel = when (currentReminder.days) {
                            7 -> stringResource(R.string.billing_reminder_7_days)
                            3 -> stringResource(R.string.billing_reminder_3_days)
                            1 -> stringResource(R.string.billing_reminder_1_day)
                            else -> currentReminder.label
                        }
                        
                        OutlinedTextField(
                            value = "$reminderLabel ${if (currentReminder.isPremium) "(${stringResource(R.string.premium_label)})" else "(${stringResource(R.string.free_label)})"}",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.section_billing_reminder)) },
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
                            ReminderManager.reminderOptions.forEach { option ->
                                val optionLabel = when (option.days) {
                                    7 -> stringResource(R.string.billing_reminder_7_days)
                                    3 -> stringResource(R.string.billing_reminder_3_days)
                                    1 -> stringResource(R.string.billing_reminder_1_day)
                                    else -> option.label
                                }
                                
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = optionLabel,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = if (option.isPremium) 
                                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                    else 
                                                        MaterialTheme.colorScheme.onSurface
                                                )
                                                if (option.isPremium) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (option.isPremium) stringResource(R.string.premium_label) else stringResource(R.string.free_label),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (option.isPremium) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (option.isPremium && !isPremium) {
                                            // Premium özellik ama kullanıcı premium değil - Premium ekranına yönlendir
                                            reminderExpanded = false
                                            onNavigateToPremium()
                                        } else if (option.isPremium && isPremium) {
                                            // Premium özellik ve kullanıcı premium - kaydet
                                            scope.launch {
                                                ReminderManager.saveReminderDays(context, option.days)
                                                reminderExpanded = false
                                            }
                                        } else {
                                            // Free özellik - kaydet
                                            scope.launch {
                                                ReminderManager.saveReminderDays(context, option.days)
                                                reminderExpanded = false
                                            }
                                        }
                                    },
                                    enabled = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Premium Feature Dialog
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            shape = MaterialTheme.shapes.extraLarge,
            title = {
                Text(
                    text = stringResource(R.string.premium_feature_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.premium_feature_description),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showPremiumDialog = false
                        // TODO: Premium upgrade akışı
                    },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(R.string.upgrade_to_premium))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPremiumDialog = false },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
fun SettingItem(
    label: String,
    value: String,
    onClick: () -> Unit,
    dropdownContent: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            readOnly = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            trailingIcon = {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }
        )
        dropdownContent()
    }
}

@Composable
fun PremiumBannerCard(
    onClick: () -> Unit,
    isPremium: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    
    val gradientColors = if (isDark) {
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = MaterialTheme.shapes.large
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFFD700) else Color(0xFFFFA500),
                        modifier = Modifier.size(32.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isPremium) stringResource(R.string.premium_active_title) else stringResource(R.string.upgrade_to_premium),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPremium) stringResource(R.string.premium_active_subtitle) else stringResource(R.string.premium_banner_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
