package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.DreamEntry
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekLilac
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SymbolChatSection(
    dream: DreamEntry,
    chatMessages: List<ChatMessage>,
    isSending: Boolean,
    onSendMessage: (text: String, model: String) -> Unit,
    onClearChat: () -> Unit,
    prefilledQuery: String? = null,
    onPrefilledHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("gemini-3.5-flash") }
    var showModelMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(prefilledQuery) {
        if (!prefilledQuery.isNullOrBlank()) {
            inputText = prefilledQuery
            onPrefilledHandled()
        }
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Dynamic suggested symbol questions
    val suggestedPrompts = remember(dream) {
        val list = mutableListOf<String>()
        list.add("What is the psychological message of this dream?")
        if (dream.emotionalTheme.isNotBlank()) {
            list.add("How can I explore '${dream.emotionalTheme}' in waking life?")
        }
        list.add("What archetype was most active in this scenario?")
        list.add("How can I do active imagination with the key symbols?")
        list
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, SleekCardBorder, RoundedCornerShape(24.dp)),
        color = SleekWhite,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Model Selector & Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(SleekPrimaryContainer, CircleShape)
                            .border(1.dp, SleekPrimary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Jungian Symbol Inquiry",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Dialogue with Dr. Jung's Depth Analyst",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = SleekPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Model switcher button
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekSurfaceVariant)
                                .border(1.dp, SleekCardBorder, RoundedCornerShape(8.dp))
                                .clickable { showModelMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (selectedModel) {
                                    "gemini-3.1-pro-preview" -> "Pro Preview"
                                    "gemini-3.1-flash-lite-preview" -> "Flash Lite"
                                    else -> "3.5 Flash"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SleekPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Change Model",
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showModelMenu,
                            onDismissRequest = { showModelMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("gemini-3.5-flash", fontWeight = FontWeight.Bold)
                                        Text("General & Balanced reasoning", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                },
                                onClick = {
                                    selectedModel = "gemini-3.5-flash"
                                    showModelMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("gemini-3.1-pro-preview", fontWeight = FontWeight.Bold)
                                        Text("Complex depth psychology tasks", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                },
                                onClick = {
                                    selectedModel = "gemini-3.1-pro-preview"
                                    showModelMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("gemini-3.1-flash-lite-preview", fontWeight = FontWeight.Bold)
                                        Text("Fast symbol decoding", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                },
                                onClick = {
                                    selectedModel = "gemini-3.1-flash-lite-preview"
                                    showModelMenu = false
                                }
                            )
                        }
                    }

                    if (chatMessages.isNotEmpty()) {
                        IconButton(
                            onClick = onClearChat,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Chat History",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Suggested prompt chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(suggestedPrompts) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekSurfaceVariant)
                            .border(1.dp, SleekCardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                inputText = prompt
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Chat Thread
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(SleekSurfaceVariant, RoundedCornerShape(16.dp))
                    .border(1.dp, SleekCardBorder, RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = SleekPrimary.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ask about any symbol, figure, or emotion",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The analyst draws on archetypal amplification & your dream transcript",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { message ->
                            ChatBubbleItem(
                                message = message,
                                onCopyText = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Dream Analysis", message.text))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        if (isSending) {
                            item {
                                Row(
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = SleekPrimary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dr. Jung's analyst is contemplating the symbol...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chat Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask about a symbol (e.g., 'What did the owl mean?')",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekCardBorder,
                        focusedContainerColor = SleekWhite,
                        unfocusedContainerColor = SleekWhite,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !isSending) {
                                val text = inputText.trim()
                                inputText = ""
                                keyboardController?.hide()
                                onSendMessage(text, selectedModel)
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isSending) {
                            val text = inputText.trim()
                            inputText = ""
                            keyboardController?.hide()
                            onSendMessage(text, selectedModel)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (inputText.isNotBlank() && !isSending) SleekPrimary else SleekSurfaceVariant,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isSending) SleekWhite else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    onCopyText: () -> Unit
) {
    val isUser = message.sender == "USER"
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(SleekPrimaryContainer, CircleShape)
                    .border(1.dp, SleekPrimary.copy(alpha = 0.3f), CircleShape)
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI",
                    tint = SleekPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) SleekPrimary else SleekWhite,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUser) SleekPrimary else SleekCardBorder
                ),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = if (isUser) SleekWhite else TextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (isUser) SleekWhite.copy(alpha = 0.7f) else TextSecondary
                        )
                        if (!isUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { onCopyText() }
                            )
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(SleekPrimaryContainer, CircleShape)
                    .border(1.dp, SleekPrimary.copy(alpha = 0.3f), CircleShape)
                    .align(Alignment.Top),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = SleekPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

