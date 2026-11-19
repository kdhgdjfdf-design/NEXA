package com.example

import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppNotificationEntity
import com.example.data.local.CardLayoutConfig
import com.example.data.local.IslamicOccasion
import com.example.domain.i18n.LocalNexaStrings
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.services.ReminderScheduler
import com.example.ui.NexaMainViewModel
import com.example.ui.components.ExpandableVinylSheet
import com.example.ui.components.SectionLockGate
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.habits.HabitsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.life.MyLifeHubScreen
import com.example.ui.screens.life.PrayerSanctuaryScreen
import com.example.ui.screens.settings.AchievementsScreen
import com.example.ui.screens.settings.NotificationsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.study.StudyOrganizerScreen
import com.example.ui.screens.summaries.SummariesScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.theme.NexaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar

@Serializable object HomeRoute
@Serializable object TasksRoute
@Serializable object StudyRoute
@Serializable object HabitsRoute
@Serializable object FocusRoute
@Serializable object PrayerRoute
@Serializable object SummariesRoute
@Serializable object LifeRoute
@Serializable object LibraryRoute
@Serializable object NotificationsRoute
@Serializable object AchievementsRoute
@Serializable object StatisticsRoute
@Serializable object SettingsRoute

private data class MainNavTab(
    val routeObj: Any,
    val routeKey: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val labelProvider: (com.example.domain.i18n.NexaStrings) -> String
)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: NexaMainViewModel = viewModel()
            NexaAppRoot(vm)
        }
    }
}

