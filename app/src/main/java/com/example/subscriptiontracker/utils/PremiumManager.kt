package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PremiumManager {
    private val IS_PREMIUM_KEY = booleanPreferencesKey("is_premium")
    
    fun isPremiumFlow(context: Context): Flow<Boolean> {
        return context.appDataStore.data.map { preferences ->
            preferences[IS_PREMIUM_KEY] ?: false
        }
    }
    
    suspend fun setPremium(context: Context, isPremium: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[IS_PREMIUM_KEY] = isPremium
        }
    }
}

