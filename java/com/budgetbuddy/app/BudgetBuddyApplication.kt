package com.budgetbuddy.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BudgetBuddyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    TRANSACTION_CHANNEL_ID,
                    "Transaction Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for detected transactions that need categorization"
                },
                NotificationChannel(
                    BUDGET_ALERT_CHANNEL_ID,
                    "Budget Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts when you exceed or approach budget limits"
                },
                NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    "Reminders",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Reminders for budget reviews and financial goals"
                }
            )
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    companion object {
        const val TRANSACTION_CHANNEL_ID = "transaction_channel"
        const val BUDGET_ALERT_CHANNEL_ID = "budget_alert_channel"
        const val REMINDER_CHANNEL_ID = "reminder_channel"
    }
}
