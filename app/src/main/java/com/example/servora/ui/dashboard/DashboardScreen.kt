package com.example.servora.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.servora.data.model.AlertItem
import com.example.servora.data.model.AlertSeverity
import com.example.servora.data.model.Server
import com.example.servora.data.model.ServerStatus
import com.example.servora.ui.components.GaugeChart
import com.example.servora.ui.components.StatusBadge
import com.example.servora.ui.components.getStatusColor
import com.example.servora.ui.theme.AmberWarning
import com.example.servora.ui.theme.CardBorder
import com.example.servora.ui.theme.CardSurface
import com.example.servora.ui.theme.CoralRed
import com.example.servora.ui.theme.DeepNavy
import com.example.servora.ui.theme.GaugeCpuEnd
import com.example.servora.ui.theme.GaugeCpuStart
import com.example.servora.ui.theme.GaugeDiskEnd
import com.example.servora.ui.theme.GaugeDiskStart
import com.example.servora.ui.theme.GaugeMemoryEnd
import com.example.servora.ui.theme.GaugeMemoryStart
import com.example.servora.ui.theme.MintGreen
import com.example.servora.ui.theme.NeonCyan
import com.example.servora.ui.theme.TextPrimary
import com.example.servora.ui.theme.TextSecondary
import com.example.servora.ui.theme.TextTertiary
import com.example.servora.ui.theme.MonoFontFamily
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    onServerClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            DashboardHeader(uiState)
        }

        item {
            SummaryStrip(uiState)
        }

        item {
            SectionTitle(title = "Servers", icon = Icons.Default.Dns)
        }

        itemsIndexed(uiState.servers, key = { _, server -> server.id }) { index, server ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(index * 80L)
                visible = true
            }
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    tween(400),
                    initialOffsetY = { it / 3 }
                )
            ) {
                ServerCard(
                    server = server,
                    onClick = { onServerClick(server.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        item {
            SectionTitle(title = "Recent Alerts", icon = Icons.Default.Notifications)
        }

        item {
            AlertsRow(alerts = uiState.alerts)
        }
    }
}

@Composable
private fun DashboardHeader(uiState: DashboardUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(top = 40.dp)
    ) {
        Text(
            text = "SERVORA",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                brush = Brush.horizontalGradient(
                    colors = listOf(NeonCyan, MintGreen)
                )
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Real-time Server Monitoring",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun SummaryStrip(uiState: DashboardUiState) {
    val summary = uiState.summary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip(
            count = summary.onlineCount,
            label = "Online",
            color = MintGreen,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            count = summary.warningCount,
            label = "Warning",
            color = AmberWarning,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            count = summary.criticalCount,
            label = "Critical",
            color = CoralRed,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            count = summary.offlineCount,
            label = "Offline",
            color = TextTertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), shape)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
    }
}

@Composable
private fun ServerCard(
    server: Server,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = getStatusColor(server.status)
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CardSurface,
                        CardSurface.copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CardBorder,
                        statusColor.copy(alpha = 0.15f)
                    )
                ),
                shape = shape
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getServerIcon(server.type),
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${server.ipAddress} · ${server.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            StatusBadge(status = server.status)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GaugeChart(
                value = server.metrics.cpuUsage,
                label = "CPU",
                size = 72.dp,
                strokeWidth = 6.dp,
                startColor = GaugeCpuStart,
                endColor = GaugeCpuEnd
            )
            GaugeChart(
                value = server.metrics.memoryUsage,
                label = "MEM",
                size = 72.dp,
                strokeWidth = 6.dp,
                startColor = GaugeMemoryStart,
                endColor = GaugeMemoryEnd
            )
            GaugeChart(
                value = server.metrics.diskUsage,
                label = "DISK",
                size = 72.dp,
                strokeWidth = 6.dp,
                startColor = GaugeDiskStart,
                endColor = GaugeDiskEnd
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MiniStat(label = "Response", value = "${server.metrics.responseTime}ms")
            MiniStat(label = "Req/s", value = "${server.metrics.requestsPerSecond}")
            MiniStat(label = "Conn", value = "${server.metrics.activeConnections}")
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = MonoFontFamily
            ),
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun AlertsRow(alerts: List<AlertItem>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(alerts, key = { it.id }) { alert ->
            AlertCard(alert = alert)
        }
    }
}

@Composable
private fun AlertCard(alert: AlertItem) {
    val severityColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> CoralRed
        AlertSeverity.WARNING -> AmberWarning
        AlertSeverity.INFO -> NeonCyan
    }
    val shape = RoundedCornerShape(12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(280.dp)
            .clip(shape)
            .background(CardSurface)
            .border(1.dp, severityColor.copy(alpha = 0.2f), shape)
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
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = severityColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.serverName,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary
            )
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2
            )
            Text(
                text = formatTimeAgo(alert.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

private fun getServerIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type.lowercase()) {
        "nginx" -> Icons.Default.Cloud
        "postgresql" -> Icons.Default.Storage
        "kong" -> Icons.Default.Speed
        "redis" -> Icons.Default.Memory
        "minio" -> Icons.Default.Dns
        else -> Icons.Default.Dns
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
