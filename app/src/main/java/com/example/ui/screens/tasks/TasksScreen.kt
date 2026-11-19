package com.example.ui.screens.tasks

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.data.local.TaskEntity
import com.example.domain.i18n.LocalNexaStrings
import com.example.domain.i18n.NexaLang
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaEmptyState
import com.example.ui.components.NexaScreenTitleRow
import com.example.ui.components.NexaSectionAccents
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    onSaveTask: (TaskEntity) -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onSnoozeTask: (TaskEntity, Long) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    val strings = LocalNexaStrings.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    val categories = remember(tasks) {
        listOf("ALL") + tasks.map { it.category }.distinct().filter { it.isNotBlank() }
    }

    val filtered = remember(tasks, searchQuery, selectedCategory) {
        tasks.filter { task ->
            val matchesSearch = searchQuery.isBlank() ||
                task.title.contains(searchQuery, ignoreCase = true) ||
                task.description.contains(searchQuery, ignoreCase = true) ||
                task.notes.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategory == "ALL" || task.category == selectedCategory
            matchesSearch && matchesCat
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = TaskEntity(title = "", dueAt = System.currentTimeMillis() + 3600_000L)
                    showEditor = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.newTask)
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
                title = strings.tasks,
                icon = Icons.Rounded.TaskAlt,
                accent = NexaSectionAccents.tasks
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.search) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (categories.size > 1) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(if (cat == "ALL") strings.all else cat) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                NexaEmptyState(
                    message = strings.emptyTasksHint,
                    icon = Icons.Rounded.Checklist,
                    accent = NexaSectionAccents.tasks
                )
            } else {
                NexaCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 20.dp,
                    elevation = 2.dp
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp).let {
                            PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 80.dp)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("tasks_flat_list")
                    ) {
                        items(filtered, key = { it.id }) { task ->
                            TaskItemRow(
                                task = task,
                                onToggle = { onToggleComplete(task) },
                                onEdit = {
                                    editingTask = task
                                    showEditor = true
                                },
                                onSnooze = { delay -> onSnoozeTask(task, delay) },
                                onDelete = { onDeleteTask(task) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                        }
                    }
                }
            }
        }
    }

    if (showEditor && editingTask != null) {
        TaskEditorDialog(
            initial = editingTask!!,
            onDismiss = { showEditor = false },
            onSave = { updated ->
                onSaveTask(updated)
                showEditor = false
            }
        )
    }
}

