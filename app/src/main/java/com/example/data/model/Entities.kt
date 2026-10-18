package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isReminder: Boolean = false,
    val reminderTimeMillis: Long? = null,
    val isCompleted: Boolean = false
)

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val spokenText: String,
    val actionType: String,
    val executionStatus: String, // "SUCCESS", "FAILED", "INFO"
    val feedbackMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
