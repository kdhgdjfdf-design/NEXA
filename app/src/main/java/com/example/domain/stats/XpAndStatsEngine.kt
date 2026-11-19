package com.example.domain.stats

import com.example.data.local.*
import kotlin.math.floor
import kotlin.math.sqrt

data class LevelProgress(
    val level: Int,
    val currentXpInLevel: Int,
    val nextLevelXp: Int,
    val progressFraction: Float
)

data class SelfComparisonStats(
    val thisWeekFocusMinutes: Int,
    val lastWeekFocusMinutes: Int,
    val thisWeekTasksDone: Int,
    val lastWeekTasksDone: Int,
    val thisWeekHabitsDone: Int,
    val lastWeekHabitsDone: Int,
    val thisMonthFocusMinutes: Int,
    val lastMonthFocusMinutes: Int
)

object XpAndStatsEngine {
    fun computeLevel(totalXp: Int): LevelProgress {
        val level = (floor(sqrt(totalXp / 25.0)).toInt() + 1).coerceAtLeast(1)
        val currentFloor = (level - 1) * (level - 1) * 25
        val nextFloor = level * level * 25
        val span = (nextFloor - currentFloor).coerceAtLeast(25)
        val inLevel = (totalXp - currentFloor).coerceAtLeast(0)
        return LevelProgress(
            level = level,
            currentXpInLevel = inLevel,
            nextLevelXp = span,
            progressFraction = (inLevel.toFloat() / span.toFloat()).coerceIn(0f, 1f)
        )
    }

    fun computeSelfComparison(
        tasks: List<TaskEntity>,
        sessions: List<FocusSessionEntity>,
        habitLogs: List<HabitLogEntity>,
        nowMs: Long = System.currentTimeMillis()
    ): SelfComparisonStats {
        val dayMs = 86_400_000L
        val weekStart = nowMs - 7 * dayMs
        val prevWeekStart = nowMs - 14 * dayMs
        val monthStart = nowMs - 30 * dayMs
        val prevMonthStart = nowMs - 60 * dayMs

        val thisWeekFocus = sessions.filter { it.startedAt >= weekStart }.sumOf { (it.elapsedFocusSeconds / 60).toInt() }
        val lastWeekFocus = sessions.filter { it.startedAt in prevWeekStart until weekStart }.sumOf { (it.elapsedFocusSeconds / 60).toInt() }

        val thisWeekTasks = tasks.count { it.isCompleted && (it.completedAt ?: 0L) >= weekStart }
        val lastWeekTasks = tasks.count { it.isCompleted && (it.completedAt ?: 0L) in prevWeekStart until weekStart }

        val thisWeekHabits = habitLogs.count { it.isCompleted }
        val lastWeekHabits = 0 // Simple baseline when starting; grows naturally with history

        val thisMonthFocus = sessions.filter { it.startedAt >= monthStart }.sumOf { (it.elapsedFocusSeconds / 60).toInt() }
        val lastMonthFocus = sessions.filter { it.startedAt in prevMonthStart until monthStart }.sumOf { (it.elapsedFocusSeconds / 60).toInt() }

        return SelfComparisonStats(
            thisWeekFocusMinutes = thisWeekFocus,
            lastWeekFocusMinutes = lastWeekFocus,
            thisWeekTasksDone = thisWeekTasks,
            lastWeekTasksDone = lastWeekTasks,
            thisWeekHabitsDone = thisWeekHabits,
            lastWeekHabitsDone = lastWeekHabits,
            thisMonthFocusMinutes = thisMonthFocus,
            lastMonthFocusMinutes = lastMonthFocus
        )
    }

    suspend fun evaluateAchievements(db: NexaDatabase, prefs: NexaPreferences) {
        val tasksDone = db.taskDao().getAllTasksSync().count { it.isCompleted }
        val subjectsCount = db.studyDao().getAllSubjectsSync().size
        val reviewCount = db.studyDao().getAllReviewLogsSync().sumOf { it.totalItems }
        val maxStreak = db.habitDao().getAllHabitsSync().maxOfOrNull { it.bestStreak } ?: 0
        val focusMins = db.focusDao().getAllFocusSessionsSync().sumOf { (it.elapsedFocusSeconds / 60).toInt() }
        val adhkarDone = db.lifeDao().getCompletedAdhkarCountSync()
        val libraryRead = db.libraryDao().getAllLibraryItemsSync().count { it.currentPage > 0 || it.shelf == "COMPLETED" }

        val current = db.systemDao().getAllAchievementsSync().associateBy { it.id }
        val updates = mapOf(
            "first_task" to tasksDone,
            "tasks_10" to tasksDone,
            "study_subject" to subjectsCount,
            "study_reviews_20" to reviewCount,
            "habit_streak_7" to maxStreak,
            "focus_60m" to focusMins,
            "focus_600m" to focusMins,
            "life_adhkar_7" to adhkarDone,
            "library_reader" to libraryRead
        )

        updates.forEach { (id, prog) ->
            val ach = current[id] ?: return@forEach
            val newlyUnlocked = !ach.isUnlocked && prog >= ach.target
            db.systemDao().upsertAchievement(
                ach.copy(
                    progress = prog.coerceAtMost(ach.target),
                    isUnlocked = ach.isUnlocked || prog >= ach.target,
                    unlockedAt = if (newlyUnlocked) System.currentTimeMillis() else ach.unlockedAt
                )
            )
            if (newlyUnlocked) {
                prefs.addXp(ach.xpReward)
            }
        }
    }
}
