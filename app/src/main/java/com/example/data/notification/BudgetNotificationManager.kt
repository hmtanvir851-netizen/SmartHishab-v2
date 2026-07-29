package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object BudgetNotificationManager {

    private const val CHANNEL_ID = "smart_hishab_budget_alerts"
    private const val CHANNEL_NAME = "Budget Limit Alerts"
    private const val CHANNEL_DESC = "Notifies when monthly spending exceeds warning or maximum budget limits"
    private const val NOTIFICATION_ID_80 = 8001
    private const val NOTIFICATION_ID_100 = 1001

    private var hasNotified80ThisMonth = false
    private var hasNotified100ThisMonth = false

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun checkAndTriggerBudgetNotification(
        context: Context,
        currentExpense: Double,
        monthlyBudget: Double,
        currencySymbol: String = "৳",
        languageCode: String = "en"
    ) {
        if (monthlyBudget <= 0) return

        val ratio = currentExpense / monthlyBudget
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (ratio >= 1.0) {
            if (!hasNotified100ThisMonth) {
                hasNotified100ThisMonth = true

                val title = if (languageCode == "bn")
                    "⚠️ বাজেট সীমা অতিক্রম করেছে!"
                else
                    "⚠️ Monthly Budget Limit Exceeded!"

                val message = if (languageCode == "bn")
                    "আপনার মাসিক খরচ $currencySymbol${String.format("%.2f", currentExpense)} এ পৌঁছেছে (বাজেট: $currencySymbol${String.format("%.2f", monthlyBudget)})।"
                else
                    "Your monthly spending has reached $currencySymbol${String.format("%.2f", currentExpense)}, exceeding your $currencySymbol${String.format("%.2f", monthlyBudget)} budget!"

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                try {
                    notificationManager.notify(NOTIFICATION_ID_100, builder.build())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (ratio >= 0.8) {
            if (!hasNotified80ThisMonth) {
                hasNotified80ThisMonth = true

                val title = if (languageCode == "bn")
                    "🔔 ৮০% বাজেট খরচ হয়ে গেছে!"
                else
                    "🔔 80% Budget Limit Warning!"

                val message = if (languageCode == "bn")
                    "আপনি ইতোমধ্যেই আপনার বাজেটের ৮০% ($currencySymbol${String.format("%.2f", currentExpense)}) খরচ করে ফেলেছেন।"
                else
                    "You have spent 80% of your monthly budget ($currencySymbol${String.format("%.2f", currentExpense)} / $currencySymbol${String.format("%.2f", monthlyBudget)})."

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_logo)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                try {
                    notificationManager.notify(NOTIFICATION_ID_80, builder.build())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // Reset flags when expense falls below 80%
            hasNotified80ThisMonth = false
            hasNotified100ThisMonth = false
        }
    }

    fun resetNotificationState() {
        hasNotified80ThisMonth = false
        hasNotified100ThisMonth = false
    }
}
