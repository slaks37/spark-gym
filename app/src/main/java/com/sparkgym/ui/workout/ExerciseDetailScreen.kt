package com.sparkgym.ui.workout

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparkgym.R
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.Dates
import com.sparkgym.core.util.S
import com.sparkgym.data.local.SetLogEntity
import com.sparkgym.di.AppContainer
import com.sparkgym.domain.engine.HeatmapEngine
import com.sparkgym.domain.engine.StrengthMath
import com.sparkgym.domain.model.Muscle
import com.sparkgym.ui.heatmap.MuscleHeatMap

/**
 * Exercise reference page: highly aesthetic hero layout, details, and history.
 */
@Composable
fun ExerciseDetailScreen(
    container: AppContainer,
    exerciseId: Long,
    onBack: () -> Unit
) {
    val detail by container.workoutRepository.observeExercise(exerciseId)
        .collectAsState(initial = null)

    var historySets by remember { mutableStateOf<List<com.sparkgym.domain.model.ExerciseHistoryRow>>(emptyList()) }
    var estimatedMaxHistory by remember { mutableStateOf<List<com.sparkgym.data.local.VolumePointRow>>(emptyList()) }
    var volumePoints by remember { mutableStateOf<List<com.sparkgym.data.local.VolumePointRow>>(emptyList()) }
    
    LaunchedEffect(exerciseId) {
        historySets = container.workoutRepository.getExerciseHistorySets(exerciseId)
        estimatedMaxHistory = container.workoutRepository.getEstimatedMaxHistory(exerciseId)
        volumePoints = container.workoutRepository.getVolumePoints(exerciseId)
    }

    val exercise = detail?.exercise

    Box(modifier = Modifier.fillMaxSize().background(SparkColors.Void)) {
        // Hero Image Background
        Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            val primaryMuscleKey = detail?.muscles
                ?.filter { it.contribution >= 1f }
                ?.mapNotNull { Muscle.fromKey(it.muscle) }
                ?.firstOrNull()
                
            val context = androidx.compose.ui.platform.LocalContext.current
            val exerciseName = detail?.exercise?.name ?: ""
            val iconName = "ex_" + exerciseName.lowercase().replace(" ", "_").replace("-", "_")
            val specificResId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                
            val imageResId = if (specificResId != 0) {
                specificResId
            } else when {
                detail?.exercise?.equipment == com.sparkgym.domain.model.Equipment.CARDIO -> R.drawable.gym_hiit_runner
                else -> when (primaryMuscleKey) {
                    Muscle.CHEST -> R.drawable.exercise_dumbbell_flys
                    Muscle.TRICEPS -> R.drawable.muscle_triceps
                    Muscle.FOREARMS -> R.drawable.muscle_forearms
                    Muscle.FRONT_DELTS, Muscle.SIDE_DELTS -> R.drawable.muscle_shoulders
                    Muscle.LATS -> R.drawable.muscle_lats
                    Muscle.TRAPS -> R.drawable.muscle_traps
                    Muscle.LOWER_BACK, Muscle.REAR_DELTS -> R.drawable.muscle_lower_back
                    Muscle.HAMSTRINGS -> R.drawable.muscle_hamstrings
                    Muscle.GLUTES -> R.drawable.muscle_glutes
                    Muscle.CALVES -> R.drawable.muscle_calves
                    Muscle.QUADS, Muscle.ADDUCTORS, Muscle.ABDUCTORS -> R.drawable.gym_squat_athlete
                    Muscle.BICEPS -> R.drawable.exercise_preacher_curls
                    Muscle.ABS -> R.drawable.gym_pushup_athlete
                    Muscle.OBLIQUES -> R.drawable.muscle_obliques
                    Muscle.NECK -> R.drawable.muscle_neck
                    else -> R.drawable.tool_dumbbells_rack
                }
            }
            
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay for smooth transition
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, SparkColors.Void),
                            startY = 300f
                        )
                    )
            )
            
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 220.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        S.exercise,
                        style = SystemLabel.copy(color = SparkColors.Cyan),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        exercise?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = SparkColors.TextPrimary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                        )
                    )
                    
                    Spacer(Modifier.height(12.dp))

                    exercise?.let { e ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SystemChip(e.equipment.displayName, accent = SparkColors.Violet, filled = true)
                            SystemChip(e.difficulty.displayName, accent = SparkColors.Amber, filled = true)
                            SystemChip(e.force.name.lowercase(), accent = SparkColors.TextSecondary)
                        }
                    }
                }
            }

            exercise?.let { e ->
                item {
                    SystemPanel(
                        title = S.howTo, 
                        accent = SparkColors.Cyan,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Text(
                            e.instructions,
                            color = SparkColors.TextSecondary,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            detail?.let { withMuscles ->
                item {
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

                    SystemPanel(
                        title = S.musclesWorked, 
                        accent = SparkColors.Violet,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MuscleHeatMap(synthetic, isFront = true, modifier = Modifier.height(190.dp))
                            MuscleHeatMap(synthetic, isFront = false, modifier = Modifier.height(190.dp))
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                val primaryList = withMuscles.muscles.filter { it.contribution >= 1f }
                                    .mapNotNull { Muscle.fromKey(it.muscle)?.displayName }
                                val secondaryList = withMuscles.muscles.filter { it.contribution < 1f }
                                    .mapNotNull { Muscle.fromKey(it.muscle)?.displayName }
                                
                                Text(S.primary, style = SystemLabel.copy(color = SparkColors.Danger))
                                Text(
                                    primaryList.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SparkColors.TextPrimary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )
                                
                                if (secondaryList.isNotEmpty()) {
                                    Text(S.secondary, style = SystemLabel.copy(color = SparkColors.TextMuted))
                                    Text(
                                        secondaryList.joinToString(", "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SparkColors.TextSecondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                ExerciseHistoryView(
                    history = historySets,
                    estimatedMaxHistory = estimatedMaxHistory,
                    volumePoints = volumePoints
                )
            }
        }
    }
}
