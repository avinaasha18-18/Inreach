package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Profiles ---
    @Query("SELECT * FROM profiles")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE username = :username LIMIT 1")
    suspend fun getProfileByUsername(username: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    // --- Messages ---
    @Query("SELECT * FROM messages ORDER BY priorityScore DESC, timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Int): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Int)

    // --- Workspaces ---
    @Query("SELECT * FROM workspaces")
    fun getAllWorkspaces(): Flow<List<WorkspaceEntity>>

    @Query("SELECT * FROM workspaces WHERE id = :id")
    fun getWorkspaceByIdFlow(id: Int): Flow<WorkspaceEntity?>

    @Query("SELECT * FROM workspaces WHERE id = :id")
    suspend fun getWorkspaceById(id: Int): WorkspaceEntity?

    @Query("SELECT * FROM workspaces WHERE messageId = :messageId LIMIT 1")
    suspend fun getWorkspaceByMessageId(messageId: Int): WorkspaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: WorkspaceEntity): Long

    @Update
    suspend fun updateWorkspace(workspace: WorkspaceEntity)

    // --- Chats ---
    @Query("SELECT * FROM chats WHERE workspaceId = :workspaceId ORDER BY timestamp ASC")
    fun getChatsByWorkspace(workspaceId: Int): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long

    // --- Sticky Notes ---
    @Query("SELECT * FROM sticky_notes WHERE workspaceId = :workspaceId")
    fun getStickyNotesByWorkspace(workspaceId: Int): Flow<List<StickyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStickyNote(note: StickyNoteEntity): Long

    @Update
    suspend fun updateStickyNote(note: StickyNoteEntity)

    @Delete
    suspend fun deleteStickyNote(note: StickyNoteEntity)

    // --- Tasks ---
    @Query("SELECT * FROM tasks WHERE workspaceId = :workspaceId")
    fun getTasksByWorkspace(workspaceId: Int): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // --- Meetings ---
    @Query("SELECT * FROM meetings WHERE workspaceId = :workspaceId")
    fun getMeetingsByWorkspace(workspaceId: Int): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long

    @Delete
    suspend fun deleteMeeting(meeting: MeetingEntity)

    // --- Milestones ---
    @Query("SELECT * FROM milestones WHERE workspaceId = :workspaceId")
    fun getMilestonesByWorkspace(workspaceId: Int): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)
}