@Composable
private fun TaskItemRow(
    task: TaskEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onSnooze: (Long) -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalNexaStrings.current
    var showSnoozeMenu by remember { mutableStateOf(false) }

    val priorityLabel = when (task.priority) {
        3 -> strings.urgent
        2 -> strings.high
        1 -> strings.medium
        else -> strings.low
    }
    val dateFmt = remember { SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(vertical = 10.dp)
            .alpha(if (task.isCompleted) 0.65f else 1f)
            .testTag("task_item_card_${task.id}"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotBlank()) {
                    Text(task.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }
            if (task.isCompleted) {
                IconButton(onClick = onToggle) {
                    Icon(Icons.Rounded.Replay, contentDescription = strings.reopenTask, tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                Box {
                    IconButton(onClick = { showSnoozeMenu = true }) {
                        Icon(Icons.Rounded.Snooze, contentDescription = strings.snooze15m)
                    }
                    DropdownMenu(expanded = showSnoozeMenu, onDismissRequest = { showSnoozeMenu = false }) {
                        DropdownMenuItem(text = { Text(strings.snooze15m) }, onClick = { showSnoozeMenu = false; onSnooze(15 * 60_000L) })
                        DropdownMenuItem(text = { Text(strings.snooze1h) }, onClick = { showSnoozeMenu = false; onSnooze(3600_000L) })
                        DropdownMenuItem(text = { Text(strings.snoozeTomorrow) }, onClick = { showSnoozeMenu = false; onSnooze(86_400_000L) })
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
            }
        }

        Row(
            modifier = Modifier.padding(start = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = priorityLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            if (task.category.isNotBlank()) {
                Text(
                    text = "• ${task.category}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (task.dueAt != null) {
                Text(
                    text = "• ${dateFmt.format(Date(task.dueAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (task.focusGuardOnDue) {
                Icon(Icons.Rounded.Shield, contentDescription = strings.focusGuard, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.tertiary)
            }
            if (task.attachmentName.isNotBlank()) {
                Icon(Icons.Rounded.AttachFile, contentDescription = task.attachmentName, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun TaskEditorDialog(
    initial: TaskEntity,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    val strings = LocalNexaStrings.current
    val context = LocalContext.current
    var title by remember { mutableStateOf(initial.title) }
    var desc by remember { mutableStateOf(initial.description) }
    var notes by remember { mutableStateOf(initial.notes) }
    var category by remember { mutableStateOf(initial.category) }
    var priority by remember { mutableIntStateOf(initial.priority) }
    var recurrence by remember { mutableStateOf(initial.recurrenceType) }
    var customDays by remember { mutableStateOf(initial.recurrenceIntervalDays.toString()) }
    var dueOffsetHours by remember { mutableIntStateOf(2) }
    var enableReminder by remember { mutableStateOf(initial.reminderAt != null) }
    var focusGuardOnDue by remember { mutableStateOf(initial.focusGuardOnDue) }
    var attachmentUri by remember { mutableStateOf(initial.attachmentUri) }
    var attachmentName by remember { mutableStateOf(initial.attachmentName) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            attachmentUri = uri.toString()
            attachmentName = uri.lastPathSegment?.substringAfterLast('/') ?: strings.attachmentDefault
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.newTask) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 460.dp)) {
                item {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(strings.title) }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text(strings.description) }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(strings.notes) }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(strings.category) }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    Text(strings.priority, style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0 to strings.low, 1 to strings.medium, 2 to strings.high, 3 to strings.urgent).forEach { (p, lbl) ->
                            FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(lbl) })
                        }
                    }
                }
                item {
                    Text(strings.recurrence, style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(
                            listOf(
                                "NONE" to strings.recNone,
                                "DAILY" to strings.recDaily,
                                "WEEKLY" to strings.recWeekly,
                                "MONTHLY" to strings.recMonthly,
                                "CUSTOM" to strings.recCustom
                            )
                        ) { (r, lbl) ->
                            FilterChip(selected = recurrence == r, onClick = { recurrence = r }, label = { Text(lbl) })
                        }
                    }
                    if (recurrence == "CUSTOM") {
                        val daysLabel = when (strings.lang) {
                            NexaLang.AR -> "كل كم يوم"
                            NexaLang.FR -> "Tous les (jours)"
                            NexaLang.EN -> "Interval (days)"
                        }
                        OutlinedTextField(value = customDays, onValueChange = { customDays = it.filter { c -> c.isDigit() } }, label = { Text(daysLabel) })
                    }
                }
                item {
                    Text(strings.reminderLabel, style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 6, 24).forEach { hrs ->
                            val hrsLabel = when (strings.lang) {
                                NexaLang.AR -> "+$hrs س"
                                else -> "+${hrs}h"
                            }
                            FilterChip(selected = dueOffsetHours == hrs, onClick = { dueOffsetHours = hrs }, label = { Text(hrsLabel) })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = enableReminder, onCheckedChange = { enableReminder = it })
                        Text(strings.reminderLabel)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = focusGuardOnDue, onCheckedChange = { focusGuardOnDue = it })
                        Text(strings.focusGuardOnDue)
                    }
                    OutlinedButton(onClick = { filePicker.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Rounded.AttachFile, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(attachmentName.ifBlank { strings.attachFile })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isNotBlank()) {
                    val due = initial.dueAt ?: (System.currentTimeMillis() + dueOffsetHours * 3600_000L)
                    val rem = if (enableReminder) due - 15 * 60_000L else null
                    onSave(
                        initial.copy(
                            title = title.trim(),
                            description = desc.trim(),
                            notes = notes.trim(),
                            category = category.trim().ifBlank { strings.generalCategory },
                            priority = priority,
                            dueAt = due,
                            reminderAt = rem,
                            recurrenceType = recurrence,
                            recurrenceIntervalDays = customDays.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                            attachmentUri = attachmentUri,
                            attachmentName = attachmentName,
                            focusGuardOnDue = focusGuardOnDue
                        )
                    )
                }
            }) { Text(strings.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
