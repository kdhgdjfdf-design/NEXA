package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.local.AudioTrackEntity
import com.example.domain.i18n.LocalNexaStrings
import com.example.services.LocalAudioUiState

@Composable
fun VinylDiscGraphic(
    isSpinning: Boolean,
    size: Dp = 44.dp,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    var angle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isSpinning) {
        if (!isSpinning) return@LaunchedEffect
        var lastFrameNanos = withFrameNanos { it }
        while (true) {
            val frameNanos = withFrameNanos { it }
            val dtSec = ((frameNanos - lastFrameNanos).coerceIn(0L, 100_000_000L)) / 1_000_000_000f
            lastFrameNanos = frameNanos
            angle = (angle + dtSec * 84f) % 360f
        }
    }

    Canvas(modifier = Modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        rotate(degrees = angle, pivot = center) {
            // Outer matte black vinyl platter with subtle warm rim
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF28221E), Color(0xFF161311), Color(0xFF0D0B0A)),
                    center = center,
                    radius = r
                ),
                radius = r,
                center = center
            )
            drawCircle(
                color = Color(0xFF4B3D34).copy(alpha = 0.55f),
                radius = r - 0.8.dp.toPx(),
                center = center,
                style = Stroke(1.2.dp.toPx())
            )

            // Concentric vinyl grooves
            val grooveFactors = floatArrayOf(0.86f, 0.74f, 0.62f, 0.50f)
            grooveFactors.forEachIndexed { idx, factor ->
                drawCircle(
                    color = if (idx % 2 == 0) Color(0xFF39322E) else Color(0xFF2C2622),
                    radius = r * factor,
                    center = center,
                    style = Stroke(0.9.dp.toPx())
                )
            }

            // Diagonal light sheen sweep (makes rotation clearly visible)
            drawArc(
                color = Color.White.copy(alpha = 0.22f),
                startAngle = -35f,
                sweepAngle = 52f,
                useCenter = false,
                topLeft = Offset(center.x - r * 0.78f, center.y - r * 0.78f),
                size = androidx.compose.ui.geometry.Size(r * 1.56f, r * 1.56f),
                style = Stroke(width = 2.4.dp.toPx())
            )
            drawArc(
                color = Color.White.copy(alpha = 0.14f),
                startAngle = 145f,
                sweepAngle = 44f,
                useCenter = false,
                topLeft = Offset(center.x - r * 0.68f, center.y - r * 0.68f),
                size = androidx.compose.ui.geometry.Size(r * 1.36f, r * 1.36f),
                style = Stroke(width = 2.0.dp.toPx())
            )

            // Center label + subtle golden ring + notch marker
            drawCircle(color = accentColor, radius = r * 0.37f, center = center)
            drawCircle(
                color = Color(0xFFFFF1DC).copy(alpha = 0.55f),
                radius = r * 0.34f,
                center = center,
                style = Stroke(1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFFFF4E3).copy(alpha = 0.85f),
                radius = (r * 0.06f).coerceAtLeast(1.8.dp.toPx()),
                center = Offset(center.x + r * 0.22f, center.y)
            )
            // Center spindle hole
            drawCircle(color = Color(0xFF141110), radius = r * 0.11f, center = center)
        }
    }
}

