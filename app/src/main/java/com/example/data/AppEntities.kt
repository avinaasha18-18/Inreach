package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val username: String,
    val displayName: String,
    val bio: String,
    val openIntents: String, // Comma separated, e.g., "Job Offer, Collaboration, Mentorship"
    val trustScore: Int,
    val responseRate: Int,
    val verificationTier: Int, // 1 to 5
    val availabilityWindows: String, // comma-separated JSON or human readable, e.g. "Collaboration: 9AM-2PM Mon-Fri"
    val avatarUrl: String,
    val reputationScore: Int
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val senderUsername: String,
    val recipientUsername: String,
    val intent: String, // e.g. "Job Offer", "Collaboration", "Mentorship"
    val questionnaireAnswers: String, // JSON answers
    val rawText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "ESCROW", "UNREAD", "READ", "ACCEPTED", "DECLINED"
    val priorityScore: Int = 75,
    val toneScore: Int = 80,
    val spamScore: Int = 5,
    val summary: String = "",
    val smartReplies: String = "", // Comma separated
    val escrowHoursRemaining: Int = 48 // Count down
)

@Entity(tableName = "workspaces")
data class WorkspaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val messageId: Int,
    val originalIntent: String,
    val recipientUsername: String,
    val senderUsername: String,
    val title: String,
    val docContent: String,
    val proofOfWorkSummary: String = ""
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val senderName: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val replyToId: Int? = null,
    val emojiReactions: String = "", // comma sep
    val attachedFileUri: String? = null,
    val voiceNoteDuration: Int? = null // in seconds if simulated voice note
)

@Entity(tableName = "sticky_notes")
data class StickyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val text: String,
    val posX: Float = 100f,
    val posY: Float = 100f,
    val colorHex: String = "#FFF9C4" // Standard yellow sticky color
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val title: String,
    val assignee: String,
    val deadline: String,
    val columnStatus: String // "TODO", "IN_PROGRESS", "DONE"
)

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val title: String,
    val timeStr: String,
    val agendaText: String
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val title: String,
    val targetDate: String,
    val isCompleted: Boolean = false
)
