package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.nexaDataStore: DataStore<Preferences> by preferencesDataStore(name = "nexa_prefs")

data class NexaPrefsState(
    val language: String = "ar",
    val activeProfileId: Long = 1L,
    val globalEditMode: Boolean = false,
    val totalXp: Int = 0,
    val pinHash: String = "",
    val pinSalt: String = "",
    val biometricEnabled: Boolean = false,
    val autoLockMinutes: Int = 5,
    val lockedSections: Set<String> = emptySet(),
    val hideNotificationContent: Boolean = false,
    val dndEnabled: Boolean = false,
    val dndStartHour: Int = 22,
    val dndEndHour: Int = 6,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val prayerCity: String = "Riyadh",
    val prayerLat: Double = 24.7136,
    val prayerLon: Double = 46.6753,
    val prayerMethod: String = "UMM_AL_QURA",
    val prayerRemindersEnabled: Boolean = false,
    val focusGuardPrayerBlock: Boolean = false,
    val focusGuardCountdownEnabled: Boolean = true,
    val focusGuardCustomMessage: String = "",
    val focusDailyGoalMinutes: Int = 90,
    val activeFocusRunning: Boolean = false,
    val activeFocusMode: String = "POMODORO",
    val activeFocusEndEpochMs: Long = 0L,
    val activeFocusTotalSeconds: Long = 1500L,
    val activeFocusPaused: Boolean = false,
    val activeFocusRemainingOnPauseSec: Long = 0L,
    val activeFocusTaskId: Long = -1L,
    val activeFocusLessonId: Long = -1L,
    val activeFocusReason: String = "FOCUS",
    val activeAudioTrackId: Long = -1L,
    val activeAudioPositionMs: Long = 0L,
    val podcastPlaybackSpeed: Float = 1.0f,
    val autoBackupEnabled: Boolean = false,
    val lastBackupTimestamp: Long = 0L,
    val seededInitialData: Boolean = false,
    val userName: String = "",
    val userTagline: String = "",
    val userAvatarUri: String = "",
    val quoteIndex: Int = 0,
    val focusBackgroundUri: String = "",
    val migratedToNoxTheme: Boolean = false
)

