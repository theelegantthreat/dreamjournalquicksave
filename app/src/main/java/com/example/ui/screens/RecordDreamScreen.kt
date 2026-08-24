package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.theme.SleekCanvas
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekLilac
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DreamViewModel
import java.util.Locale

@Composable
fun RecordDreamScreen(
    viewModel: DreamViewModel,
    onBack: () -> Unit,
    onDreamCreated: (Long) -> Unit
) {
    val context = LocalContext.current
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDurationMs by viewModel.recordingDurationMs.collectAsState()
    val recordingAmplitude by viewModel.recordingAmplitude.collectAsState()
    val isTranscribing by viewModel.isTranscribing.collectAsState()
    val isProcessingDream by viewModel.isProcessingDream.collectAsState()
    val processingStep by viewModel.processingStep.collectAsState()
    val isAudioPlaying by viewModel.isPlayingAudio.collectAsState()

    var dreamTitle by remember { mutableStateOf("") }
    var dreamTranscript by remember { mutableStateOf("") }
    var selectedImageSize by remember { mutableStateOf("1K") } // Affordance for 1K, 2K, 4K
    var selectedSurrealistStyle by remember { mutableStateOf("Masterwork Surrealism") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var hasRecordedAudio by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf("Peaceful") }
    var customMoodText by remember { mutableStateOf("") }
    var isCustomMoodActive by remember { mutableStateOf(false) }

    val effectiveWakingMood = remember(selectedMood, customMoodText, isCustomMoodActive) {
        if (isCustomMoodActive && customMoodText.isNotBlank()) {
            customMoodText.trim()
        } else {
            selectedMood
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        } else {
            Toast.makeText(context, "Microphone permission is needed to record your waking dream", Toast.LENGTH_LONG).show()
        }
    }

    val minutes = (recordingDurationMs / 1000) / 60
    val seconds = (recordingDurationMs / 1000) % 60
    val formattedDuration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Scaffold(
        containerColor = SleekCanvas,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isRecording) viewModel.cancelVoiceRecording()
                        onBack()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(SleekWhite, CircleShape)
                        .border(1.dp, SleekCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Capture Awakening Dream",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Record immediately while impressions remain fresh",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Hero Voice Recording Pod
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        if (isRecording) Color(0xFFBA1A1A) else SleekCardBorder,
                        RoundedCornerShape(24.dp)
                    ),
                color = SleekWhite,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NightlightRound,
                                contentDescription = null,
                                tint = if (isRecording) Color(0xFFBA1A1A) else SleekPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRecording) "RECORDING WAKING MEMORY" else "VOICE JOURNAL RECORDER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isRecording) Color(0xFFBA1A1A) else SleekPrimary
                            )
                        }

                        Text(
                            text = formattedDuration,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = if (isRecording) Color(0xFFBA1A1A) else TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Waveform Display
                    AudioWaveformVisualizer(
                        isRecording = isRecording,
                        amplitude = recordingAmplitude
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Big Central Record Button
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isRecording) {
                            // Stop & Transcribe button
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFBA1A1A))
                                    .clickable {
                                        val recordedFile = viewModel.stopVoiceRecording()
                                        if (recordedFile != null) {
                                            hasRecordedAudio = true
                                            viewModel.transcribeRecordedAudio(recordedFile) { res ->
                                                if (res != null) {
                                                    dreamTitle = res.title
                                                    dreamTranscript = res.transcript
                                                }
                                            }
                                        }
                                    }
                                    .testTag("stop_recording_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop and Transcribe",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        } else {
                            // Start Record Button
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary)
                                    .clickable {
                                        val hasPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasPerm) {
                                            viewModel.startVoiceRecording()
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                    .testTag("start_recording_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Start Voice Recording",
                                    tint = SleekWhite,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isRecording) "Tap red button when finished recounting" else "Tap microphone to speak your dream thoughts",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Audio Playback Preview if recording completed
                    if (hasRecordedAudio && !isRecording && viewModel.currentAudioFilePath != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SleekSurfaceVariant)
                                .border(1.dp, SleekCardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.toggleAudioPlayback(viewModel.currentAudioFilePath!!)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play voice recording",
                                tint = SleekPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAudioPlaying) "Playing Waking Voice Note" else "Listen to Recorded Voice Note",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AI Transcription Status or manual entry
            if (isTranscribing) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, SleekPrimary.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
                    color = SleekWhite,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = SleekPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Transcribing Awakening Audio...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SleekPrimary
                            )
                            Text(
                                text = "Using Gemini to transcribe words & extract symbolic nuances",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Waking Mood Tracker Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp))
                    .testTag("waking_mood_section"),
                color = SleekWhite,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Waking Mood",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "State of consciousness upon awakening",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekPrimaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = effectiveWakingMood,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SleekOnPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val predefinedMoods = listOf("Peaceful", "Anxious", "Confused", "Inspired", "Awe", "Melancholic", "Energized")

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            predefinedMoods.take(4).forEach { mood ->
                                val isSelected = !isCustomMoodActive && selectedMood == mood
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) SleekPrimary else SleekSurfaceVariant)
                                        .border(
                                            1.dp,
                                            if (isSelected) SleekPrimary else SleekCardBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedMood = mood
                                            isCustomMoodActive = false
                                        }
                                        .padding(vertical = 8.dp)
                                        .testTag("mood_chip_$mood"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mood,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) SleekWhite else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            predefinedMoods.drop(4).forEach { mood ->
                                val isSelected = !isCustomMoodActive && selectedMood == mood
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) SleekPrimary else SleekSurfaceVariant)
                                        .border(
                                            1.dp,
                                            if (isSelected) SleekPrimary else SleekCardBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedMood = mood
                                            isCustomMoodActive = false
                                        }
                                        .padding(vertical = 8.dp)
                                        .testTag("mood_chip_$mood"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mood,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) SleekWhite else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Custom Mood Toggle Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCustomMoodActive) SleekPrimary else SleekSurfaceVariant)
                                    .border(
                                        1.dp,
                                        if (isCustomMoodActive) SleekPrimary else SleekCardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        isCustomMoodActive = true
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("mood_chip_custom"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+ Custom",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isCustomMoodActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isCustomMoodActive) SleekWhite else SleekPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Custom Mood Input Field (when active)
                    AnimatedVisibility(visible = isCustomMoodActive) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            OutlinedTextField(
                                value = customMoodText,
                                onValueChange = { customMoodText = it },
                                placeholder = { Text("e.g., Nostalgic, Disoriented, Euphoric...", color = TextSecondary, fontSize = 13.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_mood_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekPrimary,
                                    unfocusedBorderColor = SleekCardBorder,
                                    focusedContainerColor = SleekCanvas,
                                    unfocusedContainerColor = SleekCanvas,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dream Title Field
            Text(
                text = "Dream Title",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = SleekPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = dreamTitle,
                onValueChange = { dreamTitle = it },
                placeholder = { Text("e.g., The Obsidian Tower and the Silver Clock", color = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dream_title_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekCardBorder,
                    focusedContainerColor = SleekWhite,
                    unfocusedContainerColor = SleekWhite,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dream Narrative Transcript Text Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dream Transcript & Waking Notes",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SleekPrimary
                )
                Text(
                    text = "Voice or Type",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = dreamTranscript,
                onValueChange = { dreamTranscript = it },
                placeholder = {
                    Text(
                        "Describe everything you saw, felt, and experienced upon waking. Every setting, character, emotion, color, or symbol matters for Jungian decoding...",
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .testTag("dream_transcript_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekCardBorder,
                    focusedContainerColor = SleekWhite,
                    unfocusedContainerColor = SleekWhite,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Surrealist Dream Visuals Generation Studio
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SleekCardBorder, RoundedCornerShape(20.dp)),
                color = SleekWhite,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(SleekPrimaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Surrealist Dream Visuals Studio",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "AI Visual Synthesis",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SleekPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Movement / Master Style Selector
                    Text(
                        text = "Surrealist Movement & Aesthetic Style",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val styles = listOf(
                        "Masterwork Surrealism" to "Dali & Magritte Classic",
                        "Dalí Metamorphic" to "Melting Clocks & Deserts",
                        "Magritte Paradox" to "Impossible Day Skies",
                        "Chirico Metaphysical" to "Arcades & Deep Shadows",
                        "Varo Alchemical" to "Occult Towers & Starlight",
                        "Carrington Mythic" to "Folkloric Chimeras"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        styles.chunked(2).forEach { rowStyles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowStyles.forEach { (styleKey, styleSub) ->
                                    val isSelected = selectedSurrealistStyle == styleKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) SleekPrimary else SleekSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) SleekPrimary else SleekCardBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedSurrealistStyle = styleKey }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                            .testTag("style_chip_$styleKey")
                                    ) {
                                        Column {
                                            Text(
                                                text = styleKey,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    fontSize = 11.sp
                                                ),
                                                color = if (isSelected) SleekWhite else TextPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = styleSub,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.5.sp
                                                ),
                                                color = if (isSelected) SleekLilac else TextSecondary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Aspect Ratio & Resolution Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Aspect Ratio
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aspect Ratio",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("1:1" to "1:1", "16:9" to "16:9", "4:3" to "4:3").forEach { (ratio, label) ->
                                    val isSelected = selectedAspectRatio == ratio
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) SleekPrimary else SleekSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) SleekPrimary else SleekCardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedAspectRatio = ratio }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                            .testTag("aspect_chip_$ratio")
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            ),
                                            color = if (isSelected) SleekWhite else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Resolution Level
                        Column {
                            Text(
                                text = "Resolution",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("1K", "2K", "4K").forEach { size ->
                                    val isSelected = selectedImageSize == size
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) SleekPrimaryContainer else SleekSurfaceVariant)
                                            .border(
                                                1.dp,
                                                if (isSelected) SleekPrimary else SleekCardBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedImageSize = size }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                            .testTag("image_size_chip_$size")
                                    ) {
                                        Text(
                                            text = size,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            ),
                                            color = if (isSelected) SleekOnPrimaryContainer else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress banner during full dream creation (transcription -> interpretation -> 1K/2K/4K surrealist art)
            AnimatedVisibility(visible = isProcessingDream) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, SleekPrimaryContainer, RoundedCornerShape(18.dp)),
                    color = SleekWhite,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SleekPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = processingStep.ifBlank { "Analyzing dream subconscious..." },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SleekPrimary
                        )
                        Text(
                            text = "Decoding Jungian archetypes & painting $selectedSurrealistStyle visual in $selectedImageSize ($selectedAspectRatio)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Main CTA: "Interpret Subconscious & Paint Surrealist Dream"
            Button(
                onClick = {
                    if (dreamTranscript.isBlank()) {
                        Toast.makeText(context, "Please recount your dream by voice or typing first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val finalTitle = if (dreamTitle.isBlank()) "Subconscious Awakening Dream" else dreamTitle
                    viewModel.createAndProcessDream(
                        title = finalTitle,
                        transcript = dreamTranscript,
                        imageSize = selectedImageSize,
                        wakingMood = effectiveWakingMood,
                        surrealistStyle = selectedSurrealistStyle,
                        aspectRatio = selectedAspectRatio,
                        onSuccess = { newId ->
                            onDreamCreated(newId)
                        }
                    )
                },
                enabled = dreamTranscript.isNotBlank() && !isProcessingDream && !isRecording,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("analyze_and_paint_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekPrimary,
                    contentColor = SleekWhite,
                    disabledContainerColor = SleekSurfaceVariant,
                    disabledContentColor = TextMuted
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (dreamTranscript.isNotBlank()) SleekWhite else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Interpret Archetypes & Paint Dream",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

