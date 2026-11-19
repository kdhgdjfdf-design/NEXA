package com.example.ui.screens.life

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.domain.i18n.NexaLang
import com.example.domain.prayer.PrayerTimesCalculator
import com.example.services.FocusGuardManager
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaScreenTitleRow
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaSegmentedTabs
import com.example.ui.components.NexaTintedBanner
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Dedicated "الصلاة" (Prayer & Worship Sanctuary) screen for NEXA:
 * Prayer Times + Next Prayer Banner + Animated Qibla Compass + Adhkar + Duas + Hijri Occasions.
 */
@Composable
fun PrayerSanctuaryScreen(
    prefs: NexaPrefsState,
    adhkarProgress: List<AdhkarProgressEntity>,
    duaFavorites: List<DuaFavoriteEntity>,
    focusGuardManager: FocusGuardManager? = null,
    blockedAppsCount: Int = 0,
    onTogglePrayerBlock: (Boolean) -> Unit = {},
    onOpenFocusGuard: () -> Unit = {},
    onSavePrayerConfig: (String, Double, Double, String) -> Unit,
    onTogglePrayerReminders: (Boolean) -> Unit,
    onIncrementDhikr: (DhikrItem) -> Unit,
    onToggleDuaFav: (String, Boolean) -> Unit,
    onScheduleOccasionReminder: (IslamicOccasion) -> Unit
) {
    val strings = LocalNexaStrings.current
    var activeTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("prayer_sanctuary_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NexaScreenTitleRow(
            title = strings.prayer,
            icon = Icons.Rounded.Mosque,
            accent = NexaSectionAccents.prayer
        )

        val tabs = listOf(strings.prayer, strings.adhkar, strings.duas, strings.islamicOccasions)
        NexaSegmentedTabs(
            tabs = tabs,
            selectedIndex = activeTab,
            onSelect = { activeTab = it }
        )

        when (activeTab) {
            0 -> PrayerTab(
                prefs = prefs,
                focusGuardManager = focusGuardManager,
                blockedAppsCount = blockedAppsCount,
                onTogglePrayerBlock = onTogglePrayerBlock,
                onOpenFocusGuard = onOpenFocusGuard,
                onSavePrayerConfig = onSavePrayerConfig,
                onTogglePrayerReminders = onTogglePrayerReminders
            )
            1 -> AdhkarTab(adhkarProgress, onIncrementDhikr)
            2 -> DuasTab(duaFavorites, onToggleDuaFav)
            3 -> OccasionsTab(onScheduleOccasionReminder)
        }
    }
}

@Composable
fun LifeScreen(
    prefs: NexaPrefsState,
    adhkarProgress: List<AdhkarProgressEntity>,
    duaFavorites: List<DuaFavoriteEntity>,
    moodEntries: List<MoodEntryEntity>,
    onSavePrayerConfig: (String, Double, Double, String) -> Unit,
    onTogglePrayerReminders: (Boolean) -> Unit,
    onIncrementDhikr: (DhikrItem) -> Unit,
    onToggleDuaFav: (String, Boolean) -> Unit,
    onAddMoodEntry: (Set<String>, String) -> Unit,
    onScheduleOccasionReminder: (IslamicOccasion) -> Unit
) {
    PrayerSanctuaryScreen(
        prefs = prefs,
        adhkarProgress = adhkarProgress,
        duaFavorites = duaFavorites,
        onSavePrayerConfig = onSavePrayerConfig,
        onTogglePrayerReminders = onTogglePrayerReminders,
        onIncrementDhikr = onIncrementDhikr,
        onToggleDuaFav = onToggleDuaFav,
        onScheduleOccasionReminder = onScheduleOccasionReminder
    )
}

