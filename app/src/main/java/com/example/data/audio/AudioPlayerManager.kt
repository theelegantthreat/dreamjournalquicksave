package com.example.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentFilePath: String? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun playAudio(filePath: String) {
        try {
            if (currentFilePath == filePath && mediaPlayer != null) {
                if (!_isPlaying.value) {
                    mediaPlayer?.start()
                    _isPlaying.value = true
                    startProgressTracker()
                }
                return
            }

            stopAudio()

            val file = File(filePath)
            if (!file.exists()) {
                Log.w("AudioPlayer", "Audio file does not exist: $filePath")
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(file))
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                    stopProgressTracker()
                }
                start()
            }

            currentFilePath = filePath
            _isPlaying.value = true
            _durationMs.value = mediaPlayer?.duration?.toLong() ?: 0L
            startProgressTracker()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio", e)
            stopAudio()
        }
    }

    fun pauseAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
                stopProgressTracker()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error pausing audio", e)
        }
    }

    fun togglePlayback(filePath: String) {
        if (_isPlaying.value && currentFilePath == filePath) {
            pauseAudio()
        } else {
            playAudio(filePath)
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            _currentPositionMs.value = positionMs
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error seeking audio", e)
        }
    }

    fun stopAudio() {
        stopProgressTracker()
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error stopping audio", e)
        } finally {
            mediaPlayer = null
            currentFilePath = null
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        _currentPositionMs.value = it.currentPosition.toLong()
                        _durationMs.value = it.duration.toLong()
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopAudio()
    }
}
