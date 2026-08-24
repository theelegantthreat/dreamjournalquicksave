package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.api.TranscriptionResult
import com.example.data.audio.AudioPlayerManager
import com.example.data.audio.AudioRecorderManager
import com.example.data.db.DreamDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DreamEntry
import com.example.data.model.PsychologicalInterpretation
import com.example.data.repository.DreamRepository
import com.example.data.repository.DreamRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class DreamViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: DreamRepository = DreamRepositoryImpl(
        DreamDatabase.getDatabase(application).dreamDao(),
        DreamDatabase.getDatabase(application).chatDao()
    )
) : AndroidViewModel(application) {

    private val audioRecorder = AudioRecorderManager(application)
    private val audioPlayer = AudioPlayerManager(application)

    val allDreams: StateFlow<List<DreamEntry>> = repository.allDreams
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Audio Recorder State
    val isRecording: StateFlow<Boolean> = audioRecorder.isRecording
    val recordingDurationMs: StateFlow<Long> = audioRecorder.recordingDurationMs
    val recordingAmplitude: StateFlow<Float> = audioRecorder.amplitude
    var currentAudioFilePath: String? = null
        private set

    // Audio Player State
    val isPlayingAudio: StateFlow<Boolean> = audioPlayer.isPlaying
    val audioCurrentPositionMs: StateFlow<Long> = audioPlayer.currentPositionMs
    val audioDurationMs: StateFlow<Long> = audioPlayer.durationMs

    // Processing States
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _isProcessingDream = MutableStateFlow(false)
    val isProcessingDream: StateFlow<Boolean> = _isProcessingDream.asStateFlow()

    private val _processingStep = MutableStateFlow("")
    val processingStep: StateFlow<String> = _processingStep.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    private val _isRepaintingImage = MutableStateFlow(false)
    val isRepaintingImage: StateFlow<Boolean> = _isRepaintingImage.asStateFlow()

    fun getChatMessagesForDream(dreamId: Long) = repository.getChatMessagesForDream(dreamId)

    fun startVoiceRecording() {
        audioPlayer.stopAudio()
        val result = audioRecorder.startRecording()
        result.onSuccess { file ->
            currentAudioFilePath = file.absolutePath
        }
    }

    fun stopVoiceRecording(): File? {
        val file = audioRecorder.stopRecording()
        currentAudioFilePath = file?.absolutePath
        return file
    }

    fun cancelVoiceRecording() {
        audioRecorder.cancelRecording()
        currentAudioFilePath = null
    }

    fun toggleAudioPlayback(filePath: String) {
        audioPlayer.togglePlayback(filePath)
    }

    fun stopAudioPlayback() {
        audioPlayer.stopAudio()
    }

    fun transcribeRecordedAudio(file: File, onComplete: (TranscriptionResult?) -> Unit) {
        viewModelScope.launch {
            _isTranscribing.value = true
            val result = GeminiService.transcribeDreamAudio(file)
            _isTranscribing.value = false
            result.onSuccess { res ->
                onComplete(res)
            }.onFailure {
                onComplete(null)
            }
        }
    }

    fun createAndProcessDream(
        title: String,
        transcript: String,
        imageSize: String, // "1K", "2K", "4K"
        wakingMood: String = "",
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            _isProcessingDream.value = true
            _processingStep.value = "Analyzing Jungian archetypes & symbols..."

            // Step 1: Psychological Interpretation
            val analysisResult = GeminiService.analyzeDreamPsychology(
                transcript = transcript,
                wakingMood = wakingMood,
                modelName = "gemini-3.1-pro-preview"
            )

            val interpretation = analysisResult.getOrNull() ?: PsychologicalInterpretation(
                dreamTitle = title,
                emotionalTheme = "Subconscious Awakening",
                surrealistImagePrompt = "A surrealist dream painting depicting $transcript in the style of Salvador Dali and Rene Magritte",
                corePsychologicalSynthesis = "This dream reflects an emergence from the unconscious mind, inviting deeper personal reflection."
            )

            // Step 2: Surrealist Artwork Generation with gemini-3-pro-image-preview
            _processingStep.value = "Painting surrealist dreamscape in $imageSize with Gemini 3 Pro..."
            val imageResult = GeminiService.generateSurrealistImage(
                prompt = interpretation.surrealistImagePrompt,
                imageSize = imageSize
            )

            val imageBase64 = imageResult.getOrNull()?.base64Data

            // Step 3: Insert Dream Entry to Room Database via Repository
            _processingStep.value = "Inscribing dream into subconscious codex..."
            val dreamEntry = DreamEntry(
                title = if (title.isNotBlank()) title else interpretation.dreamTitle,
                rawTranscription = transcript,
                audioFilePath = currentAudioFilePath,
                audioDurationMs = audioRecorder.recordingDurationMs.value,
                emotionalTheme = interpretation.emotionalTheme,
                emotionalToneTags = interpretation.emotionalToneTags.joinToString(", "),
                archetypesJson = PsychologicalInterpretation.archetypesToJsonString(interpretation.archetypes),
                symbolsJson = PsychologicalInterpretation.symbolsToJsonString(interpretation.keySymbols),
                psychologicalAnalysis = interpretation.corePsychologicalSynthesis,
                actionablePromptsJson = PsychologicalInterpretation.listToJsonString(interpretation.activeImaginationPrompts),
                imageUrl = imageBase64,
                imageSize = imageSize,
                imagePrompt = interpretation.surrealistImagePrompt,
                wakingMood = wakingMood
            )

            val newId = repository.insertDream(dreamEntry)

            // Step 4: Seed initial Jungian Analyst greeting in chat via Repository
            repository.insertChatMessage(
                ChatMessage(
                    dreamId = newId,
                    sender = "AI",
                    text = "Welcome to your dream's inner sanctum. I have analyzed your dream \"${dreamEntry.title}\". We identified ${interpretation.archetypes.size} active archetypes and ${interpretation.keySymbols.size} core symbols. Which symbol or feeling would you like to explore deeper?",
                    modelUsed = "gemini-3.5-flash"
                )
            )

            _isProcessingDream.value = false
            _processingStep.value = ""
            currentAudioFilePath = null

            onSuccess(newId)
        }
    }

    fun regenerateDreamImage(dreamId: Long, prompt: String, size: String) {
        viewModelScope.launch {
            _isRepaintingImage.value = true
            val result = GeminiService.generateSurrealistImage(
                prompt = prompt,
                imageSize = size
            )
            _isRepaintingImage.value = false

            result.onSuccess { data ->
                repository.updateDreamImage(
                    id = dreamId,
                    imageUrl = data.base64Data,
                    imageSize = size,
                    prompt = data.prompt
                )
            }
        }
    }

    fun sendSymbolChatMessage(dream: DreamEntry, userMessage: String, modelName: String) {
        viewModelScope.launch {
            _isChatSending.value = true

            // Insert User Message into Room via Repository
            repository.insertChatMessage(
                ChatMessage(
                    dreamId = dream.id,
                    sender = "USER",
                    text = userMessage,
                    modelUsed = modelName
                )
            )

            val history = repository.getChatMessagesListForDream(dream.id)

            val result = GeminiService.sendSymbolChatMessage(
                dreamTitle = dream.title,
                transcript = dream.rawTranscription,
                archetypesJson = dream.archetypesJson,
                symbolsJson = dream.symbolsJson,
                analysis = dream.psychologicalAnalysis,
                chatHistory = history,
                userMessage = userMessage,
                modelName = modelName
            )

            _isChatSending.value = false

            result.onSuccess { reply ->
                repository.insertChatMessage(
                    ChatMessage(
                        dreamId = dream.id,
                        sender = "AI",
                        text = reply,
                        modelUsed = modelName
                    )
                )
            }.onFailure { err ->
                repository.insertChatMessage(
                    ChatMessage(
                        dreamId = dream.id,
                        sender = "AI",
                        text = "I encountered a pause in our unconscious channel: ${err.message}. Please ask again.",
                        modelUsed = modelName
                    )
                )
            }
        }
    }

    fun toggleFavorite(dreamId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(dreamId)
        }
    }

    fun deleteDream(dream: DreamEntry) {
        viewModelScope.launch {
            repository.deleteDream(dream)
        }
    }

    fun clearChatMessages(dreamId: Long) {
        viewModelScope.launch {
            repository.clearChatMessagesForDream(dreamId)
        }
    }

    fun seedSampleDreams() {
        viewModelScope.launch {
            val sample1 = DreamEntry(
                title = "The Obsidian Clocktower & Sea of Glass",
                timestamp = System.currentTimeMillis() - 86400000L,
                rawTranscription = "I was standing on an endless sea made of black reflective glass. In the center was a massive melting clocktower made of obsidian. A veiled figure handed me a luminous golden key, but when I took it, flock of mechanical birds flew out of the clock's face and the sky turned into an aurora of indigo and gold.",
                emotionalTheme = "Shadow Integration & Transience",
                emotionalToneTags = "Awe, Lucid Metamorphosis, Mystery",
                archetypesJson = """
                    [
                        {
                            "name": "The Shadow",
                            "description": "Repressed or unintegrated unconscious aspects.",
                            "manifestation": "The veiled figure standing motionless offering the golden key."
                        },
                        {
                            "name": "The Self / Individuation",
                            "description": "The archetype of psychic wholeness and destiny.",
                            "manifestation": "The obsidian clocktower centered in the infinite glass ocean."
                        },
                        {
                            "name": "The Messenger / Hermes",
                            "description": "The conduit between unconscious and conscious awareness.",
                            "manifestation": "The mechanical birds bursting from the clockface."
                        }
                    ]
                """.trimIndent(),
                symbolsJson = """
                    [
                        {
                            "symbol": "Reflective Glass Ocean",
                            "psychologicalMeaning": "Calm, deep access to the collective unconscious with high clarity and lucid insight.",
                            "mythicEcho": "The primordial waters of creation and self-reflection."
                        },
                        {
                            "symbol": "Golden Key",
                            "psychologicalMeaning": "Latent creative potential waiting to be unlocked; permission to explore hidden psychic rooms.",
                            "mythicEcho": "The Key of Janus opening gates between past and future."
                        },
                        {
                            "symbol": "Obsidian Clocktower",
                            "psychologicalMeaning": "Anxiety around mortality or timing, balanced by enduring spiritual structures.",
                            "mythicEcho": "Chronos and the eternal axis mundi."
                        }
                    ]
                """.trimIndent(),
                psychologicalAnalysis = "Your subconscious is beckoning you to step beyond conventional timelines and confront the unlived creative potential represented by the veiled figure. Accepting the key triggers an explosion of new ideas (the birds). It is an encouraging call to trust your inner compass during current transitions.",
                actionablePromptsJson = """
                    [
                        "What door in your waking life feels locked that you secretly possess the key to?",
                        "In active imagination, dialog with the veiled figure: what gift do they hold for you?",
                        "How can you release rigid schedules to allow spontaneous creative flow?"
                    ]
                """.trimIndent(),
                imageSize = "2K",
                imagePrompt = "Surrealist oil painting in the style of Salvador Dali and Rene Magritte of a melting obsidian clocktower rising from a mirror-like black glass sea under an aurora sky, a hooded shadow figure extending a glowing golden key as luminous metallic birds take flight.",
                isFavorite = true,
                wakingMood = "Inspired"
            )

            val id1 = repository.insertDream(sample1)

            repository.insertChatMessage(
                ChatMessage(
                    dreamId = id1,
                    sender = "AI",
                    text = "Welcome to your dream's inner sanctum. I have analyzed \"The Obsidian Clocktower & Sea of Glass\". We identified 3 active archetypes (The Shadow, The Self, The Messenger). Which symbol or feeling would you like to explore deeper?",
                    modelUsed = "gemini-3.5-flash"
                )
            )

            repository.insertChatMessage(
                ChatMessage(
                    dreamId = id1,
                    sender = "USER",
                    text = "Why did the key feel warm when the veiled figure handed it to me?",
                    modelUsed = "gemini-3.5-flash"
                )
            )

            repository.insertChatMessage(
                ChatMessage(
                    dreamId = id1,
                    sender = "AI",
                    text = "The warmth of the key represents living psychic energy (libido in the Jungian sense). Unlike a cold, intellectual realization, warmth indicates that the latent potential is emotionally charged and ready to be integrated into your waking ego. The veiled figure—your Shadow—does not wish you harm, but rather delivers vitality that has been awaiting your conscious embrace.",
                    modelUsed = "gemini-3.1-pro-preview"
                )
            )

            val sample2 = DreamEntry(
                title = "The Emerald Labyrinth & Winged Sphinx",
                timestamp = System.currentTimeMillis() - 172800000L,
                rawTranscription = "I was walking through a spiral labyrinth made of glowing moss and ancient stone. At the center sat a gentle winged sphinx who spoke without moving its lips. It asked me to choose between a mirror and a compass.",
                emotionalTheme = "The Hero's Quest & Self-Knowledge",
                emotionalToneTags = "Contemplation, Guidance, Sacred",
                archetypesJson = """
                    [
                        {
                            "name": "The Great Sphinx / Wise Guardian",
                            "description": "Guardian of esoteric truths and threshold tester.",
                            "manifestation": "The gentle winged sphinx posing the riddle of mirror vs compass."
                        },
                        {
                            "name": "The Spiral Labyrinth",
                            "description": "The sacred path of circumambulation toward the Self.",
                            "manifestation": "The glowing moss corridors leading to the central sanctuary."
                        }
                    ]
                """.trimIndent(),
                symbolsJson = """
                    [
                        {
                            "symbol": "Mirror vs Compass",
                            "psychologicalMeaning": "The tension between self-examination (the mirror) and outward direction / action (the compass).",
                            "mythicEcho": "The Delphic oracle: 'Know thyself'."
                        }
                    ]
                """.trimIndent(),
                psychologicalAnalysis = "The dream presents a pivotal decision point: before choosing your outer direction (compass), your unconscious suggests integrating deep honest introspection (mirror).",
                actionablePromptsJson = """
                    [
                        "Are you currently prioritizing outward goals (compass) over inner alignment (mirror)?",
                        "What riddle is life currently presenting to you?"
                    ]
                """.trimIndent(),
                imageSize = "1K",
                imagePrompt = "Surrealist painting in the style of Remedios Varo and Giorgio de Chirico of an ancient spiral emerald moss labyrinth with a glowing winged sphinx at the center offering a brass compass and a silver mirror under a starlit twilight canopy.",
                isFavorite = false,
                wakingMood = "Peaceful"
            )

            val id2 = repository.insertDream(sample2)

            repository.insertChatMessage(
                ChatMessage(
                    dreamId = id2,
                    sender = "AI",
                    text = "Welcome to \"The Emerald Labyrinth & Winged Sphinx\". The Sphinx represents your inner wisdom testing your readiness for the next life chapter. Would you like to ask what choosing the mirror or compass signifies?",
                    modelUsed = "gemini-3.5-flash"
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.cancelRecording()
        audioPlayer.release()
    }
}
