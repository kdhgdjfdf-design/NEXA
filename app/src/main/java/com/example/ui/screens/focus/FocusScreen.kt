package com.example.ui.screens.focus

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.services.FocusGuardManager
import com.example.services.FocusTimerUiState
import com.example.services.InstalledAppInfo
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaEmptyState
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaScreenTitleRow
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaSegmentedTabs
import com.example.ui.components.NexaTintedBanner
import com.example.ui.components.SynchronizedHourglass
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FocusScreen(
    prefs: NexaPrefsState,
    timerState: FocusTimerUiState,
    sessions: List<FocusSessionEntity>,
    blockedApps: List<BlockedAppEntity>,
    tasks: List<TaskEntity>,
    lessons: List<StudyLessonEntity>,
    focusGuardManager: FocusGuardManager,
    onStartSession: (mode: String, mins: Int, taskId: Long?, lessonId: Long?, title: String, sound: String, localUri: String) -> Unit,
    onPauseSession: () -> Unit,
    onResumeSession: () -> Unit,
    onFinishSession: () -> Unit,
    onToggleBlockedApp: (String, String, Boolean, Int, Boolean, Boolean, Boolean) -> Unit,
    onUpdateFocusGuardPrefs: (Boolean, Boolean, String) -> Unit,
    onUpdateBlockImage: (String) -> Unit,
    onCopyPhoto: (Uri) -> String,
    onPickHourglassBackground: (Uri) -> Unit = {},
    onRestoreHourglassBackground: () -> Unit = {}
) {
    val strings = LocalNexaStrings.current
    var activeTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NexaScreenTitleRow(
            title = strings.focusSpace,
            icon = Icons.Rounded.HourglassTop,
            accent = NexaSectionAccents.focus
        )

        val tabs = listOf(strings.focusTab, strings.focusGuard, strings.focusReports)
        NexaSegmentedTabs(
            tabs = tabs,
            selectedIndex = activeTab,
            onSelect = { activeTab = it }
        )

        when (activeTab) {
            0 -> FocusTimerTab(
                prefs = prefs,
                timerState = timerState,
                tasks = tasks.filter { !it.isCompleted },
                lessons = lessons,
                onStartSession = onStartSession,
                onPauseSession = onPauseSession,
                onResumeSession = onResumeSession,
                onFinishSession = onFinishSession,
                onPickHourglassBackground = onPickHourglassBackground,
                onRestoreHourglassBackground = onRestoreHourglassBackground
            )
            1 -> FocusGuardTab(
                prefs = prefs,
                blockedApps = blockedApps,
                focusGuardManager = focusGuardManager,
                onToggleBlockedApp = onToggleBlockedApp,
                onUpdatePrefs = onUpdateFocusGuardPrefs,
                onUpdateBlockImage = onUpdateBlockImage,
                onCopyPhoto = onCopyPhoto
            )
            2 -> FocusReportsTab(
                prefs = prefs,
                sessions = sessions
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FocusTimerTab(
    prefs: NexaPrefsState,
    timerState: FocusTimerUiState,
    tasks: List<TaskEntity>,
    lessons: List<StudyLessonEntity>,
    onStartSession: (String, Int, Long?, Long?, String, String, String) -> Unit,
    onPauseSession: () -> Unit,
    onResumeSession: () -> Unit,
    onFinishSession: () -> Unit,
    onPickHourglassBackground: (Uri) -> Unit,
    onRestoreHourglassBackground: () -> Unit
) {
    val strings = LocalNexaStrings.current
    var selectedMode by remember { mutableStateOf("POMODORO") }
    var customMinutes by remember { mutableStateOf("40") }
    var linkedTitle by remember { mutableStateOf("") }
    var linkedTaskId by remember { mutableStateOf<Long?>(null) }
    var linkedLessonId by remember { mutableStateOf<Long?>(null) }
    var showBgSheet by remember { mutableStateOf(false) }

    val bgPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            onPickHourglassBackground(uri)
        }
    }

    val customBgFile = remember(prefs.focusBackgroundUri) {
        SectionImageCodec.resolveExistingLocalFile(prefs.focusBackgroundUri)
    }

    val fraction = when {
        !timerState.isRunning -> 1f
        timerState.mode == "OPEN" -> (1f - ((timerState.elapsedFocusSeconds % 60) / 60f)).coerceIn(0f, 1f)
        timerState.plannedSeconds > 0 -> (timerState.remainingSeconds.toFloat() / timerState.plannedSeconds.toFloat()).coerceIn(0f, 1f)
        else -> 0f
    }

    val displaySec = if (!timerState.isRunning) {
        when (selectedMode) {
            "POMODORO" -> 1500L
            "CUSTOM" -> (customMinutes.toLongOrNull() ?: 40L) * 60L
            else -> 0L
        }
    } else if (timerState.mode == "OPEN") {
        timerState.elapsedFocusSeconds
    } else {
        timerState.remainingSeconds
    }

    val selectedModeLabel = when (selectedMode) {
        "POMODORO" -> strings.pomodoro
        "CUSTOM" -> strings.customTimer
        else -> strings.openSession
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!timerState.isRunning) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(selected = selectedMode == "POMODORO", onClick = { selectedMode = "POMODORO" }, label = { Text("${strings.pomodoro} (25 ${strings.minutesUnit})") })
                    }
                    item {
                        FilterChip(selected = selectedMode == "CUSTOM", onClick = { selectedMode = "CUSTOM" }, label = { Text(strings.customTimer) })
                    }
                    item {
                        FilterChip(selected = selectedMode == "OPEN", onClick = { selectedMode = "OPEN" }, label = { Text(strings.openSession) })
                    }
                }
            }
            if (selectedMode == "CUSTOM") {
                item {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { customMinutes = it.filter { c -> c.isDigit() } },
                        label = { Text(strings.minutesInputLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Luxury Hourglass Stage Card with customizable local gallery background
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .testTag("hourglass_stage_card"),
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (customBgFile != null) {
                        AsyncImage(
                            model = customBgFile,
                            contentDescription = strings.customizeFocusBg,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_hourglass_bg),
                            contentDescription = strings.focusSpace,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    // Warm roasted espresso gradient scrim for contrast
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color(0x88140C08),
                                    0.5f to Color(0x70140C08),
                                    1.0f to Color(0xD9120A06)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0x661C120C),
                                border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.22f))
                            ) {
                                Text(
                                    text = if (timerState.isRunning) {
                                        if (timerState.isPaused) strings.pauseFocus else strings.focusTab
                                    } else selectedModeLabel,
                                    color = Color(0xFFF5E6D3),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                onClick = { showBgSheet = true },
                                shape = CircleShape,
                                color = Color(0x661C120C),
                                border = BorderStroke(0.75.dp, Color.White.copy(alpha = 0.28f)),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("customize_hourglass_bg_btn")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.PhotoCamera,
                                        contentDescription = strings.customizeFocusBg,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        SynchronizedHourglass(
                            remainingFraction = fraction,
                            isRunning = timerState.isRunning && !timerState.isPaused,
                            size = 230.dp,
                            modifier = Modifier.testTag("focus_hourglass_canvas")
                        )

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0x60190F09),
                            border = BorderStroke(1.dp, Color(0xFFDAB795).copy(alpha = 0.28f))
                        ) {
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", displaySec / 60, displaySec % 60),
                                style = MaterialTheme.typography.displayMedium.merge(TextStyle(fontFeatureSettings = "tnum")),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFF6EB),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }

                        val shownLink = if (timerState.isRunning) timerState.linkedTitle else linkedTitle
                        if (shownLink.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x552E1C11)
                            ) {
                                Text(
                                    text = shownLink,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFEBD1B5),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (!timerState.isRunning) {
                                Button(
                                    onClick = {
                                        val mins = when (selectedMode) {
                                            "POMODORO" -> 25
                                            "CUSTOM" -> customMinutes.toIntOrNull()?.coerceIn(1, 300) ?: 40
                                            else -> 0
                                        }
                                        onStartSession(selectedMode, mins, linkedTaskId, linkedLessonId, linkedTitle, "NONE", "")
                                    },
                                    modifier = Modifier
                                        .height(50.dp)
                                        .testTag("start_focus_btn"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFDAB795),
                                        contentColor = Color(0xFF28160C)
                                    )
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(strings.startFocus, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                if (timerState.isPaused) {
                                    Button(
                                        onClick = onResumeSession,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFDAB795),
                                            contentColor = Color(0xFF28160C)
                                        )
                                    ) { Text(strings.resumeFocus, fontWeight = FontWeight.Bold) }
                                } else {
                                    FilledTonalButton(onClick = onPauseSession) { Text(strings.pauseFocus) }
                                }
                                OutlinedButton(
                                    onClick = onFinishSession,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFF2E2)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f))
                                ) { Text(strings.finishFocus) }
                            }
                        }
                    }
                }
            }
        }

        if (!timerState.isRunning && (tasks.isNotEmpty() || lessons.isNotEmpty())) {
            item {
                NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(strings.linkTaskOrLesson, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(tasks.take(8)) { t ->
                                FilterChip(
                                    selected = linkedTaskId == t.id,
                                    onClick = {
                                        val nowSel = linkedTaskId != t.id
                                        linkedTaskId = if (nowSel) t.id else null
                                        linkedLessonId = null
                                        linkedTitle = if (nowSel) t.title else ""
                                    },
                                    label = { Text(t.title) }
                                )
                            }
                            items(lessons.take(8)) { l ->
                                FilterChip(
                                    selected = linkedLessonId == l.id,
                                    onClick = {
                                        val nowSel = linkedLessonId != l.id
                                        linkedLessonId = if (nowSel) l.id else null
                                        linkedTaskId = null
                                        linkedTitle = if (nowSel) l.title else ""
                                    },
                                    label = { Text(l.title) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBgSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showBgSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(strings.customizeFocusBg, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (customBgFile != null) strings.customImageActive else strings.defaultImageActive,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        bgPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pick_hourglass_bg_btn")
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.chooseFromGallery)
                }
                if (customBgFile != null) {
                    OutlinedButton(
                        onClick = {
                            onRestoreHourglassBackground()
                            showBgSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restore_hourglass_bg_btn")
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.restoreDefaultImage)
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusGuardTab(
    prefs: NexaPrefsState,
    blockedApps: List<BlockedAppEntity>,
    focusGuardManager: FocusGuardManager,
    onToggleBlockedApp: (String, String, Boolean, Int, Boolean, Boolean, Boolean) -> Unit,
    onUpdatePrefs: (Boolean, Boolean, String) -> Unit,
    onUpdateBlockImage: (String) -> Unit,
    onCopyPhoto: (Uri) -> String
) {
    val strings = LocalNexaStrings.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var installedApps by remember { mutableStateOf<List<InstalledAppInfo>>(emptyList()) }
    var searchApp by remember { mutableStateOf("") }
    var customMsg by remember(prefs.focusGuardCustomMessage) { mutableStateOf(prefs.focusGuardCustomMessage) }

    var isA11yOn by remember { mutableStateOf(focusGuardManager.isAccessibilityEnabled()) }
    var hasUsage by remember { mutableStateOf(focusGuardManager.hasUsageStatsPermission()) }
    var isBatteryExempt by remember { mutableStateOf(focusGuardManager.isIgnoringBatteryOptimizations()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isA11yOn = focusGuardManager.isAccessibilityEnabled()
                hasUsage = focusGuardManager.hasUsageStatsPermission()
                isBatteryExempt = focusGuardManager.isIgnoringBatteryOptimizations()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        installedApps = focusGuardManager.getLaunchableInstalledApps()
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onUpdateBlockImage(onCopyPhoto(uri))
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            NexaTintedBanner(
                text = strings.oemBatteryDisclaimer,
                icon = Icons.Rounded.Info,
                accent = NexaSectionAccents.focus
            )
        }

        if (!isA11yOn) {
            item {
                NexaCard(
                    modifier = Modifier.testTag("accessibility_warning_card"),
                    cornerRadius = 18.dp,
                    elevation = 2.dp,
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NexaFilledIconBadge(
                                icon = Icons.Rounded.WarningAmber,
                                tint = MaterialTheme.colorScheme.error,
                                size = 36.dp,
                                iconSize = 19.dp,
                                cornerRadius = 11.dp
                            )
                            Text(
                                text = strings.accessibilityRequiredHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                            Text(strings.openSettingsLabel)
                        }
                    }
                }
            }
        }

        item {
            NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.Shield,
                            tint = NexaSectionAccents.focus,
                            size = 38.dp,
                            iconSize = 20.dp,
                            cornerRadius = 12.dp
                        )
                        Text(strings.protectionStatus, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${strings.accessibilityServiceLabel}: ${if (isA11yOn) strings.enabledLabel else strings.disabledLabel}", modifier = Modifier.weight(1f))
                        TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text(strings.openSettingsLabel) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${strings.usageStatsLabel}: ${if (hasUsage) strings.enabledLabel else strings.disabledLabel}", modifier = Modifier.weight(1f))
                        TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) { Text(strings.openSettingsLabel) }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${strings.batteryUnrestrictedLabel}: ${if (isBatteryExempt) strings.yesLabel else strings.noLabel}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        if (!isBatteryExempt) {
                            TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }) { Text(strings.openSettingsLabel) }
                        }
                    }
                }
            }
        }

        item {
            NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings.blockScreenCustomizer, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.countdown30sNote, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        Switch(checked = prefs.focusGuardCountdownEnabled, onCheckedChange = { onUpdatePrefs(prefs.focusGuardPrayerBlock, it, customMsg) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.prayerBlockTitle, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        Switch(checked = prefs.focusGuardPrayerBlock, onCheckedChange = { onUpdatePrefs(it, prefs.focusGuardCountdownEnabled, customMsg) })
                    }
                    OutlinedTextField(
                        value = customMsg,
                        onValueChange = {
                            customMsg = it
                            onUpdatePrefs(prefs.focusGuardPrayerBlock, prefs.focusGuardCountdownEnabled, it)
                        },
                        label = { Text(strings.notes) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Rounded.Image, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(strings.attachPhoto)
                    }
                }
            }
        }

        item {
            Text(strings.selectAppsToBlock, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = searchApp,
                onValueChange = { searchApp = it },
                placeholder = { Text(strings.search) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        val visibleApps = installedApps.filter { searchApp.isBlank() || it.label.contains(searchApp, true) }
        items(visibleApps, key = { it.packageName }) { appInfo ->
            val saved = blockedApps.firstOrNull { it.packageName == appInfo.packageName }
            val enabled = saved?.isEnabled == true
            NexaCard(cornerRadius = 18.dp, elevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(appInfo.label, fontWeight = FontWeight.SemiBold)
                        Text(appInfo.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { checked ->
                            onToggleBlockedApp(
                                appInfo.packageName,
                                appInfo.label,
                                checked,
                                saved?.dailyLimitMinutes ?: 0,
                                saved?.blockInFocus ?: true,
                                saved?.blockInTasks ?: true,
                                saved?.blockInPrayer ?: true
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusReportsTab(
    prefs: NexaPrefsState,
    sessions: List<FocusSessionEntity>
) {
    val strings = LocalNexaStrings.current
    val totalMinutes = sessions.sumOf { (it.elapsedFocusSeconds / 60).toInt() }
    val completedSessions = sessions.count { it.status == "COMPLETED" }
    val fmt = remember { SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.Assessment,
                            tint = NexaSectionAccents.focus,
                            size = 44.dp,
                            iconSize = 22.dp,
                            cornerRadius = 14.dp
                        )
                        Column {
                            Text(strings.focusTodayMinutes, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalMinutes ${strings.minutesUnit}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${strings.dailyGoalPrefix}: ${prefs.focusDailyGoalMinutes} ${strings.minutesUnit}")
                        Text("$completedSessions ${strings.sessionsCountLabel}", fontWeight = FontWeight.SemiBold, color = NexaSectionAccents.focus)
                    }
                }
            }
        }
        if (sessions.isEmpty()) {
            item {
                NexaEmptyState(
                    message = strings.focusReports,
                    icon = Icons.Rounded.HourglassEmpty,
                    accent = NexaSectionAccents.focus
                )
            }
        }
        items(sessions, key = { it.id }) { s ->
            val modeLabel = when (s.mode) {
                "POMODORO" -> strings.pomodoro
                "CUSTOM" -> strings.customTimer
                else -> strings.openSession
            }
            val pausedMinutes = (s.pausedSeconds / 60).toInt()
            val pausedLabel = String.format(Locale.getDefault(), strings.pausedMinutesExcluded, pausedMinutes)
            NexaCard(cornerRadius = 18.dp, elevation = 1.dp) {
                ListItem(
                    headlineContent = { Text("$modeLabel • ${s.elapsedFocusSeconds / 60} ${strings.minutesUnit} ${if (s.linkedTitle.isNotBlank()) "(${s.linkedTitle})" else ""}") },
                    supportingContent = { Text("${fmt.format(Date(s.startedAt))} • $pausedLabel") },
                    leadingContent = {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.Timer,
                            tint = NexaSectionAccents.focus,
                            size = 38.dp,
                            iconSize = 19.dp,
                            cornerRadius = 11.dp
                        )
                    }
                )
            }
        }
    }
}
