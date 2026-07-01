package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GemstoneEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.QrCodeView
import com.example.ui.theme.*
import com.example.viewmodel.GemVaultViewModel

@Composable
fun GemstoneDetailScreen(
    viewModel: GemVaultViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedGem by viewModel.selectedGemstone.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()
    val context = LocalContext.current

    var activeDetailTab by remember { mutableStateOf(0) }
    val detailTabs = listOf("Specs", "Storage Location", "Documents", "Reports & Shares")

    // Image & Document Simulators
    var showMediaSelectorDialog by remember { mutableStateOf(false) }
    var uploadTypeSelected by remember { mutableStateOf("Image") } // or "Document"

    if (selectedGem == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("No gemstone selected.", color = TextLight)
        }
        return
    }

    val gem = selectedGem!!

    // Dynamic Gemstone Refraction Drawing based on color
    val gemstoneColorValue = remember(gem.color) {
        val lowercaseColor = gem.color.lowercase()
        when {
            lowercaseColor.contains("blue") -> Color(0xFF1E3A8A)
            lowercaseColor.contains("red") -> Color(0xFF991B1B)
            lowercaseColor.contains("green") -> Color(0xFF065F46)
            lowercaseColor.contains("pink") || lowercaseColor.contains("sunset") -> Color(0xFFDB2777)
            lowercaseColor.contains("yellow") || lowercaseColor.contains("gold") -> Color(0xFFD97706)
            else -> GoldPrimary
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // --- Custom Header Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                colors = IconButtonDefaults.iconButtonColors(containerColor = NavySurface)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back", tint = GoldPrimary)
            }

            Text(
                text = "GEM PROFILE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Serif
            )

            // Edit button shortcut
            IconButton(
                onClick = {
                    viewModel.isEditingGem.value = true
                    viewModel.showAddEditGemDialog.value = true
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = NavySurface)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit specs", tint = GoldPrimary)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Hero Showcase Display ---
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = gem.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextLight
                        )
                        Text(
                            text = gem.gemId,
                            fontSize = 12.sp,
                            color = GoldSecondary,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column {
                                Text("CARATS", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text("${gem.weightCarats} ct", fontSize = 16.sp, color = TextLight, fontWeight = FontWeight.ExtraBold)
                            }
                            Column {
                                Text("CLARITY", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(gem.clarity, fontSize = 16.sp, color = TextLight, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("SHAPE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(gem.shape, fontSize = 16.sp, color = TextLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Shimmering Canvas Drawing of a gemstone diamond
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyLight)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            val w = size.width
                            val h = size.height
                            
                            // Drawing diamond vector nodes on the Canvas
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.5f, 0f)         // top peak
                                lineTo(w, h * 0.4f)         // mid-right
                                lineTo(w * 0.5f, h)         // bottom tip
                                lineTo(0f, h * 0.4f)         // mid-left
                                close()
                            }
                            drawPath(
                                path = path,
                                color = gemstoneColorValue.copy(alpha = 0.8f)
                            )
                            
                            // Draws sparkling facet lines
                            drawLine(
                                color = Color.White.copy(alpha = 0.6f),
                                start = androidx.compose.ui.geometry.Offset(0f, h * 0.4f),
                                end = androidx.compose.ui.geometry.Offset(w, h * 0.4f),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.6f),
                                start = androidx.compose.ui.geometry.Offset(w * 0.5f, 0f),
                                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h),
                                strokeWidth = 2f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price Tag Valuation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldPrimary.copy(alpha = 0.08f))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ESTIMATED VAULT VALUATION", fontSize = 10.sp, color = GoldSecondary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("${settings.currency} " + String.format("%,.2f", gem.estimatedValue), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextLight)
                    }

                    val statusColor = when (gem.status.lowercase()) {
                        "available" -> StatusAvailable
                        "reserved" -> StatusReserved
                        else -> StatusSold
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(gem.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }
            }

            // --- Tab Segment Selector ---
            TabRow(
                selectedTabIndex = activeDetailTab,
                containerColor = NavySurface,
                contentColor = GoldPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                detailTabs.forEachIndexed { index, label ->
                    Tab(
                        selected = activeDetailTab == index,
                        onClick = { activeDetailTab = index },
                        text = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // --- Tab Content Renderer ---
            when (activeDetailTab) {
                0 -> { // Specs
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Gemstone Specifications", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        SpecRow(label = "Species", value = gem.species)
                        SpecRow(label = "Variety", value = gem.variety)
                        SpecRow(label = "Color Hue", value = gem.color)
                        SpecRow(label = "Cut Type", value = gem.cut)
                        SpecRow(label = "Transparency", value = gem.transparency)
                        SpecRow(label = "Origin Country", value = gem.originCountry)
                        SpecRow(label = "Treatment Status", value = gem.treatment.ifBlank { "Unheated / Natural" })
                        SpecRow(label = "Natural/Synthetic", value = if (gem.isNatural) "Natural Mineral" else "Synthetic Lab Grown")
                        SpecRow(label = "Measurements", value = "${gem.lengthMm} x ${gem.widthMm} x ${gem.heightMm} mm")
                        SpecRow(label = "Purchase Price", value = "${settings.currency} " + String.format("%,.2f", gem.purchasePrice))
                        SpecRow(label = "Selling Price", value = "${settings.currency} " + String.format("%,.2f", gem.sellingPrice))
                        SpecRow(label = "Supplier Guild", value = gem.supplier)
                        SpecRow(label = "Buyer Client", value = gem.buyer.ifBlank { "Unassigned" })
                        SpecRow(label = "Internal Remarks", value = gem.notes.ifBlank { "No remarks listed." })
                    }
                }
                1 -> { // Storage Location
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Secure Storage & Tracking Coordinates", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        StorageCoordinateItem(label = "Main Vault Vault", value = gem.vault.ifBlank { "Vault A" }, icon = Icons.Default.Inventory2)
                        StorageCoordinateItem(label = "Cabinet Row", value = gem.cabinet.ifBlank { "Cabinet 1" }, icon = Icons.Default.GridOn)
                        StorageCoordinateItem(label = "Drawer Slot", value = gem.drawer.ifBlank { "Drawer 3" }, icon = Icons.Default.Layers)
                        StorageCoordinateItem(label = "Tray Coordinate", value = gem.tray.ifBlank { "Tray B" }, icon = Icons.Default.ViewAgenda)
                        StorageCoordinateItem(label = "Box Number", value = gem.box.ifBlank { "Box 14" }, icon = Icons.Default.MoveToInbox)
                        StorageCoordinateItem(label = "Pocket Sleeve", value = gem.pocket.ifBlank { "Pocket 2" }, icon = Icons.Default.Folder)

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavyLight)
                                .padding(12.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = GoldPrimary)
                                Text(
                                    text = "Current secure location address is registered as: ${gem.storageLocation.ifBlank { "Vault A - Cab 1" }}",
                                    fontSize = 12.sp,
                                    color = TextLight
                                )
                            }
                        }
                    }
                }
                2 -> { // Documents & Images Gallery
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Media Gallery Images list
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("High-Resolution Gallery", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                
                                Button(
                                    onClick = {
                                        uploadTypeSelected = "Image"
                                        showMediaSelectorDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(12.dp), tint = GoldPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("UPLOAD IMAGE", fontSize = 9.sp, color = GoldSecondary)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            val imgList = gem.images.split(",").filter { it.isNotBlank() }
                            if (imgList.isEmpty()) {
                                Text("No gallery images uploaded.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(imgList) { imgName ->
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NavyLight)
                                                .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(36.dp))
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .padding(2.dp)
                                            ) {
                                                Text(imgName, fontSize = 9.sp, color = TextLight, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Certificates and Documents list
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Laboratory Certificates & Reports", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                
                                Button(
                                    onClick = {
                                        uploadTypeSelected = "Document"
                                        showMediaSelectorDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(12.dp), tint = GoldPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("UPLOAD FILE", fontSize = 9.sp, color = GoldSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (gem.certificateNumber.isNotBlank()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = NavyLight)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = StatusSold, modifier = Modifier.size(32.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Cert: ${gem.certificateNumber}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                            Text("Issuer: ${gem.laboratoryName}", fontSize = 11.sp, color = TextMuted)
                                            Text("Issue Date: ${gem.certIssueDate}", fontSize = 10.sp, color = TextMuted)
                                        }

                                        IconButton(onClick = {
                                            // Simulate downloading certificate PDF
                                            viewModel.uploadFileToGemstone(gem.gemId, "DOWNLOAD_COMPLETE_${gem.certificateNumber}.pdf", "Document")
                                        }) {
                                            Icon(Icons.Default.Download, contentDescription = "Download certificate", tint = GoldPrimary)
                                        }
                                    }
                                }
                            } else {
                                Text("No laboratory certificates listed for this gemstone.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
                            }
                        }
                    }
                }
                2 -> { // Vault Reports & Printing Shares
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Export Records & Reports Sharing", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Export premium certificate sheets, inventories, or local valuations in standard CSV/Text profiles directly to business partners.", fontSize = 11.sp, color = TextMuted)
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Sharing CSV Shortcut button
                        Button(
                            onClick = {
                                val csv = "Gem ID: ${gem.gemId}\nStock #: ${gem.stockNumber}\nName: ${gem.name}\nCarats: ${gem.weightCarats} ct\nSpecies: ${gem.species}\nVariety: ${gem.variety}\nColor: ${gem.color}\nValuation: ${settings.currency} ${gem.estimatedValue}\nStatus: ${gem.status}\nLocation: ${gem.storageLocation}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Gemstone Specification Export: ${gem.gemId}")
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Specification Profile"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SHARE PROFILE SPECIFICATION", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Simulation buttons
                        Button(
                            onClick = {
                                viewModel.uploadFileToGemstone(gem.gemId, "INVENTORY_VALUATION_SHEET_${gem.gemId}.csv", "Document")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyLight)
                        ) {
                            Icon(Icons.Default.SimCardDownload, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXPORT CSV LEDGER SHEET", color = TextLight)
                        }
                    }
                }
            }

            // --- QR Code Section ---
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cryptographic QR Code Identity",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "A unique ID pattern generated for this gemstone. Scan to instantly synchronize database coordinates.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QrCodeView(
                        data = "gemvault://gem/${gem.gemId}",
                        modifier = Modifier.size(160.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "SCAN IDENTITY: gemvault://gem/${gem.gemId}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = GoldSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(50.dp)) // padding for navigation
        }
    }

    // --- Media Presets Selector Dialog ---
    if (showMediaSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showMediaSelectorDialog = false },
            title = { Text(text = "Choose Simulated Asset to Upload", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select a professional pre-configured gemstone file asset to add to this profile:", fontSize = 12.sp, color = TextMuted)
                    
                    if (uploadTypeSelected == "Image") {
                        val imagesPresets = listOf(
                            "gemstone_microscope_close.png" to "Microscope Inspection View",
                            "gemstone_facets_refraction.png" to "Facet Angle Refraction Diagram",
                            "gemstone_natural_inclusion.png" to "Internal Silk Inclusion Micrograph",
                            "gemstone_girdle_laser_inscription.png" to "Laser Girdle Serial Number Inscription"
                        )
                        imagesPresets.forEach { (file, label) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.uploadFileToGemstone(gem.gemId, file, "Image")
                                        showMediaSelectorDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = NavyLight)
                            ) {
                                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = GoldSecondary)
                                    Text(label, color = TextLight, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        val docPresets = listOf(
                            "GIA_LABORATORY_REPORT_GIA2026.pdf" to "GIA Laboratory Identification Report",
                            "SWISS_SSEF_VALUATION_SSEF991.pdf" to "SSEF Certificate & Origin Opinion",
                            "INVENTORY_INSURANCE_VALUATION_SG2026.pdf" to "Corporate Valuation Insurance Binder",
                            "CUSTOMS_EXPORT_DECLARATION_LK445.pdf" to "Sri Lanka Customs Export Appraisal Form"
                        )
                        docPresets.forEach { (file, label) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.uploadFileToGemstone(gem.gemId, file, "Document")
                                        showMediaSelectorDialog = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = NavyLight)
                            ) {
                                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = StatusSold)
                                    Text(label, color = TextLight, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMediaSelectorDialog = false }) {
                    Text("CANCEL", color = TextMuted)
                }
            },
            containerColor = NavySurface
        )
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextLight,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
fun StorageCoordinateItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(16.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextLight,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            color = GoldPrimary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
