package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DesignDao {
    @Query("SELECT * FROM design_profiles ORDER BY isBuiltIn DESC, id ASC")
    fun observeProfiles(): Flow<List<DesignProfileEntity>>

    @Query("SELECT * FROM design_profiles")
    suspend fun getAllProfiles(): List<DesignProfileEntity>

    @Query("SELECT * FROM design_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): DesignProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DesignProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: DesignProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: DesignProfileEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, COALESCE(dueAt, 9223372036854775807) ASC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_subjects ORDER BY createdAt ASC")
    fun observeSubjects(): Flow<List<StudySubjectEntity>>

    @Query("SELECT * FROM study_subjects")
    suspend fun getAllSubjectsSync(): List<StudySubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: StudySubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: StudySubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: StudySubjectEntity)

    @Query("SELECT * FROM study_lessons ORDER BY importance DESC, COALESCE(scheduledAt, 9223372036854775807) ASC")
    fun observeAllLessons(): Flow<List<StudyLessonEntity>>

    @Query("SELECT * FROM study_lessons WHERE subjectId = :subjectId ORDER BY importance DESC, createdAt ASC")
    fun observeLessonsBySubject(subjectId: Long): Flow<List<StudyLessonEntity>>

    @Query("SELECT * FROM study_lessons")
    suspend fun getAllLessonsSync(): List<StudyLessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: StudyLessonEntity): Long

    @Update
    suspend fun updateLesson(lesson: StudyLessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: StudyLessonEntity)

    @Query("SELECT * FROM study_flashcards ORDER BY nextReviewAt ASC")
    fun observeAllFlashcards(): Flow<List<StudyFlashcardEntity>>

    @Query("SELECT * FROM study_flashcards")
    suspend fun getAllFlashcardsSync(): List<StudyFlashcardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: StudyFlashcardEntity): Long

    @Update
    suspend fun updateFlashcard(card: StudyFlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(card: StudyFlashcardEntity)

    @Query("SELECT * FROM study_review_logs ORDER BY createdAt DESC")
    fun observeReviewLogs(): Flow<List<StudyReviewLogEntity>>

    @Query("SELECT * FROM study_review_logs")
    suspend fun getAllReviewLogsSync(): List<StudyReviewLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLog(log: StudyReviewLogEntity): Long

    @Query("SELECT * FROM study_schedule_slots ORDER BY dayOfWeek ASC, startMinute ASC")
    fun observeScheduleSlots(): Flow<List<StudyScheduleSlotEntity>>

    @Query("SELECT * FROM study_schedule_slots")
    suspend fun getAllScheduleSlotsSync(): List<StudyScheduleSlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleSlot(slot: StudyScheduleSlotEntity): Long

    @Delete
    suspend fun deleteScheduleSlot(slot: StudyScheduleSlotEntity)

    @Query("SELECT * FROM study_grades ORDER BY recordedAt DESC")
    fun observeGrades(): Flow<List<StudyGradeEntity>>

    @Query("SELECT * FROM study_grades")
    suspend fun getAllGradesSync(): List<StudyGradeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: StudyGradeEntity): Long

    @Delete
    suspend fun deleteGrade(grade: StudyGradeEntity)

    @Query("SELECT * FROM study_goals ORDER BY isCompleted ASC, id DESC")
    fun observeGoals(): Flow<List<StudyGoalEntity>>

    @Query("SELECT * FROM study_goals")
    suspend fun getAllGoalsSync(): List<StudyGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: StudyGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: StudyGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: StudyGoalEntity)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, createdAt ASC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsSync(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM habit_logs ORDER BY dateIso DESC")
    fun observeHabitLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs")
    suspend fun getAllHabitLogsSync(): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateIso = :dateIso LIMIT 1")
    suspend fun getLogForDate(habitId: Long, dateIso: String): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity): Long
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun observeFocusSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions")
    suspend fun getAllFocusSessionsSync(): List<FocusSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateFocusSession(session: FocusSessionEntity)

    @Query("SELECT * FROM blocked_apps ORDER BY appLabel ASC")
    fun observeBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps")
    suspend fun getBlockedAppsSync(): List<BlockedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlockedApp(app: BlockedAppEntity)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedAppEntity)
}

