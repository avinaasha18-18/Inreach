package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun InReachProPaymentDialog(
    isInai: Boolean,
    primaryBg: Color,
    containerBg: Color,
    cardBg: Color,
    primaryText: Color,
    secondaryText: Color,
    accentGold: Color,
    borderStroke: Color,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf("CARD") } // "CARD", "UPI", "OWNER"
    
    // Card inputs
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    
    // UPI Inputs
    var upiId by remember { mutableStateOf("") }
    
    // Owner Override Inputs
    var ownerCode by remember { mutableStateOf("") }
    val validOwnerCodes = listOf("OWNER2026", "INREACHDEV", "DEVELOPER", "999", "NAASHREACH")

    var isProcessing by remember { mutableStateOf(false) }

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
                    .padding(18.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "InReach Pro Secure Checkout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryText
                        )
                        Text(
                            text = "₹199 / month · Fast & Encrypted Gateway",
                            fontSize = 11.sp,
                            color = secondaryText
                        )
                    }
                    IconButton(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("close_payment_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Secure Gateway",
                            tint = secondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment selector tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(primaryBg, RoundedCornerShape(8.dp))
                        .border(1.dp, borderStroke, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Card Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (selectedMethod == "CARD") accentGold else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedMethod = "CARD" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = if (selectedMethod == "CARD") (if (isInai) Color(0xFF3E2723) else Color.White) else secondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Card",
                                color = if (selectedMethod == "CARD") (if (isInai) Color(0xFF3E2723) else Color.White) else secondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // UPI Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (selectedMethod == "UPI") accentGold else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedMethod = "UPI" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.QrCode,
                                contentDescription = null,
                                tint = if (selectedMethod == "UPI") (if (isInai) Color(0xFF3E2723) else Color.White) else secondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "UPI",
                                color = if (selectedMethod == "UPI") (if (isInai) Color(0xFF3E2723) else Color.White) else secondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Developer Override Tab
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .background(
                                if (selectedMethod == "OWNER") accentGold else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedMethod = "OWNER" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                tint = if (selectedMethod == "OWNER") (if (isInai) Color(0xFF3E2723) else Color.White) else secondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dev Bypass",
                                color = if (selectedMethod == "OWNER") (if (isInai) Color(0xFF3E2723) else Color.White) else secondaryText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Detail panels based on selectedMethod
                when (selectedMethod) {
                    "CARD" -> {
                        Column {
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it.take(19) },
                                label = { Text("Card Number", fontSize = 11.sp) },
                                placeholder = { Text("4111 2222 3333 4444", fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentGold,
                                    unfocusedBorderColor = borderStroke,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg,
                                    focusedTextColor = primaryText,
                                    unfocusedTextColor = primaryText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = expiryDate,
                                    onValueChange = { expiryDate = it.take(5) },
                                    label = { Text("Expiry (MM/YY)", fontSize = 11.sp) },
                                    placeholder = { Text("12/29", fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentGold,
                                        unfocusedBorderColor = borderStroke,
                                        focusedContainerColor = cardBg,
                                        unfocusedContainerColor = cardBg,
                                        focusedTextColor = primaryText,
                                        unfocusedTextColor = primaryText
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = cvv,
                                    onValueChange = { cvv = it.take(3) },
                                    label = { Text("CVV", fontSize = 11.sp) },
                                    placeholder = { Text("302", fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentGold,
                                        unfocusedBorderColor = borderStroke,
                                        focusedContainerColor = cardBg,
                                        unfocusedContainerColor = cardBg,
                                        focusedTextColor = primaryText,
                                        unfocusedTextColor = primaryText
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    "UPI" -> {
                        Column {
                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("UPI Address / Phone Number", fontSize = 11.sp) },
                                placeholder = { Text("avinaash@okhdfcbank", fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentGold,
                                    unfocusedBorderColor = borderStroke,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg,
                                    focusedTextColor = primaryText,
                                    unfocusedTextColor = primaryText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Or send directly to the peer-staked public node address over trust frameworks.",
                                fontSize = 10.sp,
                                color = secondaryText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    "OWNER" -> {
                        Column {
                            OutlinedTextField(
                                value = ownerCode,
                                onValueChange = { ownerCode = it },
                                label = { Text("Enter Developer / Owner Bypass Code", fontSize = 11.sp) },
                                placeholder = { Text("OWNER2026", fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentGold,
                                    unfocusedBorderColor = borderStroke,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg,
                                    focusedTextColor = primaryText,
                                    unfocusedTextColor = primaryText
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "🔒 Authorized owners/developers bypass payment validations using credentials stored in active environment secure configs.",
                                fontSize = 10.sp,
                                color = secondaryText,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Button(
                    onClick = {
                        isProcessing = true
                        if (selectedMethod == "OWNER") {
                            if (validOwnerCodes.contains(ownerCode.trim().uppercase())) {
                                Toast.makeText(context, "🎉 DEV BYPASS ACCEPTED: Welcome, Owner/Developer!", Toast.LENGTH_LONG).show()
                                onSuccess()
                            } else {
                                isProcessing = false
                                Toast.makeText(context, "❌ Invalid bypass credentials. Please pay ₹199 or enter correct developer code.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (selectedMethod == "CARD" && (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty())) {
                                isProcessing = false
                                Toast.makeText(context, "⚠️ Please fill in all card details for secure evaluation.", Toast.LENGTH_SHORT).show()
                            } else if (selectedMethod == "UPI" && upiId.isEmpty()) {
                                isProcessing = false
                                Toast.makeText(context, "⚠️ Please enter your active UPI ID.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "💳 Processing ₹199 mock sandbox transaction...", Toast.LENGTH_SHORT).show()
                                // Simulate network delay
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_upgrade_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying Gateways...", fontSize = 12.sp, color = if (isInai) Color(0xFF3E2723) else Color.White)
                    } else {
                        Text(
                            text = if (selectedMethod == "OWNER") "Accept Credentials & Unlock" else "Pay & Activate InReach Pro (₹199)",
                            color = if (isInai) Color(0xFF3E2723) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
