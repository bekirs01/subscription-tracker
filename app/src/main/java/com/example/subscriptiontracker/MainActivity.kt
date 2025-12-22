package com.example.subscriptiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.navigation.NavGraph
import com.example.subscriptiontracker.ui.theme.SubscriptionTrackerTheme
import com.example.subscriptiontracker.utils.LocaleManager

// Veri Modeli
data class Subscription(
    val id: Int,
    val name: String,
    val price: String,
    val period: Period,
    val renewalDate: String
)

enum class Period {
    MONTHLY, YEARLY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dil tercihini uygula (varsayılan Türkçe)
        try {
            val locale = LocaleManager.getLocale("tr")
            val config = resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        } catch (e: Exception) {
            // Hata durumunda varsayılan devam et
        }
        
        enableEdgeToEdge()
        setContent {
            SubscriptionTrackerTheme {
                val navController = rememberNavController()
                var themeChanged by remember { mutableIntStateOf(0) }
                
                NavGraph(
                    navController = navController,
                    onThemeChanged = {
                        themeChanged++
                        // Activity'yi yeniden başlat
                        recreate()
                    }
                )
            }
        }
    }
}

@Composable
fun SubscriptionItem(subscription: Subscription) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${subscription.price} TL",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${stringResource(R.string.period)}: ${if (subscription.period == Period.MONTHLY) stringResource(R.string.monthly) else stringResource(R.string.yearly)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(R.string.renewal)}: ${subscription.renewalDate}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onSave: (Subscription) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(Period.MONTHLY) }
    var renewalDate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_subscription_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text(stringResource(R.string.price)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Box {
                    OutlinedTextField(
                        value = if (selectedPeriod == Period.MONTHLY) stringResource(R.string.monthly) else stringResource(R.string.yearly),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.period)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.monthly)) },
                            onClick = {
                                selectedPeriod = Period.MONTHLY
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.yearly)) },
                            onClick = {
                                selectedPeriod = Period.YEARLY
                                expanded = false
                            }
                        )
                    }
                }
                
                OutlinedTextField(
                    value = renewalDate,
                    onValueChange = { renewalDate = it },
                    label = { Text(stringResource(R.string.renewal_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.renewal_date_placeholder)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && price.isNotBlank() && renewalDate.isNotBlank()) {
                        onSave(
                            Subscription(
                                id = 0, // ID parent'ta atanacak
                                name = name,
                                price = price,
                                period = selectedPeriod,
                                renewalDate = renewalDate
                            )
                        )
                        name = ""
                        price = ""
                        selectedPeriod = Period.MONTHLY
                        renewalDate = ""
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}