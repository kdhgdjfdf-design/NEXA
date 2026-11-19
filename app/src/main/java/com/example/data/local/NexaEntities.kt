package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "design_profiles")
data class DesignProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isBuiltIn: Boolean = false,
    val paletteKey: String = "NOX_ESPRESSO", // NOX_ESPRESSO, SERENE_SAGE, WARM_SAND, MIDNIGHT_FOCUS, OCEAN_MIST, BOTANICAL_GOLD
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val fontKey: String = "CAIRO", // CAIRO, TAJAWAL, AMIRI, READEX_PRO
    val iconStyle: String = "ROUNDED", // ROUNDED, SHARP, MINIMAL
    val homeBannerUri: String = "",
    val focusBlockImageUri: String = "",
    val sectionImagesJson: String = "{}", // JSON map of sectionKey -> uri
    val cardConfigJson: String = "", // JSON list of CardLayoutConfig
    val customShortcutsJson: String = "[]" // JSON list of CustomShortcut
)

@Serializable
data class CardLayoutConfig(
    val id: String,
    val visible: Boolean = true,
    val size: String = "STANDARD", // COMPACT, STANDARD, EXPANDED
    val order: Int = 0
)

@Serializable
data class CustomShortcut(
    val id: String,
    val title: String,
    val targetRoute: String,
    val iconKey: String = "STAR"
)

@Serializable
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val notes: String = "",
    val category: String = "General",
    val priority: Int = 1, // 0=Low, 1=Medium, 2=High, 3=Urgent
    val dueAt: Long? = null,
    val reminderAt: Long? = null,
    val snoozedUntil: Long? = null,
    val recurrenceType: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY, CUSTOM
    val recurrenceIntervalDays: Int = 1,
    val attachmentUri: String = "",
    val attachmentName: String = "",
    val focusGuardOnDue: Boolean = false,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "study_subjects")
data class StudySubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val coefficient: Double = 1.0,
    val targetAverage: Double = 15.0,
    val colorKey: String = "TEAL",
    val bannerUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "study_lessons")
data class StudyLessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val scheduledAt: Long? = null,
    val importance: Int = 1, // 0=Normal, 1=Important, 2=ExamCritical
    val pdfUri: String = "",
    val pdfName: String = "",
    val youtubeUrl: String = "",
    val sideNoteText: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "study_flashcards")
data class StudyFlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val lessonId: Long? = null,
    val front: String,
    val back: String,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReviewAt: Long = System.currentTimeMillis(),
    val lastRecallLevel: Int = 0 // 0=New, 1=Again, 2=Hard, 3=Good, 4=Easy
)

@Serializable
@Entity(tableName = "study_review_logs")
data class StudyReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val lessonId: Long? = null,
    val mode: String, // FLASHCARD, TEST_MEMORY
    val scorePercent: Int,
    val totalItems: Int,
    val correctItems: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "study_schedule_slots")
data class StudyScheduleSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 1=Monday .. 7=Sunday
    val startMinute: Int, // minutes from midnight
    val endMinute: Int,
    val subjectId: Long? = null,
    val title: String,
    val isException: Boolean = false,
    val exceptionDateIso: String = ""
)

@Serializable
@Entity(tableName = "study_grades")
data class StudyGradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val title: String,
    val score: Double,
    val maxScore: Double = 20.0,
    val weight: Double = 1.0,
    val recordedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "study_goals")
data class StudyGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetMinutes: Int,
    val completedMinutes: Int = 0,
    val deadlineAt: Long? = null,
    val isCompleted: Boolean = false
)

@Serializable
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val routineName: String = "Morning", // Morning, Study, Evening, Custom
    val sortOrder: Int = 0,
    val frequencyType: String = "DAILY", // DAILY, WEEKLY, MONTHLY, CUSTOM
    val customIntervalDays: Int = 1,
    val measurementType: String = "COMPLETION", // COMPLETION, COUNT, DURATION
    val targetValue: Int = 1,
    val unitLabel: String = "",
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val notes: String = "",
    val imageUri: String = "",
    val streakCount: Int = 0,
    val bestStreak: Int = 0,
    val restDaysCsv: String = "", // e.g. "5,6" (Friday, Saturday)
    val lastCompletedDateIso: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val dateIso: String,
    val valueAchieved: Int = 0,
    val isCompleted: Boolean = false,
    val isRestDay: Boolean = false,
    val note: String = ""
)

