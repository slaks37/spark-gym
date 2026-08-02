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
import com.sparkgym.core.design.SparkDimens
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.compactVolume
import com.sparkgym.core.util.oneDecimal
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.ui.common.SelectableChip
import com.sparkgym.ui.workout.VolumeBars


import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import com.sparkgym.ui.common.ExerciseCard
import com.sparkgym.domain.model.Muscle

@Composable
fun HeatmapScreen(viewModel: HeatmapViewModel, onOpenExercise: (Long) -> Unit = {}) {
    val heat by viewModel.heat.collectAsStateWithLifecycle()
    val showFront by viewModel.showFront.collectAsStateWithLifecycle()
    val windowDays by viewModel.windowDays.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val trend by viewModel.volumeTrend.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    var currentAngle by remember { mutableStateOf(ViewAngle.FRONT) }
    var is3DMode by remember { mutableStateOf(true) }

    val ranked = viewModel.ranked(heat)
    val balance = HeatmapEngine.balanceScore(heat)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = SparkDimens.screen,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("MUSCLE HEAT MAP (360° 3D GRAPHICS)", style = SystemLabel.copy(color = SparkColors.Violet))
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
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SelectableChip("🌐 3D WebGL Engine", is3DMode, { is3DMode = true }, accent = SparkColors.Cyan)
                SelectableChip("⚡ 2D Vector", !is3DMode, { is3DMode = false }, accent = SparkColors.Violet)
            }
        }

        item {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(ViewAngle.entries.size) { index ->
                    val angle = ViewAngle.entries[index]
                    SelectableChip(
                        text = angle.labelId,
                        selected = currentAngle == angle,
                        onClick = { currentAngle = angle },
                        accent = SparkColors.Cyan
                    )
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
                        currentAngle.labelId.uppercase(),
                        style = SystemLabel.copy(color = SparkColors.Cyan)
                    )
                    Text(if (is3DMode) "Orbit 3D & Pinch Zoom" else "Geser / Drag untuk Putar 360°", style = SystemLabel.copy(color = SparkColors.TextMuted))
                }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (is3DMode) {
                        ThreeBodyView(
                            heat = heat,
                            angle = currentAngle,
                            onMuscleTap = viewModel::select,
                            modifier = Modifier.fillMaxWidth().height(420.dp)
                        )
                    } else {
                        MuscleHeatMap(
                            heat = heat,
                            angle = currentAngle,
                            selected = selected,
                            onAngleChange = { currentAngle = it },
                            onMuscleTap = viewModel::select,
                            modifier = Modifier.height(400.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("COLD (Jarang)", style = SystemLabel.copy(color = SparkColors.Cyan))
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).height(10.dp)) { HeatLegend() }
                    Spacer(Modifier.width(8.dp))
                    Text("SORE (Overreached)", style = SystemLabel.copy(color = SparkColors.Danger))
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Otot berwarna biru jarang/belum dilatih minggu ini. Otot berwarna merah/oranye memiliki beban kerja tinggi (sore).",
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextMuted
                )
            }
        }

        selected?.let { muscle ->
            heat[muscle]?.let { entry ->
                item { MuscleDetailPanel(entry, exercises, viewModel, onOpenExercise) }
            }
        }

        item {
            val coldMuscles = heat.filter { it.value.intensity <= 0.25f }.keys.take(6)
            val hotMuscles = heat.filter { it.value.intensity > 0.25f }.keys
            SystemPanel(title = "STATUS OTOT & REKOMENDASI LATIHAN", accent = SparkColors.Cyan) {
                if (coldMuscles.isNotEmpty()) {
                    Text("🔵 Otot Belum Dilatih (Rekomendasi Latihan Berikutnya):", style = MaterialTheme.typography.titleSmall, color = SparkColors.Cyan)
                    Spacer(Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(coldMuscles.toList().size) { index ->
                            val m = coldMuscles.toList()[index]
                            SystemChip(m.nameId, accent = SparkColors.Cyan)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                if (hotMuscles.isNotEmpty()) {
                    Text("🔥 Otot Sering Dilatih / Workload Tinggi (Sore):", style = MaterialTheme.typography.titleSmall, color = SparkColors.Amber)
                    Spacer(Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(hotMuscles.toList().size) { index ->
                            val m = hotMuscles.toList()[index]
                            SystemChip(m.nameId, accent = SparkColors.Amber)
                        }
                    }
                }
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
private fun MuscleDetailPanel(
    entry: HeatmapEngine.MuscleHeat,
    allExercises: List<com.sparkgym.data.local.ExerciseWithMuscles>,
    viewModel: HeatmapViewModel,
    onOpenExercise: (Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val exerciseList = when (selectedTab) {
        0 -> viewModel.primaryExercisesFor(entry.muscle, allExercises)
        1 -> viewModel.secondaryExercisesFor(entry.muscle, allExercises)
        else -> viewModel.stretchExercisesFor(entry.muscle, allExercises)
    }

    SystemPanel(accent = heatColor(entry.intensity), title = "${entry.muscle.displayName} (${entry.muscle.nameId})") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DetailStat(entry.effectiveSets.oneDecimal(), "Effective sets")
            DetailStat(entry.target.toInt().toString(), "Weekly target")
            DetailStat(entry.volumeKg.compactVolume(), "Volume kg")
        }
        Spacer(Modifier.height(10.dp))
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

        Spacer(Modifier.height(14.dp))
        Text(
            "EXERCISES FOR ${entry.muscle.displayName.uppercase()}",
            style = SystemLabel.copy(color = SparkColors.Cyan)
        )
        Spacer(Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SelectableChip("Primary", selectedTab == 0, { selectedTab = 0 }, accent = SparkColors.Cyan)
            SelectableChip("Secondary", selectedTab == 1, { selectedTab = 1 }, accent = SparkColors.Violet)
            SelectableChip("Stretches", selectedTab == 2, { selectedTab = 2 }, accent = SparkColors.Amber)
        }

        Spacer(Modifier.height(10.dp))

        if (exerciseList.isEmpty()) {
            Text("No specific exercises registered for this category.", style = MaterialTheme.typography.bodySmall, color = SparkColors.TextMuted)
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exerciseList.size) { index ->
                    val item = exerciseList[index]
                    ExerciseCard(
                        name = item.exercise.name,
                        equipment = item.exercise.equipment,
                        primary = item.muscles.filter { it.contribution >= 1f }.mapNotNull { Muscle.fromKey(it.muscle) }.toSet().ifEmpty { setOf(entry.muscle) },
                        secondary = item.muscles.filter { it.contribution < 1f }.mapNotNull { Muscle.fromKey(it.muscle) }.toSet(),
                        modifier = Modifier.width(260.dp),
                        onClick = { onOpenExercise(item.exercise.id) }
                    )
                }
            }
        }
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
