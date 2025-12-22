package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PeriodManager {
    private val DEFAULT_PERIOD_KEY = stringPreferencesKey("default_period")
    
    const val defaultPeriod = "MONTHLY"
    
    fun getDefaultPeriodFlow(context: Context): Flow<String> {
        return context.appDataStore.data.map { preferences ->
            preferences[DEFAULT_PERIOD_KEY] ?: defaultPeriod
        }
    }
    
    suspend fun saveDefaultPeriod(context: Context, period: String) {
        context.appDataStore.edit { preferences ->
            preferences[DEFAULT_PERIOD_KEY] = period
        }
    }
}