@Serializable
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String = "POMODORO", // POMODORO, CUSTOM, OPEN
    val plannedMinutes: Int = 25,
    val elapsedFocusSeconds: Long = 0,
    val pausedSeconds: Long = 0,
    val linkedTaskId: Long? = null,
    val linkedLessonId: Long? = null,
    val linkedTitle: String = "",
    val ambientSound: String = "NONE", // NONE, RAIN, WARM_NOISE, CHIME, LOCAL_FILE
    val status: String = "COMPLETED", // RUNNING, PAUSED, COMPLETED, CANCELLED
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)

@Serializable
@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val dailyLimitMinutes: Int = 0, // 0 = no daily limit
    val blockInFocus: Boolean = true,
    val blockInTasks: Boolean = false,
    val blockInPrayer: Boolean = false,
    val isEnabled: Boolean = true
)

@Serializable
@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emotionsCsv: String, // comma-separated emotion keys
    val noteText: String = "",
    val dateIso: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "adhkar_progress")
data class AdhkarProgressEntity(
    @PrimaryKey val keyId: String, // dateIso_category_dhikrId
    val category: String,
    val dhikrId: String,
    val dateIso: String,
    val currentCount: Int = 0,
    val targetCount: Int = 1
)

@Serializable
@Entity(tableName = "dua_favorites")
data class DuaFavoriteEntity(
    @PrimaryKey val duaId: String,
    val isFavorite: Boolean = true
)

@Serializable
@Entity(tableName = "library_items")
data class LibraryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val category: String, // BOOK, NOVEL, PODCAST
    val shelf: String = "WANT_TO_READ", // READING, COMPLETED, WANT_TO_READ, CUSTOM
    val customShelfName: String = "",
    val isFavorite: Boolean = false,
    val coverKey: String = "DEFAULT",
    val localFileUri: String = "",
    val contentText: String = "",
    val sourceInfo: String = "",
    val totalPages: Int = 1,
    val currentPage: Int = 0,
    val lastReadAt: Long? = null,
    val isDownloaded: Boolean = true
)

@Serializable
@Entity(tableName = "saved_quotes")
data class SavedQuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val bookTitle: String,
    val author: String,
    val pageNumber: Int,
    val quoteText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "podcast_episodes")
data class PodcastEpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val showId: String,
    val showTitle: String,
    val showAuthor: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val localFileUri: String = "",
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val playlistTag: String = "",
    val lastPlayedAt: Long? = null
)

@Serializable
@Entity(tableName = "audio_tracks")
data class AudioTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String = "",
    val playlistName: String = "CUSTOM", // CUSTOM, QURAN, ADHKAR, CALM
    val uriString: String,
    val durationMs: Long = 0L,
    val lastPositionMs: Long = 0L,
    val isBuiltInCalm: Boolean = false
)

@Serializable
@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val section: String, // TASKS, STUDY, HABITS, FOCUS, LIFE, SYSTEM
    val title: String,
    val body: String,
    val isSensitive: Boolean = false,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "achievements")
data class AchievementProgressEntity(
    @PrimaryKey val id: String,
    val category: String, // GENERAL, STUDY, TASKS, HABITS, FOCUS, LIFE, LIBRARY
    val titleKey: String,
    val descKey: String,
    val progress: Int = 0,
    val target: Int = 1,
    val xpReward: Int = 50,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

@Serializable
@Entity(tableName = "backup_records")
data class BackupRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePathOrUri: String,
    val createdAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0,
    val isAuto: Boolean = false,
    val isEncrypted: Boolean = true,
    val status: String = "VERIFIED"
)
