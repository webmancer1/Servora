package com.example.servora.ui.servers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.servora.data.local.ServerEntity
import com.example.servora.ui.theme.AmberWarning
import com.example.servora.ui.theme.CardBorder
import com.example.servora.ui.theme.CardSurface
import com.example.servora.ui.theme.DeepNavy
import com.example.servora.ui.theme.MintGreen
import com.example.servora.ui.theme.MonoFontFamily
import com.example.servora.ui.theme.NeonCyan
import com.example.servora.ui.theme.TextPrimary
import com.example.servora.ui.theme.TextSecondary
import com.example.servora.ui.theme.TextTertiary

@Composable
fun ManageServersScreen(
    onBackClick: () -> Unit,
    viewModel: ManageServersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ManageServersHeader(onBackClick, serverCount = uiState.servers.size)
        }

        item {
            Text(
                text = "Servers with an Agent URL are polled over HTTP (X-API-Key header). " +
                    "Servers without one run on simulated data.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        items(uiState.servers, key = { it.id }) { server ->
            ServerConfigRow(
                server = server,
                onDeleteClick = { viewModel.requestDelete(server) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        item {
            Button(
                onClick = { viewModel.showAddDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Server", color = DeepNavy, style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (uiState.addState.isVisible) {
        AddServerDialog(
            state = uiState.addState,
            onUpdate = { update -> viewModel.updateAddForm(update) },
            onDismiss = { viewModel.dismissAddDialog() },
            onSave = { viewModel.saveServer() }
        )
    }

    uiState.serverPendingDelete?.let { server ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Remove ${server.name}?", color = TextPrimary) },
            text = {
                Text(
                    "This deletes the server along with its saved history, alerts and action log.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Remove", color = com.example.servora.ui.theme.CoralRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = CardSurface
        )
    }
}

@Composable
private fun ManageServersHeader(onBackClick: () -> Unit, serverCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 8.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NeonCyan)
        }
        Column {
            Text(
                text = "SERVERS",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    brush = Brush.horizontalGradient(colors = listOf(NeonCyan, MintGreen))
                )
            )
            Text(
                text = "$serverCount monitored",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ServerConfigRow(server: ServerEntity, onDeleteClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardSurface)
            .border(1.dp, CardBorder, shape)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = if (server.baseUrl.isNullOrBlank()) Icons.Default.Science else Icons.Default.Api,
            contentDescription = null,
            tint = if (server.baseUrl.isNullOrBlank()) AmberWarning else MintGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = server.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
            Text(
                text = "${server.ipAddress} · ${server.location} · ${server.type}",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
            if (!server.baseUrl.isNullOrBlank()) {
                Text(
                    text = server.baseUrl ?: "",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                    color = MintGreen.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextTertiary)
        }
    }
}

@Composable
private fun AddServerDialog(
    state: AddServerUiState,
    onUpdate: ((AddServerUiState) -> AddServerUiState) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Server", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogTextField("Name", state.name) { value -> onUpdate { it.copy(name = value) } }
                DialogTextField("IP address / host", state.ipAddress) { value -> onUpdate { it.copy(ipAddress = value) } }
                DialogTextField("Location (e.g. US-East)", state.location) { value -> onUpdate { it.copy(location = value) } }
                DialogTextField("Type (e.g. NGINX)", state.type) { value -> onUpdate { it.copy(type = value) } }
                DialogTextField("Agent URL (optional)", state.baseUrl) { value -> onUpdate { it.copy(baseUrl = value) } }
                DialogTextField("API key (optional)", state.apiKey) { value -> onUpdate { it.copy(apiKey = value) } }
                if (state.error != null) {
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = com.example.servora.ui.theme.CoralRed
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save", color = NeonCyan) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = CardSurface
    )
}

@Composable
private fun DialogTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CardBorder,
            cursorColor = NeonCyan
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
