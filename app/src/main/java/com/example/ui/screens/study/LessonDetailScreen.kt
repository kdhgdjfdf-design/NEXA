package com.example.ui.screens.study

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.domain.study.SpacedRepetitionEngine
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaEmptyState
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaSegmentedTabs
import com.example.ui.components.NexaTintedBanner
import com.example.ui.components.RuledSideNoteOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SubjectDetailScreen(
    subject: StudySubjectEntity,
    lessons: List<StudyLessonEntity>,
    flashcards: List<StudyFlashcardEntity>,
    grades: List<StudyGradeEntity>,
    onBack: () -> Unit,
    onOpenLesson: (StudyLessonEntity) -> Unit,
    onSaveLesson: (StudyLessonEntity) -> Unit,
    onDeleteLesson: (StudyLessonEntity) -> Unit,
    onSaveFlashcard: (StudyFlashcardEntity) -> Unit,
    onRateFlashcard: (StudyFlashcardEntity, Int) -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalNexaStrings.current
    val context = LocalContext.current
    var showAddLesson by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddLesson = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.addLesson)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("subject_detail_back_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                    }
                }
                NexaFilledIconBadge(
                    icon = Icons.Rounded.MenuBook,
                    tint = NexaSectionAccents.study,
                    size = 42.dp,
                    iconSize = 20.dp,
                    cornerRadius = 13.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        subject.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${strings.coefficient}: ${subject.coefficient} • ${strings.targetAverage}: ${subject.targetAverage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 84.dp)) {
                items(lessons, key = { it.id }) { lesson ->
                    NexaCard(
                        modifier = Modifier.defaultMinSize(minHeight = 76.dp),
                        cornerRadius = 20.dp,
                        elevation = 2.dp,
                        onClick = { onOpenLesson(lesson) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NexaFilledIconBadge(
                                icon = Icons.Rounded.Description,
                                tint = NexaSectionAccents.study,
                                size = 44.dp,
                                iconSize = 22.dp,
                                cornerRadius = 14.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (lesson.pdfName.isNotBlank()) Text("PDF: ${lesson.pdfName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (lesson.youtubeUrl.isNotBlank()) Text(strings.youtubeAppLabel, style = MaterialTheme.typography.labelSmall, color = NexaSectionAccents.study)
                                }
                            }
                            IconButton(onClick = { onDeleteLesson(lesson) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddLesson) {
        var title by remember { mutableStateOf("") }
        var youtubeUrl by remember { mutableStateOf("") }
        var pdfUri by remember { mutableStateOf("") }
        var pdfName by remember { mutableStateOf("") }
        var importance by remember { mutableIntStateOf(1) }

        val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                pdfUri = uri.toString()
                pdfName = uri.lastPathSegment?.substringAfterLast('/') ?: "document.pdf"
            }
        }

        AlertDialog(
            onDismissRequest = { showAddLesson = false },
            title = { Text(strings.addLesson) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(strings.title) })
                    OutlinedTextField(value = youtubeUrl, onValueChange = { youtubeUrl = it }, label = { Text(strings.youtubeLink) })
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0 to strings.low, 1 to strings.medium, 2 to strings.high).forEach { (imp, lbl) ->
                            FilterChip(selected = importance == imp, onClick = { importance = imp }, label = { Text(lbl) })
                        }
                    }
                    OutlinedButton(onClick = { pdfPicker.launch(arrayOf("application/pdf")) }) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(pdfName.ifBlank { strings.attachPdf })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onSaveLesson(
                            StudyLessonEntity(
                                subjectId = subject.id,
                                title = title.trim(),
                                importance = importance,
                                pdfUri = pdfUri,
                                pdfName = pdfName,
                                youtubeUrl = youtubeUrl.trim()
                            )
                        )
                    }
                    showAddLesson = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showAddLesson = false }) { Text(strings.cancel) } }
        )
    }
}

