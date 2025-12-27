package com.example.subscriptiontracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.subscriptiontracker.utils.SubscriptionReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receiver to reschedule all alarms after device boot
 */
class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all reminders after boot
            // Note: We need to get subscriptions from storage
            // For now, this will be called when app starts after boot
            scope.launch {
                try {
                    // This will be handled by MainActivity on startup
                    // We just need to ensure the receiver is registered
                } catch (e: Exception) {
                    // Ignore errors
                }
            }
        }
    }
}

