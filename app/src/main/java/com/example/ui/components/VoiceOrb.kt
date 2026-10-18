package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AyraCyan
import com.example.ui.theme.AyraError
import com.example.ui.theme.AyraPink
import com.example.ui.theme.AyraViolet
import com.example.voice.VoiceState

@Composable
fun VoiceOrb(
    voiceState: VoiceState,
    rmsAmplitude: Float,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val activeScale = when {
        voiceState == VoiceState.LISTENING -> (1f + rmsAmplitude * 0.35f) * pulseScale
        isSpeaking || voiceState == VoiceState.SPEAKING -> pulseScale * 1.05f
        voiceState == VoiceState.PROCESSING -> pulseScale * 1.02f
        else -> 1.0f
    }

    val primaryColor = when {
        voiceState == VoiceState.ERROR -> AyraError
        voiceState == VoiceState.LISTENING -> AyraCyan
        isSpeaking || voiceState == VoiceState.SPEAKING -> AyraViolet
        voiceState == VoiceState.PROCESSING -> AyraPink
        else -> AyraCyan.copy(alpha = 0.7f)
    }

    val secondaryColor = when {
        voiceState == VoiceState.ERROR -> Color(0xFFFF8A80)
        voiceState == VoiceState.LISTENING -> AyraViolet
        isSpeaking || voiceState == VoiceState.SPEAKING -> AyraCyan
        voiceState == VoiceState.PROCESSING -> AyraViolet
        else -> AyraViolet.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .size(220.dp)
            .testTag("voice_orb_button")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 110.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.65f * activeScale

            // Outer subtle glow ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.28f),
                        secondaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.4f
                ),
                radius = baseRadius * 1.4f,
                center = center
            )

            // Middle pulsing aura ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.6f),
                        secondaryColor.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.15f
                ),
                radius = baseRadius * 1.15f,
                center = center
            )

            // Inner vibrant sphere
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor),
                    start = Offset(center.x - baseRadius, center.y - baseRadius),
                    end = Offset(center.x + baseRadius, center.y + baseRadius)
                ),
                radius = baseRadius * 0.85f,
                center = center
            )
        }

        // Center Icon
        val icon = when {
            voiceState == VoiceState.LISTENING -> Icons.Default.Mic
            isSpeaking || voiceState == VoiceState.SPEAKING -> Icons.AutoMirrored.Filled.VolumeUp
            voiceState == VoiceState.PROCESSING -> Icons.Default.RecordVoiceOver
            voiceState == VoiceState.ERROR -> Icons.Default.MicOff
            else -> Icons.Default.Mic
        }

        Icon(
            imageVector = icon,
            contentDescription = "Voice State Icon",
            tint = Color.White,
            modifier = Modifier.size(44.dp)
        )
    }
}
