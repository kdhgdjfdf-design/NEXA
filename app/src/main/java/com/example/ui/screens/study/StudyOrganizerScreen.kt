package com.example.ui.screens.study

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.ui.screens.tasks.TasksScreen

/**
 * Unified "تنظيم الدراسة" (Study Organizer) experience for NOX STUDY.
 * Organizes Subjects/Lessons/Schedule/Grades and Tasks cleanly under one hub without duplicating tools.
 */
@Composable
fun StudyOrganizerScreen(
    tasks: List<TaskEntity>,
    subjects: List<StudySubjectEntity>,
    lessons: List<StudyLessonEntity>,
    flashcards: List<StudyFlashcardEntity>,
    reviewLogs: List<StudyReviewLogEntity>,
    scheduleSlots: List<StudyScheduleSlotEntity>,
    grades: List<StudyGradeEntity>,
    goals: List<StudyGoalEntity>,
    onSaveTask: (TaskEntity) -> Unit,
    onToggleTaskComplete: (TaskEntity) -> Unit,
    onSnoozeTask: (TaskEntity, Long) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
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
    onDeleteGoal: (StudyGoalEntity) -> Unit,
    initialMode: Int = 0
) {
    val strings = LocalNexaStrings.current
    var mode by rememberSaveable(initialMode) { mutableIntStateOf(initialMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("study_organizer_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Triple(0, strings.study, Icons.Rounded.MenuBook) to "study_org_tab_subjects",
                    Triple(1, strings.tasks, Icons.Rounded.TaskAlt) to "study_org_tab_tasks"
                ).forEach { (item, tag) ->
                    val (idx, label, icon) = item
                    val selected = mode == idx
                    Surface(
                        onClick = { mode = idx },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag(tag),
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (mode == 0) {
                StudyScreen(
                    subjects = subjects,
                    lessons = lessons,
                    flashcards = flashcards,
                    reviewLogs = reviewLogs,
                    scheduleSlots = scheduleSlots,
                    grades = grades,
                    goals = goals,
                    onSaveSubject = onSaveSubject,
                    onDeleteSubject = onDeleteSubject,
                    onSaveLesson = onSaveLesson,
                    onDeleteLesson = onDeleteLesson,
                    onSaveFlashcard = onSaveFlashcard,
                    onDeleteFlashcard = onDeleteFlashcard,
                    onRateFlashcard = onRateFlashcard,
                    onRecordMemoryTest = onRecordMemoryTest,
                    onSaveScheduleSlot = onSaveScheduleSlot,
                    onDeleteScheduleSlot = onDeleteScheduleSlot,
                    onSaveGrade = onSaveGrade,
                    onDeleteGrade = onDeleteGrade,
                    onSaveGoal = onSaveGoal,
                    onDeleteGoal = onDeleteGoal
                )
            } else {
                TasksScreen(
                    tasks = tasks,
                    onSaveTask = onSaveTask,
                    onToggleComplete = onToggleTaskComplete,
                    onSnoozeTask = onSnoozeTask,
                    onDeleteTask = onDeleteTask
                )
            }
        }
    }
}
