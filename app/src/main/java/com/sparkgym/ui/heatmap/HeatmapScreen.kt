package com.sparkgym.ui.heatmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SegmentedBar
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.compactVolume
import com.sparkgym.core.util.oneDecimal
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.ui.common.SelectableChip
import com.sparkgym.ui.workout.VolumeBars

@Composable
fun HeatmapScreen(viewModel: HeatmapViewModel) {
    val heat by viewModel.heat.collectAsStateWithLifecycle()
    val showFront by viewModel.showFront.collectAsStateWithLifecycle()
    val windowDays by viewModel.windowDays.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val trend by viewModel.volumeTrend.collectAsStateWithLifecycle()

    val ranked = viewModel.ranked(heat)
    val balance = HeatmapEngine.balanceScore(heat)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("MUSCLE HEAT MAP", style = SystemLabel.copy(color = SparkColors.Violet))
                Text(
                    "What you actually trained",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SparkColors.TextPrimary
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7 to "7 days", 14 to "14 days", 30 to "30 days").forEach { (days, label) ->
                    SelectableChip(label, windowDays == days, { viewModel.setWindow(days) }, accent = SparkColors.Violet)
                }
            }
        }

        item {
            SystemPanel(accent = SparkColors.Violet) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (showFront) "ANTERIOR" else "POSTERIOR",
                        style = SystemLabel.copy(color = SparkColors.Violet)
                    )
                    SystemButton(if (showFront) "Show back" else "Show front", { viewModel.flip() }, accent = SparkColors.Violet)
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    MuscleHeatMap(
                        heat = heat,
                        isFront = showFront,
                        selected = selected,
                        onMuscleTap = viewModel::select,
                        modifier = Modifier.height(400.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("COLD", style = SystemLabel)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).height(10.dp)) { HeatLegend() }
                    Spacer(Modifier.width(8.dp))
                    Text("OVERREACHED", style = SystemLabel.copy(color = SparkColors.Danger))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tap a muscle to inspect it. Colour is weekly effective sets against that muscle's target, " +
                        "counting a synergist as half a set.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextMuted
                )
            }
        }

        selected?.let { muscle ->
            heat[muscle]?.let { entry ->
                item { MuscleDetailPanel(entry) }
            }
        }

        item {
            SystemPanel(title = "Balance score", accent = balanceColor(balance)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        balance.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = balanceColor(balance)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        SegmentedBar(progress = balance / 100f, color = balanceColor(balance))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            when {
                                balance >= 75 -> "Well distributed — nothing is being neglected."
                                balance >= 50 -> "Reasonable, but some regions are trailing."
                                else -> "Lopsided. Check the coldest muscles below."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = SparkColors.TextMuted
                        )
                    }
                }
            }
        }

        if (trend.isNotEmpty()) {
            item {
                SystemPanel(title = "Volume trend — 28 days") {
                    VolumeBars(trend.map { it.volumeKg }, Modifier.fillMaxWidth().height(90.dp))
                }
            }
        }

        item {
            Text(
                "PER MUSCLE — ${windowDays} DAY WINDOW",
                style = SystemLabel.copy(color = SparkColors.TextSecondary),
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        items(ranked, key = { it.muscle.name }) { entry ->
            MuscleRow(entry) { viewModel.select(entry.muscle) }
        }
    }
}

@Composable
private fun MuscleDetailPanel(entry: HeatmapEngine.MuscleHeat) {
    SystemPanel(accent = heatColor(entry.intensity), title = entry.muscle.displayName) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStat(entry.effectiveSets.oneDecimal(), "Effective sets")
            DetailStat(entry.target.toInt().toString(), "Weekly target")
            DetailStat(entry.volumeKg.compactVolume(), "Volume kg")
        }
        Spacer(Modifier.height(12.dp))
        SegmentedBar(progress = entry.intensity.coerceAtMost(1f), color = heatColor(entry.intensity))
        Spacer(Modifier.height(8.dp))
        Text(
            when (entry.status) {
                HeatmapEngine.Status.UNTRAINED -> "Not trained in this window at all."
                HeatmapEngine.Status.UNDER -> "Below the volume that reliably drives growth. Add a set or two."
                HeatmapEngine.Status.OPTIMAL -> "In the productive range. Keep it here."
                HeatmapEngine.Status.HIGH -> "High volume — fine short term, watch your recovery."
                HeatmapEngine.Status.OVERREACHED -> "Past what most people recover from. Consider a lighter week."
            },
            style = MaterialTheme.typography.bodySmall,
            color = SparkColors.TextSecondary
        )
    }
}

@Composable
private fun DetailStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = SparkColors.TextPrimary)
        Text(label.uppercase(), style = SystemLabel, textAlign = TextAlign.Center)
    }
}

@Composable
private fun MuscleRow(entry: HeatmapEngine.MuscleHeat, onClick: () -> Unit) {
    SystemPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        accent = heatColor(entry.intensity),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    entry.muscle.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = SparkColors.TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                SegmentedBar(
                    progress = entry.intensity.coerceAtMost(1f),
                    color = heatColor(entry.intensity),
                    segments = 16,
                    height = 6.dp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${entry.effectiveSets.oneDecimal()} / ${entry.target.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                SystemChip(entry.status.label, accent = heatColor(entry.intensity))
            }
        }
    }
}

private fun balanceColor(score: Int) = when {
    score >= 75 -> SparkColors.Success
    score >= 50 -> SparkColors.Amber
    else -> SparkColors.Danger
}
