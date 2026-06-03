package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InReachProInspectPage(
    isInai: Boolean,
    isDark: Boolean,
    primaryBg: Color,
    containerBg: Color,
    cardBg: Color,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    borderStroke: Color,
    isPro: Boolean,
    onUpgradeTap: () -> Unit,
    onFeatureTap: (InReachFeature) -> Unit,
    onClose: () -> Unit
) {
    // Keep track of expanded sections for the collapsible accordions
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }
    
    // Initialize all sections as expanded by default
    LaunchedEffect(Unit) {
        InReachProFeatures.sections.forEach { section ->
            expandedSections[section] = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBg)
    ) {
        // --- Top App Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerBg)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onClose() },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("back_button_inspect")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back",
                    tint = primaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "InReach Pro Service Catalog",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                Text(
                    text = "Verify and audit gateway modules.",
                    fontSize = 10.sp,
                    color = secondaryText
                )
            }
        }
        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(borderStroke))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // --- Premium Header Hero Brand ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = containerBg),
                    border = BorderStroke(1.5.dp, accentGold),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(accentGold.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, accentGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = accentGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "InReach Pro",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "One plan. Everything unlocked. No confusion.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = secondaryText,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .background(primaryBg, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .border(1.dp, borderStroke, RoundedCornerShape(20.dp))
                        ) {
                            Text(
                                text = "₹199 / month",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "·",
                                fontSize = 12.sp,
                                color = secondaryText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cancel anytime · Less than one Swiggy order",
                                fontSize = 10.sp,
                                color = secondaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(primaryBg, RoundedCornerShape(12.dp))
                                .border(1.dp, borderStroke, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("10", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = primaryText)
                                Text("Always Free", fontSize = 9.sp, color = secondaryText, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(26.dp)
                                    .background(borderStroke)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("27", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = accentGold)
                                Text("Pro Features", fontSize = 9.sp, color = secondaryText, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(26.dp)
                                    .background(borderStroke)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("₹199", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = primaryText)
                                Text("Single Price", fontSize = 9.sp, color = secondaryText, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // --- Collapsible Feature Sections ---
            InReachProFeatures.sections.forEach { sectionName ->
                val sectionFeatures = InReachProFeatures.list.filter { it.section == sectionName }
                val isExpanded = expandedSections[sectionName] ?: true

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(containerBg, RoundedCornerShape(12.dp))
                            .border(1.dp, borderStroke, RoundedCornerShape(12.dp))
                    ) {
                        // Section header accordion tap
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedSections[sectionName] = !isExpanded }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(accentGold.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = accentGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sectionName.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentGold,
                                    letterSpacing = 1.sp
                                )
                            }
                            
                            // Features count indicators
                            val proCount = sectionFeatures.count { it.isPro }
                            val freeCount = sectionFeatures.size - proCount
                            Text(
                                text = "$freeCount Free | $proCount Pro",
                                fontSize = 9.sp,
                                color = secondaryText,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp)
                            ) {
                                // Add a divider
                                Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(borderStroke))
                                Spacer(modifier = Modifier.height(8.dp))

                                sectionFeatures.forEachIndexed { index, feature ->
                                    if (index > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(borderStroke.copy(alpha = 0.5f)))
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    // Indivudual feature click to trigger details modal
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onFeatureTap(feature) }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = feature.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryText
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                // Pill Badge Type
                                                val badgeColor = if (feature.isPro) Color(0xFF3B82F6) else Color(0xFF10B981)
                                                val badgeText = if (feature.isPro) "PRO" else "FREE"
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                                        .border(1.dp, badgeColor, RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = badgeText,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = badgeColor
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = feature.description,
                                                fontSize = 11.sp,
                                                color = secondaryText,
                                                lineHeight = 14.sp
                                            )
                                        }

                                        // Lock Icon indicators
                                        if (feature.isPro && !isPro) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked feature",
                                                tint = accentGold,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .padding(4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // --- Bottom Sticky CTA Row ---
        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(borderStroke))
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = containerBg,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Subscription Status indicator
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT BOUND PROFILE LEVEL :",
                            fontSize = 9.sp,
                            color = secondaryText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPro) "INREACH PRO MEMBERSHIP ACTIVE" else "STANDARD GATEWAY FREE PLAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPro) Color(0xFF10B981) else secondaryText
                        )
                    }
                    if (isPro) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, Color(0xFF10B981), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Active Node", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(borderStroke, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Free Plan", color = secondaryText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (!isPro) {
                    Button(
                        onClick = { onUpgradeTap() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("inspect_primary_upgrade_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Upgrade to InReach Pro (₹199 / Month)",
                            color = if (isInai) Color(0xFF3E2723) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            // Developer tool logic for canceling subscription safely during evaluation
                            onUpgradeTap()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("inspect_cancel_pro_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Deactivate Pro Subscription (Dev Tool)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Always free features remain accessible indefinitely for all nodes inside the peer workspace.",
                    fontSize = 10.sp,
                    color = secondaryText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
