package com.example.subscriptiontracker.data.fx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

@Serializable
private data class ErApiResponse(
    val result: String,
    val base_code: String,
    val rates: Map<String, Double>
)

object ErApiProvider : FxProvider {
    private const val API_URL = "https://open.er-api.com/v6/latest/"
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
            val apiResponse = json.decodeFromString<ErApiResponse>(response)
            
            if (apiResponse.result != "success" || apiResponse.base_code != base) {
                return@withContext null
            }
            
            val timestamp = System.currentTimeMillis() / 1000
            return@withContext FxRates(
                base = apiResponse.base_code,
                rates = apiResponse.rates,
                timestampEpochSec = timestamp
            )
        } catch (e: Exception) {
            return@withContext null
        }
    }
}

