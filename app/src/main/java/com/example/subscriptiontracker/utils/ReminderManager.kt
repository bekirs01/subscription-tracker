package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ReminderOption(
    val days: Int,
    val isPremium: Boolean,
    val label: String
)

object ReminderManager {
    private val REMINDER_DAYS_KEY = intPreferencesKey("reminder_days")
    
    val defaultReminderDays = 7
    
    val reminderOptions = listOf(
        ReminderOption(7, false, "7 days before"),
        ReminderOption(3, true, "3 days before"),
        ReminderOption(1, true, "1 day before")
    )
    
    fun getReminderDaysFlow(context: Context): Flow<Int> {
        return context.appDataStore.data.map { preferences ->
            preferences[REMINDER_DAYS_KEY] ?: defaultReminderDays
        }
    }
    
    suspend fun saveReminderDays(context: Context, days: Int) {
        context.appDataStore.edit { preferences ->
            preferences[REMINDER_DAYS_KEY] = days
        }
    }
    
    fun getReminderOption(days: Int): ReminderOption? {
        return reminderOptions.find { it.days == days }
    }
}

