package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.example.data.local.NexaDatabase
import com.example.data.local.NexaPreferences
import com.example.domain.backup.BackupRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val db = NexaDatabase.getInstance(applicationContext)
        val prefs = NexaPreferences(applicationContext)
        val repo = BackupRepository(applicationContext, db, prefs)
        return runCatching {
            repo.createLocalEncryptedBackup(isAuto = true)
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        private const val WORK_NAME = "nexa_auto_encrypted_backup"

        fun schedule(context: Context, enabled: Boolean) {
            val wm = WorkManager.getInstance(context)
            if (enabled) {
                val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(24, TimeUnit.HOURS)
                    .build()
                wm.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            } else {
                wm.cancelUniqueWork(WORK_NAME)
            }
        }
    }
}

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = NexaDatabase.getInstance(context)
                val now = System.currentTimeMillis()
                db.taskDao().getAllTasksSync().forEach { task ->
                    val rem = task.snoozedUntil ?: task.reminderAt
                    if (!task.isCompleted && rem != null && rem > now) {
                        ReminderScheduler.scheduleTaskReminder(context, task.id, task.title, rem)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
