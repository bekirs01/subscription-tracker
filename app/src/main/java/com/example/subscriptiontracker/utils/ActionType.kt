package com.example.subscriptiontracker.utils

import com.example.subscriptiontracker.Period

/**
 * AI Assistant'ın yapabileceği aksiyonlar
 */
sealed class ActionType {
    data class AddSubscription(
        val name: String,
        val price: String,
        val period: Period,
        val renewalDate: String
    ) : ActionType()
    
    data class ChangeTheme(val theme: AppTheme) : ActionType()
    data class ChangeLanguage(val languageCode: String) : ActionType()
    data class ChangeCurrency(val currencyCode: String) : ActionType()
    data class ChangeNotifications(val enabled: Boolean) : ActionType()
    
    object None : ActionType()
}

