package com.example.ui.screens.life

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaScreenTitleRow
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaSegmentedTabs
import com.example.ui.components.NexaTintedBanner
import com.example.ui.screens.habits.HabitsScreen

/**
 * Dedicated "حياتي" (My Life) Hub combining Habits & Routines, Serenity Reflection Journal,
 * and Personal Growth Stats/Badges under a calm personal sanctuary.
 */
@Composable
fun MyLifeHubScreen(
    prefs: NexaPrefsState,
    habits: List<HabitEntity>,
    habitLogs: List<HabitLogEntity>,
    moodEntries: List<MoodEntryEntity>,
    achievements: List<AchievementProgressEntity>,
    tasks: List<TaskEntity>,
    focusSessions: List<FocusSessionEntity>,
    onSaveHabit: (HabitEntity) -> Unit,
    onDeleteHabit: (HabitEntity) -> Unit,
    onSwapHabitOrder: (HabitEntity, HabitEntity) -> Unit,
    onLogHabitProgress: (HabitEntity, Int, Boolean) -> Unit,
    onCopyHabitPhoto: (Uri) -> String,
    onAddMoodEntry: (Set<String>, String) -> Unit
) {
    val strings = LocalNexaStrings.current
    var activeTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("my_life_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NexaScreenTitleRow(
                title = strings.life,
                icon = Icons.Rounded.Spa,
                accent = NexaSectionAccents.life
            )
            val tabs = listOf(strings.habitsAndRoutines, strings.serenity)
            NexaSegmentedTabs(
                tabs = tabs,
                selectedIndex = activeTab,
                onSelect = { activeTab = it }
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeTab) {
                0 -> HabitsScreen(
                    habits = habits,
                    logs = habitLogs,
                    onSaveHabit = onSaveHabit,
                    onDeleteHabit = onDeleteHabit,
                    onSwapOrder = onSwapHabitOrder,
                    onLogProgress = onLogHabitProgress,
                    onCopyPhoto = onCopyHabitPhoto
                )
                1 -> Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    SerenityTab(entries = moodEntries, onAddMood = onAddMoodEntry)
                }

            }
        }
    }
}

@Composable
private fun LifeGrowthTab(
    prefs: NexaPrefsState,
    achievements: List<AchievementProgressEntity>,
    tasks: List<TaskEntity>,
    focusSessions: List<FocusSessionEntity>,
    habitLogs: List<HabitLogEntity>
) {
    val strings = LocalNexaStrings.current
    val level = XpAndStatsEngine.computeLevel(prefs.totalXp)
    val stats = remember(tasks, focusSessions, habitLogs) {
        XpAndStatsEngine.computeSelfComparison(tasks, focusSessions, habitLogs)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.EmojiEvents,
                            tint = NexaSectionAccents.badges,
                            size = 44.dp,
                            iconSize = 22.dp,
                            cornerRadius = 14.dp
                        )
                        Text(
                            "${strings.levelLabel} ${level.level} • ${prefs.totalXp} ${strings.xpLabel}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(progress = { level.progressFraction }, modifier = Modifier.fillMaxWidth())
                    Text("${level.currentXpInLevel} / ${level.nextLevelXp} ${strings.xpLabel}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            NexaTintedBanner(
                text = "${strings.selfComparisonNote}\n" +
                    "${strings.focus7dLabel}: ${stats.thisWeekFocusMinutes} ${strings.minutesUnit} (${strings.vsPreviousLabel}: ${stats.lastWeekFocusMinutes} ${strings.minutesUnit})\n" +
                    "${strings.tasksDone7dLabel}: ${stats.thisWeekTasksDone} (${strings.vsPreviousLabel}: ${stats.lastWeekTasksDone}) • " +
                    "${strings.habitsLoggedLabel}: ${stats.thisWeekHabitsDone}",
                icon = Icons.Rounded.TrendingUp,
                accent = NexaSectionAccents.life
            )
        }
        items(achievements, key = { it.id }) { ach ->
            AchievementBadgeCard(ach = ach)
        }
    }
}
