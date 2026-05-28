package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CardPhysics
import com.example.data.MessageEntity

@Composable
fun ShakePhysicsView(
    isSelectedInai: Boolean,
    cards: List<CardPhysics>,
    rawMessages: List<MessageEntity>,
    isSimulating: Boolean,
    onTriggerShake: () -> Unit,
    onCardClicked: (Int) -> Unit
) {
    // Background style
    val paneBg = if (isSelectedInai) Color(0xFFF5ECE1) else Color(0xFF112240)
    val cardBg = if (isSelectedInai) Color(0xFFEADAC2) else Color(0xFF1A2E5A)
    val cardAccent = if (isSelectedInai) Color(0xFFD49F43) else Color(0xFFE8B84B)
    val borderCol = if (isSelectedInai) Color(0xFF4E342E) else Color(0xFF2E4057)
    val txtCol = if (isSelectedInai) Color(0xFF3E2723) else Color(0xFFFFFFFF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .background(paneBg, RoundedCornerShape(16.dp))
            .border(2.dp, borderCol, RoundedCornerShape(16.dp))
    ) {
        // Subtle watermark or Kolam background in INAI mode
        if (isSelectedInai) {
                DrawingKolam(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.Center)
                        .graphicsLayer(alpha = 0.15f),
                    color = Color(0xFFA16B56)
                )
        }

        // Top Header Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Magnetic Message Pocket",
                    color = txtCol,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isSimulating) "💥 Tumbling with Newton Gravity..." else "Unread cards stick to magnetic corners.",
                    color = if (isSelectedInai) Color(0xFF795548) else Color(0xFF8892B0),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onTriggerShake,
                colors = ButtonDefaults.buttonColors(containerColor = cardAccent),
                modifier = Modifier.testTag("shake_button")
            ) {
                Text(
                    text = if (isSimulating) "Shaking..." else "📳 Shake Screen",
                    color = if (isSelectedInai) Color(0xFF3E2723) else Color(0xFF0D1B2A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // If cards are empty, show clean empty indicator
        if (cards.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Mail,
                    contentDescription = "Empty",
                    tint = cardAccent,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Unread corner cards left!",
                    color = txtCol,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Render each falling physical card
        cards.forEach { card ->
            Box(
                modifier = Modifier
                    .offset(x = (card.x / 2.2f).dp, y = (card.y / 2.2f).dp)
                    .graphicsLayer(rotationZ = card.rotation)
                    .size(width = 160.dp, height = 85.dp)
                    .background(cardBg, RoundedCornerShape(8.dp))
                    .border(
                        width = if (card.score >= 85) 2.dp else 1.dp,
                        color = if (card.score >= 85) cardAccent else borderCol,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onCardClicked(card.id) }
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = card.category,
                            color = cardAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .background(cardAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Score: ${card.score}",
                                color = cardAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = card.text,
                        color = txtCol,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Touch to view questionnaire",
                        color = if (isSelectedInai) Color(0xFF795548) else Color(0xFF8892B0),
                        fontSize = 8.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
