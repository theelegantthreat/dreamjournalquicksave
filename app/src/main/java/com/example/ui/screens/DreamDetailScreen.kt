package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DreamEntry
import com.example.data.model.PsychologicalInterpretation
import com.example.ui.components.ArchetypeCard
import com.example.ui.components.SurrealistImageViewer
import com.example.ui.components.SymbolCard
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DreamDetailScreen(
    dreamId: Long,
    viewModel: DreamViewModel,
    onBack: () -> Unit
) {
    val dreams by viewModel.allDreams.collectAsState()
    val dream = dreams.firstOrNull { it.id == dreamId }
    val chatMessages by viewModel.getChatMessagesForDream(dreamId).collectAsState(initial = emptyList())
    val isChatSending by viewModel.isChatSending.collectAsState()
    val isRepaintingImage by viewModel.isRepaintingImage.collectAsState()
    val isAudioPlaying by viewModel.isPlayingAudio.collectAsState()

    var prefilledChatQuery by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    if (dream == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SleekCanvas),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SleekPrimary)
        }
        return
    }

    val interpretation = remember(dream) {
        PsychologicalInterpretation.fromJsonString(
            archetypesJson = dream.archetypesJson,
            symbolsJson = dream.symbolsJson,
            promptsJson = dream.actionablePromptsJson,
            title = dream.title,
            theme = dream.emotionalTheme,
            toneTags = dream.emotionalToneTags,
            analysis = dream.psychologicalAnalysis,
            prompt = dream.imagePrompt
        )
    }

    val dateFormatter = remember { SimpleDateFormat("MMMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(dream.timestamp) { dateFormatter.format(Date(dream.timestamp)) }

    Scaffold(
        containerColor = SleekCanvas,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.toggleFavorite(dream.id) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(SleekWhite, CircleShape)
                            .border(1.dp, SleekCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (dream.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (dream.isFavorite) Color(0xFFBA1A1A) else TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.deleteDream(dream)
                            onBack()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(SleekWhite, CircleShape)
                            .border(1.dp, SleekCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Dream",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            // Header Info & Title
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dream.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                ),
                color = TextPrimary,
                lineHeight = 30.sp
            )

            // Emotional Theme and Waking Mood Badge Bar
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (dream.wakingMood.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekPrimary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("detail_waking_mood_badge")
                    ) {
                        Text(
                            text = "Mood: ${dream.wakingMood}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SleekWhite
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SleekPrimaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dream.emotionalTheme,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SleekOnPrimaryContainer
                        )
                    )
                }

                if (dream.emotionalToneTags.isNotBlank()) {
                    val firstTag = dream.emotionalToneTags.split(",").firstOrNull()?.trim() ?: ""
                    if (firstTag.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekSurfaceVariant)
                                .border(1.dp, SleekCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = firstTag,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 1: Surrealist Artwork (Gemini 3 Pro Image Preview at 1K, 2K, 4K)
            Text(
                text = "SURREALIST EMOTIONAL ESSENCE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                color = SleekPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            SurrealistImageViewer(
                imageUrl = dream.imageUrl,
                imagePrompt = dream.imagePrompt.ifBlank { interpretation.surrealistImagePrompt },
                imageSize = dream.imageSize,
                isGenerating = isRepaintingImage,
                onRegenerate = { newSize, prompt, style, aspectRatio ->
                    viewModel.regenerateDreamImage(dream.id, prompt, newSize, style, aspectRatio)
                }
            )

            // Audio Player Bar (if recorded audio file exists)
            if (!dream.audioFilePath.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp)),
                    color = SleekWhite,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleAudioPlayback(dream.audioFilePath) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(SleekPrimary, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play waking recording",
                                tint = SleekWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isAudioPlaying) "Playing Waking Voice Journal" else "Recorded Waking Audio",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Listen to your immediate post-awakening impression",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // SECTION 2: Waking Dream Narrative / Transcript
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "DREAM NARRATIVE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                color = SleekPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp)),
                color = SleekWhite,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = dream.rawTranscription,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 23.sp,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // SECTION 3: Structured Psychological Interpretation (Jungian Archetypes)
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "JUNGIAN DEPTH INTERPRETATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                    color = SleekPrimary
                )
            }

            // Unconscious Synthesis Card
            if (dream.psychologicalAnalysis.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp)),
                    color = SleekWhite,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SleekPrimaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelfImprovement,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Unconscious Message to Conscious Self",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = SleekPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = dream.psychologicalAnalysis,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 22.sp,
                                fontSize = 14.5.sp
                            ),
                            color = TextPrimary
                        )
                    }
                }
            }

            // Jungian Archetypes Cards
            if (interpretation.archetypes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Active Archetypes (${interpretation.archetypes.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    interpretation.archetypes.forEach { archetype ->
                        ArchetypeCard(
                            archetype = archetype,
                            onInquireClick = { query ->
                                prefilledChatQuery = query
                            }
                        )
                    }
                }
            }

            // Key Dream Symbols Cards
            if (interpretation.keySymbols.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Decoded Dream Symbols (${interpretation.keySymbols.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    interpretation.keySymbols.forEach { symbolItem ->
                        SymbolCard(
                            symbolItem = symbolItem,
                            onInquireClick = { query ->
                                prefilledChatQuery = query
                            }
                        )
                    }
                }
            }

            // Active Imagination Prompts
            if (interpretation.activeImaginationPrompts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Active Imagination & Reflection Prompts",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(18.dp)),
                    color = SleekWhite,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        interpretation.activeImaginationPrompts.forEachIndexed { idx, prompt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SleekSurfaceVariant)
                                    .clickable { prefilledChatQuery = prompt }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${idx + 1}.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = SleekPrimary,
                                    modifier = Modifier.width(22.dp)
                                )
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 4: Multi-Turn Chat Section for Symbol Inquiry
            Spacer(modifier = Modifier.height(26.dp))
            SymbolChatSection(
                dream = dream,
                chatMessages = chatMessages,
                isSending = isChatSending,
                onSendMessage = { text, model ->
                    viewModel.sendSymbolChatMessage(dream, text, model)
                },
                onClearChat = {
                    viewModel.clearChatMessages(dream.id)
                },
                prefilledQuery = prefilledChatQuery,
                onPrefilledHandled = { prefilledChatQuery = null }
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

