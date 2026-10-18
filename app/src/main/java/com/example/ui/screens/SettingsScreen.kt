package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AyraViewModel
import com.example.ui.theme.AyraCardBorder
import com.example.ui.theme.AyraCyan
import com.example.ui.theme.AyraMidnightBg
import com.example.ui.theme.AyraSuccess
import com.example.ui.theme.AyraSurfaceDark
import com.example.ui.theme.AyraSurfaceVariant
import com.example.ui.theme.AyraTextPrimary
import com.example.ui.theme.AyraTextSecondary
import com.example.ui.theme.AyraViolet
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: AyraViewModel,
    onBack: () -> Unit,
    onNavigate: (route: String) -> Unit
) {
    val context = LocalContext.current
    val preferences = viewModel.preferences

    val apiKey by preferences.geminiApiKey.collectAsState()
    val selectedModel by preferences.selectedModel.collectAsState()
    val wakeWordEnabled by preferences.wakeWordEnabled.collectAsState()
    val bgSessionEnabled by preferences.backgroundSessionEnabled.collectAsState()
    val sessionDuration by preferences.sessionDurationMinutes.collectAsState()
    val speechLang by preferences.speechLanguage.collectAsState()
    val speechRate by preferences.speechRate.collectAsState()
    val voiceRespEnabled by preferences.voiceResponseEnabled.collectAsState()
    val confirmSensitive by preferences.confirmSensitiveActions.collectAsState()

    val apiTestStatus by viewModel.apiTestStatus.collectAsState()
    val isTestingApi by viewModel.isTestingApi.collectAsState()

    var tempKeyInput by remember(apiKey) { mutableStateOf(apiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AyraMidnightBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AyraCyan
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Settings & AI Configuration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AyraTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gemini AI Section
        SettingsSectionHeader(title = "GEMINI INTELLIGENCE", icon = Icons.Default.VpnKey)

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Gemini API Key",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AyraTextPrimary
                )
                Text(
                    text = "Used for natural language reasoning, Bengali translation, and web queries. AYRA also works offline for phone controls.",
                    fontSize = 12.sp,
                    color = AyraTextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tempKeyInput,
                    onValueChange = { tempKeyInput = it },
                    placeholder = { Text("Enter your Gemini API key", color = AyraTextSecondary, fontSize = 13.sp) },
                    visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Key Visibility",
                                tint = AyraTextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AyraCyan,
                        unfocusedBorderColor = AyraCardBorder,
                        focusedTextColor = AyraTextPrimary,
                        unfocusedTextColor = AyraTextPrimary
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { preferences.setGeminiApiKey(tempKeyInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = AyraCyan, contentColor = Color(0xFF001F29)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Key", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.testGeminiApi(tempKeyInput.ifBlank { preferences.getEffectiveApiKey() }, selectedModel) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTestingApi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AyraCyan, strokeWidth = 2.dp)
                        } else {
                            Text("Test API", fontSize = 13.sp, color = AyraCyan)
                        }
                    }
                }

                if (apiTestStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = apiTestStatus!!,
                        fontSize = 12.sp,
                        color = if (apiTestStatus!!.startsWith("Failed")) AyraViolet else AyraSuccess
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AI Model Selection",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AyraTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedModel == "gemini-3.5-flash",
                        onClick = { preferences.setSelectedModel("gemini-3.5-flash") },
                        label = { Text("gemini-3.5-flash (Fast)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AyraCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AyraCyan
                        )
                    )

                    FilterChip(
                        selected = selectedModel == "gemini-3.1-pro-preview",
                        onClick = { preferences.setSelectedModel("gemini-3.1-pro-preview") },
                        label = { Text("gemini-3.1-pro") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AyraViolet.copy(alpha = 0.2f),
                            selectedLabelColor = AyraViolet
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Voice & Session Settings
        SettingsSectionHeader(title = "VOICE & BACKGROUND SESSION", icon = Icons.Default.Timer)

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Wake phrase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Wake Word: 'Hey Ayra'", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AyraTextPrimary)
                        Text(text = "Responds to 'Hey Ayra', 'Ayra', and 'আইরা'", fontSize = 12.sp, color = AyraTextSecondary)
                    }
                    Switch(
                        checked = wakeWordEnabled,
                        onCheckedChange = { preferences.setWakeWordEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AyraCyan, checkedTrackColor = AyraCyan.copy(alpha = 0.4f))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Background listening
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Background Session", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AyraTextPrimary)
                        Text(text = "Keep AYRA listening with persistent notification while using other apps", fontSize = 12.sp, color = AyraTextSecondary)
                    }
                    Switch(
                        checked = bgSessionEnabled,
                        onCheckedChange = { preferences.setBackgroundSessionEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AyraCyan, checkedTrackColor = AyraCyan.copy(alpha = 0.4f))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Session duration
                Text(
                    text = "Session Duration (Auto-resets per command):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AyraTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 3, 5, 10).forEach { mins ->
                        FilterChip(
                            selected = sessionDuration == mins,
                            onClick = { preferences.setSessionDurationMinutes(mins) },
                            label = { Text("${mins}m") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AyraCyan.copy(alpha = 0.2f),
                                selectedLabelColor = AyraCyan
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Language & Speech Output
        SettingsSectionHeader(title = "SPEECH & TEXT-TO-SPEECH", icon = Icons.AutoMirrored.Filled.VolumeUp)

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Speech Language
                Text(text = "Recognition Language", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AyraTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val langs = listOf("auto" to "Auto", "en-US" to "English", "bn-BD" to "বাংলা (BD)", "bn-IN" to "বাংলা (IN)")
                    langs.forEach { (code, label) ->
                        FilterChip(
                            selected = speechLang == code,
                            onClick = { preferences.setSpeechLanguage(code) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AyraViolet.copy(alpha = 0.2f),
                                selectedLabelColor = AyraViolet
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Response Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Spoken Voice Responses", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AyraTextPrimary)
                        Text(text = "Speak responses aloud via Android Text-To-Speech", fontSize = 12.sp, color = AyraTextSecondary)
                    }
                    Switch(
                        checked = voiceRespEnabled,
                        onCheckedChange = { preferences.setVoiceResponseEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AyraViolet, checkedTrackColor = AyraViolet.copy(alpha = 0.4f))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Speech Speed
                Text(
                    text = String.format(Locale.US, "Speech Rate: %.1fx", speechRate),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AyraTextPrimary
                )
                Slider(
                    value = speechRate,
                    onValueChange = { preferences.setSpeechRate(it) },
                    valueRange = 0.8f..1.5f,
                    steps = 6,
                    colors = SliderDefaults.colors(thumbColor = AyraViolet, activeTrackColor = AyraViolet)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Safety & Confirmation
        SettingsSectionHeader(title = "SAFETY & CONFIRMATION", icon = Icons.Default.Security)

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Confirm Sensitive Actions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AyraTextPrimary)
                        Text(text = "Require dialog confirmation before dialing or deleting files", fontSize = 12.sp, color = AyraTextSecondary)
                    }
                    Switch(
                        checked = confirmSensitive,
                        onCheckedChange = { preferences.setConfirmSensitiveActions(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = AyraCyan, checkedTrackColor = AyraCyan.copy(alpha = 0.4f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Navigation Sub-pages
        SettingsSectionHeader(title = "MANAGEMENT & SYSTEM TOOLS", icon = Icons.Default.Folder)

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsNavRow(title = "Accessibility Gestures Setup", subtitle = "Configure screen tapping & scrolling", icon = Icons.Default.Accessibility) {
                    onNavigate("accessibility")
                }
                SettingsNavRow(title = "Permission Dashboard", subtitle = "View status of all Android privileges", icon = Icons.Default.Security) {
                    onNavigate("permissions")
                }
                SettingsNavRow(title = "Command History", subtitle = "Review and clear recent voice commands", icon = Icons.Default.History) {
                    onNavigate("history")
                }
                SettingsNavRow(title = "Voice Notes & Reminders", subtitle = "Browse voice-recorded notes", icon = Icons.AutoMirrored.Filled.Note) {
                    onNavigate("notes")
                }
                SettingsNavRow(title = "File Manager", subtitle = "Authorized notes and documents", icon = Icons.Default.Folder) {
                    onNavigate("files")
                }
                SettingsNavRow(title = "Privacy & Diagnostics", subtitle = "Local vs cloud privacy, troubleshooting", icon = Icons.Default.Security) {
                    onNavigate("privacy")
                }
                SettingsNavRow(title = "Battery Optimization Settings", subtitle = "Ensure background listening is not killed by OS", icon = Icons.Default.BatteryChargingFull) {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AyraCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AyraCyan,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AyraViolet.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AyraViolet,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AyraTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = AyraTextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Navigate",
            tint = AyraTextSecondary,
            modifier = Modifier.size(14.dp)
        )
    }
}
