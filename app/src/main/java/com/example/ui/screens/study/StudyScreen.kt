package com.example.ui.screens.study

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaEmptyState
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaScreenTitleRow
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaSegmentedTabs
import com.example.ui.components.NexaTintedBanner
import java.util.Locale

@Composable
fun StudyScreen(
    subjects: List<StudySubjectEntity>,
    lessons: List<StudyLessonEntity>,
    flashcards: List<StudyFlashcardEntity>,
    reviewLogs: List<StudyReviewLogEntity>,
    scheduleSlots: List<StudyScheduleSlotEntity>,
    grades: List<StudyGradeEntity>,
    goals: List<StudyGoalEntity>,
    onSaveSubject: (StudySubjectEntity) -> Unit,
    onDeleteSubject: (StudySubjectEntity) -> Unit,
    onSaveLesson: (StudyLessonEntity) -> Unit,
    onDeleteLesson: (StudyLessonEntity) -> Unit,
    onSaveFlashcard: (StudyFlashcardEntity) -> Unit,
    onDeleteFlashcard: (StudyFlashcardEntity) -> Unit,
    onRateFlashcard: (StudyFlashcardEntity, Int) -> Unit,
    onRecordMemoryTest: (Long, Long?, Int, Int, Int) -> Unit,
    onSaveScheduleSlot: (StudyScheduleSlotEntity) -> Unit,
    onDeleteScheduleSlot: (StudyScheduleSlotEntity) -> Unit,
    onSaveGrade: (StudyGradeEntity) -> Unit,
    onDeleteGrade: (StudyGradeEntity) -> Unit,
    onSaveGoal: (StudyGoalEntity) -> Unit,
    onDeleteGoal: (StudyGoalEntity) -> Unit
) {
    val strings = LocalNexaStrings.current
    var selectedSubject by remember { mutableStateOf<StudySubjectEntity?>(null) }
    var selectedLesson by remember { mutableStateOf<StudyLessonEntity?>(null) }
    var activeTab by remember { mutableIntStateOf(0) }
    var showAddSubject by remember { mutableStateOf(false) }
    var showAddScheduleSlot by remember { mutableStateOf(false) }
    var dismissedSuggestion by remember { mutableStateOf(false) }

    // Keep selectedLesson/Subject synced with Room flows
    val syncedSubject = selectedSubject?.let { s -> subjects.firstOrNull { it.id == s.id } ?: s }
    val syncedLesson = selectedLesson?.let { l -> lessons.firstOrNull { it.id == l.id } ?: l }

    if (syncedLesson != null && syncedSubject != null) {
        LessonDetailScreen(
            subject = syncedSubject,
            lesson = syncedLesson,
            flashcards = flashcards.filter { it.lessonId == syncedLesson.id || (it.lessonId == null && it.subjectId == syncedSubject.id) },
            reviewLogs = reviewLogs.filter { it.subjectId == syncedSubject.id },
            onBack = { selectedLesson = null },
            onUpdateLesson = onSaveLesson,
            onSaveFlashcard = onSaveFlashcard,
            onDeleteFlashcard = onDeleteFlashcard,
            onRateFlashcard = onRateFlashcard,
            onRecordMemoryTest = onRecordMemoryTest
        )
        return
    }

    if (syncedSubject != null) {
        SubjectDetailScreen(
            subject = syncedSubject,
            lessons = lessons.filter { it.subjectId == syncedSubject.id },
            flashcards = flashcards.filter { it.subjectId == syncedSubject.id },
            grades = grades.filter { it.subjectId == syncedSubject.id },
            onBack = { selectedSubject = null },
            onOpenLesson = { selectedLesson = it },
            onSaveLesson = onSaveLesson,
            onDeleteLesson = onDeleteLesson,
            onSaveFlashcard = onSaveFlashcard,
            onRateFlashcard = onRateFlashcard
        )
        return
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            when (activeTab) {
                0 -> FloatingActionButton(
                    onClick = { showAddSubject = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_subject_fab")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = strings.addSubject)
                }
                1 -> FloatingActionButton(
                    onClick = { showAddScheduleSlot = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_schedule_slot_fab")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = strings.add)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NexaScreenTitleRow(
                title = strings.study,
                subtitle = strings.offlineLocalBadge,
                icon = Icons.Rounded.MenuBook,
                accent = NexaSectionAccents.study
            )

            val tabs = listOf(strings.subjects, strings.schedule, strings.gradesAndGpa, strings.goalsAndStats)
            NexaSegmentedTabs(
                tabs = tabs,
                selectedIndex = activeTab.coerceIn(0, tabs.lastIndex),
                onSelect = { activeTab = it }
            )

            when (activeTab) {
                0 -> {
                    val dueNowCount = flashcards.count { it.nextReviewAt <= System.currentTimeMillis() }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 84.dp)
                    ) {
                        if (!dismissedSuggestion && dueNowCount > 0) {
                            item {
                                NexaTintedBanner(
                                    text = "${strings.spacedReview}: $dueNowCount",
                                    icon = Icons.Rounded.AutoAwesome,
                                    accent = NexaSectionAccents.study,
                                    action = {
                                        IconButton(onClick = { dismissedSuggestion = true }) {
                                            Icon(Icons.Rounded.Close, contentDescription = strings.close)
                                        }
                                    }
                                )
                            }
                        }

                        if (subjects.isEmpty()) {
                            item {
                                NexaEmptyState(
                                    message = "${strings.addSubject}\n${strings.noAiStudyNote}",
                                    icon = Icons.Rounded.School,
                                    accent = NexaSectionAccents.study
                                )
                            }
                        }

                        items(subjects, key = { it.id }) { subj ->
                            val subjLessons = lessons.filter { it.subjectId == subj.id }
                            val subjCards = flashcards.filter { it.subjectId == subj.id }
                            NexaCard(
                                modifier = Modifier.defaultMinSize(minHeight = 78.dp),
                                cornerRadius = 20.dp,
                                elevation = 2.dp,
                                onClick = { selectedSubject = subj }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NexaFilledIconBadge(
                                        icon = Icons.Rounded.MenuBook,
                                        tint = NexaSectionAccents.study,
                                        size = 44.dp,
                                        iconSize = 22.dp,
                                        cornerRadius = 14.dp
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(subj.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${strings.lessons}: ${subjLessons.size} • ${strings.flashcards}: ${subjCards.size}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeleteSubject(subj) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> StudyScheduleTab(
                    subjects = subjects,
                    scheduleSlots = scheduleSlots,
                    showAddDialog = showAddScheduleSlot,
                    onRequestAddDialog = { showAddScheduleSlot = true },
                    onDismissAddDialog = { showAddScheduleSlot = false },
                    onSaveSlot = onSaveScheduleSlot,
                    onDeleteSlot = onDeleteScheduleSlot
                )

                2 -> StudyGradesTab(
                    subjects = subjects,
                    grades = grades,
                    onSaveGrade = onSaveGrade,
                    onDeleteGrade = onDeleteGrade
                )

                else -> StudyGoalsAndStatsTab(
                    goals = goals,
                    reviewLogs = reviewLogs,
                    onSaveGoal = onSaveGoal,
                    onDeleteGoal = onDeleteGoal
                )
            }
        }
    }

    if (showAddSubject) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddSubject = false },
            title = { Text(strings.addSubject) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(strings.title) })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onSaveSubject(
                            StudySubjectEntity(
                                name = name.trim(),
                            )
                        )
                    }
                    showAddSubject = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showAddSubject = false }) { Text(strings.cancel) } }
        )
    }
}

