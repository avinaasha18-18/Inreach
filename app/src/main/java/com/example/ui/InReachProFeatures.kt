package com.example.ui

data class InReachFeature(
    val id: String,
    val name: String,
    val description: String,
    val isPro: Boolean,
    val section: String,
    val benefits: List<String>
)

object InReachProFeatures {
    val sections = listOf(
        "Inbox Screen",
        "Message Detail Screen",
        "Analytics Dashboard",
        "Identity Verification",
        "Live Chat",
        "Proposal Documents",
        "Whiteboard",
        "Task Board",
        "Meetings",
        "Milestones",
        "Profile Screen",
        "Settings Screen"
    )

    val list: List<InReachFeature> = listOf(
        // === INBOX SCREEN ===
        InReachFeature(
            id = "inbox_receive_messages",
            name = "Receive structured messages",
            description = "Receive structured, intent-based messages with pre-filled answers directly from your public link.",
            isPro = false,
            section = "Inbox Screen",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "inbox_summary",
            name = "AI 1-line message summary",
            description = "Instantly read 1-line AI summaries of incoming requests.",
            isPro = false,
            section = "Inbox Screen",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "inbox_spam_score",
            name = "Basic AI spam score",
            description = "Get automatic standard analysis and spam indicators on all messages.",
            isPro = false,
            section = "Inbox Screen",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "inbox_escrow",
            name = "48-hour message escrow",
            description = "Holds incoming messages for up to 48 hours to prevent spam flood.",
            isPro = false,
            section = "Inbox Screen",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "inbox_priority_sorting",
            name = "Full AI priority inbox sorting",
            description = "Sort your inbox intelligently using priority indices generated from past interactions, intent matching, and compatibility.",
            isPro = true,
            section = "Inbox Screen",
            benefits = listOf("Never miss high-value opportunities", "Smart score calculation", "Save up to 10 hours of sorting weekly")
        ),
        InReachFeature(
            id = "inbox_challenge_response",
            name = "Challenge-response inbox gate",
            description = "Gate unknown senders behind customizable interactive questionnaire challenges before they can land in your active feed.",
            isPro = true,
            section = "Inbox Screen",
            benefits = listOf("Stop all automated cold spam", "Collect critical context first", "Tailor rules per target audience")
        ),
        InReachFeature(
            id = "inbox_manual_review",
            name = "Manual review queue mode",
            description = "Double-gate incoming intents by routing low-priority messages to a separate sandbox review queue for audit.",
            isPro = true,
            section = "Inbox Screen",
            benefits = listOf("Unclutter main working panels", "Keep focus on high-trust connections", "Review on your own active timeframe")
        ),
        InReachFeature(
            id = "inbox_expiry_controls",
            name = "Message expiry controls",
            description = "Set custom escrow window: 24h / 48h / 72h / 7 days per intent category to manage responder timelines.",
            isPro = true,
            section = "Inbox Screen",
            benefits = listOf("Define strict response deadlines", "Auto-refund stake or expire intents", "Maintains active network velocity")
        ),
        InReachFeature(
            id = "inbox_bulk_actions",
            name = "Bulk message actions",
            description = "Select multiple messages inside the inbox queue. Bulk accept, decline, or archive in one simplified tap.",
            isPro = true,
            section = "Inbox Screen",
            benefits = listOf("Clear high volumes of inquiries in seconds", "Standardized decline responses in mass", "Highly efficient list operations")
        ),

        // === MESSAGE DETAIL SCREEN ===
        InReachFeature(
            id = "detail_read_full",
            name = "Read full message + questionnaire answers",
            description = "Access complete textual query and response forms from your public link.",
            isPro = false,
            section = "Message Detail Screen",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "detail_accept_decline",
            name = "Accept or decline message",
            description = "Direct controls to accept to open workspace or decline and release the connection.",
            isPro = false,
            section = "Message Detail Screen",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "detail_rate_sender",
            name = "Rate sender (thumbs up/down/spam)",
            description = "Feedback interface to flag sender quality and build consensus score.",
            isPro = false,
            section = "Message Detail Screen",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "detail_smart_replies",
            name = "AI smart reply suggestions",
            description = "Get instantaneous contextual smart-response drafts tailored specifically to the sender's details and active intents.",
            isPro = true,
            section = "Message Detail Screen",
            benefits = listOf("Respond with 1-click professional drafts", "Incorporates your bio-credentials", "Maintains consistent active posture")
        ),
        InReachFeature(
            id = "detail_tamil_translation",
            name = "Tamil translation toggle",
            description = "Synthesize and translate message details into Tamil with Gemini AI semantic alignment.",
            isPro = true,
            section = "Message Detail Screen",
            benefits = listOf("Inclusive regional language support", "Preserves intent alignment precisely", "Translate messages in 1 tap")
        ),
        InReachFeature(
            id = "detail_ai_breakdown",
            name = "Full AI analysis breakdown",
            description = "Deep semantic analysis dashboard examining sender credibility, intent legitimacy, tone safety, and compatibility.",
            isPro = true,
            section = "Message Detail Screen",
            benefits = listOf("Exposes hidden spam indicators", "Highlights core technical requirements", "Confidence mapping on proposal success")
        ),

        // === ANALYTICS DASHBOARD ===
        InReachFeature(
            id = "analytics_dashboard",
            name = "Entire analytics dashboard",
            description = "Unlock the statistics cockpit to observe your incoming lead pipeline, metrics, and trends.",
            isPro = true,
            section = "Analytics Dashboard",
            benefits = listOf("High-level performance overview", "Visual tracking of opportunity funnels", "Understand incoming growth vectors")
        ),
        InReachFeature(
            id = "analytics_weekly_chart",
            name = "Weekly volume trend chart",
            description = "Interactive visualization detailing incoming message volumes over time to capture seasonality and outreach campaigns.",
            isPro = true,
            section = "Analytics Dashboard",
            benefits = listOf("Track historic intake numbers", "Monitor response efficiency curves", "Analyze professional outreach spikes")
        ),
        InReachFeature(
            id = "analytics_intent_dist",
            name = "Intent category distribution",
            description = "Breakdown of inquiries into Job Offers, Collaborations, Mentorships, and customizable intent vectors shown dynamically.",
            isPro = true,
            section = "Analytics Dashboard",
            benefits = listOf("Identify dominant demand sources", "Pivot your public credentials towards high-demand categories", "Real-time query segment mapping")
        ),
        InReachFeature(
            id = "analytics_engagement_stats",
            name = "Engagement rate + response time stats",
            description = "Calculates response speeds and acceptance ratios to showcase your active communication reliability index.",
            isPro = true,
            section = "Analytics Dashboard",
            benefits = listOf("Benchmark response velocity", "Build on public response reputation scores", "Keep interaction health optimal")
        ),
        InReachFeature(
            id = "analytics_demographics",
            name = "Sender demographics breakdown",
            description = "Provides geographic, background, and verification status segmentation of senders targeting your profile.",
            isPro = true,
            section = "Analytics Dashboard",
            benefits = listOf("Understand where your audience originates", "Filter lists based on geographical credentials", "Maximize targeted alignment ratios")
        ),
        InReachFeature(
            id = "analytics_export_pdf",
            name = "Export PDF report",
            description = "Generate an official, cryptographically verified PDF summary report containing all incoming pipeline stats and trust ratings.",
            isPro = true,
            section = "Analytics Dashboard",
            benefits = listOf("Share visual reports with project backer", "Keep hard records of intake details", "Professional, high-fidelity layouts")
        ),

        // === IDENTITY VERIFICATION ===
        InReachFeature(
            id = "id_tier1",
            name = "Tier 1 — Email OTP verification",
            description = "Basic confirmation of user identity via standard disposable-free email verification.",
            isPro = false,
            section = "Identity Verification",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "id_tier2",
            name = "Tier 2 — Phone OTP verification",
            description = "Secure SMS mobile verification block for double-checking target user geographic presence.",
            isPro = true,
            section = "Identity Verification",
            benefits = listOf("Secure identity from automated bots", "Double trust ratings immediately", "Enables higher tier permissions")
        ),
        InReachFeature(
            id = "id_tier3",
            name = "Tier 3 — AI video selfie confirmation",
            description = "Incorporate AI-powered liveness checkups and biometric signature matching dynamically inside your profile.",
            isPro = true,
            section = "Identity Verification",
            benefits = listOf("Foolproof anti-deepfake verification", "Highest security clearance badge", "Unlocks unlimited priority connections")
        ),

        // === LIVE CHAT (WORKSPACE) ===
        InReachFeature(
            id = "chat_text",
            name = "Real-time text chat",
            description = "Basic real-time messaging pipeline inside active mutual workspace channels.",
            isPro = false,
            section = "Live Chat",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "chat_attachments",
            name = "File attachments in chat",
            description = "Directly upload, share, and sandbox files, document bundles, or project templates inside active chat circles.",
            isPro = true,
            section = "Live Chat",
            benefits = listOf("Share code bundles or assets preview", "Integrates sandbox antivirus audit", "Access files on persistent timeline")
        ),
        InReachFeature(
            id = "chat_voice_notes",
            name = "Voice notes in chat",
            description = "Record and share voice files with native speech waveform preview inside workspace portals.",
            isPro = true,
            section = "Live Chat",
            benefits = listOf("Share audio updates with vocal nuances", "Built-in dynamic transcripts summary", "Highly accessible interface controls")
        ),
        InReachFeature(
            id = "chat_reactions",
            name = "Message reactions and thread replies",
            description = "Interact with emojis and target specific chat updates with full nested thread replies for clean communication history.",
            isPro = true,
            section = "Live Chat",
            benefits = listOf("Uncluttered conversations structure", "Express quick feedback in emojis", "Maintains clear context mapping")
        ),
        InReachFeature(
            id = "chat_invite_controls",
            name = "Workspace invite controls",
            description = "Control which sender types can initiate mutual workspaces. Restrict invitations strictly to high-verification-tier users.",
            isPro = true,
            section = "Live Chat",
            benefits = listOf("Prohibit low-trust connections completely", "Set boundary rules for team triggers", "Strict, customized intake management")
        ),

        // === PROPOSAL DOCUMENTS (WORKSPACE) ===
        InReachFeature(
            id = "doc_view",
            name = "View shared documents",
            description = "Accessible document board to read proposal agreements shared with partners.",
            isPro = false,
            section = "Proposal Documents",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "doc_co_edit",
            name = "Create and co-edit documents in real time",
            description = "Synchronized rich document editor with collaborative draft mechanics so multiple members can co-write concurrently.",
            isPro = true,
            section = "Proposal Documents",
            benefits = listOf("Eliminate file transfer iterations", "Instant live preview of updates", "Saves co-authorship records securely")
        ),
        InReachFeature(
            id = "doc_formatting",
            name = "Rich text formatting + export as PDF",
            description = "Format custom co-signed agreements in Markdown and export to high-fidelity PDF designs ready for external distribution.",
            isPro = true,
            section = "Proposal Documents",
            benefits = listOf("Produce standard document outlines", "Pristine styling templates built-in", "Create official files from drafts in 1 click")
        ),

        // === WHITEBOARD (WORKSPACE) ===
        InReachFeature(
            id = "whiteboard_basic",
            name = "Basic sticky notes whiteboard",
            description = "Simple online whiteboard page to place key sticky notes for collective ideation.",
            isPro = false,
            section = "Whiteboard",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "whiteboard_kolam_backdrop",
            name = "Kolam geometry backdrop + colour-coded notes",
            description = "Unlock customized ambient whiteboards styled with traditional South Indian geometric grids, helping structure notes perfectly.",
            isPro = true,
            section = "Whiteboard",
            benefits = listOf("Unique visual geometric guidance", "Organize sticky notes by custom themes", "Vibrant, creative session aesthetics")
        ),
        InReachFeature(
            id = "whiteboard_convert_agenda",
            name = "AI convert notes to meeting agenda",
            description = "Let Gemini AI aggregate scattered visual sticky notes and output a cohesive, action-oriented text meeting agenda automatically.",
            isPro = true,
            section = "Whiteboard",
            benefits = listOf("Saves pre-meeting coordination time", "Identifies key owners and tasks", "Ready-to-copy notes format")
        ),

        // === TASK BOARD (WORKSPACE) ===
        InReachFeature(
            id = "task_kanban",
            name = "Basic Kanban (To Do, In Progress, Done)",
            description = "Three-column project sprint tracker to align simple task delivery status.",
            isPro = false,
            section = "Task Board",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "task_custom_columns",
            name = "Custom columns + task assignee + due dates",
            description = "Create tailored multi-column sprints, explicitly assign deliverables, and set targeted calendar deadlines.",
            isPro = true,
            section = "Task Board",
            benefits = listOf("Adapt board to nested workflows", "Clarify individual accountability", "Visual alerts on expiring targets")
        ),
        InReachFeature(
            id = "task_priority_labels",
            name = "Task priority labels and filters",
            description = "Label tasks by critical priorities (High/Medium/Low) and filter your dashboard view instantly.",
            isPro = true,
            section = "Task Board",
            benefits = listOf("Focus on immediate bottlenecks", "Organize complex boards seamlessly", "Save sorting overheads")
        ),

        // === MEETINGS (WORKSPACE) ===
        InReachFeature(
            id = "meeting_link",
            name = "Share a meeting link in chat",
            description = "Convenient input row to write your Zoom/Meet links inside chat boards.",
            isPro = false,
            section = "Meetings",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "meeting_scheduler",
            name = "Built-in meeting scheduler + Google Calendar sync",
            description = "Native booking calendar showing availability windows directly to project partners, with two-way Google Calendar sync.",
            isPro = true,
            section = "Meetings",
            benefits = listOf("End the backward-and-forward email loop", "Avoid overlapping bookings", "Instant email confirm triggers")
        ),
        InReachFeature(
            id = "meeting_agenda",
            name = "AI meeting agenda from whiteboard notes",
            description = "Synthesize discussion outlines directly from whiteboard drafts to feed into scheduled meeting events.",
            isPro = true,
            section = "Meetings",
            benefits = listOf("Structured meeting preparation", "Enforce precise, goal-driven discussion", "Generates reference records")
        ),

        // === MILESTONES (WORKSPACE) ===
        InReachFeature(
            id = "milestone_view",
            name = "View milestones",
            description = "Progress indicator detailing fundamental contract delivery stages.",
            isPro = false,
            section = "Milestones",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "milestone_create",
            name = "Create and track milestones + notifications",
            description = "Collaborative milestone builder. Define delivery targets and get notifications on completions.",
            isPro = true,
            section = "Milestones",
            benefits = listOf("Structured roadmap transparency", "Real-time updates on task triggers", "Maintains active pace and safety")
        ),
        InReachFeature(
            id = "milestone_proof",
            name = "Proof of work outcome card",
            description = "Extract and display verifiable documents or certificates inside project milestone cards.",
            isPro = true,
            section = "Milestones",
            benefits = listOf("Build real portfolio proof of works", "Cryptographically secured outputs", "Easy sharing to other workspaces")
        ),

        // === PROFILE SCREEN ===
        InReachFeature(
            id = "profile_basic",
            name = "Public profile link + basic profile",
            description = "Standard digital card profile hosting name, bio, and verification icons.",
            isPro = false,
            section = "Profile Screen",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "profile_passport",
            name = "Opportunity Passport shareable card",
            description = "Generate a visually elegant, high-fidelity digital card of your credentials optimized for visual sharing.",
            isPro = true,
            section = "Profile Screen",
            benefits = listOf("Distinct aesthetic presentation", "Embed active reputation statistics", "Quick sharing on professional feeds")
        ),
        InReachFeature(
            id = "profile_scorecard",
            name = "Full credibility scorecard + resume vault",
            description = "Expose nested credibility indicators, response times, and store security-audited PDF portfolios inside a secure vault.",
            isPro = true,
            section = "Profile Screen",
            benefits = listOf("Establishes deep trust with recruiters", "Safe, verified CV downloads", "Detailed competency visualization")
        ),
        InReachFeature(
            id = "profile_priority_badge",
            name = "Priority sender badge on all sent messages",
            description = "Places a visible Priority checkmark on messages you issue to other nodes, guaranteeing fast evaluation.",
            isPro = true,
            section = "Profile Screen",
            benefits = listOf("Stand out inside noisy inboxes", "Unlocks top-queue positions for your intents", "Increases response rates by 2.5x")
        ),

        // === SETTINGS SCREEN ===
        InReachFeature(
            id = "set_privacy",
            name = "Basic privacy settings + theme toggle",
            description = "Toggle profile visibility, light/dark themes, and basic alert options.",
            isPro = false,
            section = "Settings Screen",
            benefits = emptyList()
        ),
        InReachFeature(
            id = "set_windows",
            name = "Availability windows editor",
            description = "Granular hourly and weekly planner that blocks message intakes except during designated open hours.",
            isPro = true,
            section = "Settings Screen",
            benefits = listOf("Define target hours for intake", "Respects personal and working blocks", "Drives organized, focused schedules")
        ),
        InReachFeature(
            id = "set_retention",
            name = "Data retention controls",
            description = "Configure custom auto-wipe rules on local chat databases, sticky notes, and cache to protect IP.",
            isPro = true,
            section = "Settings Screen",
            benefits = listOf("Configure automatic wiping rules", "Secure confidential communications", "Highest level of data privacy")
        ),
        InReachFeature(
            id = "set_notifications",
            name = "Notification preferences",
            description = "Choose which event types trigger push notifications (new message, accepted, milestone, etc.)",
            isPro = true,
            section = "Settings Screen",
            benefits = listOf("Mute non-priority noise alerts", "Never miss crucial milestone updates", "Highly customizable triggers")
        ),
        InReachFeature(
            id = "set_blocklist",
            name = "Sender blocklist management",
            description = "Manually block senders by email domain or username. View, audít, and manage block lists.",
            isPro = true,
            section = "Settings Screen",
            benefits = listOf("Prohibit troublesome domains", "Clean list overview", "Wipe outbound footprints instantly")
        ),
        InReachFeature(
            id = "set_intent_categories",
            name = "Custom intent categories",
            description = "Define your own intent types beyond standard system templates, sorting inquiries specifically.",
            isPro = true,
            section = "Settings Screen",
            benefits = listOf("Create custom intake pipelines", "Flexible, contextual forms creator", "Highly personalized node integration")
        )
    )
}
