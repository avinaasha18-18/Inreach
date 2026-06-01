package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- Profiles ---
    val allProfiles: Flow<List<ProfileEntity>> = appDao.getAllProfiles()

    suspend fun getProfile(username: String): ProfileEntity? {
        return appDao.getProfileByUsername(username)
    }

    suspend fun insertProfile(profile: ProfileEntity) {
        appDao.insertProfile(profile)
    }

    // --- Messages ---
    val allMessages: Flow<List<MessageEntity>> = appDao.getAllMessages()

    suspend fun getMessageById(id: Int): MessageEntity? {
        return appDao.getMessageById(id)
    }

    suspend fun insertMessage(message: MessageEntity) {
        appDao.insertMessage(message)
    }

    suspend fun updateMessage(message: MessageEntity) {
        appDao.updateMessage(message)
    }

    suspend fun deleteMessage(id: Int) {
        appDao.deleteMessageById(id)
    }

    // --- Workspaces ---
    val allWorkspaces: Flow<List<WorkspaceEntity>> = appDao.getAllWorkspaces()

    fun getWorkspaceByIdFlow(id: Int): Flow<WorkspaceEntity?> {
        return appDao.getWorkspaceByIdFlow(id)
    }

    suspend fun getWorkspaceById(id: Int): WorkspaceEntity? {
        return appDao.getWorkspaceById(id)
    }

    suspend fun getWorkspaceByMessageId(messageId: Int): WorkspaceEntity? {
        return appDao.getWorkspaceByMessageId(messageId)
    }

    suspend fun insertWorkspace(workspace: WorkspaceEntity): Int {
        return appDao.insertWorkspace(workspace).toInt()
    }

    suspend fun updateWorkspace(workspace: WorkspaceEntity) {
        appDao.updateWorkspace(workspace)
    }

    // --- Chats ---
    fun getChats(workspaceId: Int): Flow<List<ChatEntity>> = appDao.getChatsByWorkspace(workspaceId)

    suspend fun insertChat(chat: ChatEntity): Int {
        return appDao.insertChat(chat).toInt()
    }

    // --- Sticky Notes ---
    fun getStickyNotes(workspaceId: Int): Flow<List<StickyNoteEntity>> = appDao.getStickyNotesByWorkspace(workspaceId)

    suspend fun insertStickyNote(note: StickyNoteEntity): Int {
        return appDao.insertStickyNote(note).toInt()
    }

    suspend fun updateStickyNote(note: StickyNoteEntity) {
        appDao.updateStickyNote(note)
    }

    suspend fun deleteStickyNote(note: StickyNoteEntity) {
        appDao.deleteStickyNote(note)
    }

    // --- Tasks ---
    fun getTasks(workspaceId: Int): Flow<List<TaskEntity>> = appDao.getTasksByWorkspace(workspaceId)

    suspend fun insertTask(task: TaskEntity): Int {
        return appDao.insertTask(task).toInt()
    }

    suspend fun updateTask(task: TaskEntity) {
        appDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        appDao.deleteTask(task)
    }

    // --- Meetings ---
    fun getMeetings(workspaceId: Int): Flow<List<MeetingEntity>> = appDao.getMeetingsByWorkspace(workspaceId)

    suspend fun insertMeeting(meeting: MeetingEntity): Int {
        return appDao.insertMeeting(meeting).toInt()
    }

    suspend fun deleteMeeting(meeting: MeetingEntity) {
        appDao.deleteMeeting(meeting)
    }

    // --- Milestones ---
    fun getMilestones(workspaceId: Int): Flow<List<MilestoneEntity>> = appDao.getMilestonesByWorkspace(workspaceId)

    suspend fun insertMilestone(milestone: MilestoneEntity): Int {
        return appDao.insertMilestone(milestone).toInt()
    }

    suspend fun updateMilestone(milestone: MilestoneEntity) {
        appDao.updateMilestone(milestone)
    }

    suspend fun deleteMilestone(milestone: MilestoneEntity) {
        appDao.deleteMilestone(milestone)
    }

    suspend fun getMessagesByStatusSync(status: String): List<MessageEntity> {
        return appDao.getMessagesByStatusSync(status)
    }

    val allWarmIntroRequests: Flow<List<WarmIntroRequest>> = appDao.getAllWarmIntroRequests()

    fun getWarmIntroRequestsByMutual(mutualUserId: String): Flow<List<WarmIntroRequest>> {
        return appDao.getWarmIntroRequestsByMutual(mutualUserId)
    }

    suspend fun insertWarmIntroRequest(request: WarmIntroRequest) {
        appDao.insertWarmIntroRequest(request)
    }

    suspend fun updateWarmIntroRequestStatus(id: Int, status: String) {
        appDao.updateWarmIntroRequestStatus(id, status)
    }
}
