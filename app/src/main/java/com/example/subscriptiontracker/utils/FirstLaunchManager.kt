package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object FirstLaunchManager {
    private val HAS_SEEN_PREMIUM_GATE_KEY = booleanPreferencesKey("has_seen_premium_gate")
    
    fun hasSeenPremiumGateFlow(context: Context): Flow<Boolean> {
        return context.appDataStore.data.map { preferences ->
            preferences[HAS_SEEN_PREMIUM_GATE_KEY] ?: false
        }
    }
    
    suspend fun setPremiumGateSeen(context: Context) {
        context.appDataStore.edit { preferences ->
            preferences[HAS_SEEN_PREMIUM_GATE_KEY] = true
        }
    }
}