@SuppressLint("MissingPermission")
@Composable
private fun PrayerTab(
    prefs: NexaPrefsState,
    focusGuardManager: FocusGuardManager?,
    blockedAppsCount: Int,
    onTogglePrayerBlock: (Boolean) -> Unit,
    onOpenFocusGuard: () -> Unit,
    onSavePrayerConfig: (String, Double, Double, String) -> Unit,
    onTogglePrayerReminders: (Boolean) -> Unit
) {
    val strings = LocalNexaStrings.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isA11yEnabled by remember {
        mutableStateOf(focusGuardManager?.isAccessibilityEnabled() ?: false)
    }

    DisposableEffect(lifecycleOwner, focusGuardManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isA11yEnabled = focusGuardManager?.isAccessibilityEnabled() ?: false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cal = remember { Calendar.getInstance() }
    val times = remember(prefs.prayerLat, prefs.prayerLon, prefs.prayerMethod) {
        PrayerTimesCalculator.calculate(prefs.prayerLat, prefs.prayerLon, prefs.prayerMethod, cal)
    }
    val hijri = remember { PrayerTimesCalculator.getHijriDate(cal) }
    val nowMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    val nextPrayer = times.nextPrayer(nowMin)
    val nextPrayerTitle = when (nextPrayer.first) {
        "FAJR" -> if (strings.lang == NexaLang.AR) "الفجر" else "Fajr"
        "SUNRISE" -> if (strings.lang == NexaLang.AR) "الشروق" else "Sunrise"
        "DHUHR" -> if (strings.lang == NexaLang.AR) "الظهر" else "Dhuhr"
        "ASR" -> if (strings.lang == NexaLang.AR) "العصر" else "Asr"
        "MAGHRIB" -> if (strings.lang == NexaLang.AR) "المغرب" else "Maghrib"
        else -> if (strings.lang == NexaLang.AR) "العشاء" else "Isha"
    }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            if (loc != null) {
                onSavePrayerConfig("${strings.cityLabel} (${String.format(Locale.US, "%.2f", loc.latitude)}, ${String.format(Locale.US, "%.2f", loc.longitude)})", loc.latitude, loc.longitude, prefs.prayerMethod)
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_section_prayer),
                    contentDescription = strings.prayer,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color(0x33120A06),
                                1f to Color(0xDF120A06)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${hijri.day} ${if (strings.lang == NexaLang.AR) hijri.monthNameAr else hijri.monthNameEn} ${hijri.year} هـ",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE9CFAF)
                            )
                            Text(
                                text = "$nextPrayerTitle • ${times.formatTime(nextPrayer.second)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${strings.cityLabel}: ${prefs.prayerCity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        QiblaCompassMini(qiblaDegrees = times.qiblaDegrees.toFloat())
                    }
                }
            }
        }

        // Dedicated "حجب التطبيقات أثناء وقت الصلاة" toggle & permission health card (#14)
        item {
            NexaCard(
                modifier = Modifier.testTag("prayer_block_card"),
                cornerRadius = 20.dp,
                elevation = 2.dp,
                borderColor = NexaSectionAccents.prayer.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                icon = Icons.Rounded.Shield,
                                tint = NexaSectionAccents.prayer,
                                size = 38.dp,
                                iconSize = 20.dp,
                                cornerRadius = 12.dp
                            )
                            Text(strings.prayerBlockTitle, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = prefs.focusGuardPrayerBlock,
                            onCheckedChange = onTogglePrayerBlock,
                            modifier = Modifier.testTag("prayer_block_switch")
                        )
                    }
                    Text(
                        text = strings.prayerBlockDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (prefs.focusGuardPrayerBlock && !isA11yEnabled && focusGuardManager != null) {
                        Text(
                            text = strings.accessibilityRequiredHint,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onOpenFocusGuard) {
                            Text("${strings.selectAppsToBlock} ($blockedAppsCount)")
                        }
                        if (!isA11yEnabled && focusGuardManager != null) {
                            FilledTonalButton(onClick = {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            }) {
                                Text(strings.openSettingsLabel)
                            }
                        }
                    }
                }
            }
        }

        item {
            NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${strings.qiblaDirection}: ${times.qiblaDegrees.roundToInt()}°", fontWeight = FontWeight.Bold)
                        FilledTonalButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                    ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                                if (loc != null) {
                                    onSavePrayerConfig("${strings.cityLabel} (${String.format(Locale.US, "%.2f", loc.latitude)}, ${String.format(Locale.US, "%.2f", loc.longitude)})", loc.latitude, loc.longitude, prefs.prayerMethod)
                                }
                            } else {
                                locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                            }
                        }) {
                            Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.locationPermissionLabel)
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(PrayerTimesCalculator.presetCities) { city ->
                            val cityLabel = when (strings.lang) {
                                NexaLang.AR -> city.nameAr
                                NexaLang.FR -> city.nameFr
                                NexaLang.EN -> city.nameEn
                            }
                            FilterChip(
                                selected = prefs.prayerCity == city.nameEn || prefs.prayerCity == city.nameAr,
                                onClick = { onSavePrayerConfig(cityLabel, city.lat, city.lon, prefs.prayerMethod) },
                                label = { Text(cityLabel) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.reminderLabel)
                        Switch(checked = prefs.prayerRemindersEnabled, onCheckedChange = onTogglePrayerReminders)
                    }
                }
            }
        }

        val prayerRows = listOf(
            Triple("FAJR", "الفجر • Fajr", times.formatTime(times.fajrMinutes)),
            Triple("SUNRISE", "الشروق • Sunrise", times.formatTime(times.sunriseMinutes)),
            Triple("DHUHR", "الظهر • Dhuhr", times.formatTime(times.dhuhrMinutes)),
            Triple("ASR", "العصر • Asr", times.formatTime(times.asrMinutes)),
            Triple("MAGHRIB", "المغرب • Maghrib", times.formatTime(times.maghribMinutes)),
            Triple("ISHA", "العشاء • Isha", times.formatTime(times.ishaMinutes))
        )
        items(prayerRows) { (key, name, timeStr) ->
            val isNext = nextPrayer.first == key
            NexaCard(
                cornerRadius = 18.dp,
                elevation = if (isNext) 3.dp else 1.dp,
                borderColor = if (isNext) NexaSectionAccents.prayer else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (isNext) {
                            NexaFilledIconBadge(
                                icon = Icons.Rounded.AccessTimeFilled,
                                tint = NexaSectionAccents.prayer,
                                size = 32.dp,
                                iconSize = 17.dp,
                                cornerRadius = 10.dp
                            )
                        }
                        Text(name, fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold)
                    }
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isNext) NexaSectionAccents.prayer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun QiblaCompassMini(qiblaDegrees: Float) {
    Box(
        modifier = Modifier.size(68.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = Color(0x661C120C),
                radius = r
            )
            drawCircle(
                color = Color(0xFFDAB795).copy(alpha = 0.75f),
                radius = r - 2.dp.toPx(),
                style = Stroke(width = 1.6.dp.toPx())
            )
            val rad = Math.toRadians((qiblaDegrees - 90f).toDouble())
            val tip = Offset(
                center.x + (r * 0.72f * cos(rad)).toFloat(),
                center.y + (r * 0.72f * sin(rad)).toFloat()
            )
            drawLine(
                color = Color(0xFFF6DEB3),
                start = center,
                end = tip,
                strokeWidth = 2.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFFF6DEB3),
                radius = 4.dp.toPx(),
                center = center
            )
        }
    }
}

