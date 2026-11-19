package com.example.services

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Plays optional built-in procedural calm ambient sounds (Rain, Warm Noise, Chime)
 * via Android AudioTrack, or a user-selected local audio file via MediaPlayer.
 */
class AmbientSoundEngine(private val context: Context) {
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playJob: Job? = null

    fun start(soundType: String, localUri: String = "") {
        stop()
        if (soundType == "NONE") return
        if (soundType == "LOCAL_FILE" && localUri.isNotBlank()) {
            runCatching {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(context, Uri.parse(localUri))
                    isLooping = true
                    prepare()
                    start()
                }
            }
            return
        }

        val sampleRate = 22050
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.play()

        playJob = scope.launch {
            val buffer = ShortArray(minBuf / 2)
            var lastOut = 0.0
            var phase = 0L
            while (isActive) {
                for (i in buffer.indices) {
                    val sample = when (soundType) {
                        "RAIN" -> {
                            val white = Random.nextDouble(-1.0, 1.0)
                            lastOut = (lastOut + 0.04 * white) / 1.04
                            val droplet = if (Random.nextFloat() < 0.0005f) Random.nextDouble(-0.25, 0.25) else 0.0
                            ((lastOut * 1.4 + droplet) * 9000.0).toInt().coerceIn(-32000, 32000)
                        }
                        "WARM_NOISE" -> {
                            val white = Random.nextDouble(-1.0, 1.0)
                            lastOut = (lastOut + 0.02 * white) / 1.02
                            (lastOut * 14000.0).toInt().coerceIn(-32000, 32000)
                        }
                        "CHIME" -> {
                            val t = (phase % (sampleRate * 6)).toDouble() / sampleRate.toDouble()
                            val env = exp(-t * 1.1)
                            val tone = sin(2.0 * PI * 432.0 * t) * 0.6 + sin(2.0 * PI * 648.0 * t) * 0.25
                            (tone * env * 9000.0).toInt().coerceIn(-32000, 32000)
                        }
                        else -> 0
                    }
                    buffer[i] = sample.toShort()
                    phase++
                }
                runCatching { track.write(buffer, 0, buffer.size) }
            }
        }
    }

    fun stop() {
        playJob?.cancel()
        playJob = null
        runCatching {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        }
        audioTrack = null
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
    }
}
