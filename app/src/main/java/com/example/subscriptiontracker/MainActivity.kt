package com.example.subscriptiontracker

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.navigation.compose.rememberNavController
import com.example.subscriptiontracker.navigation.NavGraph
import com.example.subscriptiontracker.ui.theme.SubscriptionTrackerTheme
import com.example.subscriptiontracker.utils.CurrencyManager
import com.example.subscriptiontracker.utils.LocaleManager
import com.example.subscriptiontracker.utils.PeriodManager
import kotlinx.coroutines.launch
import java.util.Locale

// Veri Modeli
data class Subscription(
    val id: Int,
    val name: String,
    val price: String,
    val period: Period,
    val renewalDate: String,
    val logoUrl: String? = null,
    val currency: String = "TRY"
)

data class PopularService(
    val id: String,
    val name: String,
    val logoUrl: String,
    val category: String
)

enum class Period {
    MONTHLY, YEARLY
}

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        // DataStore'dan dil kodunu oku
        val languageCode = LocaleManager.getLanguageSync(newBase)
        val locale = LocaleManager.getLocale(languageCode)
        
        // Locale'i ayarla
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        
        // Yeni context oluştur
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            AppContent()
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val languageFlow = remember(context) { LocaleManager.getLanguageFlow(context) }
    val currentLanguage by languageFlow.collectAsState(initial = LocaleManager.defaultLanguage)
    
            SubscriptionTrackerTheme {
        val navController = rememberNavController()
        NavGraph(
            navController = navController,
            onThemeChanged = {
                // Tema değişikliği anında uygulanır
            },
            onLanguageChanged = {
                // Dil değişikliği için Activity'yi kesin olarak yeniden başlat
                val activity = context as? ComponentActivity
                activity?.recreate()
            }
        )
    }
}

@Composable
fun SubscriptionItem(subscription: Subscription) {
    val context = LocalContext.current
    val currencyFlow = remember { CurrencyManager.getCurrencyFlow(context) }
    val currentCurrency by currencyFlow.collectAsState(initial = CurrencyManager.defaultCurrency)
    val currency = CurrencyManager.getCurrency(currentCurrency)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            if (!subscription.logoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = subscription.logoUrl,
                    contentDescription = subscription.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                // Placeholder
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subscription.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currency?.symbol ?: "₺"}${subscription.price}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (subscription.period == Period.MONTHLY) 
                                stringResource(R.string.monthly) 
                            else 
                                stringResource(R.string.yearly),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Text(
                    text = "${stringResource(R.string.renewal)}: ${subscription.renewalDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onSave: (Subscription) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currencyFlow = remember { CurrencyManager.getCurrencyFlow(context) }
    val currentCurrency by currencyFlow.collectAsState(initial = CurrencyManager.defaultCurrency)
    val currency = CurrencyManager.getCurrency(currentCurrency)
    
    // Varsayılan periyot DataStore'dan al
    val defaultPeriodFlow = remember { PeriodManager.getDefaultPeriodFlow(context) }
    val defaultPeriodString by defaultPeriodFlow.collectAsState(initial = PeriodManager.defaultPeriod)
    
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedPeriod by remember { 
        mutableStateOf(
            if (defaultPeriodString == "YEARLY") Period.YEARLY else Period.MONTHLY
        )
    }
    var renewalDate by remember { mutableStateOf("") }
    var periodExpanded by remember { mutableStateOf(false) }
    
    // Periyot seçildi mi kontrolü
    val isPeriodSelected = true // İlk açılışta varsayılan seçili
    
    // Validasyon state'leri
    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    
    // String resource'ları önceden al
    val errorNameInvalid = stringResource(R.string.error_name_invalid)
    val errorPriceInvalid = stringResource(R.string.error_price_invalid)
    val errorDateFormat = stringResource(R.string.error_date_format)
    val errorDateInvalid = stringResource(R.string.error_date_invalid)
    
    // Validasyon fonksiyonları
    fun validateName(input: String): String? {
        return when {
            input.isBlank() -> null // Boşken hata gösterme
            !input.matches(Regex("^[a-zA-ZçğıöşüÇĞIİÖŞÜ\\s]+$")) -> errorNameInvalid
            else -> null
        }
    }
    
    fun validatePrice(input: String): String? {
        return when {
            input.isBlank() -> null
            !input.matches(Regex("^\\d+(\\.\\d{1,2})?$")) -> errorPriceInvalid
            input.toDoubleOrNull() == null -> errorPriceInvalid
            else -> null
        }
    }
    
    fun validateDate(input: String): String? {
        return when {
            input.isBlank() -> null
            !input.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> errorDateFormat
            else -> {
                try {
                    val parts = input.split("-")
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    val day = parts[2].toInt()
                    if (month !in 1..12 || day !in 1..31 || year < 2020) {
                        errorDateInvalid
                    } else null
                } catch (e: Exception) {
                    errorDateInvalid
                }
            }
        }
    }
    
    // Form geçerliliği
    val isFormValid = name.isNotBlank() && 
                      price.isNotBlank() && 
                      renewalDate.isNotBlank() &&
                      nameError == null && 
                      priceError == null && 
                      dateError == null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = { 
            Text(
                text = stringResource(R.string.add_subscription_title),
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Periyot seçimi (İLK SIRADA)
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
                
                // İsim alanı (periyot seçildikten sonra aktif)
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = validateName(it)
                    },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = isPeriodSelected,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    shape = MaterialTheme.shapes.medium
                )
                
                // Ücret alanı (para birimi ile)
                OutlinedTextField(
                    value = price,
                    onValueChange = { 
                        price = it
                        priceError = validatePrice(it)
                    },
                    label = { Text(stringResource(R.string.price)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = isPeriodSelected,
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
                
                // Tarih alanı
                OutlinedTextField(
                    value = renewalDate,
                    onValueChange = { 
                        renewalDate = it
                        dateError = validateDate(it)
                    },
                    label = { Text(stringResource(R.string.renewal_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = isPeriodSelected,
                    placeholder = { Text(stringResource(R.string.renewal_date_placeholder)) },
                    isError = dateError != null,
                    supportingText = dateError?.let { { Text(it) } },
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isFormValid) {
                        scope.launch {
                            // Seçilen periyodu DataStore'a kaydet
                            PeriodManager.saveDefaultPeriod(
                                context, 
                                if (selectedPeriod == Period.MONTHLY) "MONTHLY" else "YEARLY"
                            )
                        }
                        onSave(
                            Subscription(
                                id = 0,
                                name = name.trim(),
                                price = price,
                                period = selectedPeriod,
                                renewalDate = renewalDate
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = isFormValid && isPeriodSelected,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.cancel))
    }
        }
    )
}