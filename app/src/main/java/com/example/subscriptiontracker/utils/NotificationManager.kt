package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object NotificationManager {
    private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    
    fun getNotificationsEnabledFlow(context: Context): Flow<Boolean> {
        return context.appDataStore.data.map { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] ?: false
        }
    }
    
    suspend fun saveNotificationsEnabled(context: Context, enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
}

