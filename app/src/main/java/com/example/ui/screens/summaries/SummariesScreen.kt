package com.example.ui.screens.summaries

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.domain.i18n.LocalNexaStrings
import com.example.services.PodcastPlayerController
import com.example.services.PodcastUiState
import com.example.ui.components.NexaCard
import com.example.ui.components.NexaEmptyState
import com.example.ui.components.NexaFilledIconBadge
import com.example.ui.components.NexaScreenTitleRow
import com.example.ui.components.NexaSectionAccents
import com.example.ui.components.NexaSegmentedTabs
import com.example.ui.screens.library.LibraryScreen

/**
 * Dedicated "الملخصات" (Summaries, Notes, Flashcards, Quotes & Library) hub for NEXA.
 */
@Composable
fun SummariesScreen(
    subjects: List<StudySubjectEntity>,
    lessons: List<StudyLessonEntity>,
    flashcards: List<StudyFlashcardEntity>,
    libraryItems: List<LibraryItemEntity>,
    quotes: List<SavedQuoteEntity>,
    podcastEpisodes: List<PodcastEpisodeEntity>,
    podcastState: PodcastUiState,
    podcastController: PodcastPlayerController,
    onSaveLesson: (StudyLessonEntity) -> Unit,
    onRateFlashcard: (StudyFlashcardEntity, Int) -> Unit,
    onUpdateBook: (LibraryItemEntity) -> Unit,
    onImportTextBook: (String, String, String, String) -> Unit,
    onSaveQuote: (LibraryItemEntity, Int, String) -> Unit,
    onSaveCustomQuote: (quoteText: String, sourceTitle: String, author: String) -> Unit,
    onDeleteQuote: (SavedQuoteEntity) -> Unit,
    onTogglePodcastFav: (PodcastEpisodeEntity) -> Unit,
    onDownloadPodcast: (PodcastEpisodeEntity, (Boolean) -> Unit) -> Unit
) {
    val strings = LocalNexaStrings.current
    var activeTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("summaries_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NexaScreenTitleRow(
                title = strings.summaries,
                icon = Icons.Rounded.LibraryBooks,
                accent = NexaSectionAccents.summaries
            )
            val tabs = listOf(strings.lessonNotesSummary, strings.allFlashcardsDeck, strings.savedQuotes, strings.library)
            NexaSegmentedTabs(
                tabs = tabs,
                selectedIndex = activeTab,
                onSelect = { activeTab = it }
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeTab) {
                0 -> LessonNotesSummaryTab(
                    subjects = subjects,
                    lessons = lessons,
                    flashcards = flashcards,
                    onSaveLesson = onSaveLesson
                )
                1 -> FlashcardsDeckTab(
                    subjects = subjects,
                    flashcards = flashcards,
                    onRateFlashcard = onRateFlashcard
                )
                2 -> SavedQuotesTab(
                    quotes = quotes,
                    onSaveCustomQuote = onSaveCustomQuote,
                    onDeleteQuote = onDeleteQuote
                )
                3 -> LibraryScreen(
                    itemsList = libraryItems,
                    quotes = quotes,
                    podcastEpisodes = podcastEpisodes,
                    podcastState = podcastState,
                    podcastController = podcastController,
                    onUpdateBook = onUpdateBook,
                    onImportTextBook = onImportTextBook,
                    onSaveQuote = onSaveQuote,
                    onDeleteQuote = onDeleteQuote,
                    onTogglePodcastFav = onTogglePodcastFav,
                    onDownloadPodcast = onDownloadPodcast
                )
            }
        }
    }
}

