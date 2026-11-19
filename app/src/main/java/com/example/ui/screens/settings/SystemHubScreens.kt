package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.domain.stats.XpAndStatsEngine
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaEmptyState
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaSegmentedTabs
import com.example.ui.components.NexaTintedBanner
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    prefs: NexaPrefsState,
    notifications: List<AppNotificationEntity>,
    onBack: () -> Unit,
    onMarkRead: (Long) -> Unit,
    onMarkAllRead: () -> Unit,
    onClearAll: () -> Unit,
    onUpdateChannels: (Boolean, Boolean) -> Unit,
    onUpdateDnd: (Boolean, Int, Int) -> Unit
) {
    val strings = LocalNexaStrings.current
    var filterSection by remember { mutableStateOf("ALL") }
    val fmt = remember { SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()) }

    val sections = listOf("ALL", "TASKS", "STUDY", "HABITS", "FOCUS", "LIFE", "SYSTEM")
    val filtered = notifications.filter { filterSection == "ALL" || it.section == filterSection }
    val accent = NexaSectionAccents.badges

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("notifications_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("notifications_back_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.back,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                NexaFilledIconBadge(
                    icon = Icons.Rounded.NotificationsActive,
                    tint = accent,
                    size = 42.dp,
                    iconSize = 20.dp,
                    cornerRadius = 13.dp
                )
                Text(
                    strings.notifications,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onMarkAllRead) {
                    Text(strings.markAllReadLabel, fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onClearAll) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = strings.delete)
                }
            }
        }

        NexaCard {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.soundLabel, fontWeight = FontWeight.Medium)
                    Switch(checked = prefs.notificationSound, onCheckedChange = { onUpdateChannels(it, prefs.notificationVibration) })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.vibrationLabel, fontWeight = FontWeight.Medium)
                    Switch(checked = prefs.notificationVibration, onCheckedChange = { onUpdateChannels(prefs.notificationSound, it) })
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${strings.dndLabel} (${prefs.dndStartHour}:00 - ${prefs.dndEndHour}:00)", fontWeight = FontWeight.Medium)
                    Switch(checked = prefs.dndEnabled, onCheckedChange = { onUpdateDnd(it, prefs.dndStartHour, prefs.dndEndHour) })
                }
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sections) { sec ->
                FilterChip(
                    selected = filterSection == sec,
                    onClick = { filterSection = sec },
                    label = { Text(strings.sectionDisplayName(sec)) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            if (filtered.isEmpty()) {
                item {
                    NexaEmptyState(
                        message = strings.noNewNotifications,
                        icon = Icons.Rounded.NotificationsNone,
                        accent = accent
                    )
                }
            } else {
                items(filtered, key = { it.id }) { notif ->
                    NexaCard(
                        onClick = { onMarkRead(notif.id) },
                        borderColor = if (!notif.isRead) accent.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NexaFilledIconBadge(
                                icon = if (notif.isRead) Icons.Rounded.NotificationsNone else Icons.Rounded.NotificationsActive,
                                tint = if (notif.isRead) MaterialTheme.colorScheme.outline else accent,
                                size = 40.dp,
                                iconSize = 20.dp,
                                cornerRadius = 12.dp
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    notif.title,
                                    fontWeight = if (notif.isRead) FontWeight.Medium else FontWeight.Bold
                                )
                                Text(
                                    "${strings.sectionDisplayName(notif.section)} • ${fmt.format(Date(notif.createdAt))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!notif.isRead) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = accent.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        strings.unreadBadgeSuffix,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsScreen(
    prefs: NexaPrefsState,
    achievements: List<AchievementProgressEntity>,
    tasks: List<TaskEntity>,
    focusSessions: List<FocusSessionEntity>,
    habitLogs: List<HabitLogEntity>,
    onBack: () -> Unit,
    initialTab: Int = 0
) {
    val strings = LocalNexaStrings.current
    var activeTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val level = XpAndStatsEngine.computeLevel(prefs.totalXp)
    val stats = remember(tasks, focusSessions, habitLogs) {
        XpAndStatsEngine.computeSelfComparison(tasks, focusSessions, habitLogs)
    }
    val accent = NexaSectionAccents.badges

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("achievements_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = onBack,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("achievements_back_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                }
            }
            NexaFilledIconBadge(
                icon = Icons.Rounded.EmojiEvents,
                tint = accent,
                size = 42.dp,
                iconSize = 20.dp,
                cornerRadius = 13.dp
            )
            Text(
                strings.achievements,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        NexaSegmentedTabs(
            tabs = listOf(strings.badges, strings.statistics),
            selectedIndex = activeTab,
            onSelect = { activeTab = it },
            testTagPrefix = "achievements_tab"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                NexaCard(
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NexaFilledIconBadge(
                                icon = Icons.Rounded.MilitaryTech,
                                tint = MaterialTheme.colorScheme.primary,
                                size = 46.dp,
                                iconSize = 24.dp,
                                cornerRadius = 14.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${strings.levelLabel} ${level.level}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${prefs.totalXp} ${strings.xpLabel}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = accent.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    "${level.currentXpInLevel} / ${level.nextLevelXp}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { level.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            if (activeTab == 1) {
                item {
                    NexaTintedBanner(
                        text = strings.selfComparisonNote,
                        icon = Icons.Rounded.Insights,
                        accent = accent
                    )
                }
                item {
                    NexaCard {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatComparisonRow(
                                icon = Icons.Rounded.Timer,
                                tint = NexaSectionAccents.focus,
                                label = strings.focus7dLabel,
                                value = "${stats.thisWeekFocusMinutes} ${strings.minutesUnit}",
                                vsPrev = "${strings.vsPreviousLabel}: ${stats.lastWeekFocusMinutes} ${strings.minutesUnit}"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            StatComparisonRow(
                                icon = Icons.Rounded.CalendarMonth,
                                tint = NexaSectionAccents.study,
                                label = strings.focus30dLabel,
                                value = "${stats.thisMonthFocusMinutes} ${strings.minutesUnit}",
                                vsPrev = "${strings.vsPreviousLabel}: ${stats.lastMonthFocusMinutes} ${strings.minutesUnit}"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            StatComparisonRow(
                                icon = Icons.Rounded.TaskAlt,
                                tint = NexaSectionAccents.tasks,
                                label = strings.tasksDone7dLabel,
                                value = "${stats.thisWeekTasksDone}",
                                vsPrev = "${strings.vsPreviousLabel}: ${stats.lastWeekTasksDone}"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            StatComparisonRow(
                                icon = Icons.Rounded.Spa,
                                tint = NexaSectionAccents.habits,
                                label = strings.habitsLoggedLabel,
                                value = "${stats.thisWeekHabitsDone}",
                                vsPrev = null
                            )
                        }
                    }
                }
            } else {
                items(achievements, key = { it.id }) { ach ->
                    AchievementBadgeCard(ach = ach)
                }
            }
        }
    }
}

@Composable
private fun StatComparisonRow(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String,
    vsPrev: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NexaFilledIconBadge(
            icon = icon,
            tint = tint,
            size = 38.dp,
            iconSize = 19.dp,
            cornerRadius = 11.dp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (vsPrev != null) {
                Text(vsPrev, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tint)
    }
}

@Composable
fun AchievementBadgeCard(
    ach: AchievementProgressEntity,
    modifier: Modifier = Modifier
) {
    val strings = LocalNexaStrings.current
    val badgeColor = when (ach.category) {
        "STUDY" -> NexaSectionAccents.study
        "FOCUS" -> NexaSectionAccents.focus
        "HABITS" -> NexaSectionAccents.habits
        "LIFE" -> NexaSectionAccents.prayer
        "LIBRARY" -> NexaSectionAccents.summaries
        else -> NexaSectionAccents.tasks
    }
    val medalIcon = when (ach.id) {
        "first_task", "tasks_10" -> Icons.Rounded.TaskAlt
        "study_subject" -> Icons.Rounded.MenuBook
        "study_reviews_20" -> Icons.Rounded.Style
        "habit_streak_7" -> Icons.Rounded.LocalFireDepartment
        "focus_60m", "focus_600m" -> Icons.Rounded.HourglassTop
        "life_adhkar_7" -> Icons.Rounded.Mosque
        "library_reader" -> Icons.Rounded.AutoStories
        else -> Icons.Rounded.WorkspacePremium
    }
    val progress = (ach.progress.toFloat() / ach.target.coerceAtLeast(1)).coerceIn(0f, 1f)

    NexaCard(
        modifier = modifier.testTag("achievement_badge_${ach.id}"),
        cornerRadius = 20.dp,
        elevation = if (ach.isUnlocked) 3.dp else 1.dp,
        borderColor = if (ach.isUnlocked) badgeColor.copy(alpha = 0.52f)
        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NexaFilledIconBadge(
                icon = medalIcon,
                tint = if (ach.isUnlocked) badgeColor else MaterialTheme.colorScheme.outline,
                contentDescription = strings.achievementTitle(ach.id),
                size = 52.dp,
                iconSize = 26.dp,
                cornerRadius = 16.dp
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.achievementTitle(ach.id),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (ach.isUnlocked) badgeColor else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (ach.isUnlocked) "${strings.unlockedLabel} • +${ach.xpReward}" else "+${ach.xpReward} ${strings.xpLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (ach.isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = strings.achievementDesc(ach.id),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (ach.isUnlocked) badgeColor else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${ach.progress} / ${ach.target} • ${strings.sectionDisplayName(ach.category)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
