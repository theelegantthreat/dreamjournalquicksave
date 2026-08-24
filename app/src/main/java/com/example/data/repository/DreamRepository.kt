package com.example.data.repository

import com.example.data.db.ChatDao
import com.example.data.db.DreamDao
import com.example.data.model.ChatMessage
import com.example.data.model.DreamEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining local Room database operations for Dream Entries and Chat Inquiries.
 * Adheres to the Room Database Integration pattern.
 */
interface DreamRepository {
    val allDreams: Flow<List<DreamEntry>>
    fun getDreamByIdFlow(id: Long): Flow<DreamEntry?>
    suspend fun getDreamById(id: Long): DreamEntry?
    fun searchDreams(query: String): Flow<List<DreamEntry>>
    suspend fun insertDream(dream: DreamEntry): Long
    suspend fun updateDream(dream: DreamEntry)
    suspend fun deleteDream(dream: DreamEntry)
    suspend fun deleteDreamById(id: Long)
    suspend fun toggleFavorite(id: Long)
    suspend fun updateDreamImage(id: Long, imageUrl: String, imageSize: String, prompt: String)

    // Chat / Dialogue operations
    fun getChatMessagesForDream(dreamId: Long): Flow<List<ChatMessage>>
    suspend fun getChatMessagesListForDream(dreamId: Long): List<ChatMessage>
    suspend fun insertChatMessage(message: ChatMessage): Long
    suspend fun clearChatMessagesForDream(dreamId: Long)
}

class DreamRepositoryImpl(
    private val dreamDao: DreamDao,
    private val chatDao: ChatDao
) : DreamRepository {

    override val allDreams: Flow<List<DreamEntry>> = dreamDao.getAllDreams()

    override fun getDreamByIdFlow(id: Long): Flow<DreamEntry?> =
        dreamDao.getDreamByIdFlow(id)

    override suspend fun getDreamById(id: Long): DreamEntry? =
        dreamDao.getDreamById(id)

    override fun searchDreams(query: String): Flow<List<DreamEntry>> =
        dreamDao.searchDreams(query)

    override suspend fun insertDream(dream: DreamEntry): Long =
        dreamDao.insertDream(dream)

    override suspend fun updateDream(dream: DreamEntry) =
        dreamDao.updateDream(dream)

    override suspend fun deleteDream(dream: DreamEntry) =
        dreamDao.deleteDream(dream)

    override suspend fun deleteDreamById(id: Long) =
        dreamDao.deleteDreamById(id)

    override suspend fun toggleFavorite(id: Long) =
        dreamDao.toggleFavorite(id)

    override suspend fun updateDreamImage(id: Long, imageUrl: String, imageSize: String, prompt: String) =
        dreamDao.updateDreamImage(id, imageUrl, imageSize, prompt)

    override fun getChatMessagesForDream(dreamId: Long): Flow<List<ChatMessage>> =
        chatDao.getMessagesForDream(dreamId)

    override suspend fun getChatMessagesListForDream(dreamId: Long): List<ChatMessage> =
        chatDao.getMessagesListForDream(dreamId)

    override suspend fun insertChatMessage(message: ChatMessage): Long =
        chatDao.insertMessage(message)

    override suspend fun clearChatMessagesForDream(dreamId: Long) =
        chatDao.clearMessagesForDream(dreamId)
}