@Composable
private fun LessonNotesSummaryTab(
    subjects: List<StudySubjectEntity>,
    lessons: List<StudyLessonEntity>,
    flashcards: List<StudyFlashcardEntity>,
    onSaveLesson: (StudyLessonEntity) -> Unit
) {
    val strings = LocalNexaStrings.current
    var search by remember { mutableStateOf("") }
    var selectedSubjId by remember { mutableStateOf<Long?>(null) }
    var editingLesson by remember { mutableStateOf<StudyLessonEntity?>(null) }

    val filtered = remember(lessons, search, selectedSubjId) {
        lessons.filter { l ->
            (selectedSubjId == null || l.subjectId == selectedSubjId) &&
                (search.isBlank() || l.title.contains(search, true) || l.sideNoteText.contains(search, true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
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

        if (subjects.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(selected = selectedSubjId == null, onClick = { selectedSubjId = null }, label = { Text(strings.all) })
                }
                items(subjects, key = { it.id }) { s ->
                    FilterChip(selected = selectedSubjId == s.id, onClick = { selectedSubjId = s.id }, label = { Text(s.name) })
                }
            }
        }

        if (filtered.isEmpty()) {
            NexaEmptyState(
                message = strings.noLessonNotesYet,
                icon = Icons.Rounded.EditNote,
                accent = NexaSectionAccents.summaries
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(filtered, key = { it.id }) { lesson ->
                    val subjName = subjects.firstOrNull { it.id == lesson.subjectId }?.name ?: ""
                    val cardsCount = flashcards.count { it.lessonId == lesson.id }
                    NexaCard(
                        cornerRadius = 20.dp,
                        elevation = 2.dp,
                        onClick = { editingLesson = lesson }
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(lesson.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (subjName.isNotBlank()) {
                                        Text("$subjName • $cardsCount ${strings.flashcards}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                IconButton(onClick = { editingLesson = lesson }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = strings.edit)
                                }
                            }
                            Text(
                                text = lesson.sideNoteText.ifBlank { strings.sideNoteHint },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (lesson.sideNoteText.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    editingLesson?.let { target ->
        var noteDraft by remember(target.id) { mutableStateOf(target.sideNoteText) }
        AlertDialog(
            onDismissRequest = { editingLesson = null },
            title = { Text(target.title) },
            text = {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    label = { Text(strings.sideNoteTitle) },
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onSaveLesson(target.copy(sideNoteText = noteDraft))
                    editingLesson = null
                }) { Text(strings.save) }
            },
            dismissButton = {
                TextButton(onClick = { editingLesson = null }) { Text(strings.cancel) }
            }
        )
    }
}

@Composable
private fun FlashcardsDeckTab(
    subjects: List<StudySubjectEntity>,
    flashcards: List<StudyFlashcardEntity>,
    onRateFlashcard: (StudyFlashcardEntity, Int) -> Unit
) {
    val strings = LocalNexaStrings.current
    var showAnswer by remember { mutableStateOf(false) }
    val dueCards = remember(flashcards) {
        val now = System.currentTimeMillis()
        flashcards.filter { it.nextReviewAt <= now }.ifEmpty { flashcards }
    }
    val currentCard = dueCards.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.Style,
                            tint = NexaSectionAccents.summaries,
                            size = 42.dp,
                            iconSize = 21.dp,
                            cornerRadius = 13.dp
                        )
                        Column {
                            Text(strings.spacedReview, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${flashcards.count { it.nextReviewAt <= System.currentTimeMillis() }} / ${flashcards.size} ${strings.flashcards}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (currentCard != null) {
            item {
                NexaCard(
                    onClick = { showAnswer = !showAnswer },
                    cornerRadius = 22.dp,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val subj = subjects.firstOrNull { it.id == currentCard.subjectId }?.name ?: ""
                        if (subj.isNotBlank()) {
                            Text(subj, style = MaterialTheme.typography.labelMedium, color = NexaSectionAccents.summaries, fontWeight = FontWeight.Bold)
                        }
                        Text(currentCard.front, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (showAnswer) {
                            HorizontalDivider()
                            Text(currentCard.back, style = MaterialTheme.typography.bodyLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { showAnswer = false; onRateFlashcard(currentCard, 1) }) { Text(strings.again) }
                                FilledTonalButton(onClick = { showAnswer = false; onRateFlashcard(currentCard, 2) }) { Text(strings.hard) }
                                Button(onClick = { showAnswer = false; onRateFlashcard(currentCard, 3) }) { Text(strings.good) }
                                FilledTonalButton(onClick = { showAnswer = false; onRateFlashcard(currentCard, 4) }) { Text(strings.easy) }
                            }
                        } else {
                            Text(strings.tapToFlipLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        items(flashcards, key = { it.id }) { card ->
            val subj = subjects.firstOrNull { it.id == card.subjectId }?.name ?: ""
            NexaCard(cornerRadius = 18.dp, elevation = 1.dp) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (subj.isNotBlank()) Text(subj, style = MaterialTheme.typography.labelSmall, color = NexaSectionAccents.summaries, fontWeight = FontWeight.Bold)
                    Text(card.front, fontWeight = FontWeight.Bold)
                    Text(card.back, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SavedQuotesTab(
    quotes: List<SavedQuoteEntity>,
    onSaveCustomQuote: (String, String, String) -> Unit,
    onDeleteQuote: (SavedQuoteEntity) -> Unit
) {
    val strings = LocalNexaStrings.current
    var showAdd by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(strings.savedQuotes, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FilledTonalButton(onClick = { showAdd = true }) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(strings.saveQuote)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(quotes, key = { it.id }) { q ->
                NexaCard(cornerRadius = 18.dp, elevation = 1.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("“${q.quoteText}”", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("${q.bookTitle} • ${q.author}", style = MaterialTheme.typography.labelSmall, color = NexaSectionAccents.summaries, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { onDeleteQuote(q) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var text by remember { mutableStateOf("") }
        var source by remember { mutableStateOf("NEXA") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(strings.saveQuote) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(strings.notes) }, minLines = 2)
                    OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text(strings.title) })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (text.isNotBlank()) onSaveCustomQuote(text, source, source)
                    showAdd = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text(strings.cancel) } }
        )
    }
}
