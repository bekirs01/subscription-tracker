package com.example.subscriptiontracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.subscriptiontracker.MainActivity
import com.example.subscriptiontracker.R

class PremiumReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val subscriptionName = inputData.getString(KEY_SUBSCRIPTION_NAME) ?: "Abonelik"
        val renewalDate = inputData.getString(KEY_RENEWAL_DATE) ?: ""
        val daysBefore = inputData.getInt(KEY_DAYS_BEFORE, 0)

        showNotification(
            context = applicationContext,
            subscriptionName = subscriptionName,
            renewalDate = renewalDate,
            daysBefore = daysBefore
        )

        return Result.success()
    }

    private fun showNotification(
        context: Context,
        subscriptionName: String,
        renewalDate: String,
        daysBefore: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val daysText = when (daysBefore) {
            1 -> "1 gün"
            2 -> "2 gün"
            3 -> "3 gün"
            5 -> "5 gün"
            7 -> "7 gün"
            else -> "$daysBefore gün"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Aboneliğin yenileniyor")
            .setContentText("$daysText sonra aboneliğin sona erecek")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$subscriptionName aboneliğin $daysText sonra ($renewalDate) sona erecek."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .build()

        // Unique notification ID based on daysBefore
        notificationManager.notify(daysBefore, notification)
    }

    companion object {
        const val CHANNEL_ID = "premium_reminders"
        private const val CHANNEL_NAME = "Premium Hatırlatıcılar"
        private const val CHANNEL_DESCRIPTION = "Abonelik yenilenme hatırlatıcı bildirimleri"

        const val KEY_SUBSCRIPTION_NAME = "subscription_name"
        const val KEY_RENEWAL_DATE = "renewal_date"
        const val KEY_DAYS_BEFORE = "days_before"
    }
}

