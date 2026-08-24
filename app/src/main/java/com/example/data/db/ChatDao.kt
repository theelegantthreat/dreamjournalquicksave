package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM dream_chat_messages WHERE dreamId = :dreamId ORDER BY timestamp ASC")
    fun getMessagesForDream(dreamId: Long): Flow<List<ChatMessage>>

    @Query("SELECT * FROM dream_chat_messages WHERE dreamId = :dreamId ORDER BY timestamp ASC")
    suspend fun getMessagesListForDream(dreamId: Long): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Delete
    suspend fun deleteMessage(message: ChatMessage)

    @Query("DELETE FROM dream_chat_messages WHERE dreamId = :dreamId")
    suspend fun clearMessagesForDream(dreamId: Long)
}
