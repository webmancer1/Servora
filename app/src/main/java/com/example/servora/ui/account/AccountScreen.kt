package com.example.servora.ui.account

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.servora.ui.theme.AmberWarning
import com.example.servora.ui.theme.CardBorder
import com.example.servora.ui.theme.CardSurface
import com.example.servora.ui.theme.DeepNavy
import com.example.servora.ui.theme.ElectricBlue
import com.example.servora.ui.theme.MintGreen
import com.example.servora.ui.theme.NeonCyan
import com.example.servora.ui.theme.TextPrimary
import com.example.servora.ui.theme.TextSecondary
import com.example.servora.ui.theme.TextTertiary
import com.example.servora.ui.theme.MonoFontFamily
import com.example.servora.ui.theme.CoralRed

@Composable
fun AccountScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            AccountHeader()
        }

        item {
            ProfileCard()
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            StatsRow()
        }

        item {
            AccountSectionTitle("Account")
        }

        item {
            AccountSettingsCard {
                AccountRow(
                    icon = Icons.Default.Email,
                    title = "Email",
                    value = "admin@servora.io"
                )
                AccountDivider()
                AccountRow(
                    icon = Icons.Default.Shield,
                    title = "Role",
                    value = "Administrator"
                )
                AccountDivider()
                AccountRow(
                    icon = Icons.Default.Verified,
                    title = "Plan",
                    value = "Pro"
                )
            }
        }

        item {
            AccountSectionTitle("Security")
        }

        item {
            AccountSettingsCard {
                AccountRow(
                    icon = Icons.Default.Key,
                    title = "API Keys",
                    value = "3 active"
                )
                AccountDivider()
                AccountRow(
                    icon = Icons.Default.Security,
                    title = "Two-Factor Auth",
                    value = "Enabled"
                )
                AccountDivider()
                AccountRow(
                    icon = Icons.Default.History,
                    title = "Login History",
                    value = "View"
                )
            }
        }

        item {
            AccountSectionTitle("Preferences")
        }

        item {
            AccountSettingsCard {
                AccountRow(
                    icon = Icons.Default.Notifications,
                    title = "Notification Channels",
                    value = "Email, Push"
                )
                AccountDivider()
                AccountRow(
                    icon = Icons.Default.Cloud,
                    title = "Data Region",
                    value = "US-East"
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            LogoutButton()
        }
    }
}

@Composable
private fun AccountHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(top = 40.dp)
    ) {
        Text(
            text = "ACCOUNT",
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
            text = "Manage your profile & security",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun ProfileCard() {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.08f),
                        CardSurface
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.3f),
                        CardBorder
                    )
                ),
                shape = shape
            )
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonCyan, ElectricBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = DeepNavy,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alex Morgan",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "admin@servora.io",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MintGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MintGreen
                        )
                    }
                }
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit profile",
                    tint = NeonCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(
            value = "5",
            label = "Servers",
            color = NeonCyan,
            icon = Icons.Default.Dns,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            value = "99.8%",
            label = "Uptime",
            color = MintGreen,
            icon = Icons.Default.Storage,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            value = "12",
            label = "Alerts",
            color = AmberWarning,
            icon = Icons.Default.Notifications,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.06f))
            .border(1.dp, color.copy(alpha = 0.15f), shape)
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
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
private fun AccountSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = NeonCyan,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun AccountSettingsCard(content: @Composable () -> Unit) {
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
private fun AccountRow(
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
private fun AccountDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 64.dp)
            .height(0.5.dp)
            .background(CardBorder.copy(alpha = 0.5f))
    )
}

@Composable
private fun LogoutButton() {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(CoralRed.copy(alpha = 0.08f))
            .border(1.dp, CoralRed.copy(alpha = 0.2f), shape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = CoralRed,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Sign Out",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = CoralRed
        )
    }
}
