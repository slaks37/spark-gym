package com.sparkgym.ui.quests

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.SegmentedBar
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.domain.model.DailyQuest
import com.sparkgym.domain.model.QuestMetric
import com.sparkgym.domain.model.QuestSource
import com.sparkgym.ui.common.SystemMessageDialog
import com.sparkgym.ui.hunter.HunterViewModel
import kotlin.math.roundToInt

@Composable
fun QuestScreen(viewModel: HunterViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("DAILY QUEST", style = SystemLabel.copy(color = SparkColors.Amber))
                Text(
                    "Complete before midnight",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SparkColors.TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Failure to complete the daily quest will result in an appropriate penalty.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextMuted
                )
            }
        }

        item {
            val core = state.coreQuests
            SystemPanel(
                title = "Progress",
                accent = if (state.boardCleared) SparkColors.Success else SparkColors.Amber
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${state.questsCompleted}/${state.quests.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SparkColors.TextPrimary
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        SegmentedBar(
                            progress = if (state.quests.isEmpty()) 0f
                            else state.questsCompleted.toFloat() / state.quests.size,
                            color = if (state.boardCleared) SparkColors.Success else SparkColors.Amber,
                            segments = state.quests.size.coerceAtLeast(1)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (core.all { it.completed } && core.isNotEmpty()) {
                                "Core quests cleared — streak secured"
                            } else {
                                "${core.count { !it.completed }} core quests remaining"
                            },
                            style = SystemLabel,
                            color = SparkColors.TextMuted
                        )
                    }
                }
            }
        }

        item { SectionLabel("Core — required") }
        items(state.coreQuests, key = { it.id }) { quest ->
            QuestCard(quest) { delta -> viewModel.incrementQuest(quest, delta) }
        }

        item { SectionLabel("Support — bonus XP") }
        items(state.supportQuests, key = { it.id }) { quest ->
            QuestCard(quest) { delta -> viewModel.incrementQuest(quest, delta) }
        }

        item {
            Text(
                "Automatic quests fill in from your workout log, your food diary and your watch. " +
                    "Manual quests are yours to tap.",
                style = MaterialTheme.typography.bodySmall,
                color = SparkColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        }
    }

    message?.let {
        SystemMessageDialog(
            title = it.title,
            lines = it.lines,
            accent = SparkColors.Success,
            onDismiss = viewModel::dismissMessage
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = SystemLabel.copy(color = SparkColors.TextSecondary),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun QuestCard(quest: DailyQuest, onIncrement: (Double) -> Unit) {
    val accent = when {
        quest.completed -> SparkColors.Success
        quest.isCore -> SparkColors.Amber
        else -> SparkColors.Cyan
    }

    SystemPanel(accent = accent, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        quest.metric.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = SparkColors.TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    if (quest.completed) {
                        SystemChip("done", accent = SparkColors.Success, filled = true)
                    } else {
                        SystemChip("+${quest.xpReward} xp", accent = accent)
                    }
                }
                Spacer(Modifier.height(8.dp))
                SegmentedBar(progress = quest.ratio, color = accent, segments = 20, height = 8.dp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "${format(quest.progress)} / ${format(quest.target)} ${quest.metric.unit}",
                    style = SystemLabel,
                    color = SparkColors.TextMuted
                )
            }

            if (quest.source == QuestSource.MANUAL && !quest.completed) {
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StepButton("+${stepSize(quest.metric).roundToInt()}", accent) {
                        onIncrement(stepSize(quest.metric))
                    }
                    Spacer(Modifier.height(6.dp))
                    StepButton("+1", accent) { onIncrement(1.0) }
                }
            } else if (quest.completed) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = SparkColors.Success,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun StepButton(label: String, accent: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(accent.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(2.dp))
            Text(label.removePrefix("+"), color = accent, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Sensible bulk-increment per metric — nobody taps +1 a hundred times. */
private fun stepSize(metric: QuestMetric): Double = when (metric) {
    QuestMetric.PUSHUPS, QuestMetric.SITUPS, QuestMetric.SQUATS -> 10.0
    QuestMetric.WATER_ML -> 250.0
    QuestMetric.STEPS -> 500.0
    QuestMetric.PROTEIN_G -> 10.0
    else -> 5.0
}

private fun format(value: Double): String =
    if (value >= 1000) "${(value / 1000).let { String.format(java.util.Locale.US, "%.1f", it) }}k"
    else value.roundToInt().toString()
