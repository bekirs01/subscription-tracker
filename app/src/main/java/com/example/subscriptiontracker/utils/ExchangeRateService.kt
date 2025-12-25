package com.example.subscriptiontracker.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject

object ExchangeRateService {
    
    // Ücretsiz API: exchangerate-api.com
    private const val API_URL = "https://api.exchangerate-api.com/v4/latest/"
    
    // Cache için (son alınan kurlar)
    private var cachedRates: Map<String, Double>? = null
    private var cacheTimestamp: Long = 0
    private const val CACHE_DURATION_MS = 60 * 60 * 1000 // 1 saat
    
    /**
     * Base currency'ye göre güncel döviz kurlarını alır
     * @param baseCurrency Base currency kodu (örn: "TRY", "USD", "EUR")
     * @return Map<CurrencyCode, Rate> veya null (hata durumunda)
     */
    suspend fun getExchangeRates(baseCurrency: String): Map<String, Double>? = withContext(Dispatchers.IO) {
        try {
            // Cache kontrolü
            val now = System.currentTimeMillis()
            if (cachedRates != null && (now - cacheTimestamp) < CACHE_DURATION_MS) {
                return@withContext cachedRates
            }
            
            val url = "$API_URL$baseCurrency"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val ratesJson = json.optJSONObject("rates")
            
            if (ratesJson == null) {
                return@withContext null
            }
            
            val rates = mutableMapOf<String, Double>()
            ratesJson.keys().forEach { currencyCode ->
                val rate = ratesJson.optDouble(currencyCode, Double.NaN)
                if (!rate.isNaN()) {
                    rates[currencyCode] = rate
                }
            }
            
            // Base currency'nin kendisi için 1.0 ekle
            rates[baseCurrency] = 1.0
            
            // Cache'e kaydet
            cachedRates = rates
            cacheTimestamp = now
            
            return@withContext rates
        } catch (e: Exception) {
            // Hata durumunda cache varsa onu kullan
            return@withContext cachedRates
        }
    }
    
    /**
     * Belirli bir para birimini base currency'ye dönüştürür
     * @param amount Dönüştürülecek tutar
     * @param fromCurrency Kaynak para birimi
     * @param toCurrency Hedef para birimi (base currency)
     * @param rates Exchange rate map'i
     * @return Dönüştürülmüş tutar veya null (kur bulunamazsa)
     */
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
        
        // Önce USD'ye çevir (eğer base currency USD değilse)
        // Sonra base currency'ye çevir
        // Formül: amount * (toRate / fromRate)
        return amount * (toRate / fromRate)
    }
    
    /**
     * Cache'i temizle (test veya force refresh için)
     */
    fun clearCache() {
        cachedRates = null
        cacheTimestamp = 0
    }
}

