package com.example

import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTouchInput
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AchievementProgressEntity
import com.example.data.local.BlockedAppEntity
import com.example.data.local.DefaultSeedData
import com.example.data.local.DesignProfileEntity
import com.example.data.local.FocusSessionEntity
import com.example.data.local.HabitEntity
import com.example.data.local.NexaDatabase
import com.example.data.local.NexaPreferences
import com.example.data.local.NexaPrefsState
import com.example.data.local.SectionImageCodec
import com.example.data.local.StudyLessonEntity
import com.example.data.local.StudyScheduleSlotEntity
import com.example.data.local.StudySubjectEntity
import com.example.data.local.TaskEntity
import com.example.services.FocusGuardManager
import com.example.services.FocusTimerUiState
import com.example.services.LocalAudioUiState
import com.example.services.PodcastPlayerController
import com.example.services.PodcastUiState
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.habits.HabitsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.life.PrayerSanctuaryScreen
import com.example.ui.screens.life.SerenityTab
import com.example.ui.screens.settings.AchievementsScreen
import com.example.ui.screens.study.StudyOrganizerScreen
import com.example.ui.screens.study.StudyScreen
import com.example.ui.screens.summaries.SummariesScreen
import com.example.ui.screens.tasks.TasksScreen
import com.example.ui.theme.NexaTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h915dp")
class HomeScreenRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun allDefaultSectionDrawables_areBundledLocally() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val drawables = listOf(
            R.drawable.img_section_study,
            R.drawable.img_section_focus,
            R.drawable.img_section_prayer,
            R.drawable.img_section_summaries,
            R.drawable.img_section_life,
            R.drawable.img_quote_banner,
            R.drawable.img_hourglass_bg
        )
        drawables.forEach { resId ->
            assertNotNull("Drawable $resId must load locally", ContextCompat.getDrawable(context, resId))
        }
    }

    @Test
    fun homeScreen_displaysTopOrderVinylControlsCompactSectionsAndNoShortcutsOrIcons() {
        val navigated = mutableListOf<String>()
        var cycledTo: Int? = null

        composeRule.setContent {
            NexaTheme(
                profile = DefaultSeedData.defaultDesignProfiles().first(),
                languageCode = "ar"
            ) {
                HomeScreen(
                    prefs = NexaPrefsState(language = "ar", totalXp = 240, quoteIndex = 0),
                    profile = DefaultSeedData.defaultDesignProfiles().first(),
                    tasks = emptyList(),
                    habits = emptyList(),
                    habitLogs = emptyList(),
                    focusSessions = emptyList(),
                    focusTimerState = FocusTimerUiState(),
                    audioState = LocalAudioUiState(),
                    unreadNotifs = 2,
                    unlockedBadgesCount = 3,
                    totalBadgesCount = 9,
                    subjectsCount = 3,
                    dueFlashcardsCount = 4,
                    lessonsNotesCount = 2,
                    onNavigateRoute = { navigated += it },
                    onToggleAudio = {},
                    onExpandAudio = {},
                    onCycleQuote = { cycledTo = it },
                    onBookmarkQuote = { _, _, _ -> },
                    onUpdateCardsConfig = {},
                    onToggleEditMode = {}
                )
            }
        }
        composeRule.waitForIdle()

        // Verify top Hub Row (Badges + compact Notifications + Stats + Settings), Welcome Card, Vinyl Player, XP Bar
        val topHub = composeRule.onNodeWithTag("home_top_hub_row").assertIsDisplayed()
        composeRule.onNodeWithTag("home_achievements_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("home_notifications_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("home_stats_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("home_settings_btn").assertIsDisplayed()

        val welcome = composeRule.onNodeWithTag("home_welcome_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_profile_header", useUnmergedTree = true).assertIsDisplayed()
        val vinylBar = composeRule.onNodeWithTag("mini_vinyl_bar").assertIsDisplayed()
        val xpBar = composeRule.onNodeWithTag("home_xp_bar").assertIsDisplayed()

        // Check required vertical ordering: Top Hub -> Welcome -> Music Player -> XP Bar
        val topHubY = topHub.getUnclippedBoundsInRoot().top.value
        val welcomeY = welcome.getUnclippedBoundsInRoot().top.value
        val vinylY = vinylBar.getUnclippedBoundsInRoot().top.value
        val xpY = xpBar.getUnclippedBoundsInRoot().top.value
        assertTrue("Top hub must sit above welcome card", topHubY < welcomeY)
        assertTrue("Welcome card must sit above vinyl player", welcomeY < vinylY)
        assertTrue("Vinyl player must sit above XP bar", vinylY < xpY)

        // Tap vinyl disc toggles top playback controls
        composeRule.onNodeWithTag("mini_vinyl_top_controls").assertDoesNotExist()
        composeRule.onNodeWithTag("vinyl_disc_toggle").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mini_vinyl_top_controls").assertIsDisplayed()
        composeRule.onNodeWithTag("mini_vinyl_prev_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("mini_vinyl_top_play_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("mini_vinyl_next_btn").assertIsDisplayed()

        // Custom shortcuts completely removed (#11)
        composeRule.onNodeWithTag("quick_shortcuts").assertDoesNotExist()

        val listNode = composeRule.onNodeWithTag("home_screen_list")
        listNode.performScrollToKey("home_quote_card")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_quote_card").assertIsDisplayed().performClick()
        assertEquals(1, cycledTo)

        val sectionKeys = listOf("study", "focus", "prayer", "summaries", "life")
        val titles = listOf("تنظيم الدراسة", "مساحة التركيز", "الصلاة", "الملخصات", "حياتي")

        var expectedLeft = -1f
        sectionKeys.forEachIndexed { idx, key ->
            listNode.performScrollToKey("section_$key")
            composeRule.waitForIdle()

            composeRule.onNodeWithText(titles[idx]).assertIsDisplayed()
            // Top camera customization icon removed (#10)
            composeRule.onNodeWithTag("customize_img_$key").assertDoesNotExist()

            val cardNode = composeRule.onNodeWithTag("home_section_$key")
            cardNode.assertIsDisplayed()

            val bounds = cardNode.getUnclippedBoundsInRoot()
            if (expectedLeft < 0f) {
                expectedLeft = bounds.left.value
            } else {
                assertEquals("Section $key must align in a single column", expectedLeft, bounds.left.value, 2f)
            }
            val cardWidth = bounds.right.value - bounds.left.value
            val cardHeight = bounds.bottom.value - bounds.top.value
            assertTrue("Card $key must have full-bleed width (was $cardWidth)", cardWidth >= 300f)
            assertTrue("Card $key height must stay compact <= 124dp (was $cardHeight)", cardHeight <= 124f)
            cardNode.performClick()
        }
        composeRule.waitForIdle()
        assertEquals(sectionKeys, navigated)
    }

    @Test
    fun homeScreen_sectionLongPressOpensCustomizationSheetAndRestoresImage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempFile = File(context.cacheDir, "test_custom_study.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        val customJson = SectionImageCodec.put("{}", "study", Uri.fromFile(tempFile).toString())
        val profile = DesignProfileEntity(
            id = 1L,
            name = "Custom Test",
            sectionImagesJson = customJson
        )
        var restoredKey: String? = null

        composeRule.setContent {
            NexaTheme(profile = profile, languageCode = "ar") {
                HomeScreen(
                    prefs = NexaPrefsState(language = "ar"),
                    profile = profile,
                    tasks = emptyList(),
                    habits = emptyList(),
                    habitLogs = emptyList(),
                    focusSessions = emptyList(),
                    focusTimerState = FocusTimerUiState(),
                    audioState = LocalAudioUiState(),
                    unreadNotifs = 0,
                    onNavigateRoute = {},
                    onToggleAudio = {},
                    onExpandAudio = {},
                    onRestoreSectionImage = { restoredKey = it },
                    onUpdateCardsConfig = {},
                    onToggleEditMode = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_screen_list").performScrollToKey("section_study")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_section_study").performTouchInput { longClick() }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("pick_section_image_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("restore_section_image_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        assertEquals("study", restoredKey)
        tempFile.delete()
    }

    @Test
    fun studyScreen_displaysScheduleTabSubjectsGradesAndGoals() {
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                StudyScreen(
                    subjects = listOf(StudySubjectEntity(id = 1L, name = "الفيزياء")),
                    lessons = emptyList(),
                    flashcards = emptyList(),
                    reviewLogs = emptyList(),
                    scheduleSlots = listOf(
                        StudyScheduleSlotEntity(
                            id = 5L,
                            dayOfWeek = 6,
                            startMinute = 540,
                            endMinute = 600,
                            subjectId = 1L,
                            title = "مراجعة الفيزياء"
                        )
                    ),
                    grades = emptyList(),
                    goals = emptyList(),
                    onSaveSubject = {},
                    onDeleteSubject = {},
                    onSaveLesson = {},
                    onDeleteLesson = {},
                    onSaveFlashcard = {},
                    onDeleteFlashcard = {},
                    onRateFlashcard = { _, _ -> },
                    onRecordMemoryTest = { _, _, _, _, _ -> },
                    onSaveScheduleSlot = {},
                    onDeleteScheduleSlot = {},
                    onSaveGrade = {},
                    onDeleteGrade = {},
                    onSaveGoal = {},
                    onDeleteGoal = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("الفيزياء").assertIsDisplayed()
        composeRule.onNodeWithText("الجدول").assertIsDisplayed()
        composeRule.onNodeWithText("المعدلات والنتائج").assertIsDisplayed()
        composeRule.onNodeWithText("الأهداف والإحصائيات").assertIsDisplayed()

        composeRule.onNodeWithText("الجدول").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("study_schedule_tab").assertIsDisplayed()
        composeRule.onNodeWithTag("schedule_slot_5").assertIsDisplayed()
        composeRule.onNodeWithText("مراجعة الفيزياء").assertIsDisplayed()
    }

    @Test
    fun studyOrganizerScreen_switchesBetweenSubjectsAndFlatTasks() {
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                StudyOrganizerScreen(
                    tasks = listOf(
                        TaskEntity(id = 1L, title = "مراجعة الفصل الأول"),
                        TaskEntity(id = 2L, title = "حل تمارين الهندسة", isCompleted = true)
                    ),
                    subjects = listOf(StudySubjectEntity(id = 1L, name = "الفيزياء")),
                    lessons = emptyList(),
                    flashcards = emptyList(),
                    reviewLogs = emptyList(),
                    scheduleSlots = emptyList(),
                    grades = emptyList(),
                    goals = emptyList(),
                    onSaveTask = {},
                    onToggleTaskComplete = {},
                    onSnoozeTask = { _, _ -> },
                    onDeleteTask = {},
                    onSaveSubject = {},
                    onDeleteSubject = {},
                    onSaveLesson = {},
                    onDeleteLesson = {},
                    onSaveFlashcard = {},
                    onDeleteFlashcard = {},
                    onRateFlashcard = { _, _ -> },
                    onRecordMemoryTest = { _, _, _, _, _ -> },
                    onSaveScheduleSlot = {},
                    onDeleteScheduleSlot = {},
                    onSaveGrade = {},
                    onDeleteGrade = {},
                    onSaveGoal = {},
                    onDeleteGoal = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("study_organizer_screen").assertIsDisplayed()
        composeRule.onNodeWithText("الفيزياء").assertIsDisplayed()
        composeRule.onNodeWithTag("study_org_tab_tasks").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("add_task_fab").assertIsDisplayed()
        composeRule.onNodeWithTag("tasks_flat_list").assertIsDisplayed()
        composeRule.onNodeWithTag("task_item_card_1").assertIsDisplayed()
        composeRule.onNodeWithTag("task_item_card_2").assertIsDisplayed()
        // Old 4 task tabs must not exist (#7)
        composeRule.onNodeWithText("المتأخرة").assertDoesNotExist()
        composeRule.onNodeWithText("القادمة").assertDoesNotExist()
    }

    @Test
    fun tasksScreen_displaysFlatListWithoutFourTabs() {
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                TasksScreen(
                    tasks = listOf(
                        TaskEntity(id = 10L, title = "تلخيص النحو"),
                        TaskEntity(id = 11L, title = "اختبار الرياضيات", isCompleted = true)
                    ),
                    onSaveTask = {},
                    onToggleComplete = {},
                    onSnoozeTask = { _, _ -> },
                    onDeleteTask = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tasks_flat_list").assertIsDisplayed()
        composeRule.onNodeWithTag("task_item_card_10").assertIsDisplayed()
        composeRule.onNodeWithTag("task_item_card_11").assertIsDisplayed()
        composeRule.onNodeWithText("تلخيص النحو").assertIsDisplayed()
        composeRule.onNodeWithText("اختبار الرياضيات").assertIsDisplayed()
        composeRule.onNodeWithText("المتأخرة").assertDoesNotExist()
        composeRule.onNodeWithText("المكتملة").assertDoesNotExist()
    }

    @Test
    fun habitsScreen_ordersRoutinesLogicallyAndLocalizesLabels() {
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                HabitsScreen(
                    habits = listOf(
                        HabitEntity(id = 1L, title = "تدوين المساء", routineName = "Evening"),
                        HabitEntity(id = 2L, title = "مذاكرة صباحية", routineName = "Morning"),
                        HabitEntity(id = 3L, title = "مراجعة الدرس", routineName = "Study")
                    ),
                    logs = emptyList(),
                    onSaveHabit = {},
                    onDeleteHabit = {},
                    onSwapOrder = { _, _ -> },
                    onLogProgress = { _, _, _ -> },
                    onCopyPhoto = { "" }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("الصباح").assertIsDisplayed()
        composeRule.onNodeWithText("الدراسة").assertIsDisplayed()
        composeRule.onNodeWithText("المساء").assertIsDisplayed()
        val morningY = composeRule.onNodeWithText("مذاكرة صباحية").assertIsDisplayed().getUnclippedBoundsInRoot().top.value
        val studyY = composeRule.onNodeWithText("مراجعة الدرس").assertIsDisplayed().getUnclippedBoundsInRoot().top.value
        val eveningY = composeRule.onNodeWithText("تدوين المساء").assertIsDisplayed().getUnclippedBoundsInRoot().top.value
        assertTrue("Morning must precede Study", morningY < studyY)
        assertTrue("Study must precede Evening", studyY < eveningY)
    }

    @Test
    fun focusScreen_rendersRenamedTabsHourglassAndOmitsAmbientSound() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = NexaDatabase.getInstance(context)
        val fgm = FocusGuardManager(context, db, NexaPreferences(context))
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                FocusScreen(
                    prefs = NexaPrefsState(language = "ar"),
                    timerState = FocusTimerUiState(),
                    sessions = emptyList(),
                    blockedApps = emptyList(),
                    tasks = emptyList(),
                    lessons = emptyList(),
                    focusGuardManager = fgm,
                    onStartSession = { _, _, _, _, _, _, _ -> },
                    onPauseSession = {},
                    onResumeSession = {},
                    onFinishSession = {},
                    onToggleBlockedApp = { _, _, _, _, _, _, _ -> },
                    onUpdateFocusGuardPrefs = { _, _, _ -> },
                    onUpdateBlockImage = {},
                    onCopyPhoto = { "" }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hourglass_stage_card").assertIsDisplayed()
        composeRule.onNodeWithTag("focus_hourglass_canvas").assertIsDisplayed()
        composeRule.onNodeWithTag("customize_hourglass_bg_btn").assertIsDisplayed()
        // Renamed tabs (#16) & ambient sound removed (#15)
        composeRule.onNodeWithText("تركيز").assertIsDisplayed()
        composeRule.onNodeWithText("حارس التركيز").assertIsDisplayed()
        composeRule.onNodeWithText("تقارير التركيز").assertIsDisplayed()
        composeRule.onNodeWithText("صوت هادئ").assertDoesNotExist()
    }

    @Test
    fun focusReportsTab_formatsPausedMinutesWithoutRawSpecifier() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = NexaDatabase.getInstance(context)
        val fgm = FocusGuardManager(context, db, NexaPreferences(context))
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                FocusScreen(
                    prefs = NexaPrefsState(language = "ar"),
                    timerState = FocusTimerUiState(),
                    sessions = listOf(
                        FocusSessionEntity(
                            id = 99L,
                            mode = "POMODORO",
                            plannedMinutes = 25,
                            elapsedFocusSeconds = 1500,
                            pausedSeconds = 120,
                            status = "COMPLETED",
                            linkedTitle = "مراجعة الكيمياء"
                        )
                    ),
                    blockedApps = emptyList(),
                    tasks = emptyList(),
                    lessons = emptyList(),
                    focusGuardManager = fgm,
                    onStartSession = { _, _, _, _, _, _, _ -> },
                    onPauseSession = {},
                    onResumeSession = {},
                    onFinishSession = {},
                    onToggleBlockedApp = { _, _, _, _, _, _, _ -> },
                    onUpdateFocusGuardPrefs = { _, _, _ -> },
                    onUpdateBlockImage = {},
                    onCopyPhoto = { "" }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("تقارير التركيز").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("توقف مؤقت 2 د (مستبعدة)", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("%d", substring = true).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w360dp-h780dp")
    fun homeScreen_narrowWidthFitsTopHubAndSingleColumnSectionsWithoutClipping() {
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                HomeScreen(
                    prefs = NexaPrefsState(language = "ar", totalXp = 120),
                    profile = DefaultSeedData.defaultDesignProfiles().first(),
                    tasks = emptyList(),
                    habits = emptyList(),
                    habitLogs = emptyList(),
                    focusSessions = emptyList(),
                    focusTimerState = FocusTimerUiState(),
                    audioState = LocalAudioUiState(),
                    unreadNotifs = 2,
                    unlockedBadgesCount = 3,
                    totalBadgesCount = 9,
                    onNavigateRoute = {},
                    onToggleAudio = {},
                    onExpandAudio = {},
                    onUpdateCardsConfig = {},
                    onToggleEditMode = {}
                )
            }
        }
        composeRule.waitForIdle()
        val hubBounds = composeRule.onNodeWithTag("home_top_hub_row").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertTrue("Top hub must stay within 360dp viewport (right=${hubBounds.right.value})", hubBounds.right.value <= 360f)
        composeRule.onNodeWithTag("home_achievements_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("home_notifications_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("home_stats_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("home_settings_btn").assertIsDisplayed()
    }

    @Test
    fun prayerSanctuaryScreen_removesCalcMethodAndProvidesPrayerBlockSwitch() {
        var toggledTo: Boolean? = null
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                PrayerSanctuaryScreen(
                    prefs = NexaPrefsState(language = "ar", focusGuardPrayerBlock = false),
                    adhkarProgress = emptyList(),
                    duaFavorites = emptyList(),
                    blockedAppsCount = 2,
                    onTogglePrayerBlock = { toggledTo = it },
                    onSavePrayerConfig = { _, _, _, _ -> },
                    onTogglePrayerReminders = {},
                    onIncrementDhikr = {},
                    onToggleDuaFav = { _, _ -> },
                    onScheduleOccasionReminder = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("prayer_sanctuary_screen").assertIsDisplayed()
        composeRule.onNodeWithText("طريقة الحساب").assertDoesNotExist()
        composeRule.onNodeWithTag("prayer_block_card").assertIsDisplayed()
        composeRule.onNodeWithText("حجب التطبيقات أثناء وقت الصلاة").assertIsDisplayed()
        composeRule.onNodeWithTag("prayer_block_switch").performClick()
        composeRule.waitForIdle()
        assertEquals(true, toggledTo)
    }

    @Test
    fun serenityTab_updatesEmpatheticPhraseBasedOnSelectedEmotion() {
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                SerenityTab(entries = emptyList(), onAddMood = { _, _ -> })
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("serenity_emotion_phrase_card").assertIsDisplayed()
        composeRule.onNodeWithText("قلق").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("توقّف لحظة وتنفّس ببطء: لست مطالباً بحل كل شيء الآن. اختر خطوة واحدة صغيرة ودع الباقي يتيسّر.")
            .assertIsDisplayed()
    }

    @Test
    fun achievementsScreen_displaysLocalizedBadgeTitlesAndUnlockDescriptions() {
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                AchievementsScreen(
                    prefs = NexaPrefsState(language = "ar"),
                    achievements = listOf(
                        AchievementProgressEntity(
                            id = "first_task",
                            category = "TASKS",
                            titleKey = "first_task",
                            descKey = "first_task",
                            xpReward = 30,
                            isUnlocked = true,
                            progress = 1,
                            target = 1
                        )
                    ),
                    tasks = emptyList(),
                    focusSessions = emptyList(),
                    habitLogs = emptyList(),
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("achievement_badge_first_task").assertIsDisplayed()
        composeRule.onNodeWithText("أول خطوة").assertIsDisplayed()
        composeRule.onNodeWithText("أتمّ مهمتك الأولى لتبدأ رحلة الإنجاز").assertIsDisplayed()
    }

    @Test
    fun focusGuardManager_evaluatesFocusPauseAndTemporaryBypassCorrectly() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = NexaDatabase.getInstance(context)
        val fgm = FocusGuardManager(context, db, NexaPreferences(context))
        val blocked = BlockedAppEntity(
            packageName = "com.example.distraction",
            appLabel = "Distraction",
            isEnabled = true,
            blockInFocus = true,
            blockInPrayer = true,
            blockInTasks = false
        )
        val runningFocus = FocusTimerUiState(isRunning = true, isPaused = false)
        assertEquals("FOCUS", fgm.evaluateBlockReason(blocked, NexaPrefsState(), runningFocus))

        val pausedFocus = FocusTimerUiState(isRunning = true, isPaused = true)
        assertNull(fgm.evaluateBlockReason(blocked, NexaPrefsState(), pausedFocus))

        // Grant temporary bypass -> reason should become null during bypass window
        fgm.grantTemporaryBypass("com.example.distraction", "FOCUS", 60_000L)
        assertTrue(fgm.isBypassed("com.example.distraction", "FOCUS"))
        assertNull(fgm.evaluateBlockReason(blocked, NexaPrefsState(), runningFocus))
    }

    @Test
    fun summariesScreen_rendersCoreSections() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = NexaDatabase.getInstance(context)
        val podcastCtrl = PodcastPlayerController(context, db)
        composeRule.setContent {
            NexaTheme(profile = DefaultSeedData.defaultDesignProfiles().first(), languageCode = "ar") {
                SummariesScreen(
                    subjects = listOf(StudySubjectEntity(id = 1L, name = "التاريخ")),
                    lessons = listOf(
                        StudyLessonEntity(
                            id = 1L,
                            subjectId = 1L,
                            title = "الحضارة الإسلامية",
                            sideNoteText = "ملخص علمي مهم"
                        )
                    ),
                    flashcards = emptyList(),
                    libraryItems = emptyList(),
                    quotes = emptyList(),
                    podcastEpisodes = emptyList(),
                    podcastState = PodcastUiState(),
                    podcastController = podcastCtrl,
                    onSaveLesson = {},
                    onRateFlashcard = { _, _ -> },
                    onUpdateBook = {},
                    onImportTextBook = { _, _, _, _ -> },
                    onSaveQuote = { _, _, _ -> },
                    onSaveCustomQuote = { _, _, _ -> },
                    onDeleteQuote = {},
                    onTogglePodcastFav = {},
                    onDownloadPodcast = { _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("summaries_hub_screen").assertIsDisplayed()
        composeRule.onNodeWithText("الحضارة الإسلامية").assertIsDisplayed()
        composeRule.onNodeWithText("ملخص علمي مهم").assertIsDisplayed()
    }

    @Test
    fun notificationsAndSettingsScreens_renderCleanlyInDarkAndLightMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = NexaDatabase.getInstance(context)
        val fgm = FocusGuardManager(context, db, NexaPreferences(context))
        val darkProfile = DefaultSeedData.defaultDesignProfiles().first().copy(themeMode = "DARK")
        var backClicked = false

        composeRule.setContent {
            NexaTheme(profile = darkProfile, languageCode = "en") {
                com.example.ui.screens.settings.NotificationsScreen(
                    prefs = NexaPrefsState(language = "en"),
                    notifications = emptyList(),
                    onBack = { backClicked = true },
                    onMarkRead = {},
                    onMarkAllRead = {},
                    onClearAll = {},
                    onUpdateChannels = { _, _ -> },
                    onUpdateDnd = { _, _, _ -> }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("notifications_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("notifications_back_btn").performClick()
        assertTrue(backClicked)
        assertNotNull(fgm)
    }

    @Test
    fun navHostBackstack_popsToPreviousDestinationInsteadOfResettingToHome() {
        lateinit var navCtrl: NavHostController
        composeRule.setContent {
            navCtrl = rememberNavController()
            NavHost(
                navController = navCtrl,
                startDestination = HomeRoute
            ) {
                composable<HomeRoute> { androidx.compose.material3.Text("HOME") }
                composable<StudyRoute> { androidx.compose.material3.Text("STUDY") }
                composable<SettingsRoute> { androidx.compose.material3.Text("SETTINGS") }
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnUiThread {
            navCtrl.navigate(StudyRoute) { launchSingleTop = true }
            navCtrl.navigate(SettingsRoute) { launchSingleTop = true }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("SETTINGS").assertIsDisplayed()

        composeRule.runOnUiThread { navCtrl.popBackStack() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("STUDY").assertIsDisplayed()

        composeRule.runOnUiThread { navCtrl.popBackStack() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("HOME").assertIsDisplayed()
    }
}
