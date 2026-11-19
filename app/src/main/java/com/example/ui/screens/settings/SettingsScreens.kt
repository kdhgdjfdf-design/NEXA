package com.example.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.domain.security.CryptoAndLockManager
import com.example.services.FocusGuardManager
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaScreenTitleRow
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaSegmentedTabs
import com.example.ui.components.NexaTintedBanner
import com.example.ui.theme.NexaFonts
import com.example.ui.theme.NexaPalettes
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    prefs: NexaPrefsState,
    profiles: List<DesignProfileEntity>,
    activeProfile: DesignProfileEntity?,
    backupRecords: List<BackupRecordEntity>,
    focusGuardManager: FocusGuardManager,
    onSetLanguage: (String) -> Unit,
    onSelectProfile: (Long) -> Unit,
    onCreateProfile: (String) -> Unit,
    onDeleteProfile: (DesignProfileEntity) -> Unit,
    onUpdateActiveProfile: ((DesignProfileEntity) -> DesignProfileEntity) -> Unit,
    onRestoreDefaultDesign: () -> Unit,
    onToggleGlobalEdit: (Boolean) -> Unit,
    onCopyPhoto: (Uri, String) -> String,
    onSetPin: (String) -> Unit,
    onSetBiometric: (Boolean) -> Unit,
    onSetAutoLock: (Int) -> Unit,
    onSetLockedSections: (Set<String>) -> Unit,
    onSetHideNotifContent: (Boolean) -> Unit,
    onToggleAutoBackup: (Boolean) -> Unit,
    onCreateBackupNow: ((Boolean) -> Unit) -> Unit,
    onExportBackup: (Uri, (Boolean) -> Unit) -> Unit,
    onRestoreBackup: (String, (Result<Int>) -> Unit) -> Unit
) {
    val strings = LocalNexaStrings.current
    var activeTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(2.dp))
        NexaScreenTitleRow(
            title = strings.settings,
            subtitle = strings.offlineLocalBadge,
            icon = Icons.Rounded.Settings,
            accent = MaterialTheme.colorScheme.primary
        )

        val tabs = listOf(strings.customizationStudio, strings.accountAndPrivacy, strings.appStatus)
        NexaSegmentedTabs(
            tabs = tabs,
            selectedIndex = activeTab,
            onSelect = { activeTab = it },
            testTagPrefix = "settings_tab"
        )

        when (activeTab) {
            0 -> CustomizationStudioTab(
                prefs = prefs,
                profiles = profiles,
                activeProfile = activeProfile,
                onSetLanguage = onSetLanguage,
                onSelectProfile = onSelectProfile,
                onCreateProfile = onCreateProfile,
                onDeleteProfile = onDeleteProfile,
                onUpdateActiveProfile = onUpdateActiveProfile,
                onRestoreDefaultDesign = onRestoreDefaultDesign,
                onToggleGlobalEdit = onToggleGlobalEdit,
                onCopyPhoto = onCopyPhoto
            )
            1 -> AccountPrivacyTab(
                prefs = prefs,
                backupRecords = backupRecords,
                onSetPin = onSetPin,
                onSetBiometric = onSetBiometric,
                onSetAutoLock = onSetAutoLock,
                onSetLockedSections = onSetLockedSections,
                onSetHideNotifContent = onSetHideNotifContent,
                onToggleAutoBackup = onToggleAutoBackup,
                onCreateBackupNow = onCreateBackupNow,
                onExportBackup = onExportBackup,
                onRestoreBackup = onRestoreBackup
            )
            2 -> AppStatusTab(
                prefs = prefs,
                backupRecords = backupRecords,
                focusGuardManager = focusGuardManager
            )
        }
    }
}

