package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AyraAccessibilityService
import com.example.ui.theme.AyraCardBorder
import com.example.ui.theme.AyraCyan
import com.example.ui.theme.AyraMidnightBg
import com.example.ui.theme.AyraSuccess
import com.example.ui.theme.AyraSurfaceDark
import com.example.ui.theme.AyraTextPrimary
import com.example.ui.theme.AyraTextSecondary
import com.example.ui.theme.AyraViolet
import com.example.ui.theme.AyraWarning

@Composable
fun AccessibilitySetupScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isServiceActive by AyraAccessibilityService.isServiceActive.collectAsState()

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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AyraCyan
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Accessibility Gestures Setup",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AyraTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (isServiceActive) AyraSuccess.copy(alpha = 0.15f) else AyraWarning.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isServiceActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isServiceActive) AyraSuccess else AyraWarning,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (isServiceActive) "Service is Active & Ready" else "Service is Currently Disabled",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isServiceActive) AyraSuccess else AyraWarning
                    )
                    Text(
                        text = if (isServiceActive) "AYRA can perform voice-directed scroll and back actions." else "Enable AYRA in Android Accessibility to allow screen gestures.",
                        fontSize = 12.sp,
                        color = AyraTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "HOW TO ENABLE IN ANDROID SETTINGS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AyraCyan,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SetupStepRow(step = "1", text = "Tap the button below to open system Accessibility settings.")
                Spacer(modifier = Modifier.height(10.dp))
                SetupStepRow(step = "2", text = "Locate and tap 'AYRA' under Downloaded / Installed apps.")
                Spacer(modifier = Modifier.height(10.dp))
                SetupStepRow(step = "3", text = "Turn ON the toggle switch to authorize voice screen gestures.")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy Guarantee Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AyraSurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AyraCardBorder)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Privacy",
                    tint = AyraViolet,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Strict Privacy Policy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AyraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "AYRA does NOT read passwords, keystrokes, messages, or screen content. It is only utilized when you explicitly speak navigation commands such as 'Scroll down', 'Go back', or 'Take screenshot'.",
                        fontSize = 12.sp,
                        color = AyraTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = AyraCyan, contentColor = Color(0xFF001F29)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Open Accessibility Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SetupStepRow(step: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = AyraCyan.copy(alpha = 0.2f),
            shape = CircleShape,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = step, color = AyraCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 13.sp, color = AyraTextPrimary)
    }
}
