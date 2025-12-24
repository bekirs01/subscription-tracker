package com.example.subscriptiontracker.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.receiver.ReminderReceiver
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

object SubscriptionReminderManager {
    private const val REMINDER_HOUR = 9 // 09:00 local time
    private const val REMINDER_MINUTE = 0
    
    // Reminder days: 7, 3, 1 days before renewal
    private val REMINDER_DAYS = listOf(7, 3, 1)
    
    /**
     * Generate unique request code for a subscription reminder
     * Format: subscriptionId * 10 + reminderIndex (0, 1, 2)
     */
    private fun getRequestCode(subscriptionId: Int, reminderIndex: Int): Int {
        return subscriptionId * 10 + reminderIndex
    }
    
    /**
     * Schedule all reminders for a subscription
     */
    fun scheduleReminders(context: Context, subscription: Subscription) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Parse renewal date
        val renewalDate = try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            LocalDate.parse(subscription.renewalDate, formatter)
        } catch (e: Exception) {
            return // Invalid date, skip reminders
        }
        
        REMINDER_DAYS.forEachIndexed { index, daysBefore ->
            val reminderDate = renewalDate.minusDays(daysBefore.toLong())
            val reminderTime = getReminderTime(reminderDate)
            
            // Only schedule if reminder date is in the future
            if (reminderTime > System.currentTimeMillis()) {
                val requestCode = getRequestCode(subscription.id, index)
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    putExtra("subscription_id", subscription.id)
                    putExtra("subscription_name", subscription.name)
                    putExtra("subscription_price", subscription.price)
                    putExtra("subscription_currency", subscription.currency)
                    putExtra("renewal_date", subscription.renewalDate)
                    putExtra("days_before", daysBefore)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                }
            }
        }
    }
    
    /**
     * Cancel all reminders for a subscription
     */
    fun cancelReminders(context: Context, subscriptionId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        REMINDER_DAYS.forEachIndexed { index, _ ->
            val requestCode = getRequestCode(subscriptionId, index)
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
    
    /**
     * Update reminders for a subscription (cancel old, schedule new)
     */
    fun updateReminders(context: Context, subscription: Subscription) {
        cancelReminders(context, subscription.id)
        scheduleReminders(context, subscription)
    }
    
    /**
     * Get reminder time in milliseconds for a given date at 09:00
     */
    private fun getReminderTime(date: LocalDate): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, date.year)
            set(Calendar.MONTH, date.monthValue - 1) // Calendar months are 0-based
            set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, REMINDER_HOUR)
            set(Calendar.MINUTE, REMINDER_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}

