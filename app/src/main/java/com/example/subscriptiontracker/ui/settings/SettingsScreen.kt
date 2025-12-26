package com.example.subscriptiontracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.AssistChip

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onThemeChanged: () -> Unit = {},
    onLanguageChanged: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {}
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
    // Premium state - Flow'dan gelen değeri dinle
    val premiumFlow = remember(context) { PremiumManager.isPremiumFlow(context) }
    val premiumFromFlow by premiumFlow.collectAsState(initial = false)
    var isPremium by rememberSaveable { mutableStateOf(premiumFromFlow) }
    
    // Premium durumu değiştiğinde state'i güncelle
    LaunchedEffect(premiumFromFlow) {
        isPremium = premiumFromFlow
    }
    
    // Developer Mode: Premium Test Switch (sadece UI state)
    var developerPremiumTest by rememberSaveable { mutableStateOf(false) }
    // Developer switch açıksa premium aktif sayılır
    val effectivePremium = isPremium || developerPremiumTest
    
    // Premium Reminder Settings State
    var selectedReminderDays by rememberSaveable { mutableStateOf(mutableSetOf(3)) } // Varsayılan: 3 gün seçili
    var reminderTime by rememberSaveable { mutableStateOf(Pair(9, 0)) } // Varsayılan: 09:00
    var multiReminderEnabled by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddDayDialog by remember { mutableStateOf(false) }
    var customReminderDays by rememberSaveable { mutableStateOf(mutableSetOf<Int>()) }
    
    // TimePicker state
    val timePickerState = rememberTimePickerState(
        initialHour = reminderTime.first,
        initialMinute = reminderTime.second,
        is24Hour = true
    )
    
    // Tüm seçili günleri birleştir (senkronizasyon için)
    val allSelectedDays = remember(selectedReminderDays, customReminderDays) {
        (selectedReminderDays + customReminderDays).toSet()
    }
    
    var themeExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
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
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
            TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            windowInsets = WindowInsets(0.dp),
                title = { 
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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
            
            // Billing Cycle Reminder Bölümü - Yeni Tasarım
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Başlık ve Açıklama
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Faturalandırma Hatırlatıcısı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Abonelik yenilenmeden önce bildirim al",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // ÜCRETSİZ BÖLÜM - Sabit bilgilendirici satır
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "7 gün kala hatırlat",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Bu özellik ücretsizdir",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Developer Mode: Premium Test Switch
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Developer Mode: Premium Test",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = developerPremiumTest,
                                onCheckedChange = { developerPremiumTest = it }
                            )
                        }
                    }
                    
                    // PREMIUM BÖLÜM
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = if (!effectivePremium) {
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        } else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Premium Başlık
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Premium Hatırlatıcı Ayarları",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Premium Seçenekler
                            if (effectivePremium) {
                                // 3 gün kala hatırlat - Checkbox
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
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "3 gün kala hatırlat",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (3 in selectedReminderDays) FontWeight.Medium else FontWeight.Normal
                                        )
                                    }
                                    Checkbox(
                                        checked = 3 in selectedReminderDays,
                                        onCheckedChange = {
                                            if (it) {
                                                selectedReminderDays.add(3)
                                            } else {
                                                selectedReminderDays.remove(3)
                                            }
                                        }
                                    )
                                }
                                
                                // 1 gün kala hatırlat - Checkbox
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
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "1 gün kala hatırlat",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (1 in selectedReminderDays) FontWeight.Medium else FontWeight.Normal
                                        )
                                    }
                                    Checkbox(
                                        checked = 1 in selectedReminderDays,
                                        onCheckedChange = {
                                            if (it) {
                                                selectedReminderDays.add(1)
                                            } else {
                                                selectedReminderDays.remove(1)
                                            }
                                        }
                                    )
                                }
                                
                                // Bildirim saati seç - TimePicker
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showTimePicker = true }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Bildirim saati",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = String.format("%02d:%02d", reminderTime.first, reminderTime.second),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                
                                // Birden fazla hatırlatma al - Switch
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
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Birden fazla hatırlatma al",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Switch(
                                        checked = multiReminderEnabled,
                                        onCheckedChange = { multiReminderEnabled = it }
                                    )
                                }
                                
                                // Ek hatırlatmalar alanı (sadece switch AÇIKKEN)
                                AnimatedVisibility(visible = multiReminderEnabled) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Başlık ve Açıklama
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Ek hatırlatmalar (Premium)",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Birden fazla günü işaretleyebilir, ek gün ekleyebilirsiniz.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        // Checkbox listesi: 1, 2, 3, 5, 7 gün
                                        val standardDays = listOf(1, 2, 3, 5, 7)
                                        standardDays.forEach { day ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "$day gün kala hatırlat",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Checkbox(
                                                    checked = day in allSelectedDays,
                                                    onCheckedChange = {
                                                        if (it) {
                                                            if (day in listOf(1, 3)) {
                                                                selectedReminderDays.add(day)
                                                            } else {
                                                                customReminderDays.add(day)
                                                            }
                                                        } else {
                                                            selectedReminderDays.remove(day)
                                                            customReminderDays.remove(day)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                        
                                        // "+ Gün ekle" butonu
                                        Button(
                                            onClick = { showAddDayDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Gün ekle")
                                        }
                                        
                                        // Seçili günlerin listesi (Chip'ler)
                                        if (allSelectedDays.isNotEmpty()) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Seçili günler:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                FlowRow(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    allSelectedDays.forEach { day ->
                                                        AssistChip(
                                                            onClick = {
                                                                selectedReminderDays.remove(day)
                                                                customReminderDays.remove(day)
                                                            },
                                                            label = {
                                                                Row(
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text("$day gün")
                                                                    Icon(
                                                                        imageVector = Icons.Default.Close,
                                                                        contentDescription = "Sil",
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Premium kapalıyken: Kilitli görünüm (mevcut davranış)
                                PremiumReminderOption(
                                    label = "3 gün kala hatırlat",
                                    isEnabled = false,
                                    isSelected = false,
                                    onClick = { onNavigateToPremium() }
                                )
                                
                                PremiumReminderOption(
                                    label = "1 gün kala hatırlat",
                                    isEnabled = false,
                                    isSelected = false,
                                    onClick = { onNavigateToPremium() }
                                )
                                
                                PremiumReminderOption(
                                    label = "Bildirim saati seç",
                                    isEnabled = false,
                                    isSelected = false,
                                    onClick = { onNavigateToPremium() }
                                )
                                
                                PremiumReminderOption(
                                    label = "Birden fazla hatırlatma al",
                                    isEnabled = false,
                                    isSelected = false,
                                    onClick = { onNavigateToPremium() }
                                )
                            }
                        }
                    }
                    
                    // Premium CTA
                    if (!effectivePremium) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Bildirimleri istediğin gün ve saatte al",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = onNavigateToPremium,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = "Premium'a Yükselt",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // TimePicker Dialog (Premium aktifken)
    if (showTimePicker && effectivePremium) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Bildirim Saati Seç") },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialSelectedContentColor = MaterialTheme.colorScheme.primary,
                        clockDialColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderTime = Pair(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("İptal")
                }
            }
        )
    }
    
    // "+ Gün ekle" Dialog
    if (showAddDayDialog && effectivePremium) {
        var selectedDay by remember { mutableStateOf(1) }
        val allSelectedDaysForDialog = remember(selectedReminderDays, customReminderDays) {
            (selectedReminderDays + customReminderDays).toSet()
        }
        AlertDialog(
            onDismissRequest = { showAddDayDialog = false },
            title = { Text("Gün Ekle") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("1-30 arası bir gün seçin:")
                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(30) { index ->
                            val day = index + 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDay = day }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$day gün",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (selectedDay == day) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedDay !in allSelectedDaysForDialog) {
                            customReminderDays.add(selectedDay)
                        }
                        showAddDayDialog = false
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDayDialog = false }) {
                    Text("İptal")
                }
            }
        )
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
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumReminderOption(
    label: String,
    isEnabled: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected && isEnabled) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isEnabled) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isEnabled) 
                    MaterialTheme.colorScheme.onSurface 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
        if (isSelected && isEnabled) {
            Switch(
                checked = true,
                onCheckedChange = { onClick() },
                enabled = true
            )
        }
    }
}
