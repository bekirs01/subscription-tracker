package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Tekil DataStore tanımı (tema + dil için ortak)
val Context.appDataStore by preferencesDataStore(name = "settings")


