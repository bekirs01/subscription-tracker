package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale

data class Language(
    val code: String,
    val name: String,
    val flag: String
)

object LocaleManager {
    private val LANGUAGE_KEY = stringPreferencesKey("language")
    
    // Varsayılan dil: Türkçe
    const val defaultLanguage = "tr"
    
    // Çalışan diller (sadece strings.xml dosyaları olanlar)
    // Dil isimleri HER ZAMAN İngilizce gösterilir
    val supportedLanguages = listOf(
        Language("tr", "Turkish", "🇹🇷"),
        Language("en", "English", "🇺🇸"),
        Language("de", "German", "🇩🇪"),
        Language("ru", "Russian", "🇷🇺"),
        Language("fr", "French", "🇫🇷"),
        Language("es", "Spanish", "🇪🇸"),
        Language("it", "Italian", "🇮🇹"),
        Language("pt", "Portuguese", "🇧🇷"),
        Language("ar", "Arabic", "🇸🇦"),
        Language("zh", "Chinese", "🇨🇳")
    )
    
    fun getLanguageFlow(context: Context): Flow<String> {
        return context.appDataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: defaultLanguage
        }
    }
    
    // Sync okuma (attachBaseContext için)
    fun getLanguageSync(context: Context): String {
        return runBlocking {
            context.appDataStore.data.first()[LANGUAGE_KEY] ?: defaultLanguage
        }
    }
    
    suspend fun saveLanguage(context: Context, languageCode: String) {
        if (supportedLanguages.any { it.code == languageCode }) {
            context.appDataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = languageCode
            }
        }
    }
    
    fun getLocale(languageCode: String): Locale {
        val language = supportedLanguages.find { it.code == languageCode }
        return if (language != null) {
            Locale(languageCode)
        } else {
            Locale(defaultLanguage) // Varsayılan Türkçe
        }
    }
    
    fun getLanguage(languageCode: String): Language? {
        return supportedLanguages.find { it.code == languageCode }
            ?: supportedLanguages.find { it.code == defaultLanguage }
    }
}

