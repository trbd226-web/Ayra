package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CommandHistoryEntity
import com.example.data.model.NoteEntity

@Database(
    entities = [NoteEntity::class, CommandHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AyraDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun commandDao(): CommandDao

    companion object {
        @Volatile
        private var INSTANCE: AyraDatabase? = null

        fun getDatabase(context: Context): AyraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AyraDatabase::class.java,
                    "ayra_assistant_db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
