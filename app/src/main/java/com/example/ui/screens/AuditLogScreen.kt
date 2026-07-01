package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.GemVaultViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AuditLogScreen(
    viewModel: GemVaultViewModel,
    modifier: Modifier = Modifier
) {
    val auditLogs by viewModel.auditLogs.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(auditLogs, searchQuery) {
        if (searchQuery.isBlank()) {
            auditLogs
        } else {
            val q = searchQuery.trim().lowercase()
            auditLogs.filter {
                it.username.lowercase().contains(q) ||
                it.action.lowercase().contains(q) ||
                it.details.lowercase().contains(q)
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Security Audit Log",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = "Immutable historical action records",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Search log field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter logs by user, action, or details...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextLight,
                unfocusedTextColor = TextLight,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = GoldPrimary.copy(alpha = 0.3f),
                focusedContainerColor = NavySurface,
                unfocusedContainerColor = NavySurface
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Clear logs button (admin setting)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredLogs.size} logs recorded",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            TextButton(
                onClick = { viewModel.clearAuditLogs() },
                colors = ButtonDefaults.textButtonColors(contentColor = StatusSold)
            ) {
                Text("CLEAR HISTORY LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Log Entries List
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Text("No audit log matches", color = TextLight, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLogs) { log ->
                    AuditLogItemRow(log = log, formatter = dateFormatter)
                }
            }
        }
    }
}

@Composable
fun AuditLogItemRow(
    log: com.example.data.model.AuditLogEntity,
    formatter: SimpleDateFormat
) {
    val dateStr = remember(log.timestamp) { formatter.format(Date(log.timestamp)) }
    
    // Choose status color based on action type
    val actionColor = when {
        log.action.contains("Login", ignoreCase = true) -> StatusAvailable
        log.action.contains("Fail", ignoreCase = true) -> StatusSold
        log.action.contains("Reset", ignoreCase = true) -> StatusReserved
        log.action.contains("Delete", ignoreCase = true) -> StatusSold
        log.action.contains("Addition", ignoreCase = true) -> GoldSecondary
        else -> TextLight
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NavySurface.copy(alpha = 0.5f))
            .border(0.5.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.action.uppercase(),
                    color = actionColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = dateStr,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
            
            Text(
                text = log.details,
                color = TextLight,
                fontSize = 13.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Initiated by:",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = log.username,
                    color = GoldSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
