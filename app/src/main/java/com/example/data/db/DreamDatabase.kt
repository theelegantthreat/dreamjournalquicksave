package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DreamEntry

@Database(
    entities = [DreamEntry::class, ChatMessage::class],
    version = 2,
    exportSchema = false
)
abstract class DreamDatabase : RoomDatabase() {
    abstract fun dreamDao(): DreamDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: DreamDatabase? = null

        fun getDatabase(context: Context): DreamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DreamDatabase::class.java,
                    "dream_journal_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
