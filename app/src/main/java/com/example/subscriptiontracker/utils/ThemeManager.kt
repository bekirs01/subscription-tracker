package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

object ThemeManager {
    private val THEME_KEY = stringPreferencesKey("theme")
    
    fun getThemeFlow(context: Context): Flow<AppTheme> {
        return context.dataStore.data.map { preferences ->
            val themeString = preferences[THEME_KEY] ?: "system"
            when (themeString) {
                "light" -> AppTheme.LIGHT
                "dark" -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }
        }
    }
    
    suspend fun saveTheme(context: Context, theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = when (theme) {
                AppTheme.LIGHT -> "light"
                AppTheme.DARK -> "dark"
                AppTheme.SYSTEM -> "system"
            }
        }
    }
}

