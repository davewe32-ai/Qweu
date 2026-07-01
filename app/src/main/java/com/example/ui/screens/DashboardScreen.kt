package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.example.data.model.GemstoneEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GemStatusDonutChart
import com.example.ui.components.OriginValuationBarChart
import com.example.ui.theme.*
import com.example.viewmodel.GemVaultViewModel

@Composable
fun DashboardScreen(
    viewModel: GemVaultViewModel,
    onNavigateToGem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()
    val gems by viewModel.gemstones.collectAsState()
    
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Executive Welcome Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Executive Inventory Dashboard",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = settings.companyName,
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldPrimary.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = settings.currency,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
            }
        }

        // --- KPI Cards Grid ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                title = "Total Gems",
                value = stats.totalGems.toString(),
                subtitle = "Active items",
                icon = Icons.Default.Diamond,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Total Carats",
                value = String.format("%.2f ct", stats.totalCarats),
                subtitle = "Weight in stock",
                icon = Icons.Default.Scale,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                title = "Estimated Value",
                value = "${settings.currency} " + formatValueLong(stats.estimatedValue),
                subtitle = "Valuation total",
                icon = Icons.Default.AccountBalanceWallet,
                modifier = Modifier.weight(1f),
                isHighlight = true
            )
        }

        // Status counts row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusIndicatorMini(
                title = "Available",
                count = stats.availableGems,
                color = StatusAvailable,
                modifier = Modifier.weight(1f)
            )
            StatusIndicatorMini(
                title = "Reserved",
                count = stats.reservedGems,
                color = StatusReserved,
                modifier = Modifier.weight(1f)
            )
            StatusIndicatorMini(
                title = "Sold",
                count = stats.soldGems,
                color = StatusSold,
                modifier = Modifier.weight(1f)
            )
        }

        // --- Analytical Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Gemstone Allocation Status",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    GemStatusDonutChart(
                        available = stats.availableGems,
                        reserved = stats.reservedGems,
                        sold = stats.soldGems
                    )
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Valuation Distribution by Origin Country",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            OriginValuationBarChart(
                valueByOrigin = stats.valueByOrigin,
                currencySymbol = if (settings.currency == "USD") "$" else "${settings.currency} "
            )
        }

        // --- Recently Added Gems ---
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Recently Deposited Gemstones",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )
            
            if (gems.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No gemstones deposited yet.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(gems.take(5)) { gem ->
                        RecentGemCard(
                            gem = gem,
                            currency = settings.currency,
                            onClick = {
                                viewModel.selectedGemstone.value = gem
                                onNavigateToGem()
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(50.dp)) // padding for bottom bars
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    val borderBrush = if (isHighlight) {
        Brush.verticalGradient(listOf(GoldPrimary, Color.Transparent))
    } else {
        Brush.verticalGradient(listOf(GoldPrimary.copy(alpha = 0.2f), Color.Transparent))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isHighlight) {
                        listOf(NavySurface, NavyDeep)
                    } else {
                        listOf(NavySurface.copy(alpha = 0.5f), NavyDeep.copy(alpha = 0.5f))
                    }
                )
            )
            .border(1.5.dp, borderBrush, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isHighlight) GoldPrimary else GoldSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isHighlight) GoldPrimary else TextLight
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun StatusIndicatorMini(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RecentGemCard(
    gem: GemstoneEntity,
    currency: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(NavySurface)
            .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Visual Gemstone Placeholder with refraction effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.sweepGradient(
                            colors = listOf(NavyLight, NavyDeep, NavyLight)
                        )
                    )
                    .border(1.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = null,
                    tint = GoldPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(36.dp)
                )
                
                // Mini Status Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (gem.status.lowercase()) {
                                "available" -> StatusAvailable
                                "reserved" -> StatusReserved
                                else -> StatusSold
                            }
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        gem.status.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Text(
                text = gem.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${gem.weightCarats} ct",
                    fontSize = 11.sp,
                    color = GoldSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = gem.originCountry,
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Text(
                text = "$currency " + formatValueShort(gem.estimatedValue),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
        }
    }
}

private fun formatValueLong(valAmt: Double): String {
    return String.format("%,.0f", valAmt)
}

private fun formatValueShort(valAmt: Double): String {
    return if (valAmt >= 1000) {
        String.format("%,.0fk", valAmt / 1000.0)
    } else {
        String.format("%.0f", valAmt)
    }
}
