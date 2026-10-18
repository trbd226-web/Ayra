package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.AyraViewModel
import com.example.ui.components.VoiceOrb
import com.example.ui.theme.AyraCardBorder
import com.example.ui.theme.AyraCyan
import com.example.ui.theme.AyraError
import com.example.ui.theme.AyraMidnightBg
import com.example.ui.theme.AyraPink
import com.example.ui.theme.AyraSuccess
import com.example.ui.theme.AyraSurfaceDark
import com.example.ui.theme.AyraSurfaceVariant
import com.example.ui.theme.AyraTextPrimary
import com.example.ui.theme.AyraTextSecondary
import com.example.ui.theme.AyraViolet
import com.example.voice.VoiceState
import java.util.Locale

@Composable
fun MainVoiceAssistantScreen(
    viewModel: AyraViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val voiceState by viewModel.voiceState.collectAsState()
    val rmsAmplitude by viewModel.rmsAmplitude.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isSessionActive by viewModel.isSessionActive.collectAsState()
    val remainingSeconds by viewModel.sessionRemainingSeconds.collectAsState()
    val currentPrompt by viewModel.currentPrompt.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val spokenResponse by viewModel.spokenResponseText.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmationCommand.collectAsState()
    val speechError by viewModel.speechError.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showTextInput by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.triggerSingleListening()
        }
    }

    val onMicClick = {
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (hasMic) {
            viewModel.triggerSingleListening()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Confirmation Dialog for Sensitive Actions
    if (pendingConfirmation != null) {
        val cmd = pendingConfirmation!!
        AlertDialog(
            onDismissRequest = { viewModel.cancelPendingAction() },
            title = {
                Text(
                    text = "Confirm Action",
                    color = AyraCyan,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = cmd.confirmationPrompt.ifBlank { "Do you want to proceed with: ${cmd.target}?" },
                    color = AyraTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingAction() },
                    colors = ButtonDefaults.buttonColors(containerColor = AyraCyan, contentColor = Color.Black)
                ) {
                    Text("Proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelPendingAction() }) {
                    Text("Cancel", color = AyraTextSecondary)
                }
            },
            containerColor = AyraSurfaceDark,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AyraMidnightBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AyraSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_ayra_logo),
                        contentDescription = "AYRA Logo",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "AYRA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AyraCyan
                    )
                    Text(
                        text = "Voice Assistant",
                        fontSize = 11.sp,
                        color = AyraTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSessionActive) {
                    val mins = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)

                    Surface(
                        color = AyraCyan.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AyraCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Active Session Timer",
                                tint = AyraCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = timeStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AyraCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.stopSession() },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("stop_session_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Active Session",
                            tint = AyraError
                        )
                    }
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Open Settings",
                        tint = AyraViolet
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Tag
        val statusText = when {
            voiceState == VoiceState.LISTENING -> "Listening to your voice..."
            voiceState == VoiceState.PROCESSING -> "Interpreting command..."
            isSpeaking || voiceState == VoiceState.SPEAKING -> "Ayra speaking..."
            voiceState == VoiceState.ERROR -> speechError ?: "Speech error"
            isSessionActive -> "Active Session • Say 'Hey Ayra' or tap Orb"
            else -> "Idle • Tap Orb to Speak"
        }

        val statusColor = when {
            voiceState == VoiceState.LISTENING -> AyraCyan
            voiceState == VoiceState.PROCESSING -> AyraPink
            isSpeaking || voiceState == VoiceState.SPEAKING -> AyraViolet
            voiceState == VoiceState.ERROR -> AyraError
            isSessionActive -> AyraSuccess
            else -> AyraTextSecondary
        }

        Surface(
            color = statusColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
        ) {
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Voice Orb
        VoiceOrb(
            voiceState = voiceState,
            rmsAmplitude = rmsAmplitude,
            isSpeaking = isSpeaking,
            onClick = onMicClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live Transcript & Spoken Response Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // User Prompt / Partial transcript
                val userSpeech = when {
                    partialTranscript.isNotBlank() -> partialTranscript
                    currentPrompt.isNotBlank() -> currentPrompt
                    else -> "Say something like 'Hey Ayra, open YouTube' or 'Turn on flashlight'"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "YOU",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AyraViolet,
                        letterSpacing = 1.sp
                    )

                    if (voiceState == VoiceState.LISTENING) {
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AyraCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userSpeech,
                    fontSize = 15.sp,
                    color = if (partialTranscript.isNotBlank() || currentPrompt.isNotBlank()) AyraTextPrimary else AyraTextSecondary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AyraCardBorder.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Assistant Response
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "AYRA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AyraCyan,
                        letterSpacing = 1.sp
                    )

                    Row {
                        IconButton(
                            onClick = { viewModel.repeatLastResponse() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Repeat Spoken Response",
                                tint = AyraCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (isSpeaking) {
                            IconButton(
                                onClick = { viewModel.stopSpeaking() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = "Stop Speaking",
                                    tint = AyraPink,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = spokenResponse,
                    fontSize = 14.sp,
                    color = AyraTextPrimary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Command Suggestions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "SUGGESTED COMMANDS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AyraTextSecondary,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val sampleCommands = listOf(
            "Open YouTube",
            "Turn on flashlight",
            "Battery level",
            "Call Mother",
            "Scroll down",
            "Take a screenshot",
            "Save note: Buy groceries",
            "Search for offline games",
            "ক্যামেরা খোলো",
            "টর্চ জ্বালাও",
            "Stop listening"
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleCommands) { cmd ->
                Surface(
                    color = AyraSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AyraCardBorder),
                    modifier = Modifier.clickable {
                        viewModel.handleRecognizedInput(cmd)
                    }
                ) {
                    Text(
                        text = cmd,
                        color = AyraCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Text input toggle & field (Optional alternative, never the primary interface)
        if (showTextInput) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Type command as alternative...", color = AyraTextSecondary, fontSize = 13.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendTextMessage(textInput)
                        textInput = ""
                    }
                }),
                trailingIcon = {
                    IconButton(onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendTextMessage(textInput)
                            textInput = ""
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send Command", tint = AyraCyan)
                    }
                },
                leadingIcon = {
                    IconButton(onClick = { showTextInput = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Text Input", tint = AyraTextSecondary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AyraCyan,
                    unfocusedBorderColor = AyraCardBorder,
                    focusedTextColor = AyraTextPrimary,
                    unfocusedTextColor = AyraTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("text_command_input")
            )
        } else {
            TextButton(
                onClick = { showTextInput = true },
                modifier = Modifier.testTag("toggle_text_input")
            ) {
                Text(
                    text = "Type a command instead",
                    fontSize = 12.sp,
                    color = AyraTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSessionActive) {
                Button(
                    onClick = { viewModel.stopSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = AyraError.copy(alpha = 0.15f), contentColor = AyraError),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stop AYRA")
                }
            } else {
                Button(
                    onClick = { viewModel.startListeningSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = AyraCyan, contentColor = Color(0xFF001F29)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .testTag("start_session_button")
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Start Voice Session", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Voice Session", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