@Composable
private fun StudyScheduleTab(
    subjects: List<StudySubjectEntity>,
    scheduleSlots: List<StudyScheduleSlotEntity>,
    showAddDialog: Boolean,
    onRequestAddDialog: () -> Unit,
    onDismissAddDialog: () -> Unit,
    onSaveSlot: (StudyScheduleSlotEntity) -> Unit,
    onDeleteSlot: (StudyScheduleSlotEntity) -> Unit
) {
    val strings = LocalNexaStrings.current
    var selectedDayFilter by remember { mutableIntStateOf(0) } // 0 = All, 1=Mon .. 7=Sun
    val dayOrder = remember { listOf(6, 7, 1, 2, 3, 4, 5) } // Sat .. Fri

    fun dayLabel(day: Int): String = when (strings.lang) {
        com.example.domain.i18n.NexaLang.AR -> when (day) {
            1 -> "الإثنين"
            2 -> "الثلاثاء"
            3 -> "الأربعاء"
            4 -> "الخميس"
            5 -> "الجمعة"
            6 -> "السبت"
            else -> "الأحد"
        }
        com.example.domain.i18n.NexaLang.FR -> when (day) {
            1 -> "Lun"
            2 -> "Mar"
            3 -> "Mer"
            4 -> "Jeu"
            5 -> "Ven"
            6 -> "Sam"
            else -> "Dim"
        }
        else -> when (day) {
            1 -> "Mon"
            2 -> "Tue"
            3 -> "Wed"
            4 -> "Thu"
            5 -> "Fri"
            6 -> "Sat"
            else -> "Sun"
        }
    }

    fun formatMinute(min: Int): String = String.format(Locale.US, "%02d:%02d", (min / 60).coerceIn(0, 23), (min % 60).coerceIn(0, 59))

    val filtered = remember(scheduleSlots, selectedDayFilter) {
        scheduleSlots.filter { selectedDayFilter == 0 || it.dayOfWeek == selectedDayFilter }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("study_schedule_tab"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    NexaFilledIconBadge(
                        icon = Icons.Rounded.CalendarMonth,
                        tint = NexaSectionAccents.study,
                        size = 44.dp,
                        iconSize = 22.dp,
                        cornerRadius = 14.dp
                    )
                    Column {
                        Text(strings.weeklySchedule, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${scheduleSlots.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FilledTonalButton(onClick = onRequestAddDialog) {
                    Text(strings.add)
                }
            }
        }

        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = selectedDayFilter == 0,
                    onClick = { selectedDayFilter = 0 },
                    label = { Text(strings.all) }
                )
            }
            items(dayOrder) { d ->
                FilterChip(
                    selected = selectedDayFilter == d,
                    onClick = { selectedDayFilter = d },
                    label = { Text(dayLabel(d)) }
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 84.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    NexaEmptyState(
                        message = strings.weeklySchedule,
                        icon = Icons.Rounded.Schedule,
                        accent = NexaSectionAccents.study
                    )
                }
            }
            items(filtered, key = { it.id }) { slot ->
                val subjName = subjects.firstOrNull { it.id == slot.subjectId }?.name
                NexaCard(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 76.dp)
                        .testTag("schedule_slot_${slot.id}"),
                    cornerRadius = 18.dp,
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NexaFilledIconBadge(
                            icon = if (slot.isException) Icons.Rounded.EventBusy else Icons.Rounded.Schedule,
                            tint = if (slot.isException) NexaSectionAccents.habits else NexaSectionAccents.study,
                            size = 42.dp,
                            iconSize = 20.dp,
                            cornerRadius = 13.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = slot.title.ifBlank { subjName ?: strings.schedule },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${dayLabel(slot.dayOfWeek)} • ${formatMinute(slot.startMinute)} - ${formatMinute(slot.endMinute)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (slot.isException || !subjName.isNullOrBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (!subjName.isNullOrBlank()) {
                                        Text(
                                            text = subjName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NexaSectionAccents.study,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (slot.isException) {
                                        Text(
                                            text = strings.exceptionSchedule,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NexaSectionAccents.habits,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                        IconButton(onClick = { onDeleteSlot(slot) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var day by remember { mutableIntStateOf(if (selectedDayFilter in 1..7) selectedDayFilter else 6) }
        var startHour by remember { mutableStateOf("09") }
        var endHour by remember { mutableStateOf("10") }
        var selectedSubjId by remember { mutableStateOf(subjects.firstOrNull()?.id) }
        var isException by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismissAddDialog,
            title = { Text(strings.weeklySchedule) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(strings.title) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(dayOrder) { d ->
                            FilterChip(selected = day == d, onClick = { day = d }, label = { Text(dayLabel(d)) })
                        }
                    }
                    if (subjects.isNotEmpty()) {
                        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(subjects, key = { it.id }) { s ->
                                FilterChip(
                                    selected = selectedSubjId == s.id,
                                    onClick = { selectedSubjId = if (selectedSubjId == s.id) null else s.id },
                                    label = { Text(s.name) }
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startHour,
                            onValueChange = { startHour = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Start (h)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endHour,
                            onValueChange = { endHour = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("End (h)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.exceptionSchedule, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Switch(checked = isException, onCheckedChange = { isException = it })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val sMin = (startHour.toIntOrNull()?.coerceIn(0, 23) ?: 9) * 60
                    val eMin = (endHour.toIntOrNull()?.coerceIn(0, 23) ?: 10) * 60
                    val resolvedTitle = title.trim().ifBlank {
                        subjects.firstOrNull { it.id == selectedSubjId }?.name ?: strings.schedule
                    }
                    onSaveSlot(
                        StudyScheduleSlotEntity(
                            dayOfWeek = day,
                            startMinute = sMin,
                            endMinute = eMin.coerceAtLeast(sMin + 30),
                            subjectId = selectedSubjId,
                            title = resolvedTitle,
                            isException = isException
                        )
                    )
                    onDismissAddDialog()
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = onDismissAddDialog) { Text(strings.cancel) } }
        )
    }
}

@Composable
private fun StudyGradesTab(
    subjects: List<StudySubjectEntity>,
    grades: List<StudyGradeEntity>,
    onSaveGrade: (StudyGradeEntity) -> Unit,
    onDeleteGrade: (StudyGradeEntity) -> Unit
) {
    val strings = LocalNexaStrings.current
    var showAddGrade by remember { mutableStateOf(false) }

    // Simple average calculator: each subject counts equally; no subject coefficients.
    val weightedAverage = remember(subjects, grades) {
        val averages = subjects.mapNotNull { subj ->
            val sg = grades.filter { it.subjectId == subj.id }
            if (sg.isEmpty()) null else {
                sg.sumOf { (it.score / it.maxScore.coerceAtLeast(1.0)) * 20.0 * it.weight } /
                    sg.sumOf { it.weight }.coerceAtLeast(1.0)
            }
        }
        if (averages.isEmpty()) 0.0 else averages.average()
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NexaFilledIconBadge(
                        icon = Icons.Rounded.Grade,
                        tint = NexaSectionAccents.study,
                        size = 44.dp,
                        iconSize = 22.dp,
                        cornerRadius = 14.dp
                    )
                    Column {
                        Text(strings.weightedAverage, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format(Locale.US, "%.2f / 20", weightedAverage), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Button(onClick = { showAddGrade = true }, enabled = subjects.isNotEmpty()) {
                    Text(strings.add)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(grades, key = { it.id }) { g ->
                val subjName = subjects.firstOrNull { it.id == g.subjectId }?.name ?: ""
                NexaCard(
                    modifier = Modifier.defaultMinSize(minHeight = 74.dp),
                    cornerRadius = 18.dp,
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("$subjName • ${g.title}", fontWeight = FontWeight.SemiBold)
                            Text("${g.score} / ${g.maxScore}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDeleteGrade(g) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                        }
                    }
                }
            }
        }
    }

    if (showAddGrade && subjects.isNotEmpty()) {
        var selectedSubjId by remember { mutableLongStateOf(subjects.first().id) }
        var title by remember { mutableStateOf("") }
        var score by remember { mutableStateOf("16") }
        var maxScore by remember { mutableStateOf("20") }
        AlertDialog(
            onDismissRequest = { showAddGrade = false },
            title = { Text(strings.gradesAndGpa) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    subjects.forEach { s ->
                        FilterChip(selected = selectedSubjId == s.id, onClick = { selectedSubjId = s.id }, label = { Text(s.name) })
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(strings.title) })
                    OutlinedTextField(value = score, onValueChange = { score = it }, label = { Text(strings.scoreLabel) })
                    OutlinedTextField(value = maxScore, onValueChange = { maxScore = it }, label = { Text(strings.maxScoreLabel) })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSaveGrade(
                        StudyGradeEntity(
                            subjectId = selectedSubjId,
                            title = title.ifBlank { strings.gradesAndGpa },
                            score = score.toDoubleOrNull() ?: 0.0,
                            maxScore = maxScore.toDoubleOrNull() ?: 20.0
                        )
                    )
                    showAddGrade = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showAddGrade = false }) { Text(strings.cancel) } }
        )
    }
}

@Composable
private fun StudyGoalsAndStatsTab(
    goals: List<StudyGoalEntity>,
    reviewLogs: List<StudyReviewLogEntity>,
    onSaveGoal: (StudyGoalEntity) -> Unit,
    onDeleteGoal: (StudyGoalEntity) -> Unit
) {
    val strings = LocalNexaStrings.current
    var showAddGoal by remember { mutableStateOf(false) }

    val goalPalette = remember {
        listOf(
            Color(0xFF0F766E), // Vibrant Deep Teal
            Color(0xFF2563EB), // Royal Cobalt
            Color(0xFFD97706), // Saturated Amber Gold
            Color(0xFF9333EA), // Vivid Plum
            Color(0xFFDC2626), // Warm Crimson
            Color(0xFF0891B2)  // Rich Cyan
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(strings.goalsAndStats, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FilledTonalButton(onClick = { showAddGoal = true }) { Text(strings.add) }
            }
        }
        itemsIndexed(goals, key = { _, g -> g.id }) { idx, goal ->
            val accent = goalPalette[idx % goalPalette.size]
            val progress = (goal.completedMinutes.toFloat() / goal.targetMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
            val pct = (progress * 100f).toInt()
            NexaCard(
                modifier = Modifier.defaultMinSize(minHeight = 96.dp),
                cornerRadius = 20.dp,
                elevation = 2.dp,
                borderColor = accent.copy(alpha = 0.45f)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(accent)
                            )
                            Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accent
                        ) {
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        IconButton(onClick = { onDeleteGoal(goal) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                        }
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accent,
                        trackColor = accent.copy(alpha = 0.18f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${goal.completedMinutes} / ${goal.targetMinutes} ${strings.minutesUnit}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = accent
                        )
                        FilledTonalButton(
                            onClick = {
                                onSaveGoal(goal.copy(completedMinutes = goal.completedMinutes + 30, isCompleted = goal.completedMinutes + 30 >= goal.targetMinutes))
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = accent.copy(alpha = 0.16f),
                                contentColor = accent
                            )
                        ) { Text("+30 ${strings.minutesUnit}", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
        item {
            Text(strings.reviewHistory, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(reviewLogs.take(20), key = { it.id }) { log ->
            NexaCard(cornerRadius = 18.dp, elevation = 1.dp) {
                ListItem(
                    headlineContent = { Text("${strings.reviewModeDisplay(log.mode)} • ${log.scorePercent}%") },
                    supportingContent = { Text("${log.correctItems}/${log.totalItems}") },
                    leadingContent = {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.History,
                            tint = NexaSectionAccents.study,
                            size = 38.dp,
                            iconSize = 19.dp,
                            cornerRadius = 11.dp
                        )
                    }
                )
            }
        }
    }

    if (showAddGoal) {
        var title by remember { mutableStateOf("") }
        var mins by remember { mutableStateOf("180") }
        AlertDialog(
            onDismissRequest = { showAddGoal = false },
            title = { Text(strings.goalsAndStats) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(strings.title) })
                    OutlinedTextField(
                        value = mins,
                        onValueChange = { mins = it.filter { c -> c.isDigit() } },
                        label = { Text("${strings.targetLabel} (${strings.minutesUnit})") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onSaveGoal(StudyGoalEntity(title = title.trim(), targetMinutes = mins.toIntOrNull() ?: 120))
                    }
                    showAddGoal = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showAddGoal = false }) { Text(strings.cancel) } }
        )
    }
}
