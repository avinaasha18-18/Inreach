package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun InReachProDetailPopup(
    feature: InReachFeature,
    isInai: Boolean,
    primaryBg: Color,
    containerBg: Color,
    cardBg: Color,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    borderStroke: Color,
    onUpgradeTap: () -> Unit,
    onSeeAllProFeatures: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.5.dp, accentGold, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = containerBg
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Topic Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                    .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(20.dp))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = "Pro badge",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "InReach Pro",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3B82F6)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = feature.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "Section: ${feature.section}",
                            fontSize = 10.sp,
                            color = secondaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .size(32.dp)
                            .background(primaryBg.copy(alpha = 0.6f), CircleShape)
                            .border(1.dp, borderStroke, CircleShape)
                            .testTag("close_feature_popup")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close feature popup",
                            tint = primaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Short Description
                Text(
                    text = feature.description,
                    fontSize = 12.sp,
                    color = primaryText,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Benefit summary checklist
                if (feature.benefits.isNotEmpty()) {
                    Text(
                        text = "WHAT THIS UNLOCKS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(primaryBg, RoundedCornerShape(10.dp))
                            .border(1.dp, borderStroke, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        feature.benefits.forEach { benefit ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "✦",
                                    color = accentGold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(
                                    text = benefit,
                                    color = primaryText,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CTA Button
                Button(
                    onClick = {
                        onDismiss()
                        onUpgradeTap()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("upgrade_button_gate"),
                    colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Upgrade to InReach Pro — ₹199/month",
                        color = if (isInai) Color(0xFF3E2723) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Link & Indication
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "See all Pro features",
                        color = Color(0xFF3B82F6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                onDismiss()
                                onSeeAllProFeatures()
                            }
                            .testTag("see_all_pro_link")
                    )
                }
            }
        }
    }
}
