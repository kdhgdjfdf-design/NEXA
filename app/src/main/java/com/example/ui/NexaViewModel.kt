package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.NexaApplication
import com.example.data.local.*
import com.example.domain.security.CryptoAndLockManager
import com.example.domain.stats.XpAndStatsEngine
import com.example.domain.study.SpacedRepetitionEngine
import com.example.services.AutoBackupWorker
import com.example.services.ReminderScheduler
import com.example.services.widget.NexaWidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NexaMainViewModel(application: Application) : AndroidViewModel(application) {
    val container: AppContainer = (application as NexaApplication).container
    private val db = container.db
    val prefs = container.prefs
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val httpClient = OkHttpClient()

    val prefsState: StateFlow<NexaPrefsState> = prefs.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NexaPrefsState())

    val designProfiles: StateFlow<List<DesignProfileEntity>> = db.designDao().observeProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<DesignProfileEntity?> = combine(designProfiles, prefsState) { list, p ->
        list.firstOrNull { it.id == p.activeProfileId } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val tasks: StateFlow<List<TaskEntity>> = db.taskDao().observeAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<StudySubjectEntity>> = db.studyDao().observeSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lessons: StateFlow<List<StudyLessonEntity>> = db.studyDao().observeAllLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<StudyFlashcardEntity>> = db.studyDao().observeAllFlashcards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviewLogs: StateFlow<List<StudyReviewLogEntity>> = db.studyDao().observeReviewLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleSlots: StateFlow<List<StudyScheduleSlotEntity>> = db.studyDao().observeScheduleSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val grades: StateFlow<List<StudyGradeEntity>> = db.studyDao().observeGrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyGoals: StateFlow<List<StudyGoalEntity>> = db.studyDao().observeGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = db.habitDao().observeHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitLogs: StateFlow<List<HabitLogEntity>> = db.habitDao().observeHabitLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionEntity>> = db.focusDao().observeFocusSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedApps: StateFlow<List<BlockedAppEntity>> = db.focusDao().observeBlockedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val moodEntries: StateFlow<List<MoodEntryEntity>> = db.lifeDao().observeMoodEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayIso: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val adhkarProgress: StateFlow<List<AdhkarProgressEntity>> = db.lifeDao().observeAdhkarProgress(todayIso)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val duaFavorites: StateFlow<List<DuaFavoriteEntity>> = db.lifeDao().observeDuaFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val libraryItems: StateFlow<List<LibraryItemEntity>> = db.libraryDao().observeLibraryItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedQuotes: StateFlow<List<SavedQuoteEntity>> = db.libraryDao().observeQuotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val podcastEpisodes: StateFlow<List<PodcastEpisodeEntity>> = db.libraryDao().observePodcastEpisodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val audioTracks: StateFlow<List<AudioTrackEntity>> = db.audioDao().observeAudioTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotificationEntity>> = db.systemDao().observeNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementProgressEntity>> = db.systemDao().observeAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backupRecords: StateFlow<List<BackupRecordEntity>> = db.systemDao().observeBackups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Language & Customization ---
    fun setLanguage(lang: String) = viewModelScope.launch { prefs.setLanguage(lang) }
    fun setGlobalEditMode(enabled: Boolean) = viewModelScope.launch { prefs.setGlobalEditMode(enabled) }
    fun selectDesignProfile(id: Long) = viewModelScope.launch { prefs.setActiveProfileId(id) }

    fun updateActiveProfile(transform: (DesignProfileEntity) -> DesignProfileEntity) = viewModelScope.launch {
        val current = activeProfile.value ?: return@launch
        db.designDao().updateProfile(transform(current))
    }

    fun createDesignProfile(name: String) = viewModelScope.launch {
        if (designProfiles.value.size >= 10) return@launch
        val base = activeProfile.value ?: DefaultSeedData.defaultDesignProfiles().first()
        val id = db.designDao().insertProfile(
            base.copy(id = 0, name = name.ifBlank { "Custom ${designProfiles.value.size + 1}" }, isBuiltIn = false)
        )
        prefs.setActiveProfileId(id)
    }

    fun deleteDesignProfile(profile: DesignProfileEntity) = viewModelScope.launch {
        if (profile.isBuiltIn) return@launch
        db.designDao().deleteProfile(profile)
        val first = designProfiles.value.firstOrNull { it.id != profile.id }
        if (first != null) prefs.setActiveProfileId(first.id)
    }

    fun restoreDefaultDesign() = viewModelScope.launch {
        val def = designProfiles.value.firstOrNull { it.isBuiltIn } ?: return@launch
        db.designDao().updateProfile(
            def.copy(
                paletteKey = "NOX_ESPRESSO",
                themeMode = "SYSTEM",
                fontKey = "CAIRO",
                iconStyle = "ROUNDED",
                homeBannerUri = "",
                focusBlockImageUri = "",
                sectionImagesJson = "{}",
                cardConfigJson = DefaultSeedData.defaultHomeCardsJson,
                customShortcutsJson = "[]"
            )
        )
        prefs.setActiveProfileId(def.id)
    }

    fun updateUserProfile(context: Context, name: String, tagline: String, pickedAvatarUri: Uri?, resetAvatar: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            val currentAvatar = prefsState.value.userAvatarUri
            val finalAvatar = when {
                resetAvatar -> {
                    SectionImageCodec.deleteManagedFileIfOwned(context, currentAvatar)
                    ""
                }
                pickedAvatarUri != null -> {
                    val saved = copyPickedImageToInternal(context, pickedAvatarUri, "user_avatar")
                    if (saved.isNotBlank()) {
                        SectionImageCodec.deleteManagedFileIfOwned(context, currentAvatar)
                        saved
                    } else currentAvatar
                }
                else -> currentAvatar
            }
            prefs.setUserProfile(name.trim(), tagline.trim(), finalAvatar)
        }

    fun cycleQuote(nextIndex: Int) = viewModelScope.launch {
        val count = DefaultSeedData.motivationalQuotes.size.coerceAtLeast(1)
        prefs.setQuoteIndex(((nextIndex % count) + count) % count)
    }

    fun saveCustomQuoteText(quoteText: String, sourceTitle: String, author: String) =
        viewModelScope.launch(Dispatchers.IO) {
            if (quoteText.isBlank()) return@launch
            db.libraryDao().insertQuote(
                SavedQuoteEntity(
                    bookId = 0L,
                    bookTitle = sourceTitle.ifBlank { "NEXA" },
                    author = author.ifBlank { "NEXA" },
                    pageNumber = 1,
                    quoteText = quoteText.trim()
                )
            )
            prefs.addXp(5)
        }

    fun setFocusBackgroundImage(context: Context, pickedUri: Uri) =
        viewModelScope.launch(Dispatchers.IO) {
            val saved = copyPickedImageToInternal(context, pickedUri, "hourglass_bg")
            if (saved.isBlank()) return@launch
            SectionImageCodec.deleteManagedFileIfOwned(context, prefsState.value.focusBackgroundUri)
            prefs.setFocusBackgroundUri(saved)
        }

    fun restoreFocusBackgroundImage(context: Context) =
        viewModelScope.launch(Dispatchers.IO) {
            SectionImageCodec.deleteManagedFileIfOwned(context, prefsState.value.focusBackgroundUri)
            prefs.setFocusBackgroundUri("")
        }

    fun copyPickedImageToInternal(context: Context, uri: Uri, tag: String): String {
        val dir = File(context.filesDir, "custom_images").apply { mkdirs() }
        val file = File(dir, "${tag}_${System.currentTimeMillis()}.jpg")
        val copied = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: 0L
        }.getOrDefault(0L)
        return if (copied > 0L && file.exists()) {
            Uri.fromFile(file).toString()
        } else {
            file.delete()
            ""
        }
    }

    fun setSectionBackgroundImage(context: Context, sectionKey: String, pickedUri: Uri) =
        viewModelScope.launch(Dispatchers.IO) {
            val savedUri = copyPickedImageToInternal(context, pickedUri, "section_$sectionKey")
            if (savedUri.isBlank()) return@launch
            val current = activeProfile.value ?: return@launch
            val existingMap = SectionImageCodec.decode(current.sectionImagesJson)
            SectionImageCodec.deleteManagedFileIfOwned(context, existingMap[sectionKey])
            val updatedJson = SectionImageCodec.put(current.sectionImagesJson, sectionKey, savedUri)
            db.designDao().updateProfile(current.copy(sectionImagesJson = updatedJson))
        }

    fun restoreSectionBackgroundImage(context: Context, sectionKey: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val current = activeProfile.value ?: return@launch
            val existingMap = SectionImageCodec.decode(current.sectionImagesJson)
            SectionImageCodec.deleteManagedFileIfOwned(context, existingMap[sectionKey])
            val updatedJson = SectionImageCodec.remove(current.sectionImagesJson, sectionKey)
            db.designDao().updateProfile(current.copy(sectionImagesJson = updatedJson))
        }

    fun addCustomShortcut(title: String, targetRoute: String) {
        updateActiveProfile { prof ->
            val current = runCatching {
                json.decodeFromString<List<CustomShortcut>>(prof.customShortcutsJson)
            }.getOrDefault(emptyList())
            val updated = current + CustomShortcut(UUID.randomUUID().toString(), title, targetRoute)
            prof.copy(customShortcutsJson = json.encodeToString(updated))
        }
    }

    fun removeCustomShortcut(id: String) {
        updateActiveProfile { prof ->
            val current = runCatching {
                json.decodeFromString<List<CustomShortcut>>(prof.customShortcutsJson)
            }.getOrDefault(emptyList())
            prof.copy(customShortcutsJson = json.encodeToString(current.filterNot { it.id == id }))
        }
    }

    // --- Tasks ---
    fun saveTask(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        val id = db.taskDao().insertTask(task)
        val finalId = if (task.id == 0L) id else task.id
        val rem = task.snoozedUntil ?: task.reminderAt
        if (!task.isCompleted && rem != null && rem > System.currentTimeMillis()) {
            ReminderScheduler.scheduleTaskReminder(getApplication(), finalId, task.title, rem)
        } else {
            ReminderScheduler.cancelTaskReminder(getApplication(), finalId)
        }
        NexaWidgetUpdater.updateAll(getApplication())
    }

    fun toggleTaskComplete(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        val nowCompleted = !task.isCompleted
        db.taskDao().updateTask(
            task.copy(
                isCompleted = nowCompleted,
                completedAt = if (nowCompleted) System.currentTimeMillis() else null
            )
        )
        if (nowCompleted) {
            ReminderScheduler.cancelTaskReminder(getApplication(), task.id)
            prefs.addXp(15)
            // Generate next recurring task instance if applicable
            if (task.recurrenceType != "NONE") {
                val baseDue = task.dueAt ?: System.currentTimeMillis()
                val deltaDays = when (task.recurrenceType) {
                    "DAILY" -> 1L
                    "WEEKLY" -> 7L
                    "MONTHLY" -> 30L
                    "CUSTOM" -> task.recurrenceIntervalDays.coerceAtLeast(1).toLong()
                    else -> 0L
                }
                if (deltaDays > 0L) {
                    val nextDue = baseDue + deltaDays * 86_400_000L
                    val nextRem = task.reminderAt?.let { it + deltaDays * 86_400_000L }
                    val newId = db.taskDao().insertTask(
                        task.copy(
                            id = 0,
                            dueAt = nextDue,
                            reminderAt = nextRem,
                            snoozedUntil = null,
                            isCompleted = false,
                            completedAt = null,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    if (nextRem != null && nextRem > System.currentTimeMillis()) {
                        ReminderScheduler.scheduleTaskReminder(getApplication(), newId, task.title, nextRem)
                    }
                }
            }
            XpAndStatsEngine.evaluateAchievements(db, prefs)
        }
        NexaWidgetUpdater.updateAll(getApplication())
    }

    fun snoozeTask(task: TaskEntity, delayMillis: Long) = viewModelScope.launch(Dispatchers.IO) {
        val newTime = System.currentTimeMillis() + delayMillis
        db.taskDao().updateTask(task.copy(snoozedUntil = newTime, reminderAt = newTime))
        ReminderScheduler.scheduleTaskReminder(getApplication(), task.id, task.title, newTime)
        NexaWidgetUpdater.updateAll(getApplication())
    }

    fun deleteTask(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        ReminderScheduler.cancelTaskReminder(getApplication(), task.id)
        db.taskDao().deleteTask(task)
        NexaWidgetUpdater.updateAll(getApplication())
    }

    // --- Study (Zero AI) ---
    fun saveSubject(subject: StudySubjectEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().insertSubject(subject)
        XpAndStatsEngine.evaluateAchievements(db, prefs)
    }

    fun deleteSubject(subject: StudySubjectEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().deleteSubject(subject)
    }

    fun saveLesson(lesson: StudyLessonEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().insertLesson(lesson)
    }

    fun deleteLesson(lesson: StudyLessonEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().deleteLesson(lesson)
    }

    fun saveFlashcard(card: StudyFlashcardEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().insertFlashcard(card)
    }

    fun deleteFlashcard(card: StudyFlashcardEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().deleteFlashcard(card)
    }

    fun rateFlashcard(card: StudyFlashcardEntity, quality: Int) = viewModelScope.launch(Dispatchers.IO) {
        val updated = SpacedRepetitionEngine.scheduleNext(card, quality)
        db.studyDao().updateFlashcard(updated)
        db.studyDao().insertReviewLog(
            StudyReviewLogEntity(
                subjectId = card.subjectId,
                lessonId = card.lessonId,
                mode = "FLASHCARD",
                scorePercent = if (quality >= 3) 100 else 50,
                totalItems = 1,
                correctItems = if (quality >= 3) 1 else 0
            )
        )
        prefs.addXp(5)
        XpAndStatsEngine.evaluateAchievements(db, prefs)
    }

    fun recordMemoryTestResult(subjectId: Long, lessonId: Long?, scorePercent: Int, total: Int, correct: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            db.studyDao().insertReviewLog(
                StudyReviewLogEntity(
                    subjectId = subjectId,
                    lessonId = lessonId,
                    mode = "TEST_MEMORY",
                    scorePercent = scorePercent,
                    totalItems = total,
                    correctItems = correct
                )
            )
            prefs.addXp(10 + correct * 3)
            XpAndStatsEngine.evaluateAchievements(db, prefs)
        }

    fun saveScheduleSlot(slot: StudyScheduleSlotEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().insertScheduleSlot(slot)
    }

    fun deleteScheduleSlot(slot: StudyScheduleSlotEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().deleteScheduleSlot(slot)
    }

    fun saveGrade(grade: StudyGradeEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().insertGrade(grade)
    }

    fun deleteGrade(grade: StudyGradeEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().deleteGrade(grade)
    }

    fun saveStudyGoal(goal: StudyGoalEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().insertGoal(goal)
    }

    fun deleteStudyGoal(goal: StudyGoalEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.studyDao().deleteGoal(goal)
    }

    // --- Habits (No penalties) ---
    fun saveHabit(habit: HabitEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.habitDao().insertHabit(habit)
    }

    fun deleteHabit(habit: HabitEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.habitDao().deleteHabit(habit)
    }

    fun swapHabitOrder(a: HabitEntity, b: HabitEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.habitDao().updateHabit(a.copy(sortOrder = b.sortOrder))
        db.habitDao().updateHabit(b.copy(sortOrder = a.sortOrder))
    }

    fun logHabitProgress(habit: HabitEntity, newValue: Int, isRestDay: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            val today = todayIso
            val existing = db.habitDao().getLogForDate(habit.id, today)
            val completed = isRestDay || newValue >= habit.targetValue
            db.habitDao().insertHabitLog(
                HabitLogEntity(
                    id = existing?.id ?: 0L,
                    habitId = habit.id,
                    dateIso = today,
                    valueAchieved = newValue,
                    isCompleted = completed && !isRestDay,
                    isRestDay = isRestDay
                )
            )
            if (completed && !isRestDay && habit.lastCompletedDateIso != today) {
                val newStreak = habit.streakCount + 1
                db.habitDao().updateHabit(
                    habit.copy(
                        streakCount = newStreak,
                        bestStreak = maxOf(habit.bestStreak, newStreak),
                        lastCompletedDateIso = today
                    )
                )
                prefs.addXp(10)
                XpAndStatsEngine.evaluateAchievements(db, prefs)
            }
        }

    // --- Focus & Focus Guard ---
    fun toggleBlockedApp(pkg: String, label: String, enabled: Boolean, dailyLimit: Int = 0, inFocus: Boolean = true, inTasks: Boolean = false, inPrayer: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            db.focusDao().upsertBlockedApp(
                BlockedAppEntity(
                    packageName = pkg,
                    appLabel = label,
                    dailyLimitMinutes = dailyLimit,
                    blockInFocus = inFocus,
                    blockInTasks = inTasks,
                    blockInPrayer = inPrayer,
                    isEnabled = enabled
                )
            )
        }

    // --- Life ---
    fun savePrayerConfig(city: String, lat: Double, lon: Double, method: String) = viewModelScope.launch {
        prefs.setPrayerConfig(city, lat, lon, method)
    }

    fun incrementDhikr(item: DhikrItem) = viewModelScope.launch(Dispatchers.IO) {
        val key = "${todayIso}_${item.category}_${item.id}"
        val current = adhkarProgress.value.firstOrNull { it.keyId == key }?.currentCount ?: 0
        val next = (current + 1).coerceAtMost(item.targetCount)
        db.lifeDao().upsertAdhkarProgress(
            AdhkarProgressEntity(
                keyId = key,
                category = item.category,
                dhikrId = item.id,
                dateIso = todayIso,
                currentCount = next,
                targetCount = item.targetCount
            )
        )
        if (next == item.targetCount && current < item.targetCount) {
            prefs.addXp(5)
            XpAndStatsEngine.evaluateAchievements(db, prefs)
        }
    }

    fun toggleDuaFavorite(duaId: String, currentFav: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        db.lifeDao().upsertDuaFavorite(DuaFavoriteEntity(duaId = duaId, isFavorite = !currentFav))
    }

    fun addMoodEntry(emotions: Set<String>, note: String) = viewModelScope.launch(Dispatchers.IO) {
        if (emotions.isEmpty() && note.isBlank()) return@launch
        db.lifeDao().insertMoodEntry(
            MoodEntryEntity(
                emotionsCsv = emotions.joinToString(","),
                noteText = note.trim(),
                dateIso = todayIso
            )
        )
        prefs.addXp(8)
    }

    // --- Library & Audio ---
    fun updateLibraryItem(item: LibraryItemEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.libraryDao().updateLibraryItem(item)
        XpAndStatsEngine.evaluateAchievements(db, prefs)
    }

    fun importTextBook(title: String, author: String, category: String, text: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val pages = (text.length / 950).coerceAtLeast(1)
            db.libraryDao().insertLibraryItem(
                LibraryItemEntity(
                    title = title,
                    author = author.ifBlank { "Local Import" },
                    category = category,
                    shelf = "WANT_TO_READ",
                    contentText = text,
                    totalPages = pages,
                    sourceInfo = "Imported locally by user",
                    isDownloaded = true
                )
            )
        }

    fun saveQuote(book: LibraryItemEntity, page: Int, quoteText: String) = viewModelScope.launch(Dispatchers.IO) {
        if (quoteText.isBlank()) return@launch
        db.libraryDao().insertQuote(
            SavedQuoteEntity(
                bookId = book.id,
                bookTitle = book.title,
                author = book.author,
                pageNumber = page + 1,
                quoteText = quoteText.trim()
            )
        )
        prefs.addXp(5)
    }

    fun deleteQuote(q: SavedQuoteEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.libraryDao().deleteQuote(q)
    }

    fun togglePodcastFavorite(ep: PodcastEpisodeEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.libraryDao().updatePodcastEpisode(ep.copy(isFavorite = !ep.isFavorite))
    }

    fun downloadPodcastEpisode(ep: PodcastEpisodeEntity, onDone: (Boolean) -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching {
                val dir = File(getApplication<Application>().filesDir, "podcasts").apply { mkdirs() }
                val dest = File(dir, "ep_${ep.id}.mp3")
                val req = Request.Builder().url(ep.audioUrl).build()
                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) error("HTTP ${resp.code}")
                    resp.body?.byteStream()?.use { inp ->
                        dest.outputStream().use { out -> inp.copyTo(out) }
                    } ?: error("Empty body")
                }
                db.libraryDao().updatePodcastEpisode(
                    ep.copy(isDownloaded = true, localFileUri = Uri.fromFile(dest).toString())
                )
                true
            }.getOrDefault(false)
            launch(Dispatchers.Main) { onDone(ok) }
        }

    fun importAudioTrack(title: String, playlist: String, uriString: String) =
        viewModelScope.launch(Dispatchers.IO) {
            val id = db.audioDao().insertAudioTrack(
                AudioTrackEntity(
                    title = title,
                    artist = "Local MP3",
                    playlistName = playlist,
                    uriString = uriString
                )
            )
            val saved = db.audioDao().getAllAudioTracksSync().firstOrNull { it.id == id }
            if (saved != null) {
                launch(Dispatchers.Main) { container.localAudioController.playTrack(saved) }
            }
        }

    fun deleteAudioTrack(track: AudioTrackEntity) = viewModelScope.launch(Dispatchers.IO) {
        db.audioDao().deleteAudioTrack(track)
    }

    // --- Notifications, Security & Backups ---
    fun markNotificationRead(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        db.systemDao().markNotificationRead(id)
    }

    fun markAllNotificationsRead() = viewModelScope.launch(Dispatchers.IO) {
        db.systemDao().markAllNotificationsRead()
    }

    fun clearNotifications() = viewModelScope.launch(Dispatchers.IO) {
        db.systemDao().clearNotifications()
    }

    fun setAppPin(pin: String) = viewModelScope.launch {
        if (pin.isBlank()) {
            prefs.setPin("", "")
        } else {
            val salt = CryptoAndLockManager.generateSalt()
            val hash = CryptoAndLockManager.hashPin(pin, salt)
            prefs.setPin(hash, salt)
        }
    }

    fun toggleAutoBackup(enabled: Boolean) = viewModelScope.launch {
        prefs.setAutoBackup(enabled)
        AutoBackupWorker.schedule(getApplication(), enabled)
    }

    fun createLocalBackupNow(onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = runCatching { container.backupRepository.createLocalEncryptedBackup(false) }.isSuccess
        onDone(ok)
    }

    fun exportBackupToUri(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = container.backupRepository.exportBackupToUri(uri)
        onDone(ok)
    }

    fun restoreBackup(pathOrUri: String, onDone: (Result<Int>) -> Unit) = viewModelScope.launch {
        val res = container.backupRepository.restoreFromFileOrUri(pathOrUri)
        onDone(res)
    }
}
