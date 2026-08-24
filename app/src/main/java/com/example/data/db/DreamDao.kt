package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DreamEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DreamDao {
    @Query("SELECT * FROM dream_entries ORDER BY timestamp DESC")
    fun getAllDreams(): Flow<List<DreamEntry>>

    @Query("SELECT * FROM dream_entries WHERE id = :id LIMIT 1")
    fun getDreamByIdFlow(id: Long): Flow<DreamEntry?>

    @Query("SELECT * FROM dream_entries WHERE id = :id LIMIT 1")
    suspend fun getDreamById(id: Long): DreamEntry?

    @Query("SELECT * FROM dream_entries WHERE title LIKE '%' || :query || '%' OR rawTranscription LIKE '%' || :query || '%' OR emotionalTheme LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchDreams(query: String): Flow<List<DreamEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDream(dream: DreamEntry): Long

    @Update
    suspend fun updateDream(dream: DreamEntry)

    @Delete
    suspend fun deleteDream(dream: DreamEntry)

    @Query("DELETE FROM dream_entries WHERE id = :id")
    suspend fun deleteDreamById(id: Long)

    @Query("UPDATE dream_entries SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("UPDATE dream_entries SET imageUrl = :imageUrl, imageSize = :imageSize, imagePrompt = :prompt WHERE id = :id")
    suspend fun updateDreamImage(id: Long, imageUrl: String, imageSize: String, prompt: String)
}
