package com.example.subscriptiontracker.utils

import com.example.subscriptiontracker.Period
import java.time.LocalDate

object ActionParser {
    
    /**
     * Kullanıcı mesajından aksiyon çıkarır
     */
    fun parseAction(message: String): ActionType {
        val lowerMessage = message.lowercase().trim()
        
        // Abonelik ekleme
        val subscriptionAction = parseAddSubscription(lowerMessage)
        if (subscriptionAction != ActionType.None) return subscriptionAction
        
        // Tema değiştirme
        val themeAction = parseChangeTheme(lowerMessage)
        if (themeAction != ActionType.None) return themeAction
        
        // Dil değiştirme
        val languageAction = parseChangeLanguage(lowerMessage)
        if (languageAction != ActionType.None) return languageAction
        
        // Para birimi değiştirme
        val currencyAction = parseChangeCurrency(lowerMessage)
        if (currencyAction != ActionType.None) return currencyAction
        
        // Bildirim ayarları
        val notificationAction = parseChangeNotifications(lowerMessage)
        if (notificationAction != ActionType.None) return notificationAction
        
        return ActionType.None
    }
    
    private fun parseAddSubscription(message: String): ActionType {
        // Örnek: "Netflix aboneliğim var, aylık 99 TL, yenileme 15 Ekim"
        // Örnek: "Spotify ekle, 49.99, aylık, 2024-12-31"
        
        val subscriptionKeywords = listOf("abonelik", "subscription", "ekle", "add", "var", "aboneliğim")
        if (!subscriptionKeywords.any { message.contains(it) }) {
            return ActionType.None
        }
        
        // İsim çıkar
        val name = extractSubscriptionName(message)
        if (name.isBlank()) return ActionType.None
        
        // Fiyat çıkar
        val price = extractPrice(message)
        if (price.isBlank()) return ActionType.None
        
        // Periyot çıkar
        val period = extractPeriod(message)
        
        // Tarih çıkar
        val date = extractDate(message)
        if (date.isBlank()) return ActionType.None
        
        return ActionType.AddSubscription(name, price, period, date)
    }
    
    private fun extractSubscriptionName(message: String): String {
        // Yaygın abonelik isimleri
        val commonNames = listOf(
            "netflix", "spotify", "youtube", "disney", "amazon", "apple", "icloud",
            "adobe", "microsoft", "office", "dropbox", "google", "drive", "photoshop"
        )
        
        for (name in commonNames) {
            if (message.contains(name)) {
                return name.replaceFirstChar { it.uppercase() }
            }
        }
        
        // "X aboneliğim var" veya "X ekle" formatından isim çıkar
        val patterns = listOf(
            "(\\w+)\\s+(aboneliğim|abonelik|ekle|add)",
            "(aboneliğim|abonelik|ekle|add)\\s+(\\w+)"
        )
        
        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(message)
            if (match != null) {
                val name = match.groupValues.lastOrNull { it.isNotBlank() && !it.equals("aboneliğim", ignoreCase = true) && !it.equals("abonelik", ignoreCase = true) && !it.equals("ekle", ignoreCase = true) && !it.equals("add", ignoreCase = true) }
                if (name != null && name.length > 2) {
                    return name.replaceFirstChar { it.uppercase() }
                }
            }
        }
        
