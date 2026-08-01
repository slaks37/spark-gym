package com.sparkgym.ui.hunter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.core.design.RankColor
import com.sparkgym.core.design.SegmentedBar
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.StatTile
import com.sparkgym.core.design.SystemButton
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.compactVolume
import com.sparkgym.domain.model.Attribute
import com.sparkgym.domain.model.HunterProfile
import com.sparkgym.ui.common.ProfileAvatar
import com.sparkgym.ui.common.SystemMessageDialog
import com.sparkgym.ui.heatmap.MuscleHeatMap
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun StatusScreen(
    viewModel: HunterViewModel,
    onOpenAchievements: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenConnect: () -> Unit,
    onOpenHeatmap: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val advice by viewModel.advice.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val hunter = state.hunter

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(
                        uri = state.profile.avatarUri,
                        name = hunter?.name ?: state.profile.name,
                        size = 46.dp,
                        accent = hunter?.let { RankColor.valueOf(it.rank.name).color } ?: SparkColors.Cyan,
                        onClick = onOpenProfile
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            hunter?.name ?: state.profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = SparkColors.TextPrimary
                        )
                        hunter?.let {
                            Text(
                                "${it.rank.label} · Lv ${it.level}",
                                style = SystemLabel.copy(color = RankColor.valueOf(it.rank.name).color)
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = { viewModel.sync() }) {
                        Icon(Icons.Filled.Sync, "Sync wearable", tint = SparkColors.TextSecondary)
                    }
                    IconButton(onClick = onOpenConnect) {
                        Icon(Icons.Filled.Watch, "Connections", tint = SparkColors.TextSecondary)
                    }
                    IconButton(onClick = onOpenProfile) {
                        Icon(Icons.Filled.Settings, "Settings", tint = SparkColors.TextSecondary)
                    }
                }
            }
        }

        if (hunter != null) {
            item { LevelPanel(hunter) }
            item { AttributePanel(hunter, onSpend = viewModel::spendPoint) }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = state.today?.steps?.toString() ?: "—",
                    label = "Steps",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.DirectionsWalk
                )
                StatTile(
                    value = "${state.macros.calories.roundToInt()}",
                    label = "Kcal in",
                    modifier = Modifier.weight(1f),
                    accent = SparkColors.Amber
                )
                StatTile(
                    value = state.today?.activeCalories?.toString() ?: "—",
                    label = "Kcal out",
                    modifier = Modifier.weight(1f),
                    accent = SparkColors.Danger,
                    icon = Icons.Filled.LocalFireDepartment
                )
            }
        }

        item {
            SystemPanel(
                title = "Daily quest board",
                accent = if (state.boardCleared) SparkColors.Success else SparkColors.Amber
            ) {
                Text(
                    "${state.questsCompleted} of ${state.quests.size} complete",
                    color = SparkColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(10.dp))
                SegmentedBar(
                    progress = if (state.quests.isEmpty()) 0f
                    else state.questsCompleted.toFloat() / state.quests.size,
                    color = if (state.boardCleared) SparkColors.Success else SparkColors.Amber,
                    segments = state.quests.size.coerceAtLeast(1)
                )
                if (state.boardCleared) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Board cleared. The System is satisfied.",
                        color = SparkColors.Success,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            SystemPanel(
                title = "Muscle heat map",
                accent = SparkColors.Violet,
                trailing = {
                    Text(
                        "Balance ${state.balanceScore}",
                        style = SystemLabel.copy(color = SparkColors.Violet)
                    )
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MuscleHeatMap(
                        heat = state.heat,
                        isFront = true,
                        modifier = Modifier.height(150.dp)
                    )
                    MuscleHeatMap(
                        heat = state.heat,
                        isFront = false,
                        modifier = Modifier.height(150.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            advice.ifBlank { "Log a session to light up the map." },
                            color = SparkColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(10.dp))
                        SystemButton("Open map", onOpenHeatmap, accent = SparkColors.Violet)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    value = state.workoutsCompleted.toString(),
                    label = "Gates cleared",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "${hunter?.currentStreak ?: 0}d",
                    label = "Streak",
                    modifier = Modifier.weight(1f),
                    accent = SparkColors.Amber
                )
                StatTile(
                    value = state.heat.values.sumOf { it.volumeKg }.compactVolume(),
                    label = "Week volume",
                    modifier = Modifier.weight(1f),
                    accent = SparkColors.Violet
                )
            }
        }

        item {
            SystemButton(
                text = "Achievements",
                onClick = onOpenAchievements,
                icon = Icons.Filled.EmojiEvents,
                accent = SparkColors.Amber,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    message?.let {
        SystemMessageDialog(
            title = it.title,
            lines = it.lines,
            accent = if (it.accentViolet) SparkColors.Violet else SparkColors.Cyan,
            onDismiss = viewModel::dismissMessage
        )
    }
}

@Composable
private fun LevelPanel(hunter: HunterProfile) {
    val rankColor = RankColor.valueOf(hunter.rank.name).color
    SystemPanel(accent = rankColor) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(62.dp)
                    .background(rankColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        hunter.rank.label,
                        color = rankColor,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text("RANK", style = SystemLabel.copy(color = rankColor, fontSize = 8.sp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "LEVEL ${hunter.level}",
                        style = MaterialTheme.typography.titleMedium,
                        color = SparkColors.TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    SystemChip(hunter.hunterClass.displayName, accent = SparkColors.Violet)
                }
                Spacer(Modifier.height(8.dp))
                SegmentedBar(progress = hunter.levelProgress, color = rankColor)
                Spacer(Modifier.height(6.dp))
                Text(
                    "${hunter.xpIntoLevel} / ${hunter.xpForNextLevel} XP",
                    style = SystemLabel,
                    color = SparkColors.TextMuted
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            hunter.hunterClass.description,
            color = SparkColors.TextMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun AttributePanel(hunter: HunterProfile, onSpend: (Attribute) -> Unit) {
    SystemPanel(
        title = "Attributes",
        trailing = {
            if (hunter.unspentPoints > 0) {
                SystemChip("${hunter.unspentPoints} points", accent = SparkColors.Amber, filled = true)
            } else {
                Text("PWR ${hunter.power}", style = SystemLabel.copy(color = SparkColors.Cyan))
            }
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AttributeRadar(
                values = hunter.attributes,
                modifier = Modifier.width(150.dp).aspectRatio(1f)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Attribute.entries.forEach { attr ->
                    AttributeRow(
                        attribute = attr,
                        value = hunter.attributes[attr] ?: 10,
                        canSpend = hunter.unspentPoints > 0,
                        onSpend = { onSpend(attr) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttributeRow(
    attribute: Attribute,
    value: Int,
    canSpend: Boolean,
    onSpend: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            attribute.short,
            style = SystemLabel.copy(color = SparkColors.TextSecondary),
            modifier = Modifier.width(30.dp)
        )
        Box(Modifier.weight(1f)) {
            SegmentedBar(
                progress = value / 99f,
                segments = 12,
                height = 6.dp,
                color = SparkColors.Cyan
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            value.toString(),
            color = SparkColors.TextPrimary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.End
        )
        if (canSpend) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Increase ${attribute.displayName}",
                tint = SparkColors.Amber,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(16.dp)
                    .clickable(onClick = onSpend)
            )
        }
    }
}

/** Six-spoke radar. The shape is the point — you read imbalance at a glance. */
@Composable
fun AttributeRadar(
    values: Map<Attribute, Int>,
    modifier: Modifier = Modifier,
    accent: Color = SparkColors.Cyan
) {
    val order = Attribute.entries
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f * 0.82f
        val count = order.size

        fun point(index: Int, ratio: Float): Offset {
            val angle = (-Math.PI / 2 + 2 * Math.PI * index / count).toFloat()
            return Offset(
                center.x + cos(angle) * radius * ratio,
                center.y + sin(angle) * radius * ratio
            )
        }

        // Web rings
        listOf(0.25f, 0.5f, 0.75f, 1f).forEach { ring ->
            val path = Path()
            for (i in 0 until count) {
                val p = point(i, ring)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            path.close()
            drawPath(path, SparkColors.Divider.copy(alpha = 0.55f), style = Stroke(width = 1f))
        }

        // Spokes
        for (i in 0 until count) {
            drawLine(SparkColors.Divider.copy(alpha = 0.4f), center, point(i, 1f), strokeWidth = 1f)
        }

        // Value polygon
        val shape = Path()
        order.forEachIndexed { i, attr ->
            val ratio = ((values[attr] ?: 10) / 99f).coerceIn(0.05f, 1f)
            val p = point(i, ratio)
            if (i == 0) shape.moveTo(p.x, p.y) else shape.lineTo(p.x, p.y)
        }
        shape.close()
        drawPath(shape, accent.copy(alpha = 0.22f))
        drawPath(shape, accent, style = Stroke(width = 2f))

        order.forEachIndexed { i, attr ->
            val ratio = ((values[attr] ?: 10) / 99f).coerceIn(0.05f, 1f)
            drawCircle(accent, radius = 3f, center = point(i, ratio))
        }
    }
}
