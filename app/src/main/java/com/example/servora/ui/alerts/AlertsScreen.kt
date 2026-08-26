package com.example.servora.ui.alerts

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.servora.data.model.AlertItem
import com.example.servora.data.model.AlertSeverity
import com.example.servora.ui.theme.AmberWarning
import com.example.servora.ui.theme.CardBorder
import com.example.servora.ui.theme.CardSurface
import com.example.servora.ui.theme.CoralRed
import com.example.servora.ui.theme.DeepNavy
import com.example.servora.ui.theme.MintGreen
import com.example.servora.ui.theme.MonoFontFamily
import com.example.servora.ui.theme.NeonCyan
import com.example.servora.ui.theme.TextPrimary
import com.example.servora.ui.theme.TextSecondary
import com.example.servora.ui.theme.TextTertiary

@Composable
fun AlertsScreen(
    onServerClick: (String) -> Unit,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        AlertsHeader(unreadCount = uiState.unreadCount, onMarkAllRead = { viewModel.markAllRead() })

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AlertFilter.entries) { filter ->
                FilterChip(
                    label = filter.label,
                    selected = uiState.filter == filter,
                    onClick = { viewModel.setFilter(filter) }
                )
            }
        }

        if (uiState.alerts.isEmpty() && !uiState.isLoading) {
            EmptyAlertsState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.alerts, key = { it.id }) { alert ->
                    AlertRow(
                        alert = alert,
                        onClick = {
                            viewModel.markRead(alert)
                            if (alert.serverId.isNotBlank()) onServerClick(alert.serverId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsHeader(unreadCount: Int, onMarkAllRead: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(top = 40.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ALERTS",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    brush = Brush.horizontalGradient(colors = listOf(NeonCyan, MintGreen))
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (unreadCount > 0) "$unreadCount unread" else "All caught up",
                style = MaterialTheme.typography.bodyMedium,
                color = if (unreadCount > 0) AmberWarning else TextSecondary
            )
        }
        TextButton(onClick = onMarkAllRead) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Mark all read", color = NeonCyan, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val color = if (selected) NeonCyan else TextTertiary
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) NeonCyan.copy(alpha = 0.12f) else CardSurface)
            .border(1.dp, if (selected) NeonCyan.copy(alpha = 0.5f) else CardBorder, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) NeonCyan else TextSecondary
        )
    }
}

@Composable
private fun AlertRow(alert: AlertItem, onClick: () -> Unit) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> CoralRed
        AlertSeverity.WARNING -> AmberWarning
        AlertSeverity.INFO -> NeonCyan
    }
    val shape = RoundedCornerShape(12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(if (alert.isRead) CardSurface.copy(alpha = 0.6f) else CardSurface)
            .border(1.dp, severityColor.copy(alpha = if (alert.isRead) 0.1f else 0.25f), shape)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(severityColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (alert.isRead) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (alert.isRead) TextTertiary else severityColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = alert.serverName,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                    color = TextPrimary
                )
                if (!alert.isRead) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(severityColor)
                    )
                }
            }
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = formatTimeAgo(alert.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun EmptyAlertsState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp)
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("No alerts", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
        Text(
            "Alerts will appear here when thresholds are breached",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}
