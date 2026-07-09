package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.viewmodel.ChatUiState
import com.example.ui.viewmodel.NutriViewModel

// Unified Cosmic Palette
private val HdPrimaryPurple = Color(0xFF10B981) // EmeraldMint
private val HdDarkText = Color.White
private val HdSecondaryText = Color(0xFF94A3B8)
private val HdBackground = Color(0xFF0F172A)
private val HdCardBg = Color(0xFF1E293B)
private val HdBorderColor = Color(0xFF334155)
private val HdAccentPill = Color(0xFF1E293B)
private val HdAccentGreen = Color(0xFF10B981).copy(alpha = 0.15f)
private val HdGreenText = Color(0xFF10B981)
private val EmeraldMint = Color(0xFF10B981)
private val CoralRed = Color(0xFFEF4444)
private val DeepSkyBlue = Color(0xFF6366F1) // NeonIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatCoachScreen(
    viewModel: NutriViewModel,
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val chatUiState by viewModel.chatUiState.collectAsState()
    var inputMessage by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    val suggestions = remember {
        listOf(
            "Give me some high-protein snacks 🍳",
            "What's a fast low-carb dinner idea? 🥗",
            "I ate a chicken bowl for lunch!",
            "Explain keto macro ratios simply 🥑",
            "How can I manage sweet cravings? 🍫"
        )
    }

    // Auto-scroll to the bottom when a new chat message arrives
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HdBackground)
    ) {
        // 1. App Header inside Coach Screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(HdPrimaryPurple.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = HdPrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AI Nutrition Coach",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = HdDarkText
                        )
                    )
                    Text(
                        text = "Talk to log food or ask questions",
                        style = MaterialTheme.typography.labelSmall.copy(color = HdSecondaryText)
                    )
                }
            }

            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HdCardBg)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = CoralRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 2. Chat history lazy list
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { msg ->
                val isSystemMsg = msg.message.startsWith("📝 System:")
                
                ChatBubble(
                    message = msg,
                    isSystem = isSystemMsg
                )
            }

            // Typing state indicator
            if (chatUiState is ChatUiState.Sending) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(HdCardBg)
                                .border(BorderStroke(1.dp, HdBorderColor), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Coach is thinking...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = HdPrimaryPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = HdPrimaryPurple,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Horizontal list of suggestions
        if (chatMessages.size <= 2) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { text ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(HdCardBg)
                            .border(BorderStroke(1.dp, HdBorderColor), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.sendChatMessage(text)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HdDarkText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // 4. Input layout row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HdBackground)
                .border(BorderStroke(1.dp, HdBorderColor.copy(alpha = 0.4f)))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                placeholder = {
                    Text(
                        "e.g. I had two bananas for breakfast",
                        style = MaterialTheme.typography.bodyMedium.copy(color = HdSecondaryText.copy(alpha = 0.6f))
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = HdDarkText,
                    unfocusedTextColor = HdDarkText,
                    focusedContainerColor = HdCardBg,
                    unfocusedContainerColor = HdCardBg,
                    focusedBorderColor = HdPrimaryPurple,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        viewModel.sendChatMessage(inputMessage.trim())
                        inputMessage = ""
                    }
                },
                enabled = chatUiState !is ChatUiState.Sending && inputMessage.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (inputMessage.isNotBlank()) HdPrimaryPurple else HdCardBg)
                    .testTag("send_chat_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputMessage.isNotBlank()) Color.White else HdSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isSystem: Boolean
) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSystem) Arrangement.Center else if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (isSystem) {
            Card(
                colors = CardDefaults.cardColors(containerColor = HdAccentGreen),
                border = BorderStroke(1.dp, HdPrimaryPurple.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HdGreenText,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        } else {
            val bubbleColor = if (isUser) Color(0xFF334155) else HdCardBg
            val textColor = Color.White
            val bubbleShape = if (isUser) {
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp)
            } else {
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp)
            }

            Column(
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isUser) "You" else "AI Coach",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HdSecondaryText,
                        fontWeight = FontWeight.Bold
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(bubbleColor)
                        .then(
                            if (!isUser) {
                                Modifier.border(BorderStroke(1.dp, HdBorderColor), bubbleShape)
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}
