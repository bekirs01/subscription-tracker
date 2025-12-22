package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object LocaleManager {
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    
    // Aktif dil: Sadece Türkçe
    val activeLanguage = "tr"
    
    // Tüm diller (UI'da gösterilmek için)
    val allLanguages = listOf(
        "tr" to true,  // Aktif
        "en" to false, // Pasif - Yakında
        "de" to false, // Pasif - Yakında
        "ru" to false  // Pasif - Yakında
    )
    
    fun getLanguageFlow(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            val savedLanguage = preferences[LANGUAGE_KEY] ?: activeLanguage
            // Sadece Türkçe aktif, her zaman Türkçe döndür
            activeLanguage
        }
    }
    
    suspend fun saveLanguage(context: Context, languageCode: String) {
        // Sadece Türkçe kaydedilebilir
        if (languageCode == activeLanguage) {
            context.dataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = activeLanguage
            }
        }
    }
    
    fun getLocale(languageCode: String): Locale {
        // Her zaman Türkçe döndür
        return Locale("tr")
    }
    
    fun isLanguageActive(languageCode: String): Boolean {
        return languageCode == activeLanguage
    }
}

