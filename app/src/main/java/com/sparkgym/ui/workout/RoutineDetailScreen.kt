package com.sparkgym.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.sparkgym.ui.common.SelectableChip

@Composable
fun RoutineDetailScreen(
    viewModel: WorkoutViewModel,
    routineId: Long,
    onBack: () -> Unit,
    onStartSession: (Long) -> Unit,
    onOpenExercise: (Long) -> Unit
) {
    LaunchedEffect(routineId) { viewModel.selectRoutine(routineId) }

    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val days by viewModel.routineDays.collectAsStateWithLifecycle()
    val selectedDayId by viewModel.selectedDayId.collectAsStateWithLifecycle()
    val exercises by viewModel.dayExercises.collectAsStateWithLifecycle()
    val startedId by viewModel.startedSessionId.collectAsStateWithLifecycle()

    val routine = routines.firstOrNull { it.id == routineId }

    // Default to the first day so the screen is never empty on arrival.
    LaunchedEffect(days) {
        if (selectedDayId == null) days.firstOrNull()?.let { viewModel.selectDay(it.id) }
    }

    LaunchedEffect(startedId) {
        startedId?.let {
            viewModel.consumeStartedSession()
            onStartSession(it)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SparkColors.TextSecondary)
                }
                Column(Modifier.weight(1f)) {
                    Text("ROUTINE", style = SystemLabel.copy(color = SparkColors.Cyan))
                    Text(
                        routine?.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        color = SparkColors.TextPrimary
                    )
                }
            }
        }

        routine?.let {
            item {
                SystemPanel {
                    Text(it.description, color = SparkColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SystemChip("${it.daysPerWeek} days/week")
                        SystemChip(it.level, accent = SparkColors.Violet)
                        SystemChip(it.goal, accent = SparkColors.Amber)
                    }
                    Spacer(Modifier.height(12.dp))
                    SystemButton(
                        "Set as active routine",
                        { viewModel.setActiveRoutine(routineId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (it.isCustom) {
                        Spacer(Modifier.height(10.dp))
                        SystemButton(
                            "Delete custom routine",
                            { 
                                viewModel.deleteRoutine(routineId)
                                onBack() 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            accent = SparkColors.Danger
                        )
                    }
                }
            }
        }

        item {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days.size) { index ->
                    val day = days[index]
                    SelectableChip(
                        text = day.name,
                        selected = day.id == selectedDayId,
                        onClick = { viewModel.selectDay(day.id) }
                    )
                }
            }
        }

        val currentDay = days.firstOrNull { it.id == selectedDayId }
        if (currentDay != null) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(currentDay.name, style = MaterialTheme.typography.titleMedium, color = SparkColors.TextPrimary)
                        Text(currentDay.focus, style = SystemLabel, color = SparkColors.TextMuted)
                    }
                    SystemButton(
                        "Start",
                        { viewModel.startFromDay(currentDay) },
                        icon = Icons.Filled.PlayArrow,
                        accent = SparkColors.Success
                    )
                }
            }
        }

        if (exercises.isEmpty()) {
            item { EmptyState("Pick a day to see its exercises.") }
        }

        items(exercises, key = { it.prescription.id }) { item ->
            SystemPanel(
                modifier = Modifier.fillMaxWidth().clickable { onOpenExercise(item.exercise.id) },
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.exercise.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = SparkColors.TextPrimary
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "${item.prescription.targetSets} × ${item.prescription.repsMin}-${item.prescription.repsMax}" +
                                " · rest ${item.prescription.restSeconds}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = SparkColors.TextMuted
                        )
                        if (item.prescription.notes.isNotBlank()) {
                            Spacer(Modifier.height(3.dp))
                            Text(
                                item.prescription.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = SparkColors.Cyan
                            )
                        }
                    }
                    SystemChip(item.exercise.equipment.displayName, accent = SparkColors.Violet)
                }
            }
        }
    }
}