@Composable
private fun NexaAppRoot(vm: NexaMainViewModel) {
    val prefs by vm.prefsState.collectAsStateWithLifecycle()
    val activeProfile by vm.activeProfile.collectAsStateWithLifecycle()
    val designProfiles by vm.designProfiles.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val subjects by vm.subjects.collectAsStateWithLifecycle()
    val lessons by vm.lessons.collectAsStateWithLifecycle()
    val flashcards by vm.flashcards.collectAsStateWithLifecycle()
    val reviewLogs by vm.reviewLogs.collectAsStateWithLifecycle()
    val scheduleSlots by vm.scheduleSlots.collectAsStateWithLifecycle()
    val grades by vm.grades.collectAsStateWithLifecycle()
    val studyGoals by vm.studyGoals.collectAsStateWithLifecycle()
    val habits by vm.habits.collectAsStateWithLifecycle()
    val habitLogs by vm.habitLogs.collectAsStateWithLifecycle()
    val focusSessions by vm.focusSessions.collectAsStateWithLifecycle()
    val blockedApps by vm.blockedApps.collectAsStateWithLifecycle()
    val moodEntries by vm.moodEntries.collectAsStateWithLifecycle()
    val adhkarProgress by vm.adhkarProgress.collectAsStateWithLifecycle()
    val duaFavorites by vm.duaFavorites.collectAsStateWithLifecycle()
    val libraryItems by vm.libraryItems.collectAsStateWithLifecycle()
    val savedQuotes by vm.savedQuotes.collectAsStateWithLifecycle()
    val podcastEpisodes by vm.podcastEpisodes.collectAsStateWithLifecycle()
    val audioTracks by vm.audioTracks.collectAsStateWithLifecycle()
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val achievements by vm.achievements.collectAsStateWithLifecycle()
    val backupRecords by vm.backupRecords.collectAsStateWithLifecycle()

    val focusTimerState by vm.container.focusGuardManager.timerState.collectAsStateWithLifecycle()
    val localAudioState by vm.container.localAudioController.uiState.collectAsStateWithLifecycle()
    val podcastState by vm.container.podcastController.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val json = remember { Json { ignoreUnknownKeys = true; encodeDefaults = true } }
    val snackbarHostState = remember { SnackbarHostState() }

    // App Auto-Lock & Per-Section Lock tracking
    var appLocked by rememberSaveable { mutableStateOf(false) }
    var lastBackgroundMs by rememberSaveable { mutableLongStateOf(0L) }
    var unlockedSections by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showAudioSheet by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, prefs.pinHash, prefs.autoLockMinutes) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    lastBackgroundMs = System.currentTimeMillis()
                }
                Lifecycle.Event.ON_START -> {
                    if (prefs.pinHash.isNotBlank() && lastBackgroundMs > 0L) {
                        val elapsed = System.currentTimeMillis() - lastBackgroundMs
                        if (elapsed >= prefs.autoLockMinutes * 60_000L) {
                            appLocked = true
                            unlockedSections = emptySet()
                        }
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    NexaTheme(profile = activeProfile, languageCode = prefs.language) {
        val strings = LocalNexaStrings.current
        if (appLocked && prefs.pinHash.isNotBlank()) {
            Surface(modifier = Modifier.fillMaxSize()) {
                SectionLockGate(
                    sectionTitle = strings.appName,
                    pinHash = prefs.pinHash,
                    pinSalt = prefs.pinSalt,
                    biometricEnabled = prefs.biometricEnabled,
                    onUnlocked = { appLocked = false }
                )
            }
            return@NexaTheme
        }

        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        val isHome = currentDestination?.hasRoute<HomeRoute>() == true || currentDestination == null

        fun navigateToRouteKey(routeKey: String) {
            val dest: Any = when (routeKey) {
                "home" -> HomeRoute
                "tasks" -> TasksRoute
                "study" -> StudyRoute
                "habits" -> HabitsRoute
                "focus" -> FocusRoute
                "prayer" -> PrayerRoute
                "summaries" -> SummariesRoute
                "life" -> LifeRoute
                "library" -> LibraryRoute
                "notifications" -> NotificationsRoute
                "achievements" -> AchievementsRoute
                "statistics" -> StatisticsRoute
                "settings" -> SettingsRoute
                else -> HomeRoute
            }
            if (dest is HomeRoute) {
                navController.popBackStack(HomeRoute, inclusive = false)
            } else {
                navController.navigate(dest) {
                    launchSingleTop = true
                }
            }
        }

        fun navigateBack() {
            if (!navController.popBackStack()) {
                navigateToRouteKey("home")
            }
        }

        // 5 Core NEXA Sections for Bottom NavigationBar / NavigationRail:
        // 1. تنظيم الدراسة  2. مساحة التركيز  3. الصلاة  4. الملخصات  5. حياتي
        val fiveTabs = remember {
            listOf(
                MainNavTab(StudyRoute, "study", Icons.Rounded.MenuBook) { it.studyOrganizer },
                MainNavTab(FocusRoute, "focus", Icons.Rounded.HourglassTop) { it.focusSpace },
                MainNavTab(PrayerRoute, "prayer", Icons.Rounded.Mosque) { it.prayer },
                MainNavTab(SummariesRoute, "summaries", Icons.Rounded.LibraryBooks) { it.summaries },
                MainNavTab(LifeRoute, "life", Icons.Rounded.Spa) { it.life }
            )
        }

        val currentSectionKey: String? = when {
            currentDestination?.hasRoute<StudyRoute>() == true -> "study"
            currentDestination?.hasRoute<TasksRoute>() == true -> "study"
            currentDestination?.hasRoute<FocusRoute>() == true -> "focus"
            currentDestination?.hasRoute<PrayerRoute>() == true -> "prayer"
            currentDestination?.hasRoute<SummariesRoute>() == true -> "summaries"
            currentDestination?.hasRoute<LibraryRoute>() == true -> "summaries"
            currentDestination?.hasRoute<LifeRoute>() == true -> "life"
            currentDestination?.hasRoute<HabitsRoute>() == true -> "life"
            else -> null
        }

        fun toggleSchedulePrayerReminder(enabled: Boolean) {
            scope.launch {
                vm.prefs.setPrayerReminders(enabled)
                if (enabled) {
                    val cal = Calendar.getInstance()
                    val times = PrayerTimesCalculator.calculate(
                        prefs.prayerLat,
                        prefs.prayerLon,
                        prefs.prayerMethod,
                        cal
                    )
                    val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                    val next = times.nextPrayer(nowMin)
                    val targetCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, next.second / 60)
                        set(Calendar.MINUTE, next.second % 60)
                        set(Calendar.SECOND, 0)
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    ReminderScheduler.scheduleTaskReminder(
                        context = context,
                        taskId = 80_000L + next.first.hashCode(),
                        title = "${strings.prayer}: ${next.first}",
                        triggerAtMs = targetCal.timeInMillis
                    )
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            IconButton(
                                onClick = { navigateToRouteKey("home") },
                                modifier = Modifier.testTag("rail_home_btn")
                            ) {
                                Icon(
                                    Icons.Rounded.Home,
                                    contentDescription = strings.home,
                                    tint = if (isHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    ) {
                        Spacer(Modifier.height(8.dp))
                        fiveTabs.forEach { tab ->
                            val selected = currentSectionKey == tab.routeKey
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToRouteKey(tab.routeKey) },
                                icon = { Icon(tab.icon, contentDescription = tab.labelProvider(strings)) },
                                label = { Text(tab.labelProvider(strings), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_${tab.routeKey}")
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.background,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        if (!isHome && !useRail) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = { navigateBack() },
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("top_home_chip"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.back, fontWeight = FontWeight.SemiBold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        IconButton(
                                            onClick = { showAudioSheet = true },
                                            modifier = Modifier.testTag("top_audio_btn")
                                        ) {
                                            Icon(Icons.Rounded.GraphicEq, contentDescription = strings.audioPlayer, tint = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        IconButton(
                                            onClick = { navigateToRouteKey("settings") },
                                            modifier = Modifier.testTag("top_settings_btn")
                                        ) {
                                            Icon(Icons.Rounded.Tune, contentDescription = strings.settings, tint = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        if (!useRail) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp
                            ) {
                                fiveTabs.forEach { tab ->
                                    val selected = currentSectionKey == tab.routeKey
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateToRouteKey(tab.routeKey) },
                                        icon = { Icon(tab.icon, contentDescription = tab.labelProvider(strings)) },
                                        label = { Text(tab.labelProvider(strings), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                            indicatorColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_${tab.routeKey}")
                                    )
                                }
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .widthIn(max = 920.dp)
                            .align(Alignment.CenterVertically)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = HomeRoute,
                            enterTransition = { fadeIn(animationSpec = tween(220)) },
                            exitTransition = { fadeOut(animationSpec = tween(180)) }
                        ) {
                            composable<HomeRoute> {
                                HomeScreen(
                                    prefs = prefs,
                                    profile = activeProfile,
                                    tasks = tasks,
                                    habits = habits,
                                    habitLogs = habitLogs,
                                    focusSessions = focusSessions,
                                    focusTimerState = focusTimerState,
                                    audioState = localAudioState,
                                    unreadNotifs = notifications.count { !it.isRead },
                                    subjectsCount = subjects.size,
                                    dueFlashcardsCount = flashcards.count { it.nextReviewAt <= System.currentTimeMillis() },
                                    lessonsNotesCount = lessons.count { it.sideNoteText.isNotBlank() },
                                    unlockedBadgesCount = achievements.count { it.isUnlocked },
                                    totalBadgesCount = achievements.size.coerceAtLeast(9),
                                    onNavigateRoute = { routeKey -> navigateToRouteKey(routeKey) },
                                    onToggleAudio = {
                                        if (localAudioState.currentTrack == null && audioTracks.isNotEmpty()) {
                                            vm.container.localAudioController.playTrack(audioTracks.first())
                                        } else {
                                            vm.container.localAudioController.togglePlayPause()
                                        }
                                    },
                                    onExpandAudio = { showAudioSheet = true },
                                    onPreviousAudio = { vm.container.localAudioController.previousTrack(audioTracks) },
                                    onNextAudio = { vm.container.localAudioController.nextTrack(audioTracks) },
                                    onSeekAudio = { posMs -> vm.container.localAudioController.seekTo(posMs) },
                                    onPickSectionImage = { sectionKey, uri ->
                                        vm.setSectionBackgroundImage(context, sectionKey, uri)
                                    },
                                    onRestoreSectionImage = { sectionKey ->
                                        vm.restoreSectionBackgroundImage(context, sectionKey)
                                    },
                                    onUpdateUserProfile = { name, tagline, pickedUri, resetAvatar ->
                                        vm.updateUserProfile(context, name, tagline, pickedUri, resetAvatar)
                                    },
                                    onCycleQuote = vm::cycleQuote,
                                    onBookmarkQuote = { qText, qTitle, qAuthor ->
                                        vm.saveCustomQuoteText(qText, qTitle, qAuthor)
                                        scope.launch { snackbarHostState.showSnackbar("✓ ${strings.savedQuotes}") }
                                    },
                                    onUpdateCardsConfig = { updatedCards: List<CardLayoutConfig> ->
                                        vm.updateActiveProfile { prof ->
                                            prof.copy(cardConfigJson = json.encodeToString(updatedCards))
                                        }
                                    },
                                    onAddCustomShortcut = { title, targetRoute ->
                                        vm.addCustomShortcut(title, targetRoute)
                                    },
                                    onRemoveCustomShortcut = { id ->
                                        vm.removeCustomShortcut(id)
                                    },
                                    onToggleEditMode = { enabled ->
                                        vm.setGlobalEditMode(enabled)
                                    }
                                )
                            }

                            composable<StudyRoute> {
                                SectionProtectedContent(
                                    sectionKey = "study",
                                    sectionTitle = strings.studyOrganizer,
                                    prefs = prefs,
                                    unlockedSections = unlockedSections,
                                    onUnlockSection = { unlockedSections = unlockedSections + "study" }
                                ) {
                                    StudyOrganizerScreen(
                                        tasks = tasks,
                                        subjects = subjects,
                                        lessons = lessons,
                                        flashcards = flashcards,
                                        reviewLogs = reviewLogs,
                                        scheduleSlots = scheduleSlots,
                                        grades = grades,
                                        goals = studyGoals,
                                        onSaveTask = vm::saveTask,
                                        onToggleTaskComplete = vm::toggleTaskComplete,
                                        onSnoozeTask = vm::snoozeTask,
                                        onDeleteTask = vm::deleteTask,
                                        onSaveSubject = vm::saveSubject,
                                        onDeleteSubject = vm::deleteSubject,
                                        onSaveLesson = vm::saveLesson,
                                        onDeleteLesson = vm::deleteLesson,
                                        onSaveFlashcard = vm::saveFlashcard,
                                        onDeleteFlashcard = vm::deleteFlashcard,
                                        onRateFlashcard = vm::rateFlashcard,
                                        onRecordMemoryTest = vm::recordMemoryTestResult,
                                        onSaveScheduleSlot = vm::saveScheduleSlot,
                                        onDeleteScheduleSlot = vm::deleteScheduleSlot,
                                        onSaveGrade = vm::saveGrade,
                                        onDeleteGrade = vm::deleteGrade,
                                        onSaveGoal = vm::saveStudyGoal,
                                        onDeleteGoal = vm::deleteStudyGoal
                                    )
                                }
                            }

                            composable<TasksRoute> {
                                SectionProtectedContent(
                                    sectionKey = "tasks",
                                    sectionTitle = strings.tasks,
                                    prefs = prefs,
                                    unlockedSections = unlockedSections,
                                    onUnlockSection = { unlockedSections = unlockedSections + "tasks" }
                                ) {
                                    TasksScreen(
                                        tasks = tasks,
                                        onSaveTask = vm::saveTask,
                                        onToggleComplete = vm::toggleTaskComplete,
                                        onSnoozeTask = vm::snoozeTask,
                                        onDeleteTask = vm::deleteTask
                                    )
                                }
                            }

                            composable<FocusRoute> {
                                SectionProtectedContent(
                                    sectionKey = "focus",
                                    sectionTitle = strings.focusSpace,
                                    prefs = prefs,
                                    unlockedSections = unlockedSections,
                                    onUnlockSection = { unlockedSections = unlockedSections + "focus" }
                                ) {
                                    FocusScreen(
                                        prefs = prefs,
                                        timerState = focusTimerState,
                                        sessions = focusSessions,
                                        blockedApps = blockedApps,
                                        tasks = tasks,
                                        lessons = lessons,
                                        focusGuardManager = vm.container.focusGuardManager,
                                        onStartSession = { mode, mins, taskId, lessonId, title, sound, localUri ->
                                            vm.container.focusGuardManager.startFocusSession(
                                                mode = mode,
                                                plannedMinutes = mins,
                                                linkedTaskId = taskId,
                                                linkedLessonId = lessonId,
                                                linkedTitle = title,
                                                ambientSound = sound,
                                                localAmbientUri = localUri
                                            )
                                        },
                                        onPauseSession = { vm.container.focusGuardManager.pauseSession() },
                                        onResumeSession = { vm.container.focusGuardManager.resumeSession() },
                                        onFinishSession = { vm.container.focusGuardManager.completeSession(false) },
                                        onToggleBlockedApp = vm::toggleBlockedApp,
                                        onUpdateFocusGuardPrefs = { prayerBlock, countdown, message ->
                                            scope.launch {
                                                vm.prefs.setFocusGuardConfig(prayerBlock, countdown, message)
                                            }
                                        },
                                        onUpdateBlockImage = { imgUri ->
                                            vm.updateActiveProfile { prof ->
                                                prof.copy(focusBlockImageUri = imgUri)
                                            }
                                        },
                                        onCopyPhoto = { uri ->
                                            vm.copyPickedImageToInternal(context, uri, "focus_block")
                                        },
                                        onPickHourglassBackground = { uri ->
                                            vm.setFocusBackgroundImage(context, uri)
                                        },
                                        onRestoreHourglassBackground = {
                                            vm.restoreFocusBackgroundImage(context)
                                        }
                                    )
                                }
                            }

                            composable<PrayerRoute> {
                                SectionProtectedContent(
                                    sectionKey = "prayer",
                                    sectionTitle = strings.prayer,
                                    prefs = prefs,
                                    unlockedSections = unlockedSections,
                                    onUnlockSection = { unlockedSections = unlockedSections + "prayer" }
                                ) {
                                    PrayerSanctuaryScreen(
                                        prefs = prefs,
                                        adhkarProgress = adhkarProgress,
                                        duaFavorites = duaFavorites,
                                        focusGuardManager = vm.container.focusGuardManager,
                                        blockedAppsCount = blockedApps.count { it.isEnabled },
                                        onTogglePrayerBlock = { checked ->
                                            scope.launch {
                                                vm.prefs.setFocusGuardConfig(
                                                    checked,
                                                    prefs.focusGuardCountdownEnabled,
                                                    prefs.focusGuardCustomMessage
                                                )
                                            }
                                        },
                                        onOpenFocusGuard = { navigateToRouteKey("focus") },
                                        onSavePrayerConfig = vm::savePrayerConfig,
                                        onTogglePrayerReminders = ::toggleSchedulePrayerReminder,
                                        onIncrementDhikr = vm::incrementDhikr,
                                        onToggleDuaFav = vm::toggleDuaFavorite,
                                        onScheduleOccasionReminder = { occasion ->
                                            scope.launch(Dispatchers.IO) {
                                                val triggerMs = findNextHijriOccasionEpoch(occasion)
                                                val title = when (prefs.language) {
                                                    "en" -> occasion.titleEn
                                                    "fr" -> occasion.titleFr
                                                    else -> occasion.titleAr
                                                }
                                                ReminderScheduler.scheduleTaskReminder(
                                                    context = context,
                                                    taskId = 90_000L + occasion.id.hashCode(),
                                                    title = title,
                                                    triggerAtMs = triggerMs
                                                )
                                                vm.container.db.systemDao().insertNotification(
                                                    AppNotificationEntity(
                                                        section = "LIFE",
                                                        title = title,
                                                        body = "تم تفعيل تذكير المناسبة الإسلامية"
                                                    )
                                                )
                                            }
                                            scope.launch {
                                                snackbarHostState.showSnackbar("✓ ${occasion.titleAr}")
                                            }
                                        }
                                    )
                                }
                            }

                            composable<SummariesRoute> {
                                SectionProtectedContent(
                                    sectionKey = "summaries",
                                    sectionTitle = strings.summaries,
                                    prefs = prefs,
                                    unlockedSections = unlockedSections,
                                    onUnlockSection = { unlockedSections = unlockedSections + "summaries" }
                                ) {
                                    SummariesScreen(
                                        subjects = subjects,
                                        lessons = lessons,
                                        flashcards = flashcards,
                                        libraryItems = libraryItems,
                                        quotes = savedQuotes,
                                        podcastEpisodes = podcastEpisodes,
                                        podcastState = podcastState,
                                        podcastController = vm.container.podcastController,
                                        onSaveLesson = vm::saveLesson,
                                        onRateFlashcard = vm::rateFlashcard,
                                        onUpdateBook = vm::updateLibraryItem,
                                        onImportTextBook = vm::importTextBook,
                                        onSaveQuote = vm::saveQuote,
                                        onSaveCustomQuote = vm::saveCustomQuoteText,
                                        onDeleteQuote = vm::deleteQuote,
                                        onTogglePodcastFav = vm::togglePodcastFavorite,
                                        onDownloadPodcast = vm::downloadPodcastEpisode
                                    )
                                }
                            }

                            composable<LifeRoute> {
                                SectionProtectedContent(
                                    sectionKey = "life",
                                    sectionTitle = strings.life,
                                    prefs = prefs,
                                    unlockedSections = unlockedSections,
                                    onUnlockSection = { unlockedSections = unlockedSections + "life" }
                                ) {
                                    MyLifeHubScreen(
                                        prefs = prefs,
                                        habits = habits,
                                        habitLogs = habitLogs,
                                        moodEntries = moodEntries,
                                        achievements = achievements,
                                        tasks = tasks,
                                        focusSessions = focusSessions,
                                        onSaveHabit = vm::saveHabit,
                                        onDeleteHabit = vm::deleteHabit,
                                        onSwapHabitOrder = vm::swapHabitOrder,
                                        onLogHabitProgress = vm::logHabitProgress,
                                        onCopyHabitPhoto = { uri ->
                                            vm.copyPickedImageToInternal(context, uri, "habit")
                                        },
                                        onAddMoodEntry = vm::addMoodEntry
                                    )
                                }
                            }

                            composable<HabitsRoute> {
                                SectionProtectedContent(
                                    sectionKey = "habits",
                                    sectionTitle = strings.habits,
                                    prefs = prefs,
                                    unlockedSections = unlockedSections,
                                    onUnlockSection = { unlockedSections = unlockedSections + "habits" }
                                ) {
                                    HabitsScreen(
                                        habits = habits,
                                        logs = habitLogs,
                                        onSaveHabit = vm::saveHabit,
                                        onDeleteHabit = vm::deleteHabit,
                                        onSwapOrder = vm::swapHabitOrder,
                                        onLogProgress = vm::logHabitProgress,
                                        onCopyPhoto = { uri ->
                                            vm.copyPickedImageToInternal(context, uri, "habit")
                                        }
                                    )
                                }
                            }

                            composable<LibraryRoute> {
                                SectionProtectedContent(
                                    sectionKey = "library",
                                    sectionTitle = strings.library,
                                    prefs = prefs,
                                    unlockedSections = unlockedSections,
                                    onUnlockSection = { unlockedSections = unlockedSections + "library" }
                                ) {
                                    LibraryScreen(
                                        itemsList = libraryItems,
                                        quotes = savedQuotes,
                                        podcastEpisodes = podcastEpisodes,
                                        podcastState = podcastState,
                                        podcastController = vm.container.podcastController,
                                        onUpdateBook = vm::updateLibraryItem,
                                        onImportTextBook = vm::importTextBook,
                                        onSaveQuote = vm::saveQuote,
                                        onDeleteQuote = vm::deleteQuote,
                                        onTogglePodcastFav = vm::togglePodcastFavorite,
                                        onDownloadPodcast = vm::downloadPodcastEpisode
                                    )
                                }
                            }

                            composable<NotificationsRoute> {
                                NotificationsScreen(
                                    prefs = prefs,
                                    notifications = notifications,
                                    onBack = { navigateBack() },
                                    onMarkRead = vm::markNotificationRead,
                                    onMarkAllRead = vm::markAllNotificationsRead,
                                    onClearAll = vm::clearNotifications,
                                    onUpdateChannels = { sound, vib ->
                                        scope.launch { vm.prefs.setNotificationChannels(sound, vib) }
                                    },
                                    onUpdateDnd = { enabled, startH, endH ->
                                        scope.launch { vm.prefs.setDnd(enabled, startH, endH) }
                                    }
                                )
                            }

                            composable<AchievementsRoute> {
                                AchievementsScreen(
                                    prefs = prefs,
                                    achievements = achievements,
                                    tasks = tasks,
                                    focusSessions = focusSessions,
                                    habitLogs = habitLogs,
                                    onBack = { navigateBack() },
                                    initialTab = 0
                                )
                            }

                            composable<StatisticsRoute> {
                                AchievementsScreen(
                                    prefs = prefs,
                                    achievements = achievements,
                                    tasks = tasks,
                                    focusSessions = focusSessions,
                                    habitLogs = habitLogs,
                                    onBack = { navigateBack() },
                                    initialTab = 1
                                )
                            }

                            composable<SettingsRoute> {
                                SettingsScreen(
                                    prefs = prefs,
                                    profiles = designProfiles,
                                    activeProfile = activeProfile,
                                    backupRecords = backupRecords,
                                    focusGuardManager = vm.container.focusGuardManager,
                                    onSetLanguage = vm::setLanguage,
                                    onSelectProfile = vm::selectDesignProfile,
                                    onCreateProfile = vm::createDesignProfile,
                                    onDeleteProfile = vm::deleteDesignProfile,
                                    onUpdateActiveProfile = vm::updateActiveProfile,
                                    onRestoreDefaultDesign = vm::restoreDefaultDesign,
                                    onToggleGlobalEdit = vm::setGlobalEditMode,
                                    onCopyPhoto = { uri, tag ->
                                        vm.copyPickedImageToInternal(context, uri, tag)
                                    },
                                    onSetPin = vm::setAppPin,
                                    onSetBiometric = { enabled ->
                                        scope.launch { vm.prefs.setBiometricEnabled(enabled) }
                                    },
                                    onSetAutoLock = { mins ->
                                        scope.launch { vm.prefs.setAutoLockMinutes(mins) }
                                    },
                                    onSetLockedSections = { set ->
                                        scope.launch { vm.prefs.setLockedSections(set) }
                                    },
                                    onSetHideNotifContent = { hide ->
                                        scope.launch { vm.prefs.setHideNotificationContent(hide) }
                                    },
                                    onToggleAutoBackup = vm::toggleAutoBackup,
                                    onCreateBackupNow = vm::createLocalBackupNow,
                                    onExportBackup = vm::exportBackupToUri,
                                    onRestoreBackup = vm::restoreBackup
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAudioSheet) {
            ExpandableVinylSheet(
                audioState = localAudioState,
                tracks = audioTracks,
                onDismiss = { showAudioSheet = false },
                onPlayTrack = { track -> vm.container.localAudioController.playTrack(track) },
                onTogglePlay = {
                    if (localAudioState.currentTrack == null && audioTracks.isNotEmpty()) {
                        vm.container.localAudioController.playTrack(audioTracks.first())
                    } else {
                        vm.container.localAudioController.togglePlayPause()
                    }
                },
                onSeek = { posMs -> vm.container.localAudioController.seekTo(posMs) },
                onImportLocalMp3 = { title, playlist, uriString ->
                    vm.importAudioTrack(title, playlist, uriString)
                },
                onDeleteTrack = vm::deleteAudioTrack,
                onPrevious = { vm.container.localAudioController.previousTrack(audioTracks) },
                onNext = { vm.container.localAudioController.nextTrack(audioTracks) }
            )
        }
    }
}

@Composable
private fun SectionProtectedContent(
    sectionKey: String,
    sectionTitle: String,
    prefs: com.example.data.local.NexaPrefsState,
    unlockedSections: Set<String>,
    onUnlockSection: () -> Unit,
    content: @Composable () -> Unit
) {
    val isLocked = prefs.pinHash.isNotBlank() &&
        sectionKey in prefs.lockedSections &&
        sectionKey !in unlockedSections
    if (isLocked) {
        SectionLockGate(
            sectionTitle = sectionTitle,
            pinHash = prefs.pinHash,
            pinSalt = prefs.pinSalt,
            biometricEnabled = prefs.biometricEnabled,
            onUnlocked = onUnlockSection
        )
    } else {
        content()
    }
}

private fun findNextHijriOccasionEpoch(occasion: IslamicOccasion): Long {
    val hijri = IslamicCalendar(ULocale("@calendar=islamic-umalqura"))
    val nowMs = System.currentTimeMillis()
    hijri.timeInMillis = nowMs
    val currentHijriYear = hijri.get(IslamicCalendar.YEAR)

    hijri.set(IslamicCalendar.YEAR, currentHijriYear)
    hijri.set(IslamicCalendar.MONTH, (occasion.hijriMonth - 1).coerceIn(0, 11))
    hijri.set(IslamicCalendar.DAY_OF_MONTH, occasion.hijriDay.coerceIn(1, 29))
    hijri.set(IslamicCalendar.HOUR_OF_DAY, 8)
    hijri.set(IslamicCalendar.MINUTE, 0)
    hijri.set(IslamicCalendar.SECOND, 0)

    if (hijri.timeInMillis <= nowMs) {
        hijri.set(IslamicCalendar.YEAR, currentHijriYear + 1)
    }
    return hijri.timeInMillis
}