@Dao
interface LifeDao {
    @Query("SELECT * FROM mood_entries ORDER BY createdAt DESC")
    fun observeMoodEntries(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries")
    suspend fun getAllMoodEntriesSync(): List<MoodEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: MoodEntryEntity): Long

    @Delete
    suspend fun deleteMoodEntry(entry: MoodEntryEntity)

    @Query("SELECT * FROM adhkar_progress WHERE dateIso = :dateIso")
    fun observeAdhkarProgress(dateIso: String): Flow<List<AdhkarProgressEntity>>

    @Query("SELECT COUNT(*) FROM adhkar_progress WHERE currentCount >= targetCount")
    suspend fun getCompletedAdhkarCountSync(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAdhkarProgress(progress: AdhkarProgressEntity)

    @Query("SELECT * FROM dua_favorites")
    fun observeDuaFavorites(): Flow<List<DuaFavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDuaFavorite(fav: DuaFavoriteEntity)
}

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library_items ORDER BY COALESCE(lastReadAt, 0) DESC, id ASC")
    fun observeLibraryItems(): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM library_items")
    suspend fun getAllLibraryItemsSync(): List<LibraryItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryItem(item: LibraryItemEntity): Long

    @Update
    suspend fun updateLibraryItem(item: LibraryItemEntity)

    @Delete
    suspend fun deleteLibraryItem(item: LibraryItemEntity)

    @Query("SELECT * FROM saved_quotes ORDER BY createdAt DESC")
    fun observeQuotes(): Flow<List<SavedQuoteEntity>>

    @Query("SELECT * FROM saved_quotes")
    suspend fun getAllQuotesSync(): List<SavedQuoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: SavedQuoteEntity): Long

    @Delete
    suspend fun deleteQuote(quote: SavedQuoteEntity)

    @Query("SELECT * FROM podcast_episodes ORDER BY COALESCE(lastPlayedAt, 0) DESC, id ASC")
    fun observePodcastEpisodes(): Flow<List<PodcastEpisodeEntity>>

    @Query("SELECT * FROM podcast_episodes")
    suspend fun getAllPodcastEpisodesSync(): List<PodcastEpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPodcastEpisode(ep: PodcastEpisodeEntity): Long

    @Update
    suspend fun updatePodcastEpisode(ep: PodcastEpisodeEntity)

    @Delete
    suspend fun deletePodcastEpisode(ep: PodcastEpisodeEntity)
}

@Dao
interface AudioDao {
    @Query("SELECT * FROM audio_tracks ORDER BY id ASC")
    fun observeAudioTracks(): Flow<List<AudioTrackEntity>>

    @Query("SELECT * FROM audio_tracks")
    suspend fun getAllAudioTracksSync(): List<AudioTrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioTrack(track: AudioTrackEntity): Long

    @Update
    suspend fun updateAudioTrack(track: AudioTrackEntity)

    @Delete
    suspend fun deleteAudioTrack(track: AudioTrackEntity)
}

@Dao
interface SystemDao {
    @Query("SELECT * FROM app_notifications ORDER BY createdAt DESC")
    fun observeNotifications(): Flow<List<AppNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotificationEntity): Long

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    @Query("DELETE FROM app_notifications")
    suspend fun clearNotifications()

    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, xpReward DESC")
    fun observeAchievements(): Flow<List<AchievementProgressEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievementsSync(): List<AchievementProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAchievement(item: AchievementProgressEntity)

    @Query("SELECT * FROM backup_records ORDER BY createdAt DESC")
    fun observeBackups(): Flow<List<BackupRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(record: BackupRecordEntity): Long

    @Delete
    suspend fun deleteBackup(record: BackupRecordEntity)
}
