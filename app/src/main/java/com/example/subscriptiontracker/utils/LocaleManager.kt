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
    
    // Dünyada en çok kullanılan 30 dil
    val supportedLanguages = listOf(
        Language("tr", "Türkçe", "🇹🇷"),
        Language("en", "English", "🇺🇸"),
        Language("de", "Deutsch", "🇩🇪"),
        Language("ru", "Русский", "🇷🇺"),
        Language("fr", "Français", "🇫🇷"),
        Language("es", "Español", "🇪🇸"),
        Language("pt", "Português", "🇧🇷"),
        Language("it", "Italiano", "🇮🇹"),
        Language("ar", "العربية", "🇸🇦"),
        Language("zh", "中文", "🇨🇳"),
        Language("ja", "日本語", "🇯🇵"),
        Language("ko", "한국어", "🇰🇷"),
        Language("hi", "हिन्दी", "🇮🇳"),
        Language("nl", "Nederlands", "🇳🇱"),
        Language("pl", "Polski", "🇵🇱"),
        Language("sv", "Svenska", "🇸🇪"),
        Language("no", "Norsk", "🇳🇴"),
        Language("da", "Dansk", "🇩🇰"),
        Language("fi", "Suomi", "🇫🇮"),
        Language("cs", "Čeština", "🇨🇿"),
        Language("hu", "Magyar", "🇭🇺"),
        Language("ro", "Română", "🇷🇴"),
        Language("el", "Ελληνικά", "🇬🇷"),
        Language("he", "עברית", "🇮🇱"),
        Language("th", "ไทย", "🇹🇭"),
        Language("vi", "Tiếng Việt", "🇻🇳"),
        Language("id", "Bahasa Indonesia", "🇮🇩"),
        Language("ms", "Bahasa Melayu", "🇲🇾"),
        Language("uk", "Українська", "🇺🇦")
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

