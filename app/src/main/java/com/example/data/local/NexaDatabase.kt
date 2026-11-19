package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DesignProfileEntity::class,
        TaskEntity::class,
        StudySubjectEntity::class,
        StudyLessonEntity::class,
        StudyFlashcardEntity::class,
        StudyReviewLogEntity::class,
        StudyScheduleSlotEntity::class,
        StudyGradeEntity::class,
        StudyGoalEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        FocusSessionEntity::class,
        BlockedAppEntity::class,
        MoodEntryEntity::class,
        AdhkarProgressEntity::class,
        DuaFavoriteEntity::class,
        LibraryItemEntity::class,
        SavedQuoteEntity::class,
        PodcastEpisodeEntity::class,
        AudioTrackEntity::class,
        AppNotificationEntity::class,
        AchievementProgressEntity::class,
        BackupRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexaDatabase : RoomDatabase() {
    abstract fun designDao(): DesignDao
    abstract fun taskDao(): TaskDao
    abstract fun studyDao(): StudyDao
    abstract fun habitDao(): HabitDao
    abstract fun focusDao(): FocusDao
    abstract fun lifeDao(): LifeDao
    abstract fun libraryDao(): LibraryDao
    abstract fun audioDao(): AudioDao
    abstract fun systemDao(): SystemDao

    companion object {
        @Volatile
        private var INSTANCE: NexaDatabase? = null

        fun getInstance(context: Context): NexaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NexaDatabase::class.java,
                    "nexa_offline.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
