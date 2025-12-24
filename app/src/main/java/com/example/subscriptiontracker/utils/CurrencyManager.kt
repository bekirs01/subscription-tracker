package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

data class Currency(
    val code: String,
    val symbol: String,
    val name: String,
    val flag: String
)

object CurrencyManager {
    private val CURRENCY_KEY = stringPreferencesKey("currency")
    
    // Varsayılan para birimi: TL
    const val defaultCurrency = "TRY"
    
    // Dünyada en çok kullanılan para birimleri
    val supportedCurrencies = listOf(
        Currency("TRY", "₺", "Turkish Lira", "🇹🇷"),
        Currency("USD", "$", "US Dollar", "🇺🇸"),
        Currency("EUR", "€", "Euro", "🇪🇺"),
        Currency("GBP", "£", "British Pound", "🇬🇧"),
        Currency("JPY", "¥", "Japanese Yen", "🇯🇵"),
        Currency("CNY", "¥", "Chinese Yuan", "🇨🇳"),
        Currency("KRW", "₩", "South Korean Won", "🇰🇷"),
        Currency("RUB", "₽", "Russian Ruble", "🇷🇺"),
        Currency("INR", "₹", "Indian Rupee", "🇮🇳"),
        Currency("CAD", "C$", "Canadian Dollar", "🇨🇦"),
        Currency("AUD", "A$", "Australian Dollar", "🇦🇺"),
        Currency("CHF", "CHF", "Swiss Franc", "🇨🇭"),
        Currency("SEK", "kr", "Swedish Krona", "🇸🇪"),
        Currency("NOK", "kr", "Norwegian Krone", "🇳🇴"),
        Currency("DKK", "kr", "Danish Krone", "🇩🇰"),
        Currency("PLN", "zł", "Polish Zloty", "🇵🇱"),
        Currency("BRL", "R$", "Brazilian Real", "🇧🇷"),
        Currency("MXN", "$", "Mexican Peso", "🇲🇽"),
        Currency("ZAR", "R", "South African Rand", "🇿🇦"),
        Currency("SAR", "﷼", "Saudi Riyal", "🇸🇦"),
        Currency("AED", "د.إ", "UAE Dirham", "🇦🇪"),
        Currency("HKD", "HK$", "Hong Kong Dollar", "🇭🇰"),
        Currency("SGD", "S$", "Singapore Dollar", "🇸🇬"),
        Currency("NZD", "NZ$", "New Zealand Dollar", "🇳🇿"),
        Currency("THB", "฿", "Thai Baht", "🇹🇭"),
        Currency("IDR", "Rp", "Indonesian Rupiah", "🇮🇩"),
        Currency("MYR", "RM", "Malaysian Ringgit", "🇲🇾"),
        Currency("PHP", "₱", "Philippine Peso", "🇵🇭"),
        Currency("ILS", "₪", "Israeli Shekel", "🇮🇱"),
        Currency("CLP", "$", "Chilean Peso", "🇨🇱")
    )
    
    fun getCurrencyFlow(context: Context): Flow<String> {
        return context.appDataStore.data.map { preferences ->
            preferences[CURRENCY_KEY] ?: defaultCurrency
        }
    }
    
    fun getCurrencySync(context: Context): String {
        return runBlocking {
            context.appDataStore.data.first()[CURRENCY_KEY] ?: defaultCurrency
        }
    }
    
    suspend fun saveCurrency(context: Context, currencyCode: String) {
        if (supportedCurrencies.any { it.code == currencyCode }) {
            context.appDataStore.edit { preferences ->
                preferences[CURRENCY_KEY] = currencyCode
            }
        }
    }
    
    fun getCurrency(currencyCode: String): Currency? {
        return supportedCurrencies.find { it.code == currencyCode }
            ?: supportedCurrencies.find { it.code == defaultCurrency }
    }
    
    fun getAllCurrencies(): List<Currency> {
        return supportedCurrencies
    }
}

