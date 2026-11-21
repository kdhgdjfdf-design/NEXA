package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

private val Room = Color(0xFFF4F4F0)
private val Ink = Color(0xFF17191B)
private val Muted = Color(0xFF777C80)
private val Line = Color(0xFFE3E4E0)
private val Accent = Color(0xFFB8A27A)
private val Soft = Color(0xFFE9E9E3)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Area51App(this) }
    }
}

private enum class AreaSystem(val title: String, val subtitle: String) {
    KNOWLEDGE("KNOWLEDGE", "مساحة المعرفة"),
    LIBRARY("LIBRARY", "مساحة القراءة"),
    MINDSET("MINDSET", "مساحة التشجيع"),
    PASSION("PASSION", "مساحة الشغف"),
    PROJECT_90("PROJECT 90", "النظام الأكاديمي")
}

private enum class AreaTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ROOM("الغرفة", Icons.Rounded.GridView),
    PROJECT("PROJECT 90", Icons.Rounded.Timeline),
    FOCUS("التركيز", Icons.Rounded.HourglassTop),
    LOGBOOK("السجل", Icons.Rounded.MenuBook)
}

@Composable
private fun Area51App(context: Context) {
    val prefs = remember { context.getSharedPreferences("area51_local", Context.MODE_PRIVATE) }
    var selectedSystem by rememberSaveable { mutableStateOf(AreaSystem.PROJECT_90) }
    var tab by rememberSaveable { mutableStateOf(AreaTab.ROOM) }
    var page by rememberSaveable { mutableStateOf("home") }
    var projectStarted by rememberSaveable { mutableStateOf(prefs.getBoolean("started", false)) }
    var completed by rememberSaveable { mutableIntStateOf(prefs.getInt("completed", 0)) }
    var focusSeconds by rememberSaveable { mutableIntStateOf(25 * 60) }
    var focusRunning by rememberSaveable { mutableStateOf(false) }
    var focusDone by rememberSaveable { mutableIntStateOf(prefs.getInt("focusDone", 0)) }
    var noteText by rememberSaveable { mutableStateOf("") }
    var notes by remember { mutableStateOf(loadNotes(prefs)) }
    var diagnosticAnswer by rememberSaveable { mutableStateOf("") }
    var diagnosticAnswers by rememberSaveable { mutableStateOf("") }
    var diagnosticStep by rememberSaveable { mutableIntStateOf(0) }
    var showDiagnosticResult by rememberSaveable { mutableStateOf(false) }
    var dailyChecks by remember { mutableStateOf(loadChecks(prefs)) }

    LaunchedEffect(focusRunning, focusSeconds) {
        if (focusRunning && focusSeconds > 0) {
            delay(1000)
            focusSeconds -= 1
        } else if (focusRunning && focusSeconds == 0) {
            focusRunning = false
            focusDone += 1
            prefs.edit().putInt("focusDone", focusDone).apply()
        }
    }

    val scheme = lightColorScheme(
        primary = Ink,
        onPrimary = Color.White,
        secondary = Accent,
        background = Room,
        surface = Color.White,
        onSurface = Ink,
        onBackground = Ink,
        outline = Line
    )

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = Room,
            bottomBar = {
                if (selectedSystem == AreaSystem.PROJECT_90) {
                    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                        AreaTab.entries.forEach { item ->
                            NavigationBarItem(
                                selected = tab == item,
                                onClick = { tab = item; page = "home" },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Ink,
                                    selectedTextColor = Ink,
                                    indicatorColor = Soft,
                                    unselectedIconColor = Muted,
                                    unselectedTextColor = Muted
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding)
            ) {
                AreaHeader(
                    system = selectedSystem,
                    onSelectSystem = { next ->
                        selectedSystem = next
                        tab = AreaTab.ROOM
                        page = "home"
                    },
                    onBack = { page = "home" },
                    showBack = page != "home"
                )
                if (selectedSystem != AreaSystem.PROJECT_90) {
                    SystemSpacePage(system = selectedSystem)
                } else when {
                    page == "diagnostic" -> DiagnosticPage(
                        step = diagnosticStep,
                        answer = diagnosticAnswer,
                        answers = diagnosticAnswers,
                        onAnswer = { diagnosticAnswer = it },
                        onNext = {
                            diagnosticAnswers += diagnosticAnswer + "\n"
                            if (diagnosticStep < 3) {
                                diagnosticStep += 1
                                diagnosticAnswer = ""
                            } else {
                                showDiagnosticResult = true
                            }
                        },
                        showResult = showDiagnosticResult,
                        onClose = { page = "home"; diagnosticStep = 0; diagnosticAnswer = ""; diagnosticAnswers = ""; showDiagnosticResult = false }
                    )
                    page == "errors" -> ErrorBookPage(
                        text = noteText,
                        onText = { noteText = it },
                        notes = notes,
                        onSave = {
                            if (noteText.isNotBlank()) {
                                notes = notes + noteText.trim()
                                saveNotes(prefs, notes)
                                noteText = ""
                            }
                        },
                        onDelete = { index ->
                            notes = notes.toMutableList().also { it.removeAt(index) }
                            saveNotes(prefs, notes)
                        }
                    )
                    page == "settings" -> SettingsPage(onResetFocus = { focusSeconds = 25 * 60; focusRunning = false })
                    else -> when (tab) {
                        AreaTab.ROOM -> RoomPage(
                            started = projectStarted,
                            completed = completed,
                            focusDone = focusDone,
                            onStart = {
                                projectStarted = true
                                prefs.edit().putBoolean("started", true).apply()
                                tab = AreaTab.PROJECT
                            },
                            onOpenProject = { tab = AreaTab.PROJECT },
                            onOpenDiagnostic = { page = "diagnostic" },
                            onOpenErrors = { page = "errors" }
                        )
                        AreaTab.PROJECT -> ProjectPage(
                            started = projectStarted,
                            completed = completed,
                            checks = dailyChecks,
                            onStart = {
                                projectStarted = true
                                prefs.edit().putBoolean("started", true).apply()
                            },
                            onToggle = { key, checked ->
                                dailyChecks = dailyChecks.toMutableMap().also { it[key] = checked }
                                prefs.edit().putString("checks", JSONObject(dailyChecks as Map<*, *>).toString()).apply()
                                completed = dailyChecks.values.count { it }
                                prefs.edit().putInt("completed", completed).apply()
                            },
                            onDiagnostic = { page = "diagnostic" },
                            onErrors = { page = "errors" }
                        )
                        AreaTab.FOCUS -> FocusPage(
                            seconds = focusSeconds,
                            running = focusRunning,
                            onToggle = { focusRunning = !focusRunning },
                            onReset = { focusRunning = false; focusSeconds = 25 * 60 },
                            onAdjust = { minutes -> focusRunning = false; focusSeconds = minutes * 60 }
                        )
                        AreaTab.LOGBOOK -> LogbookPage(
                            notes = notes,
                            onAdd = { page = "errors" },
                            onDiagnostic = { page = "diagnostic" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaHeader(
    system: AreaSystem,
    showBack: Boolean,
    onBack: () -> Unit,
    onSelectSystem: (AreaSystem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth().background(Room).statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box {
            Row(
                modifier = Modifier.clickable { expanded = true }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    system.title,
                    fontWeight = FontWeight.Black,
                    color = Ink,
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 1.4.sp
                )
                Icon(Icons.Rounded.ExpandMore, contentDescription = "تغيير النظام", tint = Muted)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                AreaSystem.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(option.title, fontWeight = FontWeight.SemiBold, color = Ink)
                                Text(option.subtitle, color = Muted, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        onClick = {
                            expanded = false
                            onSelectSystem(option)
                        },
                        trailingIcon = {
                            if (option == system) Icon(Icons.Rounded.Check, contentDescription = "النظام الحالي", tint = Accent)
                        }
                    )
                }
            }
        }
        if (showBack) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "رجوع", tint = Muted)
            }
        }
    }
}


@Composable
private fun SystemSpacePage(system: AreaSystem) {
    val items = when (system) {
        AreaSystem.KNOWLEDGE -> listOf(
            Triple("العلوم", "افهم الظواهر والأفكار التي تفسّر العالم.", Icons.Rounded.Science),
            Triple("التاريخ والحضارات", "تعرّف إلى الأحداث التي شكّلت عالمنا.", Icons.Rounded.AccountBalance),
            Triple("الفلسفة والتفكير", "درّب عقلك على السؤال والتحليل.", Icons.Rounded.Psychology),
            Triple("التكنولوجيا", "اكتشف كيف تتطور الأدوات والأفكار.", Icons.Rounded.Memory)
        )
        AreaSystem.LIBRARY -> listOf(
            Triple("كتبك", "اجمع الكتب التي تريد قراءتها في مكان واحد.", Icons.Rounded.MenuBook),
            Triple("مقالات مختارة", "قراءات قصيرة توسّع معرفتك.", Icons.Rounded.Article),
            Triple("ملاحظات القراءة", "احتفظ بالأفكار والاقتباسات المهمة.", Icons.Rounded.EditNote)
        )
        AreaSystem.MINDSET -> listOf(
            Triple("رسالة اليوم", "لا تنتظر الحماس كي تبدأ؛ ابدأ بخطوة صغيرة.", Icons.Rounded.Lightbulb),
            Triple("مبادئ الغرفة", "الانضباط هدوء في اتخاذ القرار، وليس عقوبة.", Icons.Rounded.Psychology),
            Triple("تحدٍ ذهني", "اسأل نفسك: ما الفكرة التي غيّرت رأيي مؤخراً؟", Icons.Rounded.Forum)
        )
        AreaSystem.PASSION -> listOf(
            Triple("الطب والعلوم", "استكشف جسم الإنسان والعلوم الطبية.", Icons.Rounded.MedicalServices),
            Triple("اللغات والثقافات", "افتح نافذة على لغات وثقافات جديدة.", Icons.Rounded.Translate),
            Triple("الابتكار والمشاريع", "حوّل فضولك إلى تجربة أو مشروع صغير.", Icons.Rounded.Explore)
        )
        AreaSystem.PROJECT_90 -> emptyList()
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(system.subtitle, color = Muted, style = MaterialTheme.typography.labelLarge)
            Text(
                when (system) {
                    AreaSystem.KNOWLEDGE -> "وسّع حدود معرفتك."
                    AreaSystem.LIBRARY -> "اقرأ بعمق، واحتفظ بما يستحق."
                    AreaSystem.MINDSET -> "ابنِ طريقة تفكيرك."
                    AreaSystem.PASSION -> "اتبع فضولك واكتشف ما يشعل اهتمامك."
                    AreaSystem.PROJECT_90 -> "نظامك الأكاديمي."
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                when (system) {
                    AreaSystem.KNOWLEDGE -> "مساحة للاستكشاف والتعلّم خارج حدود المنهج."
                    AreaSystem.LIBRARY -> "مكتبتك الشخصية للأفكار والكتب والقراءات."
                    AreaSystem.MINDSET -> "تذكيرات وتحديات قصيرة تساعدك على الاستمرار بوعي."
                    AreaSystem.PASSION -> "مساحة لاكتشاف مجالات جديدة وتجربة اهتماماتك."
                    AreaSystem.PROJECT_90 -> ""
                },
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        items(items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        Modifier.size(46.dp).background(Room, RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.third, contentDescription = null, tint = Ink)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.first, color = Ink, fontWeight = FontWeight.Bold)
                        Text(item.second, color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Accent)
                }
            }
        }
        item {
            Text(
                "هذه المساحة مستقلة عن PROJECT 90. ستُضاف أدواتها ومحتوياتها تدريجياً دون ازدحام.",
                color = Muted,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun RoomPage(
    started: Boolean,
    completed: Int,
    focusDone: Int,
    onStart: () -> Unit,
    onOpenProject: () -> Unit,
    onOpenDiagnostic: () -> Unit,
    onOpenErrors: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("مساحتك الخاصة.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Ink)
            Text("مكان هادئ لبناء عقل أقوى، خطوة واحدة في كل مرة.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Ink)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PROJECT 90", color = Color.White.copy(alpha = .7f), style = MaterialTheme.typography.labelLarge, letterSpacing = 1.5.sp)
                        Text(if (started) "ACTIVE" else "READY", color = Accent, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(if (started) "رحلتك بدأت." else "ابدأ ببناء أساسك العلمي.", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(if (started) "لا نطارد الساعات. نثبت الفهم." else "90 يوماً من الفهم، التدريب، والاستقلالية.", color = Color.White.copy(alpha = .68f))
                    if (started) {
                        LinearProgressIndicator(progress = { (completed / 3f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = Accent, trackColor = Color.White.copy(alpha = .18f))
                        Text("$completed / 3 مهام اليوم • $focusDone جلسات تركيز مكتملة", color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.labelMedium)
                        Button(onClick = onOpenProject, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Ink), shape = RoundedCornerShape(14.dp)) {
                            Text("متابعة المشروع")
                            Icon(Icons.Rounded.ArrowForward, contentDescription = null)
                        }
                    } else {
                        Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Ink), shape = RoundedCornerShape(14.dp)) {
                            Text("تهيئة PROJECT 90")
                            Icon(Icons.Rounded.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
        item {
            Text("بواباتك", fontWeight = FontWeight.Bold, color = Ink, style = MaterialTheme.typography.titleMedium)
        }
        item {
            FeatureCard("التشخيص العلمي", "اكتشف نقطة البداية قبل أن نضع المنهج.", Icons.Rounded.Biotech, "ابدأ التشخيص", onOpenDiagnostic)
        }
        item {
            FeatureCard("دفتر الأخطاء", "حوّل كل خطأ إلى معرفة لا تتكرر.", Icons.Rounded.EditNote, "افتح الدفتر", onOpenErrors)
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("قاعدة الغرفة", color = Muted, style = MaterialTheme.typography.labelMedium)
                    Text("الفهم قبل السرعة. والاستمرارية قبل الكمال.", color = Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(46.dp).background(Room, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Ink)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Bold, color = Ink)
                Text(description, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = action, tint = Accent)
        }
    }
}

@Composable
private fun ProjectPage(
    started: Boolean,
    completed: Int,
    checks: Map<String, Boolean>,
    onStart: () -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onDiagnostic: () -> Unit,
    onErrors: () -> Unit
) {
    val missions = listOf(
        "math" to "مسألة رياضيات بفهم كامل",
        "science" to "مفهوم علمي واحد بعمق",
        "recall" to "استرجاع ما تعلمته دون النظر"
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("PROJECT 90", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Ink)
            Text("من التأسيس إلى الاستقلال العلمي.", color = Muted)
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("التقدم اليومي", fontWeight = FontWeight.Bold)
                        Text("$completed / 3", color = Accent, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(progress = { completed / 3f }, modifier = Modifier.fillMaxWidth(), color = Ink, trackColor = Soft)
                    Text(if (started) "تقدمك يُقاس بما أتقنته، لا بعدد الساعات." else "لم تبدأ الرحلة بعد. يمكنك تجهيز التشخيص أولاً.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    if (!started) Button(onClick = onStart) { Text("تفعيل المشروع") }
                }
            }
        }
        item { Text("مهمة اليوم", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
        items(missions) { (key, label) ->
            Row(
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).clickable { onToggle(key, !(checks[key] ?: false)) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = checks[key] ?: false, onCheckedChange = { onToggle(key, it) }, colors = CheckboxDefaults.colors(checkedColor = Ink))
                Spacer(Modifier.width(8.dp))
                Text(label, color = Ink, modifier = Modifier.weight(1f))
            }
        }
        item { Text("المواد الأساسية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) }
        item { SubjectRow("الرياضيات", "Math", "المنطق وحل المسائل", "01") }
        item { SubjectRow("الفيزياء", "Physics", "فهم القوانين والظواهر", "02") }
        item { SubjectRow("الكيمياء", "Chemistry", "المادة والتفاعلات", "03") }
        item { SubjectRow("علوم الحياة والأرض", "Life & Earth", "الأنظمة والعلاقات الحيوية", "04") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDiagnostic, modifier = Modifier.weight(1f)) { Text("التشخيص") }
                OutlinedButton(onClick = onErrors, modifier = Modifier.weight(1f)) { Text("دفتر الأخطاء") }
            }
        }
    }
}

@Composable
private fun SubjectRow(name: String, english: String, detail: String, number: String) {
    Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(number, color = Accent, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, fontWeight = FontWeight.Bold, color = Ink)
            Text("$english • $detail", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Rounded.ArrowOutward, contentDescription = null, tint = Muted)
    }
}

@Composable
private fun FocusPage(seconds: Int, running: Boolean, onToggle: () -> Unit, onReset: () -> Unit, onAdjust: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("FOCUS CHAMBER", color = Muted, style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
        Text("جلسة واحدة. مهمة واحدة.", color = Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Box(Modifier.size(250.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("%02d:%02d".format(seconds / 60, seconds % 60), color = Ink, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Light)
                Text(if (running) "مستمر" else "جاهز", color = Muted)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(15, 25, 40).forEach { mins ->
                FilterChip(selected = seconds == mins * 60 && !running, onClick = { onAdjust(mins) }, label = { Text("${mins}m") })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onToggle, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Ink)) {
                Icon(if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (running) "إيقاف مؤقت" else "ابدأ")
            }
            OutlinedButton(onClick = onReset, shape = RoundedCornerShape(14.dp)) { Text("إعادة ضبط") }
        }
        Text("استراحة قصيرة بعد الجلسة. لا حاجة لمطاردة عدد الساعات.", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LogbookPage(notes: List<String>, onAdd: () -> Unit, onDiagnostic: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("السجل الشخصي", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Ink)
            Text("أفكارك، أخطاؤك، وما تعلمته.", color = Muted)
        }
        item { FeatureCard("دفتر الأخطاء", "راجع أسباب الخطأ وتأكد أنك تجاوزته.", Icons.Rounded.EditNote, "إضافة خطأ", onAdd) }
        item { FeatureCard("التشخيص", "حدد نقطة البداية في المواد الأربع.", Icons.Rounded.Assignment, "متابعة", onDiagnostic) }
        item { Text("آخر الملاحظات", fontWeight = FontWeight.Bold) }
        if (notes.isEmpty()) {
            item { Text("لا توجد ملاحظات بعد. ابدأ بتسجيل أول فكرة أو خطأ.", color = Muted) }
        } else {
            items(notes.takeLast(5).reversed()) { note ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Text(note, Modifier.fillMaxWidth().padding(16.dp), color = Ink)
                }
            }
        }
    }
}

@Composable
private fun ErrorBookPage(text: String, onText: (String) -> Unit, notes: List<String>, onSave: () -> Unit, onDelete: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("دفتر الأخطاء", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Ink)
            Text("الخطأ معلومة عن طريقة تفكيرك. سجّله ثم تعلّم منه.", color = Muted)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = text, onValueChange = onText, modifier = Modifier.fillMaxWidth(), minLines = 4, label = { Text("السؤال أو الخطأ أو القاعدة التي تريد تذكرها") })
                    Button(onClick = onSave, enabled = text.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("حفظ في السجل") }
                }
            }
        }
        items(notes.mapIndexed { index, value -> index to value }.reversed()) { (index, value) ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(value, Modifier.weight(1f), color = Ink)
                    IconButton(onClick = { onDelete(index) }) { Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف", tint = Muted) }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticPage(step: Int, answer: String, answers: String, onAnswer: (String) -> Unit, onNext: () -> Unit, showResult: Boolean, onClose: () -> Unit) {
    val questions = listOf(
        "رياضيات" to "ما قيمة 3/4 + 1/2 ؟",
        "فيزياء" to "ما وحدة قياس السرعة في النظام الدولي؟",
        "كيمياء" to "ما أصغر وحدة تحتفظ بخصائص العنصر؟",
        "علوم الحياة والأرض" to "أين تتم معظم عملية إنتاج الطاقة في الخلية؟"
    )
    val options = listOf(
        listOf("1/4", "5/4", "4/6", "3/8"),
        listOf("متر", "متر/ثانية", "كيلوغرام", "ثانية"),
        listOf("الجزيء", "الذرة", "الخلية", "المركب"),
        listOf("النواة", "الغشاء", "الميتوكوندريا", "الريبوسوم")
    )
    val correct = listOf("5/4", "متر/ثانية", "الذرة", "الميتوكوندريا")
    val q = questions[step.coerceIn(0, 3)]
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("INITIAL ASSESSMENT", color = Muted, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.5.sp)
            Text("التشخيص الأولي", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Ink)
            Text("سؤال واحد في كل مرة. هذا فحص مبدئي فقط، وسنحتاج لاحقاً إلى أسئلة أعمق.", color = Muted)
        }
        if (showResult) {
            val score = answers.split("\n").filterIndexed { index, value -> index < correct.size && value == correct[index] }.size
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("اكتمل الفحص الأولي", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text("نتيجتك: $score / 4", color = Accent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        Text("هذا فحص تمهيدي قصير، وليس حكماً على مستواك. سنبني التشخيص الكامل تدريجياً، مع مراعاة مستواك الدراسي.", color = Muted)
                        Button(onClick = onClose) { Text("العودة إلى الغرفة") }
                    }
                }
            }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(q.first, color = Accent, fontWeight = FontWeight.Bold)
                    Text("${step + 1} / 4", color = Muted)
                }
                LinearProgressIndicator(progress = { (step + 1) / 4f }, modifier = Modifier.fillMaxWidth(), color = Ink, trackColor = Soft)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(22.dp)) {
                    Text(q.second, Modifier.fillMaxWidth().padding(20.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Ink)
                }
            }
            items(options[step]) { option ->
                Row(
                    Modifier.fillMaxWidth().background(if (answer == option) Soft else Color.White, RoundedCornerShape(16.dp))
                        .clickable { onAnswer(option) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = answer == option, onClick = { onAnswer(option) })
                    Text(option, color = Ink)
                }
            }
            item {
                Button(onClick = onNext, enabled = answer.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    Text(if (step == 3) "إنهاء الفحص" else "السؤال التالي")
                }
            }
        }
    }
}

@Composable
private fun SettingsPage(onResetFocus: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("الإعدادات", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("AREA 51 يعمل محلياً في هذه النسخة التجريبية. بياناتك تبقى على الجهاز.", color = Muted)
        OutlinedButton(onClick = onResetFocus) { Text("إعادة ضبط مؤقت التركيز") }
        Text("النسخة الأولى • White Room", color = Muted, style = MaterialTheme.typography.labelSmall)
    }
}

private fun loadNotes(prefs: android.content.SharedPreferences): List<String> = try {
    val array = JSONArray(prefs.getString("notes", "[]"))
    (0 until array.length()).map { array.getString(it) }
} catch (_: Exception) { emptyList() }

private fun saveNotes(prefs: android.content.SharedPreferences, notes: List<String>) {
    val array = JSONArray()
    notes.forEach { array.put(it) }
    prefs.edit().putString("notes", array.toString()).apply()
}

private fun loadChecks(prefs: android.content.SharedPreferences): Map<String, Boolean> = try {
    val obj = JSONObject(prefs.getString("checks", "{}"))
    listOf("math", "science", "recall").associateWith { obj.optBoolean(it, false) }
} catch (_: Exception) {
    mapOf("math" to false, "science" to false, "recall" to false)
}
