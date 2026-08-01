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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.S
import com.sparkgym.data.local.ExerciseWithMuscles
import com.sparkgym.ui.common.SparkTextField
import com.sparkgym.ui.workout.WorkoutViewModel.CustomDaySpec
import com.sparkgym.ui.workout.WorkoutViewModel.CustomExerciseSpec
import com.sparkgym.ui.workout.WorkoutViewModel.CustomRoutineSpec

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var days by remember { mutableStateOf(listOf(CustomDaySpec("Day 1", emptyList()))) }

    var pickingExerciseForDayIndex by remember { mutableStateOf<Int?>(null) }
    
    val library by viewModel.library.collectAsStateWithLifecycle()

    if (pickingExerciseForDayIndex != null) {
        ModalBottomSheet(
            onDismissRequest = { pickingExerciseForDayIndex = null },
            containerColor = SparkColors.Panel
        ) {
            ExercisePickerContent(
                library = library,
                onSelect = { exerciseId ->
                    val dayIndex = pickingExerciseForDayIndex!!
                    val day = days[dayIndex]
                    val updatedDay = day.copy(
                        exercises = day.exercises + CustomExerciseSpec(exerciseId, 3)
                    )
                    days = days.toMutableList().apply { set(dayIndex, updatedDay) }
                    pickingExerciseForDayIndex = null
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, S.back, tint = SparkColors.TextSecondary)
                }
                Column {
                    Text(S.createRoutine.uppercase(), style = SystemLabel.copy(color = SparkColors.Cyan))
                    Text(
                        S.custom,
                        style = MaterialTheme.typography.titleMedium,
                        color = SparkColors.TextPrimary
                    )
                }
            }
        }

        item {
            SystemPanel(title = S.routine) {
                SparkTextField(name, { name = it }, S.routineName, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                SparkTextField(notes, { notes = it }, S.description, Modifier.fillMaxWidth())
            }
        }

        itemsIndexed(days) { dayIndex, day ->
            SystemPanel(title = "${S.dayName} ${dayIndex + 1}", accent = SparkColors.Violet) {
                SparkTextField(
                    day.name,
                    { newName -> 
                        days = days.toMutableList().apply { set(dayIndex, day.copy(name = newName)) }
                    },
                    S.dayName,
                    Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(12.dp))
                
                if (day.exercises.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        day.exercises.forEachIndexed { exIndex, ex ->
                            val exerciseInfo = library.find { it.exercise.id == ex.exerciseId }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    exerciseInfo?.exercise?.name ?: "Unknown Exercise",
                                    color = SparkColors.TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                SparkTextField(
                                    value = ex.targetSets.toString(),
                                    onValueChange = { setsStr ->
                                        val sets = setsStr.toIntOrNull() ?: 0
                                        val updatedExercises = day.exercises.toMutableList().apply { 
                                            set(exIndex, ex.copy(targetSets = sets)) 
                                        }
                                        days = days.toMutableList().apply { 
                                            set(dayIndex, day.copy(exercises = updatedExercises)) 
                                        }
                                    },
                                    label = S.targetSets,
                                    keyboardType = KeyboardType.Number,
                                    modifier = Modifier.weight(0.3f)
                                )
                                IconButton(onClick = {
                                    val updatedExercises = day.exercises.toMutableList().apply { removeAt(exIndex) }
                                    days = days.toMutableList().apply { 
                                        set(dayIndex, day.copy(exercises = updatedExercises)) 
                                    }
                                }) {
                                    Icon(Icons.Filled.Delete, S.delete, tint = SparkColors.Danger)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                SystemButton(
                    S.addExercise,
                    onClick = { pickingExerciseForDayIndex = dayIndex },
                    modifier = Modifier.fillMaxWidth(),
                    accent = SparkColors.Violet.copy(alpha = 0.5f)
                )
            }
        }

        item {
            val dayNameLabel = S.dayName
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SystemButton(
                    S.addDay,
                    onClick = {
                        days = days + CustomDaySpec("$dayNameLabel ${days.size + 1}", emptyList())
                    },
                    modifier = Modifier.weight(1f),
                    accent = SparkColors.Divider
                )
                SystemButton(
                    S.save,
                    onClick = {
                        if (name.isNotBlank() && days.isNotEmpty()) {
                            viewModel.saveCustomRoutine(CustomRoutineSpec(name, notes, days)) {
                                onDone()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    accent = SparkColors.Success
                )
            }
        }
    }
}

@Composable
private fun ExercisePickerContent(
    library: List<ExerciseWithMuscles>,
    onSelect: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                S.exercise,
                style = MaterialTheme.typography.titleMedium,
                color = SparkColors.TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        itemsIndexed(library) { _, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(item.exercise.id) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.exercise.name,
                    color = SparkColors.TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
