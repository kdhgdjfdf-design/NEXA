package com.example.ui.screens.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LibraryItemEntity
import com.example.domain.i18n.LocalNexaStrings
import kotlinx.coroutines.launch

@Composable
fun BookReaderScreen(
    book: LibraryItemEntity,
    onBack: () -> Unit,
    onUpdatePage: (Int) -> Unit,
    onChangeShelf: (String, String) -> Unit,
    onToggleFavorite: () -> Unit,
    onSaveQuote: (Int, String) -> Unit
) {
    BackHandler { onBack() }
    val strings = LocalNexaStrings.current
    val scope = rememberCoroutineScope()

    var fontSizeSp by remember { mutableIntStateOf(18) }
    var paperTheme by remember { mutableStateOf("SEPIA") } // WHITE, SEPIA, SAGE, NIGHT
    var showQuoteDialog by remember { mutableStateOf(false) }

    val pages = remember(book.contentText, fontSizeSp) {
        paginateText(book.contentText, charsPerPage = (1100 - (fontSizeSp - 14) * 45).coerceIn(450, 1200))
    }
    val pagerState = rememberPagerState(
        initialPage = book.currentPage.coerceIn(0, (pages.size - 1).coerceAtLeast(0)),
        pageCount = { pages.size.coerceAtLeast(1) }
    )

    LaunchedEffect(pagerState.currentPage) {
        onUpdatePage(pagerState.currentPage)
    }

    val (bgColor, textColor, barBg) = when (paperTheme) {
        "WHITE" -> Triple(Color(0xFFFFFFFF), Color(0xFF171310), Color(0xFFF6F2EC))
        "SAGE" -> Triple(Color(0xFFEDF4EE), Color(0xFF17261E), Color(0xFFE0ECE2))
        "NIGHT" -> Triple(Color(0xFF120E0C), Color(0xFFEAE2D8), Color(0xFF1E1815))
        else -> Triple(Color(0xFFF7EFE5), Color(0xFF231C16), Color(0xFFEFE6DC))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("book_reader_screen")
    ) {
        Surface(
            color = barBg,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reader_back_btn")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.back, tint = textColor)
                    }
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { showQuoteDialog = true }) {
                        Icon(Icons.Rounded.FormatQuote, contentDescription = strings.saveQuote, tint = textColor)
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (book.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = strings.favorites,
                            tint = textColor
                        )
                    }
                }

                // Paper & Font Controls + Shelf switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("WHITE" to "W", "SEPIA" to "S", "SAGE" to "G", "NIGHT" to "N").forEach { (k, lbl) ->
                            FilterChip(
                                selected = paperTheme == k,
                                onClick = { paperTheme = k },
                                label = { Text(lbl) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { fontSizeSp = (fontSizeSp - 2).coerceAtLeast(14) }) {
                            Text("A-", color = textColor, fontWeight = FontWeight.Bold)
                        }
                        Text("$fontSizeSp", color = textColor, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { fontSizeSp = (fontSizeSp + 2).coerceAtMost(28) }) {
                            Text("A+", color = textColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "READING" to strings.shelfReading,
                        "COMPLETED" to strings.shelfCompleted,
                        "WANT_TO_READ" to strings.shelfWantToRead
                    ).forEach { (s, lbl) ->
                        FilterChip(
                            selected = book.shelf == s,
                            onClick = { onChangeShelf(s, "") },
                            label = { Text(lbl) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) { pageIdx ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = pages.getOrElse(pageIdx) { "" },
                    color = textColor,
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.65f).sp
                )
            }
        }

        Surface(
            color = barBg,
            border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) } },
                    enabled = pagerState.currentPage > 0
                ) { Text("<", color = textColor, fontWeight = FontWeight.Bold) }
                Text(
                    "${strings.pagePrefix} ${pagerState.currentPage + 1} / ${pages.size}",
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(pages.size - 1)) } },
                    enabled = pagerState.currentPage < pages.size - 1
                ) { Text(">", color = textColor, fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (showQuoteDialog) {
        val currentPageText = pages.getOrElse(pagerState.currentPage) { "" }
        var quoteInput by remember { mutableStateOf(currentPageText.take(180)) }
        AlertDialog(
            onDismissRequest = { showQuoteDialog = false },
            title = { Text(strings.saveQuote) },
            text = {
                OutlinedTextField(
                    value = quoteInput,
                    onValueChange = { quoteInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(onClick = {
                    onSaveQuote(pagerState.currentPage, quoteInput)
                    showQuoteDialog = false
                }) { Text(strings.save) }
            },
            dismissButton = { TextButton(onClick = { showQuoteDialog = false }) { Text(strings.cancel) } }
        )
    }
}

private fun paginateText(text: String, charsPerPage: Int): List<String> {
    if (text.isBlank()) return listOf("")
    val words = text.split(Regex("(?<=\\s)"))
    val pages = mutableListOf<String>()
    val sb = StringBuilder()
    for (w in words) {
        if (sb.length + w.length > charsPerPage && sb.isNotEmpty()) {
            pages += sb.toString().trim()
            sb.clear()
        }
        sb.append(w)
    }
    if (sb.isNotEmpty()) pages += sb.toString().trim()
    return pages.ifEmpty { listOf(text) }
}
