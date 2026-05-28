package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InReachApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State flows
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val currentTheme by viewModel.themeMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val workspacesByFlow by viewModel.workspaces.collectAsState()

    val chats by viewModel.workspaceChats.collectAsState()
    val stickyNotes by viewModel.workspaceStickyNotes.collectAsState()
    val tasks by viewModel.workspaceTasks.collectAsState()
    val meetings by viewModel.workspaceMeetings.collectAsState()
    val milestones by viewModel.workspaceMilestones.collectAsState()

    val activeDraftState by viewModel.activeDraft.collectAsState()
    val draftGeneratingState by viewModel.draftGenerating.collectAsState()
    val toneScoreState by viewModel.toneScoreResult.collectAsState()
    val spamCheckResultState by viewModel.spamCheckResult.collectAsState()

    val selectedProfileUsername by viewModel.selectedProfileUsername.collectAsState()
    val selectedIntentType by viewModel.selectedIntentType.collectAsState()
    val questionnaireAnswers = viewModel.questionnaireAnswers

    val isPhysicsActive by viewModel.isPhysicsActive.collectAsState()
    val selectedWorkspaceId by viewModel.selectedWorkspaceId.collectAsState()

    // Active visual settings based on theme
    val isInai = currentTheme == "INAI"

    // Theme values (Responsive Light/Dark across both Global Slate and Regional Temple themes!)
    val primaryBg = if (isInai) {
        if (isDark) Color(0xFF211510) else Color(0xFFF4EBE1)
    } else {
        if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    }

    val containerBg = if (isInai) {
        if (isDark) Color(0xFF322019) else Color(0xFFEADAC2)
    } else {
        if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    }

    val cardBg = if (isInai) {
        if (isDark) Color(0xFF2D1E18) else Color(0xFFF5ECE1)
    } else {
        if (isDark) Color(0xFF0D1B2A) else Color(0xFFF1F5F9)
    }

    val overlayBg = if (isInai) {
        if (isDark) Color(0xFF3E2921) else Color(0xFFF2E6D5)
    } else {
        if (isDark) Color(0xFF112240) else Color(0xFFE2E8F0)
    }

    val primaryText = if (isInai) {
        if (isDark) Color(0xFFF5ECE1) else Color(0xFF3E2723)
    } else {
        if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    }

    val secondaryText = if (isInai) {
        if (isDark) Color(0xFFBCAAA4) else Color(0xFF5D4037)
    } else {
        if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    }

    val accentGold = if (isInai) {
        if (isDark) Color(0xFFE5A93C) else Color(0xFFD49F43)
    } else {
        if (isDark) Color(0xFFE8B84B) else Color(0xFF2563EB)
    }

    val borderStroke = if (isInai) {
        if (isDark) Color(0xFF5D4037) else Color(0xFF8D6E63)
    } else {
        if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    }

    // Form controllers
    var isWritingMessage by remember { mutableStateOf(false) }
    var inputMessageText by remember { mutableStateOf("") }
    var inputExtraDetails by remember { mutableStateOf("") }
    var selectedMessageForDetail by remember { mutableStateOf<MessageEntity?>(null) }
    var showProofOfWorkDialog by remember { mutableStateOf(false) }

    // Toggle for physics
    var isPhysicsEnabled by remember { mutableStateOf(true) }

    // Multi-Language target indicator
    var translateToTamilState by remember { mutableStateOf(false) }

    // Settings and auth controllers
    var showSettingsScreen by remember { mutableStateOf(false) }
    var currentAuthScreen by remember { mutableStateOf("login") }

    if (!isLoggedIn) {
        AuthLayout(
            isInai = isInai,
            isDark = isDark,
            primaryBg = primaryBg,
            containerBg = containerBg,
            cardBg = cardBg,
            primaryText = primaryText,
            secondaryText = secondaryText,
            accentGold = accentGold,
            borderStroke = borderStroke,
            currentScreen = currentAuthScreen,
            onScreenChange = { currentAuthScreen = it },
            onLoginSuccess = { email, password ->
                viewModel.isLoggedIn.value = true
                Toast.makeText(context, "Logged in as ${viewModel.currentUser.value}!", Toast.LENGTH_SHORT).show()
            },
            onSignUpSuccess = { name, email, password ->
                viewModel.isLoggedIn.value = true
                Toast.makeText(context, "Account created! Welcome, $name", Toast.LENGTH_SHORT).show()
            },
            onGoogleSignIn = {
                viewModel.isLoggedIn.value = true
                Toast.makeText(context, "Authenticated via Google securely.", Toast.LENGTH_SHORT).show()
            }
        )
        return
    }

    if (showSettingsScreen) {
        SettingsScreen(
            isInai = isInai,
            isDark = isDark,
            primaryBg = primaryBg,
            containerBg = containerBg,
            cardBg = cardBg,
            primaryText = primaryText,
            secondaryText = secondaryText,
            accentGold = accentGold,
            borderStroke = borderStroke,
            viewModel = viewModel,
            onClose = { showSettingsScreen = false },
            onLogout = {
                viewModel.isLoggedIn.value = false
                viewModel.activeTab.value = "inbox"
                showSettingsScreen = false
                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
        )
        return
    }

    // UI Structure
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(containerBg)
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Unique InReach title reflecting INAI Sandstone manuscript and classical text
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isInai) {
                            Text(
                                text = "இன்-ரீச் ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentGold,
                                fontFamily = FontFamily.Serif
                            )
                        }
                        Text(
                            text = "InReach",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }

                    // Simple verified connection badge
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Safe Sandbox",
                        tint = accentGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Decorative Kolam wave border in INAI mode
                if (isInai) {
                    DrawingKolamBorderHorizontal(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp),
                        color = accentGold
                    )
                } else {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(borderStroke)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = containerBg,
                contentColor = accentGold,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeTab == "inbox",
                    onClick = { viewModel.activeTab.value = "inbox" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentGold,
                        unselectedIconColor = secondaryText,
                        indicatorColor = accentGold.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "passports",
                    onClick = { viewModel.activeTab.value = "passports" },
                    icon = { Icon(Icons.Default.TravelExplore, contentDescription = "Explore") },
                    label = { Text("Explore", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentGold,
                        unselectedIconColor = secondaryText,
                        indicatorColor = accentGold.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "connections",
                    onClick = { viewModel.activeTab.value = "connections" },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Connections") },
                    label = { Text("Connections", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentGold,
                        unselectedIconColor = secondaryText,
                        indicatorColor = accentGold.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "analytics",
                    onClick = { viewModel.activeTab.value = "analytics" },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentGold,
                        unselectedIconColor = secondaryText,
                        indicatorColor = accentGold.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { viewModel.activeTab.value = "profile" },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentGold,
                        unselectedIconColor = secondaryText,
                        indicatorColor = accentGold.copy(alpha = 0.2f)
                    )
                )
            }
        },
        containerColor = primaryBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(primaryBg)
        ) {
            when (activeTab) {
                "inbox" -> {
                    InboxTabContent(
                        isInai = isInai,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        accentGold = accentGold,
                        cardBg = cardBg,
                        borderStroke = borderStroke,
                        messages = messages.filter { it.recipientUsername == currentUser },
                        selectedMessageDetail = selectedMessageForDetail,
                        onMessageSelect = { selectedMessageForDetail = it },
                        isPhysicsEnabled = isPhysicsEnabled,
                        onPhysicsToggle = { isPhysicsEnabled = it },
                        isPhysicsActive = isPhysicsActive,
                        physicsCards = viewModel.physicsCards,
                        onTriggerShake = { viewModel.triggerShakeToRelease() },
                        onAccept = { msg ->
                            viewModel.acceptMessage(msg)
                            selectedMessageForDetail = null
                        },
                        onDecline = { msg ->
                            viewModel.declineMessage(msg)
                            selectedMessageForDetail = null
                        },
                        viewModel = viewModel
                    )
                }

                "workspaces" -> {
                    WorkspaceTabContent(
                        isInai = isInai,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        accentGold = accentGold,
                        cardBg = cardBg,
                        overlayBg = overlayBg,
                        borderStroke = borderStroke,
                        workspaces = workspacesByFlow.filter { it.recipientUsername == currentUser || it.senderUsername == currentUser },
                        selectedWorkspaceId = selectedWorkspaceId,
                        onSelectWorkspace = { viewModel.selectWorkspace(it) },
                        chats = chats,
                        stickyNotes = stickyNotes,
                        tasks = tasks,
                        meetings = meetings,
                        milestones = milestones,
                        viewModel = viewModel,
                        showProofOfWork = { showProofOfWorkDialog = true }
                    )
                }

                "passports" -> {
                    PassportsTabContent(
                        isInai = isInai,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        accentGold = accentGold,
                        cardBg = cardBg,
                        borderStroke = borderStroke,
                        profiles = profiles,
                        selectedProfileUsername = selectedProfileUsername,
                        onSelectProfile = { viewModel.selectedProfileUsername.value = it },
                        isWritingMessage = isWritingMessage,
                        onOpenComposer = { isWritingMessage = true },
                        onCloseComposer = { isWritingMessage = false },
                        selectedIntentType = selectedIntentType,
                        onSelectIntentType = { intent ->
                            viewModel.selectedIntentType.value = intent
                            viewModel.updateQuestionnaireAnswers()
                        },
                        questionnaireAnswers = questionnaireAnswers,
                        questionnaireSchema = viewModel.questionnaires[selectedIntentType] ?: emptyList(),
                        inputMessageText = inputMessageText,
                        onMessageChange = { inputMessageText = it },
                        inputExtraDetails = inputExtraDetails,
                        onExtraDetailsChange = { inputExtraDetails = it },
                        activeDraft = activeDraftState,
                        draftGenerating = draftGeneratingState,
                        toneScore = toneScoreState,
                        spamScoreText = spamCheckResultState,
                        onToneCheck = { viewModel.runToneCheckAndSpamFilter(inputMessageText) },
                        onAiDraft = { name, tags -> viewModel.autoGenerateAiDraft(name, tags) },
                        onSubmitMessage = { recipient ->
                            viewModel.submitMessageRequest(recipient, inputMessageText) {
                                isWritingMessage = false
                                inputMessageText = ""
                                viewModel.activeTab.value = "inbox"
                                Toast.makeText(context, "Request sent through warm introduction engine!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }

                "analytics" -> {
                    AnalyticsTabContent(
                        isInai = isInai,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        accentGold = accentGold,
                        cardBg = cardBg,
                        borderStroke = borderStroke,
                        messages = messages
                    )
                }

                "connections" -> {
                    ConnectionsTabContent(
                        isInai = isInai,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        accentGold = accentGold,
                        cardBg = cardBg,
                        borderStroke = borderStroke,
                        profiles = profiles,
                        messages = messages,
                        workspaces = workspacesByFlow,
                        onOpenWorkspace = { wsId ->
                            viewModel.selectWorkspace(wsId)
                            viewModel.activeTab.value = "workspaces"
                        },
                        viewModel = viewModel
                    )
                }

                "profile" -> {
                    ProfileTabContent(
                        isInai = isInai,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        accentGold = accentGold,
                        cardBg = cardBg,
                        borderStroke = borderStroke,
                        currentUser = currentUser,
                        onOpenSettings = { showSettingsScreen = true }
                    )
                }
            }

            // Proof of Work Outcome Summary dialog
            if (showProofOfWorkDialog) {
                var currentWorkspace = workspacesByFlow.find { it.id == selectedWorkspaceId }
                if (currentWorkspace != null) {
                    AlertDialog(
                        onDismissRequest = { showProofOfWorkDialog = false },
                        containerColor = overlayBg,
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = accentGold, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Proof of Work: ${currentWorkspace.title}", color = primaryText, fontSize = 16.sp)
                            }
                        },
                        text = {
                            Column {
                                Text(
                                    text = "This public proof-of-work overview page is generated securely inside the local sandbox. It highlights deliverables achieved with cryptographic trust signatures.",
                                    color = secondaryText,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(cardBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, accentGold, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "DELIVERABLES MATRIX",
                                                color = accentGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "VERIFIED IN-REACH ID",
                                                color = secondaryText,
                                                fontSize = 9.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = currentWorkspace.proofOfWorkSummary.ifEmpty { "Generating proof summary of milestones..." },
                                            color = primaryText,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.summarizeWorkspaceProofOfWork()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = accentGold)
                            ) {
                                Text("AI Regenerate with Gemini", color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showProofOfWorkDialog = false }) {
                                Text("Close Out", color = primaryText)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// INBOX WINDOWS
// ==========================================
@Composable
fun InboxTabContent(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    messages: List<MessageEntity>,
    selectedMessageDetail: MessageEntity?,
    onMessageSelect: (MessageEntity?) -> Unit,
    isPhysicsEnabled: Boolean,
    onPhysicsToggle: (Boolean) -> Unit,
    isPhysicsActive: Boolean,
    physicsCards: List<com.example.CardPhysics>,
    onTriggerShake: () -> Unit,
    onAccept: (MessageEntity) -> Unit,
    onDecline: (MessageEntity) -> Unit,
    viewModel: MainViewModel
) {
    var translateToTamilState by remember { mutableStateOf(false) }
    var translatedSummaryState by remember { mutableStateOf<String?>(null) }
    var translationLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (selectedMessageDetail != null) {
        // Detailed Message & Questionnaire view
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    onMessageSelect(null)
                    translatedSummaryState = null
                    translateToTamilState = false
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
                }
                Box(
                    modifier = Modifier
                        .background(accentGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = selectedMessageDetail.intent.uppercase(),
                        color = accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Message Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(16.dp))
                    .border(2.dp, accentGold, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedMessageDetail.senderName,
                                color = primaryText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${selectedMessageDetail.senderUsername}",
                                color = secondaryText,
                                fontSize = 12.sp
                            )
                        }

                        // Priority rating indicators
                        Box(
                            modifier = Modifier
                                .background(accentGold, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "AI Priority: ${selectedMessageDetail.priorityScore}%",
                                color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = borderStroke)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "1-LINE AI BRIEF OMNI-SUMMARY:",
                        color = accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedMessageDetail.summary.ifEmpty { "Synthesizing briefing..." },
                        color = primaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "INTENT PRE-MESSAGE QUESTIONNAIRE RESPONSES:",
                        color = accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Parse questionnaire responses
                    val answers = try {
                        val arr = org.json.JSONArray(selectedMessageDetail.questionnaireAnswers)
                        val list = mutableListOf<String>()
                        for (i in 0 until arr.length()) {
                            list.add(arr.getString(i))
                        }
                        list
                    } catch (e: Exception) {
                        listOf("Failed parsing custom answers schema.")
                    }

                    val questions = viewModel.questionnaires[selectedMessageDetail.intent] ?: emptyList()
                    questions.forEachIndexed { i, q ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .background(primaryText.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(text = "Q: $q", color = secondaryText, fontSize = 11.sp)
                            Text(
                                text = answers.getOrNull(i) ?: "Not Answered",
                                color = primaryText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "DETAILED INQUIRY PITCH:",
                        color = accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedMessageDetail.rawText,
                        color = primaryText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Simulated Escrow banner showing anti-spam measures
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE2E8F0).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Escrow warning", tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ESCROW & TRUST FLAGS ACTIVE",
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Spam score: ${selectedMessageDetail.spamScore}% | Courteous Tone index: ${selectedMessageDetail.toneScore}%",
                                    color = secondaryText,
                                    fontSize = 11.sp
                                )
                                if (selectedMessageDetail.status == "ESCROW") {
                                    Text(
                                        text = "⏳ SENDER ESCROW PENDING RELEASE (${selectedMessageDetail.escrowHoursRemaining} Hours remain)",
                                        color = accentGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (translatedSummaryState != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(accentGold.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .border(1.dp, accentGold, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("TRANSLATION / மொழிபெயர்ப்பு", color = accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(translatedSummaryState ?: "", color = primaryText, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Translation controls (multilingual support)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    translationLoading = true
                                    val targetToTamil = !translateToTamilState
                                    val translated = GeminiClient.translateText(selectedMessageDetail.rawText, targetToTamil)
                                    translatedSummaryState = translated
                                    translateToTamilState = targetToTamil
                                    translationLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = borderStroke)
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = "Translate", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (translationLoading) "Processing language..."
                                else if (translateToTamilState) "Switch back to English"
                                else "Translate to Tamil / தமிழ்",
                                fontSize = 12.sp,
                                color = primaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { onDecline(selectedMessageDetail) },
                    border = BorderStroke(1.dp, Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Decline Partner", color = Color.Red)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { onAccept(selectedMessageDetail) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept & Deploy Space", color = Color.White)
                }
            }
        }
    } else {
        // Inbox Directory Screen (Traditional view vs Shake physics view)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Mechanics selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Swipe,
                        contentDescription = "Shake mechanics",
                        tint = accentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Shake to Release Mechanic",
                            color = primaryText,
                            fontSize = 11.9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Unread cards stick to corner pockets",
                            color = secondaryText,
                            fontSize = 11.sp
                        )
                    }
                }
                Switch(
                    checked = isPhysicsEnabled,
                    onCheckedChange = {
                        onPhysicsToggle(it)
                        if (it) {
                            viewModel.initializePhysics()
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = accentGold)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isPhysicsEnabled) {
                // Interactive physics arena
                ShakePhysicsView(
                    isSelectedInai = isInai,
                    cards = physicsCards,
                    rawMessages = messages,
                    isSimulating = isPhysicsActive,
                    onTriggerShake = onTriggerShake,
                    onCardClicked = { id ->
                        val found = messages.find { it.id == id }
                        if (found != null) {
                            onMessageSelect(found)
                        }
                    }
                )
            } else {
                // Classical vertical standard list
                Text(
                    text = "RECEIVER CENTRAL PORTAL (${messages.size} Messages)",
                    color = accentGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Inbox is clear! No inquiries present.", color = secondaryText, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(messages) { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(cardBg, RoundedCornerShape(12.dp))
                                    .border(1.dp, borderStroke, RoundedCornerShape(12.dp))
                                    .clickable { onMessageSelect(msg) }
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(msg.senderName, color = primaryText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(msg.intent, color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(accentGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("S: ${msg.priorityScore}", color = accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = msg.summary.ifEmpty { msg.rawText },
                                        color = secondaryText,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// WORKSPACE PLATFORM
// ==========================================
@Composable
fun WorkspaceTabContent(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    overlayBg: Color,
    borderStroke: Color,
    workspaces: List<WorkspaceEntity>,
    selectedWorkspaceId: Int?,
    onSelectWorkspace: (Int) -> Unit,
    chats: List<ChatEntity>,
    stickyNotes: List<StickyNoteEntity>,
    tasks: List<TaskEntity>,
    meetings: List<MeetingEntity>,
    milestones: List<MilestoneEntity>,
    viewModel: MainViewModel,
    showProofOfWork: () -> Unit
) {
    if (selectedWorkspaceId == null) {
        // Selection board
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "ACTIVE COLLABORATION SPACES",
                color = accentGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (workspaces.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = secondaryText, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No workspaces active yet.\nAccept a connection request to launch a space!",
                            color = secondaryText,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(workspaces) { ws ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBg, RoundedCornerShape(12.dp))
                                .border(1.dp, accentGold, RoundedCornerShape(12.dp))
                                .clickable { onSelectWorkspace(ws.id) }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ws.title, color = primaryText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Box(
                                        modifier = Modifier
                                            .background(accentGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = ws.originalIntent, color = accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Permanent intent tagging active. Real-time lists updated.",
                                    color = secondaryText,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Click to enter workspace",
                                    color = accentGold,
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Shared Workspace Area
        var innerTab by remember { mutableStateOf("chat") } // chat, notes, doc, tasks, meetings, milestones
        val workspace = workspaces.find { it.id == selectedWorkspaceId } ?: return

        Column(modifier = Modifier.fillMaxSize()) {
            // Workspace Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(overlayBg)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.selectedWorkspaceId.value = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
                    }
                    Column {
                        Text(workspace.title, color = primaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Intent Tag: ${workspace.originalIntent}", color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Button(
                    onClick = showProofOfWork,
                    colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "📄 Proof of Work",
                        color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Scrolling view tags inside workspace options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(overlayBg)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val tabsList = listOf(
                    "chat" to "💬 Live Chat",
                    "notes" to "💡 Whiteboard",
                    "doc" to "📝 Proposal Docs",
                    "tasks" to "📋 Task Board",
                    "meetings" to "📅 Meetings",
                    "milestones" to "🏁 Milestones"
                )
                tabsList.forEach { (key, label) ->
                    val sel = innerTab == key
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = if (sel) accentGold else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (sel) accentGold else borderStroke,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { innerTab = key }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (sel) (if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A)) else primaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub Tab viewport panel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                when (innerTab) {
                    "chat" -> {
                        ChatPanel(
                            isInai, primaryText, secondaryText, accentGold, cardBg, borderStroke, overlayBg,
                            chats = chats,
                            onSendMessage = { text, files, voice -> viewModel.sendChatMessage(text, null, files, voice) }
                        )
                    }

                    "notes" -> {
                        WhiteboardPanel(
                            isInai, primaryText, secondaryText, accentGold, cardBg, borderStroke,
                            notes = stickyNotes,
                            onAddNote = { text, color -> viewModel.addStickyNote(text, color) },
                            onDeleteNote = { viewModel.deleteStickyNote(it) }
                        )
                    }

                    "doc" -> {
                        DocEditorPanel(
                            isInai, primaryText, secondaryText, accentGold, cardBg, borderStroke,
                            savedContent = workspace.docContent,
                            onSave = { viewModel.saveDocumentContent(it) },
                            viewModel = viewModel
                        )
                    }

                    "tasks" -> {
                        TaskBoardPanel(
                            isInai, primaryText, secondaryText, accentGold, cardBg, borderStroke,
                            tasks = tasks,
                            onAddTask = { title, user, date -> viewModel.addTaskCard(title, user, date) },
                            onUpdateTaskStatus = { task, status -> viewModel.updateTaskStatus(task, status) },
                            onDeleteTask = { viewModel.deleteTaskCard(it) }
                        )
                    }

                    "meetings" -> {
                        MeetingsPanel(
                            isInai, primaryText, secondaryText, accentGold, cardBg, borderStroke,
                            meetings = meetings,
                            onAddMeeting = { title, agendaText -> viewModel.generateMeetingAgendaWithAi(title) },
                            onDeleteMeeting = { viewModel.deleteMeetingSlot(it) }
                        )
                    }

                    "milestones" -> {
                        MilestonesPanel(
                            isInai, primaryText, secondaryText, accentGold, cardBg, borderStroke,
                            milestones = milestones,
                            onAddMilestone = { title, date -> viewModel.addMilestoneItem(title, date) },
                            onToggle = { viewModel.toggleMilestoneItem(it) },
                            onDelete = { viewModel.deleteMilestoneItem(it) }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// PASSPORTS TAB (PUBLIC PROFILES & COMPOSE)
// ==========================================
@Composable
fun PassportsTabContent(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    profiles: List<ProfileEntity>,
    selectedProfileUsername: String?,
    onSelectProfile: (String) -> Unit,
    isWritingMessage: Boolean,
    onOpenComposer: () -> Unit,
    onCloseComposer: () -> Unit,
    selectedIntentType: String,
    onSelectIntentType: (String) -> Unit,
    questionnaireAnswers: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    questionnaireSchema: List<String>,
    inputMessageText: String,
    onMessageChange: (String) -> Unit,
    inputExtraDetails: String,
    onExtraDetailsChange: (String) -> Unit,
    activeDraft: String,
    draftGenerating: Boolean,
    toneScore: Int?,
    spamScoreText: String?,
    onToneCheck: () -> Unit,
    onAiDraft: (String, String) -> Unit,
    onSubmitMessage: (String) -> Unit
) {
    val draftStr = activeDraft
    val isGenDraft = draftGenerating
    val toneScoreVal = toneScore
    val spamScoreStr = spamScoreText

    val context = LocalContext.current

    if (isWritingMessage) {
        // Custom Intent Questionnaire Composition page
        val profile = profiles.find { it.username == selectedProfileUsername } ?: return
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseComposer) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Close", tint = accentGold)
                }
                Text(
                    text = "Intent-based Questionnaire Pitch",
                    color = primaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Select intent type
            Text("Select Opportunity Intent Category:", color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                val intentCategories = listOf("Job Offer", "Collaboration", "Mentorship", "Research", "Investment", "Service Request", "Speaking Invitation", "Networking")
                intentCategories.forEach { category ->
                    val selected = selectedIntentType == category
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                color = if (selected) accentGold else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, accentGold, RoundedCornerShape(12.dp))
                            .clickable { onSelectIntentType(category) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (selected) (if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A)) else primaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pre-Message Questionnaire fields representation
            Text(
                text = "Pre-Message Questionnaire Required:",
                color = accentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "InReach mandates answering category-specific verification questions to eliminate mass cold outreach spam.",
                color = secondaryText,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            questionnaireSchema.forEachIndexed { i, question ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(text = "Q${i + 1}: $question", color = primaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = questionnaireAnswers.getOrElse(i) { "" },
                        onValueChange = { newValue ->
                            if (i < questionnaireAnswers.size) {
                                questionnaireAnswers[i] = newValue
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentGold,
                            unfocusedBorderColor = borderStroke,
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText
                        ),
                        placeholder = { Text("State verification details...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Draft Composition Panel & AI helper prompt
            Text("Write / Refine Detailed Inquiry Pitch:", color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = inputMessageText,
                onValueChange = onMessageChange,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = borderStroke,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                placeholder = { Text("Briefly explain your mission, proposed scope, or opportunity details...", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // AI Co-writer trigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(10.dp))
                    .border(1.dp, borderStroke, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "🤖 AI Co-Writer Prompt (Powered by GPT-4 / Gemini)",
                        color = accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = inputExtraDetails,
                        onValueChange = onExtraDetailsChange,
                        placeholder = { Text("Add bullet points (e.g., 'restoring high density meshes', 'joint grants available')", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentGold,
                            unfocusedBorderColor = borderStroke,
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onAiDraft(profile.displayName, inputExtraDetails) },
                            colors = ButtonDefaults.buttonColors(containerColor = accentGold)
                        ) {
                            Text(
                                if (isGenDraft) "Drafting pitch..." else "Generate AI Draft Template",
                                fontSize = 11.sp,
                                color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A)
                            )
                        }

                        // Apply generated draft button
                        if (draftStr.isNotEmpty()) {
                            TextButton(onClick = { onMessageChange(draftStr) }) {
                                Text("Apply Template", color = accentGold, fontSize = 12.sp)
                            }
                        }
                    }

                    if (draftStr.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = draftStr,
                            color = primaryText,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .background(primaryBgColor(isInai), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tone index checker before submission
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onToneCheck,
                    colors = ButtonDefaults.buttonColors(containerColor = borderStroke)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Check quality", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("De-spam & Tone Check Pitch", fontSize = 11.sp, color = primaryText)
                }

                if (toneScoreVal != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Courtesy Meter: $toneScoreVal%", color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(spamScoreStr ?: "", color = secondaryText, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Connection offer card
            Button(
                onClick = { onSubmitMessage(profile.username) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Submit to Sender Reputation Hub",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        // Browsing Directory list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "OPPORTUNITY PASSPORT REGISTRY",
                color = accentGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(profiles) { profile ->
                    val isExpanded = selectedProfileUsername == profile.username
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBg, RoundedCornerShape(14.dp))
                            .border(
                                width = if (isExpanded) 2.dp else 1.dp,
                                color = if (isExpanded) accentGold else borderStroke,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectProfile(profile.username) }
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Simulated profile image
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(accentGold.copy(alpha = 0.2f), CircleShape)
                                        .border(1.dp, accentGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profile.displayName.take(1).uppercase(),
                                        color = accentGold,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.displayName,
                                            color = primaryText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Five-tier identity check badge system representation
                                        for (step in 1..profile.verificationTier) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Tier $step verified",
                                                tint = if (step == 5) Color(0xFF10B981) else accentGold,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Text(text = "@${profile.username}", color = secondaryText, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = profile.bio, color = primaryText, fontSize = 12.sp, lineHeight = 16.sp)

                            // Quick trust rates
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = "Trust Rating", tint = accentGold, modifier = Modifier.size(14.dp))
                                Text(
                                    text = " Verified Trust Metrics: ${profile.trustScore}% Trust index | Responses: ${profile.responseRate}% in Windows",
                                    color = secondaryText,
                                    fontSize = 11.sp
                                )
                            }

                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = borderStroke)
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "AVAILABILITY CHANNELS:",
                                    color = accentGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = profile.availabilityWindows,
                                    color = primaryText,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = onOpenComposer,
                                    colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                                    modifier = Modifier.fillMaxWidth().testTag("open_composer_button")
                                ) {
                                    Text(
                                        text = "Compose Warm connection request",
                                        color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ANALYTICS PANEL DESIGN
// ==========================================
@Composable
fun AnalyticsTabContent(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    messages: List<MessageEntity>
) {
    var generateAnalyticsLoading by remember { mutableStateOf(false) }
    var showSuccessReportMsg by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OPPORTUNITY ANALYTICS DASHBOARD",
                color = accentGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            // Export PDF button representation
            Button(
                onClick = {
                    generateAnalyticsLoading = true
                    showSuccessReportMsg = false
                    // Simulate processing
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        generateAnalyticsLoading = false
                        showSuccessReportMsg = true
                    }, 1200)
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    if (generateAnalyticsLoading) "Compiling PDF..." else "📥 Export PDF Report",
                    color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showSuccessReportMsg) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Success: Local report signed and stored in device cache ('inreach_outcomes_analytics.pdf').",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chart 1: Volume graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, borderStroke, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "INCOMING INQUIRY VOLUME TREND (Monthly)",
                    color = accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Custom Canvas volume chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val points = listOf(14f, 22f, 48f, 32f, 65f, 54f, 82f)
                    val stepX = w / (points.size - 1)
                    val maxVal = 100f

                    val path = androidx.compose.ui.graphics.Path()

                    points.forEachIndexed { i, p ->
                        val ratioY = p / maxVal
                        val py = h - (h * ratioY)
                        val px = i * stepX
                        if (i == 0) {
                            path.moveTo(px, py)
                        } else {
                            path.lineTo(px, py)
                        }

                        // Draw dots
                        drawCircle(color = accentGold, radius = 5f, center = androidx.compose.ui.geometry.Offset(px, py))
                    }

                    drawPath(
                        path = path,
                        color = accentGold,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val months = listOf("Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May")
                    months.forEach { m ->
                        Text(m, color = secondaryText, fontSize = 9.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 2: Intent-Type Split
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, borderStroke, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "INBOUND INTENT CATEGORY DISTRIBUTION",
                    color = accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val intentsCount = listOf(
                    Triple("Collaboration", 44, Color(0xFF10B981)),
                    Triple("Mentorship", 22, Color(0xFF3B82F6)),
                    Triple("Investment", 18, Color(0xFFF59E0B)),
                    Triple("Research", 10, Color(0xFF8B5CF6)),
                    Triple("Speaking Invite", 6, Color(0xFFEC4899))
                )

                intentsCount.forEach { (category, percentage, color) ->
                    Column(modifier = Modifier.padding(bottom = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(category, color = primaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("$percentage%", color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(borderStroke, RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percentage / 100f)
                                    .fillMaxHeight()
                                    .background(color, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart 3: Response Dials and Speed Funnel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg, RoundedCornerShape(14.dp))
                .border(1.dp, borderStroke, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "OPPORTUNITY SPENT RESPONDING & VERIFIED ENGAGEMENTS",
                    color = accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "96%", color = Color(0xFF10B981), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Engagement Rate", color = secondaryText, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "3.2 hrs", color = accentGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Avg Response Time", color = secondaryText, fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "98", color = primaryText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Trust Rep Index", color = secondaryText, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}


// ==========================================
// CHAT CHANNEL SUB-COMPONENT
// ==========================================
@Composable
fun ChatPanel(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    overlayBg: Color,
    chats: List<ChatEntity>,
    onSendMessage: (String, String?, Int?) -> Unit
) {
    var txtInput by remember { mutableStateOf("") }
    var fileSelectedState by remember { mutableStateOf<String?>(null) }
    var voiceDurationState by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chats) { chat ->
                val fromMe = chat.senderName == "Avinaash Anand"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (fromMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .background(
                                color = if (fromMe) accentGold.copy(alpha = 0.15f) else cardBg,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (fromMe) accentGold else borderStroke,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(chat.senderName, color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (chat.emojiReactions.isNotEmpty()) "Reactions: ${chat.emojiReactions}" else "",
                                    color = secondaryText,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            if (chat.voiceNoteDuration != null) {
                                // Voice note visual waveform representation
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play voice", tint = accentGold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Voice Note: ${chat.voiceNoteDuration}s", color = primaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Canvas(modifier = Modifier.width(80.dp).height(12.dp)) {
                                        val bars = 12
                                        val waveSpacing = size.width / bars
                                        for (i in 0..bars) {
                                            drawRect(
                                                color = accentGold,
                                                size = androidx.compose.ui.geometry.Size(4f, (2..12).random().toFloat() * (size.height / 12f)),
                                                topLeft = androidx.compose.ui.geometry.Offset(i * waveSpacing, size.height / 2f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(chat.messageText, color = primaryText, fontSize = 13.sp)
                            }

                            // Show attachment visual
                            if (chat.attachedFileUri != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .background(primaryBgColor(isInai), RoundedCornerShape(6.dp))
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AttachFile, contentDescription = "Attachment", tint = accentGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(chat.attachedFileUri, color = primaryText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Attachment panel
        if (fileSelectedState != null || voiceDurationState != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Upload", tint = accentGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (fileSelectedState != null) "File attached: $fileSelectedState" else "Simulated voice note (${voiceDurationState}s)",
                        color = primaryText,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = {
                    fileSelectedState = null
                    voiceDurationState = null
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Chat controls bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated voice note record button
            IconButton(onClick = {
                voiceDurationState = (5..25).random()
                fileSelectedState = null
            }) {
                Icon(Icons.Default.Mic, contentDescription = "Simulate Recording", tint = accentGold)
            }

            // File attachments button
            IconButton(onClick = {
                fileSelectedState = listOf("proposal_restoration.pdf", "icon_design_canvas.png", "term_sheet_escrow.epub").random()
                voiceDurationState = null
            }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Simulate File Selection", tint = accentGold)
            }

            OutlinedTextField(
                value = txtInput,
                onValueChange = { txtInput = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = borderStroke,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                placeholder = { Text("Write connection thread message...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (txtInput.isNotEmpty() || fileSelectedState != null || voiceDurationState != null) {
                        onSendMessage(txtInput, fileSelectedState, voiceDurationState)
                        txtInput = ""
                        fileSelectedState = null
                        voiceDurationState = null
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = accentGold)
            }
        }
    }
}

// Helper color resolver
fun primaryBgColor(isInai: Boolean) = if (isInai) Color(0xFFF4EBE1) else Color(0xFF0F172A)

// ==========================================
// NOTES & WHITEBOARD WRITING CANVAS
// ==========================================
@Composable
fun WhiteboardPanel(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    notes: List<StickyNoteEntity>,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (StickyNoteEntity) -> Unit
) {
    var notepadText by remember { mutableStateOf("") }
    val colorsList = listOf("#FFF9C4", "#E8F5E9", "#E3F2FD", "#FCE4EC", "#ECEFF1")
    var selectedColorIndex by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "IDEATION WHITEBOARD / MIND MAP CANVAS",
            color = accentGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Custom sandbox whiteboard container with sticky notes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(borderStroke.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .border(1.dp, borderStroke, RoundedCornerShape(12.dp))
        ) {
            if (notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Canvas Empty. Tap below to pin sticky notes!", color = secondaryText, fontSize = 12.sp)
                }
            } else {
                // Render custom notes
                notes.forEach { note ->
                    val resolvedColor = try {
                        Color(android.graphics.Color.parseColor(note.colorHex))
                    } catch (e: Exception) {
                        Color(0xFFFFF9C4)
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = (note.posX / 2.5f).dp, y = (note.posY / 2.5f).dp)
                            .size(105.dp, 105.dp)
                            .shadow(2.dp, RoundedCornerShape(6.dp))
                            .background(resolvedColor, RoundedCornerShape(6.dp))
                            .clickable { }
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = note.text,
                                color = Color.Black,
                                fontSize = 11.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Sticky",
                                    tint = Color.DarkGray,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onDeleteNote(note) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Adding Sticky Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = notepadText,
                onValueChange = { notepadText = it },
                placeholder = { Text("Write short sticky card idea...", fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = borderStroke,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Selection sticky colors
            Row {
                colorsList.forEachIndexed { idx, col ->
                    val colInstance = Color(android.graphics.Color.parseColor(col))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(18.dp)
                            .background(colInstance, CircleShape)
                            .border(
                                width = if (selectedColorIndex == idx) 2.dp else 0.dp,
                                color = if (selectedColorIndex == idx) accentGold else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorIndex = idx }
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = {
                    if (notepadText.isNotEmpty()) {
                        onAddNote(notepadText, colorsList[selectedColorIndex])
                        notepadText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentGold)
            ) {
                Text("+ Sticky", color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A), fontSize = 11.sp)
            }
        }
    }
}

// ==========================================
// DOCUMENT PROPOSAL CO-WRITER
// ==========================================
@Composable
fun DocEditorPanel(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    savedContent: String,
    onSave: (String) -> Unit,
    viewModel: MainViewModel
) {
    var rawText by remember(savedContent) { mutableStateOf(savedContent) }
    var summarizeLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIGHTWEIGHT PROPOSAL & ACTION PLAN CO-WRITER",
                color = accentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    summarizeLoading = true
                    viewModel.summarizeWorkspaceProofOfWork()
                    // Delay reset
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        summarizeLoading = false
                    }, 1200)
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (summarizeLoading) "Summarizing..." else "🪄 AI Summarize Idea",
                    fontSize = 10.sp,
                    color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = rawText,
            onValueChange = {
                rawText = it
                onSave(it)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentGold,
                unfocusedBorderColor = borderStroke,
                focusedTextColor = primaryText,
                unfocusedTextColor = primaryText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

// ==========================================
// TASK BOARD (KANBAN)
// ==========================================
@Composable
fun TaskBoardPanel(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    tasks: List<TaskEntity>,
    onAddTask: (String, String, String) -> Unit,
    onUpdateTaskStatus: (TaskEntity, String) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    var taskNameInput by remember { mutableStateOf("") }
    var assigneeInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Form trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = taskNameInput,
                onValueChange = { taskNameInput = it },
                placeholder = { Text("Write task...", fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = borderStroke,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = assigneeInput,
                onValueChange = { assigneeInput = it },
                placeholder = { Text("Who...", fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = borderStroke,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                modifier = Modifier.width(90.dp),
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    if (taskNameInput.isNotEmpty()) {
                        onAddTask(taskNameInput, assigneeInput.ifEmpty { "Both" }, "June 1")
                        taskNameInput = ""
                        assigneeInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentGold)
            ) {
                Text("+ Card", color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Three Column Display (scrollable row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(rememberScrollState())
        ) {
            val cols = listOf("TODO" to "📥 To Do", "IN_PROGRESS" to "⚙️ Run Progress", "DONE" to "🏁 Finished")
            cols.forEach { (statusKey, statusHeader) ->
                val colTasks = tasks.filter { it.columnStatus == statusKey }

                Column(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .width(220.dp)
                        .background(borderStroke.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "$statusHeader (${colTasks.size})",
                        color = accentGold,
                        fontSize = 11.8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    HorizontalDivider(color = borderStroke)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colTasks) { task ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(cardBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, borderStroke, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(task.title, color = primaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("assigned: ${task.assignee}", color = secondaryText, fontSize = 10.sp)
                                        // Click to push state
                                        IconButton(
                                            onClick = {
                                                val next = when (task.columnStatus) {
                                                    "TODO" -> "IN_PROGRESS"
                                                    "IN_PROGRESS" -> "DONE"
                                                    else -> "TODO"
                                                }
                                                onUpdateTaskStatus(task, next)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.ArrowForward, contentDescription = "cycle", tint = accentGold, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// MEETING SCHEDULER
// ==========================================
@Composable
fun MeetingsPanel(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    meetings: List<MeetingEntity>,
    onAddMeeting: (String, String) -> Unit,
    onDeleteMeeting: (MeetingEntity) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var calendarChecking by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("AI SCHEDULER & DE-CONFLICT CALENDAR ENGINE", color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                placeholder = { Text("Meeting topic (e.g. Manuscript kickoff sync)", fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = borderStroke,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (titleInput.isNotEmpty()) {
                        calendarChecking = true
                        onAddMeeting(titleInput, "")
                        titleInput = ""
                        // Delay reset
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            calendarChecking = false
                        }, 500)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentGold)
            ) {
                Text(if (calendarChecking) "Checking Sync..." else "+ Schedule AI Sync", color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (meetings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No upcoming meetings listed.", color = secondaryText, fontSize = 12.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(meetings) { m ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBg, RoundedCornerShape(10.dp))
                            .border(1.dp, borderStroke, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(m.title, color = primaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Calendar indicator", tint = accentGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Calendar Connected", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(m.agendaText, color = secondaryText, fontSize = 12.sp, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { onDeleteMeeting(m) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                                Text("Cancel Slot", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// MILESTONE TRACKER
// ==========================================
@Composable
fun MilestonesPanel(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    milestones: List<MilestoneEntity>,
    onAddMilestone: (String, String) -> Unit,
    onToggle: (MilestoneEntity) -> Unit,
    onDelete: (MilestoneEntity) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }

    val finishedCount = milestones.filter { it.isCompleted }.size
    val totalCount = milestones.size
    val progressPercent = if (totalCount > 0) (finishedCount.toFloat() / totalCount.toFloat()) else 0f

    Column(modifier = Modifier.fillMaxSize()) {
        Text("MILESTONES & PROGRESS METRICS", color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(borderStroke, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressPercent)
                    .fillMaxHeight()
                    .background(Color(0xFF10B981), RoundedCornerShape(5.dp))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Progress indicator: ${(progressPercent * 100).toInt()}%", color = secondaryText, fontSize = 10.sp)
            Text("$finishedCount of $totalCount completed", color = accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                placeholder = { Text("Milestone title (e.g., Release Paper Draft)", fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = borderStroke,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    if (titleInput.isNotEmpty()) {
                        onAddMilestone(titleInput, "June 15")
                        titleInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentGold)
            ) {
                Text("+ Milestone", color = if (isInai) Color(0xFF3E2723) else Color(0xFF0F172A), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (milestones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No milestones listed.", color = secondaryText, fontSize = 12.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(milestones) { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, borderStroke, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = m.isCompleted,
                                onCheckedChange = { onToggle(m) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981), uncheckedColor = accentGold)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = m.title,
                                    color = if (m.isCompleted) secondaryText else primaryText,
                                    textDecoration = if (m.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("target deadline: ${m.targetDate}", color = secondaryText, fontSize = 10.sp)
                            }
                        }

                        IconButton(onClick = { onDelete(m) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// CUSTOM AUTHENTICATION LAYOUT
// ==========================================
@Composable
fun AuthLayout(
    isInai: Boolean,
    isDark: Boolean,
    primaryBg: Color,
    containerBg: Color,
    cardBg: Color,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    borderStroke: Color,
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    onLoginSuccess: (String, String) -> Unit,
    onSignUpSuccess: (String, String, String) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("avinaash@inreach.io") }
    var password by remember { mutableStateOf("••••••••") }
    var name by remember { mutableStateOf("Avinaash Anand") }
    var confirmPassword by remember { mutableStateOf("••••••••") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .background(containerBg, RoundedCornerShape(20.dp))
                .border(1.dp, borderStroke, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accentGold.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, accentGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "InReach Icon",
                    tint = accentGold,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isInai) "இன்-ரீச்" else "InReach",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                fontFamily = FontFamily.Serif
            )

            Text(
                text = if (currentScreen == "login") "Welcome back! Enter your credentials." else "Create your verified developer card",
                fontSize = 12.sp,
                color = secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            if (currentScreen == "signup") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User", tint = secondaryText, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentGold,
                        unfocusedBorderColor = borderStroke,
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                        .padding(bottom = 10.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = secondaryText, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = borderStroke,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = secondaryText, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle",
                            tint = secondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentGold,
                    unfocusedBorderColor = borderStroke,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (currentScreen == "signup") 10.dp else 20.dp)
            )

            if (currentScreen == "signup") {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = secondaryText, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentGold,
                        unfocusedBorderColor = borderStroke,
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
            }

            Button(
                onClick = {
                    if (currentScreen == "login") {
                        onLoginSuccess(email, password)
                    } else {
                        onSignUpSuccess(name, email, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_button")
            ) {
                Text(
                    text = if (currentScreen == "login") "Sign In To Sandbox" else "Join Peer Network",
                    color = if (isInai) Color(0xFF3E2723) else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Google Button (Interactive mock)
            OutlinedButton(
                onClick = { onGoogleSignIn() },
                border = BorderStroke(1.dp, borderStroke),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryText)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Google Secure",
                        tint = accentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .clickable {
                        if (currentScreen == "login") {
                            onScreenChange("signup")
                        } else {
                            onScreenChange("login")
                        }
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentScreen == "login") "Don't have an account? " else "Already have an account? ",
                    color = secondaryText,
                    fontSize = 11.sp
                )
                Text(
                    text = if (currentScreen == "login") "Sign Up" else "Login",
                    color = accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

// ==========================================
// STANDALONE SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    isInai: Boolean,
    isDark: Boolean,
    primaryBg: Color,
    containerBg: Color,
    cardBg: Color,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    borderStroke: Color,
    viewModel: MainViewModel,
    onClose: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isDarkModeState by viewModel.isDarkMode.collectAsState()
    val profileVisibilityPublic by viewModel.profileVisibilityPublic.collectAsState()
    val messageRequestsAnyone by viewModel.messageRequestsAnyone.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBg)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerBg)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Text(
                text = "Close",
                color = accentGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onClose() }
                    .padding(8.dp)
            )
        }

        if (isInai) {
            DrawingKolamBorderHorizontal(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                color = accentGold
            )
        } else {
            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(borderStroke))
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "APPEARANCE & RENDERING",
                color = accentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderStroke),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark Theme", color = primaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (isDarkModeState) "Deep space energy saver palette active" else "Responsive high brightness light mode active",
                                color = secondaryText,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isDarkModeState,
                            onCheckedChange = { viewModel.isDarkMode.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentGold, checkedTrackColor = accentGold.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "REGIONAL IDENTITY ARCHITECTURE",
                color = accentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderStroke),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Synchronize custom rendering schemes between global standard (InReach) and localized regional heritage preservation styles (INAI) in real time.",
                        color = secondaryText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.themeMode.value = "InReach" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.themeMode.value == "InReach") accentGold else borderStroke
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "InReach Global",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.themeMode.value == "InReach") (if (isDark) Color.Black else Color.White) else secondaryText
                            )
                        }

                        Button(
                            onClick = { viewModel.themeMode.value = "INAI" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.themeMode.value == "INAI") accentGold else borderStroke
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "INAI (பிராந்திய)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.themeMode.value == "INAI") (if (isDark) Color.Black else Color.White) else secondaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PEER SYSTEM PRIVACY OPTIONS",
                color = accentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderStroke),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.profileVisibilityPublic.value = !profileVisibilityPublic },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Public Access URL Card", color = primaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (profileVisibilityPublic) "Pitches enabled on inreach.app/u/avinaash" else "Private profile hidden from global indexes",
                                color = secondaryText,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = profileVisibilityPublic,
                            onCheckedChange = { viewModel.profileVisibilityPublic.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentGold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp).fillMaxWidth().background(borderStroke))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.messageRequestsAnyone.value = !messageRequestsAnyone },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gateway Message Requests", color = primaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (messageRequestsAnyone) "Anyone can submit warm introductions" else "Only authorized mutual nodes can send requests",
                                color = secondaryText,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = messageRequestsAnyone,
                            onCheckedChange = { viewModel.messageRequestsAnyone.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentGold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SANDBOX SUPPORT & AUDIT",
                color = accentGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, borderStroke),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { Toast.makeText(context, "Contact: support@inreach.io", Toast.LENGTH_LONG).show() }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Get Support & Help", color = primaryText, fontSize = 13.sp)
                        Icon(Icons.Default.ArrowForward, contentDescription = ">", tint = secondaryText, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(borderStroke))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { Toast.makeText(context, "V1.0.0 Live sandbox container", Toast.LENGTH_SHORT).show() }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Peer Guild Network Specs", color = primaryText, fontSize = 13.sp)
                        Icon(Icons.Default.ArrowForward, contentDescription = ">", tint = secondaryText, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invalidate Local Keypair & Logout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==========================================
// MY CONNECTIONS LIST TAB CONTENT
// ==========================================
@Composable
fun ConnectionsTabContent(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    profiles: List<ProfileEntity>,
    messages: List<MessageEntity>,
    workspaces: List<WorkspaceEntity>,
    onOpenWorkspace: (Int) -> Unit,
    viewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()
    val acceptedSenderUsernames = messages.filter { it.status == "ACCEPTED" || it.id in listOf(6, 7, 8) }.map { it.senderUsername }.toSet()
    val connectedProfiles = profiles.filter { acceptedSenderUsernames.contains(it.username) || it.username in listOf("sarah", "mike", "emma", "meenakshi") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Connections",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = primaryText
        )
        Text(
            text = "Active mutual sandbox collaborations. Select any verified peer node to open their co-writing workspace index.",
            color = secondaryText,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        if (connectedProfiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active connections yet. Browse registered Passports and compose introductions to build your secure network!",
                    color = secondaryText,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(connectedProfiles) { p ->
                    val relatedWorkspace = workspaces.find { 
                        it.senderUsername == p.username || it.recipientUsername == p.username
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, borderStroke),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(accentGold.copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, accentGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = p.displayName.take(2).uppercase(),
                                        color = accentGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = p.displayName,
                                            color = primaryText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    if (p.username in listOf("mike", "sarah")) Color(0xFF10B981) else Color.LightGray,
                                                    CircleShape
                                                )
                                        )
                                    }
                                    Text(
                                        text = p.bio,
                                        color = secondaryText,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Connected 2 days ago",
                                    color = secondaryText,
                                    fontSize = 10.sp
                                )

                                if (relatedWorkspace != null) {
                                    Button(
                                        onClick = { onOpenWorkspace(relatedWorkspace.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            "Open Workspace",
                                            fontSize = 10.sp,
                                            color = if (isInai) Color(0xFF3E2723) else Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val ws = WorkspaceEntity(
                                                    messageId = 0,
                                                    originalIntent = "Collaboration",
                                                    senderUsername = p.username,
                                                    recipientUsername = "avinaash",
                                                    title = "Workspace with ${p.displayName}",
                                                    docContent = "# Sandboxed Workspace\nDraft proposals secure here..."
                                                )
                                                val wsId = viewModel.database.appDao().insertWorkspace(ws).toInt()
                                                onOpenWorkspace(wsId)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = borderStroke),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            "Launch Space",
                                            fontSize = 10.sp,
                                            color = primaryText,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PROFILE TAB CONTENT
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileTabContent(
    isInai: Boolean,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    cardBg: Color,
    borderStroke: Color,
    currentUser: String,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Card",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            IconButton(
                onClick = { onOpenSettings() },
                modifier = Modifier.testTag("theme_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings Icon",
                    tint = accentGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderStroke),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(accentGold.copy(alpha = 0.15f), CircleShape)
                            .border(1.5.dp, accentGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentUser == "avinaash") "AA" else "MM",
                            color = accentGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (currentUser == "avinaash") "Avinaash Anand" else "Meenakshi Iyer",
                            color = primaryText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentUser == "avinaash") "Senior Platform & Layout Architect" else "Historical Epigraphy Visual Specialist",
                            color = secondaryText,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = "Trust verified", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gov ID Checked (Tier 5)", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("YOUR PUBLIC GATEWAY LINK", color = accentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryBgColor(isInai), RoundedCornerShape(8.dp))
                        .border(1.dp, borderStroke, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "inreach.app/u/$currentUser",
                        color = primaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Copied public gateway details!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = accentGold, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Link shared securely to active networks.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = accentGold, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("BIO", color = accentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (currentUser == "avinaash") {
                        "Full-stack software developer focused on preserving high visual fidelity layouts, ancient Indian heritage digital assets, robust sandboxed applications, and cryptographic contract alignments."
                    } else {
                        "Epigrapher and design visual architect specializing on multi-language transcription preservation, sandstone manuscript simulation models, and classical arts telemetry."
                    },
                    color = primaryText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("SKILLS & DOMAINS", color = accentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Kotlin", "Jetpack Compose", "React.js", "Room DB", "Heritage Graphics", "Material Design 3").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(accentGold.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .border(1.dp, accentGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(tag, color = accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("CREDIBILITY SCORECARD", color = accentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(primaryBgColor(isInai), RoundedCornerShape(8.dp))
                            .border(1.dp, borderStroke, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Reputation", color = secondaryText, fontSize = 9.sp)
                            Text("98/100", color = accentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(primaryBgColor(isInai), RoundedCornerShape(8.dp))
                            .border(1.dp, borderStroke, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Escrow Stake", color = secondaryText, fontSize = 9.sp)
                            Text("0.04 ETH", color = accentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(primaryBgColor(isInai), RoundedCornerShape(8.dp))
                            .border(1.dp, borderStroke, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Response Rate", color = secondaryText, fontSize = 9.sp)
                            Text("96%", color = Color(0xFF10B981), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, borderStroke),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { Toast.makeText(context, "Downloading verified CV asset...", Toast.LENGTH_SHORT).show() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = "CV Document", tint = accentGold)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("professional_portfolio_cv.pdf", color = primaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Size: 4.2 MB • Updated: May 23 • PGP Signed", color = secondaryText, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
