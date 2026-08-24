package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dream_chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = DreamEntry::class,
            parentColumns = ["id"],
            childColumns = ["dreamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dreamId"])]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dreamId: Long,
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-3.5-flash"
)