@Composable
fun LessonDetailScreen(
    subject: StudySubjectEntity,
    lesson: StudyLessonEntity,
    flashcards: List<StudyFlashcardEntity>,
    reviewLogs: List<StudyReviewLogEntity>,
    onBack: () -> Unit,
    onUpdateLesson: (StudyLessonEntity) -> Unit,
    onSaveFlashcard: (StudyFlashcardEntity) -> Unit,
    onDeleteFlashcard: (StudyFlashcardEntity) -> Unit,
    onRateFlashcard: (StudyFlashcardEntity, Int) -> Unit,
    onRecordMemoryTest: (Long, Long?, Int, Int, Int) -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalNexaStrings.current
    val context = LocalContext.current

    var sideNoteOpen by remember { mutableStateOf(false) }
    var noteDraft by remember(lesson.id) { mutableStateOf(lesson.sideNoteText) }
    var activeTab by remember { mutableIntStateOf(0) }
    var showPdfModal by remember { mutableStateOf(false) }
    var showAddCard by remember { mutableStateOf(false) }
    var suggestedPairs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val videoId = remember(lesson.youtubeUrl) { extractYoutubeId(lesson.youtubeUrl) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("lesson_detail_back_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        lesson.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        subject.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FilledTonalButton(onClick = { sideNoteOpen = !sideNoteOpen }) {
                    Icon(Icons.Rounded.EditNote, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(strings.notes)
                }
            }

            val tabs = listOf(strings.lessons, strings.flashcards, strings.spacedReview, strings.testYourMemory)
            NexaSegmentedTabs(
                tabs = tabs,
                selectedIndex = activeTab,
                onSelect = { activeTab = it }
            )

            when (activeTab) {
                0 -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                        if (lesson.pdfUri.isNotBlank()) {
                            item {
                                NexaCard(cornerRadius = 18.dp, elevation = 2.dp) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, tint = NexaSectionAccents.study)
                                            Text(lesson.pdfName.ifBlank { "PDF" }, fontWeight = FontWeight.SemiBold)
                                        }
                                        Button(onClick = { showPdfModal = true }) { Text(strings.openPdf) }
                                    }
                                }
                            }
                        }

                        if (videoId != null) {
                            item {
                                NexaCard(cornerRadius = 18.dp, elevation = 2.dp) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("${strings.youtubeAppLabel} • ${lesson.title}", fontWeight = FontWeight.Bold)
                                        AndroidView(
                                            factory = { ctx ->
                                                WebView(ctx).apply {
                                                    settings.javaScriptEnabled = true
                                                    webViewClient = WebViewClient()
                                                    loadUrl("https://www.youtube-nocookie.com/embed/$videoId")
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(210.dp)
                                        )
                                        TextButton(onClick = {
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(lesson.youtubeUrl)))
                                            }
                                        }) {
                                            Icon(Icons.Rounded.OpenInNew, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text(strings.youtubeAppLabel)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            NexaCard(cornerRadius = 18.dp, elevation = 2.dp) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(strings.sideNoteTitle, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = noteDraft.ifBlank { strings.sideNoteHint },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    FilledTonalButton(onClick = { sideNoteOpen = true }) {
                                        Text(strings.edit)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { showAddCard = true }) { Text(strings.addFlashcard) }
                            OutlinedButton(onClick = {
                                suggestedPairs = SpacedRepetitionEngine.suggestFlashcardsFromNotes(noteDraft)
                            }) { Text(strings.suggestFromNotes) }
                        }
                        if (suggestedPairs.isNotEmpty()) {
                            NexaCard(
                                cornerRadius = 18.dp,
                                elevation = 1.dp,
                                borderColor = NexaSectionAccents.study.copy(alpha = 0.35f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(strings.suggestFromNotes, fontWeight = FontWeight.Bold, color = NexaSectionAccents.study)
                                    suggestedPairs.forEach { (f, b) ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("$f -> $b", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                            TextButton(onClick = {
                                                onSaveFlashcard(StudyFlashcardEntity(subjectId = subject.id, lessonId = lesson.id, front = f, back = b))
                                                suggestedPairs = suggestedPairs.filterNot { it.first == f }
                                            }) { Text(strings.add) }
                                        }
                                    }
                                }
                            }
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(flashcards, key = { it.id }) { card ->
                                NexaCard(cornerRadius = 18.dp, elevation = 1.dp) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(card.front, fontWeight = FontWeight.Bold)
                                            Text(card.back, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = { onDeleteFlashcard(card) }) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    val dueCards = flashcards.filter { it.nextReviewAt <= System.currentTimeMillis() }.ifEmpty { flashcards }
                    var idx by remember { mutableIntStateOf(0) }
                    var revealed by remember { mutableStateOf(false) }
                    val card = dueCards.getOrNull(idx % dueCards.size.coerceAtLeast(1))

                    if (card == null) {
                        NexaEmptyState(
                            message = strings.addFlashcard,
                            icon = Icons.Rounded.Style,
                            accent = NexaSectionAccents.study
                        )
                    } else {
                        NexaCard(cornerRadius = 22.dp, elevation = 2.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(card.front, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                if (revealed) {
                                    HorizontalDivider()
                                    Text(card.back, style = MaterialTheme.typography.bodyLarge)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(1 to strings.again, 2 to strings.hard, 3 to strings.good, 4 to strings.easy).forEach { (q, lbl) ->
                                            FilledTonalButton(onClick = {
                                                onRateFlashcard(card, q)
                                                revealed = false
                                                idx++
                                            }) { Text(lbl) }
                                        }
                                    }
                                } else {
                                    Button(onClick = { revealed = true }) { Text(strings.showAnswerLabel) }
                                }
                            }
                        }
                    }
                }

                3 -> LocalMemoryQuizTab(
                    subject = subject,
                    lesson = lesson,
                    flashcards = flashcards,
                    onRecordResult = onRecordMemoryTest
                )
            }
        }

        if (sideNoteOpen) {
            RuledSideNoteOverlay(
                noteText = noteDraft,
                onNoteChange = { updated ->
                    noteDraft = updated
                    onUpdateLesson(lesson.copy(sideNoteText = updated))
                },
                onClose = { sideNoteOpen = false },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }

    if (showPdfModal && lesson.pdfUri.isNotBlank()) {
        NativePdfViewerDialog(pdfUri = Uri.parse(lesson.pdfUri), onDismiss = { showPdfModal = false })
    }

    if (showAddCard) {
        var front by remember { mutableStateOf("") }
        var back by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCard = false },
            title = { Text(strings.addFlashcard) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = front, onValueChange = { front = it }, label = { Text(strings.flashcardFrontLabel) })
                    OutlinedTextField(value = back, onValueChange = { back = it }, label = { Text(strings.flashcardBackLabel) })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (front.isNotBlank() && back.isNotBlank()) {
                        onSaveFlashcard(StudyFlashcardEntity(subjectId = subject.id, lessonId = lesson.id, front = front.trim(), back = back.trim()))
                    }
                    showAddCard = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showAddCard = false }) { Text(strings.cancel) } }
        )
    }
}

