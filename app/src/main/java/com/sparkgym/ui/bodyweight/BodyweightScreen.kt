package com.sparkgym.ui.bodyweight

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.StatTile
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.domain.engine.BodyweightEngine
import com.sparkgym.domain.model.BodyweightFocus
import com.sparkgym.domain.model.BodyweightLevel
import com.sparkgym.domain.model.ResolvedCircuit
import com.sparkgym.domain.model.ResolvedMove

/**
 * The bodyweight tab: a daily pick, a level switch, focus filters and the
 * circuit list. Starting one hands straight off to the normal session logger.
 */
@Composable
fun BodyweightScreen(
    viewModel: BodyweightViewModel,
    onBack: () -> Unit,
    onOpenSession: (Long) -> Unit
) {
    val circuits by viewModel.circuits.collectAsStateWithLifecycle()
    val featured by viewModel.featured.collectAsStateWithLifecycle()
    val focus by viewModel.focus.collectAsStateWithLifecycle()
    val level by viewModel.level.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val moveNames by viewModel.moveNames.collectAsStateWithLifecycle()
    val weekCount by viewModel.weekCount.collectAsStateWithLifecycle()
    val bodyweightKg by viewModel.bodyweightKg.collectAsStateWithLifecycle()
    val startedSessionId by viewModel.startedSessionId.collectAsStateWithLifecycle()

    LaunchedEffect(startedSessionId) {
        startedSessionId?.let {
            onOpenSession(it)
            viewModel.consumeStartedSession()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SparkColors.TextMuted
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("NO EQUIPMENT", style = SystemLabel.copy(color = SparkColors.Violet))
                }
                Text(
                    "Bodyweight",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SparkColors.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Circuits you can run in a bedroom. Every finished round feeds the heat map and your attributes, same as a gym session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextMuted
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = "$weekCount / 7",
                    label = "This week",
                    accent = SparkColors.Violet,
                    icon = Icons.Filled.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "${circuits.size}",
                    label = "Circuits",
                    accent = SparkColors.Cyan,
                    icon = Icons.Filled.Bolt,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { LevelSwitch(level, viewModel::setLevel) }

        featured?.let { pick ->
            item {
                FeaturedCircuit(
                    resolved = pick,
                    bodyweightKg = bodyweightKg,
                    onOpen = { viewModel.open(pick.circuit) }
                )
            }
        }

        item { FocusFilter(focus, viewModel::setFocus) }

        items(circuits, key = { it.circuit.slug }) { resolved ->
            CircuitCard(
                resolved = resolved,
                bodyweightKg = bodyweightKg,
                onClick = { viewModel.open(resolved.circuit) }
            )
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    selected?.let { resolved ->
        CircuitDetailDialog(
            resolved = resolved,
            moveNames = moveNames,
            bodyweightKg = bodyweightKg,
            onLevel = viewModel::setLevelForSelection,
            onStart = { viewModel.start(resolved) },
            onDismiss = viewModel::close
        )
    }
}

// ---------------------------------------------------------------- pieces

@Composable
private fun LevelSwitch(current: BodyweightLevel, onSelect: (BodyweightLevel) -> Unit) {
    SystemPanel(title = "Difficulty", accent = SparkColors.Amber) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BodyweightLevel.entries.forEach { level ->
                val selected = level == current
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (selected) SparkColors.Amber.copy(alpha = 0.18f)
                            else SparkColors.PanelHigh
                        )
                        .clickable { onSelect(level) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            level.shortLabel,
                            color = if (selected) SparkColors.Amber else SparkColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "${level.rounds} rounds",
                            color = SparkColors.TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusFilter(current: BodyweightFocus?, onSelect: (BodyweightFocus?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            SystemChip(
                "All",
                filled = current == null,
                accent = SparkColors.Cyan,
                modifier = Modifier.clickable { onSelect(null) }
            )
        }
        items(BodyweightFocus.entries) { focus ->
            SystemChip(
                focus.displayName,
                filled = current == focus,
                accent = focus.accent(),
                modifier = Modifier.clickable { onSelect(focus) }
            )
        }
    }
}

@Composable
private fun FeaturedCircuit(
    resolved: ResolvedCircuit,
    bodyweightKg: Double,
    onOpen: () -> Unit
) {
    val accent = resolved.circuit.focus.accent()
    SystemPanel(
        title = "Today's pick",
        accent = accent,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)
    ) {
        Text(
            resolved.circuit.name,
            style = MaterialTheme.typography.titleLarge,
            color = SparkColors.TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            resolved.circuit.tagline,
            style = MaterialTheme.typography.bodySmall,
            color = SparkColors.TextSecondary
        )
        Spacer(Modifier.height(12.dp))
        CircuitMetrics(resolved, bodyweightKg, accent)
        Spacer(Modifier.height(14.dp))
        SystemButton(
            "View circuit",
            onOpen,
            accent = accent,
            icon = Icons.Filled.PlayArrow,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CircuitCard(
    resolved: ResolvedCircuit,
    bodyweightKg: Double,
    onClick: () -> Unit
) {
    val accent = resolved.circuit.focus.accent()
    SystemPanel(
        accent = accent,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                resolved.circuit.name,
                style = MaterialTheme.typography.titleMedium,
                color = SparkColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            SystemChip(resolved.circuit.focus.displayName, accent = accent)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            resolved.circuit.tagline,
            style = MaterialTheme.typography.bodySmall,
            color = SparkColors.TextMuted
        )
        Spacer(Modifier.height(10.dp))
        CircuitMetrics(resolved, bodyweightKg, accent)
    }
}

@Composable
private fun CircuitMetrics(resolved: ResolvedCircuit, bodyweightKg: Double, accent: Color) {
    val kcal = BodyweightEngine.caloriesFor(resolved, bodyweightKg)
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Metric(Icons.Filled.AccessTime, "${resolved.estimatedMinutes} min", accent)
        Metric(Icons.Filled.Repeat, "${resolved.rounds} rounds", accent)
        Metric(Icons.Filled.LocalFireDepartment, "~$kcal kcal", accent)
        Metric(Icons.Filled.Bolt, "${resolved.moves.size} moves", accent)
    }
}

@Composable
private fun Metric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    accent: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = SparkColors.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun CircuitDetailDialog(
    resolved: ResolvedCircuit,
    moveNames: Map<String, String>,
    bodyweightKg: Double,
    onLevel: (BodyweightLevel) -> Unit,
    onStart: () -> Unit,
    onDismiss: () -> Unit
) {
    val accent = resolved.circuit.focus.accent()
    Dialog(onDismissRequest = onDismiss) {
        SystemPanel(accent = accent, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        resolved.circuit.focus.displayName.uppercase(),
                        style = SystemLabel.copy(color = accent)
                    )
                    Text(
                        resolved.circuit.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = SparkColors.TextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = SparkColors.TextMuted)
                }
            }

            Spacer(Modifier.height(8.dp))
            CircuitMetrics(resolved, bodyweightKg, accent)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BodyweightLevel.entries.forEach { level ->
                    SystemChip(
                        level.displayName,
                        accent = SparkColors.Amber,
                        filled = level == resolved.level,
                        modifier = Modifier.clickable { onLevel(level) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "${resolved.rounds} rounds · ${resolved.restSeconds} s rest between rounds",
                style = SystemLabel.copy(color = SparkColors.TextSecondary)
            )
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                resolved.moves.forEachIndexed { index, move ->
                    MoveRow(
                        index = index + 1,
                        name = moveNames[move.move.exerciseSlug] ?: move.move.exerciseSlug,
                        move = move,
                        accent = accent
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SystemButton(
                "Start circuit",
                onStart,
                accent = accent,
                icon = Icons.Filled.PlayArrow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MoveRow(index: Int, name: String, move: ResolvedMove, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(SparkColors.PanelHigh)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$index",
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.width(20.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(name, color = SparkColors.TextPrimary, fontSize = 13.sp)
            if (move.move.note.isNotBlank()) {
                Text(move.move.note, color = SparkColors.TextMuted, fontSize = 10.sp)
            }
        }
        Text(
            move.prescription,
            color = SparkColors.TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

/** One colour per focus, so the list reads at a glance rather than as a wall of cyan. */
private fun BodyweightFocus.accent(): Color = when (this) {
    BodyweightFocus.FULL_BODY -> SparkColors.Cyan
    BodyweightFocus.CORE -> SparkColors.Violet
    BodyweightFocus.UPPER -> SparkColors.Amber
    BodyweightFocus.LOWER -> SparkColors.Success
    BodyweightFocus.CARDIO -> SparkColors.Danger
    BodyweightFocus.COMBAT -> SparkColors.HeatHigh
    BodyweightFocus.MOBILITY -> SparkColors.HeatMid
    BodyweightFocus.LOW_IMPACT -> SparkColors.TextSecondary
}
