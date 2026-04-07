package com.example.servora.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.servora.data.model.AlertSeverity
import com.example.servora.data.model.ProcessInfo
import com.example.servora.ui.components.GaugeChart
import com.example.servora.ui.components.MetricCard
import com.example.servora.ui.components.MiniLineChart
import com.example.servora.ui.components.StatusBadge
import com.example.servora.ui.components.getStatusColor
import com.example.servora.ui.theme.AmberWarning
import com.example.servora.ui.theme.CardBorder
import com.example.servora.ui.theme.CardSurface
import com.example.servora.ui.theme.CoralRed
import com.example.servora.ui.theme.DeepNavy
import com.example.servora.ui.theme.ElectricBlue
import com.example.servora.ui.theme.GaugeCpuEnd
import com.example.servora.ui.theme.GaugeCpuStart
import com.example.servora.ui.theme.GaugeDiskEnd
import com.example.servora.ui.theme.GaugeDiskStart
import com.example.servora.ui.theme.GaugeMemoryEnd
import com.example.servora.ui.theme.GaugeMemoryStart
import com.example.servora.ui.theme.MintGreen
import com.example.servora.ui.theme.MonoFontFamily
import com.example.servora.ui.theme.NeonCyan
import com.example.servora.ui.theme.TextPrimary
import com.example.servora.ui.theme.TextSecondary
import com.example.servora.ui.theme.TextTertiary

@Composable
fun ServerDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ServerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val server = uiState.server

    if (server == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepNavy),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading...", color = TextSecondary)
        }
        return
    }

    val statusColor = getStatusColor(server.status)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { DetailHeader(server.name, server.ipAddress, server.location, server.type, onBackClick) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBadge(status = server.status)
                Text(
                    text = "Uptime: ${formatUptime(server.metrics.uptime)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                    color = TextSecondary
                )
            }
        }

        item {
            SectionLabel("System Resources")
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GaugeChart(
                    value = server.metrics.cpuUsage,
                    label = "CPU",
                    size = 110.dp,
                    strokeWidth = 10.dp,
                    startColor = GaugeCpuStart,
                    endColor = GaugeCpuEnd,
                    isLarge = true
                )
                GaugeChart(
                    value = server.metrics.memoryUsage,
                    label = "Memory",
                    size = 110.dp,
                    strokeWidth = 10.dp,
                    startColor = GaugeMemoryStart,
                    endColor = GaugeMemoryEnd,
                    isLarge = true
                )
                GaugeChart(
                    value = server.metrics.diskUsage,
                    label = "Disk",
                    size = 110.dp,
                    strokeWidth = 10.dp,
                    startColor = GaugeDiskStart,
                    endColor = GaugeDiskEnd,
                    isLarge = true
                )
            }
        }

        item {
            SectionLabel("Performance Metrics")
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    icon = Icons.Default.Timer,
                    label = "Response",
                    value = "${server.metrics.responseTime}",
                    unit = "ms",
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Default.Speed,
                    label = "Req/s",
                    value = "${server.metrics.requestsPerSecond}",
                    accentColor = MintGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    icon = Icons.Default.Hub,
                    label = "Connections",
                    value = "${server.metrics.activeConnections}",
                    accentColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Default.Memory,
                    label = "Memory",
                    value = String.format("%.1f", server.metrics.memoryUsed),
                    unit = "/ ${server.metrics.memoryTotal.toInt()} GB",
                    accentColor = GaugeMemoryStart,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SectionLabel("Network I/O")
        }

        item {
            NetworkChart(
                networkInHistory = server.metrics.networkInHistory,
                networkOutHistory = server.metrics.networkOutHistory,
                currentIn = server.metrics.networkIn,
                currentOut = server.metrics.networkOut
            )
        }

        item {
            SectionLabel("Top Processes")
        }

        items(uiState.processes) { process ->
            ProcessRow(process = process)
        }

        if (uiState.alerts.isNotEmpty()) {
            item {
                SectionLabel("Alerts")
            }

            items(uiState.alerts) { alert ->
                DetailAlertRow(
                    severity = alert.severity,
                    message = alert.message,
                    timeAgo = formatTimeAgoDetail(alert.timestamp)
                )
            }
        }
    }
}

@Composable
private fun DetailHeader(
    name: String,
    ip: String,
    location: String,
    type: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonCyan
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "$ip · $location · $type",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = NeonCyan,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Composable
private fun NetworkChart(
    networkInHistory: List<Float>,
    networkOutHistory: List<Float>,
    currentIn: Float,
    currentOut: Float
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(CardSurface)
            .border(1.dp, CardBorder, shape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDownload, null, tint = MintGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "IN: ${String.format("%.0f", currentIn)} MB/s",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                    color = MintGreen
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "OUT: ${String.format("%.0f", currentOut)} MB/s",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily),
                    color = ElectricBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (networkInHistory.size >= 2) {
            MiniLineChart(
                data = networkInHistory,
                lineColor = MintGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (networkOutHistory.size >= 2) {
            MiniLineChart(
                data = networkOutHistory,
                lineColor = ElectricBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}

@Composable
private fun ProcessRow(process: ProcessInfo) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(CardSurface)
            .border(1.dp, CardBorder, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = process.name,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = MonoFontFamily),
                color = TextPrimary
            )
            Text(
                text = "PID: ${process.pid}",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CPU",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { (process.cpuUsage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = NeonCyan,
                    trackColor = CardBorder,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${String.format("%.1f", process.cpuUsage)}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MEM",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { (process.memoryUsage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MintGreen,
                    trackColor = CardBorder,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${String.format("%.1f", process.memoryUsage)}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DetailAlertRow(severity: AlertSeverity, message: String, timeAgo: String) {
    val color = when (severity) {
        AlertSeverity.CRITICAL -> CoralRed
        AlertSeverity.WARNING -> AmberWarning
        AlertSeverity.INFO -> NeonCyan
    }
    val shape = RoundedCornerShape(12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(CardSurface)
            .border(1.dp, color.copy(alpha = 0.2f), shape)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            Text(text = timeAgo, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
    }
}

private fun formatUptime(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h"
        else -> "${seconds / 60}m"
    }
}

private fun formatTimeAgoDetail(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}
