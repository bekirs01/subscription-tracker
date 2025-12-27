package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Reminder settings for a subscription
 */
data class ReminderSettings(
    val days: Set<Int>, // e.g., {1, 3, 7}
    val hour: Int, // 0-23
    val minute: Int // 0-59
)

object ReminderSettingsManager {
    private val REMINDER_DAYS_KEY = stringPreferencesKey("reminder_days")
    private val REMINDER_HOUR_KEY = intPreferencesKey("reminder_hour")
    private val REMINDER_MINUTE_KEY = intPreferencesKey("reminder_minute")
    
    // Default settings: 7 days before at 09:00
    val defaultSettings = ReminderSettings(
        days = setOf(7),
        hour = 9,
        minute = 0
    )
    
    /**
     * Serialize Set<Int> to String (comma-separated)
     */
    private fun serializeDays(days: Set<Int>): String {
        return days.sorted().joinToString(",")
    }
    
    /**
     * Deserialize String to Set<Int>
     */
    private fun deserializeDays(daysString: String?): Set<Int> {
        return if (daysString.isNullOrBlank()) {
            defaultSettings.days
        } else {
            try {
                daysString.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            } catch (e: Exception) {
                defaultSettings.days
            }
        }
    }
    
    /**
     * Get reminder settings flow for a subscription
     * For now, we use global settings (can be extended to per-subscription later)
     */
    fun getReminderSettingsFlow(context: Context): Flow<ReminderSettings> {
        return context.appDataStore.data.map { preferences ->
            val daysString = preferences[REMINDER_DAYS_KEY]
            val days = deserializeDays(daysString)
            val hour = preferences[REMINDER_HOUR_KEY] ?: defaultSettings.hour
            val minute = preferences[REMINDER_MINUTE_KEY] ?: defaultSettings.minute
            ReminderSettings(days, hour, minute)
        }
    }
    
    /**
     * Save reminder settings
     */
    suspend fun saveReminderSettings(
        context: Context,
        days: Set<Int>,
        hour: Int,
        minute: Int
    ) {
        context.appDataStore.edit { preferences ->
            preferences[REMINDER_DAYS_KEY] = serializeDays(days)
            preferences[REMINDER_HOUR_KEY] = hour
            preferences[REMINDER_MINUTE_KEY] = minute
        }
    }
    
    /**
     * Get reminder settings synchronously (for immediate use)
     */
    suspend fun getReminderSettings(context: Context): ReminderSettings {
        val preferences = context.appDataStore.data.first()
        val daysString = preferences[REMINDER_DAYS_KEY]
        val days = deserializeDays(daysString)
        val hour = preferences[REMINDER_HOUR_KEY] ?: defaultSettings.hour
        val minute = preferences[REMINDER_MINUTE_KEY] ?: defaultSettings.minute
        return ReminderSettings(days, hour, minute)
    }
}

