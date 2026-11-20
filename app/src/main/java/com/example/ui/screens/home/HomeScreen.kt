package com.example.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.domain.i18n.NexaLang
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.domain.stats.XpAndStatsEngine
import com.example.services.FocusTimerUiState
import com.example.services.LocalAudioUiState
import com.example.ui.components.MiniVinylBar
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaTintedBanner
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

private data class HomeSectionSpec(
    val key: String,
    val route: String,
    val title: String,
    val description: String,
    val statusBadge: String,
    val icon: ImageVector,
    @DrawableRes val defaultDrawableRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    prefs: NexaPrefsState,
    profile: DesignProfileEntity?,
    tasks: List<TaskEntity>,
    habits: List<HabitEntity>,
    habitLogs: List<HabitLogEntity>,
    focusSessions: List<FocusSessionEntity>,
    focusTimerState: FocusTimerUiState,
    audioState: LocalAudioUiState,
    unreadNotifs: Int,
    subjectsCount: Int = 0,
    dueFlashcardsCount: Int = 0,
    lessonsNotesCount: Int = 0,
    unlockedBadgesCount: Int = 0,
    totalBadgesCount: Int = 9,
    onNavigateRoute: (String) -> Unit,
    onToggleAudio: () -> Unit,
    onExpandAudio: () -> Unit,
    onPreviousAudio: () -> Unit = {},
    onNextAudio: () -> Unit = {},
    onSeekAudio: (Long) -> Unit = {},
    onPickSectionImage: (sectionKey: String, uri: Uri) -> Unit = { _, _ -> },
    onRestoreSectionImage: (sectionKey: String) -> Unit = {},
    onUpdateUserProfile: (name: String, tagline: String, pickedAvatarUri: Uri?, resetAvatar: Boolean) -> Unit = { _, _, _, _ -> },
    onCycleQuote: (nextIndex: Int) -> Unit = {},
    onBookmarkQuote: (quoteText: String, sourceTitle: String, author: String) -> Unit = { _, _, _ -> },
    onUpdateCardsConfig: (List<CardLayoutConfig>) -> Unit,
    onAddCustomShortcut: (String, String) -> Unit = { _, _ -> },
    onRemoveCustomShortcut: (String) -> Unit = {},
    onToggleEditMode: (Boolean) -> Unit
) {
    val strings = LocalNexaStrings.current
    val json = remember { Json { ignoreUnknownKeys = true } }
    val cal = remember { Calendar.getInstance() }
    val hour = cal.get(Calendar.HOUR_OF_DAY)

    val greeting = when {
        hour < 12 -> strings.greetingMorning
        hour < 18 -> strings.greetingAfternoon
        else -> strings.greetingEvening
    }

    val hijri = remember { PrayerTimesCalculator.getHijriDate(cal) }
    val gregorian = remember(prefs.language) {
        val loc = when (prefs.language) {
            "en" -> Locale.ENGLISH
            "fr" -> Locale.FRENCH
            else -> Locale("ar")
        }
        SimpleDateFormat("EEE, d MMM yyyy", loc).format(cal.time)
    }

    val todayIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasksToday = tasks.count {
        it.isCompleted && it.completedAt != null &&
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.completedAt)) == todayIso
    }
    val todayFocusMins = focusSessions
        .filter { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.startedAt)) == todayIso }
        .sumOf { (it.elapsedFocusSeconds / 60).toInt() }
    val habitsDoneToday = habitLogs.count { it.dateIso == todayIso && it.isCompleted }
    val levelInfo = XpAndStatsEngine.computeLevel(prefs.totalXp)

    val prayerTimes = remember(prefs.prayerLat, prefs.prayerLon, prefs.prayerMethod) {
        PrayerTimesCalculator.calculate(prefs.prayerLat, prefs.prayerLon, prefs.prayerMethod, cal)
    }
    val nowMin = hour * 60 + cal.get(Calendar.MINUTE)
    val nextPrayer = prayerTimes.nextPrayer(nowMin)
    val nextPrayerLabel = when (nextPrayer.first) {
        "FAJR" -> if (strings.lang == NexaLang.AR) "الفجر" else "Fajr"
        "SUNRISE" -> if (strings.lang == NexaLang.AR) "الشروق" else "Sunrise"
        "DHUHR" -> if (strings.lang == NexaLang.AR) "الظهر" else "Dhuhr"
        "ASR" -> if (strings.lang == NexaLang.AR) "العصر" else "Asr"
        "MAGHRIB" -> if (strings.lang == NexaLang.AR) "المغرب" else "Maghrib"
        else -> if (strings.lang == NexaLang.AR) "العشاء" else "Isha"
    }

    val sectionImagesMap = remember(profile?.sectionImagesJson) {
        SectionImageCodec.decode(profile?.sectionImagesJson)
    }

    val storedCards = remember(profile?.cardConfigJson) {
        runCatching {
            if (profile?.cardConfigJson.isNullOrBlank()) emptyList()
            else json.decodeFromString<List<CardLayoutConfig>>(profile!!.cardConfigJson)
        }.getOrDefault(emptyList())
    }
    val showGreetingCard = storedCards.firstOrNull { it.id == "greeting_progress" }?.visible ?: true
    val showMiniAudioCard = storedCards.firstOrNull { it.id == "mini_audio" }?.visible ?: true

    // Strict Single-Column order matching NOX STUDY:
    // 1. تنظيم الدراسة  2. مساحة التركيز  3. الصلاة  4. الملخصات  5. حياتي
    val fiveMainSections = remember(
        strings,
        activeTasks.size,
        subjectsCount,
        dueFlashcardsCount,
        lessonsNotesCount,
        habitsDoneToday,
        habits.size,
        focusTimerState.isRunning,
        focusTimerState.remainingSeconds,
        todayFocusMins,
        prefs.focusDailyGoalMinutes,
        nextPrayerLabel,
        nextPrayer.second
    ) {
        val focusStatus = if (focusTimerState.isRunning) {
            val mm = focusTimerState.remainingSeconds / 60
            val ss = focusTimerState.remainingSeconds % 60
            String.format(Locale.US, "%02d:%02d", mm, ss)
        } else {
            "${todayFocusMins}m / ${prefs.focusDailyGoalMinutes}m"
        }
        val studyStatus = "$subjectsCount ${strings.study} • ${activeTasks.size} ${strings.tasks}"
        val summariesStatus = when {
            dueFlashcardsCount > 0 -> "$dueFlashcardsCount ${strings.spacedReview}"
            else -> "$lessonsNotesCount ${strings.lessonNotesSummary}"
        }
        listOf(
            HomeSectionSpec(
                key = "study",
                route = "study",
                title = strings.studyOrganizer,
                description = strings.sectionStudyDesc,
                statusBadge = studyStatus,
                icon = Icons.Rounded.MenuBook,
                defaultDrawableRes = R.drawable.img_section_study
            ),
            HomeSectionSpec(
                key = "focus",
                route = "focus",
                title = strings.focusSpace,
                description = strings.sectionFocusDesc,
                statusBadge = focusStatus,
                icon = Icons.Rounded.HourglassTop,
                defaultDrawableRes = R.drawable.img_section_focus
            ),
            HomeSectionSpec(
                key = "prayer",
                route = "prayer",
                title = strings.prayer,
                description = strings.sectionPrayerDesc,
                statusBadge = "$nextPrayerLabel • ${prayerTimes.formatTime(nextPrayer.second)}",
                icon = Icons.Rounded.Mosque,
                defaultDrawableRes = R.drawable.img_section_prayer
            ),
            HomeSectionSpec(
                key = "summaries",
                route = "summaries",
                title = strings.summaries,
                description = strings.sectionSummariesDesc,
                statusBadge = summariesStatus,
                icon = Icons.Rounded.LibraryBooks,
                defaultDrawableRes = R.drawable.img_section_summaries
            ),
            HomeSectionSpec(
                key = "life",
                route = "life",
                title = strings.life,
                description = strings.sectionLifeDesc,
                statusBadge = "$habitsDoneToday / ${habits.size}",
                icon = Icons.Rounded.Spa,
                defaultDrawableRes = R.drawable.img_section_life
            )
        )
    }

    var showProfileDialog by remember { mutableStateOf(false) }
    var customizingSection by remember { mutableStateOf<HomeSectionSpec?>(null) }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val target = customizingSection
        if (uri != null && target != null) {
            onPickSectionImage(target.key, uri)
        }
    }

    val quotes = DefaultSeedData.motivationalQuotes
    val currentQuote = quotes[(prefs.quoteIndex.coerceAtLeast(0)) % quotes.size]
    val quoteText = when (strings.lang) {
        NexaLang.EN -> currentQuote.textEn
        NexaLang.FR -> currentQuote.textFr
        NexaLang.AR -> currentQuote.textAr
    }
    val quoteAuthor = when (strings.lang) {
        NexaLang.EN -> currentQuote.authorEn
        NexaLang.FR -> currentQuote.authorFr
        NexaLang.AR -> currentQuote.authorAr
    }
    var quoteSavedToast by remember(currentQuote.id) { mutableStateOf(false) }

    val displayUserName = prefs.userName.ifBlank { strings.defaultUserName }
    val displayUserTagline = prefs.userTagline.ifBlank { strings.defaultUserTagline }
    val userAvatarFile = remember(prefs.userAvatarUri) {
        SectionImageCodec.resolveExistingLocalFile(prefs.userAvatarUri)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 2. Compact Welcome Card (Directly below Badges)
        item(key = "home_welcome_card") {
            NexaCard(
                modifier = Modifier.testTag("home_welcome_card"),
                cornerRadius = 20.dp,
                elevation = 2.dp,
                onClick = { showProfileDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("home_profile_header"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        if (userAvatarFile != null) {
                            AsyncImage(
                                model = userAvatarFile,
                                contentDescription = displayUserName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.nexa_app_icon),
                                contentDescription = strings.appName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "$greeting • $displayUserName",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = displayUserTagline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "$gregorian\n${hijri.day} ${if (strings.lang == NexaLang.AR) hijri.monthNameAr else hijri.monthNameEn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }

        if (prefs.globalEditMode) {
            item(key = "home_edit_banner") {
                NexaTintedBanner(
                    text = strings.globalEditMode,
                    icon = Icons.Rounded.Tune,
                    accent = NexaSectionAccents.badges,
                    action = {
                        FilledTonalButton(onClick = { onToggleEditMode(false) }) {
                            Text(strings.doneEditing)
                        }
                    }
                )
            }
        }

        // 3. Mini Music Player (with Vinyl Disc toggle for top controls)
        if (showMiniAudioCard || prefs.globalEditMode) {
            item(key = "mini_audio") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (prefs.globalEditMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                val updated = if (storedCards.any { it.id == "mini_audio" }) {
                                    storedCards.map { if (it.id == "mini_audio") it.copy(visible = !it.visible) else it }
                                } else {
                                    storedCards + CardLayoutConfig("mini_audio", !showMiniAudioCard, "COMPACT", 6)
                                }
                                onUpdateCardsConfig(updated)
                            }) {
                                Icon(
                                    if (showMiniAudioCard) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    contentDescription = strings.edit
                                )
                            }
                        }
                    }
                    MiniVinylBar(
                        audioState = audioState,
                        onTogglePlay = onToggleAudio,
                        onExpand = onExpandAudio,
                        onPrevious = onPreviousAudio,
                        onNext = onNextAudio,
                        onSeek = onSeekAudio
                    )
                }
            }
        }

        // 5. Motivational Quote Card
        item(key = "home_quote_card") {
            MotivationalQuoteHeroCard(
                badgeLabel = strings.quoteOfTheDay,
                quoteText = quoteText,
                quoteAuthor = quoteAuthor,
                isBookmarked = quoteSavedToast,
                nextQuoteContentDescription = strings.nextQuote,
                saveQuoteContentDescription = strings.saveQuote,
                onNextQuote = { onCycleQuote(prefs.quoteIndex + 1) },
                onBookmark = {
                    quoteSavedToast = true
                    onBookmarkQuote(quoteText, strings.quoteOfTheDay, quoteAuthor)
                }
            )
        }

        // Quick actions, placed after the welcome and daily inspiration like NOX STUDY
        item(key = "home_top_hub") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_top_hub_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { onNavigateRoute("achievements") },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .weight(1.08f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("home_achievements_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.EmojiEvents,
                            tint = NexaSectionAccents.badges,
                            contentDescription = strings.badges,
                            size = 28.dp,
                            iconSize = 16.dp,
                            cornerRadius = 9.dp
                        )
                        Text(
                            text = strings.badges,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "$unlockedBadgesCount/$totalBadgesCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Compact Notifications box beside Badges
                Surface(
                    onClick = { onNavigateRoute("notifications") },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = BorderStroke(
                        1.dp,
                        if (unreadNotifs > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .weight(0.92f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("home_notifications_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NexaFilledIconBadge(
                            icon = if (unreadNotifs > 0) Icons.Rounded.NotificationsActive else Icons.Rounded.Notifications,
                            tint = if (unreadNotifs > 0) MaterialTheme.colorScheme.primary else NexaSectionAccents.tasks,
                            contentDescription = strings.notifications,
                            size = 28.dp,
                            iconSize = 15.dp,
                            cornerRadius = 9.dp
                        )
                        Text(
                            text = if (unreadNotifs > 0) "$unreadNotifs ${strings.unreadBadgeSuffix}" else strings.noNewNotifications,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                Surface(
                    onClick = { onNavigateRoute("statistics") },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("home_stats_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.BarChart, contentDescription = strings.statistics, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Surface(
                    onClick = { onNavigateRoute("settings") },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("home_settings_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Tune, contentDescription = strings.settings, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }


        // 4. XP Points & Today's Progress Bar directly below the Mini Music Player
        if (showGreetingCard || prefs.globalEditMode) {
            item(key = "greeting_progress") {
                DailyProgressSummaryCard(
                    title = strings.todayAchievements,
                    level = levelInfo.level,
                    totalXp = prefs.totalXp,
                    progressFraction = levelInfo.progressFraction,
                    activeTasks = activeTasks.size,
                    completedTasksToday = completedTasksToday,
                    todayFocusMins = todayFocusMins,
                    habitsDoneToday = habitsDoneToday,
                    habitsTotal = habits.size,
                    isEditMode = prefs.globalEditMode,
                    isVisible = showGreetingCard,
                    onToggleVisibility = {
                        val updated = if (storedCards.any { it.id == "greeting_progress" }) {
                            storedCards.map { if (it.id == "greeting_progress") it.copy(visible = !it.visible) else it }
                        } else {
                            storedCards + CardLayoutConfig("greeting_progress", !showGreetingCard, "STANDARD", 0)
                        }
                        onUpdateCardsConfig(updated)
                    },
                    onClick = { onNavigateRoute("statistics") }
                )
            }
        }

        item(key = "home_sections_heading") {
            Text(
                text = if (strings.lang == NexaLang.AR) "الأقسام الرئيسية" else if (strings.lang == NexaLang.FR) "Sections principales" else "Main Sections",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp)
            )
        }

        // Five NEXA Main Sections — Compact Single-Column Photo Cards (No top Camera or bottom Arrow icons)
        items(fiveMainSections, key = { "section_${it.key}" }) { section ->
            SectionHeroCard(
                section = section,
                customImageUri = sectionImagesMap[section.key],
                onClick = { onNavigateRoute(section.route) },
                onOpenCustomize = { customizingSection = section }
            )
        }
    }

    // Section Background Image Customization Sheet
    customizingSection?.let { target ->
        val customUri = sectionImagesMap[target.key]
        val localFile = remember(customUri) { SectionImageCodec.resolveExistingLocalFile(customUri) }
        val hasCustomImage = localFile != null

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { customizingSection = null },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${strings.customizeSectionImage} • ${target.title}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasCustomImage) strings.customImageActive else strings.defaultImageActive,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { customizingSection = null }) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.close)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    if (localFile != null) {
                        AsyncImage(
                            model = localFile,
                            contentDescription = target.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = target.defaultDrawableRes),
                            contentDescription = target.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color(0x28140B06),
                                    1.0f to Color(0xD80F0804)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.align(Alignment.BottomStart)) {
                            Text(
                                text = target.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = target.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.88f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pick_section_image_btn"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.chooseFromGallery, fontWeight = FontWeight.SemiBold)
                }

                if (hasCustomImage) {
                    OutlinedButton(
                        onClick = {
                            onRestoreSectionImage(target.key)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("restore_section_image_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.restoreDefaultImage, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        UserProfileDialog(
            initialName = prefs.userName,
            initialTagline = prefs.userTagline,
            hasCustomAvatar = userAvatarFile != null,
            onDismiss = { showProfileDialog = false },
            onSave = { name, tagline, pickedUri, resetAvatar ->
                onUpdateUserProfile(name, tagline, pickedUri, resetAvatar)
                showProfileDialog = false
            }
        )
    }
}

@Composable
private fun UserProfileDialog(
    initialName: String,
    initialTagline: String,
    hasCustomAvatar: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, tagline: String, pickedAvatarUri: Uri?, resetAvatar: Boolean) -> Unit
) {
    val strings = LocalNexaStrings.current
    var name by remember { mutableStateOf(initialName) }
    var tagline by remember { mutableStateOf(initialTagline) }
    var pickedAvatar by remember { mutableStateOf<Uri?>(null) }
    var resetAvatar by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pickedAvatar = uri
            resetAvatar = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.editProfile) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.userNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text(strings.userTaglineLabel) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.chooseFromGallery)
                    }
                    if (hasCustomAvatar && !resetAvatar) {
                        TextButton(onClick = {
                            pickedAvatar = null
                            resetAvatar = true
                        }) { Text(strings.restore) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, tagline, pickedAvatar, resetAvatar) }) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@Composable
private fun MotivationalQuoteHeroCard(
    badgeLabel: String,
    quoteText: String,
    quoteAuthor: String,
    isBookmarked: Boolean,
    nextQuoteContentDescription: String,
    saveQuoteContentDescription: String,
    onNextQuote: () -> Unit,
    onBookmark: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onNextQuote() }
            .testTag("home_quote_card"),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_quote_banner),
                contentDescription = badgeLabel,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color(0x52120905),
                            0.55f to Color(0x88120905),
                            1.0f to Color(0xE8100704)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0x7021130B),
                        border = BorderStroke(0.75.dp, Color(0xFFDAB795).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Rounded.FormatQuote, contentDescription = null, tint = Color(0xFFF3D5AE), modifier = Modifier.size(16.dp))
                            Text(badgeLabel, style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFF3E4), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            onClick = onBookmark,
                            shape = CircleShape,
                            color = Color(0x661C110A),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Rounded.BookmarkAdded else Icons.Rounded.BookmarkBorder,
                                    contentDescription = saveQuoteContentDescription,
                                    tint = if (isBookmarked) Color(0xFFF5D49F) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Surface(
                            onClick = onNextQuote,
                            shape = CircleShape,
                            color = Color(0x661C110A),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = nextQuoteContentDescription,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "“$quoteText”",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFF8EE),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "— $quoteAuthor",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE7C7A3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyProgressSummaryCard(
    title: String,
    level: Int,
    totalXp: Int,
    progressFraction: Float,
    activeTasks: Int,
    completedTasksToday: Int,
    todayFocusMins: Int,
    habitsDoneToday: Int,
    habitsTotal: Int,
    isEditMode: Boolean,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onClick: () -> Unit
) {
    val strings = LocalNexaStrings.current
    NexaCard(
        modifier = Modifier.testTag("home_xp_bar"),
        cornerRadius = 22.dp,
        elevation = 2.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${strings.levelLabel} $level • $totalXp ${strings.xpLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (isEditMode) {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            if (isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = strings.edit
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "✓ $completedTasksToday",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressMiniPill(
                    icon = Icons.Rounded.TaskAlt,
                    text = "${strings.todayTasksCount}: $activeTasks",
                    modifier = Modifier.weight(1f)
                )
                ProgressMiniPill(
                    icon = Icons.Rounded.HourglassTop,
                    text = "${todayFocusMins} ${strings.minutesUnit}",
                    modifier = Modifier.weight(0.75f)
                )
                ProgressMiniPill(
                    icon = Icons.Rounded.AutoAwesome,
                    text = "$habitsDoneToday/$habitsTotal",
                    modifier = Modifier.weight(0.75f)
                )
            }
        }
    }
}

@Composable
private fun ProgressMiniPill(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SectionHeroCard(
    section: HomeSectionSpec,
    customImageUri: String?,
    onClick: () -> Unit,
    onOpenCustomize: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "section_card_scale"
    )

    val localFile = remember(customImageUri) {
        SectionImageCodec.resolveExistingLocalFile(customImageUri)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(22.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                onClick = onClick,
                onLongClick = onOpenCustomize
            )
            .testTag("home_section_${section.key}"),
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full edge-to-edge background photo covering 100% of the card
            if (localFile != null) {
                AsyncImage(
                    model = localFile,
                    contentDescription = section.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = section.defaultDrawableRes),
                    contentDescription = section.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Rich espresso dark gradient scrim for crisp legibility in Light & Dark modes
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color(0x38140B06),
                            0.45f to Color(0x7A120A06),
                            1.0f to Color(0xEB0E0704)
                        )
                    )
            )

            // NOX STUDY-style composition: badge and text on the right, soft arrow tile on the left.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.align(Alignment.TopStart),
                    shape = RoundedCornerShape(50),
                    color = Color(0x6E1B1009),
                    border = BorderStroke(0.75.dp, Color(0xFFE7C7A3).copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = Color(0xFFF3D5AE),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = section.statusBadge,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(0.82f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = section.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.24f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}
