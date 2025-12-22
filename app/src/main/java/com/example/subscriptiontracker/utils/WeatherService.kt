package com.example.subscriptiontracker.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject

object WeatherService {
    
    /**
     * Hava durumu bilgisi alır
     */
    suspend fun getWeather(context: Context, cityName: String? = null): String = withContext(Dispatchers.IO) {
        try {
            val (latitude, longitude) = if (cityName != null) {
                getCoordinatesForCity(cityName) ?: return@withContext "Şehir bulunamadı. Lütfen şehir adını belirtin."
            } else {
                getCurrentLocation(context) ?: return@withContext "Konum bilgisi alınamadı. Lütfen şehir adını belirtin (örn: 'İstanbul hava durumu')."
            }
            
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val currentWeather = json.optJSONObject("current_weather")
            
            if (currentWeather == null) {
                return@withContext "Hava durumu bilgisi alınamadı."
            }
            
            val temp = currentWeather.optDouble("temperature", Double.NaN)
            val code = currentWeather.optInt("weathercode", 0)
            
            if (temp.isNaN()) {
                return@withContext "Hava durumu bilgisi alınamadı."
            }
            
            val description = getWeatherDescription(code)
            val tempCelsius = temp.toInt()
            
            "🌤️ Hava durumu: $description, $tempCelsius°C"
        } catch (e: Exception) {
            "Hava durumu bilgisi alınamadı. Lütfen şehir adını belirtin (örn: 'İstanbul hava durumu')."
        }
    }
    
    private fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        // Basit konum - gerçek uygulamada LocationManager kullanılabilir
        // Şimdilik null döndürüyoruz, kullanıcıdan şehir isteyeceğiz
        return null
    }
    
    private fun getCoordinatesForCity(cityName: String): Pair<Double, Double>? {
        // Popüler şehirlerin koordinatları
        val cities = mapOf(
            "istanbul" to Pair(41.0082, 28.9784),
            "ankara" to Pair(39.9334, 32.8597),
            "izmir" to Pair(38.4237, 27.1428),
            "antalya" to Pair(36.8969, 30.7133),
            "bursa" to Pair(40.1826, 29.0665),
            "london" to Pair(51.5074, -0.1278),
            "paris" to Pair(48.8566, 2.3522),
            "berlin" to Pair(52.5200, 13.4050),
            "new york" to Pair(40.7128, -74.0060),
            "tokyo" to Pair(35.6762, 139.6503)
        )
        
        val lowerCity = cityName.lowercase()
        return cities[lowerCity] ?: cities.entries.find { lowerCity.contains(it.key) }?.value
    }
    
    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            in 0..1 -> "Açık"
            in 2..3 -> "Parçalı bulutlu"
            in 45..48 -> "Sisli"
            in 51..67 -> "Yağmurlu"
            in 71..77 -> "Karlı"
            in 80..82 -> "Sağanak yağışlı"
            in 85..86 -> "Kar fırtınası"
            in 95..99 -> "Fırtınalı"
            else -> "Bilinmeyen"
        }
    }
}

