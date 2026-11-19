package com.example.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppNotificationEntity
import com.example.data.local.NexaDatabase
import com.example.data.local.NexaPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_TASKS = "nexa_tasks_channel"
    const val CHANNEL_SILENT = "nexa_silent_channel"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val main = NotificationChannel(
                CHANNEL_TASKS,
                "تذكيرات NEXA",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { enableVibration(true) }
            val silent = NotificationChannel(
                CHANNEL_SILENT,
                "تذكيرات هادئة",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
                enableVibration(false)
            }
            nm.createNotificationChannel(main)
            nm.createNotificationChannel(silent)
        }
    }

    fun scheduleTaskReminder(context: Context, taskId: Long, title: String, triggerAtMs: Long) {
        if (triggerAtMs <= System.currentTimeMillis()) return
        ensureChannels(context)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("task_id", taskId)
            putExtra("title", title)
            putExtra("section", "TASKS")
        }
        val pi = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) am.cancel(pi)
    }
}

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val taskId = intent.getLongExtra("task_id", -1L)
        val title = intent.getStringExtra("title") ?: "تذكير NEXA"
        val section = intent.getStringExtra("section") ?: "TASKS"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderScheduler.ensureChannels(context)
                val prefs = NexaPreferences(context).state.first()
                val db = NexaDatabase.getInstance(context)

                val bodyText = when (prefs.language) {
                    "en" -> "Scheduled reminder from NEXA"
                    "fr" -> "Rappel planifié NEXA"
                    else -> "تذكير مجدول من NEXA"
                }

                // Store in unified notification center
                db.systemDao().insertNotification(
                    AppNotificationEntity(
                        section = section,
                        title = title,
                        body = bodyText,
                        isSensitive = true
                    )
                )

                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val inDnd = prefs.dndEnabled && (
                    if (prefs.dndStartHour > prefs.dndEndHour) {
                        hour >= prefs.dndStartHour || hour < prefs.dndEndHour
                    } else {
                        hour in prefs.dndStartHour until prefs.dndEndHour
                    }
                )

                val channelId = if (inDnd || !prefs.notificationSound) {
                    ReminderScheduler.CHANNEL_SILENT
                } else {
                    ReminderScheduler.CHANNEL_TASKS
                }

                val displayTitle = if (prefs.hideNotificationContent) "NEXA" else title
                val displayText = if (prefs.hideNotificationContent) {
                    when (prefs.language) {
                        "en" -> "Scheduled alert"
                        "fr" -> "Alerte planifiée"
                        else -> "لديك تنبيه مجدول"
                    }
                } else {
                    when (prefs.language) {
                        "en" -> "Tap to view details"
                        "fr" -> "Appuyez pour voir les détails"
                        else -> "اضغط لفتح التفاصيل"
                    }
                }

                val openIntent = PendingIntent.getActivity(
                    context,
                    taskId.toInt(),
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notif = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(displayTitle)
                    .setContentText(displayText)
                    .setVisibility(
                        if (prefs.hideNotificationContent) NotificationCompat.VISIBILITY_PRIVATE
                        else NotificationCompat.VISIBILITY_PUBLIC
                    )
                    .setContentIntent(openIntent)
                    .setAutoCancel(true)
                    .build()

                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(taskId.toInt().coerceAtLeast(100), notif)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
