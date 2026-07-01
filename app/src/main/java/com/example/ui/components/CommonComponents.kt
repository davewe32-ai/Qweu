package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// --- Glassmorphic Luxury Container ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderWidth: Float = 1.5f,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NavySurface.copy(alpha = 0.85f),
                        NavyDeep.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = borderWidth.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GoldPrimary.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
    } else {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NavySurface.copy(alpha = 0.85f),
                        NavyDeep.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = borderWidth.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GoldPrimary.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
    }

    Column(
        modifier = cardModifier.padding(16.dp)
    ) {
        content()
    }
}

// --- Dynamic Canvas Donut Chart for Gemstone Status ---
@Composable
fun GemStatusDonutChart(
    available: Int,
    reserved: Int,
    sold: Int,
    modifier: Modifier = Modifier
) {
    val total = (available + reserved + sold).toFloat()
    
    // Percentages
    val pAvailable = if (total > 0) available / total else 0f
    val pReserved = if (total > 0) reserved / total else 0f
    val pSold = if (total > 0) sold / total else 0f

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        animationTriggered = true
    }

    val animatedSweep = animateFloatAsState(
        targetValue = if (animationTriggered) 360f else 0f,
        animationSpec = tween(durationMillis = 1200)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Donut Canvas
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 32f
                val canvasSize = size.width
                val radius = (canvasSize - strokeWidth) / 2f
                
                if (total == 0f) {
                    drawCircle(
                        color = Color.DarkGray.copy(alpha = 0.3f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                } else {
                    var startAngle = -90f
                    
                    // Available (Green)
                    if (pAvailable > 0) {
                        val sweep = pAvailable * animatedSweep.value
                        drawArc(
                            color = StatusAvailable,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += pAvailable * 360f
                    }
                    
                    // Reserved (Orange)
                    if (pReserved > 0) {
                        val sweep = pReserved * animatedSweep.value
                        drawArc(
                            color = StatusReserved,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += pReserved * 360f
                    }
                    
                    // Sold (Red)
                    if (pSold > 0) {
                        val sweep = pSold * animatedSweep.value
                        drawArc(
                            color = StatusSold,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${total.toInt()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = "Gems",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Legend details
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            LegendRow(color = StatusAvailable, label = "Available", count = available, total = total.toInt())
            LegendRow(color = StatusReserved, label = "Reserved", count = reserved, total = total.toInt())
            LegendRow(color = StatusSold, label = "Sold", count = sold, total = total.toInt())
        }
    }
}

@Composable
fun LegendRow(color: Color, label: String, count: Int, total: Int) {
    val pct = if (total > 0) (count * 100) / total else 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Column {
            Text(
                text = "$label: $count",
                fontSize = 13.sp,
                color = TextLight,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$pct% of stock",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

// --- Valuation by Origin Country Bar Chart ---
@Composable
fun OriginValuationBarChart(
    valueByOrigin: Map<String, Double>,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier
) {
    val maxVal = valueByOrigin.values.maxOrNull() ?: 1.0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (valueByOrigin.isEmpty()) {
            Text(
                text = "No valuation data available",
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )
        } else {
            valueByOrigin.entries.sortedByDescending { it.value }.take(5).forEach { entry ->
                val origin = entry.key.ifBlank { "Unknown" }
                val value = entry.value
                val fraction = (value / maxVal).toFloat()

                var animateProgress by remember { mutableStateOf(false) }
                LaunchedEffect(key1 = true) {
                    animateProgress = true
                }
                
                val animatedFraction = animateFloatAsState(
                    targetValue = if (animateProgress) fraction else 0f,
                    animationSpec = tween(durationMillis = 1000)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = origin,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$currencySymbol${formatValuation(value)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(NavyLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = animatedFraction.value)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(GoldDark, GoldSecondary)
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun formatValuation(value: Double): String {
    return if (value >= 1_000_000) {
        String.format("%.2fM", value / 1_000_000.0)
    } else if (value >= 1_000) {
        String.format("%.1fk", value / 1_000.0)
    } else {
        String.format("%.0f", value)
    }
}

// --- Custom Stylized QR Code Renderer ---
@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
            .background(Color.White)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizePx = size.width
            val cellSize = sizePx / 15f
            
            // Background is white
            drawRect(color = Color.White)
            
            // Draw standard QR anchors (top-left, top-right, bottom-left)
            fun drawAnchor(x: Float, y: Float) {
                // outer 7x7 cells
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(x, y),
                    size = Size(cellSize * 7, cellSize * 7)
                )
                // white inner ring
                drawRect(
                    color = Color.White,
                    topLeft = Offset(x + cellSize, y + cellSize),
                    size = Size(cellSize * 5, cellSize * 5)
                )
                // black inner solid square
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(x + cellSize * 2, y + cellSize * 2),
                    size = Size(cellSize * 3, cellSize * 3)
                )
            }
            
            // 1. Top Left Anchor
            drawAnchor(0f, 0f)
            // 2. Top Right Anchor
            drawAnchor(sizePx - cellSize * 7, 0f)
            // 3. Bottom Left Anchor
            drawAnchor(0f, sizePx - cellSize * 7)
            
            // Draw deterministic pseudo-QR code pixels based on the data string hash
            val hash = data.hashCode()
            val random = java.util.Random(hash.toLong())
            
            for (row in 0 until 15) {
                for (col in 0 until 15) {
                    // skip anchor locations
                    if (row < 7 && col < 7) continue
                    if (row < 7 && col >= 8) continue
                    if (row >= 8 && col < 7) continue
                    
                    // 50% probability pixels
                    if (random.nextBoolean()) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }
    }
}
