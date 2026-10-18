package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AyraCardBorder
import com.example.ui.theme.AyraCyan
import com.example.ui.theme.AyraMidnightBg
import com.example.ui.theme.AyraSuccess
import com.example.ui.theme.AyraSurfaceDark
import com.example.ui.theme.AyraTextPrimary
import com.example.ui.theme.AyraTextSecondary
import com.example.ui.theme.AyraViolet

@Composable
fun PrivacyTroubleshootingScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AyraMidnightBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AyraCyan)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Privacy & Diagnostics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AyraTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Section
        SectionTitle(title = "DATA PRIVACY & LOCAL ARCHITECTURE", icon = Icons.Default.Lock)

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PrivacyItemRow(
                    title = "Local On-Device Processing",
                    description = "Phone hardware commands (flashlight, volume, Wi-Fi, camera, app launching, battery level, notes, and timers) execute strictly on your device without sending audio or data to the internet."
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrivacyItemRow(
                    title = "What Goes to Gemini",
                    description = "Only complex conversational questions or general reasoning queries are sent as text to Gemini. Raw audio recordings are never permanently stored or uploaded."
                )

                Spacer(modifier = Modifier.height(12.dp))

                PrivacyItemRow(
                    title = "No Spying or Background Recording",
                    description = "AYRA only listens when an active voice session is running (clearly indicated by the Android microphone indicator and persistent notification) or when you tap the voice orb."
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Troubleshooting Section
        SectionTitle(title = "TROUBLESHOOTING & COMMON ISSUES", icon = Icons.Default.Build)

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TroubleshootRow(
                    issue = "Speech Recognizer Not Available",
                    solution = "Ensure Google Speech Services or Android Speech Recognition is enabled on your device. You can download or update 'Speech Recognition & Synthesis from Google' on Google Play."
                )

                Spacer(modifier = Modifier.height(12.dp))

                TroubleshootRow(
                    issue = "Screen Gestures (Scroll / Back) Don't Work",
                    solution = "Verify that 'AYRA' is enabled in Android Settings > Accessibility. Some devices disable accessibility services after a restart."
                )

                Spacer(modifier = Modifier.height(12.dp))

                TroubleshootRow(
                    issue = "AYRA Stops in Background",
                    solution = "Aggressive battery optimization on Xiaomi/MIUI, Samsung OneUI, or Oppo may kill background services. Go to App Info > Battery > Select 'Unrestricted' or 'No Restrictions'."
                )

                Spacer(modifier = Modifier.height(12.dp))

                TroubleshootRow(
                    issue = "Gemini API Connection Failed",
                    solution = "Verify that your Gemini API key from Google AI Studio is active and has no billing/quota restrictions. Test the key in AYRA Settings."
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun SectionTitle(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AyraCyan, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AyraCyan, letterSpacing = 1.sp)
    }
}

@Composable
private fun PrivacyItemRow(title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AyraSuccess, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AyraTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = description, fontSize = 12.sp, color = AyraTextSecondary, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun TroubleshootRow(issue: String, solution: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AyraViolet, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = issue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AyraTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = solution, fontSize = 12.sp, color = AyraTextSecondary, lineHeight = 16.sp)
        }
    }
}
