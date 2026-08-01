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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.sparkgym.core.util.Dates
import com.sparkgym.core.util.compactVolume
import com.sparkgym.data.local.SetLogEntity
import com.sparkgym.domain.engine.StrengthMath
import com.sparkgym.domain.model.TrackingType
import com.sparkgym.ui.common.ConfirmDialog
import com.sparkgym.ui.common.NumberCell
import com.sparkgym.ui.common.SystemMessageDialog
import kotlin.math.roundToInt

@Composable
fun ActiveSessionScreen(
    viewModel: SessionViewModel,
    workoutViewModel: WorkoutViewModel,
    sessionId: Long,
    onExit: () -> Unit
) {
    LaunchedEffect(sessionId) { viewModel.bind(sessionId) }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val rest by viewModel.rest.collectAsStateWithLifecycle()
    val finished by viewModel.finished.collectAsStateWithLifecycle()
    val prFlash by viewModel.prFlash.collectAsStateWithLifecycle()

    var showDiscard by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(SparkColors.Void)) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("IN PROGRESS", style = SystemLabel.copy(color = SparkColors.Success))
                        Text(
                            state.session?.name ?: "Session",
                            style = MaterialTheme.typography.titleLarge,
                            color = SparkColors.TextPrimary
                        )
                    }
                    Text(
                        Dates.stopwatch(state.elapsedSeconds),
                        style = MaterialTheme.typography.headlineSmall,
                        color = SparkColors.Cyan
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MiniStat("${state.completedSets}/${state.totalSets}", "Sets", Modifier.weight(1f))
                    MiniStat("${state.volumeKg.compactVolume()} kg", "Volume", Modifier.weight(1f))
                    MiniStat(
                        "${state.blocks.count { it.completedSets > 0 }}/${state.blocks.size}",
                        "Exercises",
                        Modifier.weight(1f)
                    )
                }
            }

            items(state.blocks, key = { it.exercise.id }) { block ->
                ExerciseBlockCard(
                    block = block,
                    defaultRest = DEFAULT_REST_SECONDS,
                    onUpdate = viewModel::updateSet,
                    onComplete = { set, rest -> viewModel.completeSet(set, rest) },
                    onUncomplete = viewModel::uncompleteSet,
                    onAddSet = { viewModel.addSet(block.exercise.id) },
                    onDeleteSet = viewModel::deleteSet
                )
            }

            item {
                SystemButton(
                    "Add exercise",
                    { showPicker = true },
                    icon = Icons.Filled.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    SystemButton(
                        "Discard",
                        { showDiscard = true },
                        accent = SparkColors.Danger,
                        modifier = Modifier.weight(1f)
                    )
                    SystemButton(
                        "Finish",
                        { viewModel.finish() },
                        accent = SparkColors.Success,
                        enabled = state.completedSets > 0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        rest?.let { timer ->
            RestTimerBar(
                timer = timer,
                onAdjust = viewModel::adjustRest,
                onSkip = viewModel::stopRest,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showPicker) {
        ExercisePickerDialog(
            workoutViewModel = workoutViewModel,
            onPick = {
                viewModel.addExercise(it)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }

    if (showDiscard) {
        ConfirmDialog(
            title = "Discard session",
            body = "Every set logged in this session will be deleted. This cannot be undone.",
            confirmText = "Discard",
            onConfirm = {
                showDiscard = false
                viewModel.discard()
                onExit()
            },
            onDismiss = { showDiscard = false }
        )
    }

    prFlash?.let {
        SystemMessageDialog(
            title = "New record",
            lines = listOf(it, "The System has recorded your best lift."),
            accent = SparkColors.Amber,
            onDismiss = viewModel::clearPrFlash
        )
    }

    finished?.let { outcome ->
        val lines = buildList {
            add("${outcome.summary.completedSets} sets · ${outcome.summary.volumeKg.roundToInt()} kg")
            addAll(outcome.xp.breakdown.map { "${it.first}: +${it.second}" })
            if (outcome.xp.multiplier > 1.0) {
                add("Streak bonus ×${String.format(java.util.Locale.US, "%.2f", outcome.xp.multiplier)}")
            }
            add("Total +${outcome.xp.total} XP")
            outcome.levelsGained?.let { range ->
                add("LEVEL UP → ${range.last}")
                add("+${outcome.pointsGained} attribute points")
            }
            outcome.newAchievements.forEach { add("Achievement unlocked") }
        }
        SystemMessageDialog(
            title = "Session complete",
            lines = lines,
            accent = SparkColors.Success,
            onDismiss = {
                viewModel.consumeFinish()
                onExit()
            }
        )
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier = Modifier) {
    SystemPanel(modifier = modifier, contentPadding = PaddingValues(10.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = SparkColors.TextPrimary)
        Text(label.uppercase(), style = SystemLabel)
    }
}

@Composable
private fun ExerciseBlockCard(
    block: SessionViewModel.ExerciseBlock,
    defaultRest: Int,
    onUpdate: (SetLogEntity) -> Unit,
    onComplete: (SetLogEntity, Int) -> Unit,
    onUncomplete: (SetLogEntity) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (Long) -> Unit
) {
    val tracking = block.exercise.tracking

    SystemPanel(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    block.exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = SparkColors.TextPrimary
                )
                if (block.lastTime.isNotEmpty()) {
                    val last = block.lastTime.first()
                    Text(
                        "Last time: ${last.weightKg.roundToInt()} kg × ${last.reps}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SparkColors.TextMuted
                    )
                }
            }
            if (block.bestEstimated1Rm > 0) {
                SystemChip("e1RM ${block.bestEstimated1Rm.roundToInt()}", accent = SparkColors.Amber)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Column headers depend on how the exercise is measured.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SET", style = SystemLabel, modifier = Modifier.width(34.dp))
            when (tracking) {
                TrackingType.DURATION -> {
                    Text("SECONDS", style = SystemLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
                TrackingType.DISTANCE_DURATION -> {
                    Text("METRES", style = SystemLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("SECONDS", style = SystemLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
                TrackingType.REPS_ONLY -> {
                    Text("REPS", style = SystemLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
                else -> {
                    Text("KG", style = SystemLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("REPS", style = SystemLabel, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(Modifier.width(44.dp))
        }

        block.sets.forEach { set ->
            SetRow(
                set = set,
                tracking = tracking,
                onUpdate = onUpdate,
                onComplete = { onComplete(it, defaultRest) },
                onUncomplete = onUncomplete,
                onDelete = { onDeleteSet(set.id) }
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onAddSet).padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, null, tint = SparkColors.Cyan, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("ADD SET", style = SystemLabel.copy(color = SparkColors.Cyan))
        }
    }
}

@Composable
private fun SetRow(
    set: SetLogEntity,
    tracking: TrackingType,
    onUpdate: (SetLogEntity) -> Unit,
    onComplete: (SetLogEntity) -> Unit,
    onUncomplete: (SetLogEntity) -> Unit,
    onDelete: () -> Unit
) {
    // Local text state so typing "12.5" does not fight the database round-trip.
    var weightText by remember(set.id) {
        mutableStateOf(if (set.weightKg > 0) trimNumber(set.weightKg) else "")
    }
    var repsText by remember(set.id) { mutableStateOf(if (set.reps > 0) set.reps.toString() else "") }
    var secondsText by remember(set.id) {
        mutableStateOf(if (set.durationSeconds > 0) set.durationSeconds.toString() else "")
    }
    var metersText by remember(set.id) {
        mutableStateOf(if (set.distanceMeters > 0) set.distanceMeters.roundToInt().toString() else "")
    }

    fun current() = set.copy(
        weightKg = weightText.toDoubleOrNull() ?: 0.0,
        reps = repsText.toIntOrNull() ?: 0,
        durationSeconds = secondsText.toIntOrNull() ?: 0,
        distanceMeters = metersText.toDoubleOrNull() ?: 0.0
    )

    val done = set.isCompleted
    val rowAccent = when {
        set.isPersonalRecord -> SparkColors.Amber
        done -> SparkColors.Success
        else -> SparkColors.TextSecondary
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            set.setNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = rowAccent,
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.Center
        )

        when (tracking) {
            TrackingType.DURATION -> {
                NumberCell(secondsText, { secondsText = it; onUpdate(current()) }, Modifier.weight(1f), !done)
            }
            TrackingType.DISTANCE_DURATION -> {
                NumberCell(metersText, { metersText = it; onUpdate(current()) }, Modifier.weight(1f), !done)
                Spacer(Modifier.width(6.dp))
                NumberCell(secondsText, { secondsText = it; onUpdate(current()) }, Modifier.weight(1f), !done)
            }
            TrackingType.REPS_ONLY -> {
                NumberCell(repsText, { repsText = it; onUpdate(current()) }, Modifier.weight(1f), !done)
            }
            else -> {
                NumberCell(weightText, { weightText = it; onUpdate(current()) }, Modifier.weight(1f), !done, decimal = true)
                Spacer(Modifier.width(6.dp))
                NumberCell(repsText, { repsText = it; onUpdate(current()) }, Modifier.weight(1f), !done)
            }
        }

        IconButton(
            onClick = { if (done) onUncomplete(set) else onComplete(current()) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = if (done) "Undo set" else "Complete set",
                tint = if (done) SparkColors.Success else SparkColors.TextMuted
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Delete set",
                tint = SparkColors.TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun RestTimerBar(
    timer: SessionViewModel.RestTimer,
    onAdjust: (Int) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val done = timer.remainingSeconds <= 0
    SystemPanel(
        modifier = modifier.fillMaxWidth().padding(12.dp),
        accent = if (done) SparkColors.Success else SparkColors.Cyan,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (done) "REST COMPLETE" else "REST",
                    style = SystemLabel.copy(color = if (done) SparkColors.Success else SparkColors.Cyan)
                )
                Text(
                    Dates.stopwatch(timer.remainingSeconds),
                    style = MaterialTheme.typography.headlineSmall,
                    color = SparkColors.TextPrimary
                )
            }
            SystemButton("-15", { onAdjust(-15) }, accent = SparkColors.TextMuted)
            Spacer(Modifier.width(6.dp))
            SystemButton("+30", { onAdjust(30) })
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = onSkip) {
                Icon(Icons.Filled.Close, "Skip rest", tint = SparkColors.TextMuted)
            }
        }
        Spacer(Modifier.height(8.dp))
        SegmentedBar(
            progress = timer.progress,
            color = if (done) SparkColors.Success else SparkColors.Cyan,
            height = 6.dp
        )
    }
}

@Composable
private fun ExercisePickerDialog(
    workoutViewModel: WorkoutViewModel,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val library by workoutViewModel.library.collectAsStateWithLifecycle()
    val filters by workoutViewModel.filters.collectAsStateWithLifecycle()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        SystemPanel(modifier = Modifier.fillMaxWidth(), title = "Add exercise") {
            com.sparkgym.ui.common.SparkTextField(
                value = filters.query,
                onValueChange = workoutViewModel::setQuery,
                label = "Search",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.height(320.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items(library, key = { it.exercise.id }) { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(item.exercise.id) }
                            .padding(vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                item.exercise.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SparkColors.TextPrimary
                            )
                            Text(
                                item.exercise.equipment.displayName,
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

/** Fallback rest length; the profile's preference overrides it per routine. */
private const val DEFAULT_REST_SECONDS = 90

private fun trimNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(java.util.Locale.US, "%.1f", value)

/** Kept next to the logger because it is only ever used to explain a set's load. */
internal fun setLoadHint(tracking: TrackingType, weight: Double, reps: Int, bodyweight: Double): String {
    val volume = StrengthMath.setVolumeKg(tracking, weight, reps, 0, 0.0, bodyweight)
    return "${volume.roundToInt()} kg volume"
}
