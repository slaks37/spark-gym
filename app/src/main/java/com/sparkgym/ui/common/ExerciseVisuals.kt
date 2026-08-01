package com.sparkgym.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparkgym.core.design.CutCornerAngularShape
import com.sparkgym.core.design.SparkColors
import com.sparkgym.domain.model.Equipment
import com.sparkgym.domain.model.Muscle
import com.sparkgym.domain.model.MuscleGroup
import com.sparkgym.ui.heatmap.MuscleThumb

/** One glyph per equipment type, so the kit is readable without reading. */
fun equipmentIcon(equipment: Equipment): ImageVector = when (equipment) {
    Equipment.BARBELL, Equipment.EZ_BAR, Equipment.SMITH -> Icons.Filled.ViewWeek
    Equipment.DUMBBELL, Equipment.KETTLEBELL -> Icons.Filled.FitnessCenter
    Equipment.MACHINE, Equipment.CABLE -> Icons.Filled.Settings
    Equipment.CARDIO -> Icons.AutoMirrored.Filled.DirectionsRun
    Equipment.BAND -> Icons.Filled.Link
    else -> Icons.Filled.Accessibility
}

/** Push / pull / legs / core each get their own hue, used everywhere. */
fun muscleGroupColor(group: MuscleGroup): Color = when (group) {
    MuscleGroup.PUSH -> SparkColors.Danger
    MuscleGroup.PULL -> SparkColors.Cyan
    MuscleGroup.LEGS -> SparkColors.Violet
    MuscleGroup.CORE -> SparkColors.Amber
}

/**
 * The library row.
 *
 * Deliberately picture-first: the anatomical thumbnail carries the meaning and
 * the text is reduced to a name and two or three coloured muscle chips. The old
 * version spelled out every muscle in a grey sentence, which nobody reads while
 * standing in a gym.
 */
@Composable
fun ExerciseCard(
    name: String,
    equipment: Equipment,
    primary: Set<Muscle>,
    secondary: Set<Muscle>,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    trailingLabel: String? = null,
    onFavorite: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val shape = CutCornerAngularShape(10f)
    val accent = primary.firstOrNull()?.let { muscleGroupColor(it.group) } ?: SparkColors.Cyan

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SparkColors.Panel)
            .border(1.dp, accent.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The picture of the movement.
        Box(
            Modifier
                .size(width = 58.dp, height = 62.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.07f))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            MuscleThumb(
                primary = primary,
                secondary = secondary,
                modifier = Modifier.height(54.dp),
                primaryColor = accent
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                color = SparkColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    equipmentIcon(equipment),
                    contentDescription = equipment.displayName,
                    tint = SparkColors.TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                primary.take(2).forEach { MuscleDot(it) }
                if (primary.size > 2) {
                    Text(
                        "+${primary.size - 2}",
                        fontSize = 9.sp,
                        color = SparkColors.TextMuted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        trailingLabel?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
        }

        onFavorite?.let {
            Icon(
                if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Favourite",
                tint = if (isFavorite) SparkColors.Amber else SparkColors.TextMuted,
                modifier = Modifier.size(20.dp).clickable(onClick = it)
            )
        }
    }
}

/** A muscle name as a tiny coloured tag rather than another word in a sentence. */
@Composable
fun MuscleDot(muscle: Muscle, modifier: Modifier = Modifier) {
    val c = muscleGroupColor(muscle.group)
    Box(
        modifier
            .clip(RoundedCornerShape(2.dp))
            .background(c.copy(alpha = 0.14f))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(
            muscle.displayName,
            fontSize = 9.sp,
            color = c,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
