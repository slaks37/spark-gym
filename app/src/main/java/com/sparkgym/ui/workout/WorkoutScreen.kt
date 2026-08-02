package com.sparkgym.ui.workout

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.EmptyState
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.Dates
import com.sparkgym.core.util.compactVolume
import com.sparkgym.core.util.S
import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.Muscle
import com.sparkgym.ui.common.ChipRow
import com.sparkgym.ui.common.SelectableChip
import com.sparkgym.ui.common.SparkTextField

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onOpenSession: (Long) -> Unit,
    onOpenRoutine: (Long) -> Unit,
    onOpenExercise: (Long) -> Unit,
    onOpenBodyweight: () -> Unit,
    onOpenBuilder: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val startedId by viewModel.startedSessionId.collectAsStateWithLifecycle()

    LaunchedEffect(startedId) {
        startedId?.let {
            viewModel.consumeStartedSession()
            onOpenSession(it)
        }
    }

    Column(Modifier.fillMaxSize().background(SparkColors.Void)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(S.training.uppercase(), style = SystemLabel.copy(color = SparkColors.Cyan))
            Spacer(Modifier.height(8.dp))

            activeSession?.let { session ->
                SystemPanel(accent = SparkColors.Success, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(S.sessionInProgress.uppercase(), style = SystemLabel.copy(color = SparkColors.Success))
                            Text(
                                session.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = SparkColors.TextPrimary
                            )
                        }
                        SystemButton(S.resume, { onOpenSession(session.id) }, accent = SparkColors.Success)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            SystemPanel(
                accent = SparkColors.Violet,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenBodyweight),
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.SelfImprovement,
                        contentDescription = null,
                        tint = SparkColors.Violet,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(S.bodyweight.uppercase(), style = SystemLabel.copy(color = SparkColors.Violet))
                        Text(
                            S.bodyweightDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = SparkColors.TextMuted
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = SparkColors.Violet
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(S.routines, S.library, S.history).forEachIndexed { index, label ->
                    SelectableChip(label, tab == index, { tab = index })
                }
            }
        }

        when (tab) {
            0 -> RoutinesTab(viewModel, onOpenRoutine, onOpenBuilder) { viewModel.startEmptySession() }
            1 -> LibraryTab(viewModel, onOpenExercise)
            else -> HistoryTab(viewModel)
        }
    }
}

@Composable
private fun RoutinesTab(
    viewModel: WorkoutViewModel,
    onOpenRoutine: (Long) -> Unit,
    onOpenBuilder: () -> Unit,
    onStartEmpty: () -> Unit
) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    
    val folderedRoutines = remember(routines) { routines.groupBy { it.folderId } }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SystemButton(
                    S.startEmptySession,
                    onStartEmpty,
                    icon = Icons.Filled.PlayArrow,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    S.createRoutine,
                    onOpenBuilder,
                    icon = Icons.Filled.Add,
                    modifier = Modifier.weight(1f),
                    accent = SparkColors.Violet
                )
            }
        }

        folders.forEach { folder ->
            item(key = "folder_${folder.id}") {
                Text(
                    folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = SparkColors.TextPrimary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            val routinesInFolder = folderedRoutines[folder.id] ?: emptyList()
            items(routinesInFolder, key = { it.id }) { routine ->
                RoutineCard(routine = routine, onOpen = { onOpenRoutine(routine.id) })
            }
        }

        val unfoldered = folderedRoutines[null] ?: emptyList()
        if (unfoldered.isNotEmpty() && folders.isNotEmpty()) {
            item(key = "folder_unfoldered") {
                Text(
                    S.routines,
                    style = MaterialTheme.typography.titleMedium,
                    color = SparkColors.TextPrimary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
        }
        items(unfoldered, key = { it.id }) { routine ->
            RoutineCard(routine = routine, onOpen = { onOpenRoutine(routine.id) })
        }
    }
}

@Composable
private fun RoutineCard(routine: com.sparkgym.data.local.RoutineEntity, onOpen: () -> Unit) {
    SystemPanel(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        accent = if (routine.homeFriendly) SparkColors.Success else SparkColors.Cyan
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = SparkColors.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    routine.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextMuted
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SystemChip("${routine.daysPerWeek} ${S.days}")
                    SystemChip(routine.level, accent = SparkColors.Violet)
                    SystemChip(routine.goal, accent = SparkColors.Amber)
                    if (routine.homeFriendly) SystemChip(S.home, accent = SparkColors.Success)
                }
            }
        }
    }
}

