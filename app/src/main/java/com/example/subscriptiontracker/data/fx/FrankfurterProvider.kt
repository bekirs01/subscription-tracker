package com.example.subscriptiontracker.data.fx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class FrankfurterResponse(
    val base: String,
    val rates: Map<String, Double>
)

object FrankfurterProvider : FxProvider {
    private const val API_URL = "https://api.frankfurter.app/latest?from="
    private const val TIMEOUT_MS = 6000L
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetch(base: String): FxRates? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$API_URL$base")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS.toInt()
            connection.readTimeout = TIMEOUT_MS.toInt()
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val apiResponse = json.decodeFromString<FrankfurterResponse>(response)
            
            if (apiResponse.base != base) {
                return@withContext null
            }
            
            val timestamp = System.currentTimeMillis() / 1000
            return@withContext FxRates(
                base = apiResponse.base,
                rates = apiResponse.rates,
                timestampEpochSec = timestamp
            )
        } catch (e: Exception) {
            return@withContext null
        }
    }
}

