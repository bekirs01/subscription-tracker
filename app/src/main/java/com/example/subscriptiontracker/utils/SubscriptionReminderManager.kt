package com.example.subscriptiontracker.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.subscriptiontracker.Subscription
import com.example.subscriptiontracker.receiver.ReminderReceiver
import kotlinx.coroutines.flow.first
import java.util.Calendar

object SubscriptionReminderManager {
    /**
     * Generate unique request code for a subscription reminder
     * Format: subscriptionId * 1000 + daysBefore (ensures uniqueness)
     */
    private fun getRequestCode(subscriptionId: Int, daysBefore: Int): Int {
        return subscriptionId * 1000 + daysBefore
    }
    
    /**
     * Schedule all reminders for a subscription using custom settings
     */
    suspend fun scheduleReminders(
        context: Context,
        subscription: Subscription,
        reminderSettings: ReminderSettings? = null
    ) {
        try {
            val settings = reminderSettings ?: ReminderSettingsManager.getReminderSettings(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Parse renewal date
            val renewalDate = try {
                if (subscription.renewalDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                    val parts = subscription.renewalDate.split("-")
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1 // Calendar months are 0-based
                    val day = parts[2].toInt()
                    Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                } else {
                    return // Invalid date, skip reminders
                }
            } catch (e: Exception) {
                return // Invalid date, skip reminders
            }
            
            // Schedule reminder for each selected day
            settings.days.forEach { daysBefore ->
                val reminderDate = renewalDate.clone() as Calendar
                reminderDate.add(Calendar.DAY_OF_MONTH, -daysBefore)
                reminderDate.set(Calendar.HOUR_OF_DAY, settings.hour)
                reminderDate.set(Calendar.MINUTE, settings.minute)
                reminderDate.set(Calendar.SECOND, 0)
                reminderDate.set(Calendar.MILLISECOND, 0)
                
                val reminderTime = reminderDate.timeInMillis
                
                // Only schedule if reminder date is in the future
                if (reminderTime > System.currentTimeMillis()) {
                    val requestCode = getRequestCode(subscription.id, daysBefore)
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
                    
                    try {
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
                    } catch (e: Exception) {
                        // Ignore alarm scheduling errors (permissions, etc.)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors to prevent crashes
        }
    }
    
    /**
     * Cancel all reminders for a subscription
     */
    fun cancelReminders(context: Context, subscriptionId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Cancel all possible reminder days (1-30)
            (1..30).forEach { daysBefore ->
                val requestCode = getRequestCode(subscriptionId, daysBefore)
                val intent = Intent(context, ReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * Update reminders for a subscription (cancel old, schedule new)
     */
    suspend fun updateReminders(
        context: Context,
        subscription: Subscription,
        reminderSettings: ReminderSettings? = null
    ) {
        cancelReminders(context, subscription.id)
        scheduleReminders(context, subscription, reminderSettings)
    }
    
    /**
     * Reschedule all reminders for all subscriptions (e.g., after boot or settings change)
     */
    suspend fun rescheduleAllReminders(context: Context, subscriptions: List<Subscription>) {
        try {
            val settings = ReminderSettingsManager.getReminderSettings(context)
            subscriptions.forEach { subscription ->
                scheduleReminders(context, subscription, settings)
            }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
}

