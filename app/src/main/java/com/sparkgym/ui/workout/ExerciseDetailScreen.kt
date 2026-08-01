package com.sparkgym.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.Dates
import com.sparkgym.data.local.SetLogEntity
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.engine.StrengthMath
import com.sparkgym.domain.model.Muscle
import com.sparkgym.ui.heatmap.MuscleHeatMap

/**
 * Exercise reference page: how to do it, what it trains (shown on the same
 * body map used everywhere else), and your own history with it.
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SparkColors.TextSecondary)
                }
                Column(Modifier.weight(1f)) {
                    Text("EXERCISE", style = SystemLabel.copy(color = SparkColors.Cyan))
                    Text(
                        exercise?.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = SparkColors.TextPrimary
                    )
                }
            }
        }

        exercise?.let { e ->
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SystemChip(e.equipment.displayName, accent = SparkColors.Violet)
                    SystemChip(e.difficulty.displayName, accent = SparkColors.Amber)
                    SystemChip(e.force.name.lowercase())
                }
            }

            item {
                SystemPanel(title = "How to") {
                    Text(
                        e.instructions,
                        color = SparkColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        detail?.let { withMuscles ->
            item {
                // Reuse the heat map renderer with a synthetic "heat" set to the
                // contribution values — the same picture, meaning "what this trains".
                val synthetic = withMuscles.muscles.mapNotNull { link ->
                    Muscle.fromKey(link.muscle)?.let { muscle ->
                        muscle to HeatmapEngine.MuscleHeat(
                            muscle = muscle,
                            effectiveSets = 0.0,
                            volumeKg = 0.0,
                            target = 1.0,
                            intensity = if (link.contribution >= 1f) 1.0f else 0.5f,
                            status = HeatmapEngine.Status.OPTIMAL
                        )
                    }
                }.toMap()

                SystemPanel(title = "Muscles worked", accent = SparkColors.Violet) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MuscleHeatMap(synthetic, isFront = true, modifier = Modifier.height(190.dp))
                        MuscleHeatMap(synthetic, isFront = false, modifier = Modifier.height(190.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            val primary = withMuscles.muscles.filter { it.contribution >= 1f }
                                .mapNotNull { Muscle.fromKey(it.muscle)?.displayName }
                            val secondary = withMuscles.muscles.filter { it.contribution < 1f }
                                .mapNotNull { Muscle.fromKey(it.muscle)?.displayName }
                            Text("PRIMARY", style = SystemLabel.copy(color = SparkColors.Danger))
                            Text(
                                primary.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.TextPrimary
                            )
                            if (secondary.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text("SECONDARY", style = SystemLabel)
                                Text(
                                    secondary.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
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
                SystemPanel(title = "Recent sets", accent = SparkColors.Success) {
                    recent.take(8).forEach { set ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${set.weightKg.toInt()} kg × ${set.reps}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.TextSecondary
                            )
                            Text(
                                "e1RM ${StrengthMath.estimatedOneRepMax(set.weightKg, set.reps).toInt()} kg",
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
