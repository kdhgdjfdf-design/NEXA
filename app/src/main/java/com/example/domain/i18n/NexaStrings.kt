package com.example.domain.i18n

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

enum class NexaLang(val code: String, val isRtl: Boolean) {
    AR("ar", true),
    EN("en", false),
    FR("fr", false);

    companion object {
        fun fromCode(code: String): NexaLang = when (code.lowercase()) {
            "en" -> EN
            "fr" -> FR
            else -> AR
        }
    }
}

val LocalNexaStrings = staticCompositionLocalOf { NexaStrings.forLang(NexaLang.AR) }

@Immutable
class NexaExtraStrings(
    val focusTab: String,
    val focusReports: String,
    val prayerBlockTitle: String,
    val prayerBlockDesc: String,
    val offlineLocalBadge: String,
    val minutesInputLabel: String,
    val minutesUnit: String,
    val targetLabel: String,
    val unitLabel: String,
    val scoreLabel: String,
    val maxScoreLabel: String,
    val showAnswerLabel: String,
    val nextItemLabel: String,
    val tapToFlipLabel: String,
    val flashcardFrontLabel: String,
    val flashcardBackLabel: String,
    val accessibilityServiceLabel: String,
    val usageStatsLabel: String,
    val batteryUnrestrictedLabel: String,
    val accessibilityRequiredHint: String,
    val openSettingsLabel: String,
    val enabledLabel: String,
    val disabledLabel: String,
    val yesLabel: String,
    val noLabel: String,
    val fixNowLabel: String,
    val soundLabel: String,
    val vibrationLabel: String,
    val dndLabel: String,
    val markAllReadLabel: String,
    val unreadBadgeSuffix: String,
    val noNewNotifications: String,
    val sessionsCountLabel: String,
    val dailyGoalPrefix: String,
    val pausedMinutesExcluded: String,
    val colorPaletteLabel: String,
    val unlockedLabel: String,
    val lockedLabel: String,
    val completedLabel: String,
    val serenitySelectPrompt: String,
    val serenityGuidanceLabel: String,
    val last7DaysLabel: String,
    val last30DaysLabel: String,
    val focus7dLabel: String,
    val focus30dLabel: String,
    val tasksDone7dLabel: String,
    val habitsLoggedLabel: String,
    val vsPreviousLabel: String,
    val databaseLabel: String,
    val appFilesLabel: String,
    val cacheLabel: String,
    val backupsCountLabel: String,
    val notificationsPermissionLabel: String,
    val locationPermissionLabel: String,
    val offlineModeStatusNote: String,
    val pinRuleHint: String,
    val backupCreatedMsg: String,
    val backupExportedMsg: String,
    val backupErrorMsg: String,
    val previousTrack: String,
    val nextTrack: String,
    val returnHomeLabel: String,
    val invalidPinError: String,
    val playAudioLabel: String,
    val resumeAudioLabel: String,
    val pagePrefix: String,
    val importedBookDefault: String,
    val localImportDefault: String,
    val generalCategory: String,
    val attachmentDefault: String,
    val pdfPageDesc: String,
    val youtubeAppLabel: String,
    val customRoutineLabel: String
)

