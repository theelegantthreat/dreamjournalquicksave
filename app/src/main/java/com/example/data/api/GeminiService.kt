package com.example.data.api

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ArchetypeItem
import com.example.data.model.ChatMessage
import com.example.data.model.DreamSymbolItem
import com.example.data.model.PsychologicalInterpretation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    // Configured with 60-second timeouts as per skill mandates
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isNullOrBlank() || key == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "GEMINI_API_KEY is not set or using placeholder.")
        }
        return key ?: ""
    }

    /**
     * Transcribes an audio recording file into a lucid dream narrative transcript.
     * Uses gemini-3.5-flash for audio comprehension.
     */
    suspend fun transcribeDreamAudio(audioFile: File): Result<TranscriptionResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API key is not configured."))
            }

            val audioBytes = FileInputStream(audioFile).use { it.readBytes() }
            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)

            // Detect audio mime type based on file extension
            val mimeType = when {
                audioFile.name.endsWith(".m4a", true) -> "audio/mp4"
                audioFile.name.endsWith(".aac", true) -> "audio/aac"
                audioFile.name.endsWith(".wav", true) -> "audio/wav"
                audioFile.name.endsWith(".mp3", true) -> "audio/mp3"
                else -> "audio/mp4"
            }

            val prompt = """
                You are transcribing a voice note of someone who just woke up and is recounting their vivid dream.
                1. Transcribe the spoken dream accurately and completely in natural prose.
                2. Extract a poetic, evocative dream title (3 to 6 words).
                3. Return your response in pure JSON matching this exact structure:
                {
                    "title": "Title of the Dream",
                    "transcript": "Full accurate transcription of the dream recording..."
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            // Audio part
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", mimeType)
                                    put("data", base64Audio)
                                })
                            })
                            // Prompt part
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseFormat", JSONObject().apply {
                        put("text", JSONObject().apply {
                            put("mimeType", "application/json")
                        })
                    })
                })
            }

            val modelName = "gemini-3.5-flash"
            val url = "$BASE_URL$modelName:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Transcription failed: ${response.code} $responseBody")
                return@withContext Result.failure(Exception("Transcription error (${response.code}): $responseBody"))
            }

            val parsedJson = extractTextFromGeminiResponse(responseBody)
            val jsonObject = JSONObject(parsedJson)
            val title = jsonObject.optString("title", "Awakening Dream Vision")
            val transcript = jsonObject.optString("transcript", "")

            Result.success(TranscriptionResult(title = title, transcript = transcript))
        } catch (e: Exception) {
            Log.e(TAG, "Audio transcription error", e)
            Result.failure(e)
        }
    }

    /**
     * Performs a deep Jungian psychological archetype and symbol analysis of the dream.
     * Uses gemini-3.1-pro-preview or gemini-3.5-flash.
     */
    suspend fun analyzeDreamPsychology(
        transcript: String,
        wakingMood: String = "",
        modelName: String = "gemini-3.1-pro-preview"
    ): Result<PsychologicalInterpretation> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API key is not configured."))
            }

            val systemInstruction = """
                You are a world-class Jungian Depth Psychologist, Symbolist, and Mythologist specializing in dream analysis.
                Analyze the dreamer's transcript through the lens of Carl Jung's analytical psychology (Collective Unconscious, Shadow, Anima/Animus, The Self, Persona, Individuation, Active Imagination).
                Extract distinct archetypes, decode hidden symbols, synthesize the unconscious message, craft a surrealist art prompt, and provide active imagination prompts.
                Output ONLY a valid JSON object matching the requested schema.
            """.trimIndent()

            val moodContext = if (wakingMood.isNotBlank()) "Waking Mood Upon Awakening: $wakingMood\n" else ""

            val prompt = """
                ${moodContext}Dream Transcript:
                \"\"\"$transcript\"\"\"

                Provide a structured psychological interpretation in JSON with these exact fields:
                {
                    "dreamTitle": "Poetic evocative title (3-6 words)",
                    "emotionalTheme": "Core emotional/spiritual theme (e.g., Shadow Integration & Liberation)",
                    "emotionalToneTags": ["Awe", "Vulnerability", "Transformation", "Mystery"],
                    "surrealistImagePrompt": "A highly detailed surrealist painting prompt evoking Salvador Dali, Rene Magritte, and Remedios Varo capturing the core emotional and symbolic essence of this dream. E.g., A surrealist oil painting of a melting brass astrolabe floating over a luminescent midnight sea with floating monolithic keys...",
                    "archetypes": [
                        {
                            "name": "The Shadow / The Anima / The Wise Guide / The Threshold / etc.",
                            "description": "Jungian definition of this archetype.",
                            "manifestation": "How it specifically appeared and acted in this dream."
                        }
                    ],
                    "keySymbols": [
                        {
                            "symbol": "Specific object, character, or setting from the dream",
                            "psychologicalMeaning": "Depth psychological significance to the dreamer's waking psyche.",
                            "mythicEcho": "Mythological, alchemical, or universal archetype connection."
                        }
                    ],
                    "corePsychologicalSynthesis": "An eloquent, comprehensive synthesis explaining what the unconscious mind is signaling to the waking conscious self regarding current life transitions, repressed potentials, and individuation.",
                    "activeImaginationPrompts": [
                        "Self-reflection question 1 for daily meditation",
                        "Active imagination journaling exercise 2",
                        "Integration inquiry 3"
                    ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("responseFormat", JSONObject().apply {
                        put("text", JSONObject().apply {
                            put("mimeType", "application/json")
                        })
                    })
                })
            }

            val targetModel = if (modelName.isNotBlank()) modelName else "gemini-3.1-pro-preview"
            val url = "$BASE_URL$targetModel:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Dream analysis failed: ${response.code} $responseBody")
                return@withContext Result.failure(Exception("Analysis error (${response.code}): $responseBody"))
            }

            val rawJsonText = extractTextFromGeminiResponse(responseBody)
            val json = JSONObject(rawJsonText)

            val dreamTitle = json.optString("dreamTitle", "Dream Vision")
            val emotionalTheme = json.optString("emotionalTheme", "Subconscious Realization")
            val surrealistPrompt = json.optString("surrealistImagePrompt", "Surrealist artwork depicting subconscious dream visions")
            val synthesis = json.optString("corePsychologicalSynthesis", "")

            val toneTags = mutableListOf<String>()
            val tagsArray = json.optJSONArray("emotionalToneTags")
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    toneTags.add(tagsArray.optString(i))
                }
            }

            val archetypes = mutableListOf<ArchetypeItem>()
            val archArray = json.optJSONArray("archetypes")
            if (archArray != null) {
                for (i in 0 until archArray.length()) {
                    val item = archArray.getJSONObject(i)
                    archetypes.add(
                        ArchetypeItem(
                            name = item.optString("name", "Archetype"),
                            description = item.optString("description", ""),
                            manifestation = item.optString("manifestation", "")
                        )
                    )
                }
            }

            val symbols = mutableListOf<DreamSymbolItem>()
            val symArray = json.optJSONArray("keySymbols")
            if (symArray != null) {
                for (i in 0 until symArray.length()) {
                    val item = symArray.getJSONObject(i)
                    symbols.add(
                        DreamSymbolItem(
                            symbol = item.optString("symbol", "Symbol"),
                            psychologicalMeaning = item.optString("psychologicalMeaning", ""),
                            mythicEcho = item.optString("mythicEcho", "")
                        )
                    )
                }
            }

            val prompts = mutableListOf<String>()
            val promptsArray = json.optJSONArray("activeImaginationPrompts")
            if (promptsArray != null) {
                for (i in 0 until promptsArray.length()) {
                    prompts.add(promptsArray.optString(i))
                }
            }

            val interpretation = PsychologicalInterpretation(
                dreamTitle = dreamTitle,
                emotionalTheme = emotionalTheme,
                emotionalToneTags = toneTags,
                surrealistImagePrompt = surrealistPrompt,
                archetypes = archetypes,
                keySymbols = symbols,
                corePsychologicalSynthesis = synthesis,
                activeImaginationPrompts = prompts
            )

            Result.success(interpretation)
        } catch (e: Exception) {
            Log.e(TAG, "Psychological interpretation error", e)
            Result.failure(e)
        }
    }

    /**
     * Generates a surrealist dream artwork using gemini-3-pro-image-preview.
     * Supports imageSize affordances: "1K", "2K", "4K" and aspect ratios: "1:1", "16:9", "4:3".
     */
    suspend fun generateSurrealistImage(
        prompt: String,
        imageSize: String = "1K", // "1K", "2K", "4K"
        aspectRatio: String = "1:1"
    ): Result<GeneratedImageData> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API key is not configured."))
            }

            val enhancedPrompt = if (prompt.contains("surrealist", ignoreCase = true)) {
                prompt
            } else {
                "Surrealist dream painting in the masterwork styles of Salvador Dali, Rene Magritte, and Remedios Varo: $prompt. Atmospheric dreamscape, ethereal lighting, symbolic subconscious imagery, cinematic depth, rich vibrant textures."
            }

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", enhancedPrompt)
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", aspectRatio)
                        put("imageSize", imageSize)
                    })
                    val modalities = JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    }
                    put("responseModalities", modalities)
                })
            }

            // Strictly using mandated model gemini-3-pro-image-preview
            val modelName = "gemini-3-pro-image-preview"
            val url = "$BASE_URL$modelName:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Image generation error: ${response.code} $responseBody")
                return@withContext Result.failure(Exception("Image generation error (${response.code}): $responseBody"))
            }

            // Extract image data from candidates
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No image candidates returned from Gemini."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var base64Data: String? = null
            var mimeType = "image/jpeg"
            var textDescription = ""

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("inlineData")) {
                        val inlineData = part.getJSONObject("inlineData")
                        base64Data = inlineData.optString("data")
                        mimeType = inlineData.optString("mimeType", "image/jpeg")
                    } else if (part.has("text")) {
                        textDescription += part.optString("text") + " "
                    }
                }
            }

            if (base64Data.isNullOrBlank()) {
                return@withContext Result.failure(Exception("No image bytes found in response: $textDescription"))
            }

            Result.success(
                GeneratedImageData(
                    base64Data = base64Data,
                    mimeType = mimeType,
                    imageSize = imageSize,
                    prompt = enhancedPrompt,
                    description = textDescription.trim()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Surrealist image generation failed", e)
            Result.failure(e)
        }
    }

    /**
     * Multi-turn chat conversation about specific dream symbols and archetypes.
     * Incorporates system instruction for Jungian Depth Psychologist role.
     */
    suspend fun sendSymbolChatMessage(
        dreamTitle: String,
        transcript: String,
        archetypesJson: String,
        symbolsJson: String,
        analysis: String,
        chatHistory: List<ChatMessage>,
        userMessage: String,
        modelName: String = "gemini-3.5-flash"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API key is not configured."))
            }

            val systemRoleInstruction = """
                You are a compassionate, deeply insightful Jungian Depth Psychologist and Dream Symbolist.
                You are assisting the dreamer in actively decoding and exploring their dream: "$dreamTitle".
                
                Dream Context:
                - Transcript: $transcript
                - Archetypes identified: $archetypesJson
                - Key Symbols: $symbolsJson
                - Core Analysis: $analysis
                
                Guidelines for your responses:
                1. Focus deeply on the specific symbols, characters, motifs, emotions, and scenarios the user asks about.
                2. Draw on Jungian concepts (Active Imagination, Archetypal Amplification, Shadow Integration, Collective Unconscious, Synchronicity).
                3. Ask evocative, open-ended questions that prompt the dreamer to connect dream symbols to their waking emotional reality.
                4. Maintain a warm, wise, poetic, yet psychologically grounded presence.
                5. Keep replies engaging, well-formatted with clear paragraphs, bullet points when analyzing multiple facets, and insightful takeaways.
            """.trimIndent()

            val contentsArray = JSONArray()

            // Build historical conversation turns
            for (msg in chatHistory) {
                val role = if (msg.sender == "USER") "user" else "model"
                val turn = JSONObject().apply {
                    put("role", role)
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    }
                    put("parts", parts)
                }
                contentsArray.put(turn)
            }

            // Append current user message
            val currentTurn = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", userMessage) })
                }
                put("parts", parts)
            }
            contentsArray.put(currentTurn)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemRoleInstruction) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            val selectedModel = when (modelName) {
                "gemini-3.1-pro-preview", "gemini-3.1-flash-lite-preview", "gemini-3.5-flash" -> modelName
                else -> "gemini-3.5-flash"
            }

            val url = "$BASE_URL$selectedModel:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Chat request error: ${response.code} $responseBody")
                return@withContext Result.failure(Exception("Chat error (${response.code}): $responseBody"))
            }

            val responseText = extractTextFromGeminiResponse(responseBody)
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Symbol chat message error", e)
            Result.failure(e)
        }
    }

    private fun extractTextFromGeminiResponse(jsonString: String): String {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCand = candidates.getJSONObject(0)
            val content = firstCand.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    sb.append(part.getString("text"))
                }
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }
}

data class TranscriptionResult(
    val title: String,
    val transcript: String
)

data class GeneratedImageData(
    val base64Data: String,
    val mimeType: String,
    val imageSize: String,
    val prompt: String,
    val description: String
)
