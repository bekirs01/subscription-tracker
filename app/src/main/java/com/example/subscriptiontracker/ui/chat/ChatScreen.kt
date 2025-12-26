package com.example.subscriptiontracker.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.R
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.utils.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    onAddSubscription: ((Subscription) -> Unit)? = null,
    onThemeChanged: (() -> Unit)? = null,
    onLanguageChanged: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var pendingAction by remember { mutableStateOf<ActionType?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var confirmationMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    // İlk mesaj için string resource'u al
    val welcomeMessage = stringResource(R.string.ai_welcome_message)
    val cancelledMessage = stringResource(R.string.cancelled_anything_else)
    
    // İlk mesaj
    LaunchedEffect(Unit) {
        messages = listOf(
            ChatMessage(
                text = welcomeMessage,
                isUser = false
            )
        )
    }
    
    // Yeni mesaj geldiğinde en alta kaydır
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    // Onay dialog'u
    if (showConfirmationDialog && pendingAction != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmationDialog = false
                pendingAction = null
            },
            title = {
                Text(
                    text = "Onay",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(confirmationMessage)
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val action = pendingAction!!
                            val result = ActionExecutor.executeAction(
                                context = context,
                                action = action,
                                onAddSubscription = onAddSubscription,
                                onThemeChanged = onThemeChanged,
                                onLanguageChanged = onLanguageChanged
                            )
                            
                            messages = messages + ChatMessage(
                                text = result,
                                isUser = false
                            )
                            
                            showConfirmationDialog = false
                            pendingAction = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        messages = messages + ChatMessage(
                            text = cancelledMessage,
                            isUser = false
                        )
                        showConfirmationDialog = false
                        pendingAction = null
                    }
                ) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ai_assistant_title),
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
            }
            
            // Input area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.ai_ask_question)) },
                        shape = MaterialTheme.shapes.large,
                        maxLines = 3
                    )
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val userMessage = inputText.trim()
                                
                                // Kullanıcı mesajı ekle
                                messages = messages + ChatMessage(
                                    text = userMessage,
                                    isUser = true
                                )
                                
                                scope.launch {
                                    // Önce action parse et
                                    val action = ActionParser.parseAction(userMessage)
                                    
                                    if (action != ActionType.None) {
                                        // Action bulundu, onay iste
                                        pendingAction = action
                                        confirmationMessage = ActionExecutor.getConfirmationMessage(action)
                                        showConfirmationDialog = true
                                        
                                        // Onay mesajını göster
                                        messages = messages + ChatMessage(
                                            text = confirmationMessage,
                                            isUser = false
                                        )
                                    } else {
                                        // Normal AI cevabı veya hava durumu
                                        val aiResponse = if (isWeatherQuestion(userMessage)) {
                                            // Hava durumu sorusu
                                            val cityName = extractCityName(userMessage)
                                            WeatherService.getWeather(context, cityName)
                                        } else {
                                            // Normal AI cevabı
                                            AIAssistantService.getResponse(context, userMessage)
                                        }
                                        
                                        messages = messages + ChatMessage(
                                            text = aiResponse,
                                            isUser = false
                                        )
                                    }
                                }
                                
                                inputText = ""
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = if (inputText.isNotBlank()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = stringResource(R.string.send),
                            tint = if (inputText.isNotBlank()) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!message.isUser) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "🤖",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.ai_assistant),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

// Helper functions
private fun isWeatherQuestion(message: String): Boolean {
    val lowerMessage = message.lowercase()
    return lowerMessage.contains("hava") || 
           lowerMessage.contains("weather") || 
           lowerMessage.contains("hava durumu")
}

private fun extractCityName(message: String): String? {
    val lowerMessage = message.lowercase()
    val cities = listOf("istanbul", "ankara", "izmir", "antalya", "bursa", 
                       "london", "paris", "berlin", "new york", "tokyo")
    
    for (city in cities) {
        if (lowerMessage.contains(city)) {
            return city
        }
    }
    
    return null
}
