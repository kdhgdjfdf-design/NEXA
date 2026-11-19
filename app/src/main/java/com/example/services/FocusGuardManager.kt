package com.example.services

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.local.*
import com.example.domain.i18n.NexaLang
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.domain.stats.XpAndStatsEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

data class InstalledAppInfo(
    val packageName: String,
    val label: String
)

data class FocusTimerUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val mode: String = "POMODORO", // POMODORO, CUSTOM, OPEN
    val plannedSeconds: Long = 1500L,
    val remainingSeconds: Long = 1500L,
    val elapsedFocusSeconds: Long = 0L,
    val pausedSeconds: Long = 0L,
    val linkedTaskId: Long? = null,
    val linkedLessonId: Long? = null,
    val linkedTitle: String = "",
    val ambientSound: String = "NONE",
    val localAmbientUri: String = "",
    val activeReason: String = "FOCUS",
    val countdownBannerSeconds: Int = 0
)

class FocusGuardManager(
    private val context: Context,
    private val db: NexaDatabase,
    private val prefs: NexaPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val ambientEngine = AmbientSoundEngine(context)
    private var timerJob: Job? = null
    private var usageMonitorJob: Job? = null
    private var activeSessionId: Long = -1L
    private var pauseStartedMs: Long = 0L
    private var countdownJob: Job? = null
    private var pendingBlockPackage: String? = null

    @Volatile
    private var latestForegroundPackage: String? = null

    @Volatile
    private var lastBlockLaunchPkg: String? = null

    @Volatile
    private var lastBlockLaunchMs: Long = 0L

    private val temporaryBypasses = ConcurrentHashMap<String, Long>()

    private val _timerState = MutableStateFlow(FocusTimerUiState())
    val timerState: StateFlow<FocusTimerUiState> = _timerState.asStateFlow()

    companion object {
        const val CHANNEL_FOCUS_GUARD = "nexa_focus_guard"
        const val NOTIF_COUNTDOWN_ID = 4101

        @Volatile
        var instance: FocusGuardManager? = null
    }

    init {
        instance = this
        ensureNotificationChannel()
        restoreSessionIfActive()
        startForegroundMonitorLoop()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                CHANNEL_FOCUS_GUARD,
                "حارس التركيز NEXA",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(ch)
        }
    }

    private fun restoreSessionIfActive() {
        scope.launch {
            val p = prefs.state.first()
            if (p.activeFocusRunning) {
                val now = System.currentTimeMillis()
                if (p.activeFocusPaused) {
                    _timerState.update {
                        it.copy(
                            isRunning = true,
                            isPaused = true,
                            mode = p.activeFocusMode,
                            plannedSeconds = p.activeFocusTotalSeconds,
                            remainingSeconds = p.activeFocusRemainingOnPauseSec.coerceAtLeast(0L),
                            linkedTaskId = p.activeFocusTaskId.takeIf { id -> id > 0 },
                            linkedLessonId = p.activeFocusLessonId.takeIf { id -> id > 0 },
                            activeReason = p.activeFocusReason
                        )
                    }
                } else {
                    val rem = if (p.activeFocusMode == "OPEN") {
                        0L
                    } else {
                        ((p.activeFocusEndEpochMs - now) / 1000L).coerceAtLeast(0L)
                    }
                    if (p.activeFocusMode != "OPEN" && rem <= 0L) {
                        completeSession(autoFinished = true)
                    } else {
                        _timerState.update {
                            it.copy(
                                isRunning = true,
                                isPaused = false,
                                mode = p.activeFocusMode,
                                plannedSeconds = p.activeFocusTotalSeconds,
                                remainingSeconds = rem,
                                linkedTaskId = p.activeFocusTaskId.takeIf { id -> id > 0 },
                                linkedLessonId = p.activeFocusLessonId.takeIf { id -> id > 0 },
                                activeReason = p.activeFocusReason
                            )
                        }
                        startTicker()
                    }
                }
            }
        }
    }

    fun startFocusSession(
        mode: String,
        plannedMinutes: Int,
        linkedTaskId: Long? = null,
        linkedLessonId: Long? = null,
        linkedTitle: String = "",
        ambientSound: String = "NONE",
        localAmbientUri: String = ""
    ) {
        timerJob?.cancel()
        val totalSec = if (mode == "OPEN") 0L else (plannedMinutes * 60L).coerceAtLeast(60L)
        val now = System.currentTimeMillis()
        val endMs = if (mode == "OPEN") now else now + totalSec * 1000L

        _timerState.value = FocusTimerUiState(
            isRunning = true,
            isPaused = false,
            mode = mode,
            plannedSeconds = totalSec,
            remainingSeconds = totalSec,
            elapsedFocusSeconds = 0L,
            pausedSeconds = 0L,
            linkedTaskId = linkedTaskId,
            linkedLessonId = linkedLessonId,
            linkedTitle = linkedTitle,
            ambientSound = ambientSound,
            localAmbientUri = localAmbientUri,
            activeReason = "FOCUS"
        )

        ambientEngine.start(ambientSound, localAmbientUri)

        scope.launch(Dispatchers.IO) {
            activeSessionId = db.focusDao().insertFocusSession(
                FocusSessionEntity(
                    mode = mode,
                    plannedMinutes = plannedMinutes,
                    linkedTaskId = linkedTaskId,
                    linkedLessonId = linkedLessonId,
                    linkedTitle = linkedTitle,
                    ambientSound = ambientSound,
                    status = "RUNNING",
                    startedAt = now
                )
            )
            prefs.saveActiveFocusState(
                running = true,
                mode = mode,
                endMs = endMs,
                totalSec = totalSec,
                paused = false,
                pausedRemSec = totalSec,
                taskId = linkedTaskId ?: -1L,
                lessonId = linkedLessonId ?: -1L,
                reason = "FOCUS"
            )
        }
        startTicker()
        startForegroundMonitorLoop()
    }

    fun pauseSession() {
        val current = _timerState.value
        if (!current.isRunning || current.isPaused) return
        timerJob?.cancel()
        cancelCountdown()
        pauseStartedMs = System.currentTimeMillis()
        ambientEngine.stop()
        _timerState.update { it.copy(isPaused = true) }
        scope.launch(Dispatchers.IO) {
            prefs.saveActiveFocusState(
                running = true,
                mode = current.mode,
                endMs = 0L,
                totalSec = current.plannedSeconds,
                paused = true,
                pausedRemSec = current.remainingSeconds,
                taskId = current.linkedTaskId ?: -1L,
                lessonId = current.linkedLessonId ?: -1L,
                reason = current.activeReason
            )
        }
    }

    fun resumeSession() {
        val current = _timerState.value
        if (!current.isRunning || !current.isPaused) return
        val pausedDelta = if (pauseStartedMs > 0L) ((System.currentTimeMillis() - pauseStartedMs) / 1000L).coerceAtLeast(0L) else 0L
        pauseStartedMs = 0L
        val newEndMs = System.currentTimeMillis() + current.remainingSeconds * 1000L
        _timerState.update { it.copy(isPaused = false, pausedSeconds = it.pausedSeconds + pausedDelta) }
        ambientEngine.start(current.ambientSound, current.localAmbientUri)
        scope.launch(Dispatchers.IO) {
            prefs.saveActiveFocusState(
                running = true,
                mode = current.mode,
                endMs = newEndMs,
                totalSec = current.plannedSeconds,
                paused = false,
                pausedRemSec = current.remainingSeconds,
                taskId = current.linkedTaskId ?: -1L,
                lessonId = current.linkedLessonId ?: -1L,
                reason = current.activeReason
            )
        }
        startTicker()
    }

    fun completeSession(autoFinished: Boolean = false) {
        timerJob?.cancel()
        cancelCountdown()
        ambientEngine.stop()
        val current = _timerState.value
        val focusedMinutes = (current.elapsedFocusSeconds / 60L).toInt()

        _timerState.value = FocusTimerUiState()
        scope.launch(Dispatchers.IO) {
            prefs.saveActiveFocusState(false, "POMODORO", 0L, 1500L, false, 0L, -1L, -1L, "FOCUS")
            val now = System.currentTimeMillis()
            if (activeSessionId > 0) {
                db.focusDao().updateFocusSession(
                    FocusSessionEntity(
                        id = activeSessionId,
                        mode = current.mode,
                        plannedMinutes = (current.plannedSeconds / 60L).toInt(),
                        elapsedFocusSeconds = current.elapsedFocusSeconds,
                        pausedSeconds = current.pausedSeconds,
                        linkedTaskId = current.linkedTaskId,
                        linkedLessonId = current.linkedLessonId,
                        linkedTitle = current.linkedTitle,
                        ambientSound = current.ambientSound,
                        status = if (autoFinished || current.elapsedFocusSeconds >= 60) "COMPLETED" else "CANCELLED",
                        startedAt = now - (current.elapsedFocusSeconds + current.pausedSeconds) * 1000L,
                        endedAt = now
                    )
                )
            } else if (current.elapsedFocusSeconds > 0L) {
                db.focusDao().insertFocusSession(
                    FocusSessionEntity(
                        mode = current.mode,
                        plannedMinutes = (current.plannedSeconds / 60L).toInt(),
                        elapsedFocusSeconds = current.elapsedFocusSeconds,
                        pausedSeconds = current.pausedSeconds,
                        linkedTaskId = current.linkedTaskId,
                        linkedLessonId = current.linkedLessonId,
                        linkedTitle = current.linkedTitle,
                        ambientSound = current.ambientSound,
                        status = "COMPLETED",
                        startedAt = now - current.elapsedFocusSeconds * 1000L,
                        endedAt = now
                    )
                )
            }
            activeSessionId = -1L
            if (focusedMinutes > 0) {
                prefs.addXp((focusedMinutes / 5).coerceAtLeast(5))
                XpAndStatsEngine.evaluateAchievements(db, prefs)
            }
        }
    }

    fun grantTemporaryBypass(packageName: String, reason: String, durationMs: Long = 10 * 60_000L) {
        if (packageName.isNotBlank()) {
            temporaryBypasses["$packageName:$reason"] = System.currentTimeMillis() + durationMs
        }
        if (reason == "PRAYER") {
            temporaryBypasses["*:$reason"] = System.currentTimeMillis() + durationMs
        }
        cancelCountdown()
    }

    fun isBypassed(packageName: String, reason: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        val direct = temporaryBypasses["$packageName:$reason"] ?: 0L
        val global = temporaryBypasses["*:$reason"] ?: 0L
        return nowMs < direct || nowMs < global
    }

    fun onAccessibilityConnected() {
        latestForegroundPackage?.let { onForegroundPackageDetected(it) }
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                val st = _timerState.value
                if (!st.isRunning || st.isPaused) continue
                val newElapsed = st.elapsedFocusSeconds + 1L
                if (st.mode == "OPEN") {
                    _timerState.update { it.copy(elapsedFocusSeconds = newElapsed) }
                } else {
                    val newRem = (st.remainingSeconds - 1L).coerceAtLeast(0L)
                    _timerState.update { it.copy(elapsedFocusSeconds = newElapsed, remainingSeconds = newRem) }
                    if (newRem <= 0L) {
                        completeSession(autoFinished = true)
                        break
                    }
                }
            }
        }
    }

    /**
     * Periodically polls UsageStatsManager recent events when granted and Focus/Prayer/DailyLimit
     * protection has enabled apps, acting as an extra safety net alongside AccessibilityService.
     */
    private fun startForegroundMonitorLoop() {
        if (usageMonitorJob?.isActive == true) return
        usageMonitorJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(1500L)
                val enabledApps = runCatching { db.focusDao().getBlockedAppsSync().filter { it.isEnabled } }
                    .getOrDefault(emptyList())
                if (enabledApps.isEmpty()) continue
                if (hasUsageStatsPermission()) {
                    val fgPkg = queryRecentForegroundPackage()
                    if (!fgPkg.isNullOrBlank()) {
                        onForegroundPackageDetected(fgPkg)
                    }
                }
            }
        }
    }

    private fun queryRecentForegroundPackage(): String? {
        return runCatching {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val end = System.currentTimeMillis()
            val start = end - 4000L
            val events = usm.queryEvents(start, end) ?: return@runCatching null
            val ev = UsageEvents.Event()
            var latestPkg: String? = null
            var latestTs = 0L
            while (events.hasNextEvent()) {
                events.getNextEvent(ev)
                @Suppress("DEPRECATION")
                val isFg = ev.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ev.eventType == UsageEvents.Event.ACTIVITY_RESUMED)
                if (isFg && ev.timeStamp >= latestTs) {
                    latestTs = ev.timeStamp
                    latestPkg = ev.packageName
                }
            }
            latestPkg
        }.getOrNull()
    }

    /**
     * Evaluates why [blocked] should be blocked right now, or null if allowed.
     */
    suspend fun evaluateBlockReason(
        blocked: BlockedAppEntity,
        p: NexaPrefsState,
        st: FocusTimerUiState,
        nowMs: Long = System.currentTimeMillis(),
        cal: Calendar = Calendar.getInstance()
    ): String? {
        if (!blocked.isEnabled) return null
        val reason: String = when {
            st.isRunning && !st.isPaused && blocked.blockInFocus -> "FOCUS"
            blocked.blockInTasks && hasActiveTaskGuardWindow(nowMs) -> "TASK"
            blocked.blockInPrayer && p.focusGuardPrayerBlock && isInPrayerWindow(p, cal) -> "PRAYER"
            blocked.dailyLimitMinutes > 0 && getAppUsageMinutesToday(blocked.packageName) >= blocked.dailyLimitMinutes -> "DAILY_LIMIT"
            else -> return null
        }
        if (isBypassed(blocked.packageName, reason, nowMs)) return null
        return reason
    }

    /**
     * Called by FocusGuardAccessibilityService or UsageStats monitor when a foreground package is observed.
     */
    fun onForegroundPackageDetected(packageName: String) {
        if (packageName.isBlank()) return
        latestForegroundPackage = packageName

        if (packageName == context.packageName || packageName.startsWith("com.android.systemui") || packageName == "android") {
            if (packageName == context.packageName && pendingBlockPackage != null) {
                cancelCountdown()
            }
            return
        }

        scope.launch(Dispatchers.IO) {
            val blocked = db.focusDao().getBlockedAppsSync()
                .firstOrNull { it.packageName == packageName && it.isEnabled } ?: return@launch
            val p = prefs.state.first()
            val st = _timerState.value

            val reason = evaluateBlockReason(blocked, p, st) ?: return@launch
            val now = System.currentTimeMillis()
            if (lastBlockLaunchPkg == packageName && now - lastBlockLaunchMs < 2200L) return@launch

            if (p.focusGuardCountdownEnabled) {
                startThirtySecondCountdownAndBlock(packageName, blocked.appLabel, reason, p.language)
            } else {
                lastBlockLaunchPkg = packageName
                lastBlockLaunchMs = now
                launchBlockScreen(packageName, blocked.appLabel, reason, _timerState.value.remainingSeconds)
            }
        }
    }

    private fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        pendingBlockPackage = null
        _timerState.update { it.copy(countdownBannerSeconds = 0) }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        runCatching { nm?.cancel(NOTIF_COUNTDOWN_ID) }
    }

    private fun startThirtySecondCountdownAndBlock(
        packageName: String,
        appLabel: String,
        reason: String,
        languageCode: String
    ) {
        if (pendingBlockPackage == packageName && countdownJob?.isActive == true) return
        pendingBlockPackage = packageName
        countdownJob?.cancel()
        countdownJob = scope.launch {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val lang = NexaLang.fromCode(languageCode)
            val titlePrefix = when (lang) {
                NexaLang.AR -> "حارس التركيز"
                NexaLang.FR -> "Garde Focus"
                NexaLang.EN -> "Focus Guard"
            }
            for (sec in 30 downTo 1) {
                // Abort countdown immediately if user switched back to NEXA or blocking ended
                if (latestForegroundPackage == context.packageName) {
                    cancelCountdown()
                    return@launch
                }
                val currentSt = _timerState.value
                if (reason == "FOCUS" && (!currentSt.isRunning || currentSt.isPaused)) {
                    cancelCountdown()
                    return@launch
                }
                if (isBypassed(packageName, reason)) {
                    cancelCountdown()
                    return@launch
                }

                _timerState.update { it.copy(countdownBannerSeconds = sec) }
                val body = when (lang) {
                    NexaLang.AR -> "سيتم حجب التطبيق خلال $sec ثانية"
                    NexaLang.FR -> "Blocage dans ${sec}s"
                    NexaLang.EN -> "Blocking in ${sec}s"
                }
                val notif = NotificationCompat.Builder(context, CHANNEL_FOCUS_GUARD)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("$titlePrefix • $appLabel")
                    .setContentText(body)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
                runCatching { nm.notify(NOTIF_COUNTDOWN_ID, notif) }
                delay(1000L)
            }
            _timerState.update { it.copy(countdownBannerSeconds = 0) }
            runCatching { nm.cancel(NOTIF_COUNTDOWN_ID) }
            pendingBlockPackage = null
            lastBlockLaunchPkg = packageName
            lastBlockLaunchMs = System.currentTimeMillis()
            launchBlockScreen(packageName, appLabel, reason, _timerState.value.remainingSeconds)
        }
    }

    private fun launchBlockScreen(packageName: String, appLabel: String, reason: String, remainingSec: Long) {
        val launcherContext: Context = FocusGuardAccessibilityService.activeService ?: context
        val intent = Intent(launcherContext, FocusBlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("blocked_pkg", packageName)
            putExtra("blocked_label", appLabel)
            putExtra("block_reason", reason)
            putExtra("remaining_sec", remainingSec)
        }
        runCatching { launcherContext.startActivity(intent) }
    }

    private suspend fun hasActiveTaskGuardWindow(now: Long = System.currentTimeMillis()): Boolean {
        return db.taskDao().getAllTasksSync().any { task ->
            !task.isCompleted && task.focusGuardOnDue && task.dueAt != null &&
                now in (task.dueAt - 15 * 60_000L)..(task.dueAt + 30 * 60_000L)
        }
    }

    fun isInPrayerWindow(p: NexaPrefsState, cal: Calendar = Calendar.getInstance()): Boolean {
        val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val times = PrayerTimesCalculator.calculate(p.prayerLat, p.prayerLon, p.prayerMethod, cal)
        val prayers = listOf(times.fajrMinutes, times.dhuhrMinutes, times.asrMinutes, times.maghribMinutes, times.ishaMinutes)
        return prayers.any { pt -> nowMin in pt..(pt + 15) }
    }

    fun getAppUsageMinutesToday(packageName: String): Int {
        if (!hasUsageStatsPermission()) return 0
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.timeInMillis, System.currentTimeMillis())
        val entry = stats?.firstOrNull { it.packageName == packageName }
        return ((entry?.totalTimeInForeground ?: 0L) / 60_000L).toInt()
    }

    fun getLaunchableInstalledApps(): List<InstalledAppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
        return resolveInfos
            .map {
                InstalledAppInfo(
                    packageName = it.activityInfo.packageName,
                    label = it.loadLabel(pm).toString()
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun isAccessibilityEnabled(): Boolean {
        if (FocusGuardAccessibilityService.activeService != null) return true
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?: return false
        return enabled.contains(context.packageName) && enabled.contains(FocusGuardAccessibilityService::class.java.simpleName)
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }
}
