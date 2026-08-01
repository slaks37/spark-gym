package com.sparkgym.ui.workout

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.Dates
import com.sparkgym.core.util.S
import com.sparkgym.data.local.SetLogEntity
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.StrengthMath
import com.sparkgym.domain.model.Muscle
import com.sparkgym.ui.common.MuscleDot
import com.sparkgym.ui.common.equipmentIcon
import com.sparkgym.ui.common.muscleGroupColor
import com.sparkgym.ui.heatmap.MuscleThumb

/**
 * Exercise reference page.
 *
 * Picture first: a large anatomical diagram of what the movement trains, then
 * the coaching cue broken into numbered steps, then your own numbers. The
 * previous version led with two grey paragraphs, which is the wrong shape for
 * something you read between sets.
 */
@Composable
fun ExerciseDetailScreen(
    container: AppContainer,
    exerciseId: Long,
    onBack: () -> Unit
) {
    val detail by container.workoutRepository.observeExercise(exerciseId)
        .collectAsState(initial = null)

    var recent by remember { mutableStateOf<List<SetLogEntity>>(emptyList()) }
    LaunchedEffect(exerciseId) {
        recent = container.workoutRepository.lastPerformance(exerciseId)
    }

    val exercise = detail?.exercise
    val primary = detail?.muscles.orEmpty()
        .filter { it.contribution >= 1f }.mapNotNull { Muscle.fromKey(it.muscle) }.toSet()
    val secondary = detail?.muscles.orEmpty()
        .filter { it.contribution < 1f }.mapNotNull { Muscle.fromKey(it.muscle) }.toSet()
    val accent = primary.firstOrNull()?.let { muscleGroupColor(it.group) } ?: SparkColors.Cyan

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---- Hero: the diagram is the headline, not a caption ----
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(accent.copy(alpha = 0.08f))
            ) {
                IconButton(onClick = onBack, modifier = Modifier.padding(4.dp)) {
                    Icon(Icons.Filled.ArrowBack, S.back, tint = SparkColors.TextPrimary)
                }
                MuscleThumb(
                    primary = primary,
                    secondary = secondary,
                    modifier = Modifier.align(Alignment.Center).height(190.dp),
                    primaryColor = accent
                )
                exercise?.let { e ->
                    Row(
                        Modifier.align(Alignment.BottomEnd).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            equipmentIcon(e.equipment),
                            contentDescription = e.equipment.displayName,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                        SystemChip(e.difficulty.displayName, accent = SparkColors.Amber)
                    }
                }
            }
        }

        exercise?.let { e ->
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        e.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = SparkColors.TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    LegendRow(accent)
                }
            }

            // ---- What it trains, as tags rather than prose ----
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(S.primaryMuscles, style = SystemLabel.copy(color = accent))
                    Spacer(Modifier.height(6.dp))
                    WrapRow(primary.toList())
                    if (secondary.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text(S.secondaryMuscles, style = SystemLabel)
                        Spacer(Modifier.height(6.dp))
                        WrapRow(secondary.toList())
                    }
                }
            }

            // ---- The cue, split into steps ----
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SystemPanel(title = S.howTo, accent = accent) {
                        e.instructions
                            .split(". ")
                            .map { it.trim().trimEnd('.') }
                            .filter { it.isNotBlank() }
                            .forEachIndexed { index, step ->
                                Row(
                                    Modifier.padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(accent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = accent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        step,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SparkColors.TextSecondary
                                    )
                                }
                            }
                    }
                }
            }
        }

        if (recent.isNotEmpty()) {
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SystemPanel(title = S.recentSets, accent = SparkColors.Success) {
                        recent.take(6).forEach { set ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${set.weightKg.toInt()} kg × ${set.reps}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SparkColors.TextPrimary
                                )
                                Text(
                                    "e1RM ${StrengthMath.estimatedOneRepMax(set.weightKg, set.reps).toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SparkColors.Cyan
                                )
                                Text(
                                    set.completedAt?.let { Dates.pretty(Dates.epochDay(it)) } ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SparkColors.TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Explains the two fill colours on the diagram above, in four words. */
@Composable
private fun LegendRow(accent: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        LegendDot(accent, S.primaryMuscles)
        LegendDot(SparkColors.Amber, S.secondaryMuscles)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = SystemLabel, color = SparkColors.TextMuted)
    }
}

/** Muscle tags that wrap onto as many rows as they need. */
@Composable
private fun WrapRow(muscles: List<Muscle>) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        muscles.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                row.forEach { MuscleDot(it) }
            }
        }
    }
}
