package com.example.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
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

class AudioRecorderManager(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingStartTime = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun startRecording(): Result<File> {
        try {
            stopRecording() // Clean up any active session

            val outputDir = File(context.filesDir, "dream_recordings")
            if (!outputDir.exists()) outputDir.mkdirs()

            val file = File(outputDir, "dream_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            _isRecording.value = true
            _recordingDurationMs.value = 0L

            timerJob = scope.launch {
                while (isActive && _isRecording.value) {
                    val elapsed = System.currentTimeMillis() - recordingStartTime
                    _recordingDurationMs.value = elapsed

                    // Sample amplitude for real-time waveform visualizer
                    try {
                        val maxAmp = recorder?.maxAmplitude ?: 0
                        val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                        _amplitude.value = normalized
                    } catch (e: Exception) {
                        _amplitude.value = 0.1f
                    }

                    delay(60)
                }
            }

            return Result.success(file)
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            cleanUp()
            return Result.failure(e)
        }
    }

    fun stopRecording(): File? {
        timerJob?.cancel()
        timerJob = null
        _isRecording.value = false
        _amplitude.value = 0f

        val recorded = currentOutputFile
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recorder", e)
        } finally {
            recorder = null
        }
        return recorded
    }

    fun cancelRecording() {
        val file = stopRecording()
        file?.let {
            if (it.exists()) it.delete()
        }
        currentOutputFile = null
        _recordingDurationMs.value = 0L
    }

    private fun cleanUp() {
        timerJob?.cancel()
        timerJob = null
        _isRecording.value = false
        _amplitude.value = 0f
        try {
            recorder?.release()
        } catch (ignored: Exception) {}
        recorder = null
    }
}
