package com.example.servora.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.servora.ui.theme.CardBorder
import com.example.servora.ui.theme.CardSurface
import com.example.servora.ui.theme.DeepNavy
import com.example.servora.ui.theme.MintGreen
import com.example.servora.ui.theme.NeonCyan
import com.example.servora.ui.theme.TextPrimary
import com.example.servora.ui.theme.TextSecondary
import com.example.servora.ui.theme.TextTertiary
import com.example.servora.ui.theme.MonoFontFamily

import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SettingsHeader()
        }

        item {
            SettingsSectionTitle("Notifications")
        }

        item {
            SettingsCard {
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive alerts on your device",
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                )
                SettingsDivider()
                SettingsToggleRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound Alerts",
                    subtitle = "Play sound for notifications",
                    checked = settings.soundEnabled,
                    onCheckedChange = { viewModel.setSoundEnabled(it) }
                )
            }
        }

        item {
            SettingsSectionTitle("Monitoring & Polling")
        }

        item {
            SettingsCard {
                SettingsToggleRow(
                    icon = Icons.Default.Science,
                    title = "Mock Mode Engine",
                    subtitle = "Generate simulated metrics for servers without URLs",
                    checked = settings.mockModeEnabled,
                    onCheckedChange = { viewModel.setMockModeEnabled(it) }
                )
                SettingsDivider()
                SettingsInfoRow(
                    icon = Icons.Default.Timer,
                    title = "Refresh Interval",
                    value = "${settings.refreshIntervalMs / 1000} seconds"
                )
            }
        }

        item {
            SettingsSectionTitle("Threshold Configuration")
        }

        item {
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CPU Critical Threshold", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("${settings.thresholds.cpuCritical.toInt()}%", style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily), color = NeonCyan)
                    }
                    Slider(
                        value = settings.thresholds.cpuCritical,
                        onValueChange = { newCpu ->
                            viewModel.updateThresholds(
                                cpuWarn = settings.thresholds.cpuWarning,
                                cpuCrit = newCpu,
                                memWarn = settings.thresholds.memoryWarning,
                                memCrit = settings.thresholds.memoryCritical,
                                diskWarn = settings.thresholds.diskWarning,
                                diskCrit = settings.thresholds.diskCritical
                            )
                        },
                        valueRange = 50f..99f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Memory Critical Threshold", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("${settings.thresholds.memoryCritical.toInt()}%", style = MaterialTheme.typography.labelMedium.copy(fontFamily = MonoFontFamily), color = MintGreen)
                    }
                    Slider(
                        value = settings.thresholds.memoryCritical,
                        onValueChange = { newMem ->
                            viewModel.updateThresholds(
                                cpuWarn = settings.thresholds.cpuWarning,
                                cpuCrit = settings.thresholds.cpuCritical,
                                memWarn = settings.thresholds.memoryWarning,
                                memCrit = newMem,
                                diskWarn = settings.thresholds.diskWarning,
                                diskCrit = settings.thresholds.diskCritical
                            )
                        },
                        valueRange = 50f..99f,
                        colors = SliderDefaults.colors(thumbColor = MintGreen, activeTrackColor = MintGreen)
                    )
                }
            }
        }

        item {
            SettingsSectionTitle("About & System")
        }

        item {
            SettingsCard {
                SettingsInfoRow(
                    icon = Icons.Default.Info,
                    title = "Version",
                    value = "1.0.0"
                )
                SettingsDivider()
                SettingsInfoRow(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    value = ""
                )
                SettingsDivider()
                SettingsInfoRow(
                    icon = Icons.Default.BugReport,
                    title = "Report a Bug",
                    value = ""
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Servora v1.0.0",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                color = TextTertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun SettingsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(top = 40.dp)
    ) {
        Text(
            text = "SETTINGS",
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
            text = "Configure your monitoring preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = NeonCyan,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(CardSurface)
            .border(1.dp, CardBorder, shape)
            .padding(vertical = 4.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NeonCyan.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DeepNavy,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CardBorder
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NeonCyan.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 64.dp)
            .height(0.5.dp)
            .background(CardBorder.copy(alpha = 0.5f))
    )
}
