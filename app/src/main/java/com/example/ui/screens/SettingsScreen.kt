package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.GemVaultViewModel

@Composable
fun SettingsScreen(
    viewModel: GemVaultViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.systemSettings.collectAsState()
    val usersList by viewModel.users.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    val tabs = listOf("System Brand", "Access Control", "Database Backup")

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Settings Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Administration",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = "Configure corporate profiles and access tokens",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Tab selection row
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = NavySurface,
            contentColor = GoldPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = activeTab == idx,
                    onClick = { activeTab = idx },
                    text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> SystemBrandTab(viewModel = viewModel, initialSettings = settings)
                1 -> AccessControlTab(viewModel = viewModel, users = usersList)
                2 -> DatabaseBackupTab(viewModel = viewModel, context = context)
            }
        }
    }
}

// --- Tab 1: System Branding and Business Profile Settings ---
@Composable
fun SystemBrandTab(
    viewModel: GemVaultViewModel,
    initialSettings: com.example.data.model.SystemSettingsEntity
) {
    var companyName by remember { mutableStateOf(initialSettings.companyName) }
    var address by remember { mutableStateOf(initialSettings.address) }
    var contact by remember { mutableStateOf(initialSettings.contactDetails) }
    var currency by remember { mutableStateOf(initialSettings.currency) }
    var logoUrl by remember { mutableStateOf(initialSettings.companyLogoUrl) }
    var isDarkTheme by remember { mutableStateOf(initialSettings.isDarkMode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Corporate Enterprise Profile", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = logoUrl,
                onValueChange = { logoUrl = it },
                label = { Text("Logo URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Corporate Head Office Address") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Corporate Contacts") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency (USD, LKR...)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier.weight(1.2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("Dark Mode Default", fontSize = 12.sp, color = TextLight)
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = NavyLight)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.updateSystemSettings(
                        companyName = companyName,
                        address = address,
                        contact = contact,
                        currency = currency,
                        logoUrl = logoUrl,
                        isDarkMode = isDarkTheme
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE PROFILE CONFIGURATION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Tab 2: User Access Control and Role Management ---
@Composable
fun AccessControlTab(
    viewModel: GemVaultViewModel,
    users: List<UserEntity>
) {
    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedUserForPassReset by remember { mutableStateOf<UserEntity?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${users.size} Registered Operators",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )

            Button(
                onClick = { showAddUserDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("NEW USER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users) { u ->
                UserRowItem(
                    user = u,
                    onToggleStatus = { viewModel.adminToggleUserStatus(u.username) },
                    onChangeRole = { newRole -> viewModel.adminChangeUserRole(u.username, newRole) },
                    onResetPassword = { selectedUserForPassReset = u },
                    onDelete = { viewModel.adminDeleteUser(u.username) }
                )
            }
        }

        // Modals
        if (showAddUserDialog) {
            AddUserDialog(
                onDismiss = { showAddUserDialog = false },
                onAddUser = { uname, pwd, name, role ->
                    viewModel.adminCreateUser(uname, pwd, name, role)
                    showAddUserDialog = false
                }
            )
        }

        if (selectedUserForPassReset != null) {
            ResetPasswordDialog(
                user = selectedUserForPassReset!!,
                onDismiss = { selectedUserForPassReset = null },
                onReset = { newPass ->
                    viewModel.adminResetPassword(selectedUserForPassReset!!.username, newPass)
                    selectedUserForPassReset = null
                }
            )
        }
    }
}

@Composable
fun UserRowItem(
    user: UserEntity,
    onToggleStatus: () -> Unit,
    onChangeRole: (String) -> Unit,
    onResetPassword: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavySurface)
            .border(0.5.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = user.fullName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Text(
                        text = "@${user.username}",
                        fontSize = 11.sp,
                        color = GoldSecondary
                    )
                }

                // Disabled/Enabled Label Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (user.isEnabled) StatusAvailable.copy(alpha = 0.1f) else StatusSold.copy(alpha = 0.1f))
                        .clickable(onClick = onToggleStatus)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (user.isEnabled) "ACTIVE" else "DISABLED",
                        color = if (user.isEnabled) StatusAvailable else StatusSold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Role:", fontSize = 11.sp, color = TextMuted)
                    
                    val roles = listOf("Administrator", "Staff")
                    roles.forEach { r ->
                        val isSel = user.role == r
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) GoldPrimary.copy(alpha = 0.2f) else NavyLight)
                                .clickable { onChangeRole(r) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(r, fontSize = 10.sp, color = if (isSel) GoldPrimary else TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Small quick action controls
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onResetPassword, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.LockReset, contentDescription = "Reset password", tint = GoldSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete account", tint = StatusSold, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// --- Tab 3: Database Utilities, Backup & Restore console ---
@Composable
fun DatabaseBackupTab(
    viewModel: GemVaultViewModel,
    context: Context
) {
    var backupString by remember { mutableStateOf("") }
    var restoreInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Automated Database Backup Utilities", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Export complete gemstones database and user credentials as an encrypted JSON backup block to clipboard. Save this string securely as your restoration point.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val raw = viewModel.backupDatabase()
                    backupString = raw
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Gem Vault Backup", raw))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GENERATE BACKUP & COPY", fontWeight = FontWeight.Bold)
            }

            if (backupString.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = backupString,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Backup Token Output") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Restore From Backup Point", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Paste a previously generated backup JSON block below to instantly synchronize, restore, and overwrite your vault database.",
                fontSize = 11.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = restoreInput,
                onValueChange = { restoreInput = it },
                placeholder = { Text("Paste JSON backup block here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (viewModel.restoreDatabase(restoreInput)) {
                        restoreInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StatusAvailable, contentColor = Color.Black),
                enabled = restoreInput.isNotBlank()
            ) {
                Icon(Icons.Default.SettingsBackupRestore, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PERFORM DATABASE SYNCHRONIZATION", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- User Creation Dialog ---
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onAddUser: (String, String, String, String) -> Unit
) {
    var uname by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Staff") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Authorized Operator", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = uname, onValueChange = { uname = it }, label = { Text("Unique Username") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("add_user_username"))
                OutlinedTextField(value = pwd, onValueChange = { pwd = it }, label = { Text("Initial Security Password") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Operator Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("System Role:", fontSize = 13.sp, color = TextLight)
                    listOf("Administrator", "Staff").forEach { r ->
                        val isSel = role == r
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) GoldPrimary.copy(alpha = 0.2f) else NavyLight)
                                .clickable { role = r }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(r, fontSize = 11.sp, color = if (isSel) GoldPrimary else TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddUser(uname, pwd, name, role) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
            ) {
                Text("AUTHORIZE USER", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) }
        },
        containerColor = NavySurface
    )
}

// --- Admin Reset Password Dialog ---
@Composable
fun ResetPasswordDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onReset: (String) -> Unit
) {
    var newPass by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Force Reset Password", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Initiating administrative password override for operator ${user.fullName} (@${user.username}).", fontSize = 12.sp, color = TextMuted)
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Secure Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (newPass.isNotBlank()) onReset(newPass) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                enabled = newPass.isNotBlank()
            ) {
                Text("RESET PASS", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) }
        },
        containerColor = NavySurface
    )
}
