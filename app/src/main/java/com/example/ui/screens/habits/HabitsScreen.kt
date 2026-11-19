package com.example.ui.screens.habits

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.HabitEntity
import com.example.data.local.HabitLogEntity
import com.example.domain.i18n.LocalNexaStrings
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaScreenTitleRow
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaTintedBanner
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitsScreen(
    habits: List<HabitEntity>,
    logs: List<HabitLogEntity>,
    onSaveHabit: (HabitEntity) -> Unit,
    onDeleteHabit: (HabitEntity) -> Unit,
    onSwapOrder: (HabitEntity, HabitEntity) -> Unit,
    onLogProgress: (HabitEntity, Int, Boolean) -> Unit,
    onCopyPhoto: (Uri) -> String
) {
    val strings = LocalNexaStrings.current
    val todayIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var selectedRoutine by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }

    val routineOrder = remember { mapOf("Morning" to 0, "Study" to 1, "Evening" to 2) }
    val routines = remember(habits) {
        val sortedRoutines = habits.map { it.routineName }
            .distinct()
            .sortedWith(compareBy<String> { routineOrder[it] ?: 99 }.thenBy { it })
            .ifEmpty { listOf("Morning", "Study", "Evening") }
        listOf("ALL") + sortedRoutines
    }
    val filtered = remember(habits, selectedRoutine) {
        habits.filter { selectedRoutine == "ALL" || it.routineName == selectedRoutine }
            .sortedWith(compareBy<HabitEntity> { routineOrder[it.routineName] ?: 99 }.thenBy { it.sortOrder })
    }

    val weeklyCount = logs.count { it.isCompleted }
    val monthlyCompletionRate = if (habits.isEmpty()) 0 else {
        ((logs.count { it.isCompleted }.toFloat() / (habits.size * 7f)) * 100f).toInt().coerceIn(0, 100)
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.addHabit)
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
                title = strings.habits,
                icon = Icons.Rounded.AutoAwesome,
                accent = NexaSectionAccents.habits
            )

            NexaTintedBanner(
                text = "${strings.noPenaltyNote}\n${strings.weeklyMonthlyPatterns}: $weeklyCount • $monthlyCompletionRate%",
                icon = Icons.Rounded.Favorite,
                accent = NexaSectionAccents.habits
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(routines) { r ->
                    FilterChip(
                        selected = selectedRoutine == r,
                        onClick = { selectedRoutine = r },
                        label = { Text(if (r == "ALL") strings.all else strings.habitRoutineDisplay(r)) }
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                itemsIndexed(filtered, key = { _, h -> h.id }) { idx, habit ->
                    val todayLog = logs.firstOrNull { it.habitId == habit.id && it.dateIso == todayIso }
                    val achieved = todayLog?.valueAchieved ?: 0
                    val isDone = todayLog?.isCompleted == true
                    val isRest = todayLog?.isRestDay == true

                    NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (habit.imageUri.isNotBlank()) {
                                    AsyncImage(
                                        model = Uri.parse(habit.imageUri),
                                        contentDescription = habit.title,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(10.dp))
                                } else {
                                    NexaFilledIconBadge(
                                        icon = Icons.Rounded.AutoAwesome,
                                        tint = NexaSectionAccents.habits,
                                        size = 44.dp,
                                        iconSize = 22.dp,
                                        cornerRadius = 14.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(habit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${strings.habitRoutineDisplay(habit.routineName)} • ${strings.frequencyDisplay(habit.frequencyType)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "${strings.streakDays}: ${habit.streakCount} • ${strings.bestStreak}: ${habit.bestStreak}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (idx > 0) {
                                    IconButton(onClick = { onSwapOrder(habit, filtered[idx - 1]) }) {
                                        Icon(Icons.Rounded.ArrowUpward, contentDescription = strings.edit)
                                    }
                                }
                                IconButton(onClick = { onDeleteHabit(habit) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                                }
                            }

                            if (habit.notes.isNotBlank()) {
                                Text(habit.notes, style = MaterialTheme.typography.bodySmall)
                            }

                            if (habit.measurementType != "COMPLETION") {
                                LinearProgressIndicator(
                                    progress = { (achieved.toFloat() / habit.targetValue.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = NexaSectionAccents.habits,
                                    trackColor = NexaSectionAccents.habits.copy(alpha = 0.16f)
                                )
                                Text("$achieved / ${habit.targetValue} ${habit.unitLabel}", style = MaterialTheme.typography.labelMedium)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (habit.measurementType == "COMPLETION") {
                                    FilledTonalButton(
                                        onClick = { onLogProgress(habit, if (isDone) 0 else 1, false) }
                                    ) {
                                        Icon(if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null)
                                        Spacer(Modifier.width(4.dp))
                                        Text(strings.measureCompletion)
                                    }
                                } else {
                                    val step = if (habit.measurementType == "DURATION") 5 else 1
                                    FilledTonalButton(onClick = { onLogProgress(habit, achieved + step, false) }) {
                                        Text("+$step ${habit.unitLabel}")
                                    }
                                }
                                OutlinedButton(onClick = { onLogProgress(habit, achieved, !isRest) }) {
                                    Text(if (isRest) "✓ ${strings.restDay}" else strings.restDay)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        HabitEditorDialog(
            nextOrder = habits.size,
            onCopyPhoto = onCopyPhoto,
            onDismiss = { showAddDialog = false },
            onSave = { h ->
                onSaveHabit(h)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HabitEditorDialog(
    nextOrder: Int,
    onCopyPhoto: (Uri) -> String,
    onDismiss: () -> Unit,
    onSave: (HabitEntity) -> Unit
) {
    val strings = LocalNexaStrings.current
    var title by remember { mutableStateOf("") }
    var routine by remember { mutableStateOf("Morning") }
    var freq by remember { mutableStateOf("DAILY") }
    var measurement by remember { mutableStateOf("COMPLETION") }
    var target by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) imageUri = onCopyPhoto(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addHabit) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 420.dp)) {
                item {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(strings.title) }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Text(strings.routines, style = MaterialTheme.typography.labelLarge)
                    val presetRoutines = listOf("Morning", "Study", "Evening")
                    var customRoutineMode by remember { mutableStateOf(false) }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetRoutines) { preset ->
                            FilterChip(
                                selected = !customRoutineMode && routine == preset,
                                onClick = {
                                    customRoutineMode = false
                                    routine = preset
                                },
                                label = { Text(strings.habitRoutineDisplay(preset)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = customRoutineMode || routine !in presetRoutines,
                                onClick = {
                                    customRoutineMode = true
                                    if (routine in presetRoutines) routine = ""
                                },
                                label = { Text(strings.customRoutineLabel) }
                            )
                        }
                    }
                    if (customRoutineMode || routine !in presetRoutines) {
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = routine,
                            onValueChange = { routine = it },
                            label = { Text(strings.customRoutineLabel) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("DAILY" to strings.recDaily, "WEEKLY" to strings.recWeekly, "MONTHLY" to strings.recMonthly, "CUSTOM" to strings.recCustom)) { (k, l) ->
                            FilterChip(selected = freq == k, onClick = { freq = k }, label = { Text(l) })
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("COMPLETION" to strings.measureCompletion, "COUNT" to strings.measureCount, "DURATION" to strings.measureDuration).forEach { (m, l) ->
                            FilterChip(selected = measurement == m, onClick = {
                                measurement = m
                                if (m == "DURATION") { target = "20"; unit = strings.minutesUnit }
                                else if (m == "COUNT") { target = "5"; unit = "×" }
                                else { target = "1"; unit = "" }
                            }, label = { Text(l) })
                        }
                    }
                }
                if (measurement != "COMPLETION") {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = target, onValueChange = { target = it.filter { c -> c.isDigit() } }, label = { Text(strings.targetLabel) }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text(strings.unitLabel) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                item {
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(strings.notes) }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Rounded.Image, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(strings.attachPhoto)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    onSave(
                        HabitEntity(
                            title = title.trim(),
                            routineName = routine.trim().ifBlank { "Morning" },
                            sortOrder = nextOrder,
                            frequencyType = freq,
                            measurementType = measurement,
                            targetValue = target.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                            unitLabel = unit.trim(),
                            notes = notes.trim(),
                            imageUri = imageUri
                        )
                    )
                }
            }) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
