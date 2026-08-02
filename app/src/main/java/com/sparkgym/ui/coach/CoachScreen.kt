package com.sparkgym.ui.coach

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.R
import com.sparkgym.core.design.SegmentedBar
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SparkDimens
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.S
import com.sparkgym.domain.engine.CoachEngine
import kotlin.math.roundToInt

@Composable
fun CoachScreen(viewModel: CoachViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = SparkDimens.screen,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // weight(1f) on the text block, so a long headline wraps inside
                // the space left over instead of being crushed by the button.
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ai_coach_avatar),
                        contentDescription = "AI Coach Avatar",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(S.coach.uppercase(), style = SystemLabel.copy(color = SparkColors.Cyan))
                        Text(
                            S.yourDataReadBackToYou,
                            style = MaterialTheme.typography.titleMedium,
                            color = SparkColors.TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(onClick = viewModel::refresh) {
                    Icon(Icons.Filled.Refresh, "Re-analyse", tint = SparkColors.TextSecondary)
                }
            }
        }

        if (state.loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SparkColors.Cyan)
                }
            }
            return@LazyColumn
        }

        item {
            SystemPanel(accent = SparkColors.Cyan) {
                Text(S.headline, style = SystemLabel.copy(color = SparkColors.Cyan))
                Spacer(Modifier.height(6.dp))
                Text(
                    state.headline,
                    style = MaterialTheme.typography.titleMedium,
                    color = SparkColors.TextPrimary
                )
            }
        }

        state.snapshot?.let { snap ->
            item { WeekSummary(snap) }
        }

        state.deload?.let { verdict ->
            if (verdict.score > 0) {
                item {
                    SystemPanel(
                        title = S.fatigueIndex,
                        accent = if (verdict.recommended) SparkColors.Danger else SparkColors.Amber
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${verdict.score}/9",
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (verdict.recommended) SparkColors.Danger else SparkColors.Amber
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                SegmentedBar(
                                    progress = verdict.score / 9f,
                                    color = if (verdict.recommended) SparkColors.Danger else SparkColors.Amber,
                                    segments = 9
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    if (verdict.recommended) S.deloadRecommended
                                    else S.accumulatingWorthWatching,
                                    style = SystemLabel,
                                    color = SparkColors.TextMuted
                                )
                            }
                        }
                        if (verdict.reasons.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            verdict.reasons.forEach {
                                Text(
                                    "· $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SparkColors.TextSecondary,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.actionable.isNotEmpty()) {
            item {
                Text(
                    S.whatToChange,
                    style = SystemLabel.copy(color = SparkColors.TextSecondary),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(state.actionable) { insight ->
                InsightCard(insight)
            }
        }

        if (state.wins.isNotEmpty()) {
            item {
                Text(
                    S.whatIsWorking,
                    style = SystemLabel.copy(color = SparkColors.Success),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(state.wins) { insight ->
                InsightCard(insight)
            }
        }

        item {
            Text(
                S.coachDisclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = SparkColors.TextMuted,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun WeekSummary(snap: CoachEngine.Snapshot) {
    SystemPanel(title = S.thisWeek, accent = SparkColors.Violet) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Metric("${snap.sessionsThisWeek}", S.sessions)
            Metric("${snap.weeklyEffectiveSets.roundToInt()}", S.effSets)
            Metric(
                if (snap.avgDailySteps > 0) "${(snap.avgDailySteps / 1000).roundToInt()}k" else "—",
                S.stepsPerDay
            )
            Metric(
                if (snap.avgSleepMinutes > 0) "${(snap.avgSleepMinutes / 60).roundToInt()}h" else "—",
                S.sleep
            )
            Metric("${snap.daysLoggedThisWeek}/7", S.food)
        }
        if (snap.weightTrendKgPerWeek != null) {
            Spacer(Modifier.height(12.dp))
            val trend = snap.weightTrendKgPerWeek
            Text(
                "${S.bodyweightTrending} ${if (trend >= 0) "+" else "−"}" +
                    String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(trend)) + " kg ${S.perWeek}",
                style = MaterialTheme.typography.bodySmall,
                color = SparkColors.TextSecondary
            )
        }
    }
}

@Composable
private fun Metric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = SparkColors.TextPrimary)
        Text(label.uppercase(), style = SystemLabel)
    }
}

@Composable
private fun InsightCard(insight: CoachEngine.Insight) {
    val accent = accentFor(insight.priority)
    SystemPanel(modifier = Modifier.fillMaxWidth(), accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                when (insight.priority) {
                    CoachEngine.Priority.PRAISE -> Icons.Filled.CheckCircle
                    CoachEngine.Priority.CRITICAL, CoachEngine.Priority.HIGH -> Icons.Filled.Warning
                    else -> Icons.Filled.Bolt
                },
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                insight.title,
                style = MaterialTheme.typography.titleSmall,
                color = SparkColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            SystemChip(insight.category.label, accent = accent)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            insight.body,
            style = MaterialTheme.typography.bodyMedium,
            color = SparkColors.TextSecondary
        )
        insight.action?.let {
            Spacer(Modifier.height(10.dp))
            Row {
                Box(Modifier.width(3.dp).height(38.dp).background(accent))
                Spacer(Modifier.width(10.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = accent
                )
            }
        }
    }
}

private fun accentFor(priority: CoachEngine.Priority): Color = when (priority) {
    CoachEngine.Priority.CRITICAL -> SparkColors.Danger
    CoachEngine.Priority.HIGH -> SparkColors.Amber
    CoachEngine.Priority.MEDIUM -> SparkColors.Cyan
    CoachEngine.Priority.LOW -> SparkColors.Violet
    CoachEngine.Priority.PRAISE -> SparkColors.Success
}
