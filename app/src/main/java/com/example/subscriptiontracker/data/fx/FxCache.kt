package com.example.subscriptiontracker.data.fx

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.subscriptiontracker.utils.appDataStore

object FxCache {
    private val FX_BASE_KEY = stringPreferencesKey("fx_base")
    private val FX_JSON_KEY = stringPreferencesKey("fx_json")
    private val FX_SAVED_AT_KEY = stringPreferencesKey("fx_saved_at")
    private const val TTL_SECONDS = 12 * 60 * 60L
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(context: Context, base: String): FxRates? {
        return try {
            val prefs = context.appDataStore.data.first()
            val cachedBase = prefs[FX_BASE_KEY] ?: return null
            val cachedJson = prefs[FX_JSON_KEY] ?: return null
            val savedAt = prefs[FX_SAVED_AT_KEY]?.toLongOrNull() ?: return null
            
            if (cachedBase != base) {
                return null
            }
            
            val now = System.currentTimeMillis() / 1000
            if (now - savedAt > TTL_SECONDS) {
                return null
            }
            
            val rates = json.decodeFromString<Map<String, Double>>(cachedJson)
            FxRates(
                base = cachedBase,
                rates = rates,
                timestampEpochSec = savedAt
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun save(context: Context, fx: FxRates) {
        try {
            val ratesJson = json.encodeToString(fx.rates)
            context.appDataStore.edit { prefs ->
                prefs[FX_BASE_KEY] = fx.base
                prefs[FX_JSON_KEY] = ratesJson
                prefs[FX_SAVED_AT_KEY] = fx.timestampEpochSec.toString()
            }
        } catch (e: Exception) {
        }
    }
}

