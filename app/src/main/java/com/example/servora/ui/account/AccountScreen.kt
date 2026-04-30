package com.example.servora.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.servora.ui.theme.*
import com.example.servora.data.repository.User
import com.example.servora.data.repository.UserSettings

@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    onSignOut: () -> Unit = {}
) {
    val user by viewModel.currentUser.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showRegionDialog by remember { mutableStateOf(false) }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = user?.name ?: "",
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName ->
                viewModel.updateDisplayName(newName)
                showEditProfileDialog = false
            }
        )
    }

    if (showRegionDialog) {
        RegionSelectionDialog(
            currentRegion = settings?.dataRegion ?: "US-East",
            onDismiss = { showRegionDialog = false },
            onSelect = { region ->
                viewModel.updateDataRegion(region)
                showRegionDialog = false
            }
        )
    }

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
            ProfileCard(
                user = user,
                onEditClick = { showEditProfileDialog = true }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            StatsRow(apiKeys = settings?.apiKeysCount ?: 0)
        }

        item {
            AccountSectionTitle("Account")
        }

        item {
            AccountSettingsCard {
                AccountRow(
                    icon = Icons.Default.Email,
                    title = "Email",
                    value = user?.email ?: "Not Set"
                )
                AccountDivider()
                AccountRow(
                    icon = Icons.Default.Shield,
                    title = "Role",
                    value = settings?.role ?: "User"
                )
                AccountDivider()
                AccountRow(
                    icon = Icons.Default.Verified,
                    title = "Plan",
                    value = settings?.plan ?: "Free"
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
                    value = "${settings?.apiKeysCount ?: 0} active"
                )
                AccountDivider()
                val is2FaEnabled = settings?.twoFactorAuth == true
                AccountRow(
                    icon = Icons.Default.Security,
                    title = "Two-Factor Auth",
                    value = if (is2FaEnabled) "Enabled" else "Disabled",
                    onClick = { viewModel.toggleTwoFactorAuth(!is2FaEnabled) }
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
                    value = settings?.notificationChannels ?: "Email, Push"
                )
                AccountDivider()
                AccountRow(
                    icon = Icons.Default.Cloud,
                    title = "Data Region",
                    value = settings?.dataRegion ?: "US-East",
                    onClick = { showRegionDialog = true }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            LogoutButton(onSignOut = onSignOut)
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            Text(text = "Edit Profile", color = TextPrimary)
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardBorder
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }) {
                Text("Save", color = NeonCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun RegionSelectionDialog(
    currentRegion: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val regions = listOf("US-East", "US-West", "EU-Central", "AP-South")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            Text(text = "Select Data Region", color = TextPrimary)
        },
        text = {
            Column {
                regions.forEach { region ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(region) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = region == currentRegion,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = region, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
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
private fun ProfileCard(user: User?, onEditClick: () -> Unit) {
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
                    text = user?.name?.takeIf { it.isNotBlank() } ?: "Guest User",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user?.email ?: "Unknown Email",
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
            IconButton(onClick = onEditClick) {
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
private fun StatsRow(apiKeys: Int) {
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
            value = apiKeys.toString(),
            label = "Keys",
            color = AmberWarning,
            icon = Icons.Default.Key,
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
    value: String,
    onClick: (() -> Unit)? = null
) {
    var modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    
    if (onClick != null) {
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    }

    Row(
        modifier = modifier,
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
            modifier = Modifier.padding(end = 8.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(2f, fill = false)
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
private fun LogoutButton(onSignOut: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape)
            .clickable { onSignOut() }
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
