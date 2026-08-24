package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dream_entries")
data class DreamEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rawTranscription: String,
    val audioFilePath: String? = null,
    val audioDurationMs: Long = 0,
    val emotionalTheme: String = "Lucid Subconscious Exploration",
    val emotionalToneTags: String = "Mysterious, Introspective",
    val archetypesJson: String = "[]",
    val symbolsJson: String = "[]",
    val psychologicalAnalysis: String = "",
    val actionablePromptsJson: String = "[]",
    val imageUrl: String? = null, // Base64 data URI or file path
    val imageSize: String = "1K", // "1K", "2K", "4K"
    val imagePrompt: String = "",
    val isFavorite: Boolean = false,
    val lucidityLevel: Int = 3,
    val wakingMood: String = "" // e.g., 'Peaceful', 'Anxious', 'Confused', 'Inspired', or custom
)
