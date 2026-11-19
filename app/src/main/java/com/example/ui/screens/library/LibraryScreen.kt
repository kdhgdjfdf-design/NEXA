package com.example.ui.screens.library

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
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
import com.example.ui.components.NexaTintedBanner

@Composable
fun LibraryScreen(
    itemsList: List<LibraryItemEntity>,
    quotes: List<SavedQuoteEntity>,
    podcastEpisodes: List<PodcastEpisodeEntity>,
    podcastState: PodcastUiState,
    podcastController: PodcastPlayerController,
    onUpdateBook: (LibraryItemEntity) -> Unit,
    onImportTextBook: (String, String, String, String) -> Unit,
    onSaveQuote: (LibraryItemEntity, Int, String) -> Unit,
    onDeleteQuote: (SavedQuoteEntity) -> Unit,
    onTogglePodcastFav: (PodcastEpisodeEntity) -> Unit,
    onDownloadPodcast: (PodcastEpisodeEntity, (Boolean) -> Unit) -> Unit
) {
    val strings = LocalNexaStrings.current
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0=Books, 1=Novels, 2=Podcasts, 3=Favorites
    var selectedShelf by remember { mutableStateOf("ALL") }
    var readingBook by remember { mutableStateOf<LibraryItemEntity?>(null) }

    val syncedBook = readingBook?.let { b -> itemsList.firstOrNull { it.id == b.id } ?: b }
    if (syncedBook != null) {
        BookReaderScreen(
            book = syncedBook,
            onBack = { readingBook = null },
            onUpdatePage = { page ->
                onUpdateBook(
                    syncedBook.copy(
                        currentPage = page,
                        lastReadAt = System.currentTimeMillis(),
                        shelf = if (syncedBook.shelf == "WANT_TO_READ") "READING" else syncedBook.shelf
                    )
                )
            },
            onChangeShelf = { shelf, custom ->
                onUpdateBook(syncedBook.copy(shelf = shelf, customShelfName = custom))
            },
            onToggleFavorite = {
                onUpdateBook(syncedBook.copy(isFavorite = !syncedBook.isFavorite))
            },
            onSaveQuote = { page, text -> onSaveQuote(syncedBook, page, text) }
        )
        return
    }

    val txtImporter = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val content = readTextFromUri(context, uri)
            if (content.isNotBlank()) {
                val title = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                    ?.takeIf { it.isNotBlank() } ?: strings.importedBookDefault
                onImportTextBook(title, strings.localImportDefault, if (activeTab == 1) "NOVEL" else "BOOK", content)
            }
        }
    }

    val accent = NexaSectionAccents.summaries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("library_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(2.dp))
        NexaScreenTitleRow(
            title = strings.library,
            subtitle = "${itemsList.size} • ${podcastEpisodes.size} ${strings.podcasts}",
            icon = Icons.Rounded.AutoStories,
            accent = accent,
            trailing = if (activeTab == 0 || activeTab == 1) {
                {
                    FilledTonalButton(
                        onClick = { txtImporter.launch(arrayOf("text/plain")) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                    ) {
                        Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.importBook, maxLines = 1)
                    }
                }
            } else null
        )

        val tabs = listOf(strings.books, strings.novels, strings.podcasts, strings.favorites)
        NexaSegmentedTabs(
            tabs = tabs,
            selectedIndex = activeTab,
            onSelect = { activeTab = it },
            testTagPrefix = "library_tab"
        )

        when (activeTab) {
            0, 1 -> {
                val cat = if (activeTab == 0) "BOOK" else "NOVEL"
                val shelves = listOf(
                    "ALL" to strings.all,
                    "READING" to strings.shelfReading,
                    "COMPLETED" to strings.shelfCompleted,
                    "WANT_TO_READ" to strings.shelfWantToRead
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(shelves) { (k, lbl) ->
                        FilterChip(
                            selected = selectedShelf == k,
                            onClick = { selectedShelf = k },
                            label = { Text(lbl) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                val books = itemsList.filter { it.category == cat && (selectedShelf == "ALL" || it.shelf == selectedShelf) }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        NexaCard(cornerRadius = 20.dp, elevation = 2.dp) {
                            Image(
                                painter = painterResource(id = R.drawable.img_library_cover),
                                contentDescription = strings.library,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(128.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (books.isEmpty()) {
                        item {
                            NexaEmptyState(
                                message = strings.importBook,
                                icon = Icons.Rounded.MenuBook,
                                accent = accent
                            )
                        }
                    } else {
                        items(books, key = { it.id }) { item ->
                            NexaCard(
                                onClick = { readingBook = item },
                                modifier = Modifier.testTag("library_book_${item.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    NexaFilledIconBadge(
                                        icon = Icons.Rounded.MenuBook,
                                        tint = accent,
                                        size = 44.dp,
                                        iconSize = 22.dp,
                                        cornerRadius = 13.dp
                                    )
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(item.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "${item.author} • ${strings.pagePrefix} ${item.currentPage + 1}/${item.totalPages}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onUpdateBook(item.copy(isFavorite = !item.isFavorite)) }) {
                                        Icon(
                                            if (item.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                            contentDescription = strings.favorites,
                                            tint = if (item.isFavorite) accent else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (quotes.isNotEmpty()) {
                        item {
                            Text(
                                strings.savedQuotes,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        items(quotes, key = { it.id }) { q ->
                            NexaCard(cornerRadius = 18.dp, elevation = 1.dp) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    NexaFilledIconBadge(
                                        icon = Icons.Rounded.FormatQuote,
                                        tint = accent.copy(alpha = 0.85f),
                                        size = 36.dp,
                                        iconSize = 18.dp,
                                        cornerRadius = 10.dp
                                    )
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text("“${q.quoteText}”", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${q.bookTitle} • ${strings.pagePrefix} ${q.pageNumber + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { onDeleteQuote(q) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> PodcastsTab(
                episodes = podcastEpisodes,
                podcastState = podcastState,
                controller = podcastController,
                onToggleFav = onTogglePodcastFav,
                onDownload = onDownloadPodcast
            )

            3 -> {
                val favBooks = itemsList.filter { it.isFavorite }
                val favPods = podcastEpisodes.filter { it.isFavorite }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    if (favBooks.isEmpty() && favPods.isEmpty()) {
                        item {
                            NexaEmptyState(
                                message = strings.favorites,
                                icon = Icons.Rounded.FavoriteBorder,
                                accent = accent
                            )
                        }
                    }
                    items(favBooks, key = { "b_${it.id}" }) { b ->
                        NexaCard(onClick = { readingBook = b }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                NexaFilledIconBadge(
                                    icon = Icons.Rounded.Book,
                                    tint = accent,
                                    size = 42.dp,
                                    iconSize = 20.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(b.title, fontWeight = FontWeight.Bold)
                                    Text(b.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    items(favPods, key = { "p_${it.id}" }) { ep ->
                        NexaCard(onClick = { podcastController.playEpisode(ep) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                NexaFilledIconBadge(
                                    icon = Icons.Rounded.Podcasts,
                                    tint = NexaSectionAccents.focus,
                                    size = 42.dp,
                                    iconSize = 20.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ep.title, fontWeight = FontWeight.Bold)
                                    Text(ep.showTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodcastsTab(
    episodes: List<PodcastEpisodeEntity>,
    podcastState: PodcastUiState,
    controller: PodcastPlayerController,
    onToggleFav: (PodcastEpisodeEntity) -> Unit,
    onDownload: (PodcastEpisodeEntity, (Boolean) -> Unit) -> Unit
) {
    val strings = LocalNexaStrings.current
    var sectionFilter by remember { mutableStateOf("ALL") } // ALL, HISTORY, DISCOVER
    var downloadingId by remember { mutableStateOf<Long?>(null) }
    val accent = NexaSectionAccents.summaries

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            NexaTintedBanner(
                text = strings.podcastBgRuleNote,
                icon = Icons.Rounded.Podcasts,
                accent = accent
            )
        }

        if (podcastState.currentEpisode != null) {
            item {
                val ep = podcastState.currentEpisode!!
                NexaCard(
                    borderColor = accent.copy(alpha = 0.45f),
                    elevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NexaFilledIconBadge(
                                icon = Icons.Rounded.GraphicEq,
                                tint = accent,
                                size = 44.dp,
                                iconSize = 22.dp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ep.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(ep.showTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (podcastState.durationMs > 0L) {
                            Slider(
                                value = (podcastState.positionMs.toFloat() / podcastState.durationMs.toFloat()).coerceIn(0f, 1f),
                                onValueChange = { controller.seekTo((it * podcastState.durationMs).toLong()) }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { controller.togglePlayPause() }) {
                                Icon(
                                    if (podcastState.isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                                    contentDescription = if (podcastState.isPlaying) strings.pauseFocus else strings.playAudioLabel,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                                    FilterChip(
                                        selected = podcastState.playbackSpeed == spd,
                                        onClick = { controller.setSpeed(spd) },
                                        label = { Text("${spd}x") }
                                    )
                                }
                            }
                            TextButton(onClick = {
                                val nextSleep = if (podcastState.sleepTimerRemainingMinutes == 0) 15 else 0
                                controller.setSleepTimer(nextSleep)
                            }) {
                                Text(
                                    if (podcastState.sleepTimerRemainingMinutes > 0) "${podcastState.sleepTimerRemainingMinutes}${strings.minutesUnit}" else strings.sleepTimer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = sectionFilter == "ALL", onClick = { sectionFilter = "ALL" }, label = { Text(strings.all) })
                FilterChip(selected = sectionFilter == "HISTORY", onClick = { sectionFilter = "HISTORY" }, label = { Text(strings.listeningHistory) })
                FilterChip(selected = sectionFilter == "DISCOVER", onClick = { sectionFilter = "DISCOVER" }, label = { Text(strings.discover) })
            }
        }

        val list = when (sectionFilter) {
            "HISTORY" -> episodes.filter { it.lastPlayedAt != null }
            "DISCOVER" -> episodes.sortedBy { it.id }
            else -> episodes
        }
        items(list, key = { it.id }) { ep ->
            NexaCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NexaFilledIconBadge(
                            icon = Icons.Rounded.Podcasts,
                            tint = accent,
                            size = 42.dp,
                            iconSize = 20.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ep.title, fontWeight = FontWeight.Bold)
                            Text("${ep.showTitle} • ${ep.showAuthor}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onToggleFav(ep) }) {
                            Icon(
                                if (ep.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = strings.favorites,
                                tint = if (ep.isFavorite) accent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(ep.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { controller.playEpisode(ep) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(if (ep.positionMs > 0L) strings.resumeAudioLabel else strings.playAudioLabel)
                        }
                        if (ep.isDownloaded) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accent.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    strings.downloaded,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accent,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    downloadingId = ep.id
                                    onDownload(ep) { downloadingId = null }
                                },
                                enabled = downloadingId != ep.id,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (downloadingId == ep.id) "..." else strings.downloadOffline)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun readTextFromUri(context: Context, uri: Uri): String {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
    }.getOrDefault("")
}
