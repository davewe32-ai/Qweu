package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GemstoneEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.viewmodel.GemVaultViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryScreen(
    viewModel: GemVaultViewModel,
    onNavigateToGem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gems by viewModel.filteredGemstones.collectAsState()
    val allGemsRaw by viewModel.gemstones.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val filterColor by viewModel.filterColor.collectAsState()
    val filterShape by viewModel.filterShape.collectAsState()
    val filterSpecies by viewModel.filterSpecies.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val viewType by viewModel.viewType.collectAsState()

    // Dialog state
    val showAddEditDialog by viewModel.showAddEditGemDialog.collectAsState()
    val isEditMode by viewModel.isEditingGem.collectAsState()
    val selectedGemForActions by viewModel.selectedGemstone.collectAsState()

    // Filter panel collapsed/expanded
    var isFilterPanelExpanded by remember { mutableStateOf(false) }

    // Distinct values for filter dropdowns
    val colorsList by viewModel.availableColors.collectAsState()
    val shapesList by viewModel.availableShapes.collectAsState()
    val speciesList by viewModel.availableSpecies.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            // Staff and Admin can add gems
            FloatingActionButton(
                onClick = {
                    viewModel.selectedGemstone.value = null
                    viewModel.isEditingGem.value = false
                    viewModel.showAddEditGemDialog.value = true
                },
                containerColor = GoldPrimary,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_gem_fab").padding(bottom = 50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Gemstone Specimen")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Search Bar and Layout Toggle Controls ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search ID, Color, Name, Origin...", color = TextMuted, fontSize = 13.sp) },
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
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gem_search_input")
                )

                // Filter Panel toggle
                IconButton(
                    onClick = { isFilterPanelExpanded = !isFilterPanelExpanded },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isFilterPanelExpanded) GoldPrimary else NavySurface
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Toggle Filters Panel",
                        tint = if (isFilterPanelExpanded) Color.Black else GoldPrimary
                    )
                }

                // Grid (Card) vs Table view toggle
                IconButton(
                    onClick = {
                        viewModel.viewType.value = if (viewType == "Card") "Table" else "Card"
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = NavySurface),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (viewType == "Card") Icons.Default.List else Icons.Default.GridView,
                        contentDescription = "Switch View Layout",
                        tint = GoldPrimary
                    )
                }
            }

            // --- Collapsible Advanced Filter Panel ---
            AnimatedVisibility(
                visible = isFilterPanelExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderWidth = 1f
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Refine Gemstone Vault Search", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Status Filter
                            FilterDropdown(
                                label = "Status",
                                selected = filterStatus,
                                options = listOf("All", "Available", "Reserved", "Sold"),
                                onSelect = { viewModel.filterStatus.value = it }
                            )

                            // Color Filter
                            FilterDropdown(
                                label = "Color",
                                selected = filterColor,
                                options = colorsList,
                                onSelect = { viewModel.filterColor.value = it }
                            )

                            // Shape Filter
                            FilterDropdown(
                                label = "Shape",
                                selected = filterShape,
                                options = shapesList,
                                onSelect = { viewModel.filterShape.value = it }
                            )

                            // Species Filter
                            FilterDropdown(
                                label = "Species",
                                selected = filterSpecies,
                                options = speciesList,
                                onSelect = { viewModel.filterSpecies.value = it }
                            )
                        }

                        // Sorting Options Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Sort by:", color = TextMuted, fontSize = 12.sp)
                                val sortOptions = listOf(
                                    "id" to "Date Added",
                                    "name" to "Gem Name",
                                    "weightCarats" to "Weight",
                                    "estimatedValue" to "Value",
                                    "status" to "Status"
                                )
                                sortOptions.forEach { (key, label) ->
                                    val isSelected = sortBy == key
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) GoldPrimary.copy(alpha = 0.2f) else NavyLight)
                                            .border(1.dp, if (isSelected) GoldPrimary else Color.Transparent, RoundedCornerShape(6.dp))
                                            .clickable { viewModel.sortBy.value = key }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(label, color = if (isSelected) GoldPrimary else TextLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.sortAscending.value = !sortAscending },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = "Toggle Sort Direction",
                                    tint = GoldPrimary
                                )
                            }
                        }
                    }
                }
            }

            // --- Gemstone List (Grid View or Table View) ---
            if (gems.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Diamond, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                        Text("No Gemstone Records Found", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Clear search or filters to browse all items", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                if (viewType == "Card") {
                    // Card View (Grid of Cards)
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(gems, key = { it.id }) { gem ->
                            GemCard(
                                gem = gem,
                                currency = settings.currency,
                                isAdmin = currentUser?.role == "Administrator",
                                onClick = {
                                    viewModel.selectedGemstone.value = gem
                                    onNavigateToGem()
                                },
                                onEdit = {
                                    viewModel.selectedGemstone.value = gem
                                    viewModel.isEditingGem.value = true
                                    viewModel.showAddEditGemDialog.value = true
                                },
                                onDelete = { viewModel.deleteGemstone(gem) }
                            )
                        }
                    }
                } else {
                    // Table View (Scrollable List with custom columns)
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Header Row
                        item {
                            TableRowHeader()
                        }
                        // Data rows
                        items(gems, key = { it.id }) { gem ->
                            GemTableRow(
                                gem = gem,
                                currency = settings.currency,
                                isAdmin = currentUser?.role == "Administrator",
                                onClick = {
                                    viewModel.selectedGemstone.value = gem
                                    onNavigateToGem()
                                },
                                onEdit = {
                                    viewModel.selectedGemstone.value = gem
                                    viewModel.isEditingGem.value = true
                                    viewModel.showAddEditGemDialog.value = true
                                },
                                onDelete = { viewModel.deleteGemstone(gem) }
                            )
                        }
                    }
                }
            }
        }

        // Add/Edit Dialog modal
        if (showAddEditDialog) {
            AddEditGemstoneDialog(
                viewModel = viewModel,
                isEdit = isEditMode,
                existingGem = selectedGemForActions,
                onDismiss = { viewModel.showAddEditGemDialog.value = false }
            )
        }
    }
}

