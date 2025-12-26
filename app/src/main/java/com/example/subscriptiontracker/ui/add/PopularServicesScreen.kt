package com.example.subscriptiontracker.ui.add

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.subscriptiontracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularServicesScreen(
    onNavigateBack: () -> Unit,
    onServiceSelected: (ServiceItem) -> Unit,
    onCustomSelected: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter services by search query
    val filteredServices = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            services
        } else {
            services.filter { service ->
                service.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val showCustomButton = searchQuery.isNotBlank() && filteredServices.isEmpty()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_subscription_title),
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
                },
                actions = {
                    TextButton(onClick = onCustomSelected) {
                        Text(stringResource(R.string.custom))
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_subscriptions)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            // Show custom button if no search results
            if (showCustomButton) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onCustomSelected,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.create_custom_subscription))
                    }
                }
            }
            
            // Service List - one per row
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredServices) { service ->
                    ServiceCard(
                        service = service,
                        onClick = { onServiceSelected(service) }
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    service: ServiceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
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
            // Logo - Use AsyncImage for URL, fallback to drawable or default icon
            val isDarkTheme = isSystemInDarkTheme()
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isDarkTheme) {
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                                    RoundedCornerShape(12.dp)
                                )
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    spotColor = Color.White.copy(alpha = 0.1f)
                                )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !service.logoUrl.isNullOrEmpty() -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(service.logoUrl)
                                .decoderFactory(SvgDecoder.Factory())
                                .crossfade(true)
                                .allowHardware(false)
                                .build(),
                            contentDescription = service.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    service.drawableResId != null -> {
                        Image(
                            painter = painterResource(id = service.drawableResId),
                            contentDescription = service.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        // Default icon fallback
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = service.name,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Service name
            Text(
                text = service.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Arrow indicator
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
