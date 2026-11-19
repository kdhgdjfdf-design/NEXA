package com.example.services

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.local.AudioDao
import com.example.data.local.AudioTrackEntity
import com.example.data.local.NexaPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LocalAudioUiState(
    val currentTrack: AudioTrackEntity? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val pausedByLifecycle: Boolean = false
)

/**
 * Handles Music, Quran recitations, Adhkar, and Calm tracks.
 * Strictly obeys NEXA Section 10:
 * - Stops immediately when leaving app / locking screen (no background playback).
 * - Saves exact position and resumes from the same position upon returning.
 */
class LocalAudioPlayerController(
    private val context: Context,
    private val audioDao: AudioDao,
    private val prefs: NexaPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var exoPlayer: ExoPlayer? = null
    private val ambientEngine = AmbientSoundEngine(context)
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(LocalAudioUiState())
    val uiState: StateFlow<LocalAudioUiState> = _uiState.asStateFlow()

    private fun ensurePlayer(): ExoPlayer {
        return exoPlayer ?: ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (_uiState.value.currentTrack?.isBuiltInCalm != true) {
                        _uiState.update { it.copy(isPlaying = isPlaying) }
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        val dur = duration.coerceAtLeast(0L)
                        _uiState.update { it.copy(durationMs = dur) }
                    } else if (playbackState == Player.STATE_ENDED) {
                        _uiState.update { it.copy(isPlaying = false, positionMs = 0L) }
                    }
                }
            })
            exoPlayer = this
        }
    }

    fun playTrack(track: AudioTrackEntity) {
        _uiState.update {
            it.copy(
                currentTrack = track,
                positionMs = track.lastPositionMs,
                durationMs = track.durationMs,
                pausedByLifecycle = false
            )
        }

        if (track.isBuiltInCalm || track.uriString.startsWith("builtin://")) {
            exoPlayer?.pause()
            val mode = when {
                track.uriString.contains("rain") -> "RAIN"
                track.uriString.contains("warm_noise") -> "WARM_NOISE"
                else -> "CHIME"
            }
            ambientEngine.start(mode)
            _uiState.update { it.copy(isPlaying = true, durationMs = track.durationMs) }
            startProgressTicker()
            return
        }

        ambientEngine.stop()
        val player = ensurePlayer()
        runCatching {
            player.setMediaItem(MediaItem.fromUri(Uri.parse(track.uriString)))
            player.prepare()
            if (track.lastPositionMs > 0L) {
                player.seekTo(track.lastPositionMs)
            }
            player.play()
            startProgressTicker()
        }
    }

    fun togglePlayPause() {
        val current = _uiState.value.currentTrack ?: return
        if (_uiState.value.isPlaying) {
            pauseAndSave(byLifecycle = false)
        } else {
            resumeCurrent()
        }
    }

    fun nextTrack(tracks: List<AudioTrackEntity>) {
        if (tracks.isEmpty()) return
        val current = _uiState.value.currentTrack
        val idx = tracks.indexOfFirst { it.id == current?.id }
        val next = if (idx < 0) tracks.first() else tracks[(idx + 1) % tracks.size]
        playTrack(next)
    }

    fun previousTrack(tracks: List<AudioTrackEntity>) {
        if (tracks.isEmpty()) return
        val current = _uiState.value.currentTrack
        val idx = tracks.indexOfFirst { it.id == current?.id }
        val prev = if (idx <= 0) tracks.last() else tracks[idx - 1]
        playTrack(prev)
    }

    fun resumeCurrent() {
        val track = _uiState.value.currentTrack ?: return
        if (track.isBuiltInCalm || track.uriString.startsWith("builtin://")) {
            val mode = when {
                track.uriString.contains("rain") -> "RAIN"
                track.uriString.contains("warm_noise") -> "WARM_NOISE"
                else -> "CHIME"
            }
            ambientEngine.start(mode)
            _uiState.update { it.copy(isPlaying = true, pausedByLifecycle = false) }
            startProgressTicker()
        } else {
            val player = ensurePlayer()
            if (player.currentMediaItem == null) {
                playTrack(track.copy(lastPositionMs = _uiState.value.positionMs))
            } else {
                player.seekTo(_uiState.value.positionMs)
                player.play()
                _uiState.update { it.copy(isPlaying = true, pausedByLifecycle = false) }
                startProgressTicker()
            }
        }
    }

    fun seekTo(posMs: Long) {
        val clamped = posMs.coerceAtLeast(0L)
        _uiState.update { it.copy(positionMs = clamped) }
        if (_uiState.value.currentTrack?.isBuiltInCalm != true) {
            exoPlayer?.seekTo(clamped)
        }
    }

    fun onAppBackgrounded() {
        if (_uiState.value.isPlaying) {
            pauseAndSave(byLifecycle = true)
        }
    }

    fun onAppForegrounded() {
        if (_uiState.value.pausedByLifecycle && _uiState.value.currentTrack != null) {
            resumeCurrent()
        }
    }

    fun pauseAndSave(byLifecycle: Boolean = false) {
        val track = _uiState.value.currentTrack ?: return
        val pos = if (track.isBuiltInCalm) {
            _uiState.value.positionMs
        } else {
            exoPlayer?.currentPosition ?: _uiState.value.positionMs
        }
        ambientEngine.stop()
        exoPlayer?.pause()
        progressJob?.cancel()
        _uiState.update {
            it.copy(
                isPlaying = false,
                positionMs = pos,
                pausedByLifecycle = byLifecycle
            )
        }
        scope.launch(Dispatchers.IO) {
            audioDao.updateAudioTrack(track.copy(lastPositionMs = pos))
            prefs.saveAudioPlaybackState(track.id, pos)
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _uiState.value.isPlaying) {
                val track = _uiState.value.currentTrack
                val pos = if (track?.isBuiltInCalm == true) {
                    (_uiState.value.positionMs + 500L) % (track.durationMs.coerceAtLeast(1L))
                } else {
                    exoPlayer?.currentPosition ?: _uiState.value.positionMs
                }
                val dur = if (track?.isBuiltInCalm == true) track.durationMs else (exoPlayer?.duration ?: 0L).coerceAtLeast(0L)
                _uiState.update { it.copy(positionMs = pos, durationMs = dur) }
                delay(500L)
            }
        }
    }

    fun release() {
        progressJob?.cancel()
        ambientEngine.stop()
        exoPlayer?.release()
        exoPlayer = null
    }
}
