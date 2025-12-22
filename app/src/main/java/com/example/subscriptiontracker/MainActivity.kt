package com.example.subscriptiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.ui.theme.SubscriptionTrackerTheme

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
        enableEdgeToEdge()
        setContent {
            SubscriptionTrackerTheme {
                SubscriptionListScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListScreen() {
    var subscriptions by remember { mutableStateOf<List<Subscription>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var nextId by remember { mutableStateOf(1) }
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Aboneliklerim") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Ekle")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (subscriptions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Henüz abonelik eklenmedi")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subscriptions) { subscription ->
                    SubscriptionItem(subscription = subscription)
                }
            }
        }
        
        if (showDialog) {
            AddSubscriptionDialog(
                onDismiss = { showDialog = false },
                onSave = { subscription ->
                    subscriptions = subscriptions + subscription.copy(id = nextId)
                    nextId++
                    showDialog = false
                }
            )
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
                text = "Periyot: ${if (subscription.period == Period.MONTHLY) "Aylık" else "Yıllık"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Yenileme: ${subscription.renewalDate}",
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
        title = { Text("Yeni Abonelik Ekle") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("İsim") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Ücret") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Box {
                    OutlinedTextField(
                        value = if (selectedPeriod == Period.MONTHLY) "Aylık" else "Yıllık",
                        onValueChange = {},
                        label = { Text("Periyot") },
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
                            text = { Text("Aylık") },
                            onClick = {
                                selectedPeriod = Period.MONTHLY
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Yıllık") },
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
                    label = { Text("Yenileme Tarihi (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("2024-12-31") }
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
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}