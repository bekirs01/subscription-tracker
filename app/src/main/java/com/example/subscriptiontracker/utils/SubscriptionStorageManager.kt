package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.subscriptiontracker.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Subscription storage manager using DataStore
 * Persists subscriptions to survive app restarts
 */
object SubscriptionStorageManager {
    private val SUBSCRIPTIONS_KEY = stringPreferencesKey("subscriptions")
    private val NEXT_ID_KEY = intPreferencesKey("next_subscription_id")
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Load subscriptions from DataStore
     */
    fun getSubscriptionsFlow(context: Context): Flow<List<Subscription>> {
        return context.appDataStore.data.map { preferences ->
            try {
                val subscriptionsJson = preferences[SUBSCRIPTIONS_KEY]
                if (subscriptionsJson.isNullOrBlank()) {
                    emptyList()
                } else {
                    json.decodeFromString<List<SubscriptionDto>>(subscriptionsJson)
                        .map { it.toSubscription() }
                }
            } catch (e: Exception) {
                // On error, return empty list
                emptyList()
            }
        }
    }
    
    /**
     * Load subscriptions synchronously (for initial load)
     */
    suspend fun getSubscriptions(context: Context): List<Subscription> {
        return try {
            val preferences = context.appDataStore.data.first()
            val subscriptionsJson = preferences[SUBSCRIPTIONS_KEY]
            if (subscriptionsJson.isNullOrBlank()) {
                emptyList()
            } else {
                json.decodeFromString<List<SubscriptionDto>>(subscriptionsJson)
                    .map { it.toSubscription() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Save subscriptions to DataStore
     */
    suspend fun saveSubscriptions(context: Context, subscriptions: List<Subscription>) {
        try {
            val subscriptionsDto = subscriptions.map { SubscriptionDto.fromSubscription(it) }
            val subscriptionsJson = json.encodeToString(subscriptionsDto)
            context.appDataStore.edit { preferences ->
                preferences[SUBSCRIPTIONS_KEY] = subscriptionsJson
            }
        } catch (e: Exception) {
            // Ignore errors - don't crash
        }
    }
    
    /**
     * Get next ID from DataStore
     */
    suspend fun getNextId(context: Context): Int {
        return try {
            val preferences = context.appDataStore.data.first()
            preferences[NEXT_ID_KEY] ?: 1
        } catch (e: Exception) {
            1
        }
    }
    
    /**
     * Save next ID to DataStore
     */
    suspend fun saveNextId(context: Context, nextId: Int) {
        try {
            context.appDataStore.edit { preferences ->
                preferences[NEXT_ID_KEY] = nextId
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * DTO for serialization (Period enum needs special handling)
     */
    @Serializable
    private data class SubscriptionDto(
        val id: Int,
        val name: String,
        val price: String,
        val period: String, // "MONTHLY" or "YEARLY"
        val renewalDate: String,
        val logoUrl: String? = null,
        val logoResId: Int? = null,
        val emoji: String? = null,
        val currency: String = "TRY",
        val notes: String = ""
    ) {
        fun toSubscription(): Subscription {
            val periodEnum = when (period) {
                "MONTHLY" -> com.example.subscriptiontracker.Period.MONTHLY
                "YEARLY" -> com.example.subscriptiontracker.Period.YEARLY
                else -> com.example.subscriptiontracker.Period.MONTHLY
            }
            return Subscription(
                id = id,
                name = name,
                price = price,
                period = periodEnum,
                renewalDate = renewalDate,
                logoUrl = logoUrl,
                logoResId = logoResId,
                emoji = emoji,
                currency = currency,
                notes = notes
            )
        }
        
        companion object {
            fun fromSubscription(subscription: Subscription): SubscriptionDto {
                return SubscriptionDto(
                    id = subscription.id,
                    name = subscription.name,
                    price = subscription.price,
                    period = when (subscription.period) {
                        com.example.subscriptiontracker.Period.MONTHLY -> "MONTHLY"
                        com.example.subscriptiontracker.Period.YEARLY -> "YEARLY"
                    },
                    renewalDate = subscription.renewalDate,
                    logoUrl = subscription.logoUrl,
                    logoResId = subscription.logoResId,
                    emoji = subscription.emoji,
                    currency = subscription.currency,
                    notes = subscription.notes
                )
            }
        }
    }
}