        return ""
    }
    
    private fun extractPrice(message: String): String {
        // Sayı + TL/USD/EUR gibi para birimi
        val pricePattern = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:tl|usd|eur|₺|\\$|€|lira|dolar|euro)?", RegexOption.IGNORE_CASE)
        val match = pricePattern.find(message)
        return match?.groupValues?.get(1) ?: ""
    }
    
    private fun extractPeriod(message: String): Period {
        return when {
            message.contains("yıllık") || message.contains("yearly") || message.contains("annual") -> Period.YEARLY
            else -> Period.MONTHLY // Varsayılan
        }
    }
    
    private fun extractDate(message: String): String {
        // yyyy-MM-dd formatı
        val datePattern1 = Regex("(\\d{4}-\\d{2}-\\d{2})")
        val match1 = datePattern1.find(message)
        if (match1 != null) {
            return match1.groupValues[1]
        }
        
        // Türkçe tarih formatı: "15 Ekim" veya "15 Ekim 2024"
        val monthMap = mapOf(
            "ocak" to "01", "şubat" to "02", "mart" to "03", "nisan" to "04",
            "mayıs" to "05", "haziran" to "06", "temmuz" to "07", "ağustos" to "08",
            "eylül" to "09", "ekim" to "10", "kasım" to "11", "aralık" to "12"
        )
        
        val datePattern2 = Regex("(\\d{1,2})\\s+(\\w+)(?:\\s+(\\d{4}))?")
        val match2 = datePattern2.find(message)
        if (match2 != null) {
            val day = match2.groupValues[1].padStart(2, '0')
            val monthName = match2.groupValues[2].lowercase()
            val year = match2.groupValues[3].ifBlank { LocalDate.now().year.toString() }
            val month = monthMap[monthName] ?: return ""
            
            return "$year-$month-$day"
        }
        
        return ""
    }
    
    private fun parseChangeTheme(message: String): ActionType {
        val themeKeywords = mapOf(
            "koyu" to AppTheme.DARK,
            "dark" to AppTheme.DARK,
            "açık" to AppTheme.LIGHT,
            "light" to AppTheme.LIGHT,
            "sistem" to AppTheme.SYSTEM,
            "system" to AppTheme.SYSTEM
        )
        
        for ((keyword, theme) in themeKeywords) {
            if (message.contains(keyword) && (message.contains("tema") || message.contains("theme"))) {
                return ActionType.ChangeTheme(theme)
            }
        }
        
        return ActionType.None
    }
    
    private fun parseChangeLanguage(message: String): ActionType {
        val languageMap = mapOf(
            "türkçe" to "tr",
            "turkish" to "tr",
            "ingilizce" to "en",
            "english" to "en",
            "almanca" to "de",
            "german" to "de",
            "deutsch" to "de",
            "rusça" to "ru",
            "russian" to "ru",
            "fransızca" to "fr",
            "french" to "fr",
            "ispanyolca" to "es",
            "spanish" to "es",
            "italyanca" to "it",
            "italian" to "it",
            "portekizce" to "pt",
            "portuguese" to "pt",
            "arapça" to "ar",
            "arabic" to "ar",
            "çince" to "zh",
            "chinese" to "zh"
        )
        
        for ((keyword, code) in languageMap) {
            if (message.contains(keyword) && (message.contains("dil") || message.contains("language"))) {
                return ActionType.ChangeLanguage(code)
            }
        }
        
        return ActionType.None
    }
    
    private fun parseChangeCurrency(message: String): ActionType {
        val currencyMap = mapOf(
            "tl" to "TRY",
            "lira" to "TRY",
            "türk lirası" to "TRY",
            "usd" to "USD",
            "dolar" to "USD",
            "dollar" to "USD",
            "eur" to "EUR",
            "euro" to "EUR",
            "gbp" to "GBP",
            "pound" to "GBP",
            "sterlin" to "GBP"
        )
        
        for ((keyword, code) in currencyMap) {
            if (message.contains(keyword) && (message.contains("para birimi") || message.contains("currency") || message.contains("fiyat") || message.contains("price"))) {
                return ActionType.ChangeCurrency(code)
            }
        }
        
        return ActionType.None
    }
    
    private fun parseChangeNotifications(message: String): ActionType {
        return when {
            (message.contains("bildirim") || message.contains("notification")) && 
            (message.contains("aç") || message.contains("open") || message.contains("enable") || message.contains("aktif")) -> {
                ActionType.ChangeNotifications(true)
            }
            (message.contains("bildirim") || message.contains("notification")) && 
            (message.contains("kapat") || message.contains("close") || message.contains("disable") || message.contains("kapat")) -> {
                ActionType.ChangeNotifications(false)
            }
            else -> ActionType.None
        }
    }
}