@Composable
private fun LibraryTab(viewModel: WorkoutViewModel, onOpenExercise: (Long) -> Unit) {
    val library by viewModel.library.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()

    Column {
        Column(Modifier.padding(horizontal = 16.dp)) {
            SparkTextField(
                value = filters.query,
                onValueChange = viewModel::setQuery,
                label = S.searchExercises,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            ChipRow(
                options = Muscle.entries.toList(),
                selected = filters.muscle,
                label = { it.displayName },
                onSelect = viewModel::setMuscle
            )
            Spacer(Modifier.height(8.dp))
            ChipRow(
                options = Equipment.entries.toList(),
                selected = filters.equipment,
                label = { it.displayName },
                onSelect = viewModel::setEquipment,
                accent = SparkColors.Violet
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableChip(S.favourites, filters.favoritesOnly, viewModel::toggleFavoritesOnly, accent = SparkColors.Amber)
                SelectableChip(S.homeOnly, filters.homeOnly, viewModel::toggleHomeOnly, accent = SparkColors.Success)
            }
            Spacer(Modifier.height(10.dp))
        }

        if (library.isEmpty()) {
            EmptyState(S.noMatchFilter)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(library, key = { it.exercise.id }) { item ->
                    val e = item.exercise
                    SystemPanel(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenExercise(e.id) },
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val primaryMuscleKey = item.muscles
                                .filter { it.contribution >= 1f }
                                .mapNotNull { Muscle.fromKey(it.muscle) }
                                .firstOrNull()
                                
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val iconName = "ex_" + e.name.lowercase().replace(" ", "_").replace("-", "_")
                            val specificResId = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                            
                            val imageResId = if (specificResId != 0) {
                                specificResId
                            } else when {
                                e.equipment == Equipment.CARDIO -> com.sparkgym.R.drawable.gym_hiit_runner
                                else -> when (primaryMuscleKey) {
                                    Muscle.CHEST -> com.sparkgym.R.drawable.exercise_dumbbell_flys
                                    Muscle.TRICEPS -> com.sparkgym.R.drawable.muscle_triceps
                                    Muscle.FOREARMS -> com.sparkgym.R.drawable.muscle_forearms
                                    Muscle.FRONT_DELTS, Muscle.SIDE_DELTS -> com.sparkgym.R.drawable.muscle_shoulders
                                    Muscle.LATS -> com.sparkgym.R.drawable.muscle_lats
                                    Muscle.TRAPS -> com.sparkgym.R.drawable.muscle_traps
                                    Muscle.LOWER_BACK, Muscle.REAR_DELTS -> com.sparkgym.R.drawable.muscle_lower_back
                                    Muscle.HAMSTRINGS -> com.sparkgym.R.drawable.muscle_hamstrings
                                    Muscle.GLUTES -> com.sparkgym.R.drawable.muscle_glutes
                                    Muscle.CALVES -> com.sparkgym.R.drawable.muscle_calves
                                    Muscle.QUADS, Muscle.ADDUCTORS, Muscle.ABDUCTORS -> com.sparkgym.R.drawable.gym_squat_athlete
                                    Muscle.BICEPS -> com.sparkgym.R.drawable.exercise_preacher_curls
                                    Muscle.ABS -> com.sparkgym.R.drawable.gym_pushup_athlete
                                    Muscle.OBLIQUES -> com.sparkgym.R.drawable.muscle_obliques
                                    Muscle.NECK -> com.sparkgym.R.drawable.muscle_neck
                                    else -> com.sparkgym.R.drawable.tool_dumbbells_rack
                                }
                            }

                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    e.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SparkColors.TextPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                val primaryStr = item.muscles
                                    .filter { it.contribution >= 1f }
                                    .mapNotNull { Muscle.fromKey(it.muscle)?.displayName }
                                Text(
                                    "${e.equipment.displayName} · ${primaryStr.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SparkColors.TextMuted
                                )
                            }
                            Icon(
                                if (e.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Favourite",
                                tint = if (e.isFavorite) SparkColors.Amber else SparkColors.TextMuted,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { viewModel.toggleFavorite(e.id, !e.isFavorite) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(viewModel: WorkoutViewModel) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val prs by viewModel.personalRecords.collectAsStateWithLifecycle()
    val names by viewModel.exerciseNames.collectAsStateWithLifecycle()
    val trend by viewModel.volumeTrend.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (trend.isNotEmpty()) {
            item {
                SystemPanel(title = S.volumeLast30, accent = SparkColors.Violet) {
                    VolumeBars(
                        values = trend.map { it.volumeKg },
                        modifier = Modifier.fillMaxWidth().height(110.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${trend.sumOf { it.volumeKg }.compactVolume()} kg ${S.of} ${trend.size} ${S.sessions}",
                        style = SystemLabel,
                        color = SparkColors.TextMuted
                    )
                }
            }
        }

        if (prs.isNotEmpty()) {
            item {
                SystemPanel(title = S.personalRecords, accent = SparkColors.Amber) {
                    prs.sortedByDescending { it.bestEstimated1RmKg }.take(6).forEach { pr ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                names[pr.exerciseId] ?: S.exercise,
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.TextSecondary
                            )
                            Text(
                                "${pr.bestEstimated1RmKg.toInt()} kg e1RM",
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.Amber
                            )
                        }
                    }
                }
            }
        }

        if (history.isEmpty()) {
            item { EmptyState(S.noHistoryYet) }
        }

        items(history, key = { it.id }) { session ->
            SystemPanel(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            session.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = SparkColors.TextPrimary
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            Dates.pretty(session.dateEpochDay),
                            style = SystemLabel,
                            color = SparkColors.TextMuted
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${session.totalSets} ${S.sets}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SparkColors.TextSecondary
                        )
                        Text(
                            "${session.totalVolumeKg.compactVolume()} kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = SparkColors.Cyan
                        )
                        if (session.xpAwarded > 0) {
                            Text(
                                "+${session.xpAwarded} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.Violet
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Minimal bar chart — no charting dependency for eight lines of drawing. */
@Composable
fun VolumeBars(values: List<Double>, modifier: Modifier = Modifier) {
    val max = remember(values) { values.maxOrNull() ?: 0.0 }
    androidx.compose.foundation.Canvas(modifier) {
        if (values.isEmpty() || max <= 0.0) return@Canvas
        val gap = size.width * 0.01f
        val barWidth = (size.width - gap * (values.size - 1)) / values.size
        values.forEachIndexed { index, value ->
            val h = (value / max).toFloat() * size.height
            drawRect(
                color = SparkColors.Violet.copy(alpha = 0.35f + 0.55f * (value / max).toFloat()),
                topLeft = androidx.compose.ui.geometry.Offset(index * (barWidth + gap), size.height - h),
                size = androidx.compose.ui.geometry.Size(barWidth, h)
            )
        }
    }
}
