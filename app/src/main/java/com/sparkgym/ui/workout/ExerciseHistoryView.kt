package com.sparkgym.ui.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.Dates
import com.sparkgym.core.util.S
import com.sparkgym.data.local.VolumePointRow
import com.sparkgym.domain.engine.StrengthMath
import com.sparkgym.domain.model.ExerciseHistoryRow
import com.sparkgym.domain.model.SetType
import kotlin.math.roundToInt

private fun getSetTypeColor(type: SetType): Color = when (type) {
    SetType.WARMUP -> SparkColors.Amber
    SetType.DROP_SET -> SparkColors.Violet
    SetType.FAILURE -> SparkColors.Danger
    else -> SparkColors.Cyan
}

@Composable
fun ExerciseHistoryView(
    history: List<ExerciseHistoryRow>,
    estimatedMaxHistory: List<VolumePointRow>,
    volumePoints: List<VolumePointRow>
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SparkColors.Void,
            contentColor = SparkColors.Cyan,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SparkColors.Cyan
                )
            },
            divider = { HorizontalDivider(color = SparkColors.PanelHigh) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Analitik", color = if (selectedTab == 0) SparkColors.Cyan else SparkColors.TextMuted) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Riwayat", color = if (selectedTab == 1) SparkColors.Cyan else SparkColors.TextMuted) }
            )
        }

        Spacer(Modifier.height(16.dp))

        if (selectedTab == 0) {
            AnalyticsTab(estimatedMaxHistory, volumePoints)
        } else {
            HistoryListTab(history)
        }
    }
}

@Composable
private fun AnalyticsTab(
    estimatedMaxHistory: List<VolumePointRow>,
    volumePoints: List<VolumePointRow>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SystemPanel(title = "1RM Progression", accent = SparkColors.Success, modifier = Modifier.padding(horizontal = 20.dp)) {
            if (estimatedMaxHistory.size < 2) {
                Text(
                    "Belum cukup data untuk membuat grafik.",
                    color = SparkColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                SimpleLineChart(points = estimatedMaxHistory.map { it.volumeKg.toFloat() }, color = SparkColors.Success)
            }
        }

        SystemPanel(title = "Total Volume", accent = SparkColors.Violet, modifier = Modifier.padding(horizontal = 20.dp)) {
            if (volumePoints.size < 2) {
                Text(
                    "Belum cukup data untuk membuat grafik.",
                    color = SparkColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                SimpleBarChart(points = volumePoints.map { it.volumeKg.toFloat() }, color = SparkColors.Violet)
            }
        }
    }
}

@Composable
private fun HistoryListTab(history: List<ExerciseHistoryRow>) {
    val grouped = history.groupBy { it.sessionId }
    
    if (grouped.isEmpty()) {
        Text(
            "Belum ada riwayat latihan.",
            color = SparkColors.TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        return
    }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        grouped.entries.sortedByDescending { it.value.first().dateEpochDay }.forEach { (_, sets) ->
            val firstSet = sets.first()
            val date = Dates.pretty(firstSet.dateEpochDay)
            val sessionName = firstSet.sessionName
            
            SystemPanel(title = "$date - $sessionName", accent = SparkColors.Cyan) {
                Column {
                    sets.forEach { set ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val setTypeColor = getSetTypeColor(set.setType)
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (set.setType == SetType.NORMAL) SparkColors.PanelHigh else setTypeColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        set.setNumber.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (set.setType == SetType.NORMAL) SparkColors.TextPrimary else setTypeColor
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "${set.weightKg.toInt()} kg × ${set.reps}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = SparkColors.TextPrimary
                                )
                                if (set.isPersonalRecord) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SparkColors.Amber.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("PR", fontSize = 10.sp, color = SparkColors.Amber, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(
                                "${S.e1RM} ${StrengthMath.estimatedOneRepMax(set.weightKg, set.reps).toInt()} kg",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SparkColors.Success
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleLineChart(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    val max = points.maxOrNull() ?: 1f
    val min = points.minOrNull() ?: 0f
    val range = (max - min).coerceAtLeast(1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1).coerceAtLeast(1)

            val path = Path()
            points.forEachIndexed { index, value ->
                val x = index * stepX
                val normalizedY = 1f - ((value - min) / range)
                val y = normalizedY * height

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw points
            points.forEachIndexed { index, value ->
                val x = index * stepX
                val normalizedY = 1f - ((value - min) / range)
                val y = normalizedY * height
                drawCircle(
                    color = color,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun SimpleBarChart(points: List<Float>, color: Color, modifier: Modifier = Modifier) {
    val max = points.maxOrNull() ?: 1f
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val barWidth = (width / points.size) * 0.6f
            val spacing = (width / points.size)

            points.forEachIndexed { index, value ->
                val x = (index * spacing) + (spacing - barWidth) / 2
                val normalizedY = value / max
                val barHeight = normalizedY * height
                
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, height - barHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}
