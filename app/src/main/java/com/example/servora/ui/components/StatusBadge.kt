package com.example.servora.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.servora.data.model.ServerStatus
import com.example.servora.ui.theme.AmberWarning
import com.example.servora.ui.theme.CoralRed
import com.example.servora.ui.theme.MintGreen
import com.example.servora.ui.theme.TextTertiary

@Composable
fun StatusBadge(
    status: ServerStatus,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        ServerStatus.ONLINE -> MintGreen
        ServerStatus.WARNING -> AmberWarning
        ServerStatus.CRITICAL -> CoralRed
        ServerStatus.OFFLINE -> TextTertiary
    }

    val label = when (status) {
        ServerStatus.ONLINE -> "Online"
        ServerStatus.WARNING -> "Warning"
        ServerStatus.CRITICAL -> "Critical"
        ServerStatus.OFFLINE -> "Offline"
    }

    val shouldPulse = status == ServerStatus.ONLINE || status == ServerStatus.CRITICAL

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldPulse) 1.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (shouldPulse) 0f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (shouldPulse) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(color.copy(alpha = pulseAlpha))
                )
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

fun getStatusColor(status: ServerStatus): Color {
    return when (status) {
        ServerStatus.ONLINE -> MintGreen
        ServerStatus.WARNING -> AmberWarning
        ServerStatus.CRITICAL -> CoralRed
        ServerStatus.OFFLINE -> TextTertiary
    }
}