// --- Filter Dropdown composable ---
@Composable
fun FilterDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text("$label: $selected", color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(NavySurface).border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = TextLight, fontSize = 13.sp) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- Card View item ---
@Composable
fun GemCard(
    gem: GemstoneEntity,
    currency: String,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NavySurface)
            .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column {
            // Gem Thumbnail Simulator with refraction
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NavyLight, NavyDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = null,
                    tint = GoldPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )

                // Status Tag
                val tagColor = when (gem.status.lowercase()) {
                    "available" -> StatusAvailable
                    "reserved" -> StatusReserved
                    else -> StatusSold
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagColor.copy(alpha = 0.15f))
                        .border(1.dp, tagColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        gem.status.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagColor
                    )
                }

                // Quick Actions overflow dots
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Quick Actions", tint = GoldPrimary)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(NavySurface).border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("View Profile", color = TextLight) },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, tint = GoldPrimary) },
                        onClick = { showMenu = false; onClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit Specification", color = TextLight) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = GoldPrimary) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    if (isAdmin) {
                        DropdownMenuItem(
                            text = { Text("Delete Record", color = StatusSold) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StatusSold) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            // Gem Spec Details
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = gem.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = gem.gemId,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldSecondary,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${gem.weightCarats} Carats",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                    Text(
                        text = gem.shape,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Divider(color = GoldPrimary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Valuation:",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "$currency " + String.format("%,.0f", gem.estimatedValue),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldSecondary
                    )
                }
            }
        }
    }
}

