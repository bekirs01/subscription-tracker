package com.example.subscriptiontracker.utils

import android.content.Context
import com.example.subscriptiontracker.Subscription

object ActionExecutor {
    
    /**
     * Action'ı execute eder ve sonuç mesajı döner
     */
    suspend fun executeAction(
        context: Context,
        action: ActionType,
        onAddSubscription: ((Subscription) -> Unit)? = null,
        onThemeChanged: (() -> Unit)? = null,
        onLanguageChanged: (() -> Unit)? = null
    ): String {
        return when (action) {
            is ActionType.AddSubscription -> {
                val subscription = Subscription(
                    id = 0, // ID parent'ta atanacak
                    name = action.name,
                    price = action.price,
                    period = action.period,
                    renewalDate = action.renewalDate
                )
                onAddSubscription?.invoke(subscription)
                "✅ Abonelik başarıyla eklendi: ${action.name}, ${action.price} TL, ${if (action.period == com.example.subscriptiontracker.Period.MONTHLY) "Aylık" else "Yıllık"}, ${action.renewalDate}"
            }
            
            is ActionType.ChangeTheme -> {
                ThemeManager.saveTheme(context, action.theme)
                onThemeChanged?.invoke()
                val themeName = when (action.theme) {
                    AppTheme.LIGHT -> "Açık"
                    AppTheme.DARK -> "Koyu"
                    AppTheme.SYSTEM -> "Sistem"
                }
                "✅ Tema $themeName olarak değiştirildi."
            }
            
            is ActionType.ChangeLanguage -> {
                LocaleManager.saveLanguage(context, action.languageCode)
                onLanguageChanged?.invoke()
                val language = LocaleManager.getLanguage(action.languageCode)
                "✅ Dil ${language?.name ?: action.languageCode} olarak değiştirildi. Uygulama yeniden başlatılacak."
            }
            
            is ActionType.ChangeCurrency -> {
                CurrencyManager.saveCurrency(context, action.currencyCode)
                val currency = CurrencyManager.getCurrency(action.currencyCode)
                "✅ Para birimi ${currency?.name ?: action.currencyCode} olarak değiştirildi."
            }
            
            is ActionType.ChangeNotifications -> {
                NotificationManager.saveNotificationsEnabled(context, action.enabled)
                val status = if (action.enabled) "açıldı" else "kapatıldı"
                "✅ Bildirimler $status."
            }
            
            is ActionType.None -> "Aksiyon bulunamadı."
        }
    }
    
    /**
     * Action için onay mesajı oluşturur
     */
    fun getConfirmationMessage(action: ActionType): String {
        return when (action) {
            is ActionType.AddSubscription -> {
                "📝 Abonelik özeti:\n\n" +
                "İsim: ${action.name}\n" +
                "Fiyat: ${action.price} TL\n" +
                "Periyot: ${if (action.period == com.example.subscriptiontracker.Period.MONTHLY) "Aylık" else "Yıllık"}\n" +
                "Yenileme: ${action.renewalDate}\n\n" +
                "Ekleyeyim mi?"
            }
            
            is ActionType.ChangeTheme -> {
                val themeName = when (action.theme) {
                    AppTheme.LIGHT -> "Açık"
                    AppTheme.DARK -> "Koyu"
                    AppTheme.SYSTEM -> "Sistem"
                }
                "🎨 Tema $themeName olarak değiştirilecek. Onaylıyor musun?"
            }
            
            is ActionType.ChangeLanguage -> {
                val language = LocaleManager.getLanguage(action.languageCode)
                "🌍 Dil ${language?.name ?: action.languageCode} olarak değiştirilecek. Uygulama yeniden başlatılacak. Onaylıyor musun?"
            }
            
            is ActionType.ChangeCurrency -> {
                val currency = CurrencyManager.getCurrency(action.currencyCode)
                "💰 Para birimi ${currency?.name ?: action.currencyCode} olarak değiştirilecek. Onaylıyor musun?"
            }
            
            is ActionType.ChangeNotifications -> {
                val status = if (action.enabled) "açılacak" else "kapatılacak"
                "🔔 Bildirimler $status. Onaylıyor musun?"
            }
            
            is ActionType.None -> ""
        }
    }
}

