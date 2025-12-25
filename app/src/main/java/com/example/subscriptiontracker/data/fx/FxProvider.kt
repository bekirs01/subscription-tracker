package com.example.subscriptiontracker.data.fx

interface FxProvider {
    suspend fun fetch(base: String): FxRates?
}

