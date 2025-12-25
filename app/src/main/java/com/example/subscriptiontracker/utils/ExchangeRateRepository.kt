package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object ExchangeRateRepository {
    private const val API_URL = "https://open.er-api.com/v6/latest/"
    private val CACHED_RATES_KEY = stringPreferencesKey("cached_exchange_rates")
    private val CACHE_TIMESTAMP_KEY = stringPreferencesKey("cache_timestamp")
    private const val CACHE_DURATION_MS = 60 * 60 * 1000L

    suspend fun getExchangeRates(context: Context, baseCurrency: String): Map<String, Double>? = withContext(Dispatchers.IO) {
        try {
            val cached = loadCachedRates(context, baseCurrency)
            val now = System.currentTimeMillis()
            
            if (cached != null && (now - cached.second) < CACHE_DURATION_MS) {
                return@withContext cached.first
            }

            val rates = fetchOnlineRates(baseCurrency)
            if (rates != null) {
                saveCachedRates(context, baseCurrency, rates, now)
                return@withContext rates
            }

            return@withContext cached?.first
        } catch (e: Exception) {
            val cached = loadCachedRates(context, baseCurrency)
            return@withContext cached?.first
        }
    }

    private suspend fun fetchOnlineRates(baseCurrency: String): Map<String, Double>? = withContext(Dispatchers.IO) {
        try {
            val url = "$API_URL$baseCurrency"
            val response = URL(url).readText()
            val json = JSONObject(response)
            
            if (json.optString("result") != "success") {
                return@withContext null
            }
            
            val ratesJson = json.optJSONObject("rates") ?: return@withContext null
            val rates = mutableMapOf<String, Double>()
            
            ratesJson.keys().forEach { currencyCode ->
                val rate = ratesJson.optDouble(currencyCode, Double.NaN)
                if (!rate.isNaN()) {
                    rates[currencyCode] = rate
                }
            }
            
            rates[baseCurrency] = 1.0
            return@withContext rates
        } catch (e: Exception) {
            return@withContext null
        }
    }

    private suspend fun loadCachedRates(context: Context, baseCurrency: String): Pair<Map<String, Double>, Long>? {
        return try {
            val prefs = context.appDataStore.data.first()
            val cachedData = prefs[CACHED_RATES_KEY] ?: return null
            val timestamp = prefs[CACHE_TIMESTAMP_KEY]?.toLongOrNull() ?: return null
            
            val json = JSONObject(cachedData)
            if (json.optString("base") != baseCurrency) {
                return null
            }
            
            val ratesJson = json.optJSONObject("rates") ?: return null
            val rates = mutableMapOf<String, Double>()
            
            ratesJson.keys().forEach { currencyCode ->
                val rate = ratesJson.optDouble(currencyCode, Double.NaN)
                if (!rate.isNaN()) {
                    rates[currencyCode] = rate
                }
            }
            
            rates[baseCurrency] = 1.0
            Pair(rates, timestamp)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveCachedRates(context: Context, baseCurrency: String, rates: Map<String, Double>, timestamp: Long) {
        try {
            val ratesJson = JSONObject()
            rates.forEach { (code, rate) ->
                ratesJson.put(code, rate)
            }
            
            val cacheData = JSONObject().apply {
                put("base", baseCurrency)
                put("rates", ratesJson)
            }
            
            context.appDataStore.edit { prefs ->
                prefs[CACHED_RATES_KEY] = cacheData.toString()
                prefs[CACHE_TIMESTAMP_KEY] = timestamp.toString()
            }
        } catch (e: Exception) {
        }
    }

    fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        rates: Map<String, Double>?
    ): Double? {
        if (rates == null) return null
        if (fromCurrency == toCurrency) return amount
        
        val fromRate = rates[fromCurrency] ?: return null
        val toRate = rates[toCurrency] ?: return null
        
        return amount * (toRate / fromRate)
    }
}

