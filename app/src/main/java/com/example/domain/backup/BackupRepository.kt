package com.example.domain.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.*
import com.example.domain.security.CryptoAndLockManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class NexaBackupSnapshot(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val designProfiles: List<DesignProfileEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val subjects: List<StudySubjectEntity> = emptyList(),
    val lessons: List<StudyLessonEntity> = emptyList(),
    val flashcards: List<StudyFlashcardEntity> = emptyList(),
    val reviewLogs: List<StudyReviewLogEntity> = emptyList(),
    val scheduleSlots: List<StudyScheduleSlotEntity> = emptyList(),
    val grades: List<StudyGradeEntity> = emptyList(),
    val goals: List<StudyGoalEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val blockedApps: List<BlockedAppEntity> = emptyList(),
    val moodEntries: List<MoodEntryEntity> = emptyList(),
    val libraryItems: List<LibraryItemEntity> = emptyList(),
    val savedQuotes: List<SavedQuoteEntity> = emptyList(),
    val podcastEpisodes: List<PodcastEpisodeEntity> = emptyList(),
    val audioTracks: List<AudioTrackEntity> = emptyList(),
    val achievements: List<AchievementProgressEntity> = emptyList()
)

class BackupRepository(
    private val context: Context,
    private val db: NexaDatabase,
    private val prefs: NexaPreferences
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun buildEncryptedBackup(): Pair<String, Int> = withContext(Dispatchers.IO) {
        val snapshot = NexaBackupSnapshot(
            designProfiles = db.designDao().getAllProfiles(),
            tasks = db.taskDao().getAllTasksSync(),
            subjects = db.studyDao().getAllSubjectsSync(),
            lessons = db.studyDao().getAllLessonsSync(),
            flashcards = db.studyDao().getAllFlashcardsSync(),
            reviewLogs = db.studyDao().getAllReviewLogsSync(),
            scheduleSlots = db.studyDao().getAllScheduleSlotsSync(),
            grades = db.studyDao().getAllGradesSync(),
            goals = db.studyDao().getAllGoalsSync(),
            habits = db.habitDao().getAllHabitsSync(),
            habitLogs = db.habitDao().getAllHabitLogsSync(),
            focusSessions = db.focusDao().getAllFocusSessionsSync(),
            blockedApps = db.focusDao().getBlockedAppsSync(),
            moodEntries = db.lifeDao().getAllMoodEntriesSync(),
            libraryItems = db.libraryDao().getAllLibraryItemsSync(),
            savedQuotes = db.libraryDao().getAllQuotesSync(),
            podcastEpisodes = db.libraryDao().getAllPodcastEpisodesSync(),
            audioTracks = db.audioDao().getAllAudioTracksSync(),
            achievements = db.systemDao().getAllAchievementsSync()
        )
        val count = snapshot.tasks.size + snapshot.subjects.size + snapshot.lessons.size +
            snapshot.habits.size + snapshot.focusSessions.size + snapshot.moodEntries.size +
            snapshot.libraryItems.size + snapshot.savedQuotes.size
        val plain = json.encodeToString(snapshot)
        val encrypted = CryptoAndLockManager.encryptBackup(plain)
        encrypted to count
    }

    suspend fun createLocalEncryptedBackup(isAuto: Boolean = false): BackupRecordEntity = withContext(Dispatchers.IO) {
        val (encrypted, count) = buildEncryptedBackup()
        val dir = File(context.filesDir, "backups").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "nexa_backup_$stamp.nexabak"
        val file = File(dir, fileName)
        file.writeText(encrypted)
        val now = System.currentTimeMillis()
        val record = BackupRecordEntity(
            fileName = fileName,
            filePathOrUri = file.absolutePath,
            createdAt = now,
            itemCount = count,
            isAuto = isAuto,
            isEncrypted = true,
            status = "VERIFIED"
        )
        db.systemDao().insertBackup(record)
        prefs.setLastBackupTs(now)
        record
    }

    suspend fun exportBackupToUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val (encrypted, count) = buildEncryptedBackup()
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(encrypted.toByteArray(Charsets.UTF_8))
            } ?: return@withContext false
            val now = System.currentTimeMillis()
            db.systemDao().insertBackup(
                BackupRecordEntity(
                    fileName = "Exported_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.nexabak",
                    filePathOrUri = uri.toString(),
                    createdAt = now,
                    itemCount = count,
                    isAuto = false,
                    isEncrypted = true,
                    status = "EXPORTED"
                )
            )
            prefs.setLastBackupTs(now)
            true
        }.getOrDefault(false)
    }

    suspend fun restoreFromEncryptedPayload(payload: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val plainJson = CryptoAndLockManager.decryptBackup(payload.trim())
            val snapshot = json.decodeFromString<NexaBackupSnapshot>(plainJson)
            snapshot.designProfiles.forEach { db.designDao().insertProfile(it) }
            snapshot.tasks.forEach { db.taskDao().insertTask(it) }
            snapshot.subjects.forEach { db.studyDao().insertSubject(it) }
            snapshot.lessons.forEach { db.studyDao().insertLesson(it) }
            snapshot.flashcards.forEach { db.studyDao().insertFlashcard(it) }
            snapshot.reviewLogs.forEach { db.studyDao().insertReviewLog(it) }
            snapshot.scheduleSlots.forEach { db.studyDao().insertScheduleSlot(it) }
            snapshot.grades.forEach { db.studyDao().insertGrade(it) }
            snapshot.goals.forEach { db.studyDao().insertGoal(it) }
            snapshot.habits.forEach { db.habitDao().insertHabit(it) }
            snapshot.habitLogs.forEach { db.habitDao().insertHabitLog(it) }
            snapshot.focusSessions.forEach { db.focusDao().insertFocusSession(it) }
            snapshot.blockedApps.forEach { db.focusDao().upsertBlockedApp(it) }
            snapshot.moodEntries.forEach { db.lifeDao().insertMoodEntry(it) }
            snapshot.libraryItems.forEach { db.libraryDao().insertLibraryItem(it) }
            snapshot.savedQuotes.forEach { db.libraryDao().insertQuote(it) }
            snapshot.podcastEpisodes.forEach { db.libraryDao().insertPodcastEpisode(it) }
            snapshot.audioTracks.forEach { db.audioDao().insertAudioTrack(it) }
            snapshot.achievements.forEach { db.systemDao().upsertAchievement(it) }
            snapshot.tasks.size + snapshot.habits.size + snapshot.subjects.size + snapshot.libraryItems.size
        }
    }

    suspend fun restoreFromFileOrUri(pathOrUri: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = if (pathOrUri.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(pathOrUri))?.bufferedReader()?.use { it.readText() }
                    ?: error("Cannot read backup URI")
            } else {
                File(pathOrUri).readText()
            }
            restoreFromEncryptedPayload(payload).getOrThrow()
        }
    }
}