@Composable
fun MiniVinylBar(
    audioState: LocalAudioUiState,
    onTogglePlay: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onSeek: (Long) -> Unit = {}
) {
    val strings = LocalNexaStrings.current
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.wrapContentWidth().clip(RoundedCornerShape(24.dp)).testTag("mini_vinyl_bar"),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Collapsed state: only the small vinyl disc is visible.
            Box(
                modifier = Modifier.size(54.dp).clip(CircleShape).clickable { expanded = !expanded }.testTag("vinyl_disc_toggle"),
                contentAlignment = Alignment.Center
            ) {
                VinylDiscGraphic(isSpinning = audioState.isPlaying, size = 48.dp)
            }

            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = audioState.currentTrack?.title ?: strings.audioPlayer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPrevious, modifier = Modifier.size(38.dp)) { Icon(Icons.Rounded.SkipPrevious, contentDescription = strings.previousTrack) }
                        FilledIconButton(onClick = onTogglePlay, modifier = Modifier.size(40.dp), shape = CircleShape) {
                            Icon(imageVector = if (audioState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = if (audioState.isPlaying) strings.pauseFocus else strings.playAudioLabel)
                        }
                        IconButton(onClick = onNext, modifier = Modifier.size(38.dp)) { Icon(Icons.Rounded.SkipNext, contentDescription = strings.nextTrack) }
                    }
                    TextButton(onClick = onExpand, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.audioPlayer, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableVinylSheet(
    audioState: LocalAudioUiState,
    tracks: List<AudioTrackEntity>,
    onDismiss: () -> Unit,
    onPlayTrack: (AudioTrackEntity) -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onImportLocalMp3: (title: String, playlist: String, uriString: String) -> Unit,
    onDeleteTrack: (AudioTrackEntity) -> Unit,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val strings = LocalNexaStrings.current
    val context = LocalContext.current
    var selectedPlaylist by remember { mutableStateOf("ALL") }
    var targetImportPlaylist by remember { mutableStateOf("CUSTOM") }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Audio Track"
            onImportLocalMp3(fileName, targetImportPlaylist, uri.toString())
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.audioPlayer, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                FilledTonalButton(onClick = {
                    targetImportPlaylist = if (selectedPlaylist == "ALL") "CUSTOM" else selectedPlaylist
                    audioPicker.launch(arrayOf("audio/*"))
                }) {
                    Icon(Icons.Rounded.AudioFile, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(strings.importLocalAudio)
                }
            }

            // Turntable hero
            NexaCard(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                cornerRadius = 20.dp,
                elevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VinylDiscGraphic(isSpinning = audioState.isPlaying, size = 128.dp)
                    Text(
                        text = audioState.currentTrack?.title ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (audioState.durationMs > 0L) {
                        Slider(
                            value = (audioState.positionMs.toFloat() / audioState.durationMs.toFloat()).coerceIn(0f, 1f),
                            onValueChange = { frac -> onSeek((frac * audioState.durationMs).toLong()) }
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconButton(
                            onClick = onPrevious,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = strings.previousTrack)
                        }
                        FilledIconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = if (audioState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = strings.audioPlayer
                            )
                        }
                        FilledTonalIconButton(
                            onClick = onNext,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = strings.nextTrack)
                        }
                    }
                    Text(
                        text = strings.musicPauseRuleNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Playlist chips
            val tabs = listOf(
                "ALL" to strings.all,
                "QURAN" to strings.playlistQuran,
                "ADHKAR" to strings.playlistAdhkar,
                "CALM" to strings.playlistCalm,
                "CUSTOM" to strings.playlistCustom
            )
            val selectedTabIndex = tabs.indexOfFirst { it.first == selectedPlaylist }.coerceAtLeast(0)
            NexaSegmentedTabs(
                tabs = tabs.map { it.second },
                selectedIndex = selectedTabIndex,
                onSelect = { selectedPlaylist = tabs[it].first }
            )

            val filtered = tracks.filter { selectedPlaylist == "ALL" || it.playlistName == selectedPlaylist }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { track ->
                    val isCurrent = audioState.currentTrack?.id == track.id
                    NexaCard(
                        onClick = { onPlayTrack(track) },
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
                        cornerRadius = 16.dp,
                        elevation = if (isCurrent) 2.dp else 1.dp,
                        borderColor = if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NexaFilledIconBadge(
                                icon = if (isCurrent && audioState.isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote,
                                tint = if (isCurrent) MaterialTheme.colorScheme.primary else NexaSectionAccents.summaries,
                                size = 38.dp,
                                iconSize = 19.dp,
                                cornerRadius = 11.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${track.playlistName} • ${track.artist}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (!track.isBuiltInCalm) {
                                IconButton(onClick = { onDeleteTrack(track) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.delete)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