class NexaPreferences(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
        val GLOBAL_EDIT_MODE = booleanPreferencesKey("global_edit_mode")
        val TOTAL_XP = intPreferencesKey("total_xp")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOCK_MINUTES = intPreferencesKey("auto_lock_minutes")
        val LOCKED_SECTIONS = stringSetPreferencesKey("locked_sections")
        val HIDE_NOTIF_CONTENT = booleanPreferencesKey("hide_notif_content")
        val DND_ENABLED = booleanPreferencesKey("dnd_enabled")
        val DND_START_HOUR = intPreferencesKey("dnd_start_hour")
        val DND_END_HOUR = intPreferencesKey("dnd_end_hour")
        val NOTIF_SOUND = booleanPreferencesKey("notif_sound")
        val NOTIF_VIBRATION = booleanPreferencesKey("notif_vibration")
        val PRAYER_CITY = stringPreferencesKey("prayer_city")
        val PRAYER_LAT = doublePreferencesKey("prayer_lat")
        val PRAYER_LON = doublePreferencesKey("prayer_lon")
        val PRAYER_METHOD = stringPreferencesKey("prayer_method")
        val PRAYER_REMINDERS = booleanPreferencesKey("prayer_reminders")
        val FG_PRAYER_BLOCK = booleanPreferencesKey("fg_prayer_block")
        val FG_COUNTDOWN = booleanPreferencesKey("fg_countdown")
        val FG_MESSAGE = stringPreferencesKey("fg_message")
        val FOCUS_DAILY_GOAL = intPreferencesKey("focus_daily_goal")
        val FOCUS_RUNNING = booleanPreferencesKey("focus_running")
        val FOCUS_MODE = stringPreferencesKey("focus_mode")
        val FOCUS_END_MS = longPreferencesKey("focus_end_ms")
        val FOCUS_TOTAL_SEC = longPreferencesKey("focus_total_sec")
        val FOCUS_PAUSED = booleanPreferencesKey("focus_paused")
        val FOCUS_PAUSED_REM = longPreferencesKey("focus_paused_rem")
        val FOCUS_TASK_ID = longPreferencesKey("focus_task_id")
        val FOCUS_LESSON_ID = longPreferencesKey("focus_lesson_id")
        val FOCUS_REASON = stringPreferencesKey("focus_reason")
        val AUDIO_TRACK_ID = longPreferencesKey("audio_track_id")
        val AUDIO_POS_MS = longPreferencesKey("audio_pos_ms")
        val PODCAST_SPEED = stringPreferencesKey("podcast_speed")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val LAST_BACKUP_TS = longPreferencesKey("last_backup_ts")
        val SEEDED = booleanPreferencesKey("seeded_initial_data")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_TAGLINE = stringPreferencesKey("user_tagline")
        val USER_AVATAR_URI = stringPreferencesKey("user_avatar_uri")
        val QUOTE_INDEX = intPreferencesKey("quote_index")
        val FOCUS_BG_URI = stringPreferencesKey("focus_bg_uri")
        val MIGRATED_NOX = booleanPreferencesKey("migrated_nox_theme")
    }

    val state: Flow<NexaPrefsState> = context.nexaDataStore.data.map { p ->
        NexaPrefsState(
            language = p[Keys.LANGUAGE] ?: "ar",
            activeProfileId = p[Keys.ACTIVE_PROFILE_ID] ?: 1L,
            globalEditMode = p[Keys.GLOBAL_EDIT_MODE] ?: false,
            totalXp = p[Keys.TOTAL_XP] ?: 0,
            pinHash = p[Keys.PIN_HASH] ?: "",
            pinSalt = p[Keys.PIN_SALT] ?: "",
            biometricEnabled = p[Keys.BIOMETRIC_ENABLED] ?: false,
            autoLockMinutes = p[Keys.AUTO_LOCK_MINUTES] ?: 5,
            lockedSections = p[Keys.LOCKED_SECTIONS] ?: emptySet(),
            hideNotificationContent = p[Keys.HIDE_NOTIF_CONTENT] ?: false,
            dndEnabled = p[Keys.DND_ENABLED] ?: false,
            dndStartHour = p[Keys.DND_START_HOUR] ?: 22,
            dndEndHour = p[Keys.DND_END_HOUR] ?: 6,
            notificationSound = p[Keys.NOTIF_SOUND] ?: true,
            notificationVibration = p[Keys.NOTIF_VIBRATION] ?: true,
            prayerCity = p[Keys.PRAYER_CITY] ?: "Riyadh",
            prayerLat = p[Keys.PRAYER_LAT] ?: 24.7136,
            prayerLon = p[Keys.PRAYER_LON] ?: 46.6753,
            prayerMethod = p[Keys.PRAYER_METHOD] ?: "UMM_AL_QURA",
            prayerRemindersEnabled = p[Keys.PRAYER_REMINDERS] ?: false,
            focusGuardPrayerBlock = p[Keys.FG_PRAYER_BLOCK] ?: false,
            focusGuardCountdownEnabled = p[Keys.FG_COUNTDOWN] ?: true,
            focusGuardCustomMessage = p[Keys.FG_MESSAGE] ?: "",
            focusDailyGoalMinutes = p[Keys.FOCUS_DAILY_GOAL] ?: 90,
            activeFocusRunning = p[Keys.FOCUS_RUNNING] ?: false,
            activeFocusMode = p[Keys.FOCUS_MODE] ?: "POMODORO",
            activeFocusEndEpochMs = p[Keys.FOCUS_END_MS] ?: 0L,
            activeFocusTotalSeconds = p[Keys.FOCUS_TOTAL_SEC] ?: 1500L,
            activeFocusPaused = p[Keys.FOCUS_PAUSED] ?: false,
            activeFocusRemainingOnPauseSec = p[Keys.FOCUS_PAUSED_REM] ?: 0L,
            activeFocusTaskId = p[Keys.FOCUS_TASK_ID] ?: -1L,
            activeFocusLessonId = p[Keys.FOCUS_LESSON_ID] ?: -1L,
            activeFocusReason = p[Keys.FOCUS_REASON] ?: "FOCUS",
            activeAudioTrackId = p[Keys.AUDIO_TRACK_ID] ?: -1L,
            activeAudioPositionMs = p[Keys.AUDIO_POS_MS] ?: 0L,
            podcastPlaybackSpeed = p[Keys.PODCAST_SPEED]?.toFloatOrNull() ?: 1.0f,
            autoBackupEnabled = p[Keys.AUTO_BACKUP] ?: false,
            lastBackupTimestamp = p[Keys.LAST_BACKUP_TS] ?: 0L,
            seededInitialData = p[Keys.SEEDED] ?: false,
            userName = p[Keys.USER_NAME] ?: "",
            userTagline = p[Keys.USER_TAGLINE] ?: "",
            userAvatarUri = p[Keys.USER_AVATAR_URI] ?: "",
            quoteIndex = p[Keys.QUOTE_INDEX] ?: 0,
            focusBackgroundUri = p[Keys.FOCUS_BG_URI] ?: "",
            migratedToNoxTheme = p[Keys.MIGRATED_NOX] ?: false
        )
    }

    suspend fun setLanguage(lang: String) = context.nexaDataStore.edit { it[Keys.LANGUAGE] = lang }
    suspend fun setActiveProfileId(id: Long) = context.nexaDataStore.edit { it[Keys.ACTIVE_PROFILE_ID] = id }
    suspend fun setGlobalEditMode(enabled: Boolean) = context.nexaDataStore.edit { it[Keys.GLOBAL_EDIT_MODE] = enabled }
    suspend fun addXp(delta: Int) = context.nexaDataStore.edit {
        val current = it[Keys.TOTAL_XP] ?: 0
        it[Keys.TOTAL_XP] = (current + delta).coerceAtLeast(0)
    }
    suspend fun setPin(hash: String, salt: String) = context.nexaDataStore.edit {
        it[Keys.PIN_HASH] = hash
        it[Keys.PIN_SALT] = salt
    }
    suspend fun setBiometricEnabled(enabled: Boolean) = context.nexaDataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    suspend fun setAutoLockMinutes(minutes: Int) = context.nexaDataStore.edit { it[Keys.AUTO_LOCK_MINUTES] = minutes }
    suspend fun setLockedSections(sections: Set<String>) = context.nexaDataStore.edit { it[Keys.LOCKED_SECTIONS] = sections }
    suspend fun setHideNotificationContent(hide: Boolean) = context.nexaDataStore.edit { it[Keys.HIDE_NOTIF_CONTENT] = hide }
    suspend fun setDnd(enabled: Boolean, startHour: Int, endHour: Int) = context.nexaDataStore.edit {
        it[Keys.DND_ENABLED] = enabled
        it[Keys.DND_START_HOUR] = startHour
        it[Keys.DND_END_HOUR] = endHour
    }
    suspend fun setNotificationChannels(sound: Boolean, vibration: Boolean) = context.nexaDataStore.edit {
        it[Keys.NOTIF_SOUND] = sound
        it[Keys.NOTIF_VIBRATION] = vibration
    }
    suspend fun setPrayerConfig(city: String, lat: Double, lon: Double, method: String) = context.nexaDataStore.edit {
        it[Keys.PRAYER_CITY] = city
        it[Keys.PRAYER_LAT] = lat
        it[Keys.PRAYER_LON] = lon
        it[Keys.PRAYER_METHOD] = method
    }
    suspend fun setPrayerReminders(enabled: Boolean) = context.nexaDataStore.edit { it[Keys.PRAYER_REMINDERS] = enabled }
    suspend fun setFocusGuardConfig(prayerBlock: Boolean, countdown: Boolean, message: String) = context.nexaDataStore.edit {
        it[Keys.FG_PRAYER_BLOCK] = prayerBlock
        it[Keys.FG_COUNTDOWN] = countdown
        it[Keys.FG_MESSAGE] = message
    }
    suspend fun setFocusDailyGoal(minutes: Int) = context.nexaDataStore.edit { it[Keys.FOCUS_DAILY_GOAL] = minutes }
    suspend fun saveActiveFocusState(
        running: Boolean,
        mode: String,
        endMs: Long,
        totalSec: Long,
        paused: Boolean,
        pausedRemSec: Long,
        taskId: Long,
        lessonId: Long,
        reason: String
    ) = context.nexaDataStore.edit {
        it[Keys.FOCUS_RUNNING] = running
        it[Keys.FOCUS_MODE] = mode
        it[Keys.FOCUS_END_MS] = endMs
        it[Keys.FOCUS_TOTAL_SEC] = totalSec
        it[Keys.FOCUS_PAUSED] = paused
        it[Keys.FOCUS_PAUSED_REM] = pausedRemSec
        it[Keys.FOCUS_TASK_ID] = taskId
        it[Keys.FOCUS_LESSON_ID] = lessonId
        it[Keys.FOCUS_REASON] = reason
    }
    suspend fun saveAudioPlaybackState(trackId: Long, posMs: Long) = context.nexaDataStore.edit {
        it[Keys.AUDIO_TRACK_ID] = trackId
        it[Keys.AUDIO_POS_MS] = posMs
    }
    suspend fun setPodcastSpeed(speed: Float) = context.nexaDataStore.edit { it[Keys.PODCAST_SPEED] = speed.toString() }
    suspend fun setAutoBackup(enabled: Boolean) = context.nexaDataStore.edit { it[Keys.AUTO_BACKUP] = enabled }
    suspend fun setLastBackupTs(ts: Long) = context.nexaDataStore.edit { it[Keys.LAST_BACKUP_TS] = ts }
    suspend fun markSeeded() = context.nexaDataStore.edit { it[Keys.SEEDED] = true }
    suspend fun setUserProfile(name: String, tagline: String, avatarUri: String) = context.nexaDataStore.edit {
        it[Keys.USER_NAME] = name
        it[Keys.USER_TAGLINE] = tagline
        it[Keys.USER_AVATAR_URI] = avatarUri
    }
    suspend fun setQuoteIndex(index: Int) = context.nexaDataStore.edit { it[Keys.QUOTE_INDEX] = index }
    suspend fun setFocusBackgroundUri(uri: String) = context.nexaDataStore.edit { it[Keys.FOCUS_BG_URI] = uri }
    suspend fun markMigratedToNoxTheme() = context.nexaDataStore.edit { it[Keys.MIGRATED_NOX] = true }
}
