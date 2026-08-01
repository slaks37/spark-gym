package com.sparkgym.ui.hunter

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sparkgym.R
import com.sparkgym.core.design.SparkColors
import com.sparkgym.core.design.SystemChip
import com.sparkgym.core.design.SystemLabel
import com.sparkgym.core.design.SystemPanel
import com.sparkgym.core.util.Dates
import com.sparkgym.domain.model.Achievement

@Composable
fun AchievementsScreen(viewModel: HunterViewModel, onBack: () -> Unit) {
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val unlocked = achievements.count { it.unlocked }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SparkColors.Void),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SparkColors.TextSecondary)
                }
                Column {
                    Text("ACHIEVEMENTS", style = SystemLabel.copy(color = SparkColors.Amber))
                    Text(
                        "$unlocked of ${achievements.size} unlocked",
                        style = MaterialTheme.typography.titleMedium,
                        color = SparkColors.TextPrimary
                    )
                }
            }
        }

        items(achievements, key = { it.key }) { achievement ->
            AchievementRow(achievement)
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement) {
    val accent = if (achievement.unlocked) SparkColors.Amber else SparkColors.Divider
    SystemPanel(accent = accent, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val iconRes = badgeFor(achievement.key)
            if (achievement.unlocked) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = SparkColors.TextMuted,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    achievement.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (achievement.unlocked) SparkColors.TextPrimary else SparkColors.TextMuted
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SparkColors.TextMuted
                )
                achievement.unlockedAt?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Unlocked ${Dates.pretty(Dates.epochDay(it))}",
                        style = SystemLabel.copy(color = SparkColors.Success)
                    )
                }
            }
            SystemChip("+${achievement.xpReward}", accent = accent)
        }
    }
}

/**
 * Badge art per achievement. The keys have to match AchievementCatalog exactly —
 * an earlier version invented its own ("first_workout", "streak_7") and every
 * achievement silently fell through to the same fallback image.
 */
@DrawableRes
private fun badgeFor(key: String): Int = when (key) {
    "first-blood", "ten-gates", "fifty-gates", "hundred-gates" -> R.drawable.badge_first_workout
    "streak-7", "streak-30", "streak-100" -> R.drawable.badge_streak_seven
    "tonnage-10k", "tonnage-100k", "tonnage-million", "record-breaker" -> R.drawable.badge_iron_lifter
    "meal-planner", "nutritionist" -> R.drawable.badge_water_master
    else -> R.drawable.badge_first_workout
}
