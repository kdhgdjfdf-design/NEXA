package com.example.services

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.example.MainActivity
import com.example.data.local.NexaDatabase
import com.example.data.local.PodcastEpisodeEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Independent Media3 MediaSessionService for Podcasts ONLY.
 * Continues playing in the background & when the phone screen is locked,
 * with lock-screen & notification controls.
 */
class PodcastPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        val launchIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(launchIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}

data class PodcastUiState(
    val currentEpisode: PodcastEpisodeEntity? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerRemainingMinutes: Int = 0
)

class PodcastPlayerController(
    private val context: Context,
    private val db: NexaDatabase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var controller: MediaController? = null
    private var tickerJob: Job? = null
    private var sleepJob: Job? = null

    private val _uiState = MutableStateFlow(PodcastUiState())
    val uiState: StateFlow<PodcastUiState> = _uiState.asStateFlow()

    private suspend fun getOrConnectController(): MediaController? {
        controller?.let { if (it.isConnected) return it }
        return suspendCancellableCoroutine { cont ->
            val token = SessionToken(context, ComponentName(context, PodcastPlaybackService::class.java))
            val future = MediaController.Builder(context, token).buildAsync()
            future.addListener({
                val ctrl = runCatching { future.get() }.getOrNull()
                if (ctrl != null) {
                    ctrl.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _uiState.update { it.copy(isPlaying = isPlaying) }
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_READY) {
                                _uiState.update { it.copy(durationMs = ctrl.duration.coerceAtLeast(0L)) }
                            } else if (playbackState == Player.STATE_ENDED) {
                                persistEpisodePosition(0L)
                                _uiState.update { it.copy(isPlaying = false, positionMs = 0L) }
                            }
                        }
                    })
                    controller = ctrl
                }
                if (cont.isActive) cont.resume(ctrl) {}
            }, ContextCompat.getMainExecutor(context))
        }
    }

    fun playEpisode(episode: PodcastEpisodeEntity, speed: Float = _uiState.value.playbackSpeed) {
        scope.launch {
            val ctrl = getOrConnectController() ?: return@launch
            val uri = if (episode.isDownloaded && episode.localFileUri.isNotBlank()) {
                episode.localFileUri
            } else {
                episode.audioUrl
            }
            val item = MediaItem.Builder()
                .setMediaId(episode.id.toString())
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(episode.title)
                        .setArtist(episode.showTitle)
                        .build()
                )
                .build()

            ctrl.setMediaItem(item)
            ctrl.setPlaybackSpeed(speed)
            ctrl.prepare()
            if (episode.positionMs > 0L) {
                ctrl.seekTo(episode.positionMs)
            }
            ctrl.play()

            val updated = episode.copy(lastPlayedAt = System.currentTimeMillis())
            withContext(Dispatchers.IO) { db.libraryDao().updatePodcastEpisode(updated) }
            _uiState.update {
                it.copy(
                    currentEpisode = updated,
                    isPlaying = true,
                    positionMs = episode.positionMs,
                    durationMs = episode.durationMs,
                    playbackSpeed = speed
                )
            }
            startTicker()
        }
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) {
            ctrl.pause()
            persistEpisodePosition(ctrl.currentPosition)
        } else {
            ctrl.play()
            startTicker()
        }
    }

    fun seekTo(posMs: Long) {
        controller?.seekTo(posMs.coerceAtLeast(0L))
        _uiState.update { it.copy(positionMs = posMs.coerceAtLeast(0L)) }
    }

    fun setSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun setSleepTimer(minutes: Int) {
        sleepJob?.cancel()
        if (minutes <= 0) {
            _uiState.update { it.copy(sleepTimerRemainingMinutes = 0) }
            return
        }
        _uiState.update { it.copy(sleepTimerRemainingMinutes = minutes) }
        sleepJob = scope.launch {
            var rem = minutes
            while (rem > 0 && isActive) {
                delay(60_000L)
                rem--
                _uiState.update { it.copy(sleepTimerRemainingMinutes = rem) }
            }
            controller?.pause()
            persistEpisodePosition(controller?.currentPosition ?: 0L)
            _uiState.update { it.copy(isPlaying = false, sleepTimerRemainingMinutes = 0) }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            var tickCount = 0
            while (isActive) {
                val ctrl = controller
                if (ctrl != null && ctrl.isPlaying) {
                    val pos = ctrl.currentPosition.coerceAtLeast(0L)
                    val dur = ctrl.duration.coerceAtLeast(_uiState.value.durationMs)
                    _uiState.update { it.copy(positionMs = pos, durationMs = dur, isPlaying = true) }
                    tickCount++
                    if (tickCount % 6 == 0) persistEpisodePosition(pos)
                }
                delay(1000L)
            }
        }
    }

    private fun persistEpisodePosition(posMs: Long) {
        val ep = _uiState.value.currentEpisode ?: return
        val updated = ep.copy(positionMs = posMs, lastPlayedAt = System.currentTimeMillis())
        _uiState.update { it.copy(currentEpisode = updated) }
        scope.launch(Dispatchers.IO) {
            db.libraryDao().updatePodcastEpisode(updated)
        }
    }
}
