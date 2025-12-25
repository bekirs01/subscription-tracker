package com.example.subscriptiontracker.data.fx

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FxState {
    object Loading : FxState()
    data class Ready(val fx: FxRates, val fromCache: Boolean) : FxState()
    data class Unavailable(val reason: String, val hasCache: Boolean) : FxState()
}

object ExchangeRateRepository {
    // Primary: open.er-api.com, fallback: frankfurter.app
    private val providers = listOf<FxProvider>(
        ErApiProvider,
        FrankfurterProvider
    )
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun ratesFlow(context: Context, base: String): StateFlow<FxState> {
        val stateFlow = MutableStateFlow<FxState>(FxState.Loading)
        
        scope.launch {
            try {
                val cached = FxCache.load(context, base)
                if (cached != null) {
                    stateFlow.value = FxState.Ready(cached, fromCache = true)
                }
                
                var fetched: FxRates? = null
                for (provider in providers) {
                    try {
                        fetched = provider.fetch(base)
                        if (fetched != null) {
                            break
                        }
                    } catch (e: Exception) {
                        continue
                    }
                }
                
                if (fetched != null) {
                    FxCache.save(context, fetched)
                    stateFlow.value = FxState.Ready(fetched, fromCache = false)
                } else {
                    val hasCache = cached != null
                    stateFlow.value = FxState.Unavailable(
                        reason = "Döviz kurları alınamadı",
                        hasCache = hasCache
                    )
                }
            } catch (e: Exception) {
                val cached = FxCache.load(context, base)
                stateFlow.value = FxState.Unavailable(
                    reason = "Döviz kurları alınamadı",
                    hasCache = cached != null
                )
            }
        }
        
        return stateFlow.asStateFlow()
    }
}

