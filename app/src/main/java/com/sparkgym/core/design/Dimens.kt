package com.sparkgym.core.design

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/**
 * Shared spacing, so every screen breathes the same way.
 *
 * Screens used to each pick their own number — 12, 14, 16, 20 — which is why
 * the app read as slightly misaligned when you moved between tabs. One value
 * here means the left edge of a card on Coach lines up with the left edge of a
 * card on Quests.
 */
object SparkDimens {
    /** Distance from the screen edge to any content. */
    val ScreenH = 20.dp

    /** Breathing room above the first row and below the last. */
    val ScreenTop = 12.dp
    val ScreenBottom = 28.dp

    /** Gap between stacked cards. */
    val CardGap = 14.dp

    /** The standard content padding for a screen-level LazyColumn. */
    val screen = PaddingValues(
        start = ScreenH,
        end = ScreenH,
        top = ScreenTop,
        bottom = ScreenBottom
    )
}