@Composable
private fun CustomizationStudioTab(
    prefs: NexaPrefsState,
    profiles: List<DesignProfileEntity>,
    activeProfile: DesignProfileEntity?,
    onSetLanguage: (String) -> Unit,
    onSelectProfile: (Long) -> Unit,
    onCreateProfile: (String) -> Unit,
    onDeleteProfile: (DesignProfileEntity) -> Unit,
    onUpdateActiveProfile: ((DesignProfileEntity) -> DesignProfileEntity) -> Unit,
    onRestoreDefaultDesign: () -> Unit,
    onToggleGlobalEdit: (Boolean) -> Unit,
    onCopyPhoto: (Uri, String) -> String
) {
    val strings = LocalNexaStrings.current
    var showCreateDialog by remember { mutableStateOf(false) }

    val homeBannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val saved = onCopyPhoto(uri, "home_banner")
            onUpdateActiveProfile { it.copy(homeBannerUri = saved) }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            NexaCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.Translate,
                            tint = NexaSectionAccents.study,
                            size = 36.dp,
                            iconSize = 18.dp,
                            cornerRadius = 11.dp
                        )
                        Text(strings.languageLabel, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ar" to "العربية", "en" to "English", "fr" to "Français").forEach { (code, lbl) ->
                            FilterChip(
                                selected = prefs.language == code,
                                onClick = { onSetLanguage(code) },
                                label = { Text(lbl) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("lang_chip_$code")
                            )
                        }
                    }
                }
            }
        }

        item {
            NexaCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.DashboardCustomize,
                            tint = NexaSectionAccents.life,
                            size = 38.dp,
                            iconSize = 19.dp,
                            cornerRadius = 11.dp
                        )
                        Column {
                            Text(strings.globalEditMode, fontWeight = FontWeight.Bold)
                            Text(strings.editHomeLayout, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(checked = prefs.globalEditMode, onCheckedChange = onToggleGlobalEdit)
                }
            }
        }

        item {
            NexaCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            NexaFilledIconBadge(
                                icon = Icons.Rounded.Layers,
                                tint = NexaSectionAccents.badges,
                                size = 36.dp,
                                iconSize = 18.dp,
                                cornerRadius = 11.dp
                            )
                            Text(strings.designProfiles, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = { showCreateDialog = true },
                            enabled = profiles.size < 10
                        ) { Text("${strings.createProfile} (${profiles.size}/10)") }
                    }
                    profiles.forEach { prof ->
                        val selected = prof.id == activeProfile?.id
                        Surface(
                            onClick = { onSelectProfile(prof.id) },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selected, onClick = { onSelectProfile(prof.id) })
                                Text(prof.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                                if (!prof.isBuiltIn) {
                                    IconButton(onClick = { onDeleteProfile(prof) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            NexaCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.Palette,
                            tint = NexaSectionAccents.summaries,
                            size = 36.dp,
                            iconSize = 18.dp,
                            cornerRadius = 11.dp
                        )
                        Text(strings.themeMode, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SYSTEM" to strings.themeSystem, "LIGHT" to strings.themeLight, "DARK" to strings.themeDark).forEach { (m, lbl) ->
                            FilterChip(
                                selected = (activeProfile?.themeMode ?: "SYSTEM") == m,
                                onClick = { onUpdateActiveProfile { it.copy(themeMode = m) } },
                                label = { Text(lbl) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Text(strings.colorPaletteLabel, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(NexaPalettes.all) { pal ->
                            val isSel = activeProfile?.paletteKey == pal.key
                            FilterChip(
                                selected = isSel,
                                onClick = { onUpdateActiveProfile { it.copy(paletteKey = pal.key) } },
                                leadingIcon = {
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(pal.previewColor))
                                },
                                label = { Text(if (prefs.language == "ar") pal.labelAr else pal.labelEn) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Text(strings.fontFamily, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(NexaFonts.options) { (key, label) ->
                            FilterChip(
                                selected = activeProfile?.fontKey == key,
                                onClick = { onUpdateActiveProfile { it.copy(fontKey = key) } },
                                label = { Text(label) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Text(strings.iconStyle, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ROUNDED", "SHARP", "MINIMAL").forEach { style ->
                            FilterChip(
                                selected = activeProfile?.iconStyle == style,
                                onClick = { onUpdateActiveProfile { it.copy(iconStyle = style) } },
                                label = { Text(strings.iconStyleDisplay(style)) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                homeBannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Image, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(strings.attachPhoto)
                        }
                        TextButton(onClick = onRestoreDefaultDesign) {
                            Text(strings.restoreDefaultDesign)
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(strings.createProfile) },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(strings.title) }) },
            confirmButton = {
                Button(onClick = {
                    onCreateProfile(name)
                    showCreateDialog = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text(strings.cancel) } }
        )
    }
}

@Composable
private fun AccountPrivacyTab(
    prefs: NexaPrefsState,
    backupRecords: List<BackupRecordEntity>,
    onSetPin: (String) -> Unit,
    onSetBiometric: (Boolean) -> Unit,
    onSetAutoLock: (Int) -> Unit,
    onSetLockedSections: (Set<String>) -> Unit,
    onSetHideNotifContent: (Boolean) -> Unit,
    onToggleAutoBackup: (Boolean) -> Unit,
    onCreateBackupNow: ((Boolean) -> Unit) -> Unit,
    onExportBackup: (Uri, (Boolean) -> Unit) -> Unit,
    onRestoreBackup: (String, (Result<Int>) -> Unit) -> Unit
) {
    val strings = LocalNexaStrings.current
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) {
            onExportBackup(uri) { ok ->
                statusBanner = if (ok) strings.backupExportedMsg else strings.backupErrorMsg
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            onRestoreBackup(uri.toString()) { res ->
                statusBanner = res.fold(
                    onSuccess = { cnt -> "✓ $cnt" },
                    onFailure = { e -> "${strings.backupErrorMsg}: ${e.message ?: ""}" }
                )
            }
        }
    }

    val fmt = remember { SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            NexaTintedBanner(
                text = strings.offlineFirstNotice,
                icon = Icons.Rounded.Security,
                accent = NexaSectionAccents.focus
            )
        }
        if (statusBanner != null) {
            item {
                NexaTintedBanner(
                    text = statusBanner!!,
                    icon = Icons.Rounded.CheckCircle,
                    accent = NexaSectionAccents.prayer
                )
            }
        }

        item {
            NexaCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            NexaFilledIconBadge(
                                icon = Icons.Rounded.Pin,
                                tint = NexaSectionAccents.tasks,
                                size = 36.dp,
                                iconSize = 18.dp,
                                cornerRadius = 11.dp
                            )
                            Text(
                                "${strings.setPin}: ${if (prefs.pinHash.isNotBlank()) strings.enabledLabel else strings.disabledLabel}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        FilledTonalButton(
                            onClick = { showPinDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(strings.setPin) }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.biometricLock)
                        Switch(
                            checked = prefs.biometricEnabled,
                            enabled = CryptoAndLockManager.isBiometricAvailable(context) && prefs.pinHash.isNotBlank(),
                            onCheckedChange = onSetBiometric
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.hideNotifContent)
                        Switch(checked = prefs.hideNotificationContent, onCheckedChange = onSetHideNotifContent)
                    }
                    Text(strings.autoLockTimeout, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 1, 5, 15).forEach { mins ->
                            FilterChip(
                                selected = prefs.autoLockMinutes == mins,
                                onClick = { onSetAutoLock(mins) },
                                label = { Text("$mins ${strings.minutesUnit}") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Text(strings.lockedSections, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(
                            listOf(
                                "study" to strings.studyOrganizer,
                                "focus" to strings.focusSpace,
                                "prayer" to strings.prayer,
                                "summaries" to strings.summaries,
                                "life" to strings.life
                            )
                        ) { (sec, lbl) ->
                            val locked = sec in prefs.lockedSections
                            FilterChip(
                                selected = locked,
                                enabled = prefs.pinHash.isNotBlank(),
                                onClick = {
                                    val next = if (locked) prefs.lockedSections - sec else prefs.lockedSections + sec
                                    onSetLockedSections(next)
                                },
                                label = { Text(lbl) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            NexaCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.EnhancedEncryption,
                            tint = NexaSectionAccents.focus,
                            size = 36.dp,
                            iconSize = 18.dp,
                            cornerRadius = 11.dp
                        )
                        Text(strings.encryptedBackups, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.autoBackupToggle)
                        Switch(checked = prefs.autoBackupEnabled, onCheckedChange = onToggleAutoBackup)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onCreateBackupNow { ok ->
                                    statusBanner = if (ok) strings.backupCreatedMsg else strings.backupErrorMsg
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(strings.createBackupNow) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { exportLauncher.launch("nexa_backup.nexabak") },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(strings.exportBackupFile) }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("*/*")) },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(strings.restoreBackupFile) }
                    }
                }
            }
        }

        items(backupRecords, key = { it.id }) { rec ->
            NexaCard(cornerRadius = 16.dp, elevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(rec.fileName, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${fmt.format(Date(rec.createdAt))} • ${rec.itemCount} • ${strings.backupStatusLabel(rec.status)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = {
                        onRestoreBackup(rec.filePathOrUri) { res ->
                            statusBanner = res.fold(
                                onSuccess = { cnt -> "✓ $cnt" },
                                onFailure = { e -> "${strings.backupErrorMsg}: ${e.message ?: ""}" }
                            )
                        }
                    }) { Text(strings.restore) }
                }
            }
        }
    }

    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(strings.setPin) },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it.filter { c -> c.isDigit() }.take(8) },
                    label = { Text(strings.pinRuleHint) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onSetPin(newPin)
                    showPinDialog = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text(strings.cancel) } }
        )
    }
}

@Composable
private fun AppStatusTab(
    prefs: NexaPrefsState,
    backupRecords: List<BackupRecordEntity>,
    focusGuardManager: FocusGuardManager
) {
    val strings = LocalNexaStrings.current
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val dbSizeKb = remember {
        (context.getDatabasePath("nexa_offline.db").length() / 1024L)
    }
    val filesSizeKb = remember {
        (dirSizeBytes(context.filesDir) / 1024L)
    }
    val cacheSizeKb = remember {
        (dirSizeBytes(context.cacheDir) / 1024L)
    }

    var notifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var locGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var a11y by remember { mutableStateOf(focusGuardManager.isAccessibilityEnabled()) }
    var usage by remember { mutableStateOf(focusGuardManager.hasUsageStatsPermission()) }
    var battery by remember { mutableStateOf(focusGuardManager.isIgnoringBatteryOptimizations()) }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else true
                locGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                a11y = focusGuardManager.isAccessibilityEnabled()
                usage = focusGuardManager.hasUsageStatsPermission()
                battery = focusGuardManager.isIgnoringBatteryOptimizations()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifGranted = granted
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
        item {
            NexaCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.Storage,
                            tint = NexaSectionAccents.study,
                            size = 36.dp,
                            iconSize = 18.dp,
                            cornerRadius = 11.dp
                        )
                        Text(strings.storageUsage, fontWeight = FontWeight.Bold)
                    }
                    Text("${strings.databaseLabel}: $dbSizeKb KB", style = MaterialTheme.typography.bodyMedium)
                    Text("${strings.appFilesLabel}: $filesSizeKb KB", style = MaterialTheme.typography.bodyMedium)
                    Text("${strings.cacheLabel}: $cacheSizeKb KB", style = MaterialTheme.typography.bodyMedium)
                    Text("${strings.backupsCountLabel}: ${backupRecords.size}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            NexaCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.Shield,
                            tint = NexaSectionAccents.focus,
                            size = 36.dp,
                            iconSize = 18.dp,
                            cornerRadius = 11.dp
                        )
                        Text(strings.permissionsHealth, fontWeight = FontWeight.Bold)
                    }
                    StatusActionRow(strings.notificationsPermissionLabel, notifGranted, strings.fixNowLabel) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    StatusActionRow(strings.locationPermissionLabel, locGranted, strings.fixNowLabel) {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    StatusActionRow(strings.accessibilityServiceLabel, a11y, strings.fixNowLabel) {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    StatusActionRow(strings.usageStatsLabel, usage, strings.fixNowLabel) {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    StatusActionRow(strings.batteryUnrestrictedLabel, battery, strings.fixNowLabel) {
                        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    }
                }
            }
        }
        item {
            NexaTintedBanner(
                text = strings.offlineModeStatusNote,
                icon = Icons.Rounded.VerifiedUser,
                accent = NexaSectionAccents.focus
            )
        }
    }
}

@Composable
private fun StatusActionRow(label: String, ok: Boolean, fixLabel: String, onFix: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                contentDescription = null,
                tint = if (ok) NexaSectionAccents.prayer else Color(0xFFD97706)
            )
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        if (!ok) {
            TextButton(onClick = onFix) { Text(fixLabel, fontWeight = FontWeight.SemiBold) }
        }
    }
}

private fun dirSizeBytes(dir: File): Long {
    if (!dir.exists()) return 0L
    var sum = 0L
    dir.listFiles()?.forEach { f ->
        sum += if (f.isDirectory) dirSizeBytes(f) else f.length()
    }
    return sum
}
