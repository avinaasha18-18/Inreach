package com.example

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.api.AiAnalysisResult
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONArray
import kotlin.math.cos
import kotlin.math.sin

data class CardPhysics(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var rotation: Float = 0f,
    var vRot: Float = 0f,
    val text: String,
    val category: String,
    val score: Int,
    var isPinned: Boolean = true,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var bounceCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"
    val database = AppDatabase.getDatabase(application)
    val repository = AppRepository(database.appDao())

    // --- Active User States ---
    val isLoggedIn = MutableStateFlow(false) // Start logged out so user can log in
    val isDarkMode = MutableStateFlow(false) // Dark mode toggle
    val profileVisibilityPublic = MutableStateFlow(true) // Privacy: Profile visibility
    val messageRequestsAnyone = MutableStateFlow(true) // Privacy: Message Requests
    val currentUser = MutableStateFlow("avinaash") // Default logged in user
    val themeMode = MutableStateFlow("InReach") // "InReach" (Navy/Gold) or "INAI" (Sandstone/Temple)
    val selectedWorkspaceId = MutableStateFlow<Int?>(null)
    val activeTab = MutableStateFlow("inbox") // "inbox", "workspaces", "analytics", "passports"
    val isProSubscribed = MutableStateFlow(false) // Single source of truth for subscription state

    // --- Public Profile Viewer ---
    val selectedProfileUsername = MutableStateFlow<String?>("surya_jk")

    // --- Message Composer Questionnaire ---
    val selectedIntentType = MutableStateFlow("Collaboration")
    val questionnaireAnswers = mutableStateListOf<String>()

    // Questionnaire schemas for each Intent type
    val questionnaires = mapOf(
        "Job Offer" to listOf(
            "What is the annual compensation range & equity setup?",
            "What is the target start date & remote/hybrid policy?",
            "Is the position fully verified with a budget sign-off?"
        ),
        "Collaboration" to listOf(
            "What is the mutual scope of work & project objective?",
            "What are the skills or resources you are bringing?",
            "What is the anticipated timeline to complete this joint project?"
        ),
        "Mentorship" to listOf(
            "What specific professional skills or goals are you focused on?",
            "How often do you want to sync (e.g. weekly, monthly)?",
            "Are you open to structured milestones and project assignments?"
        ),
        "Research" to listOf(
            "What is the core scientific or academic problem statement?",
            "Is there any funding, grant, or institutional backing?",
            "What is the publication or commercialization plan?"
        ),
        "Investment" to listOf(
            "What is the investment stage (Pre-seed, Seed, Series A, etc.)?",
            "What is your target funding amount and current committed traction?",
            "Who are the co-investors or lead institutional VCs involved?"
        ),
        "Service Request" to listOf(
            "What is the designated budget limit for this professional service?",
            "What are the core technical constraints or requirements?",
            "What is the hard deadline for resource delivery?"
        ),
        "Speaking Invitation" to listOf(
            "What is the event format (In-person, Keynote, Panel, Virtual)?",
            "Are speaker honorariums, travel, and lodging accommodations provided?",
            "What is the estimated audience size and core professional domain?"
        ),
        "Networking" to listOf(
            "What is the joint mutual interest we possess?",
            "Where did you hear or read about my work?",
            "Is there a warm introduction route or mutual friend connecting us?"
        )
    )

    // --- State Flows from Repository ---
    val profiles: StateFlow<List<ProfileEntity>> = repository.allProfiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val messages: StateFlow<List<MessageEntity>> = repository.allMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val workspaces: StateFlow<List<WorkspaceEntity>> = repository.allWorkspaces.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Physics Simulation for Shake-to-Release
    var isPhysicsActive = MutableStateFlow(false)
    val physicsCards = mutableStateListOf<CardPhysics>()
    private var physicsJob: kotlinx.coroutines.Job? = null

    // Selected message detail view model states
    val activeDraft = MutableStateFlow("")
    val draftGenerating = MutableStateFlow(false)
    val toneScoreResult = MutableStateFlow<Int?>(null)
    val spamCheckResult = MutableStateFlow<String?>(null)

    // Workspace specific operations
    val workspaceChats = MutableStateFlow<List<ChatEntity>>(emptyList())
    val workspaceStickyNotes = MutableStateFlow<List<StickyNoteEntity>>(emptyList())
    val workspaceTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val workspaceMeetings = MutableStateFlow<List<MeetingEntity>>(emptyList())
    val workspaceMilestones = MutableStateFlow<List<MilestoneEntity>>(emptyList())

    init {
        // Seeding database if empty
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                seedDatabase()
                loadWorkspaceDetails()
            } catch (e: Exception) {
                Log.e(TAG, "Database initialization or seeding failed: ${e.message}", e)
            }
        }

        // Observe messages Flow to initialize physics when data loads
        viewModelScope.launch {
            messages.collect { list ->
                if (list.isNotEmpty() && physicsCards.isEmpty() && !isPhysicsActive.value) {
                    initializePhysics()
                }
            }
        }

        // Initialize question format for initial intent style
        updateQuestionnaireAnswers()
    }

    fun updateQuestionnaireAnswers() {
        questionnaireAnswers.clear()
        val questions = questionnaires[selectedIntentType.value] ?: emptyList()
        repeat(questions.size) { questionnaireAnswers.add("") }
    }

    private suspend fun seedDatabase() {
        // Checking profiles
        val avinaashProfileCheck = database.appDao().getProfileByUsername("avinaash")
        val hasProfiles = avinaashProfileCheck != null && avinaashProfileCheck.displayName == "Avinaash A"
        if (!hasProfiles) {
            Log.d(TAG, "New profiles missing or outdated names. Clearing old database tables before seeding...")
            database.clearAllTables()
            Log.d(TAG, "Seeding profiles database...")
            // 1. App user
            database.appDao().insertProfile(
                ProfileEntity(
                    username = "avinaash",
                    displayName = "Avinaash A",
                    bio = "Full-stack builder passionate about Global High-Trust Collaboration and Sandbox environments. Tamil heritage explorer.",
                    openIntents = "Collaboration, Mentorship, Speaking Invitation, Investment, Networking",
                    trustScore = 98,
                    responseRate = 96,
                    verificationTier = 5, // Gov ID
                    availabilityWindows = "Collaboration: 10AM-4PM Mon-Alt Fri",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop",
                    reputationScore = 98
                )
            )

            // 2. Collaborative Peers
            // --- Senders of Inbox Messages in Home ---
            database.appDao().insertProfile(
                ProfileEntity(
                    username = "surya_jk",
                    displayName = "Surya JK",
                    bio = "Indian Traditional Art historian & Generative AI designer. Researching South Indian sandstone temple systems and heritage preservation.",
                    openIntents = "Collaboration, Research",
                    trustScore = 94,
                    responseRate = 91,
                    verificationTier = 4,
                    availabilityWindows = "Research: 2PM-6PM Mon-Wed",
                    avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&h=150&fit=crop",
                    reputationScore = 93
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "hairy_gowtham",
                    displayName = "Hairy Gowtham",
                    bio = "Venture Architect. Backing deep tech ecosystems and developer platforms. Sourcing bold co-founders.",
                    openIntents = "Investment, Networking",
                    trustScore = 89,
                    responseRate = 85,
                    verificationTier = 3,
                    availabilityWindows = "Investment: 12PM-2PM Friday Only",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&h=150&fit=crop",
                    reputationScore = 88
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "shreya_a",
                    displayName = "Shreya A",
                    bio = "UX Designer focusing on delightful user interfaces, dynamic micro-interactions, and Material 3 design systems.",
                    openIntents = "Collaboration, Networking",
                    trustScore = 91,
                    responseRate = 88,
                    verificationTier = 3,
                    availabilityWindows = "Mon-Thu 10AM-4PM",
                    avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&h=150&fit=crop",
                    reputationScore = 90
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "dilli_raj",
                    displayName = "Dilli Raj",
                    bio = "Tech Recruiter seeking top mobile, web and visual engineering talents for innovative collaborative systems.",
                    openIntents = "Collaboration, Speaking Invitation",
                    trustScore = 93,
                    responseRate = 90,
                    verificationTier = 4,
                    availabilityWindows = "Mon-Fri 9AM-5PM",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&h=150&fit=crop",
                    reputationScore = 94
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "shagithiyan",
                    displayName = "Shagithiyan",
                    bio = "Web3 systems architect and smart contract auditor. Building secure sandboxes for collaborative nodes.",
                    openIntents = "Collaboration, Investment",
                    trustScore = 95,
                    responseRate = 93,
                    verificationTier = 4,
                    availabilityWindows = "Daily 11AM-5PM",
                    avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&h=150&fit=crop",
                    reputationScore = 95
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "ramya",
                    displayName = "Ramya",
                    bio = "Linguist & Semantic AI engineer. Restoring ancient Tamil iconography and sandstone tablets with LLMs.",
                    openIntents = "Collaboration, Research",
                    trustScore = 96,
                    responseRate = 95,
                    verificationTier = 5,
                    availabilityWindows = "Mon-Alt Fri 9AM-2PM",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&h=150&fit=crop",
                    reputationScore = 96
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "harshitha",
                    displayName = "Harshitha",
                    bio = "Platform Product Manager. Designing high-trust escrow workflows, verified credentials, and ID attestations.",
                    openIntents = "Collaboration, Speaking Invitation",
                    trustScore = 94,
                    responseRate = 92,
                    verificationTier = 3,
                    availabilityWindows = "Tue-Thu 3PM-6PM",
                    avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150&h=150&fit=crop",
                    reputationScore = 94
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "kumar_spam",
                    displayName = "Kumar Token Master",
                    bio = "Sending unsolicited automated token offerings. Generating mass hype fast.",
                    openIntents = "Networking, Investment",
                    trustScore = 28,
                    responseRate = 95,
                    verificationTier = 1,
                    availabilityWindows = "Always Open",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&h=150&fit=crop",
                    reputationScore = 35
                )
            )

            // --- Connected Peers in Connections & Workspaces ---
            database.appDao().insertProfile(
                ProfileEntity(
                    username = "siddarth_p",
                    displayName = "Siddarth P",
                    bio = "Senior Graphics Developer working on high custom-drawing canvas and physical-drag mechanics.",
                    openIntents = "Collaboration, Research",
                    trustScore = 97,
                    responseRate = 96,
                    verificationTier = 5,
                    availabilityWindows = "Daily 10AM-6PM",
                    avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&h=150&fit=crop",
                    reputationScore = 98
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "dhanajeyan_j",
                    displayName = "Dhanajeyan J",
                    bio = "Creative Frontend Specialist. Custom design systems, responsive layouts, and edge-to-edge Compose aesthetics.",
                    openIntents = "Collaboration, Networking",
                    trustScore = 95,
                    responseRate = 94,
                    verificationTier = 4,
                    availabilityWindows = "Daily 1PM-5PM",
                    avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150&h=150&fit=crop",
                    reputationScore = 96
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "gopi_d",
                    displayName = "Gopi D",
                    bio = "Rust & Native expert building modular backend routers and secure sandbox channels.",
                    openIntents = "Collaboration, Networking",
                    trustScore = 92,
                    responseRate = 91,
                    verificationTier = 3,
                    availabilityWindows = "Mon-Fri 2PM-5PM",
                    avatarUrl = "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=150&h=150&fit=crop",
                    reputationScore = 93
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "santhosh_s",
                    displayName = "Santhosh S",
                    bio = "Community Advocate. Cultivating decentralized groups of high-trust sandstone modelers.",
                    openIntents = "Collaboration, Speaking Invitation",
                    trustScore = 94,
                    responseRate = 93,
                    verificationTier = 4,
                    availabilityWindows = "Wed-Fri 11AM-4PM",
                    avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&h=150&fit=crop",
                    reputationScore = 95
                )
            )

            // --- Peers in Explore registry ---
            database.appDao().insertProfile(
                ProfileEntity(
                    username = "aswin",
                    displayName = "Aswin",
                    bio = "Full-Stack Dev passionate about Android, Node.js sandboxes, and Tamil typography mapping.",
                    openIntents = "Collaboration, Networking",
                    trustScore = 91,
                    responseRate = 89,
                    verificationTier = 3,
                    availabilityWindows = "Mon-Fri 9AM-1PM",
                    avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop",
                    reputationScore = 90
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "haris",
                    displayName = "Haris",
                    bio = "AI Scientist training compact on-device models for spam scoring and intent categorization.",
                    openIntents = "Collaboration, Research",
                    trustScore = 93,
                    responseRate = 92,
                    verificationTier = 4,
                    availabilityWindows = "Mon-Wed 10AM-4PM",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&h=150&fit=crop",
                    reputationScore = 94
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "logithakshan",
                    displayName = "Logithakshan",
                    bio = "System admin organizing high-availability networks and decentralized DHT node indexing.",
                    openIntents = "Collaboration, Networking",
                    trustScore = 89,
                    responseRate = 87,
                    verificationTier = 3,
                    availabilityWindows = "Daily 3PM-7PM",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&h=150&fit=crop",
                    reputationScore = 89
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "karthik_raja",
                    displayName = "Karthik Raja",
                    bio = "Mobile Interaction Designer. Perfecting custom Material 3 ripples and edge-to-edge layouts.",
                    openIntents = "Collaboration, Mentorship",
                    trustScore = 95,
                    responseRate = 94,
                    verificationTier = 4,
                    availabilityWindows = "Tue-Wed 2PM-6PM",
                    avatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=150&h=150&fit=crop",
                    reputationScore = 96
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "abishek_mr",
                    displayName = "Abishek MR",
                    bio = "DevOps Lead scaling secure decentralized workspaces across global peer-to-peer networks.",
                    openIntents = "Collaboration, Networking",
                    trustScore = 92,
                    responseRate = 90,
                    verificationTier = 4,
                    availabilityWindows = "Mon-Fri 4PM-8PM",
                    avatarUrl = "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?w=150&h=150&fit=crop",
                    reputationScore = 91
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "deepika_r",
                    displayName = "Deepika R",
                    bio = "FinTech Product Architect designing secure escrow lockers and multi-tier ID verification grids.",
                    openIntents = "Investment, Networking",
                    trustScore = 94,
                    responseRate = 93,
                    verificationTier = 5,
                    availabilityWindows = "Daily 10AM-12PM",
                    avatarUrl = "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?w=150&h=150&fit=crop",
                    reputationScore = 95
                )
            )

            database.appDao().insertProfile(
                ProfileEntity(
                    username = "preethi_k",
                    displayName = "Preethi K",
                    bio = "Frontend Architect crafting responsive, accessible visual portals and dynamic multi-window grids.",
                    openIntents = "Collaboration, Speaking Invitation",
                    trustScore = 93,
                    responseRate = 91,
                    verificationTier = 3,
                    availabilityWindows = "Mon-Thu 9AM-4PM",
                    avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=150&h=150&fit=crop",
                    reputationScore = 93
                )
            )

            // Seed Some Messages (Inboxes)
            Log.d(TAG, "Seeding messages...")
            val answersSurya = JSONArray().apply {
                put("A Joint sandstone inscription digital restoration paper & interactive Web3 gallery.")
                put("Technical pipeline using GANs mapping classical Kolam formulas.")
                put("6 months target project length.")
            }.toString()

            database.appDao().insertMessage(
                MessageEntity(
                    id = 1,
                    senderName = "Surya JK",
                    senderUsername = "surya_jk",
                    recipientUsername = "avinaash",
                    intent = "Collaboration",
                    questionnaireAnswers = answersSurya,
                    rawText = "I read your essay about digitalizing ancient classical Tamil iconography. I have access to a rich manuscript archive and want to explore mapping them to generative canvas designs using AI logic. Let's build a joint restoration project!",
                    status = "UNREAD",
                    priorityScore = 92,
                    toneScore = 96,
                    spamScore = 2,
                    summary = "Joint restoration modeling of classical Tamil script and iconography using neural meshes.",
                    smartReplies = "Let's co-author this!,Schedule alignment video call,Send archive details",
                    escrowHoursRemaining = 0
                )
            )

            // Escrow Refinement Message Placeholder
            val answersGowtham = JSONArray().apply {
                put("Pre-Seed venture backing loop.")
                put("Looking for tech founder mapping high performance compute frameworks.")
                put("Target raised: $450k with lead commitment.")
            }.toString()

            database.appDao().insertMessage(
                MessageEntity(
                    id = 2,
                    senderName = "Hairy Gowtham",
                    senderUsername = "hairy_gowtham",
                    recipientUsername = "avinaash",
                    intent = "Investment",
                    questionnaireAnswers = answersGowtham,
                    rawText = "Avinaash, we are scouting for visual systems core builder to co-found the next sandstone rendering platform. We have a solid term sheet and want to escrow this inquiry while your profile is finishing verification.",
                    status = "ESCROW",
                    priorityScore = 84,
                    toneScore = 90,
                    spamScore = 12,
                    summary = "Pre-seed co-founder scouting call for cloud visualization pipeline.",
                    smartReplies = "Unlock workspace now,Review term sheet,Schedule brief intro",
                    escrowHoursRemaining = 32
                )
            )

            // Unread messages to showcase "Shake to Release" animation
            database.appDao().insertMessage(
                MessageEntity(
                    id = 3,
                    senderName = "Shreya A",
                    senderUsername = "shreya_a",
                    recipientUsername = "avinaash",
                    intent = "Speaking Invitation",
                    questionnaireAnswers = "[\"Panelist discussion on Tamil Digital Humanities\",\"Speaker stipend coverage\",\"Estimated 400 college attendees\"]",
                    rawText = "We would love to invite you to speak at the Chennai Digital Renaissance panel this upcoming winter. Our theme focuses on classical design systems in modern mobile and web technology.",
                    status = "UNREAD",
                    priorityScore = 89,
                    toneScore = 94,
                    spamScore = 4,
                    summary = "Invitation to keynote at Chennai Digital Renaissance event.",
                    smartReplies = "I would be honored!,Let's discuss dates,Send travel arrangements",
                    escrowHoursRemaining = 0
                )
            )

            database.appDao().insertMessage(
                MessageEntity(
                    id = 4,
                    senderName = "Kumar Token Master",
                    senderUsername = "kumar_spam",
                    recipientUsername = "avinaash",
                    intent = "Investment",
                    questionnaireAnswers = "[\"Seed stage hypergrowth\",\"Uncapped meme tokens\",\"Instantly!\"]",
                    rawText = "HEY BOSS check out this AMAZING brand new token pre-sale launch! 10000x potential easy money trust the system! Click right away, limited spots active, don't miss!",
                    status = "UNREAD",
                    priorityScore = 21,
                    toneScore = 32,
                    spamScore = 97,
                    summary = "Spam crypto token pre-sale launch offer.",
                    smartReplies = "Identify sender risk,Flag as spam immediately,Mute username",
                    escrowHoursRemaining = 0
                )
            )

            // Seed an Accepted message that already has an unlocked workspace!
            val answersAlreadyAccepted = JSONArray().apply {
                put("Interactive local sandbox architecture.")
                put("UI expertise in modern material design.")
                put("ASAP launch.")
            }.toString()

            database.appDao().insertMessage(
                MessageEntity(
                    id = 5,
                    senderName = "Siddarth P",
                    senderUsername = "siddarth_p",
                    recipientUsername = "avinaash",
                    intent = "Collaboration",
                    questionnaireAnswers = answersAlreadyAccepted,
                    rawText = "Let's design a sandstone-carved digital tablet application honoring ancient heritage manuscripts with actual responsive physics UI layout.",
                    status = "ACCEPTED",
                    priorityScore = 95,
                    toneScore = 95,
                    spamScore = 1,
                    summary = "Sandstone manuscript aesthetic layout development workspace.",
                    smartReplies = "Launch Workspace",
                    escrowHoursRemaining = 0
                )
            )

            database.appDao().insertMessage(
                MessageEntity(
                    id = 6,
                    senderName = "Dilli Raj",
                    senderUsername = "dilli_raj",
                    recipientUsername = "avinaash",
                    intent = "Collaboration",
                    questionnaireAnswers = "[\"Senior visual and application engineer\",\"Material design proficiency\",\"ASAP starting window\"]",
                    rawText = "Hi! I came across your profile and I am super impressed by your experience with React, Compose and classical design layout preservation and alignment. We are hiring senior builders for high-trust systems.",
                    status = "PENDING",
                    priorityScore = 93,
                    toneScore = 96,
                    spamScore = 2,
                    summary = "Senior visual engineering role for high-trust interfaces.",
                    smartReplies = "Let's schedule a call,Send detailed JD,Nice to meet you",
                    escrowHoursRemaining = 0
                )
            )

            database.appDao().insertMessage(
                MessageEntity(
                    id = 7,
                    senderName = "Dhanajeyan J",
                    senderUsername = "dhanajeyan_j",
                    recipientUsername = "avinaash",
                    intent = "Collaboration",
                    questionnaireAnswers = "[\"Open-source sandstone rendering project\",\"Collaborative joint modules\",\"Active repo access\"]",
                    rawText = "Hello! I am working on an open-source project and would absolutely love to collaborate with you. Your expertise would be extremely valuable to design sandstone-carved tablet physics models.",
                    status = "ACCEPTED",
                    priorityScore = 95,
                    toneScore = 98,
                    spamScore = 1,
                    summary = "Open-source sandstone graphics rendering pipeline collaboration.",
                    smartReplies = "Launch Workspace,Review repository link,Discuss requirements",
                    escrowHoursRemaining = 0
                )
            )

            database.appDao().insertMessage(
                MessageEntity(
                    id = 8,
                    senderName = "Harshitha",
                    senderUsername = "harshitha",
                    recipientUsername = "avinaash",
                    intent = "Collaboration",
                    questionnaireAnswers = "[\"Delightful dynamic micro-interactions\",\"M3 design system spec\",\"Remote feedback loop\"]",
                    rawText = "Hey there! I am designing rich micro-interactions for some Indian Heritage digital canvases and wanted to run some UI/UX layout proposals by you.",
                    status = "UNREAD",
                    priorityScore = 91,
                    toneScore = 90,
                    spamScore = 3,
                    summary = "Canvas micro-interactions and high fidelity design system discussion.",
                    smartReplies = "Show me the designs,Schedule a call,Happy to help",
                    escrowHoursRemaining = 0
                )
            )

            // Seed an active workspace for Dhanajeyan J as well!
            database.appDao().insertWorkspace(
                WorkspaceEntity(
                    id = 2,
                    messageId = 7,
                    originalIntent = "Collaboration",
                    recipientUsername = "avinaash",
                    senderUsername = "dhanajeyan_j",
                    title = "Assets & Prototype with Dhanajeyan J",
                    docContent = "Let's build sandstone-carved digital tablet application honoring ancient heritage manuscripts with actual responsive physics UI layout.\n\nProject proposal drafts active here with Dhanajeyan.",
                    proofOfWorkSummary = "In progress. Sandstone manuscript design asset pipeline set in workspace draft editor."
                )
            )

            // Build Workspace logic
            val wsId = database.appDao().insertWorkspace(
                WorkspaceEntity(
                    id = 1,
                    messageId = 5,
                    originalIntent = "Collaboration",
                    recipientUsername = "avinaash",
                    senderUsername = "siddarth_p",
                    title = "Sandstone Physics Workspace with Siddarth P",
                    docContent = "# Sandstone Digital Restoration Plan with Siddarth P\nInspired by Tamil heritage, we seek to layout the ultimate sensory UI.\n- Layer 1: Warm sandstone textures (#EADAC2).\n- Layer 2: Brass accents mimicking carved golden plaques.\n- Layer 3: Reactive physics widgets resembling falling palm leaves.",
                    proofOfWorkSummary = "In progress. Sandstone manuscript design asset pipeline set in workspace draft editor. Collaborators successfully synchronized on milestone deliverables."
                )
            ).toInt()

            // Seed Workspace details
            // 1. Threaded chats
            database.appDao().insertChat(
                ChatEntity(
                    workspaceId = wsId,
                    senderName = "Siddarth P",
                    messageText = "Welcome to the workspace! Let's build a carved-tablet layout that feels heavy, permanent, yet fully responsive on Android.",
                    timestamp = System.currentTimeMillis() - 3600000,
                    replyToId = null,
                    emojiReactions = "👍,🔥"
                )
            )
            database.appDao().insertChat(
                ChatEntity(
                    workspaceId = wsId,
                    senderName = "Avinaash A",
                    messageText = "Agreed! Let's leverage high custom-drawing canvas and physical-drag mechanics. Unread cards can stick to magnetic corner pockets.",
                    timestamp = System.currentTimeMillis() - 1800000,
                    replyToId = null,
                    emojiReactions = "❤️"
                )
            )

            // 2. Sticky whiteboards
            database.appDao().insertStickyNote(StickyNoteEntity(workspaceId = wsId, text = "Sandstone Texture #EADAC2", posX = 150f, posY = 150f, colorHex = "#FFF9C4"))
            database.appDao().insertStickyNote(StickyNoteEntity(workspaceId = wsId, text = "Brass Ornaments #D49F43", posX = 450f, posY = 200f, colorHex = "#E8F5E9"))
            database.appDao().insertStickyNote(StickyNoteEntity(workspaceId = wsId, text = "Tamil Kolam Canvas Graphics", posX = 200f, posY = 400f, colorHex = "#E3F2FD"))

            // 3. Columns Kanban
            database.appDao().insertTask(TaskEntity(workspaceId = wsId, title = "Design sandstone carved-tablet border layout", assignee = "Siddarth P", deadline = "May 30", columnStatus = "IN_PROGRESS"))
            database.appDao().insertTask(TaskEntity(workspaceId = wsId, title = "Review structural intent questionnaires", assignee = "Avinaash A", deadline = "June 2", columnStatus = "TODO"))
            database.appDao().insertTask(TaskEntity(workspaceId = wsId, title = "Establish Room database schemas for chats & boards", assignee = "Avinaash A", deadline = "Completed", columnStatus = "DONE"))

            // 4. Meetings scheduler
            database.appDao().insertMeeting(
                MeetingEntity(
                    workspaceId = wsId,
                    title = "Daily Sandstone Sync",
                    timeStr = "Mon-Fri 10:30 AM",
                    agendaText = "1. Align sandstone asset packs (10m)\n2. Test 'Shake to Release' gravity parameters (15m)\n3. Verify multi-tier identity checks (5m)"
                )
            )

            // 5. Milestones milestones
            database.appDao().insertMilestone(MilestoneEntity(workspaceId = wsId, title = "Setup Android Room Offline Cache Architecture", targetDate = "May 25", isCompleted = true))
            database.appDao().insertMilestone(MilestoneEntity(workspaceId = wsId, title = "Integrate Gemini REST Prompt Logic (Flash 3.5)", targetDate = "May 29", isCompleted = true))
            database.appDao().insertMilestone(MilestoneEntity(workspaceId = wsId, title = "Interactive Kolam Canvas Design & Physics Demo", targetDate = "June 5", isCompleted = false))

            Log.d(TAG, "Completed seeding Database.")
        }

        // Initialize Physics Cards array for the unread messages
        initializePhysics()
    }

    // --- Shake to Release Physics Engine ---
    fun initializePhysics() {
        // Find all unread messages
        val unread = messages.value.filter { it.status == "UNREAD" }
        physicsCards.clear()

        unread.forEachIndexed { index, msg ->
            // Pin them initially to corners
            val cornerXState = when (index % 4) {
                0 -> 50f
                1 -> 800f
                2 -> 50f
                else -> 800f
            }
            val cornerYState = when (index % 4) {
                0 -> 100f
                1 -> 150f
                2 -> 800f
                else -> 850f
            }

            physicsCards.add(
                CardPhysics(
                    id = msg.id,
                    x = cornerXState,
                    y = cornerYState,
                    vx = 0f,
                    vy = 0f,
                    text = msg.senderName,
                    category = msg.intent,
                    score = msg.priorityScore,
                    isPinned = true
                )
            )
        }
    }

    fun triggerShakeToRelease() {
        physicsJob?.cancel()
        if (physicsCards.isEmpty()) {
            initializePhysics()
        }
        isPhysicsActive.value = true

        // Release the pins
        physicsCards.forEach { card ->
            card.isPinned = false
            // Add initial chaotic velocity from the "shake"
            card.vx = (-30..30).random().toFloat()
            card.vy = (-20..-5).random().toFloat()
            card.vRot = (-15..15).random().toFloat()
        }

        // Run continuous simulation loop
        physicsJob = viewModelScope.launch {
            val gravity = 2.2f
            val friction = 0.96f
            val restitution = 0.55f // bounce coeff
            val screenWidth = 950f
            val screenHeight = 1100f
            val cardWidth = 240f
            val cardHeight = 130f

            for (frames in 1..85) {
                physicsCards.forEach { card ->
                    if (!card.isPinned) {
                        // Apply gravity
                        card.vy += gravity
                        // Apply velocities
                        card.x += card.vx
                        card.y += card.vy
                        card.rotation += card.vRot

                        // Apply damping/friction
                        card.vx *= friction
                        card.vy *= friction
                        card.vRot *= friction

                        // Screen bounds Collision - Ground
                        val bottomLine = screenHeight - cardHeight
                        if (card.y > bottomLine) {
                            card.y = bottomLine
                            card.vy = -card.vy * restitution
                            card.vx *= 0.82f // ground slide friction
                            card.vRot *= 0.5f
                            card.bounceCount++
                        }

                        // Left Wall
                        if (card.x < 10) {
                            card.x = 10f
                            card.vx = -card.vx * restitution
                            card.vRot = -card.vRot * 0.5f
                        }

                        // Right Wall
                        val rightLine = screenWidth - cardWidth
                        if (card.x > rightLine) {
                            card.x = rightLine
                            card.vx = -card.vx * restitution
                            card.vRot = -card.vRot * 0.5f
                        }
                    }
                }
                delay(16) // ~60fps
            }

            // Finally, settle down into AI Sorted layout cleanly!
            val sortedList = physicsCards.sortedByDescending { it.score }
            sortedList.forEachIndexed { index, card ->
                card.isPinned = true
                // Center alignment
                val targetX = (screenWidth - cardWidth) / 2
                val targetY = 220f + (index * 160f)
                
                // Animate to targets
                val originalX = card.x
                val originalY = card.y
                val originalRot = card.rotation

                // Fast linear interpolator
                for (step in 1..10) {
                    val ratio = step / 10f
                    card.x = originalX + (targetX - originalX) * ratio
                    card.y = originalY + (targetY - originalY) * ratio
                    card.rotation = originalRot + (0f - originalRot) * ratio
                    delay(12)
                }

                card.x = targetX
                card.y = targetY
                card.rotation = 0f
            }

            isPhysicsActive.value = false
        }
    }

    fun togglePinnedState() {
        physicsCards.forEach { it.isPinned = !it.isPinned }
        if (physicsCards.any { !it.isPinned }) {
            triggerShakeToRelease()
        } else {
            initializePhysics()
        }
    }


    // --- Intent Questionnaire and Message Posting Flow ---
    fun submitMessageRequest(recipientUsername: String, messageText: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val senderProfile = repository.getProfile(currentUser.value)
                val answersJson = JSONArray(questionnaireAnswers).toString()

                // Run Gemini spam score and indicators in background
                val summaryAndReplies = try {
                    GeminiClient.analyzeMessage(
                        sender = senderProfile?.displayName ?: currentUser.value,
                        intent = selectedIntentType.value,
                        text = messageText
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Gemini analysis error: ${e.message}", e)
                    AiAnalysisResult(
                        priorityScore = 75,
                        toneScore = 85,
                        spamScore = 5,
                        summary = messageText.take(50) + "...",
                        smartReplies = listOf("Interesting, tell me more!", "Let's align this week.", "Thanks, let's proceed.")
                    )
                }

                val newMsg = MessageEntity(
                    senderName = senderProfile?.displayName ?: "User ${currentUser.value}",
                    senderUsername = currentUser.value,
                    recipientUsername = recipientUsername,
                    intent = selectedIntentType.value,
                    questionnaireAnswers = answersJson,
                    rawText = messageText,
                    status = "UNREAD",
                    priorityScore = ((summaryAndReplies.priorityScore * (senderProfile?.reputationScore ?: 80)) / 100).coerceIn(10, 100),
                    toneScore = summaryAndReplies.toneScore,
                    spamScore = summaryAndReplies.spamScore,
                    summary = summaryAndReplies.summary,
                    smartReplies = summaryAndReplies.smartReplies.joinToString(",")
                )

                repository.insertMessage(newMsg)
                initializePhysics()
            } catch (e: Exception) {
                Log.e(TAG, "Error in submitMessageRequest: ${e.message}", e)
            } finally {
                onComplete()
            }
        }
    }


    // --- Recipient Inbox Handling ---
    fun acceptMessage(msg: MessageEntity) {
        viewModelScope.launch {
            val updatedMsg = msg.copy(status = "ACCEPTED")
            repository.updateMessage(updatedMsg)

            // Auto-provision standard joint Workspace
            val workspace = WorkspaceEntity(
                messageId = msg.id,
                originalIntent = msg.intent,
                recipientUsername = msg.recipientUsername,
                senderUsername = msg.senderUsername,
                title = "${msg.intent} Alignment with ${msg.senderName}",
                docContent = "# ${msg.intent} Proposal Proposal\nThis workspace was automatically unlocked upon connection approval.\n\n## Questionnaire Context:\n"
            )

            val newWsId = repository.insertWorkspace(workspace)
            selectedWorkspaceId.value = newWsId

            // Add welcome chats and seed items for this workspace
            repository.insertChat(
                ChatEntity(
                    workspaceId = newWsId,
                    senderName = msg.senderName,
                    messageText = "Hello! Excited to connect around this ${msg.intent} opportunity. I've joined the workspace.",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Seed empty items
            repository.insertStickyNote(StickyNoteEntity(workspaceId = newWsId, text = "Add sticky note ideas here!", posX = 200f, posY = 200f))
            repository.insertTask(TaskEntity(workspaceId = newWsId, title = "Complete primary kickoff meeting", assignee = "Both", deadline = "TBD", columnStatus = "TODO"))
            repository.insertMilestone(MilestoneEntity(workspaceId = newWsId, title = "Kickoff Session Alignment", targetDate = "ASAP", isCompleted = false))

            // Load new details
            loadWorkspaceDetails()
            activeTab.value = "workspaces"
        }
    }

    fun declineMessage(msg: MessageEntity) {
        viewModelScope.launch {
            val updatedMsg = msg.copy(status = "DECLINED")
            repository.updateMessage(updatedMsg)
            initializePhysics()
        }
    }

    fun releaseAllUnreadMessages() {
        viewModelScope.launch {
            val unread = messages.value.filter { it.status == "UNREAD" }
            unread.forEach { msg ->
                val updated = msg.copy(status = "READ")
                repository.updateMessage(updated)
            }
            initializePhysics()
        }
    }


    // --- Workspace Details Loading (Live Flow Updates) ---
    private var workspaceJob: kotlinx.coroutines.Job? = null

    fun loadWorkspaceDetails() {
        workspaceJob?.cancel()
        val wId = selectedWorkspaceId.value
        if (wId != null) {
            workspaceJob = viewModelScope.launch {
                try {
                    launch {
                        repository.getChats(wId).collect { workspaceChats.value = it }
                    }
                    launch {
                        repository.getStickyNotes(wId).collect { workspaceStickyNotes.value = it }
                    }
                    launch {
                        repository.getTasks(wId).collect { workspaceTasks.value = it }
                    }
                    launch {
                        repository.getMeetings(wId).collect { workspaceMeetings.value = it }
                    }
                    launch {
                        repository.getMilestones(wId).collect { workspaceMilestones.value = it }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error collecting workspace details flow: ${e.message}", e)
                }
            }
        }
    }

    fun selectWorkspace(wId: Int) {
        selectedWorkspaceId.value = wId
        loadWorkspaceDetails()
    }


    // --- Workspace Actions ---
    fun sendChatMessage(text: String, replyToId: Int? = null, attachedFileUri: String? = null, voiceSeconds: Int? = null) {
        val wId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            val senderProfile = repository.getProfile(currentUser.value)
            repository.insertChat(
                ChatEntity(
                    workspaceId = wId,
                    senderName = senderProfile?.displayName ?: currentUser.value,
                    messageText = text,
                    replyToId = replyToId,
                    attachedFileUri = attachedFileUri,
                    voiceNoteDuration = voiceSeconds
                )
            )
        }
    }

    fun addStickyNote(text: String, color: String = "#FFF9C4") {
        val wId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            repository.insertStickyNote(
                StickyNoteEntity(
                    workspaceId = wId,
                    text = text,
                    posX = (100..450).random().toFloat(),
                    posY = (150..450).random().toFloat(),
                    colorHex = color
                )
            )
        }
    }

    fun updateStickyNotePosition(note: StickyNoteEntity, x: Float, y: Float) {
        viewModelScope.launch {
            repository.updateStickyNote(note.copy(posX = x, posY = y))
        }
    }

    fun deleteStickyNote(note: StickyNoteEntity) {
        viewModelScope.launch {
            repository.deleteStickyNote(note)
        }
    }

    fun addTaskCard(title: String, assignee: String, deadline: String) {
        val wId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    workspaceId = wId,
                    title = title,
                    assignee = assignee,
                    deadline = deadline,
                    columnStatus = "TODO"
                )
            )
        }
    }

    fun updateTaskStatus(task: TaskEntity, nextStatus: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(columnStatus = nextStatus))
        }
    }

    fun deleteTaskCard(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addMeetingSlot(title: String, timeStr: String, generatedAgenda: String = "") {
        val wId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            repository.insertMeeting(
                MeetingEntity(
                    workspaceId = wId,
                    title = title,
                    timeStr = timeStr,
                    agendaText = generatedAgenda
                )
            )
        }
    }

    fun deleteMeetingSlot(meeting: MeetingEntity) {
        viewModelScope.launch {
            repository.deleteMeeting(meeting)
        }
    }

    fun addMilestoneItem(title: String, targetDate: String) {
        val wId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            repository.insertMilestone(
                MilestoneEntity(
                    workspaceId = wId,
                    title = title,
                    targetDate = targetDate,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleMilestoneItem(milestone: MilestoneEntity) {
        viewModelScope.launch {
            repository.updateMilestone(milestone.copy(isCompleted = !milestone.isCompleted))
        }
    }

    fun deleteMilestoneItem(milestone: MilestoneEntity) {
        viewModelScope.launch {
            repository.deleteMilestone(milestone)
        }
    }

    fun saveDocumentContent(content: String) {
        val wId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            val ws = repository.getWorkspaceById(wId) ?: return@launch
            repository.updateWorkspace(ws.copy(docContent = content))
        }
    }


    // --- Smart AI Assist Flows ---
    fun runToneCheckAndSpamFilter(text: String) {
        viewModelScope.launch {
            try {
                val res = GeminiClient.analyzeMessage(currentUser.value, selectedIntentType.value, text)
                toneScoreResult.value = res.toneScore
                spamCheckResult.value = if (res.spamScore > 60) "High Spam Risk! (${res.spamScore}%)" else "Clean & Authentic Draft"
            } catch (e: Exception) {
                Log.e(TAG, "Failed tone check: ${e.message}", e)
                toneScoreResult.value = 85
                spamCheckResult.value = "Clean & Authentic Draft (Offline mode)"
            }
        }
    }

    fun autoGenerateAiDraft(recipientName: String, extraDetails: String) {
        viewModelScope.launch {
            try {
                draftGenerating.value = true
                val draft = GeminiClient.generateDraft(selectedIntentType.value, recipientName, extraDetails)
                activeDraft.value = draft
            } catch (e: Exception) {
                Log.e(TAG, "Failed AI draft generation: ${e.message}", e)
                activeDraft.value = "Dear $recipientName,\n\nI'm writing regarding a potential alignment around ${selectedIntentType.value}.\n\nBest regards,\nSender"
            } finally {
                draftGenerating.value = false
            }
        }
    }

    fun generateMeetingAgendaWithAi(meetingTitle: String) {
        val wId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            try {
                val notesStr = workspaceStickyNotes.value.joinToString("; ") { it.text } + " | Draft proposals: " + (repository.getWorkspaceById(wId)?.docContent ?: "")
                val agenda = GeminiClient.generateMeetingAgenda(selectedIntentType.value, meetingTitle, notesStr)
                addMeetingSlot(meetingTitle, "Scheduled Slots via InReach", agenda)
            } catch (e: Exception) {
                Log.e(TAG, "Failed meeting agenda: ${e.message}", e)
                addMeetingSlot(meetingTitle, "Scheduled Slots via InReach", "1. Introductions\n2. Design discussion")
            }
        }
    }

    fun summarizeWorkspaceProofOfWork() {
        val wId = selectedWorkspaceId.value ?: return
        viewModelScope.launch {
            try {
                val notesStr = "Active Tasks: " + workspaceTasks.value.joinToString(", ") { "${it.title} [${it.columnStatus}]" } +
                        "  Sticky Ideas: " + workspaceStickyNotes.value.joinToString("; ") { it.text }
                val summary = GeminiClient.summarizeWorkspace(notesStr)
                val ws = repository.getWorkspaceById(wId) ?: return@launch
                repository.updateWorkspace(ws.copy(proofOfWorkSummary = summary))
            } catch (e: Exception) {
                Log.e(TAG, "Failed proof of work: ${e.message}", e)
                val ws = repository.getWorkspaceById(wId) ?: return@launch
                repository.updateWorkspace(ws.copy(proofOfWorkSummary = "Active joint alignment in progress. Verified high-trust collaboration."))
            }
        }
    }

    fun toggleTheme() {
        themeMode.value = if (themeMode.value == "InReach") "INAI" else "InReach"
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.insertProfile(profile)
        }
    }

    // --- TASK 1: Real Authentication Success Handle ---
    fun handleAuthenticationSuccess(email: String, name: String, profilePicUrl: String?) {
        viewModelScope.launch {
            val username = email.substringBefore("@").lowercase().replace(".", "")
            val existing = repository.getProfile(username)
            if (existing == null) {
                val newProfile = ProfileEntity(
                    username = username,
                    displayName = name,
                    bio = "InReach peer node. Gateway builder and regional lead designer.",
                    openIntents = "Job Offer, Collaboration, Mentorship, Research",
                    trustScore = 95,
                    responseRate = 98,
                    verificationTier = 1,
                    availabilityWindows = "{\"intentCategory\":\"Collaboration\",\"startTime\":\"09:00\",\"endTime\":\"17:00\",\"activeDays\":[2,3,4,5,6]}",
                    avatarUrl = profilePicUrl ?: "",
                    reputationScore = 80
                )
                repository.insertProfile(newProfile)
                currentUser.value = username
            } else {
                currentUser.value = username
            }
            isLoggedIn.value = true
        }
    }

    suspend fun getProfileDirect(username: String): ProfileEntity? {
        return repository.getProfile(username)
    }

    // --- TASK 3: Availability Windows Parser & Checks ---
    data class AvailabilityWindow(
        val intentCategory: String = "Collaboration",
        val startTime: String = "09:00",
        val endTime: String = "17:00",
        val activeDays: List<Int> = listOf(2, 3, 4, 5, 6) // Mon to Fri
    )

    fun parseAvailabilityWindows(json: String): AvailabilityWindow {
        return try {
            val jsonObject = org.json.JSONObject(json)
            val intentCategory = jsonObject.optString("intentCategory", "Collaboration")
            val startTime = jsonObject.optString("startTime", "09:00")
            val endTime = jsonObject.optString("endTime", "17:00")
            val daysArray = jsonObject.optJSONArray("activeDays")
            val activeDays = mutableListOf<Int>()
            if (daysArray != null) {
                for (i in 0 until daysArray.length()) {
                    activeDays.add(daysArray.getInt(i))
                }
            } else {
                activeDays.addAll(listOf(2, 3, 4, 5, 6))
            }
            AvailabilityWindow(intentCategory, startTime, endTime, activeDays)
        } catch (e: Exception) {
            AvailabilityWindow()
        }
    }

    fun checkRecipientAvailabilitySync(recipientUsername: String, intentCategory: String): Boolean {
        val profile = profiles.value.find { it.username == recipientUsername } ?: return true
        val windowJson = profile.availabilityWindows
        if (!windowJson.startsWith("{")) {
            return true
        }
        
        val window = parseAvailabilityWindows(windowJson)
        if (window.intentCategory != intentCategory) {
            return true
        }
        
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        
        if (!window.activeDays.contains(dayOfWeek)) {
            return false
        }
        
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMin = calendar.get(java.util.Calendar.MINUTE)
        val currentTimeMins = currentHour * 60 + currentMin
        
        val startParts = window.startTime.split(":")
        val startHour = startParts.getOrNull(0)?.toIntOrNull() ?: 0
        val startMin = startParts.getOrNull(1)?.toIntOrNull() ?: 0
        val startTimeMins = startHour * 60 + startMin
        
        val endParts = window.endTime.split(":")
        val endHour = endParts.getOrNull(0)?.toIntOrNull() ?: 23
        val endMin = endParts.getOrNull(1)?.toIntOrNull() ?: 59
        val endTimeMins = endHour * 60 + endMin
        
        return currentTimeMins in startTimeMins..endTimeMins
    }

    fun isAvailableNow(intentCategory: String): Boolean {
        val target = selectedProfileUsername.value ?: return true
        return checkRecipientAvailabilitySync(target, intentCategory)
    }

    // --- TASK 4: Sender Reputation score tracking ---
    fun updateSenderReputation(senderUsername: String, action: String) {
        val pointsChange = when (action) {
            "THUMBS_UP" -> 2
            "THUMBS_DOWN" -> -3
            "REPORT_SPAM" -> -10
            else -> 0
        }
        viewModelScope.launch {
            val p = repository.getProfile(senderUsername) ?: return@launch
            val score = (p.reputationScore + pointsChange).coerceIn(0, 100)
            repository.insertProfile(p.copy(reputationScore = score))
        }
    }

    fun updateVerificationTier(username: String, tier: Int) {
        viewModelScope.launch {
            val p = repository.getProfile(username) ?: return@launch
            repository.insertProfile(p.copy(verificationTier = tier))
        }
    }

    // --- TASK 5: Live Analytics flow ---
    data class AnalyticsUiState(
        val totalMessagesCount: Int = 0,
        val messagesByCategory: Map<String, Int> = emptyMap(),
        val messagesByWeek: List<Int> = emptyList(),
        val messagesByStatus: Map<String, Int> = emptyMap(),
        val averageReplyTimeHrs: Double = 0.0
    )

    val analyticsUiState = messages.map { list ->
        val total = list.size
        val byCategory = list.groupBy { it.intent }.mapValues { it.value.size }
        val byStatus = list.groupBy { it.status }.mapValues { it.value.size }
        val acceptedCount = byStatus["ACCEPTED"] ?: 0
        val avgTime = if (acceptedCount > 0) 2.1 else 4.8
        
        val weeks = MutableList(12) { 0 }
        list.forEachIndexed { i, msg ->
            val wIdx = (i % 12)
            weeks[wIdx] = weeks[wIdx] + 1
        }
        if (weeks.all { it == 0 }) {
            weeks[0] = 5; weeks[2] = 12; weeks[4] = 8; weeks[6] = 15; weeks[8] = 9; weeks[10] = 14
        }
        
        AnalyticsUiState(
            totalMessagesCount = total,
            messagesByCategory = byCategory,
            messagesByWeek = weeks,
            messagesByStatus = byStatus,
            averageReplyTimeHrs = avgTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    // --- TASK 7: Warm Introduction System Engine ---
    val warmIntroRequests = MutableStateFlow<List<WarmIntroRequest>>(emptyList())
    
    fun loadWarmIntros(username: String) {
        viewModelScope.launch {
            repository.getWarmIntroRequestsByMutual(username).collect {
                warmIntroRequests.value = it
            }
        }
    }

    fun submitWarmIntro(sender: String, mutual: String, target: String, message: String) {
        viewModelScope.launch {
            val senderProfile = repository.getProfile(sender) ?: return@launch
            val targetProfile = repository.getProfile(target) ?: return@launch
            val req = WarmIntroRequest(
                senderUserId = sender,
                senderName = senderProfile.displayName,
                mutualUserId = mutual,
                targetUserId = target,
                targetName = targetProfile.displayName,
                introMessage = message,
                status = "PENDING"
            )
            repository.insertWarmIntroRequest(req)
        }
    }

    fun approveWarmIntro(req: WarmIntroRequest) {
        viewModelScope.launch {
            repository.updateWarmIntroRequestStatus(req.id, "APPROVED")
            val introText = "Warm introduction authorized by mutual contact ${req.mutualUserId}. Connection Pitch: ${req.introMessage}"
            val msg = MessageEntity(
                senderName = req.senderName,
                senderUsername = req.senderUserId,
                recipientUsername = req.targetUserId,
                intent = "Warm Introduction",
                questionnaireAnswers = "[\"Warmly introduced by Mutual Node\"]",
                rawText = introText,
                status = "UNREAD",
                priorityScore = 90
            )
            repository.insertMessage(msg)
        }
    }

    fun declineWarmIntro(req: WarmIntroRequest) {
        viewModelScope.launch {
            repository.updateWarmIntroRequestStatus(req.id, "DECLINED")
        }
    }
}
