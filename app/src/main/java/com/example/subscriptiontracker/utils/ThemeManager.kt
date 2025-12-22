package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

object ThemeManager {
    private val THEME_KEY = stringPreferencesKey("theme")
    
    fun getThemeFlow(context: Context): Flow<AppTheme> {
        return context.appDataStore.data.map { preferences ->
            val themeString = preferences[THEME_KEY] ?: "system"
            when (themeString) {
                "light" -> AppTheme.LIGHT
                "dark" -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }
        }
    }
    
    suspend fun saveTheme(context: Context, theme: AppTheme) {
        context.appDataStore.edit { preferences ->
            preferences[THEME_KEY] = when (theme) {
                AppTheme.LIGHT -> "light"
                AppTheme.DARK -> "dark"
                AppTheme.SYSTEM -> "system"
            }
        }
    }
}

