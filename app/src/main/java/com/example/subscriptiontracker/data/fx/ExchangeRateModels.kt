package com.example.subscriptiontracker.data.fx

data class FxRates(
    val base: String,
    val rates: Map<String, Double>,
    val timestampEpochSec: Long
)

