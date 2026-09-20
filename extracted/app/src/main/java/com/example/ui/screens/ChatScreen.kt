package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiEngineState
import com.example.data.model.ModelStatus
import com.example.ui.MainViewModel
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.ThinkingIndicator
import com.example.ui.components.TopAppBar
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonGlowBorder
import com.example.ui.theme.NeonPurplePrimary
import com.example.ui.theme.NeonPurpleSecondary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onMenuClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.currentMessages.collectAsState()
    val modelStatus by viewModel.modelStatus.collectAsState()
    val engineState by viewModel.engineState.collectAsState()
    val currentConvId by viewModel.currentConversationId.collectAsState()

    val isSearching by viewModel.isSearching.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val isGenerating = engineState is AiEngineState.Generating
    val isFileProcessing by viewModel.isFileProcessing.collectAsState()

    // Auto-scroll to newest message
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Top Bar
        TopAppBar(
            modelStatus = modelStatus,
            onMenuClick = onMenuClick,
            onSearchClick = {
                viewModel.isSearching.value = !viewModel.isSearching.value
                if (!viewModel.isSearching.value) viewModel.searchQuery.value = ""
            },
            onClearChat = { viewModel.clearCurrentChat() },
            onExportChat = {
                val transcript = messages.joinToString("\n\n") { msg ->
                    val sender = if (msg.fromUser) "User" else "TODO AI"
                    "[$sender]: ${msg.text}"
                }
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "TODO AI Chat Transcript")
                    putExtra(Intent.EXTRA_TEXT, transcript)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Export Chat"))
            },
            onDeleteChat = {
                currentConvId?.let { viewModel.deleteConversation(it) }
            },
            onOpenSettings = onOpenSettings,
            onOpenAbout = onOpenAbout
        )

        // Search Bar overlay
        AnimatedVisibility(visible = isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCanvas)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search messages...", color = TextMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NeonPurpleSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardElevated,
                        unfocusedContainerColor = DarkCardElevated,
                        focusedBorderColor = NeonPurplePrimary,
                        unfocusedBorderColor = DarkSurfaceStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("search_text_input")
                )
            }
        }

        // Search Results List (if search query entered)
        if (isSearching && searchQuery.isNotBlank()) {
            Text(
                text = "Search Results (${searchResults.size})",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (searchResults.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Text("No messages match '$searchQuery'", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(searchResults) { match ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkCardElevated)
                                .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.loadConversation(match.conversationId)
                                }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (match.fromUser) "You" else "TODO AI",
                                    color = NeonPurpleSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = match.text,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Main Chat Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        val isThinking = isGenerating &&
                                message.id == messages.lastOrNull()?.id &&
                                !message.fromUser

                        ChatMessageItem(
                            message = message,
                            isThinking = isThinking
                        )
                    }
                }
if (isGenerating) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 52.dp, bottom = 12.dp)
    ) {
        ThinkingIndicator()
    }
}
                // Banner if model is required
                if (modelStatus == ModelStatus.NOT_INSTALLED || modelStatus == ModelStatus.NOT_LOADED) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCardElevated)
                            .border(1.dp, StatusAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = StatusAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Local AI model required",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Import or load an offline model to chat.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onOpenSettings,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonPurplePrimary,
                                    contentColor = DeepBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("setup_model_button")
                            ) {
                                Text("Setup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Bar
        ChatInputBar(
    inputText = inputText,
    onInputChange = { inputText = it },
    onSendMessage = { text ->
        viewModel.sendMessage(text)
    },
    isGenerating = isGenerating,
    onCancelGeneration = { viewModel.cancelGeneration() },
    onQuickToolSelect = {
        onOpenTools()
    },
    onFileSelected = { uri, mimeType ->
        viewModel.onFileSelected(uri, mimeType)
    }
)
    }
}
