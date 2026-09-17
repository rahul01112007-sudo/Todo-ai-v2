package com.example.ui.components
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPurplePrimary
import com.example.ui.theme.NeonPurpleSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isGenerating: Boolean,
    onCancelGeneration: () -> Unit,
    onQuickToolSelect: (String) -> Unit,
onFileSelected: (Uri, String) -> Unit = { _, _ -> },
modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showQuickMenu by remember { mutableStateOf(false) }
    val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri ->
    if (uri != null) {
        val mimeType =
            context.contentResolver.getType(uri)
                ?: "application/octet-stream"

        onFileSelected(uri, mimeType)
    }
    }

    // Speech recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val updated = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
                onInputChange(updated)
            }
        }
    }

    // Permission launcher for microphone
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to TODO Offline AI...")
                }
                try {
                    speechLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Voice input unavailable: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Offline speech recognition not available on this device", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkCardSurface)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Quick Prompts / Tools Bar when "+" is toggled
        AnimatedVisibility(visible = showQuickMenu) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(
                    listOf(
                        Pair("📎 Attach File", "attach_file")
                        Pair("Summarize text", "Can you summarize the following text into key bullet points: "),
                        Pair("Write code", "Write a clean Kotlin function to "),
                        Pair("Calculate", "Calculate "),
                        Pair("Explain offline AI", "Explain how local on-device AI models protect my data privacy.")
                    )
                ) { (chipLabel, promptTemplate) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
.background(DarkCardElevated)
.border(1.dp, DarkSurfaceStroke, RoundedCornerShape(16.dp))
.clickable {
    if (promptTemplate == "attach_file") {
        filePickerLauncher.launch(
            arrayOf(
                "image/*",
                "application/pdf",
                "text/*",
                "application/*"
            )
        )
    } else {
        onInputChange(promptTemplate)
    }

    showQuickMenu = false
}
.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chipLabel,
                            color = NeonPurpleSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // "+" Button
            IconButton(
                onClick = {
    showQuickMenu = !showQuickMenu
},
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (showQuickMenu) DeepViolet else DarkCardElevated)
                    .testTag("attachment_button")
            ) {
                Icon(
                    imageVector = if (showQuickMenu) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Quick Tools and Prompts",
                    tint = NeonPurpleSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Rounded Input Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkCardElevated)
                    .border(
                        width = 1.dp,
                        color = if (inputText.isNotBlank()) NeonPurplePrimary.copy(alpha = 0.5f) else DarkSurfaceStroke,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "Message TODO...",
                        color = TextMuted,
                        fontSize = 14.5.sp
                    )
                }

                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 14.5.sp
                    ),
                    cursorBrush = SolidColor(NeonPurplePrimary),
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 22.dp)
                        .testTag("chat_text_input")
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Microphone Button
            IconButton(
                onClick = {
                    val permissionCheck = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        if (SpeechRecognizer.isRecognitionAvailable(context)) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to TODO Offline AI...")
                            }
                            try {
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Voice input error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Speech recognition unavailable offline", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated)
                    .testTag("mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send / Stop Button
            val isEnabled = inputText.isNotBlank() || isGenerating
            IconButton(
                onClick = {
                    if (isGenerating) {
                        onCancelGeneration()
                    } else if (inputText.isNotBlank()) {
                        val messageToSend = inputText
                        onInputChange("")
                        onSendMessage(messageToSend)
                    }
                },
                enabled = isEnabled,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isGenerating) ErrorRed else if (inputText.isNotBlank()) NeonPurplePrimary else DarkCardElevated.copy(alpha = 0.5f),
                    contentColor = if (inputText.isNotBlank() || isGenerating) DeepBlack else TextMuted
                ),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = if (isGenerating) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                    contentDescription = if (isGenerating) "Cancel generation" else "Send message",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