// --- Table View Header row ---
@Composable
fun TableRowHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NavySurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Gem Description / ID", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
        Text("Carats", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
        Text("Color/Shape", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
        Text("Origin", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Status", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Estimated Value", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
    }
}

// --- Table View Item Row ---
@Composable
fun GemTableRow(
    gem: GemstoneEntity,
    currency: String,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NavySurface.copy(alpha = 0.6f))
            .border(0.5.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail & Info
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NavyLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Diamond, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(gem.name, color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(gem.gemId, color = GoldSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Carats
        Text("${gem.weightCarats} ct", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f))

        // Color & Shape
        Column(modifier = Modifier.weight(1.2f)) {
            Text(gem.color, color = TextLight, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(gem.shape, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        // Origin
        Text(gem.originCountry, color = TextLight, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))

        // Status
        val statusColor = when (gem.status.lowercase()) {
            "available" -> StatusAvailable
            "reserved" -> StatusReserved
            else -> StatusSold
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(statusColor.copy(alpha = 0.1f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(gem.status, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        // Price / Valuation & Quick Actions combo
        Row(
            modifier = Modifier.weight(1.2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "$currency " + String.format("%,.0f", gem.estimatedValue),
                color = GoldSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Quick Actions", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(NavySurface).border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("View", color = TextLight) },
                        onClick = { showMenu = false; onClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit", color = TextLight) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    if (isAdmin) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = StatusSold) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

// --- Comprehensive Add/Edit Gemstone Modal Dialog ---
@Composable
fun AddEditGemstoneDialog(
    viewModel: GemVaultViewModel,
    isEdit: Boolean,
    existingGem: GemstoneEntity?,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) }
    val tabLabels = listOf("Identity", "Physical", "Value & Storage", "Certification")

    // State bindings
    var stockNumber by remember { mutableStateOf(existingGem?.stockNumber ?: "") }
    var name by remember { mutableStateOf(existingGem?.name ?: "") }
    var species by remember { mutableStateOf(existingGem?.species ?: "") }
    var variety by remember { mutableStateOf(existingGem?.variety ?: "") }
    var color by remember { mutableStateOf(existingGem?.color ?: "") }
    var shape by remember { mutableStateOf(existingGem?.shape ?: "") }
    var cut by remember { mutableStateOf(existingGem?.cut ?: "") }
    var clarity by remember { mutableStateOf(existingGem?.clarity ?: "") }
    var transparency by remember { mutableStateOf(existingGem?.transparency ?: "") }
    var originCountry by remember { mutableStateOf(existingGem?.originCountry ?: "") }
    var treatment by remember { mutableStateOf(existingGem?.treatment ?: "") }
    var isNatural by remember { mutableStateOf(existingGem?.isNatural ?: true) }
    
    var weightCarats by remember { mutableStateOf(existingGem?.weightCarats?.toString() ?: "1.0") }
    var lengthMm by remember { mutableStateOf(existingGem?.lengthMm?.toString() ?: "5.0") }
    var widthMm by remember { mutableStateOf(existingGem?.widthMm?.toString() ?: "5.0") }
    var heightMm by remember { mutableStateOf(existingGem?.heightMm?.toString() ?: "3.0") }

    var purchasePrice by remember { mutableStateOf(existingGem?.purchasePrice?.toString() ?: "0") }
    var sellingPrice by remember { mutableStateOf(existingGem?.sellingPrice?.toString() ?: "0") }
    var estimatedValue by remember { mutableStateOf(existingGem?.estimatedValue?.toString() ?: "0") }
    var status by remember { mutableStateOf(existingGem?.status ?: "Available") }
    var supplier by remember { mutableStateOf(existingGem?.supplier ?: "") }
    var buyer by remember { mutableStateOf(existingGem?.buyer ?: "") }

    var vault by remember { mutableStateOf(existingGem?.vault ?: "Vault A") }
    var cabinet by remember { mutableStateOf(existingGem?.cabinet ?: "") }
    var drawer by remember { mutableStateOf(existingGem?.drawer ?: "") }
    var tray by remember { mutableStateOf(existingGem?.tray ?: "") }
    var box by remember { mutableStateOf(existingGem?.box ?: "") }
    var pocket by remember { mutableStateOf(existingGem?.pocket ?: "") }
    var notes by remember { mutableStateOf(existingGem?.notes ?: "") }

    var certNumber by remember { mutableStateOf(existingGem?.certificateNumber ?: "") }
    var laboratoryName by remember { mutableStateOf(existingGem?.laboratoryName ?: "") }
    var certIssueDate by remember { mutableStateOf(existingGem?.certIssueDate ?: "2026-07-01") }
    var certExpiryDate by remember { mutableStateOf(existingGem?.certExpiryDate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Modify Gemstone Specimen" else "Deposit New Gemstone Specimen",
                color = GoldPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Scrollable Tabs Header
                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = NavySurface,
                    contentColor = GoldPrimary,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    tabLabels.forEachIndexed { index, label ->
                        Tab(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            text = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (activeTab) {
                        0 -> { // Identity
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Gemstone Name (e.g., Imperial Ruby)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("gem_name_field")
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = stockNumber,
                                    onValueChange = { stockNumber = it },
                                    label = { Text("Stock #") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = originCountry,
                                    onValueChange = { originCountry = it },
                                    label = { Text("Origin") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = species,
                                    onValueChange = { species = it },
                                    label = { Text("Species") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = variety,
                                    onValueChange = { variety = it },
                                    label = { Text("Variety") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isNatural,
                                    onCheckedChange = { isNatural = it ?: true },
                                    colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                                )
                                Text("Natural Specimen (Unchecked = Synthetic)", color = TextLight, fontSize = 13.sp)
                            }
                        }
                        1 -> { // Physical properties
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = weightCarats,
                                    onValueChange = { weightCarats = it },
                                    label = { Text("Weight (Carats)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = clarity,
                                    onValueChange = { clarity = it },
                                    label = { Text("Clarity (VVS, VS...)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = shape,
                                    onValueChange = { shape = it },
                                    label = { Text("Shape (Oval...)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = cut,
                                    onValueChange = { cut = it },
                                    label = { Text("Cut (Brilliant...)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = lengthMm,
                                    onValueChange = { lengthMm = it },
                                    label = { Text("Length (mm)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = widthMm,
                                    onValueChange = { widthMm = it },
                                    label = { Text("Width (mm)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = heightMm,
                                onValueChange = { heightMm = it },
                                label = { Text("Height (mm)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = color,
                                    onValueChange = { color = it },
                                    label = { Text("Color Saturation") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = transparency,
                                    onValueChange = { transparency = it },
                                    label = { Text("Transparency") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        2 -> { // Value, Supplier, and Storage
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = purchasePrice,
                                    onValueChange = { purchasePrice = it },
                                    label = { Text("Purchase Price") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = sellingPrice,
                                    onValueChange = { sellingPrice = it },
                                    label = { Text("Selling Price") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = estimatedValue,
                                    onValueChange = { estimatedValue = it },
                                    label = { Text("Estimated Value") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                // Status Selector
                                var statExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = status,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Status") },
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { statExpanded = true }) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    DropdownMenu(
                                        expanded = statExpanded,
                                        onDismissRequest = { statExpanded = false },
                                        modifier = Modifier.background(NavySurface)
                                    ) {
                                        listOf("Available", "Reserved", "Sold").forEach { item ->
                                            DropdownMenuItem(
                                                text = { Text(item, color = TextLight) },
                                                onClick = { status = item; statExpanded = false }
                                            )
                                        }
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = supplier,
                                    onValueChange = { supplier = it },
                                    label = { Text("Supplier") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = buyer,
                                    onValueChange = { buyer = it },
                                    label = { Text("Buyer") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text("Storage Placement Location", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(value = vault, onValueChange = { vault = it }, label = { Text("Vault") }, singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = cabinet, onValueChange = { cabinet = it }, label = { Text("Cabinet") }, singleLine = true, modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(value = drawer, onValueChange = { drawer = it }, label = { Text("Drawer") }, singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = tray, onValueChange = { tray = it }, label = { Text("Tray") }, singleLine = true, modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(value = box, onValueChange = { box = it }, label = { Text("Box") }, singleLine = true, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = pocket, onValueChange = { pocket = it }, label = { Text("Pocket") }, singleLine = true, modifier = Modifier.weight(1f))
                            }
                        }
                        3 -> { // Certification & Notes
                            OutlinedTextField(
                                value = certNumber,
                                onValueChange = { certNumber = it },
                                label = { Text("Certificate Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = laboratoryName,
                                onValueChange = { laboratoryName = it },
                                label = { Text("Testing Laboratory Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = certIssueDate,
                                    onValueChange = { certIssueDate = it },
                                    label = { Text("Issue Date") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = certExpiryDate,
                                    onValueChange = { certExpiryDate = it },
                                    label = { Text("Expiry Date") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Internal Notes") },
                                modifier = Modifier.fillMaxWidth().height(100.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        return@Button
                    }
                    val finalGem = GemstoneEntity(
                        id = existingGem?.id ?: 0,
                        gemId = existingGem?.gemId ?: "GEM-TEMP",
                        stockNumber = stockNumber,
                        name = name,
                        species = species,
                        variety = variety,
                        color = color,
                        shape = shape,
                        cut = cut,
                        clarity = clarity,
                        transparency = transparency,
                        originCountry = originCountry,
                        treatment = treatment,
                        isNatural = isNatural,
                        weightCarats = weightCarats.toDoubleOrNull() ?: 1.0,
                        lengthMm = lengthMm.toDoubleOrNull() ?: 0.0,
                        widthMm = widthMm.toDoubleOrNull() ?: 0.0,
                        heightMm = heightMm.toDoubleOrNull() ?: 0.0,
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                        estimatedValue = estimatedValue.toDoubleOrNull() ?: 0.0,
                        status = status,
                        supplier = supplier,
                        buyer = buyer,
                        storageLocation = "", // calculated in viewmodel
                        vault = vault,
                        cabinet = cabinet,
                        drawer = drawer,
                        tray = tray,
                        box = box,
                        pocket = pocket,
                        notes = notes,
                        images = existingGem?.images ?: "gem_cover.png",
                        documents = existingGem?.documents ?: "",
                        certificateNumber = certNumber,
                        laboratoryName = laboratoryName,
                        certIssueDate = certIssueDate,
                        certExpiryDate = certExpiryDate
                    )
                    viewModel.saveGemstone(finalGem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
            ) {
                Text(if (isEdit) "SAVE SPECIFICATIONS" else "DEPOSIT RECORD", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextMuted)
            }
        },
        containerColor = NavySurface,
        shape = RoundedCornerShape(16.dp)
    )
}
