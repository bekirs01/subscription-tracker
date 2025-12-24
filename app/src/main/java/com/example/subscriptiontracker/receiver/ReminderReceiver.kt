package com.example.subscriptiontracker.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.subscriptiontracker.MainActivity
import com.example.subscriptiontracker.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subscriptionId = intent.getIntExtra("subscription_id", 0)
        val subscriptionName = intent.getStringExtra("subscription_name") ?: "Subscription"
        val subscriptionPrice = intent.getStringExtra("subscription_price") ?: "0"
        val subscriptionCurrency = intent.getStringExtra("subscription_currency") ?: "TRY"
        val renewalDate = intent.getStringExtra("renewal_date") ?: ""
        val daysBefore = intent.getIntExtra("days_before", 0)
        
        showNotification(
            context = context,
            subscriptionId = subscriptionId,
            subscriptionName = subscriptionName,
            subscriptionPrice = subscriptionPrice,
            subscriptionCurrency = subscriptionCurrency,
            renewalDate = renewalDate,
            daysBefore = daysBefore
        )
    }
    
    private fun showNotification(
        context: Context,
        subscriptionId: Int,
        subscriptionName: String,
        subscriptionPrice: String,
        subscriptionCurrency: String,
        renewalDate: String,
        daysBefore: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            subscriptionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val daysText = when (daysBefore) {
            7 -> "7 days"
            3 -> "3 days"
            1 -> "1 day"
            else -> "$daysBefore days"
        }
        
        val currencySymbol = when (subscriptionCurrency) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "TRY" -> "₺"
            else -> subscriptionCurrency
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Subscription Reminder")
            .setContentText("$subscriptionName renews in $daysText")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$subscriptionName (${currencySymbol}$subscriptionPrice) will renew on $renewalDate"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(subscriptionId * 100 + daysBefore, notification)
    }
    
    companion object {
        private const val CHANNEL_ID = "subscription_reminders"
        private const val CHANNEL_NAME = "Subscription Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for subscription renewal reminders"
    }
}