@Immutable
class NexaStrings private constructor(
    val lang: NexaLang,
    // App & Nav
    val appName: String,
    val home: String,
    val tasks: String,
    val study: String,
    val studyOrganizer: String,
    val habits: String,
    val focus: String,
    val focusSpace: String,
    val summaries: String,
    val life: String,
    val library: String,
    val audioPlayer: String,
    val achievements: String,
    val badges: String,
    val statistics: String,
    val notifications: String,
    val settings: String,
    // Home
    val greetingMorning: String,
    val greetingAfternoon: String,
    val greetingEvening: String,
    val dailyProgress: String,
    val todayAchievements: String,
    val quoteOfTheDay: String,
    val nextQuote: String,
    val editProfile: String,
    val userNameLabel: String,
    val userTaglineLabel: String,
    val defaultUserName: String,
    val defaultUserTagline: String,
    val todayTasksCount: String,
    val focusTodayMinutes: String,
    val levelLabel: String,
    val xpLabel: String,
    val quickShortcuts: String,
    val customShortcuts: String,
    val editHomeLayout: String,
    val doneEditing: String,
    val emptyTasksHint: String,
    val sectionTasksDesc: String,
    val sectionStudyDesc: String,
    val sectionHabitsDesc: String,
    val sectionFocusDesc: String,
    val sectionPrayerDesc: String,
    val sectionSummariesDesc: String,
    val sectionLifeDesc: String,
    val customizeSectionImage: String,
    val customizeFocusBg: String,
    val chooseFromGallery: String,
    val restoreDefaultImage: String,
    val customImageActive: String,
    val defaultImageActive: String,
    val lessonNotesSummary: String,
    val allFlashcardsDeck: String,
    val noLessonNotesYet: String,
    val openLesson: String,
    val habitsAndRoutines: String,
    // Common
    val add: String,
    val save: String,
    val cancel: String,
    val delete: String,
    val edit: String,
    val close: String,
    val search: String,
    val filter: String,
    val all: String,
    val back: String,
    val confirm: String,
    val restore: String,
    val notes: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val low: String,
    val medium: String,
    val high: String,
    val urgent: String,
    // Tasks
    val tasksToday: String,
    val tasksUpcoming: String,
    val tasksOverdue: String,
    val tasksCompleted: String,
    val newTask: String,
    val recurrence: String,
    val recNone: String,
    val recDaily: String,
    val recWeekly: String,
    val recMonthly: String,
    val recCustom: String,
    val attachFile: String,
    val reminderLabel: String,
    val snooze15m: String,
    val snooze1h: String,
    val snoozeTomorrow: String,
    val focusGuardOnDue: String,
    val reopenTask: String,
    // Study
    val subjects: String,
    val schedule: String,
    val gradesAndGpa: String,
    val goalsAndStats: String,
    val addSubject: String,
    val coefficient: String,
    val targetAverage: String,
    val lessons: String,
    val addLesson: String,
    val attachPdf: String,
    val openPdf: String,
    val youtubeLink: String,
    val sideNoteTitle: String,
    val sideNoteHint: String,
    val flashcards: String,
    val addFlashcard: String,
    val suggestFromNotes: String,
    val spacedReview: String,
    val testYourMemory: String,
    val testMemoryDisclaimer: String,
    val again: String,
    val hard: String,
    val good: String,
    val easy: String,
    val reviewHistory: String,
    val weeklySchedule: String,
    val exceptionSchedule: String,
    val noAiStudyNote: String,
    val weightedAverage: String,
    // Habits
    val routines: String,
    val addHabit: String,
    val measureCompletion: String,
    val measureCount: String,
    val measureDuration: String,
    val streakDays: String,
    val bestStreak: String,
    val restDay: String,
    val noPenaltyNote: String,
    val attachPhoto: String,
    // Focus & Guard
    val pomodoro: String,
    val customTimer: String,
    val openSession: String,
    val startFocus: String,
    val pauseFocus: String,
    val resumeFocus: String,
    val finishFocus: String,
    val ambientSound: String,
    val linkTaskOrLesson: String,
    val focusGuard: String,
    val blockedApps: String,
    val selectAppsToBlock: String,
    val dailyLimitMinutes: String,
    val countdown30sNote: String,
    val protectionStatus: String,
    val oemBatteryDisclaimer: String,
    val emergencyExit: String,
    val confirmEarlyExit: String,
    val undoGraceTitle: String,
    val blockScreenCustomizer: String,
    // Life
    val prayer: String,
    val adhkar: String,
    val duas: String,
    val serenity: String,
    val islamicOccasions: String,
    val cityLabel: String,
    val calcMethod: String,
    val qiblaDirection: String,
    val adhkarMorning: String,
    val adhkarEvening: String,
    val adhkarAfterPrayer: String,
    val adhkarSleep: String,
    val adhkarWakeup: String,
    val serenityDisclaimer: String,
    val logMoodPrompt: String,
    val weeklyMonthlyPatterns: String,
    // Library & Audio
    val books: String,
    val novels: String,
    val podcasts: String,
    val favorites: String,
    val shelfReading: String,
    val shelfCompleted: String,
    val shelfWantToRead: String,
    val savedQuotes: String,
    val saveQuote: String,
    val importBook: String,
    val paperColor: String,
    val fontSize: String,
    val downloadOffline: String,
    val downloaded: String,
    val playbackSpeed: String,
    val sleepTimer: String,
    val discover: String,
    val listeningHistory: String,
    val importLocalAudio: String,
    val playlistQuran: String,
    val playlistAdhkar: String,
    val playlistCalm: String,
    val playlistCustom: String,
    val musicPauseRuleNote: String,
    val podcastBgRuleNote: String,
    // Settings, Security, Status
    val customizationStudio: String,
    val designProfiles: String,
    val createProfile: String,
    val restoreDefaultDesign: String,
    val globalEditMode: String,
    val themeMode: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,
    val fontFamily: String,
    val iconStyle: String,
    val languageLabel: String,
    val accountAndPrivacy: String,
    val offlineFirstNotice: String,
    val setPin: String,
    val biometricLock: String,
    val lockedSections: String,
    val autoLockTimeout: String,
    val hideNotifContent: String,
    val encryptedBackups: String,
    val createBackupNow: String,
    val exportBackupFile: String,
    val restoreBackupFile: String,
    val autoBackupToggle: String,
    val appStatus: String,
    val storageUsage: String,
    val permissionsHealth: String,
    val selfComparisonNote: String,
    private val extra: NexaExtraStrings
) {
    val focusTab: String get() = extra.focusTab
    val focusReports: String get() = extra.focusReports
    val prayerBlockTitle: String get() = extra.prayerBlockTitle
    val prayerBlockDesc: String get() = extra.prayerBlockDesc
    val offlineLocalBadge: String get() = extra.offlineLocalBadge
    val minutesInputLabel: String get() = extra.minutesInputLabel
    val minutesUnit: String get() = extra.minutesUnit
    val targetLabel: String get() = extra.targetLabel
    val unitLabel: String get() = extra.unitLabel
    val scoreLabel: String get() = extra.scoreLabel
    val maxScoreLabel: String get() = extra.maxScoreLabel
    val showAnswerLabel: String get() = extra.showAnswerLabel
    val nextItemLabel: String get() = extra.nextItemLabel
    val tapToFlipLabel: String get() = extra.tapToFlipLabel
    val flashcardFrontLabel: String get() = extra.flashcardFrontLabel
    val flashcardBackLabel: String get() = extra.flashcardBackLabel
    val accessibilityServiceLabel: String get() = extra.accessibilityServiceLabel
    val usageStatsLabel: String get() = extra.usageStatsLabel
    val batteryUnrestrictedLabel: String get() = extra.batteryUnrestrictedLabel
    val accessibilityRequiredHint: String get() = extra.accessibilityRequiredHint
    val openSettingsLabel: String get() = extra.openSettingsLabel
    val enabledLabel: String get() = extra.enabledLabel
    val disabledLabel: String get() = extra.disabledLabel
    val yesLabel: String get() = extra.yesLabel
    val noLabel: String get() = extra.noLabel
    val fixNowLabel: String get() = extra.fixNowLabel
    val soundLabel: String get() = extra.soundLabel
    val vibrationLabel: String get() = extra.vibrationLabel
    val dndLabel: String get() = extra.dndLabel
    val markAllReadLabel: String get() = extra.markAllReadLabel
    val unreadBadgeSuffix: String get() = extra.unreadBadgeSuffix
    val noNewNotifications: String get() = extra.noNewNotifications
    val sessionsCountLabel: String get() = extra.sessionsCountLabel
    val dailyGoalPrefix: String get() = extra.dailyGoalPrefix
    val pausedMinutesExcluded: String get() = extra.pausedMinutesExcluded
    val colorPaletteLabel: String get() = extra.colorPaletteLabel
    val unlockedLabel: String get() = extra.unlockedLabel
    val lockedLabel: String get() = extra.lockedLabel
    val completedLabel: String get() = extra.completedLabel
    val serenitySelectPrompt: String get() = extra.serenitySelectPrompt
    val serenityGuidanceLabel: String get() = extra.serenityGuidanceLabel
    val last7DaysLabel: String get() = extra.last7DaysLabel
    val last30DaysLabel: String get() = extra.last30DaysLabel
    val focus7dLabel: String get() = extra.focus7dLabel
    val focus30dLabel: String get() = extra.focus30dLabel
    val tasksDone7dLabel: String get() = extra.tasksDone7dLabel
    val habitsLoggedLabel: String get() = extra.habitsLoggedLabel
    val vsPreviousLabel: String get() = extra.vsPreviousLabel
    val databaseLabel: String get() = extra.databaseLabel
    val appFilesLabel: String get() = extra.appFilesLabel
    val cacheLabel: String get() = extra.cacheLabel
    val backupsCountLabel: String get() = extra.backupsCountLabel
    val notificationsPermissionLabel: String get() = extra.notificationsPermissionLabel
    val locationPermissionLabel: String get() = extra.locationPermissionLabel
    val offlineModeStatusNote: String get() = extra.offlineModeStatusNote
    val pinRuleHint: String get() = extra.pinRuleHint
    val backupCreatedMsg: String get() = extra.backupCreatedMsg
    val backupExportedMsg: String get() = extra.backupExportedMsg
    val backupErrorMsg: String get() = extra.backupErrorMsg
    val previousTrack: String get() = extra.previousTrack
    val nextTrack: String get() = extra.nextTrack
    val returnHomeLabel: String get() = extra.returnHomeLabel
    val invalidPinError: String get() = extra.invalidPinError
    val playAudioLabel: String get() = extra.playAudioLabel
    val resumeAudioLabel: String get() = extra.resumeAudioLabel
    val pagePrefix: String get() = extra.pagePrefix
    val importedBookDefault: String get() = extra.importedBookDefault
    val localImportDefault: String get() = extra.localImportDefault
    val generalCategory: String get() = extra.generalCategory
    val attachmentDefault: String get() = extra.attachmentDefault
    val pdfPageDesc: String get() = extra.pdfPageDesc
    val youtubeAppLabel: String get() = extra.youtubeAppLabel
    val customRoutineLabel: String get() = extra.customRoutineLabel

    override fun equals(other: Any?): Boolean = (other as? NexaStrings)?.lang == lang
    override fun hashCode(): Int = lang.hashCode()

    fun blockReasonLabel(reason: String): String = when (reason) {
        "PRAYER" -> when (lang) {
            NexaLang.AR -> "وقت الصلاة"
            NexaLang.FR -> "Temps de prière"
            NexaLang.EN -> "Prayer Time"
        }
        "TASK" -> when (lang) {
            NexaLang.AR -> "موعد مهمة"
            NexaLang.FR -> "Tâche en cours"
            NexaLang.EN -> "Scheduled Task"
        }
        "DAILY_LIMIT" -> when (lang) {
            NexaLang.AR -> "الحد اليومي"
            NexaLang.FR -> "Limite quotidienne"
            NexaLang.EN -> "Daily Limit"
        }
        else -> when (lang) {
            NexaLang.AR -> "جلسة تركيز"
            NexaLang.FR -> "Session Focus"
            NexaLang.EN -> "Focus Session"
        }
    }

    fun sectionDisplayName(key: String): String = when (key.uppercase()) {
        "ALL" -> all
        "TASKS" -> tasks
        "STUDY" -> study
        "HABITS" -> habits
        "FOCUS" -> focusTab
        "LIFE" -> life
        "LIBRARY" -> library
        "SYSTEM" -> when (lang) {
            NexaLang.AR -> "النظام"
            NexaLang.FR -> "Système"
            NexaLang.EN -> "System"
        }
        else -> key
    }

    fun duaCategoryDisplay(key: String): String = when (key.uppercase()) {
        "ALL" -> all
        "STUDY" -> when (lang) {
            NexaLang.AR -> "طلب العلم"
            NexaLang.FR -> "Études"
            NexaLang.EN -> "Study"
        }
        "CALM" -> when (lang) {
            NexaLang.AR -> "السكينة"
            NexaLang.FR -> "Sérénité"
            NexaLang.EN -> "Calm"
        }
        "PARENTS" -> when (lang) {
            NexaLang.AR -> "الوالدين"
            NexaLang.FR -> "Parents"
            NexaLang.EN -> "Parents"
        }
        "GUIDANCE" -> when (lang) {
            NexaLang.AR -> "التوفيق"
            NexaLang.FR -> "Guidance"
            NexaLang.EN -> "Guidance"
        }
        "DAILY" -> recDaily
        else -> key
    }

    fun habitRoutineDisplay(routine: String): String = when (routine) {
        "Morning" -> when (lang) {
            NexaLang.AR -> "الصباح"
            NexaLang.FR -> "Matin"
            NexaLang.EN -> "Morning"
        }
        "Study" -> when (lang) {
            NexaLang.AR -> "الدراسة"
            NexaLang.FR -> "Étude"
            NexaLang.EN -> "Study"
        }
        "Evening" -> when (lang) {
            NexaLang.AR -> "المساء"
            NexaLang.FR -> "Soir"
            NexaLang.EN -> "Evening"
        }
        else -> routine
    }

    fun reviewModeDisplay(mode: String): String = when (mode.uppercase()) {
        "FLASHCARD" -> flashcards
        "TEST_MEMORY" -> testYourMemory
        else -> mode
    }

    fun frequencyDisplay(freq: String): String = when (freq.uppercase()) {
        "DAILY" -> recDaily
        "WEEKLY" -> recWeekly
        "MONTHLY" -> recMonthly
        "CUSTOM" -> recCustom
        else -> freq
    }

    fun emotionDisplay(key: String): String = when (key.trim()) {
        "Calm" -> when (lang) { NexaLang.AR -> "هادئ"; NexaLang.FR -> "Calme"; NexaLang.EN -> "Calm" }
        "Grateful" -> when (lang) { NexaLang.AR -> "ممتن"; NexaLang.FR -> "Reconnaissant"; NexaLang.EN -> "Grateful" }
        "Focused" -> when (lang) { NexaLang.AR -> "مركّز"; NexaLang.FR -> "Concentré"; NexaLang.EN -> "Focused" }
        "Tired" -> when (lang) { NexaLang.AR -> "متعب"; NexaLang.FR -> "Fatigué"; NexaLang.EN -> "Tired" }
        "Anxious" -> when (lang) { NexaLang.AR -> "قلق"; NexaLang.FR -> "Anxieux"; NexaLang.EN -> "Anxious" }
        "Hopeful" -> when (lang) { NexaLang.AR -> "متفائل"; NexaLang.FR -> "Optimiste"; NexaLang.EN -> "Hopeful" }
        else -> key
    }

    fun localizeEmotionsCsv(csv: String): String =
        csv.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(if (lang == NexaLang.AR) "، " else ", ") { emotionDisplay(it) }

    fun serenityPhraseFor(selectedEmotions: Set<String>): Pair<String, String> {
        if (selectedEmotions.isEmpty()) {
            return when (lang) {
                NexaLang.AR -> "مساحة السكينة" to "اختر شعورك الحالي لتظهر لك عبارة مخصصة تلامس لحظتك بهدوء."
                NexaLang.FR -> "Espace Sérénité" to "Sélectionnez votre émotion pour afficher une phrase apaisante adaptée."
                NexaLang.EN -> "Serenity Space" to "Select how you feel to view a calming phrase tailored to your state."
            }
        }
        val primary = listOf("Anxious", "Tired", "Focused", "Calm", "Grateful", "Hopeful")
            .firstOrNull { it in selectedEmotions } ?: selectedEmotions.first()
        val emotionTags = localizeEmotionsCsv(selectedEmotions.joinToString(","))
        val phrase = when (primary) {
            "Calm" -> when (lang) {
                NexaLang.AR -> "السكينة نعمة عظيمة؛ حافظ على هذا الإيقاع المتزن في يومك، فصفاء الذهن يصنع إنجازاً هادئاً يدوم."
                NexaLang.FR -> "La sérénité est une force : gardez ce rythme paisible pour accomplir vos objectifs avec clarté."
                NexaLang.EN -> "Calmness is quiet strength; let this steady clarity guide meaningful progress today."
            }
            "Grateful" -> when (lang) {
                NexaLang.AR -> "الامتنان يزيد البركة في الوقت والجهد؛ تذكّر خطواتك المنجزة ولو بدت صغيرة فكلها ترفعك."
                NexaLang.FR -> "La gratitude multiplie la bénédiction dans vos efforts ; chaque petit pas accompli compte."
                NexaLang.EN -> "Gratitude deepens peace and turns simple daily efforts into lasting fulfillment."
            }
            "Focused" -> when (lang) {
                NexaLang.AR -> "حضورك الذهني الآن ثروة ثمينة؛ وجّه تركيزك لمهمة واحدة واضحة ودع التشتت خارج المساحة."
                NexaLang.FR -> "Votre clarté d'esprit est précieuse : avancez sur une priorité à la fois sans dispersion."
                NexaLang.EN -> "Your present clarity is powerful; protect one meaningful task at a time."
            }
            "Tired" -> when (lang) {
                NexaLang.AR -> "التعب رسالة لطيفة للاستراحة لا للتراجع؛ خذ نفساً عميقاً، اشرب ماءً، ولا تقسُ على نفسك اليوم."
                NexaLang.FR -> "La fatigue demande de la douceur, pas de la culpabilité ; accordez-vous une vraie pause sereine."
                NexaLang.EN -> "Tiredness is a gentle call to rest, not defeat. Breathe deeply and honor a lighter pace."
            }
            "Anxious" -> when (lang) {
                NexaLang.AR -> "توقّف لحظة وتنفّس ببطء: لست مطالباً بحل كل شيء الآن. اختر خطوة واحدة صغيرة ودع الباقي يتيسّر."
                NexaLang.FR -> "Respirez lentement : vous n'avez pas à tout résoudre maintenant. Un seul petit pas suffit."
                NexaLang.EN -> "Pause and breathe slowly: you don't have to solve everything at once. Take just one calm step."
            }
            "Hopeful" -> when (lang) {
                NexaLang.AR -> "التفاؤل يفتح أبواب السعي؛ ابدأ خطوتك الآن وثِق أن الاستمرار الهادئ يقودك لنتائج طيبة."
                NexaLang.FR -> "L'espoir éclaire le chemin : commencez doucement et faites confiance à la constance."
                NexaLang.EN -> "Hope lights the path forward; begin gently now and trust steady daily steps."
            }
            else -> ""
        }
        return emotionTags to phrase
    }

    fun achievementTitle(id: String): String = when (id) {
        "first_task" -> when (lang) { NexaLang.AR -> "أول خطوة"; NexaLang.FR -> "Premier pas"; NexaLang.EN -> "First Step" }
        "tasks_10" -> when (lang) { NexaLang.AR -> "منجز المهام"; NexaLang.FR -> "Maître des tâches"; NexaLang.EN -> "Task Achiever" }
        "study_subject" -> when (lang) { NexaLang.AR -> "مؤسس المعرفة"; NexaLang.FR -> "Organisateur d'études"; NexaLang.EN -> "Study Architect" }
        "study_reviews_20" -> when (lang) { NexaLang.AR -> "ذاكرة متقدة"; NexaLang.FR -> "Mémoire vive"; NexaLang.EN -> "Sharp Recall" }
        "habit_streak_7" -> when (lang) { NexaLang.AR -> "أسبوع التزام"; NexaLang.FR -> "Série de 7 jours"; NexaLang.EN -> "7-Day Rhythm" }
        "focus_60m" -> when (lang) { NexaLang.AR -> "ساعة صفاء"; NexaLang.FR -> "Heure de focus"; NexaLang.EN -> "Hour of Clarity" }
        "focus_600m" -> when (lang) { NexaLang.AR -> "سيد التركيز"; NexaLang.FR -> "Maître du Focus"; NexaLang.EN -> "Deep Focus Master" }
        "life_adhkar_7" -> when (lang) { NexaLang.AR -> "قلب ذاكر"; NexaLang.FR -> "Cœur serein"; NexaLang.EN -> "Steadfast Dhikr" }
        "library_reader" -> when (lang) { NexaLang.AR -> "رفيق الكتاب"; NexaLang.FR -> "Lecteur assidu"; NexaLang.EN -> "Thoughtful Reader" }
        else -> sectionDisplayName(id)
    }

    fun achievementDesc(id: String): String = when (id) {
        "first_task" -> when (lang) {
            NexaLang.AR -> "أتمّ مهمتك الأولى لتبدأ رحلة الإنجاز"
            NexaLang.FR -> "Terminez votre première tâche"
            NexaLang.EN -> "Complete your first task"
        }
        "tasks_10" -> when (lang) {
            NexaLang.AR -> "أتمّ ١٠ مهام بنجاح واستمرارية"
            NexaLang.FR -> "Terminez 10 tâches avec régularité"
            NexaLang.EN -> "Complete 10 tasks consistently"
        }
        "study_subject" -> when (lang) {
            NexaLang.AR -> "أضف أول مادة دراسية لتنظيم دروسك"
            NexaLang.FR -> "Ajoutez votre première matière d'étude"
            NexaLang.EN -> "Add your first study subject"
        }
        "study_reviews_20" -> when (lang) {
            NexaLang.AR -> "راجع ٢٠ بطاقة أو اختبار ذاكرة محلي"
            NexaLang.FR -> "Révisez 20 cartes mémoire"
            NexaLang.EN -> "Complete 20 spaced review items"
        }
        "habit_streak_7" -> when (lang) {
            NexaLang.AR -> "حافظ على عادة يومية لمدة ٧ أيام"
            NexaLang.FR -> "Maintenez une habitude pendant 7 jours"
            NexaLang.EN -> "Maintain a 7-day habit streak"
        }
        "focus_60m" -> when (lang) {
            NexaLang.AR -> "اجمع ٦٠ دقيقة في جلسات التركيز العميق"
            NexaLang.FR -> "Accumulez 60 minutes de concentration"
            NexaLang.EN -> "Reach 60 minutes of deep focus"
        }
        "focus_600m" -> when (lang) {
            NexaLang.AR -> "اجمع ١٠ ساعات (٦٠٠ دقيقة) من التركيز"
            NexaLang.FR -> "Atteignez 600 minutes de concentration"
            NexaLang.EN -> "Accumulate 600 minutes of deep focus"
        }
        "life_adhkar_7" -> when (lang) {
            NexaLang.AR -> "أكمل ٧ أذكار من أذكارك اليومية"
            NexaLang.FR -> "Complétez 7 adhkar quotidiens"
            NexaLang.EN -> "Complete 7 daily remembrances"
        }
        "library_reader" -> when (lang) {
            NexaLang.AR -> "ابدأ بقراءة أول كتاب في مكتبتك"
            NexaLang.FR -> "Commencez la lecture d'un livre"
            NexaLang.EN -> "Start reading a book in your library"
        }
        else -> ""
    }

    fun backupStatusLabel(status: String): String = when (status.uppercase()) {
        "VERIFIED", "OK", "CREATED" -> when (lang) { NexaLang.AR -> "متحقق"; NexaLang.FR -> "Vérifiée"; NexaLang.EN -> "Verified" }
        else -> status
    }

    fun iconStyleDisplay(style: String): String = when (style) {
        "ROUNDED" -> when (lang) { NexaLang.AR -> "دائري"; NexaLang.FR -> "Arrondi"; NexaLang.EN -> "Rounded" }
        "SHARP" -> when (lang) { NexaLang.AR -> "حاد"; NexaLang.FR -> "Net"; NexaLang.EN -> "Sharp" }
        "MINIMAL" -> when (lang) { NexaLang.AR -> "هادئ"; NexaLang.FR -> "Minimal"; NexaLang.EN -> "Minimal" }
        else -> style
    }

    companion object {
        private val arInstance by lazy { ar() }
        private val enInstance by lazy { en() }
        private val frInstance by lazy { fr() }

        fun forLang(lang: NexaLang): NexaStrings = when (lang) {
            NexaLang.AR -> arInstance
            NexaLang.EN -> enInstance
            NexaLang.FR -> frInstance
        }

        private fun ar() = NexaStrings(
            lang = NexaLang.AR,
            appName = "NEXA",
            home = "الرئيسية",
            tasks = "مهامي",
            study = "المواد",
            studyOrganizer = "تنظيم الدراسة",
            habits = "عاداتي",
            focus = "وقتي",
            focusSpace = "مساحة التركيز",
            summaries = "الملخصات",
            life = "حياتي",
            library = "مكتبتي",
            audioPlayer = "مشغل الصوت",
            achievements = "الإنجازات والأوسمة",
            badges = "الأوسمة",
            statistics = "الإحصائيات",
            notifications = "الإشعارات",
            settings = "الإعدادات",
            greetingMorning = "صباح السكينة والإنجاز",
            greetingAfternoon = "طاب يومك بالتركيز",
            greetingEvening = "مساء الهدوء والتأمل",
            dailyProgress = "تقدّم اليوم",
            todayAchievements = "إنجازات اليوم",
            quoteOfTheDay = "اقتباس ملهم",
            nextQuote = "اقتباس آخر",
            editProfile = "تعديل الهوية الشخصية",
            userNameLabel = "الاسم",
            userTaglineLabel = "شعارك الدراسي أو الشخصي",
            defaultUserName = "طالب مجتهد",
            defaultUserTagline = "خطوة هادئة كل يوم تصنع أثراً عظيماً",
            todayTasksCount = "مهام نشطة",
            focusTodayMinutes = "دقائق التركيز اليوم",
            levelLabel = "المستوى",
            xpLabel = "نقاط الخبرة XP",
            quickShortcuts = "الأقسام والاختصارات",
            customShortcuts = "اختصارات مخصصة",
            editHomeLayout = "تخصيص البطاقات",
            doneEditing = "إنهاء التعديل",
            emptyTasksHint = "لا توجد مهام بعد — أضف أول مهمة ليومك بهدوء.",
            sectionTasksDesc = "تنظيم مهام اليوم، الأولويات والمواعيد بسلاسة",
            sectionStudyDesc = "إدارة المواد، الدروس، المهام والأهداف في مساحة واحدة",
            sectionHabitsDesc = "روتينك اليومي وسلاسل الالتزام دون أي ضغط",
            sectionFocusDesc = "ساعة رملية تفاعلية، جلسات التركيز العميق وحارس التركيز",
            sectionPrayerDesc = "المواقيت الفلكية، اتجاه القبلة، الأذكار والأدعية",
            sectionSummariesDesc = "مذكرات الدروس، بطاقات المراجعة، الاقتباسات والمكتبة",
            sectionLifeDesc = "عاداتك الهادئة، دفتر السكينة وتطورك الشخصي",
            customizeSectionImage = "تخصيص صورة القسم",
            customizeFocusBg = "تخصيص خلفية الساعة",
            chooseFromGallery = "اختيار صورة من المعرض",
            restoreDefaultImage = "استعادة الصورة الافتراضية",
            customImageActive = "صورة مخصصة محفوظة محلياً",
            defaultImageActive = "الصورة الافتراضية للقسم",
            lessonNotesSummary = "مذكرات الدروس",
            allFlashcardsDeck = "بنك البطاقات",
            noLessonNotesYet = "أضف دروساً واكتب ملخصاتك في المذكرة المسطّرة لتظهر جميعها هنا.",
            openLesson = "فتح الدرس",
            habitsAndRoutines = "العادات والروتينات",
            add = "إضافة",
            save = "حفظ",
            cancel = "إلغاء",
            delete = "حذف",
            edit = "تعديل",
            close = "إغلاق",
            search = "بحث...",
            filter = "تصفية",
            all = "الكل",
            back = "رجوع",
            confirm = "تأكيد",
            restore = "استعادة",
            notes = "ملاحظات",
            title = "العنوان",
            description = "التفاصيل",
            category = "التصنيف",
            priority = "الأهمية",
            low = "منخفضة",
            medium = "متوسطة",
            high = "عالية",
            urgent = "عاجلة",
            tasksToday = "اليوم",
            tasksUpcoming = "القادمة",
            tasksOverdue = "المتأخرة",
            tasksCompleted = "المكتملة",
            newTask = "مهمة جديدة",
            recurrence = "التكرار",
            recNone = "بدون تكرار",
            recDaily = "يومي",
            recWeekly = "أسبوعي",
            recMonthly = "شهري",
            recCustom = "مخصص",
            attachFile = "إرفاق ملف",
            reminderLabel = "تذكير",
            snooze15m = "تأجيل ١٥ د",
            snooze1h = "تأجيل ساعة",
            snoozeTomorrow = "تأجيل للغد",
            focusGuardOnDue = "تفعيل حارس التركيز عند الموعد",
            reopenTask = "إعادة المهمة",
            subjects = "المواد",
            schedule = "الجدول",
            gradesAndGpa = "المعدلات والنتائج",
            goalsAndStats = "الأهداف والإحصائيات",
            addSubject = "إضافة مادة",
            coefficient = "المعامل",
            targetAverage = "المعدل المستهدف",
            lessons = "الدروس",
            addLesson = "إضافة درس",
            attachPdf = "إرفاق PDF",
            openPdf = "عرض PDF",
            youtubeLink = "رابط YouTube",
            sideNoteTitle = "مذكرة الدرس المسطرة",
            sideNoteHint = "اكتب ملخصك هنا (يُحفظ تلقائياً)... استخدم «المصطلح: الشرح» لاقتراح بطاقات محلياً.",
            flashcards = "بطاقات المراجعة",
            addFlashcard = "بطاقة جديدة",
            suggestFromNotes = "اقتراح محلي من المذكرة",
            spacedReview = "المراجعة المتباعدة",
            testYourMemory = "اختبر ذاكرتك",
            testMemoryDisclaimer = "ميزة محلية ١٠٠٪ بدون ذكاء اصطناعي — التقييم تقريبي للمساعدة على التذكر الذاتي.",
            again = "أعد",
            hard = "صعب",
            good = "جيد",
            easy = "سهل",
            reviewHistory = "سجل المراجعات",
            weeklySchedule = "الجدول الأسبوعي",
            exceptionSchedule = "جدول استثنائي مؤقت",
            noAiStudyNote = "قسم دراستي يعمل محلياً بالكامل دون ذكاء اصطناعي.",
            weightedAverage = "المعدل الموزون",
            routines = "الروتينات",
            addHabit = "عادة جديدة",
            measureCompletion = "إنجاز",
            measureCount = "عدد",
            measureDuration = "وقت (دقائق)",
            streakDays = "سلسلة الالتزام",
            bestStreak = "أفضل سلسلة",
            restDay = "يوم راحة",
            noPenaltyNote = "لا عقوبات عند تفويت عادة — استأنف بلطف متى استطعت.",
            attachPhoto = "إرفاق صورة من المعرض",
            pomodoro = "بومودورو",
            customTimer = "مؤقت مخصص",
            openSession = "جلسة مفتوحة",
            startFocus = "ابدأ التركيز",
            pauseFocus = "إيقاف مؤقت",
            resumeFocus = "استئناف",
            finishFocus = "إنهاء الجلسة",
            ambientSound = "صوت هادئ",
            linkTaskOrLesson = "ربط بمهمة أو درس",
            focusGuard = "حارس التركيز",
            blockedApps = "التطبيقات المحجوبة",
            selectAppsToBlock = "اختيار التطبيقات المحجوبة",
            dailyLimitMinutes = "الحد اليومي (دقائق)",
            countdown30sNote = "إشعار عد تنازلي ٣٠ ثانية قبل الحجب",
            protectionStatus = "حالة الحماية والصلاحيات",
            oemBatteryDisclaimer = "تنبيه: بسبب قيود نظام Android والشركات المصنّعة وإدارة البطارية، لا يمكن ضمان حجب مطلق أو عمل مستمر بنسبة ١٠٠٪ في كل الأجهزة.",
            emergencyExit = "زر الطوارئ الدائم",
            confirmEarlyExit = "هل تريد إنهاء الحجب مبكراً؟",
            undoGraceTitle = "مهلة التراجع متاحة",
            blockScreenCustomizer = "تخصيص شاشة المنع",
            prayer = "الصلاة",
            adhkar = "الأذكار",
            duas = "الأدعية",
            serenity = "السكينة",
            islamicOccasions = "المناسبات",
            cityLabel = "المدينة",
            calcMethod = "طريقة الحساب",
            qiblaDirection = "اتجاه القبلة",
            adhkarMorning = "الصباح",
            adhkarEvening = "المساء",
            adhkarAfterPrayer = "بعد الصلاة",
            adhkarSleep = "النوم",
            adhkarWakeup = "الاستيقاظ",
            serenityDisclaimer = "مساحة تأمل شخصية محفوظة محلياً فقط — للمتابعة الذاتية وليست أداة تشخيص.",
            logMoodPrompt = "كيف تشعر الآن؟ (يمكن اختيار أكثر من شعور)",
            weeklyMonthlyPatterns = "الأنماط الأسبوعية والشهرية",
            books = "الكتب",
            novels = "الروايات",
            podcasts = "البودكاست",
            favorites = "المفضلة",
            shelfReading = "أقرأ الآن",
            shelfCompleted = "أنهيتها",
            shelfWantToRead = "أريد قراءتها",
            savedQuotes = "الاقتباسات المحفوظة",
            saveQuote = "حفظ اقتباس",
            importBook = "استيراد كتاب/ملف نصي",
            paperColor = "لون الورق",
            fontSize = "حجم الخط",
            downloadOffline = "تنزيل دون إنترنت",
            downloaded = "متاح دون إنترنت",
            playbackSpeed = "سرعة التشغيل",
            sleepTimer = "مؤقت النوم",
            discover = "اكتشف",
            listeningHistory = "سجل الاستماع",
            importLocalAudio = "استيراد ملف صوتي MP3",
            playlistQuran = "القرآن الكريم",
            playlistAdhkar = "الأذكار",
            playlistCalm = "الهدوء",
            playlistCustom = "قوائمي",
            musicPauseRuleNote = "الموسيقى والتلاوات تتوقف تلقائياً عند الخروج أو قفل الهاتف وتستأنف من الموضع نفسه.",
            podcastBgRuleNote = "البودكاست يستمر بالعمل في الخلفية ومن شاشة القفل.",
            customizationStudio = "استوديو التخصيص",
            designProfiles = "ملفات التصميم (حتى ١٠)",
            createProfile = "ملف تصميم جديد",
            restoreDefaultDesign = "استعادة التصميم الافتراضي",
            globalEditMode = "وضع التعديل الشامل",
            themeMode = "مظهر التطبيق",
            themeSystem = "تلقائي",
            themeLight = "فاتح",
            themeDark = "داكن",
            fontFamily = "الخط",
            iconStyle = "نمط الأيقونات",
            languageLabel = "اللغة",
            accountAndPrivacy = "الحساب والخصوصية",
            offlineFirstNotice = "تطبيق NEXA يعمل بالكامل محلياً (Offline First) ولا يتطلب أي حساب للاستخدام. المزامنة السحابية غير مفعّلة، وتتوفر النسخ الاحتياطية المشفرة محلياً.",
            setPin = "رمز PIN",
            biometricLock = "فتح بالبصمة",
            lockedSections = "قفل الأقسام",
            autoLockTimeout = "القفل التلقائي",
            hideNotifContent = "إخفاء محتوى الإشعارات",
            encryptedBackups = "النسخ الاحتياطي المشفر",
            createBackupNow = "إنشاء نسخة مشفرة الآن",
            exportBackupFile = "تصدير نسخة مشفرة",
            restoreBackupFile = "استعادة من ملف مشفر",
            autoBackupToggle = "نسخ احتياطي تلقائي دوري",
            appStatus = "حالة التطبيق",
            storageUsage = "مساحة التخزين المحلية",
            permissionsHealth = "الصلاحيات والخدمات",
            selfComparisonNote = "إحصائياتك تقارن تقدّمك الحالي بإنجازاتك السابقة فقط.",
            extra = NexaExtraStrings(
                focusTab = "تركيز",
            focusReports = "تقارير التركيز",
            prayerBlockTitle = "حجب التطبيقات أثناء وقت الصلاة",
            prayerBlockDesc = "يعمل عبر حارس التركيز لمدة ١٥ دقيقة من دخول وقت الصلاة على التطبيقات المختارة.",
            offlineLocalBadge = "يعمل محلياً ١٠٠٪",
            minutesInputLabel = "المدة (بالدقائق)",
            minutesUnit = "دقيقة",
            targetLabel = "الهدف",
            unitLabel = "الوحدة",
            scoreLabel = "الدرجة",
            maxScoreLabel = "العظمى",
            showAnswerLabel = "إظهار الإجابة",
            nextItemLabel = "التالي",
            tapToFlipLabel = "اضغط لإظهار الإجابة",
            flashcardFrontLabel = "السؤال / الوجه الأمامي",
            flashcardBackLabel = "الإجابة / الوجه الخلفي",
            accessibilityServiceLabel = "خدمة إمكانية الوصول (مطلوبة للحجب)",
            usageStatsLabel = "صلاحية إحصاءات الاستخدام",
            batteryUnrestrictedLabel = "استثناء إدارة البطارية",
            accessibilityRequiredHint = "لتفعيل حجب التطبيقات فعلياً، يرجى تشغيل خدمة «حارس التركيز NEXA» من إعدادات إمكانية الوصول.",
            openSettingsLabel = "فتح الإعدادات",
            enabledLabel = "مفعّل",
            disabledLabel = "غير مفعّل",
            yesLabel = "نعم",
            noLabel = "لا",
            fixNowLabel = "تفعيل",
            soundLabel = "الصوت",
            vibrationLabel = "الاهتزاز",
            dndLabel = "عدم الإزعاج",
            markAllReadLabel = "تحديد الكل كمقروء",
            unreadBadgeSuffix = "جديد",
            noNewNotifications = "لا إشعارات",
            sessionsCountLabel = "جلسة",
            dailyGoalPrefix = "الهدف اليومي",
            pausedMinutesExcluded = "توقف مؤقت %d د (مستبعدة)",
            colorPaletteLabel = "لوحة الألوان",
            unlockedLabel = "مفتوح",
            lockedLabel = "قيد التقدّم",
            completedLabel = "مكتمل",
            serenitySelectPrompt = "اختر شعورك لتظهر عبارة مخصصة:",
            serenityGuidanceLabel = "عبارة السكينة المخصصة لحالتك",
            last7DaysLabel = "آخر ٧ أيام",
            last30DaysLabel = "آخر ٣٠ يوماً",
            focus7dLabel = "التركيز (آخر ٧ أيام)",
            focus30dLabel = "التركيز (آخر ٣٠ يوماً)",
            tasksDone7dLabel = "المهام المنجزة (آخر ٧ أيام)",
            habitsLoggedLabel = "العادات المسجّلة",
            vsPreviousLabel = "مقابل السابق",
            databaseLabel = "قاعدة البيانات",
            appFilesLabel = "ملفات التطبيق والتنزيلات",
            cacheLabel = "الذاكرة المؤقتة",
            backupsCountLabel = "عدد النسخ الاحتياطية",
            notificationsPermissionLabel = "صلاحية الإشعارات",
            locationPermissionLabel = "صلاحية الموقع التقريبي (الصلاة)",
            offlineModeStatusNote = "وضع العمل المحلي مفعّل: قاعدة البيانات المحلية والنسخ المشفر AES-256-GCM يعملان دون سحابة.",
            pinRuleHint = "٤ إلى ٨ أرقام (ترك الحقل فارغاً يزيل القفل)",
            backupCreatedMsg = "✓ تم إنشاء النسخة الاحتياطية المشفرة محلياً",
            backupExportedMsg = "✓ تم تصدير النسخة الاحتياطية المشفرة",
            backupErrorMsg = "تعذّر إتمام النسخة الاحتياطية",
            previousTrack = "السابق",
            nextTrack = "التالي",
            returnHomeLabel = "العودة للرئيسية",
            invalidPinError = "رمز PIN غير صحيح",
            playAudioLabel = "تشغيل",
            resumeAudioLabel = "متابعة",
            pagePrefix = "صفحة",
            importedBookDefault = "كتاب مستورد",
            localImportDefault = "استيراد محلي",
            generalCategory = "عام",
            attachmentDefault = "مرفق",
            pdfPageDesc = "صفحة PDF",
            youtubeAppLabel = "يوتيوب",
            customRoutineLabel = "روتين مخصص"
            )
        )

        private fun en() = NexaStrings(
            lang = NexaLang.EN,
            appName = "NEXA",
            home = "Home",
            tasks = "Tasks",
            study = "Subjects",
            studyOrganizer = "Study Hub",
            habits = "Habits",
            focus = "Focus",
            focusSpace = "Focus Space",
            summaries = "Summaries",
            life = "My Life",
            library = "Library",
            audioPlayer = "Audio",
            achievements = "Badges & Stats",
            badges = "Badges",
            statistics = "Statistics",
            notifications = "Notifications",
            settings = "Settings",
            greetingMorning = "Calm Morning & Clarity",
            greetingAfternoon = "Focused Afternoon",
            greetingEvening = "Peaceful Evening",
            dailyProgress = "Today's Progress",
            todayAchievements = "Today's Achievements",
            quoteOfTheDay = "Daily Inspiration",
            nextQuote = "Next Quote",
            editProfile = "Edit Profile Identity",
            userNameLabel = "Your Name",
            userTaglineLabel = "Personal Motto",
            defaultUserName = "Dedicated Scholar",
            defaultUserTagline = "Calm daily focus builds lasting mastery",
            todayTasksCount = "Active Tasks",
            focusTodayMinutes = "Focus Minutes Today",
            levelLabel = "Level",
            xpLabel = "XP",
            quickShortcuts = "Sections & Shortcuts",
            customShortcuts = "Custom Shortcuts",
            editHomeLayout = "Customize Cards",
            doneEditing = "Done",
            emptyTasksHint = "No tasks yet — add your first calm goal for today.",
            sectionTasksDesc = "Organize today's tasks, priorities, and reminders calmly",
            sectionStudyDesc = "Subjects, lessons, tasks, and academic goals in one hub",
            sectionHabitsDesc = "Daily routines and gentle streaks without penalties",
            sectionFocusDesc = "Realistic hourglass timer, deep focus sessions & Focus Guard",
            sectionPrayerDesc = "Astronomical prayer times, Qibla compass, Adhkar & Duas",
            sectionSummariesDesc = "Lesson notes, spaced flashcards, saved quotes & reading library",
            sectionLifeDesc = "Gentle habits, serenity reflections, and personal growth",
            customizeSectionImage = "Customize Section Image",
            customizeFocusBg = "Hourglass Background",
            chooseFromGallery = "Choose from Gallery",
            restoreDefaultImage = "Restore Default Image",
            customImageActive = "Custom local image active",
            defaultImageActive = "Default section artwork",
            lessonNotesSummary = "Lesson Notes",
            allFlashcardsDeck = "Flashcard Decks",
            noLessonNotesYet = "Add lessons and write summaries in the lined notebook to review them here.",
            openLesson = "Open Lesson",
            habitsAndRoutines = "Habits & Routines",
            add = "Add",
            save = "Save",
            cancel = "Cancel",
            delete = "Delete",
            edit = "Edit",
            close = "Close",
            search = "Search...",
            filter = "Filter",
            all = "All",
            back = "Back",
            confirm = "Confirm",
            restore = "Restore",
            notes = "Notes",
            title = "Title",
            description = "Details",
            category = "Category",
            priority = "Priority",
            low = "Low",
            medium = "Medium",
            high = "High",
            urgent = "Urgent",
            tasksToday = "Today",
            tasksUpcoming = "Upcoming",
            tasksOverdue = "Overdue",
            tasksCompleted = "Completed",
            newTask = "New Task",
            recurrence = "Recurrence",
            recNone = "None",
            recDaily = "Daily",
            recWeekly = "Weekly",
            recMonthly = "Monthly",
            recCustom = "Custom",
            attachFile = "Attach File",
            reminderLabel = "Reminder",
            snooze15m = "Snooze 15m",
            snooze1h = "Snooze 1h",
            snoozeTomorrow = "Tomorrow",
            focusGuardOnDue = "Activate Focus Guard when due",
            reopenTask = "Reopen Task",
            subjects = "Subjects",
            schedule = "Schedule",
            gradesAndGpa = "Grades & GPA",
            goalsAndStats = "Goals & Stats",
            addSubject = "Add Subject",
            coefficient = "Coefficient",
            targetAverage = "Target Avg",
            lessons = "Lessons",
            addLesson = "Add Lesson",
            attachPdf = "Attach PDF",
            openPdf = "View PDF",
            youtubeLink = "YouTube URL",
            sideNoteTitle = "Lined Study Side Note",
            sideNoteHint = "Write your summary (auto-saved)... Use 'Term: Definition' to suggest local flashcards.",
            flashcards = "Flashcards",
            addFlashcard = "New Card",
            suggestFromNotes = "Suggest Locally from Notes",
            spacedReview = "Spaced Review",
            testYourMemory = "Test Your Memory",
            testMemoryDisclaimer = "100% local feature without AI — score is approximate for self-recall practice.",
            again = "Again",
            hard = "Hard",
            good = "Good",
            easy = "Easy",
            reviewHistory = "Review History",
            weeklySchedule = "Weekly Schedule",
            exceptionSchedule = "Temporary Exception",
            noAiStudyNote = "My Study runs 100% locally without AI.",
            weightedAverage = "Weighted Average",
            routines = "Routines",
            addHabit = "New Habit",
            measureCompletion = "Check",
            measureCount = "Count",
            measureDuration = "Minutes",
            streakDays = "Streak",
            bestStreak = "Best Streak",
            restDay = "Rest Day",
            noPenaltyNote = "Zero penalties for missed habits — resume gently whenever ready.",
            attachPhoto = "Pick Photo",
            pomodoro = "Pomodoro",
            customTimer = "Custom",
            openSession = "Open Session",
            startFocus = "Start Focus",
            pauseFocus = "Pause",
            resumeFocus = "Resume",
            finishFocus = "Finish",
            ambientSound = "Ambient Sound",
            linkTaskOrLesson = "Link Task or Lesson",
            focusGuard = "Focus Guard",
            blockedApps = "Blocked Apps",
            selectAppsToBlock = "Choose Apps to Block",
            dailyLimitMinutes = "Daily Limit (min)",
            countdown30sNote = "30-second countdown notification before blocking",
            protectionStatus = "Protection & Permissions",
            oemBatteryDisclaimer = "Notice: Due to Android OS, OEM, and battery restrictions, 100% continuous or absolute app blocking cannot be guaranteed on all devices.",
            emergencyExit = "Emergency Exit",
            confirmEarlyExit = "End blocking early?",
            undoGraceTitle = "Undo grace period active",
            blockScreenCustomizer = "Customize Block Screen",
            prayer = "Prayer",
            adhkar = "Adhkar",
            duas = "Duas",
            serenity = "Serenity",
            islamicOccasions = "Occasions",
            cityLabel = "City",
            calcMethod = "Calculation Method",
            qiblaDirection = "Qibla",
            adhkarMorning = "Morning",
            adhkarEvening = "Evening",
            adhkarAfterPrayer = "After Prayer",
            adhkarSleep = "Sleep",
            adhkarWakeup = "Wake Up",
            serenityDisclaimer = "Private local reflection space — non-diagnostic self-check only.",
            logMoodPrompt = "How are you feeling? (Multi-select)",
            weeklyMonthlyPatterns = "Weekly & Monthly Patterns",
            books = "Books",
            novels = "Novels",
            podcasts = "Podcasts",
            favorites = "Favorites",
            shelfReading = "Reading",
            shelfCompleted = "Completed",
            shelfWantToRead = "Want to Read",
            savedQuotes = "Saved Quotes",
            saveQuote = "Save Quote",
            importBook = "Import Text Book",
            paperColor = "Paper Color",
            fontSize = "Font Size",
            downloadOffline = "Download Offline",
            downloaded = "Offline Ready",
            playbackSpeed = "Speed",
            sleepTimer = "Sleep Timer",
            discover = "Discover",
            listeningHistory = "History",
            importLocalAudio = "Import Local MP3",
            playlistQuran = "Quran",
            playlistAdhkar = "Adhkar",
            playlistCalm = "Calm",
            playlistCustom = "Custom",
            musicPauseRuleNote = "Music & recitations pause automatically on exit or lock and resume at the exact position.",
            podcastBgRuleNote = "Podcasts continue playing in background and lock screen.",
            customizationStudio = "Customization Studio",
            designProfiles = "Design Profiles (up to 10)",
            createProfile = "New Profile",
            restoreDefaultDesign = "Restore Default Design",
            globalEditMode = "Global Edit Mode",
            themeMode = "Theme Mode",
            themeSystem = "System",
            themeLight = "Light",
            themeDark = "Dark",
            fontFamily = "Font Family",
            iconStyle = "Icon Style",
            languageLabel = "Language",
            accountAndPrivacy = "Account & Privacy",
            offlineFirstNotice = "NEXA works 100% Offline-First without requiring an account. Cloud sync is inactive; AES-256-GCM encrypted local backups are available.",
            setPin = "App PIN",
            biometricLock = "Biometric Unlock",
            lockedSections = "Section Locks",
            autoLockTimeout = "Auto-Lock",
            hideNotifContent = "Hide Notification Content",
            encryptedBackups = "Encrypted Backups",
            createBackupNow = "Create Encrypted Backup",
            exportBackupFile = "Export Encrypted File",
            restoreBackupFile = "Restore Encrypted File",
            autoBackupToggle = "Periodic Auto Backup",
            appStatus = "App Status",
            storageUsage = "Local Storage Usage",
            permissionsHealth = "Permissions & Services",
            selfComparisonNote = "Statistics compare your current progress only with your own past self.",
            extra = NexaExtraStrings(
                focusTab = "Focus",
            focusReports = "Focus Reports",
            prayerBlockTitle = "Block Apps During Prayer Time",
            prayerBlockDesc = "Uses Focus Guard for 15 minutes from prayer time across your selected blocked apps.",
            offlineLocalBadge = "100% Offline",
            minutesInputLabel = "Minutes",
            minutesUnit = "min",
            targetLabel = "Target",
            unitLabel = "Unit",
            scoreLabel = "Score",
            maxScoreLabel = "Max",
            showAnswerLabel = "Show Answer",
            nextItemLabel = "Next",
            tapToFlipLabel = "Tap to flip",
            flashcardFrontLabel = "Question / Front",
            flashcardBackLabel = "Answer / Back",
            accessibilityServiceLabel = "Accessibility Service (Required)",
            usageStatsLabel = "Usage Stats Permission",
            batteryUnrestrictedLabel = "Battery Unrestricted",
            accessibilityRequiredHint = "To block apps actively, enable NEXA Focus Guard in Android Accessibility Settings.",
            openSettingsLabel = "Open Settings",
            enabledLabel = "ON",
            disabledLabel = "OFF",
            yesLabel = "YES",
            noLabel = "NO",
            fixNowLabel = "Enable",
            soundLabel = "Sound",
            vibrationLabel = "Vibration",
            dndLabel = "Do Not Disturb",
            markAllReadLabel = "Mark all read",
            unreadBadgeSuffix = "new",
            noNewNotifications = "All caught up",
            sessionsCountLabel = "sessions",
            dailyGoalPrefix = "Goal",
            pausedMinutesExcluded = "Paused %dm (excluded)",
            colorPaletteLabel = "Color Palette",
            unlockedLabel = "Unlocked",
            lockedLabel = "In Progress",
            completedLabel = "Completed",
            serenitySelectPrompt = "Tailored message for your state:",
            serenityGuidanceLabel = "Personalized Serenity Reflection",
            last7DaysLabel = "7d",
            last30DaysLabel = "30d",
            focus7dLabel = "Focus (7d)",
            focus30dLabel = "Focus (30d)",
            tasksDone7dLabel = "Tasks Done (7d)",
            habitsLoggedLabel = "Habits Logged",
            vsPreviousLabel = "prev",
            databaseLabel = "Database",
            appFilesLabel = "App Files & Downloads",
            cacheLabel = "Cache",
            backupsCountLabel = "Backups",
            notificationsPermissionLabel = "Notifications",
            locationPermissionLabel = "Coarse Location (Prayer)",
            offlineModeStatusNote = "Offline-First Mode: Local Room + AES-256-GCM Backups active. Cloud sync is inactive.",
            pinRuleHint = "4-8 digits (empty clears PIN)",
            backupCreatedMsg = "✓ Encrypted local backup created",
            backupExportedMsg = "✓ Exported encrypted backup",
            backupErrorMsg = "Backup operation failed",
            previousTrack = "Previous",
            nextTrack = "Next",
            returnHomeLabel = "Return Home",
            invalidPinError = "Invalid PIN",
            playAudioLabel = "Play",
            resumeAudioLabel = "Resume",
            pagePrefix = "Page",
            importedBookDefault = "Imported Book",
            localImportDefault = "Local Import",
            generalCategory = "General",
            attachmentDefault = "Attachment",
            pdfPageDesc = "PDF page",
            youtubeAppLabel = "YouTube",
            customRoutineLabel = "Custom Routine"
            )
        )

        private fun fr() = NexaStrings(
            lang = NexaLang.FR,
            appName = "NEXA",
            home = "Accueil",
            tasks = "Tâches",
            study = "Matières",
            studyOrganizer = "Études",
            habits = "Habitudes",
            focus = "Focus",
            focusSpace = "Concentration",
            summaries = "Résumés",
            life = "Ma Vie",
            library = "Bibliothèque",
            audioPlayer = "Audio",
            achievements = "Badges & Stats",
            badges = "Badges",
            statistics = "Statistiques",
            notifications = "Notifications",
            settings = "Paramètres",
            greetingMorning = "Matin serein et productif",
            greetingAfternoon = "Après-midi concentré",
            greetingEvening = "Soirée paisible",
            dailyProgress = "Progrès du jour",
            todayAchievements = "Réussites du jour",
            quoteOfTheDay = "Citation inspirante",
            nextQuote = "Autre citation",
            editProfile = "Modifier le profil",
            userNameLabel = "Votre nom",
            userTaglineLabel = "Devise personnelle",
            defaultUserName = "Étudiant dévoué",
            defaultUserTagline = "Un pas calme chaque jour mène à la maîtrise",
            todayTasksCount = "Tâches actives",
            focusTodayMinutes = "Minutes Focus aujourd'hui",
            levelLabel = "Niveau",
            xpLabel = "XP",
            quickShortcuts = "Sections & Raccourcis",
            customShortcuts = "Raccourcis personnalisés",
            editHomeLayout = "Personnaliser les cartes",
            doneEditing = "Terminer",
            emptyTasksHint = "Aucune tâche — ajoutez votre premier objectif du jour.",
            sectionTasksDesc = "Organisez vos tâches, priorités et rappels sereinement",
            sectionStudyDesc = "Matières, leçons, tâches et objectifs réunis",
            sectionHabitsDesc = "Routines quotidiennes et séries douces sans pénalité",
            sectionFocusDesc = "Sablier réaliste, sessions de concentration et Focus Guard",
            sectionPrayerDesc = "Horaires de prière, Qibla, Adhkar et douas",
            sectionSummariesDesc = "Notes de cours, cartes mémoire, citations & bibliothèque",
            sectionLifeDesc = "Habitudes douces, journal Sérénité et progrès personnel",
            customizeSectionImage = "Personnaliser l'image",
            customizeFocusBg = "Fond du sablier",
            chooseFromGallery = "Choisir dans la galerie",
            restoreDefaultImage = "Image par défaut",
            customImageActive = "Image locale personnalisée",
            defaultImageActive = "Image par défaut de la section",
            lessonNotesSummary = "Notes de cours",
            allFlashcardsDeck = "Paquets de cartes",
            noLessonNotesYet = "Ajoutez des leçons et rédigez vos résumés pour les retrouver ici.",
            openLesson = "Ouvrir la leçon",
            habitsAndRoutines = "Habitudes & Routines",
            add = "Ajouter",
            save = "Enregistrer",
            cancel = "Annuler",
            delete = "Supprimer",
            edit = "Modifier",
            close = "Fermer",
            search = "Rechercher...",
            filter = "Filtrer",
            all = "Tout",
            back = "Retour",
            confirm = "Confirmer",
            restore = "Restaurer",
            notes = "Notes",
            title = "Titre",
            description = "Détails",
            category = "Catégorie",
            priority = "Priorité",
            low = "Basse",
            medium = "Moyenne",
            high = "Haute",
            urgent = "Urgente",
            tasksToday = "Aujourd'hui",
            tasksUpcoming = "À venir",
            tasksOverdue = "En retard",
            tasksCompleted = "Terminées",
            newTask = "Nouvelle tâche",
            recurrence = "Récurrence",
            recNone = "Aucune",
            recDaily = "Quotidien",
            recWeekly = "Hebdomadaire",
            recMonthly = "Mensuel",
            recCustom = "Personnalisé",
            attachFile = "Joindre un fichier",
            reminderLabel = "Rappel",
            snooze15m = "Reporter 15m",
            snooze1h = "Reporter 1h",
            snoozeTomorrow = "Demain",
            focusGuardOnDue = "Activer Focus Guard à l'échéance",
            reopenTask = "Rouvrir la tâche",
            subjects = "Matières",
            schedule = "Emploi du temps",
            gradesAndGpa = "Notes & Moyenne",
            goalsAndStats = "Objectifs & Stats",
            addSubject = "Ajouter une matière",
            coefficient = "Coefficient",
            targetAverage = "Moyenne cible",
            lessons = "Leçons",
            addLesson = "Ajouter une leçon",
            attachPdf = "Joindre PDF",
            openPdf = "Lire PDF",
            youtubeLink = "Lien YouTube",
            sideNoteTitle = "Carnet ligné de la leçon",
            sideNoteHint = "Écrivez votre résumé (sauvegarde auto)... Utilisez 'Terme: Définition' pour suggérer des cartes.",
            flashcards = "Cartes mémoire",
            addFlashcard = "Nouvelle carte",
            suggestFromNotes = "Suggérer localement",
            spacedReview = "Révision espacée",
            testYourMemory = "Tester votre mémoire",
            testMemoryDisclaimer = "100% local sans IA — le score est approximatif pour l'auto-évaluation.",
            again = "À revoir",
            hard = "Difficile",
            good = "Bien",
            easy = "Facile",
            reviewHistory = "Historique",
            weeklySchedule = "Emploi hebdomadaire",
            exceptionSchedule = "Exception temporaire",
            noAiStudyNote = "Mes Études fonctionne 100% localement sans IA.",
            weightedAverage = "Moyenne pondérée",
            routines = "Routines",
            addHabit = "Nouvelle habitude",
            measureCompletion = "Fait",
            measureCount = "Compteur",
            measureDuration = "Minutes",
            streakDays = "Série",
            bestStreak = "Meilleure série",
            restDay = "Jour de repos",
            noPenaltyNote = "Aucune pénalité en cas d'oubli — reprenez en douceur.",
            attachPhoto = "Choisir une photo",
            pomodoro = "Pomodoro",
            customTimer = "Personnalisé",
            openSession = "Session ouverte",
            startFocus = "Démarrer",
            pauseFocus = "Pause",
            resumeFocus = "Reprendre",
            finishFocus = "Terminer",
            ambientSound = "Son ambiant",
            linkTaskOrLesson = "Lier tâche ou leçon",
            focusGuard = "Focus Guard",
            blockedApps = "Applications bloquées",
            selectAppsToBlock = "Choisir les apps à bloquer",
            dailyLimitMinutes = "Limite quotidienne (min)",
            countdown30sNote = "Compte à rebours de 30s avant le blocage",
            protectionStatus = "Statut & Permissions",
            oemBatteryDisclaimer = "Remarque : En raison des restrictions Android, constructeurs et batterie, un blocage continu à 100% ne peut être garanti sur tous les appareils.",
            emergencyExit = "Sortie d'urgence",
            confirmEarlyExit = "Arrêter le blocage ?",
            undoGraceTitle = "Délai d'annulation actif",
            blockScreenCustomizer = "Personnaliser l'écran de blocage",
            prayer = "Prière",
            adhkar = "Adhkar",
            duas = "Douas",
            serenity = "Sérénité",
            islamicOccasions = "Occasions",
            cityLabel = "Ville",
            calcMethod = "Méthode de calcul",
            qiblaDirection = "Qibla",
            adhkarMorning = "Matin",
            adhkarEvening = "Soir",
            adhkarAfterPrayer = "Après la prière",
            adhkarSleep = "Sommeil",
            adhkarWakeup = "Réveil",
            serenityDisclaimer = "Espace personnel local — non médical et sans diagnostic.",
            logMoodPrompt = "Comment vous sentez-vous ?",
            weeklyMonthlyPatterns = "Tendances hebdo & mensuelles",
            books = "Livres",
            novels = "Romans",
            podcasts = "Podcasts",
            favorites = "Favoris",
            shelfReading = "En cours",
            shelfCompleted = "Terminés",
            shelfWantToRead = "À lire",
            savedQuotes = "Citations",
            saveQuote = "Enregistrer citation",
            importBook = "Importer un livre texte",
            paperColor = "Couleur du papier",
            fontSize = "Taille du texte",
            downloadOffline = "Télécharger hors ligne",
            downloaded = "Hors ligne",
            playbackSpeed = "Vitesse",
            sleepTimer = "Minuteur de veille",
            discover = "Découvrir",
            listeningHistory = "Historique",
            importLocalAudio = "Importer MP3 local",
            playlistQuran = "Coran",
            playlistAdhkar = "Adhkar",
            playlistCalm = "Calme",
            playlistCustom = "Mes listes",
            musicPauseRuleNote = "Musique et récitations se mettent en pause en quittant et reprennent au même point.",
            podcastBgRuleNote = "Les podcasts continuent en arrière-plan et sur l'écran verrouillé.",
            customizationStudio = "Studio de personnalisation",
            designProfiles = "Profils de design (max 10)",
            createProfile = "Nouveau profil",
            restoreDefaultDesign = "Design par défaut",
            globalEditMode = "Mode édition global",
            themeMode = "Thème",
            themeSystem = "Système",
            themeLight = "Clair",
            themeDark = "Sombre",
            fontFamily = "Police",
            iconStyle = "Style d'icônes",
            languageLabel = "Langue",
            accountAndPrivacy = "Compte & Confidentialité",
            offlineFirstNotice = "NEXA fonctionne 100% hors ligne sans compte requis. La synchronisation cloud est inactive ; les sauvegardes locales chiffrées AES-256-GCM sont actives.",
            setPin = "Code PIN",
            biometricLock = "Biométrie",
            lockedSections = "Verrouillage des sections",
            autoLockTimeout = "Verrouillage auto",
            hideNotifContent = "Masquer le contenu des notifications",
            encryptedBackups = "Sauvegardes chiffrées",
            createBackupNow = "Créer une sauvegarde chiffrée",
            exportBackupFile = "Exporter fichier chiffré",
            restoreBackupFile = "Restaurer fichier chiffré",
            autoBackupToggle = "Sauvegarde auto périodique",
            appStatus = "État de l'application",
            storageUsage = "Stockage local",
            permissionsHealth = "Permissions & Services",
            selfComparisonNote = "Vos statistiques comparent vos progrès uniquement à votre propre historique.",
            extra = NexaExtraStrings(
                focusTab = "Focus",
            focusReports = "Rapports Focus",
            prayerBlockTitle = "Bloquer les apps pendant la prière",
            prayerBlockDesc = "Active Garde Focus pendant 15 minutes à l'heure de la prière sur vos apps sélectionnées.",
            offlineLocalBadge = "100% Hors ligne",
            minutesInputLabel = "Minutes",
            minutesUnit = "min",
            targetLabel = "Objectif",
            unitLabel = "Unité",
            scoreLabel = "Note",
            maxScoreLabel = "Max",
            showAnswerLabel = "Afficher",
            nextItemLabel = "Suivant",
            tapToFlipLabel = "Toucher pour retourner",
            flashcardFrontLabel = "Question / Recto",
            flashcardBackLabel = "Réponse / Verso",
            accessibilityServiceLabel = "Service d'accessibilité (Requis)",
            usageStatsLabel = "Statistiques d'utilisation",
            batteryUnrestrictedLabel = "Batterie sans restriction",
            accessibilityRequiredHint = "Activez Garde Focus NEXA dans l'accessibilité Android pour activer le blocage.",
            openSettingsLabel = "Paramètres",
            enabledLabel = "Activé",
            disabledLabel = "Désactivé",
            yesLabel = "Oui",
            noLabel = "Non",
            fixNowLabel = "Activer",
            soundLabel = "Son",
            vibrationLabel = "Vibration",
            dndLabel = "Ne pas déranger",
            markAllReadLabel = "Tout marquer lu",
            unreadBadgeSuffix = "nouv.",
            noNewNotifications = "Tout est lu",
            sessionsCountLabel = "sessions",
            dailyGoalPrefix = "Objectif",
            pausedMinutesExcluded = "Pause %dm (exclue)",
            colorPaletteLabel = "Palette de couleurs",
            unlockedLabel = "Débloqué",
            lockedLabel = "En cours",
            completedLabel = "Terminé",
            serenitySelectPrompt = "Message adapté à votre état :",
            serenityGuidanceLabel = "Réflexion Sérénité personnalisée",
            last7DaysLabel = "7j",
            last30DaysLabel = "30j",
            focus7dLabel = "Focus (7j)",
            focus30dLabel = "Focus (30j)",
            tasksDone7dLabel = "Tâches terminées (7j)",
            habitsLoggedLabel = "Habitudes enregistrées",
            vsPreviousLabel = "précédent",
            databaseLabel = "Base de données",
            appFilesLabel = "Fichiers & Téléchargements",
            cacheLabel = "Cache",
            backupsCountLabel = "Sauvegardes",
            notificationsPermissionLabel = "Notifications",
            locationPermissionLabel = "Localisation (Prière)",
            offlineModeStatusNote = "Mode hors ligne : Room local et sauvegardes AES-256-GCM actifs.",
            pinRuleHint = "4 à 8 chiffres (vide pour supprimer)",
            backupCreatedMsg = "✓ Sauvegarde chiffrée créée",
            backupExportedMsg = "✓ Sauvegarde chiffrée exportée",
            backupErrorMsg = "Erreur de sauvegarde",
            previousTrack = "Précédent",
            nextTrack = "Suivant",
            returnHomeLabel = "Accueil",
            invalidPinError = "Code PIN invalide",
            playAudioLabel = "Lire",
            resumeAudioLabel = "Reprendre",
            pagePrefix = "Page",
            importedBookDefault = "Livre importé",
            localImportDefault = "Import local",
            generalCategory = "Général",
            attachmentDefault = "Pièce jointe",
            pdfPageDesc = "Page PDF",
            youtubeAppLabel = "YouTube",
            customRoutineLabel = "Routine libre"
            )
        )
    }
}