@Composable
private fun AdhkarTab(
    progressList: List<AdhkarProgressEntity>,
    onIncrement: (DhikrItem) -> Unit
) {
    val strings = LocalNexaStrings.current
    var category by remember { mutableStateOf("MORNING") }
    val cats = listOf(
        "MORNING" to strings.adhkarMorning,
        "EVENING" to strings.adhkarEvening,
        "AFTER_PRAYER" to strings.adhkarAfterPrayer,
        "SLEEP" to strings.adhkarSleep,
        "WAKEUP" to strings.adhkarWakeup
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cats) { (k, lbl) ->
                FilterChip(selected = category == k, onClick = { category = k }, label = { Text(lbl) })
            }
        }
        val itemsList = DefaultSeedData.adhkarList.filter { it.category == category }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(itemsList, key = { it.id }) { item ->
                val count = progressList.firstOrNull { it.dhikrId == item.id && it.category == item.category }?.currentCount ?: 0
                val done = count >= item.targetCount
                NexaCard(
                    onClick = { onIncrement(item) },
                    cornerRadius = 20.dp,
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(item.arabicText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val virtue = when (strings.lang) {
                                NexaLang.AR -> item.virtueAr
                                NexaLang.FR -> item.virtueFr
                                NexaLang.EN -> item.virtueEn
                            }
                            Text(virtue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            AssistChip(
                                onClick = { onIncrement(item) },
                                label = { Text(if (done) "✓ $count/${item.targetCount}" else "$count/${item.targetCount}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuasTab(
    favorites: List<DuaFavoriteEntity>,
    onToggleFav: (String, Boolean) -> Unit
) {
    val strings = LocalNexaStrings.current
    var selectedCat by remember { mutableStateOf("ALL") }
    var search by remember { mutableStateOf("") }

    val cats = listOf("ALL", "STUDY", "CALM", "PARENTS", "GUIDANCE", "DAILY")
    val filtered = DefaultSeedData.duasList.filter { dua ->
        (selectedCat == "ALL" || dua.category == selectedCat) &&
            (search.isBlank() || dua.arabicText.contains(search, true) || dua.titleAr.contains(search, true) || dua.titleEn.contains(search, true))
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text(strings.search) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(cats) { c ->
                FilterChip(selected = selectedCat == c, onClick = { selectedCat = c }, label = { Text(strings.duaCategoryDisplay(c)) })
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(filtered, key = { it.id }) { dua ->
                val isFav = favorites.firstOrNull { it.duaId == dua.id }?.isFavorite == true
                val title = when (strings.lang) {
                    NexaLang.AR -> dua.titleAr
                    NexaLang.FR -> dua.titleFr
                    NexaLang.EN -> dua.titleEn
                }
                NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(title, fontWeight = FontWeight.Bold, color = NexaSectionAccents.prayer)
                            IconButton(onClick = { onToggleFav(dua.id, isFav) }) {
                                Icon(if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = strings.favorites)
                            }
                        }
                        Text(dua.arabicText, style = MaterialTheme.typography.bodyLarge)
                        Text(dua.reference, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun SerenityTab(
    entries: List<MoodEntryEntity>,
    onAddMood: (Set<String>, String) -> Unit
) {
    val strings = LocalNexaStrings.current
    val emotionKeys = remember { listOf("Calm", "Grateful", "Focused", "Tired", "Anxious", "Hopeful") }
    var selectedEmotions by remember { mutableStateOf(setOf<String>()) }
    var journalText by remember { mutableStateOf("") }

    val weeklyCount = entries.take(7).size
    val monthlyCount = entries.take(30).size
    val (selectedTagSummary, calmingPhrase) = remember(selectedEmotions, strings) {
        strings.serenityPhraseFor(selectedEmotions)
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            NexaTintedBanner(
                text = strings.serenityDisclaimer,
                icon = Icons.Rounded.Spa,
                accent = NexaSectionAccents.life
            )
        }
        item {
            NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(strings.logMoodPrompt, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(emotionKeys) { key ->
                            FilterChip(
                                selected = key in selectedEmotions,
                                onClick = {
                                    selectedEmotions = if (key in selectedEmotions) selectedEmotions - key else selectedEmotions + key
                                },
                                label = { Text(strings.emotionDisplay(key)) }
                            )
                        }
                    }

                    // Dynamic empathetic phrase linked directly to selected emotion(s) (#18)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("serenity_emotion_phrase_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NexaFilledIconBadge(
                                icon = Icons.Rounded.SelfImprovement,
                                tint = NexaSectionAccents.life,
                                size = 36.dp,
                                iconSize = 19.dp,
                                cornerRadius = 11.dp
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = if (selectedEmotions.isEmpty()) strings.serenityGuidanceLabel else selectedTagSummary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NexaSectionAccents.life
                                )
                                Text(
                                    text = calmingPhrase,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = journalText,
                        onValueChange = { journalText = it },
                        label = { Text(strings.notes) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Button(onClick = {
                        onAddMood(selectedEmotions, journalText)
                        selectedEmotions = emptySet()
                        journalText = ""
                    }) { Text(strings.save) }
                }
            }
        }
        item {
            Text(
                "${strings.weeklyMonthlyPatterns}: $weeklyCount (${strings.last7DaysLabel}) • $monthlyCount (${strings.last30DaysLabel})",
                fontWeight = FontWeight.Bold
            )
        }
        items(entries, key = { it.id }) { e ->
            NexaCard(cornerRadius = 18.dp, elevation = 1.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${e.dateIso} • ${strings.localizeEmotionsCsv(e.emotionsCsv)}",
                        fontWeight = FontWeight.SemiBold,
                        color = NexaSectionAccents.life
                    )
                    if (e.noteText.isNotBlank()) Text(e.noteText, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun OccasionsTab(onScheduleReminder: (IslamicOccasion) -> Unit) {
    val strings = LocalNexaStrings.current
    val hijri = remember { PrayerTimesCalculator.getHijriDate() }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NexaFilledIconBadge(
                        icon = Icons.Rounded.NightsStay,
                        tint = NexaSectionAccents.prayer,
                        size = 42.dp,
                        iconSize = 21.dp,
                        cornerRadius = 13.dp
                    )
                    Text("${hijri.day} ${if (strings.lang == NexaLang.AR) hijri.monthNameAr else hijri.monthNameEn} ${hijri.year} هـ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
        items(DefaultSeedData.islamicOccasions, key = { it.id }) { occ ->
            val title = when (strings.lang) {
                NexaLang.AR -> occ.titleAr
                NexaLang.FR -> occ.titleFr
                NexaLang.EN -> occ.titleEn
            }
            val note = when (strings.lang) {
                NexaLang.AR -> occ.noteAr
                NexaLang.FR -> occ.noteFr
                NexaLang.EN -> occ.noteEn
            }
            NexaCard(cornerRadius = 18.dp, elevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onScheduleReminder(occ) }) {
                        Icon(Icons.Rounded.NotificationsActive, contentDescription = strings.reminderLabel, tint = NexaSectionAccents.prayer)
                    }
                }
            }
        }
    }
}
