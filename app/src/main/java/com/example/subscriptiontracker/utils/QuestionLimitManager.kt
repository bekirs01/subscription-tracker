package com.example.subscriptiontracker.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

object QuestionLimitManager {
    private val QUESTIONS_ASKED_TODAY_KEY = intPreferencesKey("questions_asked_today")
    private val LAST_QUESTION_DATE_KEY = longPreferencesKey("last_question_date")
    
    const val FREE_DAILY_LIMIT = 3
    const val PREMIUM_LIMIT = Int.MAX_VALUE // Sınırsız
    
    fun getQuestionsAskedTodayFlow(context: Context): Flow<Int> {
        return context.appDataStore.data.map { preferences ->
            val lastDate = preferences[LAST_QUESTION_DATE_KEY] ?: 0L
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            if (lastDate < today) {
                // Yeni gün, sıfırla
                0
            } else {
                preferences[QUESTIONS_ASKED_TODAY_KEY] ?: 0
            }
        }
    }
    
    suspend fun incrementQuestionCount(context: Context) {
        context.appDataStore.edit { preferences ->
            val lastDate = preferences[LAST_QUESTION_DATE_KEY] ?: 0L
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            if (lastDate < today) {
                // Yeni gün, sıfırla ve başlat
                preferences[QUESTIONS_ASKED_TODAY_KEY] = 1
                preferences[LAST_QUESTION_DATE_KEY] = today
            } else {
                // Aynı gün, artır
                val current = preferences[QUESTIONS_ASKED_TODAY_KEY] ?: 0
                preferences[QUESTIONS_ASKED_TODAY_KEY] = current + 1
            }
        }
    }
    
    fun canAskQuestion(questionsAskedToday: Int, isPremium: Boolean): Boolean {
        return if (isPremium) {
            true // Premium sınırsız
        } else {
            questionsAskedToday < FREE_DAILY_LIMIT
        }
    }
}

