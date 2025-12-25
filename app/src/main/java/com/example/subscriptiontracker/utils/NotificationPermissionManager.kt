package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object NotificationPermissionManager {
    private val NOTIFICATION_PERMISSION_ASKED_KEY = booleanPreferencesKey("notification_permission_asked")
    
    fun hasAskedPermissionFlow(context: Context): Flow<Boolean> {
        return context.appDataStore.data.map { preferences ->
            preferences[NOTIFICATION_PERMISSION_ASKED_KEY] ?: false
        }
    }
    
    suspend fun hasAskedPermission(context: Context): Boolean {
        return context.appDataStore.data.first()[NOTIFICATION_PERMISSION_ASKED_KEY] ?: false
    }
    
    suspend fun setPermissionAsked(context: Context) {
        context.appDataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_ASKED_KEY] = true
        }
    }
}

