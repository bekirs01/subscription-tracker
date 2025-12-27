package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

object PremiumManager {
    private val IS_PREMIUM_KEY = booleanPreferencesKey("is_premium")
    
    // Tek merkezi state - StateFlow (tüm ekranlar buradan dinleyecek)
    private val _isPremium = MutableStateFlow(false)
    val isPremiumFlow: StateFlow<Boolean> = _isPremium.asStateFlow()
    
    // Geriye dönük uyumluluk için Flow döndür (context parametresi korunuyor)
    fun isPremiumFlow(context: Context): Flow<Boolean> {
        // StateFlow'u Flow'a dönüştür - tüm ekranlar aynı StateFlow'u dinleyecek
        return _isPremium.asStateFlow()
    }
    
    // Premium durumunu ayarla (hem StateFlow hem DataStore'a kaydet)
    suspend fun setPremium(context: Context, isPremium: Boolean) {
        // Önce StateFlow'u güncelle (anında etki - tüm ekranlar güncellenir)
        _isPremium.value = isPremium
        
        // Sonra DataStore'a kaydet (kalıcılık - uygulama yeniden başladığında yüklenir)
        context.appDataStore.edit { preferences ->
            preferences[IS_PREMIUM_KEY] = isPremium
        }
    }
    
    // İlk açılışta DataStore'dan yükle (context gerektirir)
    suspend fun initialize(context: Context) {
        val currentValue = context.appDataStore.data.first()[IS_PREMIUM_KEY] ?: false
        _isPremium.value = currentValue
    }
}