@Composable
private fun LocalMemoryQuizTab(
    subject: StudySubjectEntity,
    lesson: StudyLessonEntity,
    flashcards: List<StudyFlashcardEntity>,
    onRecordResult: (Long, Long?, Int, Int, Int) -> Unit
) {
    val strings = LocalNexaStrings.current
    var qIdx by remember { mutableIntStateOf(0) }
    var answer by remember { mutableStateOf("") }
    var lastScore by remember { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        NexaTintedBanner(
            text = strings.testMemoryDisclaimer,
            icon = Icons.Rounded.Psychology,
            accent = NexaSectionAccents.study
        )

        if (flashcards.isEmpty()) {
            NexaEmptyState(
                message = strings.addFlashcard,
                icon = Icons.Rounded.Psychology,
                accent = NexaSectionAccents.study
            )
        } else {
            val card = flashcards[qIdx % flashcards.size]
            NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(card.front, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        label = { Text(strings.notes) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        val score = SpacedRepetitionEngine.evaluateRecallLocally(card.back, answer)
                        lastScore = score
                        onRecordResult(subject.id, lesson.id, score, 1, if (score >= 60) 1 else 0)
                    }) {
                        Text(strings.confirm)
                    }
                    if (lastScore != null) {
                        Text("≈ ${lastScore}% (${card.back})", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        TextButton(onClick = {
                            qIdx++
                            answer = ""
                            lastScore = null
                        }) { Text(strings.nextItemLabel) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NativePdfViewerDialog(pdfUri: Uri, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var pageIndex by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pdfUri, pageIndex) {
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openFileDescriptor(pdfUri, "r")?.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        pageCount = renderer.pageCount
                        if (pageCount > 0) {
                            val idx = pageIndex.coerceIn(0, pageCount - 1)
                            renderer.openPage(idx).use { page ->
                                val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                bitmap = bmp
                            }
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PDF (${pageIndex + 1}/$pageCount)") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (bitmap != null) {
                    Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = LocalNexaStrings.current.pdfPageDesc, modifier = Modifier.fillMaxWidth().height(340.dp))
                } else {
                    CircularProgressIndicator()
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { if (pageIndex > 0) pageIndex-- }, enabled = pageIndex > 0) { Text("<") }
                    OutlinedButton(onClick = { if (pageIndex < pageCount - 1) pageIndex++ }, enabled = pageIndex < pageCount - 1) { Text(">") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(LocalNexaStrings.current.confirm) } }
    )
}

private fun extractYoutubeId(url: String): String? {
    if (url.isBlank()) return null
    val patterns = listOf(
        Regex("v=([A-Za-z0-9_-]{11})"),
        Regex("youtu\\.be/([A-Za-z0-9_-]{11})"),
        Regex("embed/([A-Za-z0-9_-]{11})")
    )
    return patterns.firstNotNullOfOrNull { it.find(url)?.groupValues?.getOrNull(1) }
}
