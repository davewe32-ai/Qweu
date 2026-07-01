package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.GlassCard
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.GemVaultViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: GemVaultViewModel = viewModel()
      val systemSettings by viewModel.systemSettings.collectAsState()

      MyApplicationTheme(darkTheme = systemSettings.isDarkMode) {
        MainAppShell(viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(viewModel: GemVaultViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    val currentUser by viewModel.currentUser.collectAsState()
    val systemSettings by viewModel.systemSettings.collectAsState()
    val notification by viewModel.uiNotification.collectAsState()

    // Screen selection inside app frame
    var selectedScreenIndex by remember { mutableStateOf(0) } // 0=Dashboard, 1=Inventory, 2=Audit, 3=Settings, 4=My Account
    val selectedGem by viewModel.selectedGemstone.collectAsState()

    // Scanning Simulator modal state
    var showQrScannerSimulator by remember { mutableStateOf(false) }

    // Session warning state
    val isTimeoutWarning by viewModel.isSessionTimeoutWarning.collectAsState()

    // 1. Timer for Splash screen
    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (showSplash) {
            // --- LUXURIOUS BRANDED SPLASH LOADING SCREEN ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(systemSettings.companyLogoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Serendib Gemstones Loading Logo",
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth(0.8f),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "GEM VAULT",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldPrimary,
                        letterSpacing = 6.sp,
                        fontFamily = FontFamily.Serif
                    )

                    Text(
                        text = "SERENDIB GEMSTONES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 3.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    CircularProgressIndicator(
                        color = GoldPrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        } else if (currentUser == null) {
            // --- SECURE AUTHORIZED PORTAL ACCESS ---
            LoginScreen(viewModel = viewModel)
        } else {
            // --- MAIN SECURE APPLICATION FRAME ---
            val configuration = LocalConfiguration.current
            val isTablet = configuration.screenWidthDp >= 600

            Row(modifier = Modifier.fillMaxSize()) {
                // Adaptive Side Rail for Tablet/Landscape
                if (isTablet) {
                    NavigationRail(
                        containerColor = NavySurface,
                        contentColor = GoldPrimary,
                        header = {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(systemSettings.companyLogoUrl)
                                    .build(),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        NavigationRailItem(
                            selected = selectedScreenIndex == 0,
                            onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 0 },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text("Dashboard", fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary)
                        )
                        NavigationRailItem(
                            selected = selectedScreenIndex == 1,
                            onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 1 },
                            icon = { Icon(Icons.Default.Diamond, contentDescription = null) },
                            label = { Text("Inventory", fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary)
                        )
                        if (currentUser?.role == "Administrator") {
                            NavigationRailItem(
                                selected = selectedScreenIndex == 2,
                                onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 2 },
                                icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                                label = { Text("Audit Log", fontSize = 10.sp) },
                                colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary)
                            )
                        }
                        NavigationRailItem(
                            selected = selectedScreenIndex == 3,
                            onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 3 },
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Settings", fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary)
                        )
                        NavigationRailItem(
                            selected = selectedScreenIndex == 4,
                            onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 4 },
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                            label = { Text("My Account", fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // QR Simulator in side rail
                        IconButton(onClick = { viewModel.onUserInteraction(); showQrScannerSimulator = true }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Gemstone ID", tint = GoldPrimary)
                        }

                        // Logout button
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Logout, contentDescription = "Secure Log Out", tint = StatusSold)
                        }
                    }
                }

                // Main Area Content
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (!isTablet) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(systemSettings.companyLogoUrl)
                                                .build(),
                                            contentDescription = "Logo",
                                            modifier = Modifier.size(36.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = systemSettings.companyName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary
                                        )
                                        Text(
                                            text = "Logged as: ${currentUser?.fullName}",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (!isTablet) {
                                    // Scan Simulator trigger in mobile toolbar
                                    IconButton(onClick = { viewModel.onUserInteraction(); showQrScannerSimulator = true }) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Gem", tint = GoldPrimary)
                                    }
                                    
                                    // Logout button in mobile toolbar
                                    IconButton(onClick = { viewModel.logout() }) {
                                        Icon(Icons.Default.Logout, contentDescription = "Log Out Portal", tint = StatusSold)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavySurface)
                        )
                    },
                    bottomBar = {
                        // Bottom Navigation on mobile only
                        if (!isTablet) {
                            NavigationBar(
                                containerColor = NavySurface,
                                contentColor = GoldPrimary
                            ) {
                                NavigationBarItem(
                                    selected = selectedScreenIndex == 0,
                                    onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 0 },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                    label = { Text("Dashboard", fontSize = 9.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary, unselectedIconColor = TextMuted, unselectedTextColor = TextMuted)
                                )
                                NavigationBarItem(
                                    selected = selectedScreenIndex == 1,
                                    onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 1 },
                                    icon = { Icon(Icons.Default.Diamond, contentDescription = null) },
                                    label = { Text("Inventory", fontSize = 9.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary, unselectedIconColor = TextMuted, unselectedTextColor = TextMuted)
                                )
                                if (currentUser?.role == "Administrator") {
                                    NavigationBarItem(
                                        selected = selectedScreenIndex == 2,
                                        onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 2 },
                                        icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                                        label = { Text("Audits", fontSize = 9.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary, unselectedIconColor = TextMuted, unselectedTextColor = TextMuted)
                                    )
                                }
                                NavigationBarItem(
                                    selected = selectedScreenIndex == 3,
                                    onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 3 },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    label = { Text("Settings", fontSize = 9.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary, unselectedIconColor = TextMuted, unselectedTextColor = TextMuted)
                                )
                                NavigationBarItem(
                                    selected = selectedScreenIndex == 4,
                                    onClick = { viewModel.onUserInteraction(); viewModel.selectedGemstone.value = null; selectedScreenIndex = 4 },
                                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                                    label = { Text("Account", fontSize = 9.sp) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, selectedTextColor = GoldPrimary, indicatorColor = GoldPrimary, unselectedIconColor = TextMuted, unselectedTextColor = TextMuted)
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { pad ->
                    Box(modifier = Modifier.padding(pad)) {
                        // Display Detail Screen directly if selected, else show active navigation screen
                        if (selectedGem != null) {
                            GemstoneDetailScreen(
                                viewModel = viewModel,
                                onNavigateBack = { viewModel.selectedGemstone.value = null }
                            )
                        } else {
                            when (selectedScreenIndex) {
                                0 -> DashboardScreen(viewModel = viewModel, onNavigateToGem = { viewModel.onUserInteraction() })
                                1 -> InventoryScreen(viewModel = viewModel, onNavigateToGem = { viewModel.onUserInteraction() })
                                2 -> {
                                    if (currentUser?.role == "Administrator") {
                                        AuditLogScreen(viewModel = viewModel)
                                    } else {
                                        selectedScreenIndex = 0
                                    }
                                }
                                3 -> SettingsScreen(viewModel = viewModel)
                                4 -> MyAccountScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }

        // --- GLOBAL OVERLAY NOTIFICATIONS ---
        notification?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurface)
                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                    .clickable { viewModel.dismissNotification() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = GoldPrimary)
                    Text(text = msg, color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // --- SESSION INACTIVITY DISCONNECT WARNING MODAL ---
        if (isTimeoutWarning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp)
                ) {
                    Text(
                        text = "SECURE PORTAL TIMEOUT WARNING",
                        color = StatusSold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "You will be disconnected shortly due to inactivity. Click 'Extend Session' to keep your security tokens active.",
                        color = TextLight,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.onUserInteraction() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("EXTEND PORTAL SESSION", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- QR SCANNER SIMULATOR WINDOW ---
        if (showQrScannerSimulator) {
            val gemsList by viewModel.gemstones.collectAsState()
            
            AlertDialog(
                onDismissRequest = { showQrScannerSimulator = false },
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = GoldPrimary)
                        Text("Gemstone Identity Scanner Simulator", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("This simulator replicates pointing a secure camera scanner at a gemstone's printed QR label. Select a gemstone tag code to instantly synchronize and open its vault record.", fontSize = 11.sp, color = TextMuted)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(gemsList) { g ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectedGemstone.value = g
                                                showQrScannerSimulator = false
                                            },
                                        colors = CardDefaults.cardColors(containerColor = NavyLight)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(g.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                                Text("ID: ${g.gemId} | Cert: ${g.certificateNumber.ifBlank { "Uncertified" }}", fontSize = 10.sp, color = TextMuted)
                                            }
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GoldPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showQrScannerSimulator = false }) {
                        Text("CANCEL SCANNER", color = TextMuted)
                    }
                },
                containerColor = NavySurface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
